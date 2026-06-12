package io.github.vivitoto.vanga

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import io.github.vivitoto.vanga.api.RemoteActuatorApi
import io.github.vivitoto.vanga.api.RemoteAnnouncementsApi
import io.github.vivitoto.vanga.api.RemoteApi
import io.github.vivitoto.vanga.api.RemoteBookApi
import io.github.vivitoto.vanga.api.RemoteCollectionsApi
import io.github.vivitoto.vanga.api.RemoteFileSystemApi
import io.github.vivitoto.vanga.api.RemoteLibraryApi
import io.github.vivitoto.vanga.api.RemoteReadListApi
import io.github.vivitoto.vanga.api.RemoteReferentialApi
import io.github.vivitoto.vanga.api.RemoteSeriesApi
import io.github.vivitoto.vanga.api.RemoteSettingsApi
import io.github.vivitoto.vanga.api.RemoteTaskApi
import io.github.vivitoto.vanga.api.RemoteUserApi
import io.github.vivitoto.vanga.http.RememberMePersistingCookieStore
import io.github.vivitoto.vanga.image.BookImageLoader
import io.github.vivitoto.vanga.image.VangaImageDecoder
import io.github.vivitoto.vanga.image.ReaderImageFactory
import io.github.vivitoto.vanga.image.coil.CoilAwareDecoder
import io.github.vivitoto.vanga.image.coil.CoilDecoder
import io.github.vivitoto.vanga.image.coil.FileMapper
import io.github.vivitoto.vanga.image.coil.VangaFetcherFactory
import io.github.vivitoto.vanga.image.processing.CropBordersStep
import io.github.vivitoto.vanga.image.processing.ImageProcessingPipeline
import io.github.vivitoto.vanga.komga.api.KomgaApi
import io.github.vivitoto.vanga.komga.api.KomgaBookApi
import io.github.vivitoto.vanga.offline.OfflineDependencies
import io.github.vivitoto.vanga.offline.OfflineModule
import io.github.vivitoto.vanga.offline.OfflineRepositories
import io.github.vivitoto.vanga.settings.ImageReaderSettingsRepository
import io.github.vivitoto.vanga.ui.DependencyContainer
import io.github.vivitoto.vanga.ui.strings.EnStrings
import io.github.vivitoto.vanga.updates.AppUpdater
import io.github.vivitoto.vanga.updates.UpdateClient
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUser
import kotlin.time.measureTimedValue

private val logger = KotlinLogging.logger { }

abstract class AppModule {
    protected val initScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    protected val appNotifications = AppNotifications()

    suspend fun initDependencies(): DependencyContainer {
        beforeInit()
        val appRepositories = createAppRepositories()
        val offlineRepositories = createOfflineRepositories()
        val ktor = createKtorClient()
        val ktorWithoutCache = createKtorClientWithoutCache()

        val updateClient = UpdateClient(
            ktor = ktor.config {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            },
            ktorWithoutCache = ktorWithoutCache.config {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        )

        val baseUrl = appRepositories.settingsRepository.getServerUrl().stateIn(initScope)
        val komfUrl = appRepositories.komfSettingsRepository.getKomfUrl().stateIn(initScope)

        val cookiesStorage = RememberMePersistingCookieStore(
            baseUrl.map { Url(it) }.stateIn(initScope),
            appRepositories.secretsRepository
        )
        cookiesStorage.loadRememberMeCookie()

        val komgaClientFactory = KomgaClientFactory.Builder()
            .ktor(ktor)
            .baseUrl { baseUrl.value }
            .cookieStorage(cookiesStorage)
            .build()

        val komgaClientFactoryNoCache = KomgaClientFactory.Builder()
            .ktor(ktor)
            .baseUrl { baseUrl.value }
            .cookieStorage(cookiesStorage)
            .build()

        val komfClientFactory = KomfClientFactory.Builder()
            .baseUrl { komfUrl.value }
            .ktor(ktor)
            .build()

        val imageDecoder = createImageDecoder()

        val isOffline = offlineRepositories.offlineSettingsRepository.getOfflineMode().stateIn(initScope)
        val currentUserFlow = MutableStateFlow<KomgaUser?>(null)
        val currentServerUrl = appRepositories.settingsRepository.getServerUrl().stateIn(initScope)

        val androidContext = createCoilContext()
        val offlineModule: OfflineDependencies = createOfflineModule(
            repositories = offlineRepositories,
            komgaClientFactory = komgaClientFactory,
            onlineUser = currentUserFlow
                .combine(isOffline) { user, isOffline -> if (isOffline) null else user }
                .stateIn(initScope),
            onlineServerUrl = appRepositories.settingsRepository.getServerUrl().stateIn(initScope),
            isOffline = isOffline,
        ).initDependencies()

        val komgaApi = isOffline.map { offline ->
            if (offline) offlineModule.komgaApi
            else createRemoteApi(
                komgaClientFactory = komgaClientFactory,
                offlineRepositories = offlineRepositories,
                offlineEvents = offlineModule.komgaEvents
            )
        }.stateIn(initScope)

        val komgaNoRemoteCacheApi = isOffline.map { offline ->
            if (offline) offlineModule.komgaApi
            else createRemoteApi(
                komgaClientFactory = komgaClientFactoryNoCache,
                offlineRepositories = offlineRepositories,
                offlineEvents = offlineModule.komgaEvents
            )
        }.stateIn(initScope)

        val komgaSharedState = KomgaAuthenticationState(
            userApi = komgaApi.map { it.userApi }.stateIn(initScope),
            libraryApi = komgaApi.map { it.libraryApi }.stateIn(initScope),
            currentUserFlow = currentUserFlow,
            serverUrl = currentServerUrl
        )

        val imagePipeline = createImagePipeline(
            cropBorders = appRepositories.imageReaderSettingsRepository.getCropBorders().stateIn(initScope)
        )
        val coil = createCoil(
            komgaApi = komgaApi,
            context = androidContext,
            decoder = imageDecoder,
        )

        val komgaEvents = ManagedKomgaEvents(
            komgaApi = komgaApi,
            memoryCache = coil.memoryCache,
            diskCache = coil.diskCache,
            libraryApi = komgaApi.map { it.libraryApi },
            komgaSharedState = komgaSharedState
        )

        val readerImageFactory = createReaderImageFactory(
            imageDecoder = imageDecoder,
            pipeline = imagePipeline,
            settings = appRepositories.imageReaderSettingsRepository,
        )

        return DependencyContainer(
            appStrings = MutableStateFlow(EnStrings),
            appRepositories = appRepositories,

            komgaApi = komgaApi,
            isOffline = isOffline,
            komfClientFactory = komfClientFactory,
            appNotifications = appNotifications,
            komgaSharedState = komgaSharedState,
            komgaEvents = komgaEvents,
            appUpdater = createAppUpdater(updateClient),

            coilContext = androidContext,
            coilImageLoader = coil,
            imageDecoder = imageDecoder,
            bookImageLoader = createReaderImageLoader(
                bookApi = komgaNoRemoteCacheApi.map { it.bookApi }.stateIn(initScope),
                imageFactory = readerImageFactory,
                imageDecoder = createImageDecoder()
            ),
            readerImageFactory = readerImageFactory,
            windowState = createWindowState(),
            offlineDependencies = offlineModule,
        )
    }

    protected open suspend fun beforeInit() = Unit

    protected fun createRemoteApi(
        komgaClientFactory: KomgaClientFactory,
        offlineRepositories: OfflineRepositories,
        offlineEvents: SharedFlow<KomgaEvent>,
    ) = RemoteApi(
        actuatorApi = RemoteActuatorApi(komgaClientFactory.actuatorClient()),
        announcementsApi = RemoteAnnouncementsApi(komgaClientFactory.announcementClient()),
        bookApi = RemoteBookApi(
            bookClient = komgaClientFactory.bookClient(),
            offlineBookRepository = offlineRepositories.bookRepository
        ),
        collectionsApi = RemoteCollectionsApi(komgaClientFactory.collectionClient()),
        fileSystemApi = RemoteFileSystemApi(komgaClientFactory.fileSystemClient()),
        libraryApi = RemoteLibraryApi(komgaClientFactory.libraryClient()),
        readListApi = RemoteReadListApi(
            readListClient = komgaClientFactory.readListClient(),
            offlineBookRepository = offlineRepositories.bookRepository
        ),
        referentialApi = RemoteReferentialApi(komgaClientFactory.referentialClient()),
        seriesApi = RemoteSeriesApi(komgaClientFactory.seriesClient()),
        settingsApi = RemoteSettingsApi(komgaClientFactory.settingsClient()),
        tasksApi = RemoteTaskApi(komgaClientFactory.taskClient()),
        userApi = RemoteUserApi(komgaClientFactory.userClient()),
        komgaClientFactory = komgaClientFactory,
        offlineEvents = offlineEvents
    )

    protected fun createCoil(
        komgaApi: StateFlow<KomgaApi>,
        context: PlatformContext,
        decoder: VangaImageDecoder,
    ): ImageLoader {

        val timed = measureTimedValue {
            val diskCache = getCoilCacheDirectory()?.let { kotlinxPath ->
                DiskCache.Builder()
                    // kotlinx -> okio path
                    .directory(kotlinxPath.toString().toPath())
                    .build()
            }
            diskCache?.clear()
            val coilAwareDecoder = CoilAwareDecoder(decoder)

            ImageLoader.Builder(context)
                .components {
                    add(FileMapper())
                    add(CoilDecoder.Factory(coilAwareDecoder))
                    add(VangaFetcherFactory(komgaApi, coilAwareDecoder))
                }
                .memoryCache(createCoilMemoryCache())
                .diskCache { diskCache }
                .build()
                .also { loader -> SingletonImageLoader.setSafe { loader } }
        }
        logger.info { "initialized Coil in ${timed.duration}" }
        return timed.value
    }

    protected fun createReaderImageLoader(
        bookApi: StateFlow<KomgaBookApi>,
        imageFactory: ReaderImageFactory,
        imageDecoder: VangaImageDecoder
    ): BookImageLoader {
        val diskCache = getReaderCacheDirectory()?.let { kotlinxPath ->
            DiskCache.Builder()
                .directory(kotlinxPath.toString().toPath())
                .build()
        }
        return BookImageLoader(
            bookClient = bookApi,
            readerImageFactory = imageFactory,
            imageDecoder = imageDecoder,
            diskCache = diskCache
        )
    }

    protected fun createImagePipeline(
        cropBorders: StateFlow<Boolean>,
    ): ImageProcessingPipeline {
        val pipeline = ImageProcessingPipeline()
        pipeline.addStep(CropBordersStep(cropBorders))
        return pipeline
    }


    protected abstract suspend fun createAppRepositories(): AppRepositories
    protected abstract suspend fun createOfflineRepositories(): OfflineRepositories
    protected abstract fun createKtorClient(): HttpClient
    protected abstract fun createKtorClientWithoutCache(): HttpClient

    protected abstract fun createAppUpdater(updateClient: UpdateClient): AppUpdater?

    protected abstract fun createImageDecoder(): VangaImageDecoder
    protected abstract suspend fun createReaderImageFactory(
        imageDecoder: VangaImageDecoder,
        pipeline: ImageProcessingPipeline,
        settings: ImageReaderSettingsRepository,
    ): ReaderImageFactory

    protected abstract fun createWindowState(): AppWindowState
    protected abstract fun createCoilContext(): PlatformContext

    protected abstract fun getCoilCacheDirectory(): Path?
    protected abstract fun createCoilMemoryCache(): MemoryCache?
    protected abstract fun getReaderCacheDirectory(): Path?

    protected abstract fun createOfflineModule(
        repositories: OfflineRepositories,
        onlineUser: StateFlow<KomgaUser?>,
        onlineServerUrl: StateFlow<String>,
        isOffline: StateFlow<Boolean>,
        komgaClientFactory: KomgaClientFactory,
    ): OfflineModule
}
