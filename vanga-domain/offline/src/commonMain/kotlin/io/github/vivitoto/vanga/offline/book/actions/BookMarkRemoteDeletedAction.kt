package io.github.vivitoto.vanga.offline.book.actions

import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.model.OfflineBook
import io.github.vivitoto.vanga.offline.book.repository.OfflineBookRepository
import snd.komga.client.book.KomgaBookId

class BookMarkRemoteDeletedAction(
    private val bookRepository: OfflineBookRepository,
    private val transactionTemplate: TransactionTemplate,
) : OfflineAction {

    suspend fun execute(bookId: KomgaBookId): OfflineBook {
        return transactionTemplate.execute {
            val book = bookRepository.get(bookId).markRemoteUnavailable()
            bookRepository.save(book)
            book
        }
    }
}