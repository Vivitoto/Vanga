package io.github.vivitoto.vanga.ui.settings.epub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.settings.model.EpubReaderType
import io.github.vivitoto.vanga.settings.model.EpubReaderType.KOMGA_EPUB
import io.github.vivitoto.vanga.settings.model.EpubReaderType.TTSU_EPUB
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard

@Composable
fun EpubReaderSettingsContent(
    readerType: EpubReaderType,
    onReaderChange: (EpubReaderType) -> Unit,
) {
    val strings = LocalStrings.current.settings
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsSectionCard(
            title = "阅读器内核",
        ) {
            SettingsRow(
                title = "当前内核",
                supportingText = when (readerType) {
                    TTSU_EPUB -> "一次性加载整本书，适合需要更完整 EPUB 排版能力的场景。"
                    KOMGA_EPUB -> "兼容性更接近服务器端体验。"
                },
                trailing = {
                    DropdownChoiceMenu(
                        selectedOption = remember(readerType) {
                            LabeledEntry(
                                readerType,
                                strings.forEpubReaderType(readerType)
                            )
                        },
                        options = remember { EpubReaderType.entries.map { LabeledEntry(it, strings.forEpubReaderType(it)) } },
                        onOptionChange = { onReaderChange(it.value) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        minHeight = 44.dp,
                        inputFieldModifier = Modifier.widthIn(min = 180.dp),
                        modifier = Modifier.widthIn(min = 180.dp),
                    )
                },
            )

            AnimatedVisibility(readerType == TTSU_EPUB) {
                val uriHandler = LocalUriHandler.current
                SettingsRow(
                    title = "TTU 阅读器",
                    trailing = {
                        ElevatedButton(
                            onClick = { uriHandler.openUri("https://github.com/ttu-ttu/ebook-reader") },
                            modifier = Modifier.cursorForHand(),
                        ) {
                            Text("打开")
                        }
                    },
                )
            }
        }
    }
}
