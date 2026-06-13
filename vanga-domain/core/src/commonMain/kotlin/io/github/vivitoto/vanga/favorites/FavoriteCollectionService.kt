package io.github.vivitoto.vanga.favorites

import snd.komga.client.series.KomgaSeriesId

/** Local Series favorites. */
class FavoriteCollectionService(
    private val localFavoritesRepository: LocalFavoritesRepository,
    private val ownerLabelProvider: () -> String?,
    private val serverUrlProvider: () -> String?,
) {
    suspend fun getFavoriteSeriesIds(): Set<KomgaSeriesId> =
        localFavoritesRepository.getSeriesIds(localScope()).toSet()

    suspend fun isFavorite(seriesId: KomgaSeriesId): Boolean =
        seriesId in getFavoriteSeriesIds()

    suspend fun addFavorite(seriesId: KomgaSeriesId) {
        localFavoritesRepository.addSeries(localScope(), seriesId)
    }

    suspend fun removeFavorite(seriesId: KomgaSeriesId) {
        localFavoritesRepository.removeSeries(localScope(), seriesId)
    }

    suspend fun toggleFavorite(seriesId: KomgaSeriesId): Boolean {
        val localIds = getFavoriteSeriesIds()
        return if (seriesId in localIds) {
            removeFavorite(seriesId)
            false
        } else {
            addFavorite(seriesId)
            true
        }
    }

    private fun localScope() = localFavoritesScope(
        serverUrl = serverUrlProvider(),
        ownerLabel = ownerLabelProvider(),
    )
}
