package io.github.vivitoto.vanga.ui.settings.komf.notifications.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.ChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import io.github.vivitoto.vanga.ui.common.components.NumberField
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.dialogs.DialogSimpleHeader
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.komf.notifications.NotificationContextState
import io.github.vivitoto.vanga.ui.settings.komf.notifications.NotificationContextState.AlternativeTitleContext
import io.github.vivitoto.vanga.ui.settings.komf.notifications.NotificationContextState.AuthorContext
import io.github.vivitoto.vanga.ui.settings.komf.notifications.NotificationContextState.BookContextState
import io.github.vivitoto.vanga.ui.settings.komf.notifications.NotificationContextState.WebLinkContext


@Composable
fun NotificationContextDialog(
    notificationContextState: NotificationContextState,
    onDismissRequest: () -> Unit,
) {
    AppDialog(
        modifier = Modifier.widthIn(max = 800.dp),
        header = { DialogSimpleHeader("预览上下文") },
        content = { NotificationContextDialogContent(notificationContextState) },
        controlButtons = {
            FilledTonalButton(
                onClick = onDismissRequest,
            ) {
                Text("关闭")
            }
        },
        onDismissRequest = onDismissRequest,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
fun NotificationContextDialogContent(
    state: NotificationContextState,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsSectionCard(
            title = "书库",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = state.libraryId,
                    onValueChange = state::libraryId::set,
                    label = { Text("ID \$library.id") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.libraryName,
                    onValueChange = state::libraryName::set,
                    label = { Text("名称 \$library.name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SettingsSectionCard(
            title = "作品",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = state.seriesId,
                    onValueChange = state::seriesId::set,
                    label = { Text("ID \$series.id") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.seriesName,
                    onValueChange = state::seriesName::set,
                    label = { Text("名称 \$series.name") },
                    modifier = Modifier.fillMaxWidth()
                )
                NumberField(
                    value = state.seriesBookCount,
                    onValueChange = { state.seriesBookCount = it },
                    label = { Text("单本数量 \$series.bookCount") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.seriesStatus,
                    onValueChange = state::seriesStatus::set,
                    label = { Text("状态 \$series.metadata.status") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.seriesTitle,
                    onValueChange = state::seriesTitle::set,
                    label = { Text("元数据标题 \$series.metadata.title") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.seriesTitleSort,
                    onValueChange = state::seriesTitleSort::set,
                    label = { Text("元数据排序标题 \$series.metadata.titleSort") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.seriesSummary,
                    onValueChange = state::seriesSummary::set,
                    label = { Text("简介 \$series.metadata.summary") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.seriesReadingDirection,
                    onValueChange = state::seriesReadingDirection::set,
                    label = { Text("阅读方向 \$series.metadata.readingDirection") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.seriesPublisher,
                    onValueChange = state::seriesPublisher::set,
                    label = { Text("出版社 \$series.metadata.publisher") },
                    modifier = Modifier.fillMaxWidth()
                )
                NumberField(
                    value = state.seriesAgeRating,
                    onValueChange = state::seriesAgeRating::set,
                    label = { Text("年龄分级 \$series.metadata.ageRating") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = state.seriesLanguage,
                    onValueChange = state::seriesLanguage::set,
                    label = { Text("语言 \$series.metadata.language") },
                    modifier = Modifier.fillMaxWidth()
                )
                NumberField(
                    value = state.seriesTotalBookCount,
                    onValueChange = state::seriesTotalBookCount::set,
                    label = { Text("总单本数量 \$series.metadata.totalBookCount") },
                    modifier = Modifier.fillMaxWidth()
                )
                NumberField(
                    value = state.seriesReleaseYer,
                    onValueChange = state::seriesReleaseYer::set,
                    label = { Text("发布年份 \$series.metadata.releaseYear") },
                    modifier = Modifier.fillMaxWidth()
                )
                StringValueList(state.seriesGenres, state::seriesGenres::set, "类型 \$series.metadata.genres[i]")
                StringValueList(state.seriesTags, state::seriesTags::set, "标签 \$series.metadata.tags[i]")
                StringValueList(
                    state.seriesAlternativePublishers,
                    state::seriesAlternativePublishers::set,
                    "备用出版社 \$series.metadata.alternativePublishers[i]"
                )
                Column(
                    modifier = Modifier.padding(start = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ValueList(
                        values = state.seriesAlternativeTitles,
                        valueName = "别名",
                        onAdd = state::onSeriesAlternativeTitleAdd,
                        onDelete = state::onSeriesAlternativeTitleDelete,
                        content = { AlternativeTitlesEdit(it) }
                    )
                    ValueList(
                        values = state.seriesAuthors,
                        valueName = "作者",
                        onAdd = state::onSeriesAuthorAdd,
                        onDelete = state::onSeriesAuthorDelete,
                        content = { AuthorsEdit(it) }
                    )
                    ValueList(
                        values = state.seriesLinks,
                        valueName = "链接",
                        onAdd = state::onSeriesLinkAdd,
                        onDelete = state::onSeriesLinkDelete,
                        content = { WebLinksEdit(it) }
                    )
                }
            }
        }

        SettingsSectionCard(
            title = "单本",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.books.forEachIndexed { index, book ->
                    var showBook by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showBook = !showBook }.cursorForHand()

                        ) {
                            Icon(if (showBook) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                            Text("单本 ${index + 1}")
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { state.onBookDelete(book) }) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                        AnimatedVisibility(
                            visible = showBook,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            BookContext(book)
                        }
                        if (index < state.books.lastIndex) {
                            HorizontalDivider()
                        }
                    }

                }

                FilledTonalButton(onClick = state::onBookAdd) { Text("添加单本") }
            }
        }
    }
}


@Composable
private fun BookContext(state: BookContextState) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        TextField(
            value = state.id,
            onValueChange = state::id::set,
            label = { Text("ID \$books[i].id") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.name,
            onValueChange = state::name::set,
            label = { Text("名称 \$books[i].name") },
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = state.number,
            onValueChange = { state.number = it ?: 0 },
            label = { Text("编号 \$books[i].number") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.title,
            onValueChange = state::title::set,
            label = { Text("元数据标题 \$books[i].metadata.title") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.summary,
            onValueChange = state::summary::set,
            label = { Text("简介 \$books[i].metadata.summary") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.metadataNumber,
            onValueChange = state::metadataNumber::set,
            label = { Text("元数据编号 \$books[i].metadata.number") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.metadataNumberSort,
            onValueChange = state::metadataNumberSort::set,
            label = { Text("元数据排序编号 \$books[i].metadata.numberSort") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.releaseDate,
            onValueChange = state::releaseDate::set,
            label = { Text("发布日期 \$books[i].metadata.releaseDate") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.isbn,
            onValueChange = state::isbn::set,
            label = { Text("ISBN \$books[i].metadata.isbn") },
            modifier = Modifier.fillMaxWidth()
        )

        StringValueList(state.tags, state::tags::set, "标签 \$book[i].metadata.tags[i]")
        Column(
            modifier = Modifier.padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ValueList(
                values = state.authors,
                valueName = "作者",
                onAdd = state::onAuthorAdd,
                onDelete = state::onAuthorDelete,
                content = { AuthorsEdit(it) }
            )
            ValueList(
                values = state.links,
                valueName = "链接",
                onAdd = state::onLinkAdd,
                onDelete = state::onLinkDelete,
                content = { WebLinksEdit(it) }
            )
        }
    }
}


@Composable
private fun StringValueList(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    label: String,
) {
    val valuesState = rememberChipTextFieldState(values.map { Chip(it) })
    LaunchedEffect(values) {
        snapshotFlow { valuesState.chips.map { it.text } }.collect { onValuesChange(it) }
    }
    ChipTextField(
        state = valuesState,
        label = { Text(label) },
        onSubmit = { text -> Chip(text) },
        readOnlyChips = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun <T> ValueList(
    values: List<T>,
    valueName: String,
    onAdd: () -> Unit,
    onDelete: (T) -> Unit,
    content: @Composable (T) -> Unit,
) {
    Column {
        values.forEachIndexed { index, value ->
            var showBook by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showBook = !showBook }.cursorForHand()

                ) {
                    Icon(if (showBook) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    Text("$valueName ${index + 1}")
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onDelete(value) }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
                AnimatedVisibility(
                    visible = showBook,
                ) {
                    content(value)
                }
            }

        }
        FilledTonalButton(
            onClick = onAdd,
            modifier = Modifier.cursorForHand()
        ) { Text("添加 $valueName") }
    }
}

@Composable
private fun AlternativeTitlesEdit(state: AlternativeTitleContext) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TextField(
            value = state.label,
            onValueChange = state::label::set,
            label = { Text("标签 \$series.metadata.alternativeTitles[i].label") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.title,
            onValueChange = state::title::set,
            label = { Text("标题 \$series.metadata.alternativeTitles[i].title") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AuthorsEdit(state: AuthorContext) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TextField(
            value = state.name,
            onValueChange = state::name::set,
            label = { Text("名称 \$series.metadata.authors[i].name") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.role,
            onValueChange = state::role::set,
            label = { Text("角色 \$series.metadata.authors[i].role") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun WebLinksEdit(state: WebLinkContext) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        TextField(
            value = state.label,
            onValueChange = state::label::set,
            label = { Text("标签 \$series.metadata.links[i].label") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = state.url,
            onValueChange = state::url::set,
            label = { Text("URL \$series.metadata.links[i].url") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
