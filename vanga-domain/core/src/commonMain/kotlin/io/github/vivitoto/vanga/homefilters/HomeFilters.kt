package io.github.vivitoto.vanga.homefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.search.allOfBooks
import kotlin.time.Duration.Companion.days

val homeScreenDefaultFilters = listOf(
    BooksHomeScreenFilter.CustomFilter(
        order = 1,
        label = "继续阅读",
        filter = allOfBooks { readStatus { isEqualTo(KomgaReadStatus.IN_PROGRESS) } }.toBookCondition(),
        pageRequest = KomgaPageRequest(sort = KomgaSort.KomgaBooksSort.byReadDateDesc())
    ),
    BooksHomeScreenFilter.OnDeck(
        order = 2,
        label = "接下来读",
        pageSize = 20,
    ),
    BooksHomeScreenFilter.CustomFilter(
        order = 3,
        label = "最近发布",
        filter = allOfBooks { releaseDate { isInLast(30.days) } }.toBookCondition(),
        pageRequest = KomgaPageRequest(
            sort = KomgaSort.KomgaBooksSort.byReleaseDateDesc(),
        )
    ),
    BooksHomeScreenFilter.CustomFilter(
        order = 4,
        label = "新入库书籍",
        filter = allOfBooks {}.toBookCondition(),
        pageRequest = KomgaPageRequest(
            sort = KomgaSort.KomgaBooksSort.byCreatedDateDesc(),
            size = 20
        )
    ),
    SeriesHomeScreenFilter.RecentlyAdded(
        order = 5,
        label = "新入库系列",
        pageSize = 20,
    ),
    SeriesHomeScreenFilter.RecentlyUpdated(
        order = 6,
        label = "最近更新系列",
        pageSize = 20,
    ),
    BooksHomeScreenFilter.CustomFilter(
        order = 7,
        label = "最近读过",
        filter = allOfBooks {
            readStatus { isEqualTo(KomgaReadStatus.READ) }
        }.toBookCondition(),
        pageRequest = KomgaPageRequest(sort = KomgaSort.KomgaBooksSort.byReadDateDesc())
    ),
).sortedBy { it.order }

@Serializable
sealed interface HomeScreenFilter {
    val order: Int
    val label: String

    fun withOrder(newOrder: Int): HomeScreenFilter
}

@Serializable
sealed interface SeriesHomeScreenFilter : HomeScreenFilter {
    override fun withOrder(newOrder: Int): SeriesHomeScreenFilter

    @Serializable
    @SerialName("io.github.vivitoto.vanga.ui.home.SeriesHomeScreenFilter.RecentlyAdded")
    data class RecentlyAdded(
        override val order: Int,
        override val label: String,
        val pageSize: Int,
    ) : SeriesHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }

    @Serializable
    @SerialName("io.github.vivitoto.vanga.ui.home.SeriesHomeScreenFilter.RecentlyUpdated")
    data class RecentlyUpdated(
        override val order: Int,
        override val label: String,
        val pageSize: Int,
    ) : SeriesHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }

    @Serializable
    @SerialName("io.github.vivitoto.vanga.ui.home.SeriesHomeScreenFilter.CustomFilter")
    data class CustomFilter(
        override val order: Int,
        override val label: String,
        val filter: KomgaSearchCondition.SeriesCondition? = null,
        val textSearch: String? = null,
        val pageRequest: KomgaPageRequest? = null,
    ) : SeriesHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }
}

@Serializable
sealed interface BooksHomeScreenFilter : HomeScreenFilter {
    override fun withOrder(newOrder: Int): BooksHomeScreenFilter

    @Serializable
    @SerialName("io.github.vivitoto.vanga.ui.home.BooksHomeScreenFilter.OnDeck")
    data class OnDeck(
        override val order: Int,
        override val label: String,
        val pageSize: Int,
    ) : BooksHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }

    @Serializable
    @SerialName("io.github.vivitoto.vanga.ui.home.BooksHomeScreenFilter.CustomFilter")
    data class CustomFilter(
        override val order: Int,
        override val label: String,
        val filter: KomgaSearchCondition.BookCondition? = null,
        val textSearch: String? = null,
        val pageRequest: KomgaPageRequest? = null,
    ) : BooksHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }
}
