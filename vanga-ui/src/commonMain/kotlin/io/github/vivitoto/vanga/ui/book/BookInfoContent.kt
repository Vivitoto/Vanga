package io.github.vivitoto.vanga.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import io.github.vivitoto.vanga.DefaultDateTimeFormats.localDateTimeFormat
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.VangaButtonShape
import io.github.vivitoto.vanga.ui.common.TagList
import io.github.vivitoto.vanga.ui.common.components.CompactMetadataEntry
import io.github.vivitoto.vanga.ui.common.components.CompactMetadataFlow
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!publisher.isNullOrBlank()) {
            DescriptionChips(
                label = "出版社",
                chipValue = stringEntry(publisher),
                onClick = { onFilterClick(SeriesScreenFilter(publisher = listOf(it))) },
            )
        }

        val genreEntries = remember(genres) { genres?.map { stringEntry(it) } }
        if (genreEntries != null) {
            DescriptionChips(
                label = "题材",
                chipValues = genreEntries,
                onChipClick = { onFilterClick(SeriesScreenFilter(genres = listOf(it))) },
            )
        }

        TagList(
            tags = tags,
            secondaryTags = null,
            onTagClick = { onFilterClick(SeriesScreenFilter(tags = listOf(it))) },
        )

        val uriHandler = LocalUriHandler.current
        val linkEntries = remember(links) { links.map { LabeledEntry(it, it.label) } }
        DescriptionChips(
            label = "链接",
            chipValues = linkEntries,
            onChipClick = { entry -> uriHandler.openUri(entry.url) },
            icon = Icons.Default.Link,
        )

        Spacer(Modifier.size(0.dp))
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

        Spacer(Modifier.size(0.dp))
        CompactMetadataFlow(
            entries = listOfNotNull(
                CompactMetadataEntry("大小", sizeInMiB),
                mediaType?.let { CompactMetadataEntry("格式", it) },
                isbn.ifBlank { null }?.let { CompactMetadataEntry("ISBN", it) },
                CompactMetadataEntry(
                    label = "文件",
                    value = fileUrl,
                    maxLines = 2,
                ),
            )
        )
    }
}

@Composable
private fun BookPositionBadge(
    book: VangaBook,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = VangaButtonShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "第 ${book.metadata.number} 本",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "共 ${book.media.pagesCount} 页",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowScope.BookInfoActionSlot(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.weight(1f).widthIn(min = 132.dp).height(48.dp),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookInfoRow(
    modifier: Modifier = Modifier,
    book: VangaBook,
    onSeriesButtonClick: (() -> Unit)? = null,
    actions: (@Composable FlowRowScope.() -> Unit)? = null,
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (onSeriesButtonClick != null) {
            ElevatedButton(
                onClick = onSeriesButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(Icons.AutoMirrored.Outlined.LibraryBooks, null)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = book.seriesTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (actions == null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    BookPositionBadge(book)
                }
            } else {
                BookInfoActionSlot {
                    BookPositionBadge(book, Modifier.fillMaxWidth())
                }
                actions()
            }
        }

        if (book.deleted || book.remoteFileUnavailable || book.isLocalFileOutdated) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
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
        }

        val releaseDate = book.metadata.releaseDate
        val readProgress = book.readProgress
        if (releaseDate != null || readProgress != null) {
            val pagesCount = book.media.pagesCount
            val readProgressText = remember(pagesCount, readProgress) {
                if (readProgress == null || readProgress.completed) {
                    null
                } else {
                    buildString {
                        val pagesLeft = (pagesCount - readProgress.page).coerceAtLeast(0)
                        val percentage = if (pagesCount > 0) {
                            (readProgress.page.toFloat() / pagesCount * 100).roundToInt()
                        } else {
                            0
                        }
                        append(percentage)
                        append("%, ")
                        append(pagesLeft)
                        append(" 页未读")
                    }
                }
            }
            val readDate = remember(readProgress) {
                readProgress?.readDate
                    ?.toLocalDateTime(TimeZone.currentSystemDefault())
                    ?.format(localDateTimeFormat)
            }
            CompactMetadataFlow(
                entries = listOfNotNull(
                    releaseDate?.let { CompactMetadataEntry("发布日期", it.toString()) },
                    readProgressText?.let { CompactMetadataEntry("阅读进度", it) },
                    readDate?.let { CompactMetadataEntry("上次阅读", it) },
                )
            )
        }
    }
}
