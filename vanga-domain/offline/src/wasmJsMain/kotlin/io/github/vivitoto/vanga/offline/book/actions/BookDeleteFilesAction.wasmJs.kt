package io.github.vivitoto.vanga.offline.book.actions

import io.github.vinceglb.filekit.PlatformFile
import io.github.vivitoto.vanga.offline.action.OfflineAction

actual class BookDeleteFilesAction actual constructor() : OfflineAction {
    actual suspend fun execute(file: PlatformFile) {
    }
}