package io.github.vivitoto.vanga.ui.series

internal data class SeriesBookListFilterState(
    val query: String = "",
    val readStatus: SeriesBookReadStatusFilter = SeriesBookReadStatusFilter.All,
    val downloadStatus: SeriesBookDownloadStatusFilter = SeriesBookDownloadStatusFilter.All,
    val favoritesOnly: Boolean = false,
)

internal enum class SeriesBookReadStatusFilter {
    All,
    Unread,
    InProgress,
    Read,
}

internal enum class SeriesBookDownloadStatusFilter {
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

internal fun filterSeriesBookList(
    books: List<SeriesBookListFilterItem>,
    filters: SeriesBookListFilterState,
): List<SeriesBookListFilterItem> = books.filter { it.matchesSeriesBookListFilters(filters) }

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
