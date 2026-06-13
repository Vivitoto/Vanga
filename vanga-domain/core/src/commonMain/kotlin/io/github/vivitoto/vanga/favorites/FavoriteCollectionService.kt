package io.github.vivitoto.vanga.favorites

import io.github.vivitoto.vanga.komga.api.KomgaCollectionsApi
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionCreateRequest
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.PatchValue
import snd.komga.client.series.KomgaSeriesId

/**
 * Server-synced Series favorites backed by a Komga Collection.
 *
 * Komga Collections are not native favorites. Vanga treats one named
 * Collection as the current user's Series favorites container.
 */
class FavoriteCollectionService(
    private val collectionsApi: KomgaCollectionsApi,
    private val ownerLabelProvider: () -> String?,
) {
    private var cachedCollection: KomgaCollection? = null
    private var cacheLoaded = false

    suspend fun getFavoriteCollection(forceRefresh: Boolean = false): KomgaCollection? =
        findFavoritesCollection(forceRefresh)

    suspend fun getFavoriteSeriesIds(): Set<KomgaSeriesId> =
        findFavoritesCollection()?.seriesIds?.toSet() ?: emptySet()

    suspend fun isFavorite(seriesId: KomgaSeriesId): Boolean =
        seriesId in getFavoriteSeriesIds()

    suspend fun addFavorite(seriesId: KomgaSeriesId): KomgaCollection =
        mutateWithRetry(operation = "add series favorite") { current ->
            val updated = if (current == null) {
                collectionsApi.addOne(
                    KomgaCollectionCreateRequest(
                        name = favoritesName(),
                        ordered = false,
                        seriesIds = listOf(seriesId)
                    )
                )
            } else {
                val nextIds = (current.seriesIds + seriesId).distinct()
                if (nextIds == current.seriesIds) current
                else {
                    collectionsApi.updateOne(
                        current.id,
                        KomgaCollectionUpdateRequest(seriesIds = PatchValue.Some(nextIds))
                    )
                    collectionsApi.getOne(current.id)
                }
            }
            cachedCollection = updated
            cacheLoaded = true
            updated
        }

    suspend fun removeFavorite(seriesId: KomgaSeriesId): KomgaCollection? =
        mutateWithRetry(operation = "remove series favorite") { current ->
            if (current == null) return@mutateWithRetry null

            val nextIds = current.seriesIds.filterNot { it == seriesId }
            if (nextIds == current.seriesIds) return@mutateWithRetry current

            val updated = if (nextIds.isEmpty()) {
                collectionsApi.deleteOne(current.id)
                null
            } else {
                collectionsApi.updateOne(
                    current.id,
                    KomgaCollectionUpdateRequest(seriesIds = PatchValue.Some(nextIds))
                )
                collectionsApi.getOne(current.id)
            }
            cachedCollection = updated
            cacheLoaded = true
            updated
        }

    suspend fun toggleFavorite(seriesId: KomgaSeriesId): Boolean {
        val currentIds = getFavoriteSeriesIds()
        return if (seriesId in currentIds) {
            removeFavorite(seriesId)
            false
        } else {
            addFavorite(seriesId)
            true
        }
    }

    private suspend fun <T> mutateWithRetry(
        operation: String,
        block: suspend (KomgaCollection?) -> T,
    ): T {
        return try {
            block(findFavoritesCollection(forceRefresh = true))
        } catch (_: Throwable) {
            runCatching { block(findFavoritesCollection(forceRefresh = true)) }
                .getOrElse { second -> throw FavoriteSyncError.SyncFailed(operation, second) }
        }
    }

    private suspend fun findFavoritesCollection(forceRefresh: Boolean = false): KomgaCollection? {
        if (cacheLoaded && !forceRefresh) return cachedCollection

        val names = FavoriteContainerNames.seriesFavoriteCandidates(ownerLabelProvider())
        val preferredName = names.first()
        val matches = collectionsApi
            .getAll(search = FavoriteContainerNames.SERIES_FAVORITES_PREFIX, pageRequest = KomgaPageRequest(unpaged = true))
            .content
            .filter { it.name in names }
            .sortedWith(
                compareByDescending<KomgaCollection> { it.seriesIds.isNotEmpty() }
                    .thenByDescending { it.name == preferredName }
            )

        return matches.firstOrNull().also {
            cachedCollection = it
            cacheLoaded = true
        }
    }

    private fun favoritesName(): String = FavoriteContainerNames.seriesFavorites(ownerLabelProvider())
}
