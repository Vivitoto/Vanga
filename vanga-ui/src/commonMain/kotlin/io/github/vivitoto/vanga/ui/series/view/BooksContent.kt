package io.github.vivitoto.vanga.ui.series.view

import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.settings.model.BooksLayout
import io.github.vivitoto.vanga.settings.model.BooksLayout.GRID
import io.github.vivitoto.vanga.settings.model.BooksLayout.LIST
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.book.BooksFilterState
import io.github.vivitoto.vanga.ui.book.BooksFilterState.BooksSort
import io.github.vivitoto.vanga.ui.common.cards.BookDetailedListCard
import io.github.vivitoto.vanga.ui.common.cards.BookImageCard
import io.github.vivitoto.vanga.ui.common.cards.defaultCardWidth
import io.github.vivitoto.vanga.ui.common.components.FilterDropdownChoice
import io.github.vivitoto.vanga.ui.common.components.FilterDropdownMultiChoice
import io.github.vivitoto.vanga.ui.common.components.EmptyState
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.components.NoPaddingTextField
import io.github.vivitoto.vanga.ui.common.components.PageSizeSelectionDropdown
import io.github.vivitoto.vanga.ui.common.components.Pagination
import io.github.vivitoto.vanga.ui.common.components.TagFiltersDropdownMenu
import io.github.vivitoto.vanga.ui.common.menus.BookMenuActions
import io.github.vivitoto.vanga.ui.common.menus.bulk.BookBulkActions
import io.github.vivitoto.vanga.ui.common.menus.bulk.BooksBulkActionsContent
import io.github.vivitoto.vanga.ui.common.menus.bulk.BulkActionsContainer
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.EXPANDED
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.FULL
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.MEDIUM
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.series.SeriesBookDownloadStatusFilter
import io.github.vivitoto.vanga.ui.series.SeriesBookListFilterState
import io.github.vivitoto.vanga.ui.series.SeriesBookReadStatusFilter
import io.github.vivitoto.vanga.ui.series.SeriesBooksState.BooksData
import io.github.vivitoto.vanga.ui.series.SeriesFilterState.TagExclusionMode
import io.github.vivitoto.vanga.ui.series.SeriesFilterState.TagInclusionMode
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.series.KomgaSeries

fun LazyGridScope.SeriesBooksContent(
    series: KomgaSeries?,
    booksLoadState: LoadState<BooksData>,
    onBookClick: (VangaBook) -> Unit,
    onBookReadClick: (VangaBook, Boolean) -> Unit,
    onBooksLayoutChange: (BooksLayout) -> Unit,
    onBooksGridDensityChange: (Dp) -> Unit,
    onBooksPageSizeChange: (Int) -> Unit,
    onPageChange: (Int) -> Unit,
    onBookListFiltersChange: (SeriesBookListFilterState) -> Unit,
    onBookSelect: (VangaBook) -> Unit,
    booksFilterState: BooksFilterState,
    bookContextMenuActions: BookMenuActions,
    scrollState: LazyGridState,
    cardWidth: Dp,
) {
    if (booksLoadState is LoadState.Success<BooksData>) {
        val booksState = booksLoadState.value
        item(span = { GridItemSpan(maxLineSpan) }) {
            BooksToolBar(
                series = series,
                booksLayout = booksState.layout,
                cardWidth = cardWidth,
                onBooksLayoutChange = onBooksLayoutChange,
                onBooksGridDensityChange = onBooksGridDensityChange,
                booksPageSize = booksState.pageSize,
                onBooksPageSizeChange = onBooksPageSizeChange,
                selectionMode = booksState.selectionMode,
                booksFilterState = booksFilterState,
                bookListFilters = booksState.listFilters,
                onBookListFiltersChange = onBookListFiltersChange,
                totalBookPages = booksState.totalPages,
                currentBookPage = booksState.currentPage,
                onPageChange = onPageChange
            )
        }
        BooksContent(
            books = booksState.books,
            onBookClick = onBookClick,
            onBookReadClick = onBookReadClick,
            bookMenuActions = bookContextMenuActions,
            selectionMode = booksState.selectionMode,
            selectedBooks = booksState.selectedBooks,
            onBookSelect = onBookSelect,
            layout = booksState.layout,
        )

        if (!booksState.selectionMode && booksState.totalPages > 1) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                val coroutineScope = rememberCoroutineScope()
                Pagination(
                    totalPages = booksState.totalPages,
                    currentPage = booksState.currentPage,
                    onPageChange = {
                        coroutineScope.launch {
                            scrollState.scrollToItem(scrollState.layoutInfo.totalItemsCount - (booksState.books.size + 2))
                            onPageChange(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

    } else {
        item(span = { GridItemSpan(maxLineSpan) }) { LoadIndicator() }
    }
}

@Composable
private fun LoadIndicator() {
    val background = MaterialTheme.colorScheme.surfaceVariant
    val animatedColor = remember { Animatable(background.copy(alpha = .0f)) }
    LaunchedEffect(Unit) {
        while (true) {
            animatedColor.animateTo(background, tween(1000, 200))
            delay(1000)
            animatedColor.animateTo(background.copy(alpha = .1f), tween(1500))
        }
    }
    Box(
        modifier = Modifier
            .padding(vertical = 30.dp)
            .height(260.dp)
            .fillMaxWidth()
            .background(animatedColor.value)
            .clip(VangaShape)
    )

}

private fun LazyGridScope.BooksContent(
    books: List<VangaBook>,
    onBookClick: (VangaBook) -> Unit,
    onBookReadClick: (VangaBook, Boolean) -> Unit,
    bookMenuActions: BookMenuActions,

    selectionMode: Boolean,
    selectedBooks: List<VangaBook>,
    onBookSelect: (VangaBook) -> Unit,
    layout: BooksLayout,
) {
    if (books.isEmpty()) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            EmptyState(
                title = "暂无书籍",
                body = "当前筛选条件下没有可显示的书籍。"
            )
        }
    } else
        when (layout) {
            GRID -> {
                BooksGrid(
                    books = books,
                    onBookClick = if (selectionMode) onBookSelect else onBookClick,
                    onBookReadClick = if (selectionMode) null else onBookReadClick,
                    bookMenuActions = if (selectionMode) null else bookMenuActions,

                    selectionMode = selectionMode,
                    selectedBooks = selectedBooks,
                    onBookSelect = onBookSelect,
                )
            }

            LIST -> BooksList(
                books = books,
                onBookClick = if (selectionMode) onBookSelect else onBookClick,
                onBookReadClick = if (selectionMode) null else onBookReadClick,
                bookMenuActions = if (selectionMode) null else bookMenuActions,
                selectedBooks = selectedBooks,
                onBookSelect = onBookSelect,
            )
        }

}

@Composable
private fun BooksToolBar(
    series: KomgaSeries?,

    booksLayout: BooksLayout,
    cardWidth: Dp,
    onBooksLayoutChange: (BooksLayout) -> Unit,
    onBooksGridDensityChange: (Dp) -> Unit,
    booksPageSize: Int,
    onBooksPageSizeChange: (Int) -> Unit,
    selectionMode: Boolean,
    booksFilterState: BooksFilterState,
    bookListFilters: SeriesBookListFilterState,
    onBookListFiltersChange: (SeriesBookListFilterState) -> Unit,

    totalBookPages: Int,
    currentBookPage: Int,
    onPageChange: (Int) -> Unit,
) {
    val width = LocalWindowWidth.current
    var showFilters by remember { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 5.dp)
        ) {
            val booksLabel = remember(series) {
                if (series == null) null
                else buildString {
                    append(series.booksCount)
                    if (series.metadata.totalBookCount != null) append(" / ${series.metadata.totalBookCount}")
                    append(" 本")
                }
            }

            if (selectionMode) {
                Spacer(Modifier.weight(1f))
            } else {
                Column(Modifier.weight(1f)) {
                    Text("单本漫画", style = MaterialTheme.typography.titleMedium)
                    booksLabel?.let {
                        Text(
                            booksLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (width == EXPANDED || width == FULL) {
                    ExpandableBookFiltersRow(filterState = booksFilterState)
                }

            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!selectionMode) {
                    if (width == COMPACT || width == MEDIUM) {
                        IconButton(onClick = { showFilters = !showFilters }, modifier = Modifier.cursorForHand()) {
                            Icon(
                                Icons.Default.FilterList,
                                null,
                                tint = if (booksFilterState.isChanged) MaterialTheme.colorScheme.tertiary else LocalContentColor.current
                            )
                        }
                    }

                    PageSizeSelectionDropdown(booksPageSize, onBooksPageSizeChange)
                }

                Box(
                    Modifier
                        .background(
                            if (booksLayout == LIST) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onBooksLayoutChange(LIST) }
                        .cursorForHand()
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = "列表视图",
                    )
                }

                Box(
                    Modifier
                        .background(
                            if (booksLayout == GRID) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.surface
                        )
                        .clickable { onBooksLayoutChange(GRID) }
                        .cursorForHand()
                        .padding(10.dp)
                ) {
                    Icon(
                        Icons.Default.GridView,
                        contentDescription = "网格视图",
                    )
                }

                BooksLayoutDensityDropdown(
                    booksLayout = booksLayout,
                    cardWidth = cardWidth,
                    onBooksLayoutChange = onBooksLayoutChange,
                    onBooksGridDensityChange = onBooksGridDensityChange,
                )
            }
        }
        if (showFilters) {
            BookFilterDialog(
                filterState = booksFilterState,
                onDismiss = { showFilters = false }
            )
        }

        if (!selectionMode) {
            SeriesBookListFiltersRow(
                filters = bookListFilters,
                onFiltersChange = onBookListFiltersChange,
            )
        }

        AnimatedVisibility(!selectionMode && totalBookPages > 1) {
            Pagination(
                totalPages = totalBookPages,
                currentPage = currentBookPage,
                onPageChange = onPageChange,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeriesBookListFiltersRow(
    filters: SeriesBookListFilterState,
    onFiltersChange: (SeriesBookListFilterState) -> Unit,
) {
    var query by remember(filters.query) { mutableStateOf(filters.query) }
    val latestFilters by rememberUpdatedState(filters)
    LaunchedEffect(query) {
        delay(200)
        val currentFilters = latestFilters
        if (query != currentFilters.query) onFiltersChange(currentFilters.copy(query = query))
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NoPaddingTextField(
            text = query,
            placeholder = "搜索书名或编号",
            onTextChange = { query = it },
            modifier = Modifier.weight(1f).height(40.dp).width(220.dp),
        )

        FilterDropdownChoice(
            selectedOption = LabeledEntry(filters.readStatus, filters.readStatus.label),
            options = SeriesBookReadStatusFilter.entries.map { LabeledEntry(it, it.label) },
            onOptionChange = { onFiltersChange(filters.copy(readStatus = it.value)) },
            label = null,
            modifier = Modifier.width(150.dp),
        )

        FilterDropdownChoice(
            selectedOption = LabeledEntry(filters.downloadStatus, filters.downloadStatus.label),
            options = SeriesBookDownloadStatusFilter.entries.map { LabeledEntry(it, it.label) },
            onOptionChange = { onFiltersChange(filters.copy(downloadStatus = it.value)) },
            label = null,
            modifier = Modifier.width(150.dp),
        )

        FilterDropdownChoice(
            selectedOption = LabeledEntry(filters.favoritesOnly, if (filters.favoritesOnly) "仅收藏" else "全部收藏状态"),
            options = listOf(
                LabeledEntry(false, "全部收藏状态"),
                LabeledEntry(true, "仅收藏"),
            ),
            onOptionChange = { onFiltersChange(filters.copy(favoritesOnly = it.value)) },
            label = null,
            modifier = Modifier.width(150.dp),
        )
    }
}

private val SeriesBookReadStatusFilter.label: String
    get() = when (this) {
        SeriesBookReadStatusFilter.All -> "全部阅读状态"
        SeriesBookReadStatusFilter.Unread -> "未读"
        SeriesBookReadStatusFilter.InProgress -> "阅读中"
        SeriesBookReadStatusFilter.Read -> "已读"
    }

private val SeriesBookDownloadStatusFilter.label: String
    get() = when (this) {
        SeriesBookDownloadStatusFilter.All -> "全部下载状态"
        SeriesBookDownloadStatusFilter.Downloaded -> "已下载"
        SeriesBookDownloadStatusFilter.NotDownloaded -> "未下载"
        SeriesBookDownloadStatusFilter.Outdated -> "本地已过期"
    }

private enum class BookGridDensity(
    val label: String,
    val cardWidthDp: Int,
) {
    COMPACT("紧凑网格", 170),
    STANDARD("标准网格", defaultCardWidth),
    COMFORTABLE("舒适网格", 300),
}

@Composable
private fun BooksLayoutDensityDropdown(
    booksLayout: BooksLayout,
    cardWidth: Dp,
    onBooksLayoutChange: (BooksLayout) -> Unit,
    onBooksGridDensityChange: (Dp) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .size(36.dp)
                .cursorForHand(),
        ) {
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "选择书籍视图",
                modifier = Modifier.size(20.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("列表") },
                onClick = {
                    expanded = false
                    onBooksLayoutChange(LIST)
                },
                leadingIcon = if (booksLayout == LIST) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null,
            )
            BookGridDensity.entries.forEach { density ->
                val selected = booksLayout == GRID && cardWidth.value.roundToInt() == density.cardWidthDp
                DropdownMenuItem(
                    text = { Text(density.label) },
                    onClick = {
                        expanded = false
                        onBooksGridDensityChange(density.cardWidthDp.dp)
                        onBooksLayoutChange(GRID)
                    },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Default.Check, contentDescription = null) }
                    } else null,
                )
            }
        }
    }
}


@Composable
fun BooksBulkActionsToolbar(
    onCancel: () -> Unit,
    books: List<VangaBook>,
    actions: BookBulkActions,
    selectedBooks: List<VangaBook>,
    onBookSelect: (VangaBook) -> Unit,
) {
    BulkActionsContainer(
        onCancel = onCancel,
        selectedCount = selectedBooks.size,
        allSelected = books.size == selectedBooks.size,
        onSelectAll = {
            if (books.size == selectedBooks.size) books.forEach { onBookSelect(it) }
            else books.filter { it !in selectedBooks }.forEach { onBookSelect(it) }
        }
    ) {
        when (LocalWindowWidth.current) {
            FULL, EXPANDED -> {
                if (selectedBooks.isEmpty()) {
                    Text("点击条目以选择或取消选择")
                } else {
                    Spacer(Modifier.weight(1f))
                    BooksBulkActionsContent(
                        books = selectedBooks,
                        actions = actions,
                        compact = false
                    )
                }
            }

            COMPACT, MEDIUM -> {}
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpandableBookFiltersRow(filterState: BooksFilterState) {
    var showFilters by remember { mutableStateOf(false) }
    val currentFilter = filterState.state.collectAsState().value
    Row(verticalAlignment = Alignment.CenterVertically) {
        BoxWithConstraints(Modifier.weight(1f, fill = false)) {
            val filterWidth = maxWidth.coerceAtMost(200.dp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                val widthModifier = Modifier.width(filterWidth)

                SortOrder(
                    sortOrder = currentFilter.sortOrder,
                    filterState = filterState,
                    modifier = widthModifier,
                    withLabel = false
                )
                ReadStatusFilter(
                    readStatus = currentFilter.readStatus,
                    filterState = filterState,
                    modifier = widthModifier,
                    withLabel = false
                )

                AnimatedVisibility(showFilters && filterState.authorsOptions.isNotEmpty()) {
                    AuthorsFilter(
                        authors = currentFilter.authors,
                        filterState = filterState,
                        modifier = widthModifier,
                        withLabel = false
                    )
                }

                AnimatedVisibility(showFilters && filterState.tagOptions.isNotEmpty()) {
                    TagsFilter(
                        includeTags = currentFilter.includeTags,
                        excludeTags = currentFilter.excludeTags,
                        inclusionMode = currentFilter.inclusionMode,
                        exclusionMode = currentFilter.exclusionMode,
                        filterState = filterState,
                        modifier = widthModifier,
                        withLabel = false
                    )
                }
            }
        }

        if (filterState.authorsOptions.isNotEmpty() || filterState.tagOptions.isNotEmpty()) {
            IconButton(onClick = { showFilters = !showFilters }, modifier = Modifier.cursorForHand()) {
                Icon(
                    imageVector = if (showFilters) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
fun BookFilterDialog(
    filterState: BooksFilterState,
    onDismiss: () -> Unit,
) {
    val currentFilter = filterState.state.collectAsState().value
    AppDialog(
        modifier = Modifier.fillMaxWidth(.8f),
        content = {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SortOrder(
                    sortOrder = currentFilter.sortOrder,
                    filterState = filterState,
                    modifier = Modifier.fillMaxWidth(),
                    withLabel = true
                )
                ReadStatusFilter(
                    readStatus = currentFilter.readStatus,
                    filterState = filterState,
                    modifier = Modifier.fillMaxWidth(),
                    withLabel = true
                )

                if (filterState.authorsOptions.isNotEmpty())
                    AuthorsFilter(
                        authors = currentFilter.authors,
                        filterState = filterState,
                        modifier = Modifier.fillMaxWidth(),
                        withLabel = true
                    )

                if (filterState.tagOptions.isNotEmpty())
                    TagsFilter(
                        includeTags = currentFilter.includeTags,
                        excludeTags = currentFilter.excludeTags,
                        inclusionMode = currentFilter.inclusionMode,
                        exclusionMode = currentFilter.exclusionMode,
                        filterState = filterState,
                        modifier = Modifier.fillMaxWidth(),
                        withLabel = true
                    )

            }
        },
        header = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("书籍筛选", modifier = Modifier.padding(start = 10.dp))
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
            }
        },
        onDismissRequest = onDismiss
    )

}

@Composable
private fun SortOrder(
    sortOrder: BooksSort,
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownChoice(
        selectedOption = LabeledEntry(sortOrder, strings.forBookSort(sortOrder)),
        options = BooksSort.entries.map { LabeledEntry(it, strings.forBookSort(it)) },
        onOptionChange = { filterState.onSortOrderChange(it.value) },
        label = if (withLabel) strings.sort else null,
        modifier = modifier
    )
}

@Composable
private fun ReadStatusFilter(
    readStatus: List<KomgaReadStatus>,
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownMultiChoice(
        selectedOptions = readStatus.map { LabeledEntry(it, strings.forReadStatus(it)) },
        options = KomgaReadStatus.entries.map { LabeledEntry(it, strings.forReadStatus(it)) },
        onOptionSelect = { changed -> filterState.onReadStatusSelect(changed.value) },
        label = if (withLabel) strings.readStatus else null,
        placeholder = if (withLabel) null else strings.readStatus,
        modifier = modifier
    )
}

@Composable
private fun AuthorsFilter(
    authors: List<KomgaAuthor>,
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    FilterDropdownMultiChoice(
        selectedOptions = authors.map { LabeledEntry(it, it.name) },
        options = filterState.authorsOptions.map { LabeledEntry(it, it.name) },
        onOptionSelect = { changed -> filterState.onAuthorSelect(changed.value) },
        label = if (withLabel) strings.authors else null,
        placeholder = if (withLabel) null else strings.authors,
        modifier = modifier
    )
}

@Composable
private fun TagsFilter(
    includeTags: List<String>,
    excludeTags: List<String>,
    inclusionMode: TagInclusionMode,
    exclusionMode: TagExclusionMode,
    filterState: BooksFilterState,
    modifier: Modifier,
    withLabel: Boolean,
) {
    val strings = LocalStrings.current.booksFilter
    TagFiltersDropdownMenu(
        allTags = filterState.tagOptions,
        includeTags = includeTags,
        excludeTags = excludeTags,
        onTagSelect = filterState::onTagSelect,
        onReset = filterState::resetTagFilters,

        inclusionMode = inclusionMode,
        onInclusionModeChange = filterState::onInclusionModeChange,
        exclusionMode = exclusionMode,
        onExclusionModeChange = filterState::onExclusionModeChange,

        label = if (withLabel) strings.tags else null,
        placeholder = if (withLabel) null else strings.tags,
        contentPadding = PaddingValues(5.dp),
        modifier = modifier.clip(VangaShape),
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant,
        inputFieldModifier = Modifier.fillMaxWidth()
    )
}

private fun LazyGridScope.BooksGrid(
    books: List<VangaBook>,
    onBookClick: ((VangaBook) -> Unit)? = null,
    onBookReadClick: ((VangaBook, Boolean) -> Unit)? = null,
    bookMenuActions: BookMenuActions? = null,

    selectionMode: Boolean = false,
    selectedBooks: List<VangaBook> = emptyList(),
    onBookSelect: ((VangaBook) -> Unit)? = null,
) {
    items(books) { book ->
        BookImageCard(
            book = book,
            onBookClick = onBookClick?.let { { onBookClick(book) } },
            onBookReadClick = onBookReadClick?.let { { onBookReadClick(book, it) } },
            bookMenuActions = bookMenuActions,
            isSelected = selectedBooks.any { it.id == book.id },
            onSelect = onBookSelect?.let { { onBookSelect(book) } },
            showSelectionControl = selectionMode,
            modifier = Modifier.padding(5.dp),
        )
    }
}

fun LazyGridScope.BooksList(
    books: List<VangaBook>,
    bookMenuActions: BookMenuActions? = null,
    onBookClick: ((VangaBook) -> Unit)? = null,
    onBookReadClick: ((VangaBook, Boolean) -> Unit)? = null,

    selectedBooks: List<VangaBook> = emptyList(),
    onBookSelect: ((VangaBook) -> Unit)? = null,
) {
    books.forEach { book ->
        item(span = { GridItemSpan(maxLineSpan) }) {
            BookDetailedListCard(
                book = book,
                onClick = onBookClick?.let { { onBookClick(book) } },
                onBookReadClick = onBookReadClick?.let { { onBookReadClick(book, it) } },
                bookMenuActions = bookMenuActions,
                isSelected = selectedBooks.any { it.id == book.id },
                onSelect = onBookSelect?.let { { onBookSelect(book) } },
                modifier = Modifier.padding(vertical = 5.dp),
            )
        }
    }
}
