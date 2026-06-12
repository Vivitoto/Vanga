package io.github.vivitoto.vanga.offline.library.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.KomgaLibraryUpdateRequest

class LibraryPatchAction : OfflineAction {

    suspend fun run(
        libraryId: KomgaLibraryId,
        request: KomgaLibraryUpdateRequest
    ) {
        TODO()
    }
}