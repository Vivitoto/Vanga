package io.github.vivitoto.vanga.ui.common.itemlist

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val CardGridItemSpacing = 14.dp
internal val CardGridHorizontalPadding = 14.dp
internal val CardGridBottomPadding = 36.dp

internal fun adaptiveCardGridMinSize(
    minSize: Dp,
    maxWidth: Dp,
    horizontalPadding: Dp = CardGridHorizontalPadding,
): Dp {
    val availableWidth = (maxWidth - horizontalPadding - horizontalPadding).coerceAtLeast(1.dp)
    return minSize.coerceAtMost(availableWidth)
}
