package io.github.vivitoto.vanga.offline.book.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.model.OfflineThumbnailBook
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.book.KomgaBookId

class BookThumbnailUploadAction : OfflineAction {
    suspend fun run(
        bookId: KomgaBookId,
        file: ByteArray,
        selected: Boolean
    ): OfflineThumbnailBook {
        offlineUnsupported("上传单本缩略图")
    }
}
