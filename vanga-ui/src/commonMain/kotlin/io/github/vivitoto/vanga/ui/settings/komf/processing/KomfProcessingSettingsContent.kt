package io.github.vivitoto.vanga.ui.settings.komf.processing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.common.components.ChipFieldWithSuggestions
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.DropdownMultiChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.components.SwitchWithLabel
import io.github.vivitoto.vanga.ui.settings.SettingsSectionHeader
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
            label = { Text("书库类型。会影响部分选项，主要是单本名称解析") },
            inputFieldModifier = Modifier.fillMaxWidth(),
        )

        SwitchWithLabel(
            checked = state.orderBooks,
            onCheckedChange = state::onOrderBooksChange,
            label = { Text("排序单本") },

            supportingText = {
                Text(
                    "尝试按所选书库类型的命名规则排序单本",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        HorizontalDivider()

        SettingsSectionHeader("聚合设置")
        SwitchWithLabel(
            checked = state.aggregate,
            onCheckedChange = state::onAggregateChange,
            label = { Text("聚合") },
            supportingText = {
                Text(
                    "聚合所有已启用数据源的元数据，而不是只采用第一个匹配结果。",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.mergeGenres,
            onCheckedChange = state::onMergeGenresChange,
            enabled = state.aggregate,
            label = { Text("合并类型") },
            supportingText = {
                Text(
                    "启用聚合后，合并多个数据源的类型，而不是只采用第一个匹配结果。",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.mergeTags,
            onCheckedChange = state::onMergeTagsChange,
            enabled = state.aggregate,
            label = { Text("合并标签") },

            supportingText = {
                Text(
                    "启用聚合后，合并多个数据源的标签，而不是只采用第一个匹配结果。",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        HorizontalDivider()
        SettingsSectionHeader("封面设置")
        SwitchWithLabel(
            checked = state.seriesCovers,
            onCheckedChange = state::onSeriesCoversChange,
            label = { Text("作品封面") },

            supportingText = {
                Text(
                    "上传作品封面",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.bookCovers,
            onCheckedChange = state::onBookCoversChange,
            label = { Text("单本封面") },

            supportingText = {
                Text(
                    "上传单本封面",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        SwitchWithLabel(
            checked = state.overrideExistingCovers,
            onCheckedChange = state::onOverrideExistingCoversChange,
            label = { Text("覆盖已有封面") },

            supportingText = {
                Text(
                    "如果条目已有用户上传的封面，则将新上传的封面设为当前封面。\n关闭后只上传新封面，不自动选中。",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )

        HorizontalDivider()
        SettingsSectionHeader("标题设置")
        SwitchWithLabel(
            checked = state.seriesTitle,
            onCheckedChange = state::onSeriesTitleChange,
            label = { Text("作品标题") },

            supportingText = {
                Text(
                    "匹配到的元数据包含标题时更新作品标题",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        SwitchWithLabel(
            checked = state.alternativeSeriesTitles,
            onCheckedChange = state::onAlternativeSeriesTitlesChange,
            label = { Text("作品别名") },

            supportingText = {
                Text(
                    "匹配到的元数据包含别名时更新作品别名",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        )
        SwitchWithLabel(
            checked = state.fallbackToAltTitle,
            onCheckedChange = state::onFallbackToAltTitleChange,
            label = { Text("别名兜底") },

            supportingText = {
                Text(
                    "如果找不到指定语言的主标题，则使用第一个可用别名",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
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
        HorizontalDivider()
        SettingsSectionHeader("默认值")
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

        Spacer(Modifier.height(16.dp))
    }
}
