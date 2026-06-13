package io.github.vivitoto.vanga.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.KeyEvent
import com.dokar.sonner.ToasterState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import io.github.vivitoto.vanga.AppWindowState
import io.github.vivitoto.vanga.KomgaAuthenticationState
import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass
import io.github.vivitoto.vanga.ui.strings.EnStrings
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.sse.KomgaEvent

val LocalViewModelFactory = compositionLocalOf<ViewModelFactory> { error("ViewModel factory is not set") }

val LocalToaster = compositionLocalOf<ToasterState> { error("Toaster is not set") }
val LocalKomgaEvents = compositionLocalOf<SharedFlow<KomgaEvent>> { error("Komga events are not set") }
val LocalKomfIntegration = compositionLocalOf { flowOf(false) }
val LocalKeyEvents = compositionLocalOf<SharedFlow<KeyEvent>> { error("Key events are not set") }
val LocalWindowWidth = compositionLocalOf<WindowSizeClass> { error("Window size is not set") }
val LocalWindowHeight = compositionLocalOf<WindowSizeClass> { error("Window size is not set") }
val LocalStrings = staticCompositionLocalOf { EnStrings }
val LocalPlatform = compositionLocalOf<PlatformType> { error("Platform type is not set") }
val LocalTheme = compositionLocalOf { Theme.DARK }
val LocalWindowState = compositionLocalOf<AppWindowState> { error("Window state was not initialized") }
val LocalLibraries = compositionLocalOf<StateFlow<List<KomgaLibrary>>> { error("Libraries were not initialized") }
val LocalReloadEvents = staticCompositionLocalOf<SharedFlow<Unit>> { error("Reload event flow was not initialized") }
val LocalBookDownloadEvents =
    staticCompositionLocalOf<SharedFlow<DownloadEvent>?> { error("Book download event flow was not initialized") }
val LocalOfflineMode = staticCompositionLocalOf<StateFlow<Boolean>> { error("offline mode flow was not initialized") }
val LocalKomgaState = staticCompositionLocalOf<KomgaAuthenticationState> { error("komga state was not initialized") }
val LocalCoverBlurSettings = staticCompositionLocalOf { CoverBlurSettings() }

data class CoverBlurSettings(
    val libraryCovers: Boolean = false,
    val collectionCovers: Boolean = false,
    val bookCovers: Boolean = false,
)
