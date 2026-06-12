package io.github.vivitoto.vanga.offline.series.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.actions.BookDeleteManyAction
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookMetadataAggregationRepository
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesMetadataRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent

class SeriesDeleteAction(
    private val seriesRepository: OfflineSeriesRepository,
    private val seriesMetadataRepository: OfflineSeriesMetadataRepository,
    private val seriesThumbnailSeriesRepository: OfflineThumbnailSeriesRepository,
    private val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    private val bookRepository: OfflineBookRepository,
    private val bookDeleteManyAction: BookDeleteManyAction,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
    private val isOffline: StateFlow<Boolean>,
) : OfflineAction {

    suspend fun execute(seriesId: KomgaSeriesId) {
        val series = transactionTemplate.execute {
            val series = seriesRepository.get(seriesId)
            val books = bookRepository.findAll(series.id)
            bookDeleteManyAction.execute(books)

            seriesThumbnailSeriesRepository.deleteBySeriesId(series.id)
            seriesMetadataRepository.delete(series.id)
            bookMetadataAggregationRepository.delete(seriesId)

            seriesRepository.delete(series.id)

            series
        }

        if (isOffline.value) {
            komgaEvents.emit(KomgaEvent.SeriesDeleted(series.id, series.libraryId))
        } else {
            komgaEvents.emit(KomgaEvent.SeriesChanged(series.id, series.libraryId))
        }

    }
}