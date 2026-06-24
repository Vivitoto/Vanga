package io.github.vivitoto.vanga.ui.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class CompactMetadataEntry(
    val label: String,
    val value: String,
    val maxLines: Int = 1,
    val wide: Boolean = false,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompactMetadataFlow(
    entries: List<CompactMetadataEntry>,
    modifier: Modifier = Modifier,
) {
    val visibleEntries = entries.filter { it.value.isNotBlank() }
    if (visibleEntries.isEmpty()) return

    SelectionContainer(modifier) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            visibleEntries.forEach { entry ->
                CompactMetadataChip(
                    label = entry.label,
                    value = entry.value,
                    maxLines = entry.maxLines,
                    wide = entry.wide,
                    modifier = if (entry.wide) Modifier.fillMaxWidth() else Modifier,
                )
            }
        }
    }
}

@Composable
fun CompactMetadataChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    wide: Boolean = false,
    minHeight: Dp = 32.dp,
) {
    val labelMinWidth = 72.dp
    val labelMaxWidth = 112.dp
    val valueWidth = 152.dp
    val corner = 14.dp
    val innerCorner = 6.dp

    Row(
        modifier = modifier.heightIn(min = minHeight),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.widthIn(min = labelMinWidth, max = labelMaxWidth).heightIn(min = minHeight),
            shape = RoundedCornerShape(
                topStart = corner,
                bottomStart = corner,
                topEnd = innerCorner,
                bottomEnd = innerCorner,
            ),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .82f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            border = BorderStroke(
                Dp.Hairline,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = .38f)
            ),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
        Surface(
            modifier = (if (wide) Modifier.weight(1f) else Modifier.width(valueWidth))
                .heightIn(min = minHeight),
            shape = RoundedCornerShape(
                topStart = innerCorner,
                bottomStart = innerCorner,
                topEnd = corner,
                bottomEnd = corner,
            ),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .68f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            border = BorderStroke(
                Dp.Hairline,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = .38f)
            ),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }
}
