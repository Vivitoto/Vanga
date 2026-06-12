package io.github.vivitoto.vanga.offline.readprogress.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import io.github.vivitoto.vanga.offline.media.repository.OfflineMediaRepository
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgress
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgressRepository
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class ProgressCompleteForSeriesAction(
    private val readProgressRepository: OfflineReadProgressRepository,
    private val bookRepository: OfflineBookRepository,
    private val mediaRepository: OfflineMediaRepository,
    private val userRepository: OfflineUserRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {
    suspend fun execute(seriesId: KomgaSeriesId, userId: KomgaUserId) {
        val progresses = transactionTemplate.execute {
            val user = userRepository.get(userId)
            val bookIds = bookRepository.findAllIdsBySeriesId(seriesId)
                .filter { bookId ->
                    val readProgress = readProgressRepository.find(bookId, user.id)
                    readProgress == null || !readProgress.completed
                }

            val progresses = mediaRepository.findAll(bookIds)
                .map {
                    OfflineReadProgress(
                        it.bookId,
                        user.id,
                        it.pages.size,
                        true
                    )
                }

            readProgressRepository.saveAll(progresses)
            progresses
        }
        progresses.forEach { komgaEvents.emit(KomgaEvent.ReadProgressChanged(it.bookId, it.userId)) }
        komgaEvents.emit(KomgaEvent.ReadProgressSeriesChanged(seriesId, userId))
    }
}