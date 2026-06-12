package io.github.vivitoto.vanga.offline.book.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.model.OfflineBook
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookMetadataRepository
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import io.github.vivitoto.vanga.offline.book.repository.OfflineThumbnailBookRepository
import io.github.vivitoto.vanga.offline.media.repository.OfflineMediaRepository
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgressRepository
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.sse.KomgaEvent.BookDeleted

class BookDeleteManyAction(
    private val bookRepository: OfflineBookRepository,
    private val bookMetadataRepository: OfflineBookMetadataRepository,
    private val thumbnailBookRepository: OfflineThumbnailBookRepository,
    private val mediaRepository: OfflineMediaRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
    private val taskEmitter: OfflineTaskEmitter,
) : OfflineAction {

    suspend fun execute(books: List<OfflineBook>) {
        transactionTemplate.execute {
            val bookIds = books.map { it.id }

            readProgressRepository.deleteByBookIds(bookIds)
            mediaRepository.delete(bookIds)
            thumbnailBookRepository.deleteByBookIds(bookIds)
            bookMetadataRepository.delete(bookIds)
            bookRepository.delete(bookIds)
        }

        books.forEach { book ->
            komgaEvents.emit(BookDeleted(book.id, book.seriesId, book.libraryId))
            taskEmitter.deleteBookFiles(book.fileDownloadPath)
        }
    }
}