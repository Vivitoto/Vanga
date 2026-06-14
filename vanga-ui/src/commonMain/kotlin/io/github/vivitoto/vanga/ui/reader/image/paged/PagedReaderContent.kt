package io.github.vivitoto.vanga.ui.reader.image.paged

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyUp
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.settings.model.PageDisplayLayout.DOUBLE_PAGES
import io.github.vivitoto.vanga.settings.model.PageDisplayLayout.DOUBLE_PAGES_NO_COVER
import io.github.vivitoto.vanga.settings.model.PageDisplayLayout.SINGLE_PAGE
import io.github.vivitoto.vanga.settings.model.PagedReadingDirection
import io.github.vivitoto.vanga.settings.model.PagedReadingDirection.LEFT_TO_RIGHT
import io.github.vivitoto.vanga.settings.model.PagedReadingDirection.RIGHT_TO_LEFT
import io.github.vivitoto.vanga.ui.reader.image.ScreenScaleState
import io.github.vivitoto.vanga.ui.reader.image.common.PagedReaderHelpDialog
import io.github.vivitoto.vanga.ui.reader.image.common.ReaderControlsOverlay
import io.github.vivitoto.vanga.ui.reader.image.common.ReaderImageContent
import io.github.vivitoto.vanga.ui.reader.image.common.ScalableContainer
import io.github.vivitoto.vanga.ui.reader.image.paged.PagedReaderState.Page
import io.github.vivitoto.vanga.ui.reader.image.paged.PagedReaderState.TransitionPage
import io.github.vivitoto.vanga.ui.reader.image.paged.PagedReaderState.TransitionPage.BookEnd
import io.github.vivitoto.vanga.ui.reader.image.paged.PagedReaderState.TransitionPage.BookStart
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun BoxScope.PagedReaderContent(
    showHelpDialog: Boolean,
    onShowHelpDialogChange: (Boolean) -> Unit,
    showSettingsMenu: Boolean,
    onShowSettingsMenuChange: (Boolean) -> Unit,
    screenScaleState: ScreenScaleState,
    pagedReaderState: PagedReaderState,
    volumeKeysNavigation: Boolean,
    onBackGesture: () -> Unit,
) {
    if (showHelpDialog) {
        PagedReaderHelpDialog(onDismissRequest = { onShowHelpDialogChange(false) })
    }

    val readingDirection = pagedReaderState.readingDirection.collectAsState().value
    val layoutDirection = when (readingDirection) {
        LEFT_TO_RIGHT -> LayoutDirection.Ltr
        RIGHT_TO_LEFT -> LayoutDirection.Rtl
    }
    val pages = pagedReaderState.currentSpread.collectAsState().value.pages
    val currentSpreadIndex = pagedReaderState.currentSpreadIndex.collectAsState().value
    val layout = pagedReaderState.layout.collectAsState().value
    val layoutOffset = pagedReaderState.layoutOffset.collectAsState().value

    val currentContainerSize = screenScaleState.areaSize.collectAsState().value

    val coroutineScope = rememberCoroutineScope()
    var horizontalSwipeOffset by remember { mutableFloatStateOf(0f) }
    ReaderControlsOverlay(
        readingDirection = layoutDirection,
        onNexPageClick = pagedReaderState::nextPage,
        onPrevPageClick = pagedReaderState::previousPage,
        contentAreaSize = currentContainerSize,
        isSettingsMenuOpen = showSettingsMenu,
        onSettingsMenuToggle = { onShowSettingsMenuChange(!showSettingsMenu) },
        canNavigateByHorizontalSwipe = { !screenScaleState.canPanHorizontally() },
        onHorizontalSwipeProgress = { horizontalSwipeOffset = it },
        onBackGesture = onBackGesture,
        modifier = Modifier.onKeyEvent { event ->
            pagedReaderOnKeyEvents(
                event = event,
                readingDirection = readingDirection,
                layoutOffset = layoutOffset,
                onReadingDirectionChange = pagedReaderState::onReadingDirectionChange,
                onScaleTypeCycle = pagedReaderState::onScaleTypeCycle,
                onLayoutCycle = pagedReaderState::onLayoutCycle,
                onChangeLayoutOffset = pagedReaderState::onLayoutOffsetChange,
                onPageChange = pagedReaderState::onPageChange,
                onMoveToLastPage = pagedReaderState::moveToLastPage,
                onMoveToNextPage = { coroutineScope.launch { pagedReaderState.nextPage() } },
                onMoveToPrevPage = { coroutineScope.launch { pagedReaderState.previousPage() } },
                volumeKeysNavigation = volumeKeysNavigation
            )
        }
    ) {
        ScalableContainer(scaleState = screenScaleState) {
            val transitionPage = pagedReaderState.transitionPage.collectAsState().value
            SlidingPagedReaderSpread(
                currentSpreadIndex = currentSpreadIndex,
                previewSpread = pagedReaderState::previewSpread,
                pages = pages,
                transitionPage = transitionPage,
                layout = layout,
                readingDirection = readingDirection,
                horizontalSwipeOffset = horizontalSwipeOffset,
            )
        }
    }
}

@Composable
private fun SlidingPagedReaderSpread(
    currentSpreadIndex: Int,
    previewSpread: (Int) -> PagedReaderState.PageSpread?,
    pages: List<Page>,
    transitionPage: TransitionPage?,
    layout: io.github.vivitoto.vanga.settings.model.PageDisplayLayout,
    readingDirection: PagedReadingDirection,
    horizontalSwipeOffset: Float,
) {
    val previewIndex = previewSpreadIndex(currentSpreadIndex, horizontalSwipeOffset, readingDirection)
    val previewPages = previewSpread(previewIndex)?.pages

    if (transitionPage != null || previewPages == null || abs(horizontalSwipeOffset) < 1f) {
        SpreadContent(pages = pages, transitionPage = transitionPage, layout = layout, readingDirection = readingDirection)
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val dragOffset = horizontalSwipeOffset.coerceIn(-widthPx, widthPx)
        val previewSide = previewSpreadSide(currentSpreadIndex, previewIndex, readingDirection)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(dragOffset.roundToInt(), 0) }
        ) {
            SpreadContent(pages = pages, transitionPage = null, layout = layout, readingDirection = readingDirection)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset((previewSide * widthPx + dragOffset).roundToInt(), 0) }
        ) {
            SpreadContent(pages = previewPages, transitionPage = null, layout = layout, readingDirection = readingDirection)
        }
    }
}

private fun previewSpreadIndex(
    currentSpreadIndex: Int,
    horizontalSwipeOffset: Float,
    readingDirection: PagedReadingDirection,
): Int {
    val forward = when (readingDirection) {
        LEFT_TO_RIGHT -> horizontalSwipeOffset < 0f
        RIGHT_TO_LEFT -> horizontalSwipeOffset > 0f
    }
    return currentSpreadIndex + if (forward) 1 else -1
}

private fun previewSpreadSide(
    currentSpreadIndex: Int,
    previewIndex: Int,
    readingDirection: PagedReadingDirection,
): Int {
    val forward = previewIndex > currentSpreadIndex
    return when (readingDirection) {
        LEFT_TO_RIGHT -> if (forward) 1 else -1
        RIGHT_TO_LEFT -> if (forward) -1 else 1
    }
}

@Composable
private fun SpreadContent(
    pages: List<Page>,
    transitionPage: TransitionPage?,
    layout: io.github.vivitoto.vanga.settings.model.PageDisplayLayout,
    readingDirection: PagedReadingDirection,
) {
    if (transitionPage != null) {
        TransitionPage(transitionPage)
    } else {
        when (layout) {
            SINGLE_PAGE -> pages.firstOrNull()?.let { SinglePageLayout(it) }
            DOUBLE_PAGES, DOUBLE_PAGES_NO_COVER -> DoublePageLayout(pages, readingDirection)
        }
    }
}


@Composable
private fun TransitionPage(page: TransitionPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (page) {
            is BookEnd -> {
                Column {
                    Text("已读完：", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        page.currentBook.metadata.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(Modifier.size(50.dp))

                if (page.nextBook != null) {
                    Column {
                        Text("下一本：", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            page.nextBook.metadata.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                } else {
                    Text("没有下一本")
                }

            }

            is BookStart -> {
                if (page.previousBook != null) {
                    Column {
                        Text("上一本：", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            page.previousBook.metadata.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                } else {
                    Text("没有上一本")

                }
                Spacer(Modifier.size(50.dp))
                Column {
                    Text("当前：", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        page.currentBook.metadata.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

            }
        }
    }
}

@Composable
private fun SinglePageLayout(page: Page) {
    Layout(content = { ReaderImageContent(page.imageResult) }) { measurable, constraints ->
        val placeable = measurable.first().measure(constraints)
        val startPadding = (constraints.maxWidth - placeable.width) / 2
        val topPadding = ((constraints.maxHeight - placeable.height) / 2).coerceAtLeast(0)
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.placeRelative(startPadding, topPadding)
        }
    }
}

@Composable
private fun DoublePageLayout(
    pages: List<Page>,
    readingDirection: PagedReadingDirection,
) {
    Layout(content = {
        when (pages.size) {
            0 -> {}
            1 -> ReaderImageContent(pages.first().imageResult)
            2 -> {
                ReaderImageContent(pages[0].imageResult)
                ReaderImageContent(pages[1].imageResult)
            }

            else -> error("无法同时显示超过 2 张图片")
        }
    }) { measurables, constraints ->
        val measured = measurables
            .map { it.measure(constraints.copy(maxWidth = constraints.maxWidth / measurables.size)) }
            .let {
                when (readingDirection) {
                    LEFT_TO_RIGHT -> it
                    RIGHT_TO_LEFT -> it.reversed()
                }
            }
        val startPadding: Int
        if (measured.size == 1 && !pages.first().metadata.isLandscape()) {
            startPadding = when (readingDirection) {
                LEFT_TO_RIGHT -> (constraints.maxWidth - (measured.first().width * 2)) / 2
                RIGHT_TO_LEFT -> ((constraints.maxWidth - (measured.first().width * 2)) / 2) + measured.first().width
            }
        } else {
            val totalWidth = measured.fold(0) { acc, placeable -> acc + placeable.width }
            startPadding = (constraints.maxWidth - totalWidth) / 2
        }

        var widthTaken = startPadding
        layout(constraints.maxWidth, constraints.maxHeight) {
            measured.forEach {
                val topPadding = ((constraints.maxHeight - it.height) / 2).coerceAtLeast(0)
                it.placeRelative(widthTaken, topPadding)
                widthTaken += it.width
            }
        }
    }
}

private fun pagedReaderOnKeyEvents(
    event: KeyEvent,
    readingDirection: PagedReadingDirection,
    layoutOffset: Boolean,
    onReadingDirectionChange: (PagedReadingDirection) -> Unit,
    onScaleTypeCycle: () -> Unit,
    onLayoutCycle: () -> Unit,
    onChangeLayoutOffset: (Boolean) -> Unit,
    onPageChange: (Int) -> Unit,
    onMoveToLastPage: () -> Unit,
    onMoveToNextPage: () -> Unit,
    onMoveToPrevPage: () -> Unit,
    volumeKeysNavigation: Boolean,
): Boolean {
    if (event.type != KeyUp) {
        return volumeKeysNavigation && (event.key == Key.VolumeUp || event.key == Key.VolumeDown)
    }

    val previousPage = {
        if (readingDirection == LEFT_TO_RIGHT) onMoveToPrevPage()
        else onMoveToNextPage()
    }
    val nextPage = {
        if (readingDirection == LEFT_TO_RIGHT) onMoveToNextPage()
        else onMoveToPrevPage()
    }

    var consumed = true
    when (event.key) {
        Key.DirectionLeft -> {
            previousPage()
            if (event.isAltPressed) consumed = false
        }

        Key.DirectionRight -> nextPage()
        Key.MoveHome -> onPageChange(0)
        Key.MoveEnd -> onMoveToLastPage()
        Key.L -> onReadingDirectionChange(LEFT_TO_RIGHT)
        Key.R -> onReadingDirectionChange(RIGHT_TO_LEFT)
        Key.C -> if (event.isAltPressed) consumed = false else onScaleTypeCycle()
        Key.D -> onLayoutCycle()
        Key.O -> onChangeLayoutOffset(!layoutOffset)
        Key.VolumeUp -> if (volumeKeysNavigation) previousPage() else consumed = false
        Key.VolumeDown -> if (volumeKeysNavigation) nextPage() else consumed = false
        else -> consumed = false
    }
    return consumed
}
