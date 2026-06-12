package io.github.vivitoto.vanga.offline.series.repository

import io.github.vivitoto.vanga.offline.series.model.OfflineSeries
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId

interface OfflineSeriesRepository {
    suspend fun save(series: OfflineSeries)

    suspend fun get(id: KomgaSeriesId): OfflineSeries
    suspend fun find(id: KomgaSeriesId): OfflineSeries?
    suspend fun findAllByLibraryId(libraryId: KomgaLibraryId): List<OfflineSeries>
    suspend fun delete(id: KomgaSeriesId)
    suspend fun delete(seriesids: List<KomgaSeriesId>)
}