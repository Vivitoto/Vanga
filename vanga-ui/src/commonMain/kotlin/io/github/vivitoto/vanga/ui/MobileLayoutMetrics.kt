package io.github.vivitoto.vanga.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT

internal val MobileTopContentPadding = 10.dp
private val MobileCompactBottomBulkActionsOffset = 56.dp
private val MobileRegularBottomBulkActionsOffset = 88.dp

internal data class MobileLayoutMetrics(
    val topContentPadding: Dp,
    val bottomContentPadding: Dp,
    val bottomBulkActionsOffset: Dp,
)

@Composable
internal fun rememberMobileLayoutMetrics(): MobileLayoutMetrics {
    val height = LocalWindowHeight.current
    return remember(height) {
        MobileLayoutMetrics(
            topContentPadding = if (height == COMPACT) 6.dp else MobileTopContentPadding,
            bottomContentPadding = if (height == COMPACT) 16.dp else 24.dp,
            bottomBulkActionsOffset = if (height == COMPACT) {
                MobileCompactBottomBulkActionsOffset
            } else {
                MobileRegularBottomBulkActionsOffset
            },
        )
    }
}

internal data class MeasuredBottomOverlayPadding(
    val bottomPadding: Dp,
    val onOverlaySizeChanged: (IntSize) -> Unit,
)

@Composable
internal fun rememberMeasuredBottomOverlayPadding(
    visible: Boolean,
    basePadding: Dp,
    overlayBottomOffset: Dp? = null,
    extraGap: Dp = 12.dp,
): MeasuredBottomOverlayPadding {
    val density = LocalDensity.current
    val metrics = rememberMobileLayoutMetrics()
    var overlayHeight by remember { mutableStateOf(0.dp) }
    val actualOverlayBottomOffset = overlayBottomOffset ?: metrics.bottomBulkActionsOffset
    val overlayPadding = overlayHeight + actualOverlayBottomOffset + extraGap
    val bottomPadding = if (visible && overlayHeight > 0.dp) {
        maxOf(basePadding, overlayPadding)
    } else {
        basePadding
    }

    return MeasuredBottomOverlayPadding(
        bottomPadding = bottomPadding,
        onOverlaySizeChanged = { size ->
            overlayHeight = with(density) { size.height.toDp() }
        },
    )
}
