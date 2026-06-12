package io.github.vivitoto.vanga

import coil3.PlatformContext
import coil3.memory.MemoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.files.Path
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import io.github.vivitoto.vanga.db.AppSettings
import io.github.vivitoto.vanga.db.EpubReaderSettings
import io.github.vivitoto.vanga.db.ExposedTransactionTemplate
import io.github.vivitoto.vanga.db.ImageReaderSettings
import io.github.vivitoto.vanga.db.VangaDatabase
import io.github.vivitoto.vanga.db.KomfSettings
import io.github.vivitoto.vanga.db.OfflineSettings
import io.github.vivitoto.vanga.db.SettingsStateWrapper
import io.github.vivitoto.vanga.db.fonts.ExposedUserFontsRepository
import io.github.vivitoto.vanga.db.homescreen.ExposedHomeScreenFilterRepository
import io.github.vivitoto.vanga.db.offline.ExposedLogJournalRepository
import io.github.vivitoto.vanga.db.offline.ExposedMediaRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineBookMetadataAggregationRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineBookMetadataRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineBookRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineLibraryRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineMediaServerRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineReadProgressRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineSeriesMetadataRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineSeriesRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineSettingsRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineTasksRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineThumbnailBookRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineThumbnailSeriesRepository
import io.github.vivitoto.vanga.db.offline.ExposedOfflineUserRepository
import io.github.vivitoto.vanga.db.offline.dto.ExposedOfflineBookDtoRepository
import io.github.vivitoto.vanga.db.offline.dto.ExposedOfflineReferentialRepository
import io.github.vivitoto.vanga.db.offline.dto.ExposedSeriesDtoRepository
import io.github.vivitoto.vanga.db.repository.EpubReaderSettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.repository.HomeScreenFilterRepositoryWrapper
import io.github.vivitoto.vanga.db.repository.KomfSettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.repository.OfflineSettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.repository.ReaderSettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.repository.SettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.settings.ExposedEpubReaderSettingsRepository
import io.github.vivitoto.vanga.db.settings.ExposedImageReaderSettingsRepository
import io.github.vivitoto.vanga.db.settings.ExposedKomfSettingsRepository
import io.github.vivitoto.vanga.db.settings.ExposedSettingsRepository
import io.github.vivitoto.vanga.homefilters.homeScreenDefaultFilters
import io.github.vivitoto.vanga.http.vangaUserAgent
import io.github.vivitoto.vanga.image.DesktopReaderImageFactory
import io.github.vivitoto.vanga.image.VangaImageDecoder
import io.github.vivitoto.vanga.image.ReaderImageFactory
import io.github.vivitoto.vanga.image.SkiaBitmap
import io.github.vivitoto.vanga.image.UpsamplingMode
import io.github.vivitoto.vanga.image.VipsImageDecoder
import io.github.vivitoto.vanga.image.VipsSharedLibraries
import io.github.vivitoto.vanga.image.processing.ImageProcessingPipeline
import io.github.vivitoto.vanga.offline.DesktopOfflineModule
import io.github.vivitoto.vanga.offline.OfflineModule
import io.github.vivitoto.vanga.offline.OfflineRepositories
import io.github.vivitoto.vanga.secrets.AppKeyring
import io.github.vivitoto.vanga.settings.ImageReaderSettingsRepository
import io.github.vivitoto.vanga.settings.KeyringSecretsRepository
import io.github.vivitoto.vanga.ui.error.NonRestartableException
import io.github.vivitoto.vanga.updates.DesktopAppUpdater
import io.github.vivitoto.vanga.updates.UpdateClient
import snd.komga.client.KomgaClientFactory
import snd.komga.client.user.KomgaUser
import io.github.vivitoto.vanga.webview.WebviewSharedLibraries
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.io.path.createDirectories
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

class DesktopAppModule(
    private val windowState: AwtWindowState
) : AppModule() {
    private val databases = VangaDatabase(AppDirectories.databaseDirectory.toString())

    private val okHttpLogger = KotlinLogging.logger("http.logging")
    private val okHttpClientWithoutCache: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor { okHttpLogger.info { it } }
            .setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()
    private val okHttpClient = okHttpClientWithoutCache.newBuilder().cache(
        Cache(
            directory = AppDirectories.okHttpCachePath.createDirectories().toFile(),
            maxSize = 64 * 1024L * 1024L // 64 MiB
        )
    ).build()

    override suspend fun beforeInit() {
        if (DesktopPlatform.Current != DesktopPlatform.Linux) {
            loadVipsLibraries()
            loadWebviewLibraries()
        }
        checkVipsLibraries()
    }


    private fun checkVipsLibraries() {
        VipsSharedLibraries.loadError?.let {
            throw NonRestartableException("Failed to load libvips shared libraries. ${it.message}", it)
        }
        if (!VipsSharedLibraries.isAvailable)
            throw NonRestartableException("libvips shared libraries were not loaded. libvips is required for image decoding")
        SkiaBitmap.load()
    }

    override suspend fun createAppRepositories(): AppRepositories {
        return AppRepositories(
            settingsRepository = ExposedSettingsRepository(databases.app).let { repository ->
                SettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get()
                            ?: AppSettings(),
                        saveSettings = repository::save
                    )
                )
            },
            epubReaderSettingsRepository = ExposedEpubReaderSettingsRepository(databases.app).let { repository ->
                EpubReaderSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get() ?: EpubReaderSettings(),
                        saveSettings = repository::save
                    )
                )
            },
            imageReaderSettingsRepository = ExposedImageReaderSettingsRepository(databases.app).let { repository ->
                ReaderSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get() ?: ImageReaderSettings(upsamplingMode = UpsamplingMode.CATMULL_ROM),
                        saveSettings = repository::save
                    )
                )
            },
            fontsRepository = ExposedUserFontsRepository(databases.app),
            secretsRepository = KeyringSecretsRepository(AppKeyring()),
            komfSettingsRepository = ExposedKomfSettingsRepository(databases.app).let { repository ->
                KomfSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get() ?: KomfSettings(),
                        saveSettings = repository::save
                    )
                )
            },
            homeScreenFilterRepository = ExposedHomeScreenFilterRepository(databases.app).let { repository ->
                HomeScreenFilterRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.getFilters() ?: homeScreenDefaultFilters,
                        saveSettings = repository::putFilters
                    )
                )
            }
        )
    }

    override suspend fun createOfflineRepositories(): OfflineRepositories {
        return OfflineRepositories(
            mediaServerRepository = ExposedOfflineMediaServerRepository(databases.offline),
            mediaRepository = ExposedMediaRepository(databases.offline),
            bookRepository = ExposedOfflineBookRepository(databases.offline),
            bookMetadataRepository = ExposedOfflineBookMetadataRepository(databases.offline),
            bookMetadataAggregationRepository = ExposedOfflineBookMetadataAggregationRepository(databases.offline),
            libraryRepository = ExposedOfflineLibraryRepository(databases.offline),
            readProgressRepository = ExposedOfflineReadProgressRepository(databases.offline),
            seriesMetadataRepository = ExposedOfflineSeriesMetadataRepository(databases.offline),
            seriesRepository = ExposedOfflineSeriesRepository(databases.offline),
            thumbnailBookRepository = ExposedOfflineThumbnailBookRepository(databases.offline),
            thumbnailSeriesRepository = ExposedOfflineThumbnailSeriesRepository(databases.offline),
            userRepository = ExposedOfflineUserRepository(databases.offline),
            tasksRepository = ExposedOfflineTasksRepository(databases.offline),
            logJournalRepository = ExposedLogJournalRepository(databases.offline),
            offlineSettingsRepository = ExposedOfflineSettingsRepository(databases.offline).let { repo ->
                OfflineSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repo.get()
                            ?: OfflineSettings(downloadDirectory = PlatformFile(AppDirectories.defaultOfflineLibraryPath.toString())),
                        saveSettings = repo::save
                    )
                )
            },
            transactionTemplate = ExposedTransactionTemplate(databases.offline),

            bookDtoRepository = ExposedOfflineBookDtoRepository(databases.offlineReadOnly),
            seriesDtoRepository = ExposedSeriesDtoRepository(databases.offlineReadOnly),
            referentialRepository = ExposedOfflineReferentialRepository(databases.offlineReadOnly),
        )
    }

    override fun createKtorClient(): HttpClient {
        return configureKtor(okHttpClient)
    }

    override fun createKtorClientWithoutCache(): HttpClient {
        return configureKtor(okHttpClientWithoutCache)
    }

    private fun configureKtor(okHttpClient: OkHttpClient): HttpClient {
        return HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            expectSuccess = true

            install(UserAgent) {
                agent = vangaUserAgent
            }
            install(HttpTimeout) {
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }

    }

    override fun createAppUpdater(updateClient: UpdateClient) = DesktopAppUpdater(updateClient)

    override fun createImageDecoder() = VipsImageDecoder()

    override suspend fun createReaderImageFactory(
        imageDecoder: VangaImageDecoder,
        pipeline: ImageProcessingPipeline,
        settings: ImageReaderSettingsRepository,
    ): ReaderImageFactory {
        return DesktopReaderImageFactory(
            imageDecoder = imageDecoder,
            downSamplingKernel = settings.getDownsamplingKernel().stateIn(initScope),
            upsamplingMode = settings.getUpsamplingMode().stateIn(initScope),
            linearLightDownSampling = settings.getLinearLightDownsampling().stateIn(initScope),
            processingPipeline = pipeline,
            stretchImages = settings.getStretchToFit().stateIn(initScope),
        )
    }

    override fun createWindowState() = windowState

    override fun createCoilContext() = PlatformContext.INSTANCE

    override fun getCoilCacheDirectory(): Path {
        return Path(AppDirectories.coilCachePath.toString())
    }

    override fun createCoilMemoryCache(): MemoryCache {
        return MemoryCache.Builder()
            .maxSizeBytes(64 * 1024 * 1024) // 64 Mib
            .build()
    }

    override fun getReaderCacheDirectory(): Path {
        return Path(AppDirectories.readerCachePath.toString())
    }

    override fun createOfflineModule(
        repositories: OfflineRepositories,
        onlineUser: StateFlow<KomgaUser?>,
        onlineServerUrl: StateFlow<String>,
        isOffline: StateFlow<Boolean>,
        komgaClientFactory: KomgaClientFactory
    ): OfflineModule {
        return DesktopOfflineModule(
            repositories = repositories,
            onlineUser = onlineUser,
            onlineServerUrl = onlineServerUrl,
            isOffline = isOffline,
            komgaClientFactory = komgaClientFactory
        )
    }
}

fun loadWebviewLibraries() {
    measureTime {
        try {
            WebviewSharedLibraries.load()
        } catch (e: UnsatisfiedLinkError) {
            logger.error(e) { "Couldn't load webview library. Epub reader will not work" }
        }
    }.also { logger.info { "Completed Webview library load in $it" } }
}

fun loadVipsLibraries() {
    measureTime {
        try {
            VipsSharedLibraries.load()
        } catch (e: UnsatisfiedLinkError) {
            logger.error(e) { "Couldn't load libvips. Vips decoder will not work" }
        }
    }.also { logger.info { "completed vips load in $it" } }
}
