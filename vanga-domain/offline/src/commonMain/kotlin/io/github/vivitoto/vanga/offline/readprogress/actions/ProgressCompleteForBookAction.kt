package io.github.vivitoto.vanga.offline.readprogress.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.media.repository.OfflineMediaRepository
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgress
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgressRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class ProgressCompleteForBookAction(
    private val mediaRepository: OfflineMediaRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun run(
        bookId: KomgaBookId,
        userId: KomgaUserId,
    ) {
        transactionTemplate.execute {
            val media = mediaRepository.get(bookId)
            val progress = OfflineReadProgress(
                bookId,
                userId,
                media.pageCount,
                true
            )
            readProgressRepository.save(progress)
        }
        komgaEvents.emit(KomgaEvent.ReadProgressChanged(bookId, userId))
    }
}