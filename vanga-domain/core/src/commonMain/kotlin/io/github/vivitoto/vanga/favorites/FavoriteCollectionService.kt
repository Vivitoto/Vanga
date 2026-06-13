package io.github.vivitoto.vanga.favorites

import io.github.vivitoto.vanga.komga.api.KomgaCollectionsApi
import snd.komga.client.collection.KomgaCollection
import snd.komga.client.collection.KomgaCollectionCreateRequest
import snd.komga.client.collection.KomgaCollectionUpdateRequest
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.PatchValue
import snd.komga.client.series.KomgaSeriesId

/**
 * Server-synced Series favorites backed by Komga Collections.
 *
 * Komga Collections are not native favorites. Vanga treats a named Collection
 * as the current user's Series favorites container, while still reading legacy
 * unsuffixed containers created before the user label was available.
 */
class FavoriteCollectionService(
    private val collectionsApi: KomgaCollectionsApi,
    private val ownerLabelProvider: () -> String?,
) {
    private var cachedCollections: List<KomgaCollection>? = null

    suspend fun getFavoriteCollection(forceRefresh: Boolean = false): KomgaCollection? =
        getFavoriteCollections(forceRefresh).firstOrNull()

    suspend fun getFavoriteCollections(forceRefresh: Boolean = false): List<KomgaCollection> =
        findFavoritesCollections(forceRefresh)

    suspend fun getFavoriteSeriesIds(): Set<KomgaSeriesId> =
        getFavoriteCollections().flatMap { it.seriesIds }.toSet()

    suspend fun isFavorite(seriesId: KomgaSeriesId): Boolean =
        seriesId in getFavoriteSeriesIds()

    suspend fun addFavorite(seriesId: KomgaSeriesId): KomgaCollection =
        mutateWithRetry(operation = "add series favorite") {
            addFavoriteOnce(seriesId)
        }

    suspend fun removeFavorite(seriesId: KomgaSeriesId): KomgaCollection? =
        mutateWithRetry(operation = "remove series favorite") {
            val targets = findFavoritesCollections(forceRefresh = true)
                .filter { seriesId in it.seriesIds }
            if (targets.isEmpty()) return@mutateWithRetry null

            var firstUpdated: KomgaCollection? = null
            targets.forEach { current ->
                val nextIds = current.seriesIds.filterNot { it == seriesId }
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
                if (firstUpdated == null) firstUpdated = updated
            }
            refreshCache()
            firstUpdated
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

    private suspend fun addFavoriteOnce(seriesId: KomgaSeriesId): KomgaCollection {
        val currentCollections = findFavoritesCollections(forceRefresh = true)
        currentCollections.firstOrNull { seriesId in it.seriesIds }?.let { return it }

        val preferredName = favoritesName()
        val preferred = currentCollections.firstOrNull { it.name == preferredName }
        if (preferred != null) return addToExisting(preferred, seriesId)

        val created = collectionsApi.addOne(
            KomgaCollectionCreateRequest(
                name = preferredName,
                ordered = false,
                seriesIds = listOf(seriesId)
            )
        )
        refreshCache()
        return created
    }

    private suspend fun addToExisting(current: KomgaCollection, seriesId: KomgaSeriesId): KomgaCollection {
        val nextIds = (current.seriesIds + seriesId).distinct()
        if (nextIds == current.seriesIds) return current

        collectionsApi.updateOne(
            current.id,
            KomgaCollectionUpdateRequest(seriesIds = PatchValue.Some(nextIds))
        )
        val updated = collectionsApi.getOne(current.id)
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
            cachedCollections = null
            runCatching { block() }
                .getOrElse { second -> throw FavoriteSyncError.SyncFailed(operation, second) }
        }
    }

    private suspend fun findFavoritesCollections(forceRefresh: Boolean = false): List<KomgaCollection> {
        cachedCollections?.takeIf { !forceRefresh }?.let { return it }

        val names = FavoriteContainerNames.seriesFavoriteCandidates(ownerLabelProvider())
        val preferredName = names.first()
        return collectionsApi
            .getAll(search = FavoriteContainerNames.SERIES_FAVORITES_PREFIX, pageRequest = KomgaPageRequest(unpaged = true))
            .content
            .filter { it.name in names }
            .sortedWith(
                compareByDescending<KomgaCollection> { it.name == preferredName }
                    .thenByDescending { it.seriesIds.isNotEmpty() }
            )
            .also { cachedCollections = it }
    }

    private suspend fun refreshCache() {
        cachedCollections = null
        findFavoritesCollections(forceRefresh = true)
    }

    private fun favoritesName(): String = FavoriteContainerNames.seriesFavorites(ownerLabelProvider())
}
