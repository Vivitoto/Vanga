package io.github.vivitoto.vanga.ui.dialogs.series.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.StateHolder
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry.Companion.stringEntry
import io.github.vivitoto.vanga.ui.common.components.LockableChipTextFieldWithSuggestions
import io.github.vivitoto.vanga.ui.dialogs.tabs.DialogTab
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabItem

internal class TagsTab(
    private val vm: SeriesEditMetadataState,
) : DialogTab {

    override fun options() = TabItem(
        title = "标签",
        icon = Icons.Default.LocalOffer
    )

    @Composable
    override fun Content() {
        TagsContent(
            tags = StateHolder(vm.tags, vm::tags::set),
            tagsLock = StateHolder(vm.tagsLock, vm::tagsLock::set),
            genres = StateHolder(vm.genres, vm::genres::set),
            genresLock = StateHolder(vm.genresLock, vm::genresLock::set),
            allTags = vm.allTags.collectAsState().value,
            allGenres = vm.allGenres.collectAsState().value
        )
    }
}

@Composable
private fun TagsContent(
    tags: StateHolder<List<String>>,
    tagsLock: StateHolder<Boolean>,
    genres: StateHolder<List<String>>,
    genresLock: StateHolder<Boolean>,
    allTags: List<String>,
    allGenres: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        LockableChipTextFieldWithSuggestions(
            values = tags.value,
            onValuesChange = { tags.setValue(it) },
            label = "标签",
            suggestions = remember(allTags) { allTags.map { stringEntry(it) } },
            locked = tagsLock.value,
            onLockChange = { tagsLock.setValue(it) }
        )
        LockableChipTextFieldWithSuggestions(
            values = genres.value,
            onValuesChange = { genres.setValue(it) },
            label = "流派",
            suggestions = remember(allGenres) { allGenres.map { stringEntry(it) } },
            locked = genresLock.value,
            onLockChange = { genresLock.setValue(it) }
        )
    }
}
