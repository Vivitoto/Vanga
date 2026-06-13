package io.github.vivitoto.vanga.ui.settings.epub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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

@Composable
fun EpubReaderSettingsContent(
    readerType: EpubReaderType,
    onReaderChange: (EpubReaderType) -> Unit,
) {
    val strings = LocalStrings.current.settings
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DropdownChoiceMenu(
                selectedOption = remember(readerType) {
                    LabeledEntry(
                        readerType,
                        strings.forEpubReaderType(readerType)
                    )
                },
                options = remember { EpubReaderType.entries.map { LabeledEntry(it, strings.forEpubReaderType(it)) } },
                onOptionChange = { onReaderChange(it.value) },
                label = { Text("阅读器内核") },
                inputFieldModifier = Modifier.fillMaxWidth().animateContentSize(),
                modifier = Modifier.weight(1f),
            )

            AnimatedVisibility(readerType == TTSU_EPUB) {
                val uriHandler = LocalUriHandler.current
                ElevatedButton(
                    onClick = { uriHandler.openUri("https://github.com/ttu-ttu/ebook-reader") },
                    modifier = Modifier.cursorForHand().padding(start = 20.dp)
                ) {
                    Text("打开 TTU 阅读器项目主页")
                }
            }
        }


        when (readerType) {
            TTSU_EPUB -> Text(
                """
                    会一次性加载整本书，部分大文件可能加载较慢或占用更多资源。
                    适合需要更完整 EPUB 排版能力的场景。
                """.trimIndent()
            )

            KOMGA_EPUB -> Text("使用 Vanga 兼容 EPUB 阅读器，兼容性更接近服务器端体验。")

        }
    }
}
