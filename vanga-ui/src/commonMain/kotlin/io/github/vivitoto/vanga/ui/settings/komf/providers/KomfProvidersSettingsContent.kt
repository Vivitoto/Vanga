package io.github.vivitoto.vanga.ui.settings.komf.providers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import sh.calvin.reorderable.ReorderableColumn
import io.github.vivitoto.vanga.DefaultDateTimeFormats.localDateFormat
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.common.components.ChipFieldWithSuggestions
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.DropdownMultiChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.components.UpdateProgressContent
import io.github.vivitoto.vanga.ui.common.components.scrollbar
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.platform.cursorForMove
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSwitchRow
import io.github.vivitoto.vanga.ui.settings.SettingsValueRow
import io.github.vivitoto.vanga.ui.settings.komf.LibraryTabs
import io.github.vivitoto.vanga.ui.settings.komf.SavableTextField
import io.github.vivitoto.vanga.ui.settings.komf.komfLanguageTagsSuggestions
import io.github.vivitoto.vanga.ui.settings.komf.providers.KomfProvidersSettingsViewModel.ProvidersConfigState
import io.github.vivitoto.vanga.updates.UpdateProgress
import snd.komf.api.KomfAuthorRole
import snd.komf.api.KomfCoreProviders
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfNameMatchingMode
import snd.komf.api.KomfProviders
import snd.komf.api.MangaBakaMode
import snd.komf.api.MangaDexLink
import snd.komf.api.config.MangaBakaDatabaseDto
import snd.komf.api.config.MangaBakaDownloadProgress
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.api.mediaserver.KomfMediaServerLibraryId

@Composable
fun KomfProvidersSettingsContent(
    defaultProcessingState: ProvidersConfigState,
    libraryProcessingState: Map<KomfMediaServerLibraryId, ProvidersConfigState>,

    onLibraryConfigAdd: (libraryId: KomfMediaServerLibraryId) -> Unit,
    onLibraryConfigRemove: (libraryId: KomfMediaServerLibraryId) -> Unit,
    libraries: List<KomfMediaServerLibrary>,

    nameMatchingMode: KomfNameMatchingMode,
    onNameMatchingModeChange: (KomfNameMatchingMode) -> Unit,

    comicVineClientId: String?,
    onComicVineClientIdSave: (String) -> Unit,

    malClientId: String?,
    onMalClientIdSave: (String) -> Unit,

    mangaBakaDbMetadata: MangaBakaDatabaseDto?,
    onMangaBakaUpdate: () -> Flow<MangaBakaDownloadProgress>
) {
    val navigator = LocalNavigator.currentOrThrow

    LibraryTabs(
        defaultProcessingState = defaultProcessingState,
        libraryProcessingState = libraryProcessingState,
        onLibraryConfigAdd = onLibraryConfigAdd,
        onLibraryConfigRemove = onLibraryConfigRemove,
        libraries = libraries
    ) { state ->
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProvidersConfigContent(
                state = state,
                onReorder = state::onProviderReorder,
                onProviderOpen = {
                    navigator.push(
                        KomfProviderDetailSettingsScreen(
                            providerKey = it.provider.providerKey,
                            libraryId = state.libraryId?.value
                        )
                    )
                }
            )

            if (state == defaultProcessingState) {
                CommonSettingsContent(
                    nameMatchingMode,
                    onNameMatchingModeChange = onNameMatchingModeChange,
                    comicVineClientId = comicVineClientId,
                    onComicVineClientIdSave = onComicVineClientIdSave,
                    malClientId = malClientId,
                    onMalClientIdSave = onMalClientIdSave,
                    mangaBakaDbMetadata = mangaBakaDbMetadata,
                    onMangaBakaUpdate = onMangaBakaUpdate
                )

            }
        }

    }
}

@Composable
private fun ProvidersConfigContent(
    state: ProvidersConfigState,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onProviderOpen: (ProviderConfigState) -> Unit,
) {
    SettingsSectionCard(
        title = "启用的数据源",
        description = "拖拽调整优先级，或添加新的元数据来源。",
    ) {
        ReorderableColumn(
            list = state.enabledProviders,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            onSettle = onReorder,
        ) { _, item, isDragging ->
            key(item) {

                ReorderableItem {
                    Row(
                        modifier = Modifier
                            .clip(VangaShape)
                            .heightIn(min = 70.dp)
                            .fillMaxWidth()
                            .background(
                                if (isDragging) MaterialTheme.colorScheme.surface.copy(alpha = .72f)
                                else MaterialTheme.colorScheme.surface
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 5.dp)
                                .widthIn(40.dp)
                                .draggableHandle()
                                .cursorForMove()
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.DragHandle,
                                contentDescription = null,
                            )

                        }

                        ProviderCard(
                            state = item,
                            onProviderOpen = onProviderOpen,
                            onProviderRemove = state::onProviderRemove,
                        )

                    }
                }
            }
        }

        AddNewProviderButton(
            onNewProviderAdd = state::onProviderAdd,
            enabledProviders = remember(state.enabledProviders) { state.enabledProviders.map { it.provider.providerKey } },
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewProviderButton(
    onNewProviderAdd: (KomfProviders) -> Unit,
    enabledProviders: List<String>,
) {
    val strings = LocalStrings.current.komf.providerSettings
    var addProviderExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = addProviderExpanded,
        onExpandedChange = { addProviderExpanded = it },
    ) {
        FilledTonalButton(
            onClick = { addProviderExpanded = true },
            modifier = Modifier
                .cursorForHand()
                .menuAnchor(PrimaryNotEditable)
        ) {
            Text("添加数据源")
        }

        val scrollState = rememberScrollState()
        ExposedDropdownMenu(
            expanded = addProviderExpanded,
            onDismissRequest = { addProviderExpanded = false },
            scrollState = scrollState,
            modifier = Modifier
                .widthIn(min = 200.dp)
                .scrollbar(scrollState, Orientation.Vertical)
        ) {
            (KomfCoreProviders.entries.map { it as KomfProviders } + KomfAverProviders.entries)
                .distinctBy { it.providerKey }
                .filter { it.providerKey !in enabledProviders }
                .forEach {
                DropdownMenuItem(
                    text = { Text(strings.forProvider(it)) },
                    onClick = {
                        addProviderExpanded = false
                        onNewProviderAdd(it)
                    },
                    modifier = Modifier.cursorForHand()
                )

            }
        }
    }

}

@Composable
private fun CommonSettingsContent(
    nameMatchingMode: KomfNameMatchingMode,
    onNameMatchingModeChange: (KomfNameMatchingMode) -> Unit,

    comicVineClientId: String?,
    onComicVineClientIdSave: (String) -> Unit,

    malClientId: String?,
    onMalClientIdSave: (String) -> Unit,
    mangaBakaDbMetadata: MangaBakaDatabaseDto?,
    onMangaBakaUpdate: () -> Flow<MangaBakaDownloadProgress>
) {
    var showMangaBakaDownloadProgress by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsSectionCard(
            title = "通用匹配",
        ) {
            DropdownChoiceMenu(
                selectedOption = remember(nameMatchingMode) {
                    LabeledEntry(
                        nameMatchingMode,
                        nameMatchingMode.name
                    )
                },
                options = remember { KomfNameMatchingMode.entries.map { LabeledEntry(it, it.name) } },
                onOptionChange = { onNameMatchingModeChange(it.value) },
                label = { Text("名称匹配模式") },
                inputFieldModifier = Modifier.fillMaxWidth()
            )

            SavableTextField(
                currentValue = comicVineClientId ?: "",
                onValueSave = onComicVineClientIdSave,
                useEditButton = true,
                label = { Text("ComicVine 客户端 ID") }
            )
            SavableTextField(
                currentValue = malClientId ?: "",
                onValueSave = onMalClientIdSave,
                useEditButton = true,
                label = { Text("MyAnimeList 客户端 ID") }
            )
        }

        SettingsSectionCard(
            title = "MangaBaka 离线数据库",
        ) {
            if (mangaBakaDbMetadata != null) {
                val downloadDate = remember(mangaBakaDbMetadata) {
                    mangaBakaDbMetadata.downloadTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(localDateFormat)
                }
                SettingsValueRow("下载日期", downloadDate)
                SettingsValueRow("校验和", mangaBakaDbMetadata.checksum.toString())
            }
            SettingsRow(
                title = "数据库文件",
                trailing = {
                    FilledTonalButton(
                        onClick = { showMangaBakaDownloadProgress = true },
                        modifier = Modifier.cursorForHand()
                    ) {
                        Text(if (mangaBakaDbMetadata != null) "更新" else "下载")
                    }
                }
            )
        }
        if (showMangaBakaDownloadProgress) {
            MangaBakaDbDownloadContent(
                onMangaBakaUpdate,
                { showMangaBakaDownloadProgress = false })
        }
    }
}

@Composable
private fun MangaBakaDbDownloadContent(
    onDownloadRequest: () -> Flow<MangaBakaDownloadProgress>,
    onDismiss: () -> Unit,
) {
    var progress by remember { mutableStateOf(UpdateProgress(0, 0)) }
    var error by remember { mutableStateOf<String?>(null) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onDownloadRequest().collect { event ->
            when (event) {
                is MangaBakaDownloadProgress.ProgressEvent -> progress = UpdateProgress(
                    event.total,
                    event.completed,
                    event.info
                )

                is MangaBakaDownloadProgress.ErrorEvent -> {
                    error = event.message
                    completed = true
                }

                MangaBakaDownloadProgress.FinishedEvent -> completed = true
            }
        }
    }

    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("正在下载 MangaBaka 数据库", style = MaterialTheme.typography.titleLarge)
                HorizontalDivider(Modifier.padding(top = 10.dp))
            }
        },
        content = {
            val errorText = error
            when {
                errorText != null -> Text(errorText, Modifier.padding(20.dp))
                completed -> Text("完成", Modifier.padding(20.dp))
                else -> UpdateProgressContent(
                    progress.total,
                    progress.completed,
                    progress.description
                )
            }
        },
        controlButtons = {
            Box(modifier = Modifier.padding(bottom = 10.dp, end = 10.dp)) {
                if (completed) {
                    FilledTonalButton(
                        onClick = onDismiss,
                        modifier = Modifier.cursorForHand(),
                        content = {
                            Text("关闭")
                        }
                    )

                } else {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.cursorForHand(),
                        content = {
                            Text("关闭")
                        }
                    )
                }
            }
        },
        onDismissRequest = onDismiss
    )
}

@Composable
private fun ProviderCard(
    state: ProviderConfigState,
    onProviderOpen: (ProviderConfigState) -> Unit,
    onProviderRemove: (ProviderConfigState) -> Unit
) {
    val strings = LocalStrings.current.komf.providerSettings
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProviderOpen(state) }
            .cursorForHand()
            .padding(end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "${state.priority}. ${strings.forProvider(state.provider)}",
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Icon(Icons.Default.ChevronRight, null)
        IconButton(
            onClick = { onProviderRemove(state) },
            modifier = Modifier.cursorForHand()
        ) {
            Icon(Icons.Default.Delete, null)
        }
    }
}

@Composable
fun ProviderDetailSettingsContent(
    state: ProviderConfigState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SeriesMetadataSettings(state)
        if (state.isBookMetadataAvailable) BookMetadataSettings(state)
        ProviderSettings(state)
    }
}

@Composable
private fun SeriesMetadataSettings(state: ProviderConfigState) {
    SettingsSectionCard(
        title = "作品元数据",
    ) {
        SettingsSwitchRow(
            title = "年龄分级",
            checked = state.seriesAgeRating,
            onCheckedChange = state::onSeriesAgeRatingChange,
        )

        SettingsSwitchRow(
            title = "作者",
            checked = state.seriesAuthors,
            onCheckedChange = state::onSeriesAuthorsChange,
        )

        SettingsSwitchRow(
            title = "单本数量",
            checked = state.seriesBookCount,
            onCheckedChange = state::onSeriesBookCountChange,
        )
        SettingsSwitchRow(
            title = "封面",
            checked = state.seriesCover,
            onCheckedChange = state::onSeriesCoverChange,
        )

        SettingsSwitchRow(
            title = "类型",
            checked = state.seriesGenres,
            onCheckedChange = state::onSeriesGenresChange,
        )

        SettingsSwitchRow(
            title = "链接",
            checked = state.seriesLinks,
            onCheckedChange = state::onSeriesLinksChange,
        )

        SettingsSwitchRow(
            title = "出版社",
            checked = state.seriesPublisher,
            onCheckedChange = state::onSeriesPublisherChange,
        )

        if (state.canHaveMultiplePublishers) {
            SettingsSwitchRow(
                title = "使用原始出版社",
                supportingText = "优先使用原始出版社名称，而不是本地化名称。",
                checked = state.seriesOriginalPublisher,
                onCheckedChange = state::onSeriesOriginalPublisherChange,
            )
        }

        SettingsSwitchRow(
            title = "发布日期",
            checked = state.seriesReleaseDate,
            onCheckedChange = state::onSeriesReleaseDateChange,
        )

        SettingsSwitchRow(
            title = "状态",
            checked = state.seriesStatus,
            onCheckedChange = state::onSeriesStatusChange,
        )

        SettingsSwitchRow(
            title = "简介",
            checked = state.seriesSummary,
            onCheckedChange = state::onSeriesSummaryChange,
        )

        SettingsSwitchRow(
            title = "标签",
            checked = state.seriesTags,
            onCheckedChange = state::onSeriesTagsChange,
        )

        SettingsSwitchRow(
            title = "标题",
            checked = state.seriesTitle,
            onCheckedChange = state::onSeriesTitleChange,
        )
    }
}

@Composable
private fun BookMetadataSettings(state: ProviderConfigState) {
    SettingsSectionCard(
        title = "单本元数据",
    ) {
        SettingsSwitchRow(
            title = "启用",
            checked = state.bookEnabled,
            onCheckedChange = state::onBookEnabledChange,
        )

        SettingsSwitchRow(
            title = "作者",
            enabled = state.bookEnabled,
            checked = state.bookAuthors,
            onCheckedChange = state::onBookAuthorsChange,
        )

        SettingsSwitchRow(
            title = "封面",
            enabled = state.bookEnabled,
            checked = state.bookCover,
            onCheckedChange = state::onBookCoverChange,
        )

        SettingsSwitchRow(
            title = "ISBN",
            enabled = state.bookEnabled,
            checked = state.bookIsbn,
            onCheckedChange = state::onBookIsbnChange,
        )

        SettingsSwitchRow(
            title = "链接",
            enabled = state.bookEnabled,
            checked = state.bookLinks,
            onCheckedChange = state::onBookLinksChange,
        )

        SettingsSwitchRow(
            title = "编号",
            enabled = state.bookEnabled,
            checked = state.bookNumber,
            onCheckedChange = state::onBookNumberChange,
        )

        SettingsSwitchRow(
            title = "发布日期",
            enabled = state.bookEnabled,
            checked = state.bookReleaseDate,
            onCheckedChange = state::onBookReleaseDateChange,
        )

        SettingsSwitchRow(
            title = "简介",
            enabled = state.bookEnabled,
            checked = state.bookSummary,
            onCheckedChange = state::onBookSummaryChange,
        )

        SettingsSwitchRow(
            title = "标签",
            enabled = state.bookEnabled,
            checked = state.bookTags,
            onCheckedChange = state::onBookTagsChange,
        )
    }
}

@Composable
private fun ProviderSettings(state: ProviderConfigState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsSectionCard(
            title = "匹配设置",
        ) {
            DropdownChoiceMenu(
                selectedOption = remember(state.mediaType) {
                    LabeledEntry(
                        state.mediaType,
                        state.mediaType?.name ?: "未设置"
                    )
                },
                options = remember {
                    listOf(LabeledEntry<KomfMediaType?>(null, "未设置")) +
                            KomfMediaType.entries.map { LabeledEntry(it, it.name) }
                },
                onOptionChange = { state.onMediaTypeChange(it.value) },
                label = { Text("媒体类型") },
                inputFieldModifier = Modifier.fillMaxWidth()
            )

            DropdownChoiceMenu(
                selectedOption = remember(state.nameMatchingMode) {
                    LabeledEntry(
                        state.nameMatchingMode,
                        state.nameMatchingMode?.name ?: "未设置"
                    )
                },
                options = remember {
                    listOf(LabeledEntry<KomfNameMatchingMode?>(null, "未设置")) +
                            KomfNameMatchingMode.entries.map { LabeledEntry(it, it.name) }
                },
                onOptionChange = { state.onNameMatchingModeChange(it.value) },
                label = { Text("名称匹配模式") },
                inputFieldModifier = Modifier.fillMaxWidth()
            )

            DropdownMultiChoiceMenu(
                selectedOptions = remember(state.authorRoles) { state.authorRoles.map { LabeledEntry(it, it.name) } },
                options = remember { KomfAuthorRole.entries.map { LabeledEntry(it, it.name) } },
                onOptionSelect = { state.onAuthorSelect(it.value) },
                label = { Text("作者角色") },
                placeholder = "未设置",
                inputFieldModifier = Modifier.fillMaxWidth()
            )
            DropdownMultiChoiceMenu(
                selectedOptions = remember(state.artistRoles) { state.artistRoles.map { LabeledEntry(it, it.name) } },
                options = remember { KomfAuthorRole.entries.map { LabeledEntry(it, it.name) } },
                onOptionSelect = { state.onArtistSelect(it.value) },
                label = { Text("画师角色") },
                placeholder = "未设置",
                inputFieldModifier = Modifier.fillMaxWidth()
            )
        }
        when (state) {
            is GenericProviderConfigState -> {}
            is EHentaiConfigState -> EHentaiProviderSettings(state)
            is KomfAverProviderConfigState -> {}
            is AniListConfigState -> AniListProviderSettings(state)
            is MangaDexConfigState -> MangaDexProviderSettings(state)
            is MangaBakaConfigState -> MangaBakaProviderSettings(state)
        }
    }
}

@Composable
private fun AniListProviderSettings(state: AniListConfigState) {
    SettingsSectionCard(
        title = "AniList 设置",
    ) {
        SavableTextField(
            currentValue = remember(state.tagScoreThreshold) { state.tagScoreThreshold.toString() },
            onValueSave = { state.onTagScoreThresholdChange(it.toInt()) },
            valueChangePolicy = { it.toIntOrNull() != null },
            label = { Text("标签分数阈值") }
        )

        SavableTextField(
            currentValue = remember(state.tagSizeLimit) { state.tagSizeLimit.toString() },
            onValueSave = { state.onTagSizeLimitChange(it.toInt()) },
            valueChangePolicy = { it.toIntOrNull() != null },
            label = { Text("标签数量上限") }
        )
    }
}

@Composable
private fun MangaDexProviderSettings(state: MangaDexConfigState) {
    SettingsSectionCard(
        title = "MangaDex 设置",
    ) {
        ChipFieldWithSuggestions(
            label = { Text("别名语言（ISO 639）") },
            values = state.coverLanguages,
            onValuesChange = state::onCoverLanguagesChange,
            suggestions = komfLanguageTagsSuggestions
        )
        DropdownMultiChoiceMenu(
            selectedOptions = state.links.map { LabeledEntry(it, it.name) },
            options = MangaDexLink.entries.map { LabeledEntry(it, it.name) },
            onOptionSelect = { state.onLinkSelect(it.value) },
            label = { Text("包含链接") },
            placeholder = "全部",
            inputFieldModifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MangaBakaProviderSettings(state: MangaBakaConfigState) {
    SettingsSectionCard("MangaBaka 设置") {
        DropdownChoiceMenu(
            selectedOption = remember(state.mode) {
                LabeledEntry(
                    state.mode,
                    state.mode.name
                )
            },
            options = remember {
                MangaBakaMode.entries.map { LabeledEntry(it, it.name) }
            },
            onOptionChange = { state.onModeChange(it.value) },
            label = { Text("数据源类型") },
            inputFieldModifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EHentaiProviderSettings(state: EHentaiConfigState) {
    SettingsSectionCard("EHentai / ExHentai 设置") {
        SettingsSwitchRow(
            title = "使用 ExHentai",
            checked = state.useExhentai,
            onCheckedChange = state::onUseExhentaiChange,
        )
        SavableTextField(
            currentValue = if (state.hasCookieAuth) SECRET_PLACEHOLDER else "",
            onValueSave = state::onCookieHeaderChange,
            useEditButton = true,
            isPassword = true,
            label = { Text("Cookie") }
        )
        SavableTextField(
            currentValue = state.userAgent ?: "",
            onValueSave = state::onUserAgentChange,
            useEditButton = true,
            label = { Text("User-Agent") }
        )
    }
}
