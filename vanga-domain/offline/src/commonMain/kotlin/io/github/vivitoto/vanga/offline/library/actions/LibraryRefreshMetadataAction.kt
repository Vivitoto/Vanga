package io.github.vivitoto.vanga.offline.library.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.offlineUnsupported
import snd.komga.client.library.KomgaLibraryId

class LibraryRefreshMetadataAction : OfflineAction {

    suspend fun run(
        libraryId: KomgaLibraryId,
    ) {
        offlineUnsupported("刷新库元数据")
    }
}
