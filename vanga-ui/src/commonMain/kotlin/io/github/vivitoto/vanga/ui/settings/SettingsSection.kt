package io.github.vivitoto.vanga.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.VangaShape

val SettingsSectionGap = 12.dp
val SettingsTitleGap = 4.dp
val SettingsCardPadding = 10.dp
val SettingsItemGap = 8.dp
val SettingsDetailGap = 6.dp
val SettingsRowHorizontalPadding = 2.dp
val SettingsRowVerticalPadding = 8.dp

@Composable
fun SettingsSectionHeader(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SettingsTitleGap),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(SettingsCardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SettingsTitleGap),
    ) {
        SettingsSectionHeader(
            title = title,
            description = description,
            modifier = Modifier.padding(start = 4.dp),
        )
        SettingsCard(contentPadding = contentPadding, content = content)
    }
}

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentPadding: PaddingValues = PaddingValues(SettingsCardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VangaShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(SettingsItemGap),
            content = content,
        )
    }
}

@Composable
fun SettingsRow(
    title: String,
    supportingText: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    stackTrailing: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val contentColor = if (enabled) {
        LocalContentColor.current
    } else {
        LocalContentColor.current.copy(alpha = 0.42f)
    }
    val supportingColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.42f)
    }
    val clickModifier = if (onClick != null) {
        Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand)
    } else {
        Modifier
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        val textContent: @Composable (Modifier) -> Unit = { textModifier ->
            Column(
                modifier = textModifier,
                verticalArrangement = Arrangement.spacedBy(SettingsTitleGap),
            ) {
                ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (!supportingText.isNullOrBlank()) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = supportingColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (stackTrailing && trailing != null) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .then(clickModifier)
                    .padding(horizontal = SettingsRowHorizontalPadding, vertical = SettingsRowVerticalPadding),
                verticalArrangement = Arrangement.spacedBy(SettingsDetailGap),
            ) {
                textContent(Modifier.fillMaxWidth())
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = androidx.compose.ui.Alignment.CenterEnd,
                ) {
                    trailing()
                }
            }
        } else {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .then(clickModifier)
                    .padding(horizontal = SettingsRowHorizontalPadding, vertical = SettingsRowVerticalPadding),
                horizontalArrangement = Arrangement.spacedBy(SettingsItemGap),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                textContent(Modifier.weight(1f))
                trailing?.let {
                    Box(
                        modifier = Modifier.widthIn(max = 220.dp),
                        contentAlignment = androidx.compose.ui.Alignment.CenterEnd,
                    ) {
                        it()
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    SettingsRow(
        title = title,
        supportingText = supportingText,
        modifier = modifier,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                    checkedThumbColor = MaterialTheme.colorScheme.onSecondary,
                ),
            )
        },
    )
}

@Composable
fun SettingsCheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
) {
    SettingsRow(
        title = title,
        supportingText = supportingText,
        modifier = modifier,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.secondaryContainer,
                    checkmarkColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        },
    )
}

@Composable
fun SettingsValueRow(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    SettingsRow(
        title = title,
        supportingText = supportingText,
        modifier = modifier,
        trailing = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}
