package io.github.vivitoto.vanga.offline

import coil3.PlatformContext
import io.github.vivitoto.vanga.offline.mediacontainer.DivinaExtractor
import io.github.vivitoto.vanga.offline.sync.BookDownloadService
import io.github.vivitoto.vanga.offline.sync.PlatformDownloadManager

internal actual fun createDivinaExtractors(): List<DivinaExtractor> {
    TODO("Not yet implemented")
}

internal actual fun createPlatformDownloadManager(
    downloadService: BookDownloadService,
    androidContext: PlatformContext
): PlatformDownloadManager {
    TODO("Not yet implemented")
}