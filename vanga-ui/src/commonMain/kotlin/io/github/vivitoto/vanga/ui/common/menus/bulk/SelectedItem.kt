package io.github.vivitoto.vanga.ui.common.menus.bulk

import io.github.vivitoto.vanga.komga.api.model.VangaBook
import snd.komga.client.series.KomgaSeries

sealed interface SelectedItem {
    val key: String

    data class Series(val series: KomgaSeries) : SelectedItem {
        override val key: String = "series:${series.id.value}"
    }

    data class Book(val book: VangaBook) : SelectedItem {
        override val key: String = "book:${book.id.value}"
    }
}

fun List<SelectedItem>.selectedSeries(): List<KomgaSeries> =
    mapNotNull { (it as? SelectedItem.Series)?.series }

fun List<SelectedItem>.selectedBooks(): List<VangaBook> =
    mapNotNull { (it as? SelectedItem.Book)?.book }

fun List<SelectedItem>.containsSelectedItem(item: SelectedItem): Boolean =
    any { it.key == item.key }

fun List<SelectedItem>.withoutSelectedItem(item: SelectedItem): List<SelectedItem> =
    filterNot { it.key == item.key }
