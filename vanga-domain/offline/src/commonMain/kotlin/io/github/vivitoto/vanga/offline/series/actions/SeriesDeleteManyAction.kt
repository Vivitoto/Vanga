package io.github.vivitoto.vanga.offline.series.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.actions.BookDeleteManyAction
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookMetadataAggregationRepository
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgressRepository
import io.github.vivitoto.vanga.offline.series.model.OfflineSeries
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesMetadataRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesRepository
import io.github.vivitoto.vanga.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komga.client.sse.KomgaEvent

class SeriesDeleteManyAction(
    private val seriesRepository: OfflineSeriesRepository,
    private val seriesMetadataRepository: OfflineSeriesMetadataRepository,
    private val seriesThumbnailSeriesRepository: OfflineThumbnailSeriesRepository,
    private val bookRepository: OfflineBookRepository,
    private val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val bookDeleteManyAction: BookDeleteManyAction,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
    private val transactionTemplate: TransactionTemplate,
) : OfflineAction {

    suspend fun execute(series: List<OfflineSeries>) {
        transactionTemplate.execute {
            val seriesIds = series.map { it.id }
            val books = bookRepository.findAllBySeriesIds(seriesIds)
            bookDeleteManyAction.execute(books)

            readProgressRepository.deleteBySeriesIds(seriesIds)
            seriesThumbnailSeriesRepository.deleteBySeriesIds(seriesIds)
            seriesMetadataRepository.delete(seriesIds)
            bookMetadataAggregationRepository.delete(seriesIds)

            seriesRepository.delete(seriesIds)
        }

        series.forEach { komgaEvents.emit(KomgaEvent.SeriesDeleted(it.id, it.libraryId)) }
    }
}