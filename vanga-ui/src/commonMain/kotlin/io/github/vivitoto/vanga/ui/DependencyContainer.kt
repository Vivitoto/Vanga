package io.github.vivitoto.vanga.ui

import coil3.ImageLoader
import coil3.PlatformContext
import kotlinx.coroutines.flow.StateFlow
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.AppRepositories
import io.github.vivitoto.vanga.AppWindowState
import io.github.vivitoto.vanga.KomgaAuthenticationState
import io.github.vivitoto.vanga.ManagedKomgaEvents
import io.github.vivitoto.vanga.image.BookImageLoader
import io.github.vivitoto.vanga.image.VangaImageDecoder
import io.github.vivitoto.vanga.image.ReaderImageFactory
import io.github.vivitoto.vanga.komga.api.KomgaApi
import io.github.vivitoto.vanga.offline.OfflineDependencies
import io.github.vivitoto.vanga.ui.strings.AppStrings
import io.github.vivitoto.vanga.updates.AppUpdater
import io.ktor.client.HttpClient
import snd.komf.client.KomfClientFactory

data class DependencyContainer(
    val appStrings: StateFlow<AppStrings>,
    val appRepositories: AppRepositories,
    val komgaApi: StateFlow<KomgaApi>,

    val isOffline: StateFlow<Boolean>,
    val komfClientFactory: KomfClientFactory,
    val appNotifications: AppNotifications,
    val webDavHttpClient: HttpClient,
    val komgaSharedState: KomgaAuthenticationState,
    val komgaEvents: ManagedKomgaEvents,
    val appUpdater: AppUpdater?,

    val coilContext: PlatformContext,
    val coilImageLoader: ImageLoader,

    val imageDecoder: VangaImageDecoder,
    val bookImageLoader: BookImageLoader,
    val readerImageFactory: ReaderImageFactory,

    val windowState: AppWindowState,

    val offlineDependencies: OfflineDependencies,
)
