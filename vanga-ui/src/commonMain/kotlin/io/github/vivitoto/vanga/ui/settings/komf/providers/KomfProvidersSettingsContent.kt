package io.github.vivitoto.vanga.ui.settings.komf.providers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import sh.calvin.reorderable.ReorderableColumn
import io.github.vivitoto.vanga.DefaultDateTimeFormats.localDateFormat
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.common.components.ChipFieldWithSuggestions
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.DropdownMultiChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.components.SwitchWithLabel
import io.github.vivitoto.vanga.ui.common.components.UpdateProgressContent
import io.github.vivitoto.vanga.ui.common.components.scrollbar
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.dialogs.tabs.DialogTab
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabDialog
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabItem
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.platform.cursorForMove
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

    LibraryTabs(
        defaultProcessingState = defaultProcessingState,
        libraryProcessingState = libraryProcessingState,
        onLibraryConfigAdd = onLibraryConfigAdd,
        onLibraryConfigRemove = onLibraryConfigRemove,
        libraries = libraries
    ) { state ->
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProvidersConfigContent(state, state::onProviderReorder)

            if (state == defaultProcessingState) {
                HorizontalDivider(Modifier.padding(vertical = 20.dp))
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
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
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
                            .clip(RoundedCornerShape(5.dp))
                            .heightIn(min = 70.dp)
                            .fillMaxWidth()
                            .background(
                                if (isDragging) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f)
                                else MaterialTheme.colorScheme.surfaceVariant
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

                        ProviderCard(item, state::onProviderRemove)

                    }
                }
            }
        }
    }

    AddNewProviderButton(
        onNewProviderAdd = state::onProviderAdd,
        enabledProviders = remember(state.enabledProviders) { state.enabledProviders.map { it.provider } },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewProviderButton(
    onNewProviderAdd: (KomfProviders) -> Unit,
    enabledProviders: List<KomfProviders>,
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
            KomfCoreProviders.entries.filter { it !in enabledProviders }.forEach {
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

        HorizontalDivider()
        Text("MangaBaka 离线数据库", style = MaterialTheme.typography.titleLarge)
        Column {
            if (mangaBakaDbMetadata != null) {
                val downloadDate = remember(mangaBakaDbMetadata) {
                    mangaBakaDbMetadata.downloadTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(localDateFormat)
                }
                Text("下载日期 $downloadDate")
                Text("校验和 ${mangaBakaDbMetadata.checksum}")
            }
            FilledTonalButton(
                onClick = { showMangaBakaDownloadProgress = true },
                modifier = Modifier.cursorForHand()
            ) {
                Text(if (mangaBakaDbMetadata != null) "更新 MangaBaka 数据库" else "下载 MangaBaka 数据库")
            }
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
    onProviderRemove: (ProviderConfigState) -> Unit
) {
    val strings = LocalStrings.current.komf.providerSettings
    var showEditDialog by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "${state.priority}. ${strings.forProvider(state.provider)}",
        )

        IconButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.cursorForHand()
        ) {
            Icon(Icons.Default.Edit, null)
        }
        Spacer(Modifier.weight(1.0f))
        IconButton(
            onClick = { onProviderRemove(state) },
            modifier = Modifier.cursorForHand()
        ) {
            Icon(Icons.Default.Delete, null)
        }
    }

    val tabs = remember(state) {
        listOfNotNull(
            SeriesMetadataTab(state),
            if (state.isBookMetadataAvailable) BookMetadataTab(state) else null,
            ProviderSettingsTab(state)
        )
    }
    var currentTab by remember { mutableStateOf(tabs.first()) }
    if (showEditDialog) {

        TabDialog(
            modifier = when (LocalWindowWidth.current) {
                WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM -> Modifier
                else -> Modifier.widthIn(max = 700.dp)
            },
            title = "Edit ${strings.forProvider(state.provider)}",
            currentTab = currentTab,
            tabs = tabs,
            onTabChange = { currentTab = it },
            showCancelButton = false,
            onConfirm = { showEditDialog = false },
            confirmationText = "关闭",
            onDismissRequest = { showEditDialog = false },
        )
    }
}

private class SeriesMetadataTab(private val state: ProviderConfigState) : DialogTab {
    override fun options() = TabItem(title = "作品元数据")

    @Composable
    override fun Content() {
        Column {
            SwitchWithLabel(
                checked = state.seriesAgeRating,
                onCheckedChange = state::onSeriesAgeRatingChange,
                label = { Text("年龄分级") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesAuthors,
                onCheckedChange = state::onSeriesAuthorsChange,
                label = { Text("作者") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesBookCount,
                onCheckedChange = state::onSeriesBookCountChange,
                label = { Text("单本数量") }
            )
            HorizontalDivider()
            SwitchWithLabel(
                checked = state.seriesCover,
                onCheckedChange = state::onSeriesCoverChange,
                label = { Text("封面") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesGenres,
                onCheckedChange = state::onSeriesGenresChange,
                label = { Text("类型") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesLinks,
                onCheckedChange = state::onSeriesLinksChange,
                label = { Text("链接") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesPublisher,
                onCheckedChange = state::onSeriesPublisherChange,
                label = { Text("出版社") }
            )
            HorizontalDivider()

            if (state.canHaveMultiplePublishers) {
                SwitchWithLabel(
                    checked = state.seriesOriginalPublisher,
                    onCheckedChange = state::onSeriesOriginalPublisherChange,
                    label = { Text("使用原始出版社") },
                    supportingText = { Text("优先使用原始出版社名称，而不是本地化名称") }
                )
                HorizontalDivider()
            }

            SwitchWithLabel(
                checked = state.seriesReleaseDate,
                onCheckedChange = state::onSeriesReleaseDateChange,
                label = { Text("发布日期") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesStatus,
                onCheckedChange = state::onSeriesStatusChange,
                label = { Text("状态") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesSummary,
                onCheckedChange = state::onSeriesSummaryChange,
                label = { Text("简介") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesTags,
                onCheckedChange = state::onSeriesTagsChange,
                label = { Text("标签") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                checked = state.seriesTitle,
                onCheckedChange = state::onSeriesTitleChange,
                label = { Text("标题") }
            )
        }
    }
}

private class BookMetadataTab(private val state: ProviderConfigState) : DialogTab {
    override fun options() = TabItem(title = "单本元数据")

    @Composable
    override fun Content() {
        Column {
            SwitchWithLabel(
                checked = state.bookEnabled,
                onCheckedChange = state::onBookEnabledChange,
                label = { Text("启用") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookAuthors,
                onCheckedChange = state::onBookAuthorsChange,
                label = { Text("作者") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookCover,
                onCheckedChange = state::onBookCoverChange,
                label = { Text("封面") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookIsbn,
                onCheckedChange = state::onBookIsbnChange,
                label = { Text("ISBN") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookLinks,
                onCheckedChange = state::onBookLinksChange,
                label = { Text("链接") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookNumber,
                onCheckedChange = state::onBookNumberChange,
                label = { Text("编号") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookReleaseDate,
                onCheckedChange = state::onBookReleaseDateChange,
                label = { Text("发布日期") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookSummary,
                onCheckedChange = state::onBookSummaryChange,
                label = { Text("简介") }
            )
            HorizontalDivider()

            SwitchWithLabel(
                enabled = state.bookEnabled,
                checked = state.bookTags,
                onCheckedChange = state::onBookTagsChange,
                label = { Text("标签") }
            )
        }
    }
}

private class ProviderSettingsTab(private val state: ProviderConfigState) : DialogTab {
    override fun options() = TabItem(title = "数据源设置")

    @Composable
    override fun Content() {

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            when (state) {
                is GenericProviderConfigState -> {}
                is AniListConfigState -> AniListProviderSettings(state)
                is MangaDexConfigState -> MangaDexProviderSettings(state)
                is MangaBakaConfigState -> MangaBakaProviderSettings(state)
            }

        }
    }

    @Composable
    private fun AniListProviderSettings(state: AniListConfigState) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        HorizontalDivider()

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
