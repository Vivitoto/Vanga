package io.github.vivitoto.vanga.ui.dialogs.readlistedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.dialogs.tabs.DialogTab
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabItem
import io.github.vivitoto.vanga.ui.settings.SettingsCheckboxRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard

internal class GeneralTab(
    private val vm: ReadListEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "基本信息",
        icon = Icons.Default.FormatAlignCenter
    )

    @Composable
    override fun Content() {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TextField(
                value = vm.name,
                onValueChange = vm::name::set,
                label = { Text("名称") },
                supportingText = {
                    vm.nameValidationError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                isError = vm.nameValidationError != null,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = vm.summary,
                onValueChange = vm::summary::set,
                label = { Text("简介") },
                minLines = 6,
                maxLines = 12,
                modifier = Modifier.fillMaxWidth()
            )

            SettingsSectionCard(
                title = "排序",
                description = "默认情况下，阅读清单中的书籍按手动顺序排列。你可以关闭手动排序，改为按发布日期排序。",
            ) {
                SettingsCheckboxRow(
                    title = "手动排序",
                    checked = vm.manualOrdering,
                    onCheckedChange = vm::manualOrdering::set,
                )
            }
        }
    }

}
