package io.github.vivitoto.vanga.ui.common.itemlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyGridState
import sh.calvin.reorderable.rememberReorderableLazyGridState
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.common.cards.DraggableImageCard
import io.github.vivitoto.vanga.ui.common.cards.SeriesImageCard
import io.github.vivitoto.vanga.ui.common.components.EmptyState
import io.github.vivitoto.vanga.ui.common.components.Pagination
import io.github.vivitoto.vanga.ui.common.menus.SeriesMenuActions
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.platform.VerticalScrollbarWithFullSpans
import snd.komga.client.series.KomgaSeries

@Composable
fun SeriesLazyCardGrid(
    series: List<KomgaSeries>,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions?,
    topStartContent: (@Composable (KomgaSeries) -> Unit)? = null,

    selectedSeries: List<KomgaSeries> = emptyList(),
    onSeriesSelect: ((KomgaSeries) -> Unit)? = null,

    reorderable: Boolean = false,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    onReorderDragStateChange: (dragging: Boolean) -> Unit = {},

    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    minSize: Dp = 200.dp,
    gridState: LazyGridState = rememberLazyGridState(),

    modifier: Modifier = Modifier,

    beforeContent: (@Composable () -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val beforeContentItemOffset = if (beforeContent != null) 1 else 0
    val reorderableLazyGridState = rememberReorderableLazyGridState(
        lazyGridState = gridState,
        onMove = { from, to -> onReorder(from.index - beforeContentItemOffset, to.index - beforeContentItemOffset) }
    )
    LaunchedEffect(reorderableLazyGridState.isAnyItemDragging) {
        onReorderDragStateChange(reorderableLazyGridState.isAnyItemDragging)
    }


    Box(modifier) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize),
            horizontalArrangement = Arrangement.spacedBy(CardGridItemSpacing),
            verticalArrangement = Arrangement.spacedBy(CardGridItemSpacing),
            contentPadding = PaddingValues(bottom = CardGridBottomPadding),
            modifier = Modifier.padding(horizontal = CardGridHorizontalPadding)
        ) {
            if (beforeContent != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    beforeContent()
                }
            }

            if (series.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(
                        title = "暂无作品",
                        body = "当前书库或筛选条件下没有可显示的作品。"
                    )
                }
            }

            items(items = series, key = { it.id.value }) { series ->
                val isSelected = remember(selectedSeries) { selectedSeries.any { it.id == series.id } }
                val seriesTopStartContent: (@Composable () -> Unit)? =
                    topStartContent?.let { content -> { content(series) } }
                DraggableImageCard(
                    key = series.id.value,
                    dragEnabled = reorderable,
                    reorderableState = reorderableLazyGridState
                ) {
                    SeriesImageCard(
                        series = series,
                        onSeriesClick = { onSeriesClick(series) },
                        seriesMenuActions = seriesMenuActions,
                        isSelected = isSelected,
                        onSeriesSelect = onSeriesSelect?.let { { onSeriesSelect(series) } },
                        topStartContent = seriesTopStartContent,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp)
                    )
                }
            }

            if (totalPages > 1) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Pagination(
                        totalPages = totalPages,
                        currentPage = currentPage,
                        onPageChange = {
                            coroutineScope.launch {
                                onPageChange(it)
                                gridState.scrollToItem(0)
                            }
                        }
                    )
                }
            }
        }

        val emptyStateItemOffset = if (series.isEmpty()) 1 else 0
        val fullSpanItems = beforeContentItemOffset + emptyStateItemOffset + if (totalPages > 1) 1 else 0
        VerticalScrollbarWithFullSpans(gridState, Modifier.align(Alignment.TopEnd), fullSpanItems)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyGridItemScope.DraggableSeriesCard(
    series: KomgaSeries,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions?,
    isSelected: Boolean = false,
    onSeriesSelect: ((KomgaSeries) -> Unit)?,
    reorderableState: ReorderableLazyGridState
) {
    val platform = LocalPlatform.current
    ReorderableItem(reorderableState, key = series.id.value) {
        if (platform == PlatformType.MOBILE) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SeriesImageCard(
                    series = series,
                    onSeriesClick = { onSeriesClick(series) },
                    seriesMenuActions = seriesMenuActions,
                    isSelected = isSelected,
                    onSeriesSelect = onSeriesSelect?.let { { onSeriesSelect(series) } },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                )

                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .draggableHandle()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.DragHandle, null) }
            }
        } else {
            SeriesImageCard(
                series = series,
                onSeriesClick = { onSeriesClick(series) },
                seriesMenuActions = seriesMenuActions,
                isSelected = isSelected,
                onSeriesSelect = onSeriesSelect?.let { { onSeriesSelect(series) } },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .draggableHandle()
            )
        }
    }
}
