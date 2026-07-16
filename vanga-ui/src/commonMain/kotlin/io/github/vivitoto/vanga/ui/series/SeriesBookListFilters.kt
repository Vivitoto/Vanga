package io.github.vivitoto.vanga.ui.series

import io.github.vivitoto.vanga.komga.api.model.VangaBook
import snd.komga.client.book.KomgaBookId

data class SeriesBookListFilterState(
    val query: String = "",
    val readStatus: SeriesBookReadStatusFilter = SeriesBookReadStatusFilter.All,
    val downloadStatus: SeriesBookDownloadStatusFilter = SeriesBookDownloadStatusFilter.All,
    val favoritesOnly: Boolean = false,
)

enum class SeriesBookReadStatusFilter {
    All,
    Unread,
    InProgress,
    Read,
}

enum class SeriesBookDownloadStatusFilter {
    All,
    Downloaded,
    NotDownloaded,
    Outdated,
}

internal data class SeriesBookListFilterItem(
    val title: String,
    val number: Int? = null,
    val readStatus: SeriesBookReadStatusFilter = SeriesBookReadStatusFilter.Unread,
    val downloaded: Boolean = false,
    val localFileOutdated: Boolean = false,
    val favorite: Boolean = false,
)

internal val SeriesBookListFilterState.isActive: Boolean
    get() = query.isNotBlank() ||
            readStatus != SeriesBookReadStatusFilter.All ||
            downloadStatus != SeriesBookDownloadStatusFilter.All ||
            favoritesOnly

internal fun filterSeriesBookList(
    books: List<SeriesBookListFilterItem>,
    filters: SeriesBookListFilterState,
): List<SeriesBookListFilterItem> = books.filter { it.matchesSeriesBookListFilters(filters) }

internal fun filterSeriesBookList(
    books: List<VangaBook>,
    filters: SeriesBookListFilterState,
    favoriteBookIds: Set<KomgaBookId> = emptySet(),
): List<VangaBook> = books.filter { book ->
    book.toSeriesBookListFilterItem(favoriteBookIds).matchesSeriesBookListFilters(filters)
}

internal fun <T> paginateSeriesBookList(
    books: List<T>,
    page: Int,
    pageSize: Int,
): List<T> {
    if (pageSize <= 0) return books
    val start = ((page.coerceAtLeast(1) - 1) * pageSize).coerceAtMost(books.size)
    val end = (start + pageSize).coerceAtMost(books.size)
    return books.subList(start, end)
}

internal fun seriesBookListPageCount(itemCount: Int, pageSize: Int): Int {
    if (pageSize <= 0) return 1
    return ((itemCount + pageSize - 1) / pageSize).coerceAtLeast(1)
}

internal fun SeriesBookListFilterItem.matchesSeriesBookListFilters(filters: SeriesBookListFilterState): Boolean =
    matchesSeriesBookQuery(filters.query) &&
            matchesReadStatusFilter(filters.readStatus) &&
            matchesDownloadStatusFilter(filters.downloadStatus) &&
            (!filters.favoritesOnly || favorite)

private fun SeriesBookListFilterItem.matchesSeriesBookQuery(query: String): Boolean {
    val normalized = query.trim()
    if (normalized.isEmpty()) return true

    return title.contains(normalized, ignoreCase = true) || number?.toString() == normalized
}

private fun SeriesBookListFilterItem.matchesReadStatusFilter(filter: SeriesBookReadStatusFilter): Boolean = when (filter) {
    SeriesBookReadStatusFilter.All -> true
    SeriesBookReadStatusFilter.Unread -> readStatus == SeriesBookReadStatusFilter.Unread
    SeriesBookReadStatusFilter.InProgress -> readStatus == SeriesBookReadStatusFilter.InProgress
    SeriesBookReadStatusFilter.Read -> readStatus == SeriesBookReadStatusFilter.Read
}

private fun SeriesBookListFilterItem.matchesDownloadStatusFilter(filter: SeriesBookDownloadStatusFilter): Boolean = when (filter) {
    SeriesBookDownloadStatusFilter.All -> true
    SeriesBookDownloadStatusFilter.Downloaded -> downloaded && !localFileOutdated
    SeriesBookDownloadStatusFilter.NotDownloaded -> !downloaded
    SeriesBookDownloadStatusFilter.Outdated -> downloaded && localFileOutdated
}

private fun VangaBook.toSeriesBookListFilterItem(favoriteBookIds: Set<KomgaBookId>): SeriesBookListFilterItem {
    val progress = readProgress
    return SeriesBookListFilterItem(
        title = metadata.title.ifBlank { name },
        number = number,
        readStatus = when {
            progress == null -> SeriesBookReadStatusFilter.Unread
            progress.completed -> SeriesBookReadStatusFilter.Read
            else -> SeriesBookReadStatusFilter.InProgress
        },
        downloaded = downloaded,
        localFileOutdated = isLocalFileOutdated,
        favorite = id in favoriteBookIds,
    )
}
