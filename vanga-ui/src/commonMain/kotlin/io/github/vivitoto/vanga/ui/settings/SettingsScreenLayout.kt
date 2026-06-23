package io.github.vivitoto.vanga.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SettingsScreenLayout(
    navMenu: @Composable () -> Unit,
    content: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
) = Layout(
    modifier = Modifier.fillMaxSize(),
    contents = listOf(navMenu, content, dismissButton)
) { (navMenuMeasurable, contentMeasurable, dismissMeasurable), constraints ->
    val preferredNavWidth = settingsDesktopNavMenuWidth.roundToPx()
    val preferredContentWidth = settingsDesktopContentWidth.roundToPx()
    val minNavWidth = 180.dp.roundToPx()
    val minContentWidth = 360.dp.roundToPx()
    val availableWidth = constraints.maxWidth
    val useSingleColumn = availableWidth < minNavWidth + minContentWidth

    if (useSingleColumn) {
        val navMaxHeight = (constraints.maxHeight / 3)
            .coerceAtMost(280.dp.roundToPx())
            .coerceAtLeast(0)
        val navMenuPlaceable = navMenuMeasurable.first()
            .measure(
                constraints.copy(
                    minWidth = 0,
                    maxWidth = availableWidth,
                    minHeight = 0,
                    maxHeight = navMaxHeight,
                )
            )
        val contentPlaceable = contentMeasurable.first()
            .measure(
                constraints.copy(
                    minWidth = 0,
                    maxWidth = availableWidth,
                    minHeight = 0,
                    maxHeight = (constraints.maxHeight - navMenuPlaceable.height).coerceAtLeast(0),
                )
            )
        val dismissPlaceable = dismissMeasurable.firstOrNull()?.measure(constraints.copy(minWidth = 0, minHeight = 0))

        return@Layout layout(constraints.maxWidth, constraints.maxHeight) {
            navMenuPlaceable.placeRelative(0, 0)
            contentPlaceable.placeRelative(0, navMenuPlaceable.height)
            dismissPlaceable?.placeRelative(
                (constraints.maxWidth - dismissPlaceable.width).coerceAtLeast(0),
                0,
            )
        }
    }

    val navWidth = if (availableWidth < preferredNavWidth + preferredContentWidth) {
        ((availableWidth * .35f).roundToInt())
            .coerceAtLeast(minNavWidth.coerceAtMost(availableWidth))
            .coerceAtMost(preferredNavWidth.coerceAtMost(availableWidth))
    } else {
        preferredNavWidth
    }
    val contentWidth = (availableWidth - navWidth).coerceAtLeast(0).coerceAtMost(preferredContentWidth)
    val padding =
        ((constraints.maxWidth - (navWidth + contentWidth)).toFloat() / 2).roundToInt().coerceAtLeast(0)

    val contentPlaceable = contentMeasurable.first()
        .measure(
            constraints.copy(
                minWidth = 0,
                maxWidth = contentWidth
            )
        )

    val navMenuPlaceable = navMenuMeasurable.first()
        .measure(
            constraints.copy(
                minWidth = 0,
                maxWidth = padding + navWidth
            )
        )
    val dismissPlaceable = dismissMeasurable.firstOrNull()?.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val contentX = padding + navWidth
    layout(constraints.maxWidth, constraints.maxHeight) {
        navMenuPlaceable.placeRelative(
            0,
            0
        )
        contentPlaceable.placeRelative(
            contentX,
            0
        )
        dismissPlaceable?.placeRelative(
            (contentX + contentPlaceable.width).coerceAtMost(constraints.maxWidth - dismissPlaceable.width),
            0
        )
    }
}
