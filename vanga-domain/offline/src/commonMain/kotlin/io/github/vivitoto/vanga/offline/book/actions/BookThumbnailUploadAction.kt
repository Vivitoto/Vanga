package io.github.vivitoto.vanga.offline.book.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.model.OfflineThumbnailBook
import snd.komga.client.book.KomgaBookId

class BookThumbnailUploadAction : OfflineAction {
    suspend fun run(
        bookId: KomgaBookId,
        file: ByteArray,
        selected: Boolean
    ): OfflineThumbnailBook {
        TODO("Not yet implemented")
    }
}