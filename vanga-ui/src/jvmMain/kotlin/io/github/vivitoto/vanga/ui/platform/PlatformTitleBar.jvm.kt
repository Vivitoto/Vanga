package io.github.vivitoto.vanga.ui.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import com.jetbrains.JBR
import io.github.vivitoto.vanga.DesktopPlatform
import io.github.vivitoto.vanga.DesktopPlatform.Linux
import io.github.vivitoto.vanga.DesktopPlatform.MacOS
import io.github.vivitoto.vanga.DesktopPlatform.Unknown
import io.github.vivitoto.vanga.DesktopPlatform.Windows
import io.github.vivitoto.vanga.ui.LocalAwtWindowState
import io.github.vivitoto.vanga.ui.LocalWindow

val csdEnvEnabled = System.getenv("USE_CSD")?.toBoolean() ?: true
actual fun canIntegrateWithSystemBar() = JBR.isAvailable() && csdEnvEnabled

@Composable
actual fun PlatformTitleBar(
    modifier: Modifier,
    applyInsets: Boolean,
    content: @Composable TitleBarScope.() -> Unit,
) {
    val window = LocalWindow.current
    val windowState = LocalAwtWindowState.current

    if (!canIntegrateWithSystemBar() || windowState.placement == WindowPlacement.Fullscreen) {
        SimpleTitleBarLayout(modifier, applyInsets, content)
    } else {
        when (DesktopPlatform.Current) {
            Windows -> TitleBarOnWindows(
                modifier.heightIn(min = 32.dp).background(MaterialTheme.colorScheme.surfaceDim),
                window,
                content
            )

            Linux -> TitleBarOnLinux(
                modifier.heightIn(min = 32.dp).background(MaterialTheme.colorScheme.surfaceDim),
                window = window,
                windowState = windowState,
                content = content
            )

            MacOS, Unknown -> SimpleTitleBarLayout(modifier, applyInsets, content)
        }
    }
}
