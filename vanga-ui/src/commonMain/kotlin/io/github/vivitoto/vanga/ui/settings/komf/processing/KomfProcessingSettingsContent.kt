package io.github.vivitoto.vanga.ui.settings.komf.processing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.common.components.ChipFieldWithSuggestions
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.DropdownMultiChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSwitchRow
import io.github.vivitoto.vanga.ui.settings.komf.LanguageSelectionField
import io.github.vivitoto.vanga.ui.settings.komf.LibraryTabs
import io.github.vivitoto.vanga.ui.settings.komf.komfLanguageTagsSuggestions
import io.github.vivitoto.vanga.ui.settings.komf.processing.KomfProcessingSettingsViewModel.ProcessingConfigState
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfReadingDirection
import snd.komf.api.KomfUpdateMode
import snd.komf.api.MediaServer
import snd.komf.api.MediaServer.KOMGA
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

@Composable
fun KomfProcessingSettingsContent(
    defaultProcessingState: ProcessingConfigState,
    libraryProcessingState: Map<KomfMediaServerLibraryId, ProcessingConfigState>,

    onLibraryConfigAdd: (libraryId: KomfMediaServerLibraryId) -> Unit,
    onLibraryConfigRemove: (libraryId: KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>,
    serverType: MediaServer,
) {
    LibraryTabs(
        defaultProcessingState,
        libraryProcessingState,
        onLibraryConfigAdd, onLibraryConfigRemove, libraries
    ) {

        ProcessingConfigContent(it, serverType)
    }
}

@Composable
private fun ProcessingConfigContent(
    state: ProcessingConfigState,
    serverType: MediaServer,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsSectionCard(
            title = "基础规则",
        ) {
            DropdownMultiChoiceMenu(
                selectedOptions = state.updateModes.map { LabeledEntry(it, it.name) },
                options = remember { KomfUpdateMode.entries.map { LabeledEntry(it, it.name) } },
                onOptionSelect = { state.onUpdateModeSelect(it.value) },
                label = { Text("更新模式") },
                placeholder = "无",
                inputFieldModifier = Modifier.fillMaxWidth()
            )

            DropdownChoiceMenu(
                selectedOption = LabeledEntry(state.libraryType, state.libraryType.name),
                options = remember { KomfMediaType.entries.map { LabeledEntry(it, it.name) } },
                onOptionChange = { state.onLibraryTypeChange(it.value) },
                label = { Text("书库类型") },
                inputFieldModifier = Modifier.fillMaxWidth(),
            )

            SettingsSwitchRow(
                title = "排序单本",
                supportingText = "尝试按所选书库类型的命名规则排序单本。",
                checked = state.orderBooks,
                onCheckedChange = state::onOrderBooksChange,
            )
        }

        SettingsSectionCard("聚合设置") {
            SettingsSwitchRow(
                title = "聚合",
                supportingText = "聚合所有已启用数据源的元数据，而不是只采用第一个匹配结果。",
                checked = state.aggregate,
                onCheckedChange = state::onAggregateChange,
            )

            SettingsSwitchRow(
                title = "合并类型",
                supportingText = "启用聚合后，合并多个数据源的类型。",
                checked = state.mergeGenres,
                onCheckedChange = state::onMergeGenresChange,
                enabled = state.aggregate,
            )

            SettingsSwitchRow(
                title = "合并标签",
                supportingText = "启用聚合后，合并多个数据源的标签。",
                checked = state.mergeTags,
                onCheckedChange = state::onMergeTagsChange,
                enabled = state.aggregate,
            )
        }

        SettingsSectionCard("封面设置") {
            SettingsSwitchRow(
                title = "作品封面",
                checked = state.seriesCovers,
                onCheckedChange = state::onSeriesCoversChange,
            )

            SettingsSwitchRow(
                title = "单本封面",
                checked = state.bookCovers,
                onCheckedChange = state::onBookCoversChange,
            )

            SettingsSwitchRow(
                title = "覆盖已有封面",
                supportingText = "已有用户上传封面时，将新封面设为当前封面。",
                checked = state.overrideExistingCovers,
                onCheckedChange = state::onOverrideExistingCoversChange,
            )
        }

        SettingsSectionCard("标题设置") {
            SettingsSwitchRow(
                title = "作品标题",
                supportingText = "匹配到的元数据包含标题时更新作品标题。",
                checked = state.seriesTitle,
                onCheckedChange = state::onSeriesTitleChange,
            )
            SettingsSwitchRow(
                title = "作品别名",
                supportingText = "匹配到的元数据包含别名时更新作品别名。",
                checked = state.alternativeSeriesTitles,
                onCheckedChange = state::onAlternativeSeriesTitlesChange,
            )
            SettingsSwitchRow(
                title = "别名兜底",
                supportingText = "找不到指定语言主标题时，使用第一个可用别名。",
                checked = state.fallbackToAltTitle,
                onCheckedChange = state::onFallbackToAltTitleChange,
            )
            LanguageSelectionField(
                label = "作品标题语言（ISO 639）",
                languageValue = state.seriesTitleLanguage,
                onLanguageValueChange = state::onSeriesTitleLanguageChange,
                onLanguageValueSave = state::onSeriesTitleLanguageSave
            )
            ChipFieldWithSuggestions(
                label = { Text("别名语言（ISO 639）") },
                values = state.alternativeSeriesTitleLanguages,
                onValuesChange = state::onAlternativeTitleLanguagesChange,
                suggestions = komfLanguageTagsSuggestions
            )
        }

        SettingsSectionCard("默认值") {
            if (serverType == KOMGA) {
                DropdownChoiceMenu(
                    selectedOption = LabeledEntry(state.readingDirectionValue, state.readingDirectionValue?.name ?: "无"),
                    options = remember {
                        listOf(LabeledEntry<KomfReadingDirection?>(null, "无")) +
                                KomfReadingDirection.entries.map { LabeledEntry(it, it.name) }
                    },
                    onOptionChange = { state.onReadingDirectionChange(it.value) },
                    label = { Text("默认作品阅读方向") },
                    inputFieldModifier = Modifier.fillMaxWidth(),
                )
            }
            LanguageSelectionField(
                label = "默认作品语言",
                languageValue = state.defaultLanguageValue ?: "",
                onLanguageValueChange = state::onDefaultLanguageChange,
                onLanguageValueSave = state::onDefaultLanguageSave
            )
        }
    }
}
