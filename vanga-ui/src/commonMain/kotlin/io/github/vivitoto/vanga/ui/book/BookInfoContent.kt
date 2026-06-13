package io.github.vivitoto.vanga.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import io.github.vivitoto.vanga.DefaultDateTimeFormats.localDateTimeFormat
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.common.TagList
import io.github.vivitoto.vanga.ui.common.components.DescriptionChips
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry.Companion.stringEntry
import io.github.vivitoto.vanga.ui.library.SeriesScreenFilter
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaWebLink
import snd.komga.client.common.coloristRole
import snd.komga.client.common.coverRole
import snd.komga.client.common.editorRole
import snd.komga.client.common.inkerRole
import snd.komga.client.common.lettererRole
import snd.komga.client.common.pencillerRole
import snd.komga.client.common.translatorRole
import snd.komga.client.common.writerRole
import kotlin.math.roundToInt

private val authorsOrder = listOf(
    writerRole,
    pencillerRole,
    inkerRole,
    coloristRole,
    lettererRole,
    coverRole,
    editorRole,
    translatorRole
)

@Composable
fun BookInfoColumn(
    publisher: String?,
    genres: List<String>?,
    authors: List<KomgaAuthor>,
    tags: List<String>,
    links: List<KomgaWebLink>,
    sizeInMiB: String,
    mediaType: String?,
    isbn: String,
    fileUrl: String,
    onFilterClick: (SeriesScreenFilter) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (!publisher.isNullOrBlank()) {
            DescriptionChips(
                label = "出版社",
                chipValue = stringEntry(publisher),
                onClick = { onFilterClick(SeriesScreenFilter(publisher = listOf(it))) },
            )
        }

        val genreEntries = remember(genres) { genres.orEmpty().map { stringEntry(it) } }
        if (genreEntries.isNotEmpty()) {
            DescriptionChips(
                label = "题材",
                chipValues = genreEntries,
                onChipClick = { onFilterClick(SeriesScreenFilter(genres = listOf(it))) },
            )
        }

        if (tags.isNotEmpty()) {
            TagList(
                tags = tags,
                secondaryTags = null,
                onTagClick = { onFilterClick(SeriesScreenFilter(tags = listOf(it))) },
            )
        }

        val uriHandler = LocalUriHandler.current
        val linkEntries = remember(links) { links.map { LabeledEntry(it, it.label) } }
        if (linkEntries.isNotEmpty()) {
            DescriptionChips(
                label = "链接",
                chipValues = linkEntries,
                onChipClick = { entry -> uriHandler.openUri(entry.url) },
                icon = Icons.Default.Link,
            )
        }

        val authorEntries = remember(authors) {
            authors
                .groupBy { it.role }
                .map { (role, authors) ->
                    role.replaceFirstChar { it.uppercase() } to authors.map { LabeledEntry(it, it.name) }
                }
                .sortedBy { (role, _) -> authorsOrder.indexOf(role.lowercase()) }
        }
        authorEntries.forEach { (role, authors) ->
            DescriptionChips(
                label = role,
                chipValues = authors,
                onChipClick = { onFilterClick(SeriesScreenFilter(authors = listOf(it))) },
            )
        }

        Row {
            Text(
                "大小",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(120.dp)
            )
            SelectionContainer { Text(sizeInMiB, style = MaterialTheme.typography.labelLarge) }
        }

        Row {
            Text(
                "格式",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(120.dp)
            )
            if (mediaType != null) {
                SelectionContainer { Text(mediaType, style = MaterialTheme.typography.labelLarge) }
            }
        }

        isbn.ifBlank { null }?.let { isbn ->
            Row {
                Text(
                    "ISBN",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.width(120.dp)
                )
                SelectionContainer { Text(isbn, style = MaterialTheme.typography.labelLarge) }
            }
        }

        Row {
            Text(
                "文件",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(120.dp)
            )
            SelectionContainer { Text(fileUrl, style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
fun BookInfoRow(
    modifier: Modifier = Modifier,
    book: VangaBook,
    onSeriesButtonClick: (() -> Unit)? = null,
) {

    Column(
        modifier = modifier,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (onSeriesButtonClick != null) {
                ElevatedButton(
                    onClick = onSeriesButtonClick,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.LibraryBooks, null)
                    Spacer(Modifier.width(3.dp))
                    Text(text = book.seriesTitle, textDecoration = TextDecoration.Underline)
                }
            }
            if (book.deleted) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("不可用") },
                    border = null,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                )
            }
            if (book.remoteFileUnavailable) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("远程文件不可用") },
                    border = null,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                )
            }

            if (book.isLocalFileOutdated) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("本地下载已过期") },
                    border = null,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                )
            }
        }

        SelectionContainer {
            Text(text = "第 ${book.metadata.number} 本 · ${book.media.pagesCount} 页")
        }

        Spacer(Modifier.heightIn(5.dp))
        SelectionContainer {
            Column {
                book.metadata.releaseDate?.let {
                    Row {
                        Text(
                            text = "发布日期：",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(120.dp)
                        )

                        Text(
                            it.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                val readProgress = book.readProgress
                val pagesCount = book.media.pagesCount
                if (readProgress != null) {
                    if (!readProgress.completed) {
                        val readProgressText = remember(pagesCount, readProgress) {
                            buildString {
                                val pagesLeft = pagesCount - readProgress.page
                                val percentage =
                                    (readProgress.page.toFloat() / pagesCount * 100)
                                        .roundToInt()
                                append(percentage)
                                append("%, ")
                                append(pagesLeft)
                                append(" 页未读")
                            }
                        }

                        Row {
                            Text(
                                "阅读进度：",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(readProgressText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Row {
                        val readDate = remember(readProgress) {
                            readProgress.readDate
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .format(localDateTimeFormat)
                        }
                        Text(
                            "上次阅读：",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            readDate,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
