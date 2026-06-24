package io.github.vivitoto.vanga.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.VangaShape

@Composable
fun <T> DescriptionChips(
    label: String,
    chipValue: LabeledEntry<T>,
    onClick: (T) -> Unit = {},
    modifier: Modifier = Modifier
) {
    DescriptionChips(
        label = label,
        chipValues = listOf(chipValue),
        onChipClick = onClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> DescriptionChips(
    label: String,
    chipValues: List<LabeledEntry<T>>,
    secondaryValues: List<LabeledEntry<T>>? = null,
    onChipClick: (T) -> Unit = {},
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    if (chipValues.isEmpty() && secondaryValues.isNullOrEmpty()) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        MetadataLabelPill(label)

        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            chipValues.forEach { entry ->
                NoPaddingChip(onClick = { onChipClick(entry.value) }) {
                    Text(entry.label, style = MaterialTheme.typography.labelMedium)
                    icon?.let { Icon(it, null, modifier = Modifier.size(18.dp)) }
                }
            }
            secondaryValues?.filter { it !in chipValues }?.forEach { entry ->
                NoPaddingChip(
                    borderColor = MaterialTheme.colorScheme.primary,
                    onClick = { onChipClick(entry.value) }) {
                    Text(entry.label, style = MaterialTheme.typography.labelMedium)
                    icon?.let { Icon(it, null, modifier = Modifier.size(18.dp)) }
                }
            }

        }

    }
}

@Composable
private fun MetadataLabelPill(label: String) {
    Box(
        modifier = Modifier
            .widthIn(min = 72.dp, max = 112.dp)
            .heightIn(min = 32.dp)
            .border(
                Dp.Hairline,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = .38f),
                RoundedCornerShape(14.dp),
            )
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .82f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun NoPaddingChip(
    borderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    color: Color = Color.Unspecified,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .border(Dp.Hairline, borderColor, VangaShape)
            .clip(VangaShape)
            .background(color)
            .clickable { onClick() }
            .padding(10.dp, 5.dp)
            .pointerHoverIcon(PointerIcon.Hand),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            content()
        }
    }
}


object AppFilterChipDefaults {

    @Composable
    fun filterChipColors() = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
    )
}
