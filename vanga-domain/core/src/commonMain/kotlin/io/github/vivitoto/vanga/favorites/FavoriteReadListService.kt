package io.github.vivitoto.vanga.favorites

import snd.komga.client.book.KomgaBookId

/** Local Book favorites. */
class FavoriteReadListService(
    private val localFavoritesRepository: LocalFavoritesRepository,
    private val ownerLabelProvider: () -> String?,
    private val serverUrlProvider: () -> String?,
) {
    suspend fun getFavoriteBookIds(): Set<KomgaBookId> =
        localFavoritesRepository.getBookIds(localScope()).toSet()

    suspend fun isFavorite(bookId: KomgaBookId): Boolean =
        bookId in getFavoriteBookIds()

    suspend fun addFavorite(bookId: KomgaBookId) {
        localFavoritesRepository.addBook(localScope(), bookId)
    }

    suspend fun removeFavorite(bookId: KomgaBookId) {
        localFavoritesRepository.removeBook(localScope(), bookId)
    }

    suspend fun toggleFavorite(bookId: KomgaBookId): Boolean {
        val localIds = getFavoriteBookIds()
        return if (bookId in localIds) {
            removeFavorite(bookId)
            false
        } else {
            addFavorite(bookId)
            true
        }
    }

    private fun localScope() = localFavoritesScope(
        serverUrl = serverUrlProvider(),
        ownerLabel = ownerLabelProvider(),
    )
}
