package io.github.vivitoto.vanga.favorites

import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.PatchValue
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListCreateRequest
import snd.komga.client.readlist.KomgaReadListUpdateRequest

/**
 * Server-synced Book favorites backed by a Komga ReadList.
 *
 * Komga ReadLists are not native favorites. Vanga treats one named ReadList
 * as the current user's Book favorites container.
 */
class FavoriteReadListService(
    private val readListApi: KomgaReadListApi,
    private val ownerLabelProvider: () -> String?,
) {
    private var cachedReadList: KomgaReadList? = null
    private var cacheLoaded = false

    suspend fun getFavoriteReadList(forceRefresh: Boolean = false): KomgaReadList? =
        findFavoritesReadList(forceRefresh)

    suspend fun getFavoriteBookIds(): Set<KomgaBookId> =
        findFavoritesReadList()?.bookIds?.toSet() ?: emptySet()

    suspend fun isFavorite(bookId: KomgaBookId): Boolean =
        bookId in getFavoriteBookIds()

    suspend fun addFavorite(bookId: KomgaBookId): KomgaReadList =
        mutateWithRetry(operation = "add book favorite") { current ->
            val updated = if (current == null) {
                readListApi.addOne(
                    KomgaReadListCreateRequest(
                        name = favoritesName(),
                        summary = "Vanga favorite books",
                        ordered = true,
                        bookIds = listOf(bookId)
                    )
                )
            } else {
                val nextIds = (current.bookIds + bookId).distinct()
                if (nextIds == current.bookIds) current
                else {
                    readListApi.updateOne(
                        current.id,
                        KomgaReadListUpdateRequest(bookIds = PatchValue.Some(nextIds))
                    )
                    readListApi.getOne(current.id)
                }
            }
            cachedReadList = updated
            cacheLoaded = true
            updated
        }

    suspend fun removeFavorite(bookId: KomgaBookId): KomgaReadList? =
        mutateWithRetry(operation = "remove book favorite") { current ->
            if (current == null) return@mutateWithRetry null

            val nextIds = current.bookIds.filterNot { it == bookId }
            if (nextIds == current.bookIds) return@mutateWithRetry current

            val updated = if (nextIds.isEmpty()) {
                readListApi.deleteOne(current.id)
                null
            } else {
                readListApi.updateOne(
                    current.id,
                    KomgaReadListUpdateRequest(bookIds = PatchValue.Some(nextIds))
                )
                readListApi.getOne(current.id)
            }
            cachedReadList = updated
            cacheLoaded = true
            updated
        }

    suspend fun toggleFavorite(bookId: KomgaBookId): Boolean {
        val currentIds = getFavoriteBookIds()
        return if (bookId in currentIds) {
            removeFavorite(bookId)
            false
        } else {
            addFavorite(bookId)
            true
        }
    }

    private suspend fun <T> mutateWithRetry(
        operation: String,
        block: suspend (KomgaReadList?) -> T,
    ): T {
        return try {
            block(findFavoritesReadList(forceRefresh = true))
        } catch (_: Throwable) {
            runCatching { block(findFavoritesReadList(forceRefresh = true)) }
                .getOrElse { second -> throw FavoriteSyncError.SyncFailed(operation, second) }
        }
    }

    private suspend fun findFavoritesReadList(forceRefresh: Boolean = false): KomgaReadList? {
        if (cacheLoaded && !forceRefresh) return cachedReadList

        val names = FavoriteContainerNames.bookFavoriteCandidates(ownerLabelProvider())
        val preferredName = names.first()
        val matches = readListApi
            .getAll(search = FavoriteContainerNames.BOOK_FAVORITES_PREFIX, pageRequest = KomgaPageRequest(unpaged = true))
            .content
            .filter { it.name in names }
            .sortedWith(
                compareByDescending<KomgaReadList> { it.bookIds.isNotEmpty() }
                    .thenByDescending { it.name == preferredName }
            )

        return matches.firstOrNull().also {
            cachedReadList = it
            cacheLoaded = true
        }
    }

    private fun favoritesName(): String = FavoriteContainerNames.bookFavorites(ownerLabelProvider())
}
