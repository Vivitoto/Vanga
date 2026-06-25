package io.github.vivitoto.vanga.ui.common.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.filter
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.LocalBookDownloadEvents
import io.github.vivitoto.vanga.ui.LocalLibraries
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.common.BookReadButton
import io.github.vivitoto.vanga.ui.common.components.NoPaddingChip
import io.github.vivitoto.vanga.ui.common.images.BookThumbnail
import io.github.vivitoto.vanga.ui.common.menus.BookActionsMenu
import io.github.vivitoto.vanga.ui.common.menus.BookMenuActions
import io.github.vivitoto.vanga.ui.common.readIsSupported
import io.github.vivitoto.vanga.ui.platform.PlatformType.MOBILE
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.MEDIUM
import io.github.vivitoto.vanga.ui.platform.cursorForHand

private val GridOverlayControlSize = 40.dp

@Composable
fun BookImageCard(
    book: VangaBook,
    bookMenuActions: BookMenuActions? = null,
    onBookClick: (() -> Unit)? = null,
    onBookReadClick: ((markProgress: Boolean) -> Unit)? = null,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null,
    showSelectionControl: Boolean = false,
    showSeriesTitle: Boolean = false,
    titleMaxLines: Int = 3,
    topStartContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val libraries = LocalLibraries.current
    val libraryIsDeleted = remember {
        libraries.value.firstOrNull { it.id == book.libraryId }?.unavailable ?: false
    }
    ItemCard(
        modifier = modifier,
        onClick = onBookClick,
        onLongClick = onSelect,
        image = {
            BookHoverOverlay(
                book = book,
                libraryIsDeleted = libraryIsDeleted,
                bookMenuActions = bookMenuActions,
                onBookReadClick = onBookReadClick,
                onSelect = onSelect,
                isSelected = isSelected,
                showSelectionControl = showSelectionControl,
            ) {
                BookImageOverlay(
                    book = book,
                    libraryIsDeleted = libraryIsDeleted,
                    showSeriesTitle = showSeriesTitle,
                    titleMaxLines = titleMaxLines,
                    topStartContent = topStartContent,
                    titleBottomPadding = if (onBookReadClick != null) GridOverlayControlSize else 0.dp,
                ) {
                    BookThumbnail(
                        book.id,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    )
}

@Composable
fun BookSimpleImageCard(
    book: VangaBook,
    onBookClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ItemCard(
        modifier = modifier,
        onClick = onBookClick,
        image = {
            BookImageOverlay(
                book = book,
                libraryIsDeleted = false,
                showTitle = false
            ) {
                BookThumbnail(
                    book.id,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    )
}

@Composable
private fun BookImageOverlay(
    book: VangaBook,
    libraryIsDeleted: Boolean,
    showTitle: Boolean = true,
    showSeriesTitle: Boolean = false,
    titleMaxLines: Int = 3,
    topStartContent: (@Composable () -> Unit)? = null,
    titleBottomPadding: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart,
    ) {
        content()
        if (showTitle)
            CardGradientOverlay()
        if (topStartContent != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(2.dp),
                contentAlignment = Alignment.TopStart
            ) { topStartContent() }
        }
        Column(Modifier.fillMaxSize()) {
            Row {
                if (book.downloaded) {
                    val tint =
                        if (book.isLocalFileOutdated || book.remoteFileUnavailable) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondary
                    Icon(
                        imageVector = Icons.Filled.OfflinePin,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier
                            .padding(1.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = .88f))
                    )
                }

                Spacer(Modifier.weight(1f))
                if (book.readProgress == null) BookUnreadBadge()
            }

            Spacer(modifier = Modifier.weight(1f))
            Column(Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp + titleBottomPadding)) {
                if (showSeriesTitle && !book.oneshot) {
                    CardOutlinedText(
                        text = book.seriesTitle,
                        modifier = Modifier.fillMaxWidth(),
                        textModifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium.copy(color = Color(195, 195, 195)),
                    )
                }
                if (showTitle) {
                    CardOutlinedText(
                        text = book.metadata.title,
                        modifier = Modifier.fillMaxWidth(),
                        textModifier = Modifier.fillMaxWidth(),
                        maxLines = titleMaxLines
                    )
                }
                if (book.deleted || libraryIsDeleted) {
                    CardOutlinedText(
                        text = "不可用",
                        textColor = MaterialTheme.colorScheme.error
                    )
                }
            }

            val readProgress = book.readProgress
            if (readProgress != null && !readProgress.completed) {
                LinearProgressIndicator(
                    progress = { getReadProgressPercentage(book) },
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surface.copy(alpha = .72f),
                    modifier = Modifier.height(5.dp).fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = .72f)),
                    drawStopIndicator = {}
                )
            }
        }
        BookDownloadCardOverlay(book)

    }
}

@Composable
private fun BookDownloadCardOverlay(book: VangaBook) {
    val downloadEvents = LocalBookDownloadEvents.current
    var downloadEvent: DownloadEvent? by remember { mutableStateOf(null) }
    LaunchedEffect(downloadEvents, book) {
        downloadEvents?.filter { it.bookId == book.id }?.collect { downloadEvent = it }
    }

    when (val event = downloadEvent) {
        is DownloadEvent.BookDownloadCompleted -> {}
        is DownloadEvent.BookDownloadError -> {}
        is DownloadEvent.BookDownloadProgress -> {

            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = .86f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.tertiary)
                CircularProgressIndicator(
                    progress = { event.completed / event.total.toFloat() },
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f),
                )
            }
        }

        null -> {}
    }

}

@Composable
private fun BookUnreadBadge() {
    Box(
        modifier = Modifier
            .padding(6.dp)
            .background(
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .96f),
                VangaShape
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "未读",
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BookHoverOverlay(
    book: VangaBook,
    libraryIsDeleted: Boolean,
    bookMenuActions: BookMenuActions?,
    onBookReadClick: ((Boolean) -> Unit)?,
    isSelected: Boolean,
    onSelect: (() -> Unit)?,
    showSelectionControl: Boolean,
    content: @Composable () -> Unit
) {
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    var isReadButtonExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    val isMobile = LocalPlatform.current == MOBILE
    val selectionControlVisible = derivedStateOf {
        onSelect != null && (showSelectionControl || isSelected || isHovered.value)
    }
    val showOverlay = derivedStateOf { isHovered.value || isActionsMenuExpanded || isReadButtonExpanded || isSelected }
    val showControls = derivedStateOf { isMobile || showOverlay.value || selectionControlVisible.value }

    val border =
        if (showOverlay.value) overlayBorderModifier() else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(interactionSource)
            .then(border),
        contentAlignment = Alignment.Center
    ) {
        content()
        if (showControls.value) {
            if (isSelected) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = .38f))
                )
            }
            if (selectionControlVisible.value) {
                Box(
                    Modifier.fillMaxSize().padding(4.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    onSelect?.let { SelectionRadioButton(isSelected, it) }
                }
            }
            // Top-right: more menu
            if (bookMenuActions != null) {
                Box(
                    Modifier.fillMaxSize().padding(4.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    BookMenuActionsDropdown(
                        book = book,
                        bookMenuActions = bookMenuActions,
                        isActionsMenuExpanded = isActionsMenuExpanded,
                        onActionsMenuExpand = { isActionsMenuExpanded = it },
                        modifier = Modifier.size(GridOverlayControlSize),
                        compact = true,
                    )
                }
            }
            // Bottom-right: read button
            if (onBookReadClick != null && !book.deleted && readIsSupported(book) && !libraryIsDeleted) {
                Box(
                    Modifier.fillMaxSize().padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 10.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    BookReadButton(
                        compact = true,
                        onRead = { onBookReadClick(true) },
                        onIncognitoRead = { onBookReadClick(false) },
                        onDropdownOpenChange = { isReadButtonExpanded = it }
                    )
                }
            }
        }
    }
}

private fun getReadProgressPercentage(book: VangaBook): Float {
    val progress = book.readProgress ?: return 0f
    if (progress.completed) return 100f

    return progress.page / book.media.pagesCount.toFloat()
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookDetailedListCard(
    book: VangaBook,
    onClick: (() -> Unit)? = null,
    bookMenuActions: BookMenuActions? = null,
    onBookReadClick: ((Boolean) -> Unit)? = null,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()
    val coverWidth = 104.dp
    val coverHeight = coverWidth / coverAspectRatio
    Card(
        shape = VangaShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .cursorForHand()
            .combinedClickable(onClick = onClick ?: {}, onLongClick = onSelect)
            .hoverable(interactionSource)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .heightIn(max = 220.dp)
                .fillMaxWidth()
                .then(
                    if (isSelected) Modifier.background(
                        MaterialTheme.colorScheme.secondary.copy(
                            alpha = .3f
                        )
                    )
                    else Modifier
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                BookSimpleImageCard(
                    book = book,
                    modifier = Modifier.width(coverWidth)
                )
                if (onSelect != null && (isSelected || isHovered.value)) {
                    SelectionRadioButton(
                        isSelected,
                        onSelect
                    )
                }
            }
            BookDetailedListDetails(
                book = book,
                bookMenuActions = bookMenuActions,
                onBookReadClick = onBookReadClick,
                modifier = Modifier.weight(1f).padding(start = 10.dp).height(coverHeight),
            )
        }
    }

}

@Composable
private fun BookDetailedListDetails(
    book: VangaBook,
    bookMenuActions: BookMenuActions?,
    onBookReadClick: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val width = LocalWindowWidth.current
    Column(modifier) {
        Column {
            Text(
                book.metadata.title,
                fontWeight = FontWeight.Bold,
                maxLines = when (width) {
                    COMPACT, MEDIUM -> 2
                    else -> 3
                },
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )

            LazyRow(
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Text(
                        "${book.media.pagesCount} 页",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                items(book.metadata.tags) {
                    NoPaddingChip(
                        borderColor = MaterialTheme.colorScheme.surface,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                book.metadata.summary,
                maxLines = when (width) {
                    COMPACT, MEDIUM -> 1
                    else -> 2
                },
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 1500.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            BookDetailedListActions(
                book = book,
                bookMenuActions = bookMenuActions,
                onBookReadClick = onBookReadClick,
            )
        }
    }
}

@Composable
private fun BookDetailedListActions(
    book: VangaBook,
    bookMenuActions: BookMenuActions?,
    onBookReadClick: ((Boolean) -> Unit)? = null,
) {
    Row(
        modifier = Modifier.padding(start = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBookReadClick != null && !book.deleted && readIsSupported(book)) {
            BookReadButton(
                onRead = { onBookReadClick(true) },
                onIncognitoRead = { onBookReadClick(false) }
            )
        }
        if (bookMenuActions != null) {
            Box {
                var isMenuExpanded by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { isMenuExpanded = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.MoreVert, null)
                }
                BookActionsMenu(
                    book = book,
                    actions = bookMenuActions,
                    expanded = isMenuExpanded,
                    showEditOption = true,
                    showDownloadOption = true,
                    onDismissRequest = { isMenuExpanded = false },
                )
            }
        }
    }
}

@Composable
private fun BookMenuActionsDropdown(
    book: VangaBook,
    bookMenuActions: BookMenuActions,
    isActionsMenuExpanded: Boolean,
    onActionsMenuExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Box {
        if (compact) {
            Box(
                modifier = modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { onActionsMenuExpand(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            IconButton(
                onClick = { onActionsMenuExpand(true) },
                modifier = modifier,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Icon(Icons.Default.MoreVert, null)
            }
        }

        BookActionsMenu(
            book = book,
            actions = bookMenuActions,
            expanded = isActionsMenuExpanded,
            showEditOption = true,
            showDownloadOption = true,
            onDismissRequest = { onActionsMenuExpand(false) },
        )
    }
}
