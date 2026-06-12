package io.github.vivitoto.vanga.ui.dialogs.book.editbulk

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.StateHolder
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry.Companion.stringEntry
import io.github.vivitoto.vanga.ui.common.components.LockableChipTextFieldWithSuggestions
import io.github.vivitoto.vanga.ui.dialogs.tabs.DialogTab
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabItem

class TagsTab(
    private val vm: BookBulkEditDialogViewModel
) : DialogTab {
    override fun options() = TabItem(
        title = "标签",
        icon = Icons.Default.LocalOffer
    )

    @Composable
    override fun Content() {
        TagsTabContent(
            tags = StateHolder(vm.tags, vm::tags::set),
            tagsLock = StateHolder(vm.tagsLock, vm::tagsLock::set),
            allTags = vm.allTags
        )
    }
}

@Composable
private fun TagsTabContent(
    tags: StateHolder<List<String>>,
    tagsLock: StateHolder<Boolean>,
    allTags: List<String>,
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val warningColor = MaterialTheme.colorScheme.tertiary
        Row(Modifier.border(Dp.Hairline, warningColor).padding(20.dp)) {
            Icon(Icons.Default.PriorityHigh, null, tint = warningColor)
            Text(
                text = "你正在批量编辑多本书的标签，这会覆盖每本书现有的标签。",
                color = warningColor
            )
        }
        LockableChipTextFieldWithSuggestions(
            values = tags.value,
            onValuesChange = { tags.setValue(it) },
            label = "标签",
            suggestions = remember(allTags) { allTags.map { stringEntry(it) } },
            locked = tagsLock.value,
            onLockChange = { tagsLock.setValue(it) }
        )
    }
}