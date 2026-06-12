package io.github.vivitoto.vanga

import WasmDependencyContainer
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.github.vivitoto.vanga.image.BookImageLoader
import io.github.vivitoto.vanga.image.ReaderImageFactory
import io.github.vivitoto.vanga.image.coil.CoilDecoder
import io.github.vivitoto.vanga.image.coil.KomgaBookFetcher
import io.github.vivitoto.vanga.image.coil.KomgaBookPageMapper
import io.github.vivitoto.vanga.image.coil.KomgaBookPageThumbnailMapper
import io.github.vivitoto.vanga.image.coil.KomgaCollectionMapper
import io.github.vivitoto.vanga.image.coil.KomgaReadListMapper
import io.github.vivitoto.vanga.image.coil.KomgaSeriesMapper
import io.github.vivitoto.vanga.image.coil.KomgaSeriesThumbnailMapper
import io.github.vivitoto.vanga.image.processing.ImageProcessingPipeline
import io.github.vivitoto.vanga.settings.CookieStoreSecretsRepository
import io.github.vivitoto.vanga.settings.ImageReaderSettingsRepository
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import io.github.vivitoto.vanga.db.SettingsStateWrapper
import io.github.vivitoto.vanga.db.repository.EpubReaderSettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.repository.KomfSettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.repository.ReaderSettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.repository.SettingsRepositoryWrapper
import io.github.vivitoto.vanga.db.settings.LocalStorageSettingsRepository
import io.github.vivitoto.vanga.db.settings.NoopFontsRepository
import io.github.vivitoto.vanga.image.VangaImageDecoder
import io.github.vivitoto.vanga.image.WasmReaderImageFactory
import io.github.vivitoto.vanga.image.coil.BlobFetcher
import io.github.vivitoto.vanga.image.wasm.client.WorkerImageDecoder
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory

suspend fun initDependencies(stateFlowScope: CoroutineScope): WasmDependencyContainer {
    val workerDecoder = WorkerImageDecoder()
    workerDecoder.init()

    val localStorageRepository = LocalStorageSettingsRepository()
    val appSettingsRepository = SettingsRepositoryWrapper(
        SettingsStateWrapper(
            localStorageRepository.getSettings(),
            localStorageRepository::saveAppSettings
        )
    )
    val imageReaderSettingsRepository = ReaderSettingsRepositoryWrapper(
        SettingsStateWrapper(
            localStorageRepository.getImageReaderSettings(),
            localStorageRepository::saveImageReaderSettings
        )
    )
    val epubReaderSettingsRepository = EpubReaderSettingsRepositoryWrapper(
        SettingsStateWrapper(
            localStorageRepository.getEpubReaderSettings(),
            localStorageRepository::saveEpubReaderSettings
        )
    )
    val komfSettingsRepository = KomfSettingsRepositoryWrapper(
        SettingsStateWrapper(
            localStorageRepository.getKomfSettings(),
            localStorageRepository::saveKomfSettings
        )
    )
    val secretsRepository = CookieStoreSecretsRepository()

    val baseUrl = appSettingsRepository.getServerUrl().stateIn(stateFlowScope)
    val komfUrl = komfSettingsRepository.getKomfUrl().stateIn(stateFlowScope)
    overrideFetch { baseUrl.value }

    val ktorClient = createKtorClient(baseUrl)
    val komgaClientFactory = createKomgaClientFactory(baseUrl, ktorClient)

    val coil = createCoil(baseUrl, ktorClient, workerDecoder)
    SingletonImageLoader.setSafe { coil }

    val komfClientFactory = KomfClientFactory.Builder()
        .baseUrl { komfUrl.value }
        .ktor(ktorClient)
        .build()

    val imagePipeline = createImagePipeline()

    val readerImageFactory = createReaderImageFactory(
        imagePreprocessingPipeline = imagePipeline,
        settings = imageReaderSettingsRepository,
        imageDecoder = workerDecoder,
        stateFlowScope = stateFlowScope
    )
    val readerImageLoader = createReaderImageLoader(
        baseUrl = baseUrl,
        ktorClient = ktorClient,
        decoder = workerDecoder,
        imageFactory = readerImageFactory
    )

    return WasmDependencyContainer(
        settingsRepository = appSettingsRepository,
        epubReaderSettingsRepository = epubReaderSettingsRepository,
        imageReaderSettingsRepository = imageReaderSettingsRepository,
        fontsRepository = NoopFontsRepository(),
        secretsRepository = secretsRepository,
        komfSettingsRepository = komfSettingsRepository,
        komgaClientFactory = komgaClientFactory,
        komfClientFactory = komfClientFactory,
        appUpdater = null,
        coilImageLoader = coil,
        bookImageLoader = readerImageLoader,
        windowState = BrowserWindowState(),
        imageDecoder = workerDecoder,
        readerImageFactory = readerImageFactory
    )
}

private fun createKtorClient(
    baseUrl: StateFlow<String>,
): HttpClient {
    return HttpClient(Js) {
        defaultRequest { url(baseUrl.value) }
        expectSuccess = true
        followRedirects = false
    }
}

private fun createKomgaClientFactory(
    baseUrl: StateFlow<String>,
    ktorClient: HttpClient,
): KomgaClientFactory {

    return KomgaClientFactory.Builder()
        .ktor(ktorClient)
        .baseUrl { baseUrl.value }
        .build()
}

private fun createCoil(
    url: StateFlow<String>,
    ktorClient: HttpClient,
    imageWorker: WorkerImageDecoder,
): ImageLoader {
    return ImageLoader.Builder(PlatformContext.INSTANCE)
        .components {
            add(KomgaBookPageMapper(url))
            add(KomgaBookPageThumbnailMapper(url))
            add(KomgaSeriesMapper(url))
            add(KomgaBookFetcher(url))
            add(KomgaCollectionMapper(url))
            add(KomgaReadListMapper(url))
            add(KomgaSeriesThumbnailMapper(url))
            add(BlobFetcher.Factory())
            add(CoilDecoder.Factory(imageWorker))
            add(KtorNetworkFetcherFactory(httpClient = ktorClient))
        }
        .memoryCache(
            MemoryCache.Builder()
                .maxSizeBytes(64 * 1024 * 1024) // 64 Mib
                .build()
        )
        .build()
}

private fun createReaderImageLoader(
    baseUrl: StateFlow<String>,
    ktorClient: HttpClient,
    decoder: WorkerImageDecoder,
    imageFactory: ReaderImageFactory
): BookImageLoader {
    val bookClient = KomgaClientFactory.Builder()
        .ktor(ktorClient)
        .baseUrl { baseUrl.value }
        .build()
        .bookClient()

    return BookImageLoader(
        bookClient = bookClient,
        imageDecoder = decoder,
        readerImageFactory = imageFactory,
        diskCache = null
    )
}

private fun createImagePipeline(): ImageProcessingPipeline = ImageProcessingPipeline()

private suspend fun createReaderImageFactory(
    imagePreprocessingPipeline: ImageProcessingPipeline,
    settings: ImageReaderSettingsRepository,
    imageDecoder: VangaImageDecoder,
    stateFlowScope: CoroutineScope,
): WasmReaderImageFactory {
    return WasmReaderImageFactory(
        imageDecoder = imageDecoder,
        downSamplingKernel = settings.getDownsamplingKernel().stateIn(stateFlowScope),
        upsamplingMode = settings.getUpsamplingMode().stateIn(stateFlowScope),
        linearLightDownSampling = settings.getLinearLightDownsampling().stateIn(stateFlowScope),
        processingPipeline = imagePreprocessingPipeline,
        stretchImages = settings.getStretchToFit().stateIn(stateFlowScope),
    )
}

private fun overrideFetch(komgaUrl: () -> String) {
    js(
        """
    window.originalFetch = window.fetch;
    window.fetch = function (resource, init) {
        init = Object.assign({}, init);
        if(typeof resource =='string' && resource.startsWith(komgaUrl())) {
            init.headers = Object.assign( { 'X-Requested-With' : 'XMLHttpRequest' }, init.headers) 
            init.credentials = 'include';
        } 
        return window.originalFetch(resource, init);
    };
"""
    )
}
