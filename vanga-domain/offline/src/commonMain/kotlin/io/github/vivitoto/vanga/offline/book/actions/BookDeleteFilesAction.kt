package io.github.vivitoto.vanga.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import io.github.vivitoto.vanga.offline.action.OfflineAction

expect class BookDeleteFilesAction(downloadsDirectory: Flow<PlatformFile>) : OfflineAction {
    suspend fun execute(file: PlatformFile)
}
