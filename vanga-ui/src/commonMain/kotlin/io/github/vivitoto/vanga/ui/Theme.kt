package io.github.vivitoto.vanga.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.settings.model.AppTheme

val VangaCornerRadius = 8.dp
val VangaShape = RoundedCornerShape(VangaCornerRadius)
val VangaButtonShape = VangaShape

val VangaShapes = Shapes(
    extraSmall = VangaShape,
    small = VangaShape,
    medium = VangaShape,
    large = VangaShape,
    extraLarge = VangaShape,
)

enum class Theme(
    val colorScheme: ColorScheme,
    val type: ThemeType,
    val shapes: Shapes = VangaShapes,
) {
    DARK(
        darkColorScheme(
            primary = VangaBrand.ChampagneDark,
            onPrimary = VangaBrand.Ink,
            primaryContainer = Color(0xFF2A2316),
            onPrimaryContainer = Color(0xFFF1E2C2),

            secondary = Color(0xFFD0CAB7),
            onSecondary = Color(0xFF1B1D22),
            secondaryContainer = Color(0xFF302B22),
            onSecondaryContainer = Color(0xFFF5F1E8),

            tertiary = VangaBrand.ChampagneLight,
            onTertiary = VangaBrand.Ink,
            tertiaryContainer = Color(0xFF3A2A12),
            onTertiaryContainer = Color(0xFFF4E6CB),

            background = VangaBrand.Ink,
            onBackground = Color(0xFFF5F1E8),

            surface = VangaBrand.Ink,
            onSurface = Color(0xFFF5F1E8),

            surfaceVariant = Color(0xFF1B1D22),
            surfaceContainerLow = Color(0xFF121318),
            surfaceContainerHighest = Color(0xFF24221E),
            onSurfaceVariant = Color(0xFFB4AC9A),

            surfaceDim = Color(0xFF08090B),
            surfaceBright = Color(0xFF2A2D34),

            error = Color(0xFFFFB4AB),
            onError = VangaBrand.Ink,
            errorContainer = Color(0xFF7A211C),
            onErrorContainer = Color(0xFFFFDAD6)
        ),
        ThemeType.DARK
    ),
    LIGHT(
        lightColorScheme(
            primary = VangaBrand.ChampagneLight,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF4E6CB),
            onPrimaryContainer = Color(0xFF3B2A10),

            secondary = Color(0xFF6F685C),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE9E0D0),
            onSecondaryContainer = Color(0xFF252018),

            tertiary = VangaBrand.ChampagneLight,
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFF4E6CB),
            onTertiaryContainer = Color(0xFF3B2A10),

            background = Color(0xFFF7F3EA),
            onBackground = Color(0xFF1C1A16),

            surface = Color(0xFFFFFCF6),
            onSurface = Color(0xFF1C1A16),

            surfaceVariant = Color(0xFFF0E8D7),
            surfaceContainerHighest = Color(0xFFE8DDCB),
            onSurfaceVariant = Color(0xFF6F685C),

            surfaceDim = Color(0xFFE8DDCB),
            surfaceBright = Color(0xFFFFFFFF),

            error = Color(0xFFBA1A1A),
            onError = Color.White,
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF410002)
        ),
        ThemeType.LIGHT
    ),

    DARKER(
        darkColorScheme(
            primary = VangaBrand.ChampagneDark,
            onPrimary = Color(0xFF000106),
            primaryContainer = Color(0xFF201A10),
            onPrimaryContainer = Color(0xFFF1E2C2),

            secondary = Color(0xFFCFC7BA),
            onSecondary = Color(0xFF000106),
            secondaryContainer = Color(0xFF211E18),
            onSecondaryContainer = Color(0xFFF4EFE3),

            tertiary = VangaBrand.ChampagneLight,
            onTertiary = Color(0xFF000106),
            tertiaryContainer = Color(0xFF2B1F0F),
            onTertiaryContainer = Color(0xFFF4E6CB),

            background = Color(0xFF000106),
            onBackground = Color(0xFFF4EFE3),

            surface = Color(0xFF000106),
            onSurface = Color(0xFFF4EFE3),

            surfaceVariant = Color(0xFF121210),
            surfaceContainerHighest = Color(0xFF1B1A17),
            onSurfaceVariant = Color(0xFFB7B0A7),

            surfaceDim = Color(0xFF000106),
            surfaceBright = Color(0xFF24221E),

            error = Color(0xFFFFB4AB),
            onError = Color(0xFF000106),
            errorContainer = Color(0xFF7A211C),
            onErrorContainer = Color(0xFFFFDAD6)
        ),
        ThemeType.DARK
    );

    enum class ThemeType {
        LIGHT,
        DARK
    }

    companion object {
        fun AppTheme.toTheme(isSystemInDarkTheme: Boolean) = when (this) {
            AppTheme.SYSTEM -> if (isSystemInDarkTheme) DARK else LIGHT
            AppTheme.DARK -> DARK
            AppTheme.LIGHT -> LIGHT
            AppTheme.DARKER -> DARK
        }

        fun Theme.toAppTheme() = AppTheme.valueOf(this.name)
    }
}

object VangaBrand {
    val Ink = Color(0xFF0E0F12)
    val ChampagneDark = Color(0xFFD6B981)
    val ChampagneLight = Color(0xFFB88945)
}
