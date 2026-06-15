package io.github.vivitoto.vanga.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.common.cards.BookImageCard
import io.github.vivitoto.vanga.ui.common.cards.SeriesImageCard
import io.github.vivitoto.vanga.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.vivitoto.vanga.ui.common.menus.bulk.BulkActionsContainer
import io.github.vivitoto.vanga.ui.common.menus.bulk.MixedBulkActionsContent
import io.github.vivitoto.vanga.ui.common.menus.bulk.SelectedItem
import io.github.vivitoto.vanga.ui.common.menus.bulk.containsSelectedItem
import io.github.vivitoto.vanga.ui.common.menus.BookMenuActions
import io.github.vivitoto.vanga.ui.common.menus.SeriesMenuActions
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass
import io.github.vivitoto.vanga.ui.platform.PlatformType
import snd.komga.client.series.KomgaSeries

@Composable
fun HomeContent(
    filters: List<HomeFilterData>,
    onEditStart: () -> Unit,

    activeFilterNumber: Int,
    onFilterChange: (Int) -> Unit,

    selectionMode: Boolean,
    selectedItems: List<SelectedItem>,
    onSelectionModeChange: (Boolean) -> Unit,
    onSelectedItemSelect: (SelectedItem) -> Unit,

    cardWidth: Dp,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    bookMenuActions: BookMenuActions,
    onBookClick: (VangaBook) -> Unit,
    onBookReadClick: (VangaBook, Boolean) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val visibleItems = remember(filters, activeFilterNumber) { filters.visibleSelectedItems(activeFilterNumber) }
    Column {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text("首页", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("继续阅读、最近更新和常用筛选", style = MaterialTheme.typography.bodySmall)
        }
        if (selectionMode) {
            HomeBulkActionsToolbar(
                visibleItems = visibleItems,
                selectedItems = selectedItems,
                onCancel = { onSelectionModeChange(false) },
                onSelectedItemSelect = onSelectedItemSelect,
            )
        }
        Toolbar(
            filters = filters,
            currentFilterNumber = activeFilterNumber,
            onEditStart = onEditStart,
            onFilterChange = {
                onFilterChange(it)
                coroutineScope.launch { gridState.animateScrollToItem(0) }
            },
        )
        DisplayContent(
            filters = filters,
            activeFilterNumber = activeFilterNumber,

            gridState = gridState,
            cardWidth = cardWidth,
            onSeriesClick = onSeriesClick,
            seriesMenuActions = seriesMenuActions,
            bookMenuActions = bookMenuActions,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick,
            selectionMode = selectionMode,
            selectedItems = selectedItems,
            onSelectedItemSelect = onSelectedItemSelect,
        )

        val width = LocalWindowWidth.current
        if ((width == WindowSizeClass.COMPACT || width == WindowSizeClass.MEDIUM) && selectedItems.isNotEmpty()) {
            BottomPopupBulkActionsPanel(onCancel = { onSelectionModeChange(false) }) {
                MixedBulkActionsContent(selectedItems, true)
            }
        }
    }
}

@Composable
private fun HomeBulkActionsToolbar(
    visibleItems: List<SelectedItem>,
    selectedItems: List<SelectedItem>,
    onCancel: () -> Unit,
    onSelectedItemSelect: (SelectedItem) -> Unit,
) {
    BulkActionsContainer(
        onCancel = onCancel,
        selectedCount = selectedItems.size,
        allSelected = visibleItems.isNotEmpty() && visibleItems.all { selectedItems.containsSelectedItem(it) },
        onSelectAll = {
            if (visibleItems.all { selectedItems.containsSelectedItem(it) }) {
                visibleItems.forEach(onSelectedItemSelect)
            } else {
                visibleItems
                    .filterNot { selectedItems.containsSelectedItem(it) }
                    .forEach(onSelectedItemSelect)
            }
        }
    ) {
        when (LocalWindowWidth.current) {
            WindowSizeClass.FULL, WindowSizeClass.EXPANDED -> {
                if (selectedItems.isEmpty()) {
                    Text("点击条目以选择或取消选择")
                } else {
                    Spacer(Modifier.weight(1f))
                    MixedBulkActionsContent(selectedItems, false)
                }
            }

            WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM -> {}
        }
    }
}

@Composable
private fun Toolbar(
    filters: List<HomeFilterData>,
    currentFilterNumber: Int,
    onFilterChange: (Int) -> Unit,
    onEditStart: () -> Unit
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.primary,
        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
    )
    val nonEmptyFilters = remember(filters) {
        filters.filter {
            when (it) {
                is BookFilterData -> it.books.isNotEmpty()
                is SeriesFilterData -> it.series.isNotEmpty()
            }
        }
    }
    Box {
        val lazyRowState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        LazyRow(
            state = lazyRowState,
            modifier = Modifier.animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item {
                Spacer(Modifier.width(20.dp))
            }

            item {
                FilterChip(
                    onClick = onEditStart,
                    selected = false,
                    label = {
                        Icon(Icons.Default.Tune, contentDescription = "编辑首页筛选")
                    },
                    colors = chipColors,
                    border = null,
                )
            }

            if (filters.size > 1) {
                item {
                    FilterChip(
                        onClick = { onFilterChange(0) },
                        selected = currentFilterNumber == 0,
                        label = { Text("全部") },
                        colors = chipColors,
                        border = null,
                    )
                }
            }
            items(nonEmptyFilters) { data ->
                val display = remember(data.filter) {
                    when (data) {
                        is BookFilterData -> data.books.isNotEmpty()
                        is SeriesFilterData -> data.series.isNotEmpty()
                    }
                }
                if (display) {
                    FilterChip(
                        onClick = { onFilterChange(data.filter.order) },
                        selected = currentFilterNumber == data.filter.order || filters.size == 1,
                        label = { Text(data.filter.label) },
                        colors = chipColors,
                        border = null,
                    )
                }
            }
            item {
                Spacer(Modifier.width(40.dp))
            }
        }

        if (LocalPlatform.current != PlatformType.MOBILE) {
            Row {
                if (lazyRowState.canScrollBackward) {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                        onClick = { coroutineScope.launch { lazyRowState.animateScrollBy(-200.0f) } },
                    ) {
                        Icon(Icons.Default.ChevronLeft, null)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (lazyRowState.canScrollForward) {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface),
                        onClick = { coroutineScope.launch { lazyRowState.animateScrollBy(200.0f) } },
                    ) {
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisplayContent(
    filters: List<HomeFilterData>,
    activeFilterNumber: Int,
    gridState: LazyGridState,
    cardWidth: Dp,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    bookMenuActions: BookMenuActions,
    onBookClick: (VangaBook) -> Unit,
    onBookReadClick: (VangaBook, Boolean) -> Unit,
    selectionMode: Boolean,
    selectedItems: List<SelectedItem>,
    onSelectedItemSelect: (SelectedItem) -> Unit,
) {
    LazyVerticalGrid(
        modifier = Modifier.padding(horizontal = 20.dp),
        state = gridState,
        columns = GridCells.Adaptive(cardWidth),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(bottom = 50.dp)
    ) {
        for (data in filters) {
            if (activeFilterNumber == 0 || data.filter.order == activeFilterNumber) {
                when (data) {
                    is BookFilterData -> BookFilterEntry(
                        label = data.filter.label,
                        books = data.books,
                        bookMenuActions = bookMenuActions,
                        onBookClick = onBookClick,
                        onBookReadClick = onBookReadClick,
                        selectionMode = selectionMode,
                        selectedItems = selectedItems,
                        onSelectedItemSelect = onSelectedItemSelect,
                    )

                    is SeriesFilterData -> SeriesFilterEntries(
                        label = data.filter.label,
                        series = data.series,
                        onSeriesClick = onSeriesClick,
                        seriesMenuActions = seriesMenuActions,
                        selectionMode = selectionMode,
                        selectedItems = selectedItems,
                        onSelectedItemSelect = onSelectedItemSelect,
                    )

                }
            }
        }
    }
}

private fun LazyGridScope.BookFilterEntry(
    label: String,
    books: List<VangaBook>,
    bookMenuActions: BookMenuActions,
    onBookClick: (VangaBook) -> Unit,
    onBookReadClick: (VangaBook, Boolean) -> Unit,
    selectionMode: Boolean,
    selectedItems: List<SelectedItem>,
    onSelectedItemSelect: (SelectedItem) -> Unit,
) {
    if (books.isEmpty()) return

    item(span = { GridItemSpan(maxLineSpan) }) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            HorizontalDivider()
        }
    }
    items(books) { book ->
        val selectedItem = SelectedItem.Book(book)
        BookImageCard(
            book = book,
            onBookClick = {
                if (selectionMode) onSelectedItemSelect(selectedItem)
                else onBookClick(book)
            },
            onBookReadClick = if (selectionMode) null else { onBookReadClick(book, it) },
            bookMenuActions = if (selectionMode) null else bookMenuActions,
            isSelected = selectedItems.containsSelectedItem(selectedItem),
            onSelect = { onSelectedItemSelect(selectedItem) },
            showSelectionControl = selectionMode,
            showSeriesTitle = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun LazyGridScope.SeriesFilterEntries(
    label: String,
    series: List<KomgaSeries>,
    onSeriesClick: (KomgaSeries) -> Unit,
    seriesMenuActions: SeriesMenuActions,
    selectionMode: Boolean,
    selectedItems: List<SelectedItem>,
    onSelectedItemSelect: (SelectedItem) -> Unit,
) {
    if (series.isEmpty()) return
    item(span = { GridItemSpan(maxLineSpan) }) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            HorizontalDivider()
        }
    }

    items(series) {
        val selectedItem = SelectedItem.Series(it)
        SeriesImageCard(
            series = it,
            onSeriesClick = {
                if (selectionMode) onSelectedItemSelect(selectedItem)
                else onSeriesClick(it)
            },
            isSelected = selectedItems.containsSelectedItem(selectedItem),
            onSeriesSelect = { onSelectedItemSelect(selectedItem) },
            showSelectionControl = selectionMode,
            seriesMenuActions = if (selectionMode) null else seriesMenuActions,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun List<HomeFilterData>.visibleSelectedItems(activeFilterNumber: Int): List<SelectedItem> {
    return asSequence()
        .filter { activeFilterNumber == 0 || it.filter.order == activeFilterNumber }
        .flatMap { data ->
            when (data) {
                is BookFilterData -> data.books.asSequence().map { SelectedItem.Book(it) }
                is SeriesFilterData -> data.series.asSequence().map { SelectedItem.Series(it) }
            }
        }
        .distinctBy { it.key }
        .toList()
}
