package io.github.vivitoto.vanga.ui.book

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import io.github.vivitoto.vanga.ui.LocalBookDownloadEvents
import io.github.vivitoto.vanga.ui.LocalKomgaState
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.VangaButtonShape
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.common.BookReadButton
import io.github.vivitoto.vanga.ui.common.cards.coverShape
import io.github.vivitoto.vanga.ui.common.components.ExpandableText
import io.github.vivitoto.vanga.ui.common.images.BookThumbnail
import io.github.vivitoto.vanga.ui.common.menus.BookActionsMenu
import io.github.vivitoto.vanga.ui.common.menus.BookMenuActions
import io.github.vivitoto.vanga.ui.common.readIsSupported
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.dialogs.book.edit.BookEditDialog
import io.github.vivitoto.vanga.ui.dialogs.permissions.DownloadNotificationRequestDialog
import io.github.vivitoto.vanga.ui.favorites.FavoriteBookButton
import io.github.vivitoto.vanga.ui.library.SeriesScreenFilter
import io.github.vivitoto.vanga.ui.platform.VerticalScrollbar
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.EXPANDED
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.FULL
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.MEDIUM
import io.github.vivitoto.vanga.ui.readlist.BookReadListsContent
import io.github.vivitoto.vanga.ui.settings.offline.downloads.downloadErrorText
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.readlist.KomgaReadList

private val DetailSectionMaxWidth = 860.dp
private val DetailContentMaxWidth = 720.dp

@Composable
fun BookScreenContent(
    library: KomgaLibrary?,
    book: VangaBook?,
    bookMenuActions: BookMenuActions,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,
    onBookDownload: () -> Unit,
    onBookDownloadDelete: () -> Unit,

    readLists: Map<KomgaReadList, List<VangaBook>>,
    onReadListClick: (KomgaReadList) -> Unit,
    onReadListBookPress: (VangaBook, KomgaReadList) -> Unit,
    onParentSeriesPress: () -> Unit,
    onFilterClick: (SeriesScreenFilter) -> Unit,
    cardWidth: Dp
) {

    val scrollState: ScrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize()) {
        if (book == null || library == null) return
        BookToolBar(
            book = book,
            bookMenuActions = bookMenuActions,
        )

        val contentPadding = when (LocalWindowWidth.current) {
            COMPACT, MEDIUM -> Modifier.padding(10.dp)
            EXPANDED -> Modifier.padding(start = 20.dp, end = 20.dp)
            FULL -> Modifier.padding(start = 30.dp, end = 30.dp)
        }

        val coverMinWidth = when (LocalWindowWidth.current) {
            COMPACT, MEDIUM -> 0.dp
            else -> 300.dp
        }

        Box {
            Column(
                modifier = contentPadding
                    .fillMaxWidth()
                    .verticalScroll(state = scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BookHero(book = book, coverMinWidth = coverMinWidth)

                DetailSection {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        BookInfoRow(
                            book = book,
                            onSeriesButtonClick = onParentSeriesPress,
                            modifier = Modifier.widthIn(max = DetailContentMaxWidth).fillMaxWidth(),
                        ) {
                            BookActionRow(
                                book = book,
                                library = library,
                                onBookReadPress = onBookReadPress,
                                onDownload = onBookDownload,
                                onDownloadDelete = onBookDownloadDelete
                            )
                        }
                    }

                    ExpandableText(
                        text = book.metadata.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                DetailSection {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        BookInfoColumn(
                            publisher = null,
                            genres = null,
                            authors = book.metadata.authors,
                            tags = book.metadata.tags,
                            links = book.metadata.links,
                            sizeInMiB = book.size,
                            mediaType = book.media.mediaType,
                            isbn = book.metadata.isbn,
                            fileUrl = book.url,
                            onFilterClick = onFilterClick,
                            modifier = Modifier.widthIn(max = DetailContentMaxWidth).fillMaxWidth(),
                        )
                    }
                }
                BookReadListsContent(
                    readLists = readLists,
                    onReadListClick = onReadListClick,
                    onBookClick = onReadListBookPress,
                    cardWidth = cardWidth
                )
            }
            VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
fun BookToolBar(
    book: VangaBook,
    bookMenuActions: BookMenuActions,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FavoriteBookButton(book.id)
        ToolbarBookActions(book, bookMenuActions)
    }
}

@Composable
private fun ToolbarBookActions(
    book: VangaBook,
    bookMenuActions: BookMenuActions,
) {
    Row {
        Box {
            var expandActions by remember { mutableStateOf(false) }
            IconButton(onClick = { expandActions = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = null)
            }
            BookActionsMenu(
                book = book,
                actions = bookMenuActions,
                expanded = expandActions,
                showEditOption = false,
                showDownloadOption = false,
                onDismissRequest = { expandActions = false }
            )
        }

        val isAdmin = LocalKomgaState.current.authenticatedUser.collectAsState().value?.roleAdmin() ?: true
        val isOffline = LocalOfflineMode.current.collectAsState().value
        var showEditDialog by remember { mutableStateOf(false) }

        if (isAdmin && !isOffline) {
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, null)
            }
        }
        if (showEditDialog) {
            BookEditDialog(book = book, onDismissRequest = { showEditDialog = false })
        }
    }
}

@Composable
private fun BookHero(
    book: VangaBook,
    coverMinWidth: Dp,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val coverMaxWidth = maxWidth.coerceAtMost(320.dp)
            Surface(
                modifier = Modifier
                    .heightIn(min = 180.dp, max = 380.dp)
                    .widthIn(
                        min = coverMinWidth.coerceAtMost(coverMaxWidth),
                        max = coverMaxWidth,
                    )
                    .animateContentSize(),
                shape = coverShape,
                tonalElevation = 1.dp,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .20f)),
            ) {
                BookThumbnail(
                    book.id,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Text(
            text = book.metadata.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = DetailContentMaxWidth).padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun DetailSection(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.widthIn(max = DetailSectionMaxWidth).fillMaxWidth(),
        shape = VangaShape,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowScope.BookActionRow(
    book: VangaBook,
    library: KomgaLibrary,
    onBookReadPress: (markReadProgress: Boolean) -> Unit,
    onDownload: () -> Unit,
    onDownloadDelete: () -> Unit
) {
    if (!book.deleted && !library.unavailable) {
        if (readIsSupported(book)) {
            BookInfoActionSlot {
                BookReadButton(
                    modifier = Modifier.fillMaxWidth(),
                    fillContentWidth = true,
                    onRead = { onBookReadPress(true) },
                    onIncognitoRead = { onBookReadPress(false) },
                )
            }
        }
        if (!book.downloaded || book.isLocalFileOutdated) {
            BookInfoActionSlot {
                DownloadButton(book, onDownload, Modifier.fillMaxWidth())
            }
        }
    }
    if (book.downloaded) {
        BookInfoActionSlot {
            DeleteDownloadedButton(
                text = "删除已下载",
                onClick = onDownloadDelete,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun DeleteDownloadedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = VangaButtonShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.error,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer),
        modifier = modifier.height(48.dp).clickable { onClick() },
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp).fillMaxHeight().fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun DownloadButton(
    book: VangaBook,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDownloadConfirmation by remember { mutableStateOf(false) }
    val downloadEvents = LocalBookDownloadEvents.current
    var downloadEvent: DownloadEvent? by remember { mutableStateOf(null) }
    LaunchedEffect(downloadEvents, book) {
        downloadEvents?.filter { it.bookId == book.id }?.collect { downloadEvent = it }
    }

    val downloadEnabled = downloadEvent == null
    val surfaceModifier = modifier.height(48.dp).pointerHoverIcon(PointerIcon.Hand)
    Surface(
        shape = VangaButtonShape,
        color = if (downloadEnabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (downloadEnabled) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = if (downloadEnabled) surfaceModifier.clickable { showDownloadConfirmation = true } else surfaceModifier,
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp).fillMaxHeight().fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (val event = downloadEvent) {
                is DownloadEvent.BookDownloadProgress -> {
                    Box(
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            progress = { event.completed / event.total.toFloat() },
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                        )
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("下载中…")
                }

                is DownloadEvent.BookDownloadError -> {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text(downloadErrorText(event.error), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                else -> {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text("下载")
                }
            }
        }
    }

    if (showDownloadConfirmation) {
        var permissionRequested by remember { mutableStateOf(false) }
        DownloadNotificationRequestDialog { permissionRequested = true }

        if (permissionRequested) {
            ConfirmationDialog(
                body = "下载单本《${book.name}》？",
                onDialogConfirm = onDownload,
                onDialogDismiss = { showDownloadConfirmation = false }
            )
        }
    }

}
