package io.github.vivitoto.vanga.offline.book.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.book.model.OfflineThumbnailBook
import snd.komga.client.book.KomgaBookId
import snd.komga.client.common.KomgaThumbnailId

class BookThumbnailSelectAction : OfflineAction {

    suspend fun run(
        bookId: KomgaBookId,
        thumbnailId: KomgaThumbnailId
    ): OfflineThumbnailBook {
        TODO("Not yet implemented")
    }
}