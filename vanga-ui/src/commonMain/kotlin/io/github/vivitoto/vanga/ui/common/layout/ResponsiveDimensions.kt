package io.github.vivitoto.vanga.ui.common.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun ResponsiveCardWidth(
    cardWidth: Dp,
    horizontalPadding: Dp = 30.dp,
    minWidth: Dp = 120.dp,
    modifier: Modifier = Modifier.fillMaxWidth(),
    content: @Composable (Dp) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val availableWidth = (maxWidth - horizontalPadding).coerceAtLeast(0.dp)
        val responsiveWidth = when {
            availableWidth <= minWidth -> availableWidth
            else -> cardWidth.coerceIn(minWidth, availableWidth)
        }
        content(responsiveWidth)
    }
}
