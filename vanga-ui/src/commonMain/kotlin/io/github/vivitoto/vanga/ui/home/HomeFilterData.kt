package io.github.vivitoto.vanga.ui.home

import io.github.vivitoto.vanga.homefilters.BooksHomeScreenFilter
import io.github.vivitoto.vanga.homefilters.HomeScreenFilter
import io.github.vivitoto.vanga.homefilters.SeriesHomeScreenFilter
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import snd.komga.client.series.KomgaSeries

sealed interface HomeFilterData {
    val filter: HomeScreenFilter
}

data class SeriesFilterData(
    val series: List<KomgaSeries>,
    override val filter: SeriesHomeScreenFilter,
) : HomeFilterData

data class BookFilterData(
    val books: List<VangaBook>,
    override val filter: BooksHomeScreenFilter,
) : HomeFilterData
