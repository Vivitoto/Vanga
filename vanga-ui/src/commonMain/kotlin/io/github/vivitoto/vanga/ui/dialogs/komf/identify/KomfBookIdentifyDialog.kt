package io.github.vivitoto.vanga.ui.dialogs.komf.identify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.dialogs.DialogSimpleHeader
import io.github.vivitoto.vanga.ui.platform.cursorForHand

@Composable
fun KomfBookIdentifyDialog(
    book: VangaBook,
    support: KomfBookIdentifySupport = KomfBookIdentifySupport.Unsupported(noKomfBookIdentifyEndpointReason),
    onDismissRequest: () -> Unit,
) {
    AppDialog(
        modifier = Modifier.widthIn(max = 620.dp),
        header = { DialogSimpleHeader("单本元数据识别（Komf）") },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("目标书籍：${book.metadata.title}", style = MaterialTheme.typography.titleMedium)
                when (support) {
                    KomfBookIdentifySupport.Supported -> Text("当前 Komf 支持单本书籍元数据识别。")
                    is KomfBookIdentifySupport.Unsupported -> Text(
                        support.reason,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Text("为避免误操作，Vanga 不会把单本书籍识别自动降级为系列级识别。")
            }
        },
        controlButtons = {
            Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.cursorForHand(),
                ) {
                    Text("取消")
                }
                FilledTonalButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.cursorForHand(),
                ) {
                    Text("知道了")
                }
            }
        },
        onDismissRequest = onDismissRequest,
    )
}
