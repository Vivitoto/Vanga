package io.github.vivitoto.vanga.ui.platform

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import io.github.vivitoto.vanga.ui.Theme
import io.github.vivitoto.vanga.ui.Theme.ThemeType

@Composable
actual fun ConfigurePlatformTheme(theme: Theme) {
    val view = LocalView.current
    val activity = view.context as Activity
    LaunchedEffect(theme) {
        activity.window.statusBarColor = theme.colorScheme.background.toArgb()
        activity.window.navigationBarColor = theme.colorScheme.background.toArgb()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isStatusBarContrastEnforced = false
            activity.window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(activity.window, view).apply {
            when (theme.type) {
                ThemeType.DARK -> {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }

                ThemeType.LIGHT -> {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true

                }
            }
        }
    }
}
