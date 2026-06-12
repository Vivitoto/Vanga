package io.github.vivitoto.vanga.offline.readprogress.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.media.model.MediaExtensionEpub
import io.github.vivitoto.vanga.offline.media.repository.OfflineMediaRepository
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgress
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgressRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.MediaProfile
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class ProgressMarkAction(
    private val mediaRepository: OfflineMediaRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun run(
        bookId: KomgaBookId,
        userId: KomgaUserId,
        page: Int
    ) {
        transactionTemplate.execute {
            val media = mediaRepository.get(bookId)
            require(page in 1..media.pageCount) { "Page argument ($page) must be within 1 and book page count (${media.pageCount})" }

            val locator =
                if (media.mediaProfile == MediaProfile.EPUB) {
                    require(media.epubDivinaCompatible) { "epub book is not Divina compatible" }

                    val extension = media.extension
                    check(extension is MediaExtensionEpub)
                    extension.positions[page - 1]
                } else {
                    null
                }

            val progress = OfflineReadProgress(
                bookId = bookId,
                userId = userId,
                page = page,
                completed = page == media.pageCount,
                locator = locator
            )
            readProgressRepository.save(progress)
        }

        komgaEvents.emit(KomgaEvent.ReadProgressChanged(bookId, userId))
    }
}