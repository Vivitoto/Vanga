package io.github.vivitoto.vanga.favorites

import io.github.vivitoto.vanga.komga.api.KomgaReadListApi
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.PatchValue
import snd.komga.client.readlist.KomgaReadList
import snd.komga.client.readlist.KomgaReadListCreateRequest
import snd.komga.client.readlist.KomgaReadListUpdateRequest

/**
 * Server-synced Book favorites backed by Komga ReadLists.
 *
 * Komga ReadLists are not native favorites. Vanga treats a named ReadList as
 * the current user's Book favorites container, while still reading legacy
 * unsuffixed containers created before the user label was available.
 */
class FavoriteReadListService(
    private val readListApi: KomgaReadListApi,
    private val ownerLabelProvider: () -> String?,
) {
    private var cachedReadLists: List<KomgaReadList>? = null

    suspend fun getFavoriteReadList(forceRefresh: Boolean = false): KomgaReadList? =
        getFavoriteReadLists(forceRefresh).firstOrNull()

    suspend fun getFavoriteReadLists(forceRefresh: Boolean = false): List<KomgaReadList> =
        findFavoritesReadLists(forceRefresh)

    suspend fun getFavoriteBookIds(): Set<KomgaBookId> =
        getFavoriteReadLists().flatMap { it.bookIds }.toSet()

    suspend fun isFavorite(bookId: KomgaBookId): Boolean =
        bookId in getFavoriteBookIds()

    suspend fun addFavorite(bookId: KomgaBookId): KomgaReadList =
        mutateWithRetry(operation = "add book favorite") {
            addFavoriteOnce(bookId)
        }

    suspend fun removeFavorite(bookId: KomgaBookId): KomgaReadList? =
        mutateWithRetry(operation = "remove book favorite") {
            val targets = findFavoritesReadLists(forceRefresh = true)
                .filter { bookId in it.bookIds }
            if (targets.isEmpty()) return@mutateWithRetry null

            var firstUpdated: KomgaReadList? = null
            targets.forEach { current ->
                val nextIds = current.bookIds.filterNot { it == bookId }
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
                if (firstUpdated == null) firstUpdated = updated
            }
            refreshCache()
            firstUpdated
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

    private suspend fun addFavoriteOnce(bookId: KomgaBookId): KomgaReadList {
        val currentLists = findFavoritesReadLists(forceRefresh = true)
        currentLists.firstOrNull { bookId in it.bookIds }?.let { return it }

        val preferredName = favoritesName()
        val preferred = currentLists.firstOrNull { it.name == preferredName }
        if (preferred != null) return addToExisting(preferred, bookId)

        val created = readListApi.addOne(
            KomgaReadListCreateRequest(
                name = preferredName,
                summary = "Vanga favorite books",
                ordered = true,
                bookIds = listOf(bookId)
            )
        )
        refreshCache()
        return created
    }

    private suspend fun addToExisting(current: KomgaReadList, bookId: KomgaBookId): KomgaReadList {
        val nextIds = (current.bookIds + bookId).distinct()
        if (nextIds == current.bookIds) return current

        readListApi.updateOne(
            current.id,
            KomgaReadListUpdateRequest(bookIds = PatchValue.Some(nextIds))
        )
        val updated = readListApi.getOne(current.id)
        refreshCache()
        return updated
    }

    private suspend fun <T> mutateWithRetry(
        operation: String,
        block: suspend () -> T,
    ): T {
        return try {
            block()
        } catch (_: Throwable) {
            cachedReadLists = null
            runCatching { block() }
                .getOrElse { second -> throw FavoriteSyncError.SyncFailed(operation, second) }
        }
    }

    private suspend fun findFavoritesReadLists(forceRefresh: Boolean = false): List<KomgaReadList> {
        cachedReadLists?.takeIf { !forceRefresh }?.let { return it }

        val names = FavoriteContainerNames.bookFavoriteCandidates(ownerLabelProvider())
        val preferredName = names.first()
        return readListApi
            .getAll(search = FavoriteContainerNames.BOOK_FAVORITES_PREFIX, pageRequest = KomgaPageRequest(unpaged = true))
            .content
            .filter { it.name in names }
            .sortedWith(
                compareByDescending<KomgaReadList> { it.name == preferredName }
                    .thenByDescending { it.bookIds.isNotEmpty() }
            )
            .also { cachedReadLists = it }
    }

    private suspend fun refreshCache() {
        cachedReadLists = null
        findFavoritesReadLists(forceRefresh = true)
    }

    private fun favoritesName(): String = FavoriteContainerNames.bookFavorites(ownerLabelProvider())
}
