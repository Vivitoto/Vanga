package io.github.vivitoto.vanga.ui.dialogs.book.edit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.github.vivitoto.vanga.ui.StateHolder
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry.Companion.stringEntry
import io.github.vivitoto.vanga.ui.common.components.LockableChipTextFieldWithSuggestions
import io.github.vivitoto.vanga.ui.dialogs.tabs.DialogTab
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabItem

class TagsTab(
    private val vm: BookEditMetadataState
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
            allTags = vm.allTags.collectAsState().value
        )
    }
}

@Composable
private fun TagsTabContent(
    tags: StateHolder<List<String>>,
    tagsLock: StateHolder<Boolean>,
    allTags: List<String>,
) {
    LockableChipTextFieldWithSuggestions(
        values = tags.value,
        onValuesChange = { tags.setValue(it) },
        label = "标签",
        suggestions = remember(allTags) { allTags.map { stringEntry(it) } },
        locked = tagsLock.value,
        onLockChange = { tagsLock.setValue(it) }
    )
}