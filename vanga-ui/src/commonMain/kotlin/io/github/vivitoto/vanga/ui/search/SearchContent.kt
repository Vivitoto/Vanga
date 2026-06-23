package io.github.vivitoto.vanga.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.rememberMeasuredBottomOverlayPadding
import io.github.vivitoto.vanga.ui.common.cards.BookDetailedListCard
import io.github.vivitoto.vanga.ui.common.cards.SeriesDetailedListCard
import io.github.vivitoto.vanga.ui.common.components.EmptyState
import io.github.vivitoto.vanga.ui.common.components.Pagination
import io.github.vivitoto.vanga.ui.common.menus.bulk.BottomPopupBulkActionsPanel
import io.github.vivitoto.vanga.ui.common.menus.bulk.BulkActionsContainer
import io.github.vivitoto.vanga.ui.common.menus.bulk.MixedBulkActionsContent
import io.github.vivitoto.vanga.ui.common.menus.bulk.SelectedItem
import io.github.vivitoto.vanga.ui.common.menus.bulk.containsSelectedItem
import io.github.vivitoto.vanga.ui.platform.VerticalScrollbar
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass
import io.github.vivitoto.vanga.ui.search.SearchViewModel.SearchResultsTab
import snd.komga.client.series.KomgaSeries

@Composable
fun SearchContent(
    query: String,
    searchType: SearchResultsTab,
    onSearchTypeChange: (SearchResultsTab) -> Unit,
    selectionMode: Boolean,
    selectedItems: List<SelectedItem>,
    onSelectionModeChange: (Boolean) -> Unit,
    onSelectedItemSelect: (SelectedItem) -> Unit,

    bookResults: List<VangaBook>,
    bookCurrentPage: Int,
    bookTotalPages: Int,
    onBookPageChange: (Int) -> Unit,
    onBookClick: (VangaBook) -> Unit,

    seriesResults: List<KomgaSeries>,
    seriesCurrentPage: Int,
    seriesTotalPages: Int,
    onSeriesPageChange: (Int) -> Unit,
    onSeriesClick: (KomgaSeries) -> Unit,
) {
    if (query.isNotBlank() && bookResults.isEmpty() && seriesResults.isEmpty()) {
        EmptySearchResults()
        return
    }

    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        val widthModifier = when (LocalWindowWidth.current) {
            WindowSizeClass.COMPACT, WindowSizeClass.MEDIUM -> Modifier.fillMaxWidth()
            WindowSizeClass.EXPANDED -> Modifier.fillMaxWidth(.8f)
            WindowSizeClass.FULL -> Modifier
                .widthIn(max = 1200.dp)
                .fillMaxWidth()
        }
        val scrollState = rememberLazyListState()
        val width = LocalWindowWidth.current
        val bottomOverlayVisible =
            (width == WindowSizeClass.COMPACT || width == WindowSizeClass.MEDIUM) && selectedItems.isNotEmpty()
        val bottomOverlayPadding = rememberMeasuredBottomOverlayPadding(
            visible = bottomOverlayVisible,
            basePadding = 0.dp,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selectionMode) {
                SearchBulkActionsToolbar(
                    visibleItems = visibleSelectedItems(searchType, seriesResults, bookResults),
                    selectedItems = selectedItems,
                    onCancel = { onSelectionModeChange(false) },
                    onSelectedItemSelect = onSelectedItemSelect,
                    modifier = widthModifier,
                )
            }
            SearchToolBar(
                searchType = searchType,
                onSearchTypeChange = onSearchTypeChange,
                hasSeries = seriesResults.isNotEmpty(),
                hasBooks = bookResults.isNotEmpty(),
                modifier = widthModifier
            )

            LazyColumn(
                state = scrollState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = bottomOverlayPadding.bottomPadding),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (searchType) {
                    SearchResultsTab.SERIES -> {
                        items(seriesResults) { series ->
                            val selectedItem = SelectedItem.Series(series)
                            SeriesDetailedListCard(
                                series = series,
                                onClick = {
                                    if (selectionMode) onSelectedItemSelect(selectedItem)
                                    else onSeriesClick(series)
                                },
                                isSelected = selectedItems.containsSelectedItem(selectedItem),
                                onSelect = { onSelectedItemSelect(selectedItem) },
                                modifier = widthModifier
                            )
                        }
                        if (seriesTotalPages > 1) {
                            item {
                                Pagination(
                                    totalPages = seriesTotalPages,
                                    currentPage = seriesCurrentPage,
                                    onPageChange = onSeriesPageChange
                                )
                            }
                        }
                    }

                    SearchResultsTab.BOOKS -> {
                        items(bookResults) { book ->
                            val selectedItem = SelectedItem.Book(book)
                            BookDetailedListCard(
                                book = book,
                                onClick = {
                                    if (selectionMode) onSelectedItemSelect(selectedItem)
                                    else onBookClick(book)
                                },
                                isSelected = selectedItems.containsSelectedItem(selectedItem),
                                onSelect = { onSelectedItemSelect(selectedItem) },
                                modifier = widthModifier
                            )
                        }
                        if (bookTotalPages > 1) {
                            item {
                                Pagination(
                                    totalPages = bookTotalPages,
                                    currentPage = bookCurrentPage,
                                    onPageChange = onBookPageChange
                                )
                            }
                        }

                    }
                }
            }
        }

        if (bottomOverlayVisible) {
            BottomPopupBulkActionsPanel(
                onCancel = { onSelectionModeChange(false) },
                onSizeChanged = bottomOverlayPadding.onOverlaySizeChanged,
            ) {
                MixedBulkActionsContent(selectedItems, true)
            }
        }

        VerticalScrollbar(scrollState, Modifier.align(Alignment.TopEnd))
    }
}

@Composable
private fun SearchBulkActionsToolbar(
    visibleItems: List<SelectedItem>,
    selectedItems: List<SelectedItem>,
    onCancel: () -> Unit,
    onSelectedItemSelect: (SelectedItem) -> Unit,
    modifier: Modifier = Modifier,
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
        },
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
private fun EmptySearchResults() {
    EmptyState(
        title = "没有找到结果",
        body = "换个关键词试试",
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
fun SearchToolBar(
    searchType: SearchResultsTab,
    onSearchTypeChange: (SearchResultsTab) -> Unit,
    hasSeries: Boolean,
    hasBooks: Boolean,
    modifier: Modifier
) {
    if (!hasSeries && !hasBooks) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Spacer(Modifier.width(20.dp))


        val chipColors = FilterChipDefaults.filterChipColors(

            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
        if (hasSeries) {
            FilterChip(
                onClick = { onSearchTypeChange(SearchResultsTab.SERIES) },
                selected = searchType == SearchResultsTab.SERIES,
                label = { Text("漫画系列") },
                colors = chipColors,
                border = null,
            )
        }
        if (hasBooks) {
            FilterChip(
                onClick = { onSearchTypeChange(SearchResultsTab.BOOKS) },
                selected = searchType == SearchResultsTab.BOOKS,
                label = { Text("单本漫画") },
                colors = chipColors,
                border = null,
            )
        }
    }
}

private fun visibleSelectedItems(
    searchType: SearchResultsTab,
    seriesResults: List<KomgaSeries>,
    bookResults: List<VangaBook>,
): List<SelectedItem> {
    return when (searchType) {
        SearchResultsTab.SERIES -> seriesResults.map { SelectedItem.Series(it) }
        SearchResultsTab.BOOKS -> bookResults.map { SelectedItem.Book(it) }
    }
}
