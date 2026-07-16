package io.github.vivitoto.vanga.offline.library.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.library.KomgaLibraryId

class LibraryEmptyTrashAction : OfflineAction {

    suspend fun execute(
        libraryId: KomgaLibraryId,
    ) {
        offlineUnsupported("清空库垃圾桶")
    }
}
