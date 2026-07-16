package io.github.vivitoto.vanga.ui.series

import kotlin.test.Test
import kotlin.test.assertEquals

class SeriesBookListFiltersTest {

    private val books = listOf(
        SeriesBookListFilterItem(
            title = "Alpha 01",
            number = 1,
            readStatus = SeriesBookReadStatusFilter.Unread,
            downloaded = false,
            favorite = true,
        ),
        SeriesBookListFilterItem(
            title = "Beta 02",
            number = 2,
            readStatus = SeriesBookReadStatusFilter.InProgress,
            downloaded = true,
        ),
        SeriesBookListFilterItem(
            title = "Gamma 03",
            number = 3,
            readStatus = SeriesBookReadStatusFilter.Read,
            downloaded = true,
            localFileOutdated = true,
        ),
    )

    @Test
    fun queryMatchesTitleCaseInsensitively() {
        val result = filterSeriesBookList(books, SeriesBookListFilterState(query = "alpha"))

        assertEquals(listOf("Alpha 01"), result.map { it.title })
    }

    @Test
    fun queryMatchesExactBookNumber() {
        val result = filterSeriesBookList(books, SeriesBookListFilterState(query = "2"))

        assertEquals(listOf("Beta 02"), result.map { it.title })
    }

    @Test
    fun filtersByReadStatus() {
        val result = filterSeriesBookList(
            books,
            SeriesBookListFilterState(readStatus = SeriesBookReadStatusFilter.InProgress),
        )

        assertEquals(listOf("Beta 02"), result.map { it.title })
    }

    @Test
    fun filtersByDownloadStatus() {
        val downloaded = filterSeriesBookList(
            books,
            SeriesBookListFilterState(downloadStatus = SeriesBookDownloadStatusFilter.Downloaded),
        )
        val outdated = filterSeriesBookList(
            books,
            SeriesBookListFilterState(downloadStatus = SeriesBookDownloadStatusFilter.Outdated),
        )
        val notDownloaded = filterSeriesBookList(
            books,
            SeriesBookListFilterState(downloadStatus = SeriesBookDownloadStatusFilter.NotDownloaded),
        )

        assertEquals(listOf("Beta 02"), downloaded.map { it.title })
        assertEquals(listOf("Gamma 03"), outdated.map { it.title })
        assertEquals(listOf("Alpha 01"), notDownloaded.map { it.title })
    }

    @Test
    fun filtersFavoritesOnly() {
        val result = filterSeriesBookList(books, SeriesBookListFilterState(favoritesOnly = true))

        assertEquals(listOf("Alpha 01"), result.map { it.title })
    }
}
