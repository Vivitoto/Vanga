package io.github.vivitoto.vanga.ui.settings.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.settings.model.AppTheme
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.common.components.AppSliderDefaults
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.layout.ResponsiveCardWidth
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSwitchRow
import io.github.vivitoto.vanga.ui.settings.SettingsValueRow
import kotlin.math.roundToInt

private val themeOptions = listOf(
    AppTheme.SYSTEM,
    AppTheme.DARK,
    AppTheme.LIGHT,
)

@Composable
fun AppearanceSettingsContent(
    cardWidth: Dp,
    onCardWidthChange: (Dp) -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    libraryCoversBlurred: Boolean,
    onLibraryCoversBlurredChange: (Boolean) -> Unit,
    collectionCoversBlurred: Boolean,
    onCollectionCoversBlurredChange: (Boolean) -> Unit,
    bookCoversBlurred: Boolean,
    onBookCoversBlurredChange: (Boolean) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val strings = LocalStrings.current.settings

        SettingsSectionCard(
            title = "主题",
        ) {
            SettingsRow(
                title = "明暗模式",
                trailing = {
                    DropdownChoiceMenu(
                        selectedOption = LabeledEntry(currentTheme, strings.forAppTheme(currentTheme)),
                        options = themeOptions.map { LabeledEntry(it, strings.forAppTheme(it)) },
                        onOptionChange = { onThemeChange(it.value) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        minHeight = 44.dp,
                        inputFieldModifier = Modifier.widthIn(min = 132.dp),
                        modifier = Modifier.widthIn(min = 132.dp),
                    )
                }
            )
        }

        SettingsSectionCard(
            title = "封面模糊",
        ) {
            SettingsSwitchRow(
                title = "书库封面模糊",
                checked = libraryCoversBlurred,
                onCheckedChange = onLibraryCoversBlurredChange,
            )
            SettingsSwitchRow(
                title = "合集封面模糊",
                checked = collectionCoversBlurred,
                onCheckedChange = onCollectionCoversBlurredChange,
            )
            SettingsSwitchRow(
                title = "单本封面模糊",
                checked = bookCoversBlurred,
                onCheckedChange = onBookCoversBlurredChange,
            )
        }

        SettingsSectionCard(
            title = "封面卡片",
        ) {
            SettingsValueRow(
                title = "封面宽度",
                value = "${cardWidth.value.roundToInt()} dp",
            )
            Slider(
                value = cardWidth.value,
                onValueChange = { onCardWidthChange(it.roundToInt().dp) },
                steps = 19,
                valueRange = 150f..350f,
                colors = AppSliderDefaults.colors(),
                modifier = Modifier.fillMaxWidth().cursorForHand().padding(horizontal = 2.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 340.dp, max = 520.dp)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ResponsiveCardWidth(cardWidth = cardWidth, horizontalPadding = 0.dp) { previewWidth ->
                    Card(
                        Modifier
                            .widthIn(max = previewWidth)
                            .fillMaxWidth()
                            .aspectRatio(0.703f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .38f)
                        ),
                    ) {

                    }
                }
            }
        }
    }
}
