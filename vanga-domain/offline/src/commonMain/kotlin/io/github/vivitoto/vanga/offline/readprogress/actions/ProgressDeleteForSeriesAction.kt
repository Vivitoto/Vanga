package io.github.vivitoto.vanga.offline.readprogress.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgressRepository
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class ProgressDeleteForSeriesAction(
    private val readProgressRepository: OfflineReadProgressRepository,
    private val bookRepository: OfflineBookRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {
    suspend fun run(seriesId: KomgaSeriesId, userId: KomgaUserId) {
        val progresses = transactionTemplate.execute {
            val bookIds = bookRepository.findAllIdsBySeriesId(seriesId)
            val progresses = readProgressRepository.findAllByBookIdsAndUserId(bookIds, userId)
            readProgressRepository.deleteByBookIdsAndUserId(bookIds, userId)
            progresses
        }

        progresses.forEach { komgaEvents.emit(KomgaEvent.ReadProgressDeleted(it.bookId, it.userId)) }
        komgaEvents.emit(KomgaEvent.ReadProgressSeriesDeleted(seriesId, userId))
    }
}