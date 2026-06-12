package io.github.vivitoto.vanga

import android.app.Activity
import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import coil3.memory.MemoryCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vivitoto.vanga.BuildConfig
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.io.files.Path
import okhttp3.Cache
import okhttp3.OkHttpClient
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
import io.github.vivitoto.vanga.fonts.fontsDirectory
import io.github.vivitoto.vanga.homefilters.homeScreenDefaultFilters
import io.github.vivitoto.vanga.http.vangaUserAgent
import io.github.vivitoto.vanga.image.AndroidReaderImageFactory
import io.github.vivitoto.vanga.image.VangaImageDecoder
import io.github.vivitoto.vanga.image.ReaderImageFactory
import io.github.vivitoto.vanga.image.UpsamplingMode
import io.github.vivitoto.vanga.image.VipsImageDecoder
import io.github.vivitoto.vanga.image.VipsSharedLibrariesLoader
import io.github.vivitoto.vanga.image.processing.ImageProcessingPipeline
import io.github.vivitoto.vanga.offline.AndroidOfflineModule
import io.github.vivitoto.vanga.offline.OfflineModule
import io.github.vivitoto.vanga.offline.OfflineRepositories
import io.github.vivitoto.vanga.settings.AndroidSecretsRepository
import io.github.vivitoto.vanga.settings.AppSettingsSerializer
import io.github.vivitoto.vanga.settings.ImageReaderSettingsRepository
import io.github.vivitoto.vanga.updates.AndroidAppUpdater
import io.github.vivitoto.vanga.updates.AppUpdater
import io.github.vivitoto.vanga.updates.UpdateClient
import snd.komga.client.KomgaClientFactory
import snd.komga.client.user.KomgaUser
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

private val logger = KotlinLogging.logger { }

class AndroidAppModule(
    private val context: Context,
    private val mainActivity: StateFlow<Activity?>,
) : AppModule() {
    private val databases = VangaDatabase(context.filesDir.absolutePath.toString())

    private val okHttpLogger = KotlinLogging.logger("http.logging")
    private val okHttpClientWithoutCache: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
//        .addInterceptor(HttpLoggingInterceptor { okHttpLogger.info { it } }
//            .setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()
    private val okHttpClient = okHttpClientWithoutCache.newBuilder().cache(
        Cache(
            directory = context.cacheDir.resolve("okhttp"),
            maxSize = 64 * 1024L * 1024L // 64 MiB
        )
    ).build()

    override suspend fun beforeInit() {
        measureTime {
            try {
                VipsSharedLibrariesLoader.load()
            } catch (e: UnsatisfiedLinkError) {
                logger.error(e) { "Couldn't load vips shared libraries. reader image loading will not work" }
            }
        }.also { logger.info { "completed vips libraries load in $it" } }

        fontsDirectory = Path(context.filesDir.resolve("fonts").absolutePath)
    }


    override suspend fun createAppRepositories(): AppRepositories {
        val datastore = DataStoreFactory.create(
            serializer = AppSettingsSerializer,
            produceFile = { context.dataStoreFile("settings.pb") },
            corruptionHandler = null,
        )

        return AppRepositories(
            settingsRepository = ExposedSettingsRepository(databases.app).let { repository ->
                SettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repository.get() ?: AppSettings(cardWidth = 150),
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
                        settings = repository.get() ?: ImageReaderSettings(upsamplingMode = UpsamplingMode.BILINEAR),
                        saveSettings = repository::save
                    )
                )
            },
            fontsRepository = ExposedUserFontsRepository(databases.app),
            secretsRepository = AndroidSecretsRepository(datastore),
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
            bookDtoRepository = ExposedOfflineBookDtoRepository(databases.offline),
            referentialRepository = ExposedOfflineReferentialRepository(databases.offline),
            seriesDtoRepository = ExposedSeriesDtoRepository(databases.offline),
            tasksRepository = ExposedOfflineTasksRepository(databases.offline),
            logJournalRepository = ExposedLogJournalRepository(databases.offline),
            offlineSettingsRepository = ExposedOfflineSettingsRepository(databases.offline).let { repo ->
                OfflineSettingsRepositoryWrapper(
                    SettingsStateWrapper(
                        settings = repo.get() ?: OfflineSettings(
                            downloadDirectory = PlatformFile(context.filesDir.resolve("offline"))
                        ),
                        saveSettings = repo::save
                    )
                )
            },


            transactionTemplate = ExposedTransactionTemplate(databases.offline),
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

    override fun createAppUpdater(updateClient: UpdateClient): AppUpdater? {
        @Suppress("KotlinConstantConditions")
        return if (BuildConfig.ENABLE_SELF_UPDATES) AndroidAppUpdater(updateClient, context) else null
    }

    override fun createImageDecoder() = VipsImageDecoder()

    override suspend fun createReaderImageFactory(
        imageDecoder: VangaImageDecoder,
        pipeline: ImageProcessingPipeline,
        settings: ImageReaderSettingsRepository,
    ): ReaderImageFactory {
        return AndroidReaderImageFactory(
            imageDecoder = imageDecoder,
            downSamplingKernel = settings.getDownsamplingKernel().stateIn(initScope),
            upsamplingMode = settings.getUpsamplingMode().stateIn(initScope),
            linearLightDownSampling = settings.getLinearLightDownsampling().stateIn(initScope),
            processingPipeline = pipeline,
            stretchImages = settings.getStretchToFit().stateIn(initScope),
        )
    }

    override fun createWindowState() = AndroidWindowState(mainActivity)

    override fun createCoilContext() = context

    override fun getCoilCacheDirectory(): Path {
        return Path(context.cacheDir.resolve("coil3_disk_cache").toString())
    }

    override fun createCoilMemoryCache(): MemoryCache {
        return MemoryCache.Builder()
            .maxSizePercent(context)
            .maxSizeBytes(64 * 1024 * 1024) // 64 Mib
            .build()
    }

    override fun getReaderCacheDirectory(): Path {
        return Path(context.cacheDir.resolve("vanga_reader_cache").toString())
    }

    override fun createOfflineModule(
        repositories: OfflineRepositories,
        onlineUser: StateFlow<KomgaUser?>,
        onlineServerUrl: StateFlow<String>,
        isOffline: StateFlow<Boolean>,
        komgaClientFactory: KomgaClientFactory
    ): OfflineModule {
        return AndroidOfflineModule(
            repositories = repositories,
            onlineUser = onlineUser,
            onlineServerUrl = onlineServerUrl,
            isOffline = isOffline,
            komgaClientFactory = komgaClientFactory,
            context = this.context,
        )
    }
}
