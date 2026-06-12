package io.github.vivitoto.vanga.ui.settings.offline.downloads

import coil3.PlatformContext
import io.github.vinceglb.filekit.PlatformFile
import io.github.vivitoto.vanga.AppDirectories

internal actual fun getDefaultInternalDownloadsDir(platformContent: PlatformContext): DefaultDownloadStorageLocation {
    return DefaultDownloadStorageLocation(
        platformFile = PlatformFile(AppDirectories.defaultOfflineLibraryPath.toFile()),
        label = AppDirectories.defaultOfflineLibraryPath.toString()
    )
}