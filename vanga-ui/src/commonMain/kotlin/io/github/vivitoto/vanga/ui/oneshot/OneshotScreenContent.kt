package io.github.vivitoto.vanga.ui.oneshot

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.VangaButtonShape
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.book.BookInfoColumn
import io.github.vivitoto.vanga.ui.book.BookInfoRow
import io.github.vivitoto.vanga.ui.book.DownloadButton
import io.github.vivitoto.vanga.ui.collection.SeriesCollectionsContent
import io.github.vivitoto.vanga.ui.common.BookReadButton
import io.github.vivitoto.vanga.ui.common.components.ExpandableText
import io.github.vivitoto.vanga.ui.common.images.BookThumbnail
import io.github.vivitoto.vanga.ui.common.menus.BookMenuActions
import io.github.vivitoto.vanga.ui.common.menus.OneshotActionsMenu
import io.github.vivitoto.vanga.ui.common.readIsSupported
import io.github.vivitoto.vanga.ui.dialogs.oneshot.OneshotEditDialog
import io.github.vivitoto.vanga.ui.library.SeriesScreenFilter
import io.github.vivitoto.vanga.ui.platform.VerticalScrollbar
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.EXPANDED
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.FULL
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.MEDIUM
import io.github.vivitoto.vanga.ui.readlist.BookReadListsContent
import io.github.vivitoto.vanga.ui.series.view.SeriesDescriptionRow
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.series.KomgaSeries

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OneshotScreenContent(
    series: KomgaSeries,
    book: VangaBook,
    library: KomgaLibrary,
    onLibraryClick: (KomgaLibrary) -> Unit,
    onBookReadClick: (markReadProgress: Boolean) -> Unit,
    oneshotMenuActions: BookMenuActions,

    collections: Map<KomgaCollection, List<KomgaSeries>>,
    onCollectionClick: (KomgaCollection) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,

    readLists: Map<KomgaReadList, List<VangaBook>>,
    onReadListClick: (KomgaReadList) -> Unit,
    onReadlistBookClick: (VangaBook, KomgaReadList) -> Unit,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    onBookDownload: () -> Unit,
    onBookDownloadDelete: () -> Unit,
    cardWidth: Dp
) {
    val scrollState: ScrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize()) {
        OneshotToolBar(
            series = series,
            book = book,
            bookMenuActions = oneshotMenuActions,
        )

        val windowWidth = LocalWindowWidth.current
        val contentPadding = when (windowWidth) {
            COMPACT, MEDIUM -> Modifier.padding(10.dp)
            EXPANDED -> Modifier.padding(start = 20.dp, end = 20.dp)
            FULL -> Modifier.padding(start = 30.dp, end = 30.dp)
        }
        val coverMinWidth = when (windowWidth) {
            COMPACT, MEDIUM -> 0.dp
            else -> 300.dp
        }

        Box {
            Column(
                modifier = contentPadding
                    .fillMaxWidth()
                    .verticalScroll(state = scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.Start
            ) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val coverMaxWidth = maxWidth.coerceAtMost(500.dp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        BookThumbnail(
                            book.id,
                            modifier = Modifier
                                .heightIn(min = 100.dp, max = 400.dp)
                                .widthIn(
                                    min = coverMinWidth.coerceAtMost(coverMaxWidth),
                                    max = coverMaxWidth,
                                )
                                .animateContentSize()
                        )
                        OneshotMainInfo(
                            series = series,
                            book = book,
                            library = library,
                            onLibraryClick = onLibraryClick,
                            onBookReadClick = onBookReadClick,
                            onDownload = onBookDownload,
                            onDownloadDelete = onBookDownloadDelete,
                        )
                    }
                }
                BookInfoColumn(
                    publisher = series.metadata.publisher,
                    genres = series.metadata.genres,
                    authors = book.metadata.authors,
                    tags = book.metadata.tags,
                    links = book.metadata.links,
                    sizeInMiB = book.size,
                    mediaType = book.media.mediaType,
                    isbn = book.metadata.isbn,
                    fileUrl = book.url,
                    onFilterClick = onFilterClick
                )
                BookReadListsContent(
                    readLists = readLists,
                    onReadListClick = onReadListClick,
                    onBookClick = onReadlistBookClick,
                    cardWidth = cardWidth
                )
                SeriesCollectionsContent(
                    collections = collections,
                    onCollectionClick = onCollectionClick,
                    onSeriesClick = onSeriesClick,
                    cardWidth = cardWidth
                )
            }
            VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
fun OneshotToolBar(
    series: KomgaSeries,
    book: VangaBook,
    bookMenuActions: BookMenuActions,
) {
    Row(
        modifier = Modifier.padding(start = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            book.metadata.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, false)
        )
        ToolbarOneshotActions(series, book, bookMenuActions)
    }
}

@Composable
private fun ToolbarOneshotActions(
    series: KomgaSeries,
    book: VangaBook,
    bookMenuActions: BookMenuActions,
) {
    Row {
        Box {
            var expandActions by remember { mutableStateOf(false) }
            IconButton(onClick = { expandActions = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            OneshotActionsMenu(
                series = series,
                book = book,
                actions = bookMenuActions,
                expanded = expandActions,
                onDismissRequest = { expandActions = false }
            )
        }

        val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
        var showEditDialog by remember { mutableStateOf(false) }
        if (isAdmin) {
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, null)
            }
        }
        if (showEditDialog) {
            OneshotEditDialog(
                seriesId = series.id,
                series = series,
                book = book,
                onDismissRequest = { showEditDialog = false })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.OneshotMainInfo(
    series: KomgaSeries,
    book: VangaBook,
    library: KomgaLibrary,
    onLibraryClick: (KomgaLibrary) -> Unit,
    onBookReadClick: (markReadProgress: Boolean) -> Unit,
    onDownload: () -> Unit,
    onDownloadDelete: () -> Unit
) {
    val isDeleted = remember(series, library) { series.deleted || library.unavailable }
    val minWidth = when (LocalWindowWidth.current) {
        COMPACT, MEDIUM -> 0.dp
        else -> 450.dp
    }
    Column(
        modifier = Modifier.weight(1f, false).widthIn(min = minWidth, max = 1200.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SeriesDescriptionRow(
            library = library,
            onLibraryClick = onLibraryClick,
            releaseDate = null,
            status = null,
            ageRating = series.metadata.ageRating,
            language = series.metadata.language,
            readingDirection = series.metadata.readingDirection,
            deleted = isDeleted,
            alternateTitles = series.metadata.alternateTitles,
            onFilterClick = {},
            modifier = Modifier.widthIn(min = minWidth.coerceAtMost(200.dp)),
        )
        BookInfoRow(
            book = book,
            onSeriesButtonClick = null,
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (readIsSupported(book) && !isDeleted) {
                    BookReadButton(
                        onRead = { onBookReadClick(true) },
                        onIncognitoRead = { onBookReadClick(false) }
                    )
                    if (!book.downloaded || book.isLocalFileOutdated) {
                        DownloadButton(book, onDownload)
                    }
                }

                if (book.downloaded) {
                    Surface(
                        shape = VangaButtonShape,
                        color = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.error,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.height(48.dp).clickable { onDownloadDelete() },
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp).fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("删除已下载文件")
                        }
                    }
                }
            }
        }

        if (book.metadata.summary.isNotBlank()) {
            HorizontalDivider()
            ExpandableText(
                text = book.metadata.summary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
