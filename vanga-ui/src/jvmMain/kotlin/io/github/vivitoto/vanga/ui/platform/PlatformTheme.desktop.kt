package io.github.vivitoto.vanga.ui.platform

import androidx.compose.runtime.Composable
import io.github.vivitoto.vanga.ui.Theme
import io.github.vivitoto.vanga.ui.windowBorder


@Composable
actual fun ConfigurePlatformTheme(theme: Theme) {
    windowBorder.value = theme.colorScheme.surfaceVariant
}