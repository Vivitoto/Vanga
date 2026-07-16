package io.github.vivitoto.vanga.offline.library.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.library.KomgaLibraryId

class LibraryAnalyzeAction : OfflineAction {

    suspend fun run(
        libraryId: KomgaLibraryId,
    ) {
        offlineUnsupported("分析库")
    }
}
