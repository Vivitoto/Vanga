package io.github.vivitoto.vanga.offline.book.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.book.KomgaBookId

class BookAnalyzeAction : OfflineAction {

    suspend fun run(bookId: KomgaBookId, ) {
        offlineUnsupported("分析单本")
    }
}
