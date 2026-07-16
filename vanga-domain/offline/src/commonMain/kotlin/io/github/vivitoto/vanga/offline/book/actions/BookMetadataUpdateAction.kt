package io.github.vivitoto.vanga.offline.book.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookMetadataUpdateRequest

class BookMetadataUpdateAction : OfflineAction {
    suspend fun run(
        bookId: KomgaBookId,
        request: KomgaBookMetadataUpdateRequest
    ) {
        offlineUnsupported("更新单本元数据")
    }
}
