package io.github.vivitoto.vanga.offline.library.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.library.repository.OfflineLibraryRepository
import io.github.vivitoto.vanga.offline.series.actions.SeriesDeleteManyAction
import io.github.vivitoto.vanga.offline.series.repository.OfflineSeriesRepository
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.sse.KomgaEvent

class LibraryDeleteAction(
    private val libraryRepository: OfflineLibraryRepository,
    private val seriesRepository: OfflineSeriesRepository,
    private val seriesDeleteManyAction: SeriesDeleteManyAction,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>
) : OfflineAction {

    suspend fun execute(libraryId: KomgaLibraryId) {
        transactionTemplate.execute {
            val series = seriesRepository.findAllByLibraryId(libraryId)
            seriesDeleteManyAction.execute(series)
            libraryRepository.delete(libraryId)
        }

        komgaEvents.emit(KomgaEvent.LibraryDeleted(libraryId))
    }
}