package io.github.vivitoto.vanga.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.StateHolder
import io.github.vivitoto.vanga.ui.dialogs.tabs.DialogTab
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabItem
import io.github.vivitoto.vanga.ui.settings.SettingsCheckboxRow
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard

internal class MetadataTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "元数据",
        icon = Icons.Default.Book
    )

    @Composable
    override fun Content() {
        MetadataTabContent(
            importComicInfoBook = StateHolder(vm.importComicInfoBook, vm::importComicInfoBook::set),
            importComicInfoSeries = StateHolder(vm.importComicInfoSeries, vm::importComicInfoSeries::set),
            importComicInfoSeriesAppendVolume = StateHolder(
                vm.importComicInfoSeriesAppendVolume,
                vm::importComicInfoSeriesAppendVolume::set
            ),
            importComicInfoCollection = StateHolder(
                vm.importComicInfoCollection,
                vm::importComicInfoCollection::set
            ),
            importComicInfoReadList = StateHolder(vm.importComicInfoReadList, vm::importComicInfoReadList::set),
            importEpubBook = StateHolder(vm.importEpubBook, vm::importEpubBook::set),
            importEpubSeries = StateHolder(vm.importEpubSeries, vm::importEpubSeries::set),
            importMylarSeries = StateHolder(vm.importMylarSeries, vm::importMylarSeries::set),
            importLocalArtwork = StateHolder(vm.importLocalArtwork, vm::importLocalArtwork::set),
            importBarcodeIsbn = StateHolder(vm.importBarcodeIsbn, vm::importBarcodeIsbn::set),
        )
    }
}


@Composable
private fun MetadataTabContent(
    importComicInfoBook: StateHolder<Boolean>,
    importComicInfoSeries: StateHolder<Boolean>,
    importComicInfoSeriesAppendVolume: StateHolder<Boolean>,
    importComicInfoCollection: StateHolder<Boolean>,
    importComicInfoReadList: StateHolder<Boolean>,
    importEpubBook: StateHolder<Boolean>,
    importEpubSeries: StateHolder<Boolean>,
    importMylarSeries: StateHolder<Boolean>,
    importLocalArtwork: StateHolder<Boolean>,
    importBarcodeIsbn: StateHolder<Boolean>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ComicInfoSettings(
            importComicInfoBook = importComicInfoBook,
            importComicInfoSeries = importComicInfoSeries,
            importComicInfoSeriesAppendVolume = importComicInfoSeriesAppendVolume,
            importComicInfoCollection = importComicInfoCollection,
            importComicInfoReadList = importComicInfoReadList,
        )
        EpubSettings(
            importEpubBook = importEpubBook,
            importEpubSeries = importEpubSeries
        )
        MylarSettings(importMylarSeries)
        LocalArtworkSettings(importLocalArtwork)
        BarcodeISBNSettings(importBarcodeIsbn)
    }
}

@Composable
private fun ComicInfoSettings(
    importComicInfoBook: StateHolder<Boolean>,
    importComicInfoSeries: StateHolder<Boolean>,
    importComicInfoSeriesAppendVolume: StateHolder<Boolean>,
    importComicInfoCollection: StateHolder<Boolean>,
    importComicInfoReadList: StateHolder<Boolean>,
) {
    SettingsSectionCard(
        title = "ComicInfo.xml",
        description = "从包含 ComicInfo.xml 的 CBR/CBZ 导入元数据。",
    ) {
        SettingsChildCheckboxRow(
            title = "全部 ComicInfo 元数据",
            children = listOf(
                importComicInfoBook,
                importComicInfoSeries,
                importComicInfoSeriesAppendVolume,
                importComicInfoCollection,
                importComicInfoReadList
            ),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            SettingsCheckboxRow(
                title = "书籍元数据",
                checked = importComicInfoBook.value,
                onCheckedChange = importComicInfoBook.setValue,
            )

            SettingsCheckboxRow(
                title = "系列元数据",
                checked = importComicInfoSeries.value,
                onCheckedChange = importComicInfoSeries.setValue,
            )

            SettingsCheckboxRow(
                title = "将卷号追加到系列标题",
                checked = importComicInfoSeriesAppendVolume.value,
                onCheckedChange = importComicInfoSeriesAppendVolume.setValue,
            )

            SettingsCheckboxRow(
                title = "合集",
                checked = importComicInfoCollection.value,
                onCheckedChange = importComicInfoCollection.setValue,
            )

            SettingsCheckboxRow(
                title = "阅读清单",
                checked = importComicInfoReadList.value,
                onCheckedChange = importComicInfoReadList.setValue,
            )
        }
    }
}

@Composable
private fun EpubSettings(
    importEpubBook: StateHolder<Boolean>,
    importEpubSeries: StateHolder<Boolean>,
) {
    SettingsSectionCard(
        title = "EPUB",
        description = "从 EPUB 文件导入元数据。",
    ) {
        SettingsChildCheckboxRow(
            title = "全部 EPUB 元数据",
            children = listOf(
                importEpubBook,
                importEpubSeries,
            ),
        )
        Column(Modifier.padding(start = 12.dp)) {
            SettingsCheckboxRow(
                title = "书籍元数据",
                checked = importEpubBook.value,
                onCheckedChange = importEpubBook.setValue,
            )
            SettingsCheckboxRow(
                title = "系列元数据",
                checked = importEpubSeries.value,
                onCheckedChange = importEpubSeries.setValue,
            )
        }
    }
}

@Composable
private fun MylarSettings(
    importMylarSeries: StateHolder<Boolean>,
) {
    SettingsSectionCard("Mylar", description = "导入 Mylar 生成的元数据。") {
        SettingsCheckboxRow(
            title = "系列元数据",
            checked = importMylarSeries.value,
            onCheckedChange = importMylarSeries.setValue,
        )
    }
}

@Composable
private fun LocalArtworkSettings(
    importLocalArtwork: StateHolder<Boolean>,
) {
    SettingsSectionCard("本地媒体资源", description = "导入本地媒体资源。") {
        SettingsCheckboxRow(
            title = "本地封面",
            checked = importLocalArtwork.value,
            onCheckedChange = importLocalArtwork.setValue,
        )
    }
}

@Composable
private fun BarcodeISBNSettings(
    importBarcodeIsbn: StateHolder<Boolean>,
) {
    SettingsSectionCard("条形码", description = "从条形码导入 ISBN。") {
        SettingsCheckboxRow(
            title = "ISBN 条形码",
            checked = importBarcodeIsbn.value,
            onCheckedChange = importBarcodeIsbn.setValue,
        )
    }
}

@Composable
private fun SettingsChildCheckboxRow(
    title: String,
    children: List<StateHolder<Boolean>>,
) {
    val selectedCount = children.count { it.value }
    val state = when (selectedCount) {
        children.size -> ToggleableState.On
        0 -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }
    SettingsRow(
        title = title,
        onClick = {
            val nextValue = state == ToggleableState.Off
            children.forEach { it.setValue(nextValue) }
        },
        trailing = {
            TriStateCheckbox(
                state = state,
                onClick = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.secondaryContainer,
                    checkmarkColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    )
}
