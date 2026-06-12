package io.github.vivitoto.vanga.offline

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.vivitoto.vanga.offline.mediacontainer.DivinaExtractor
import io.github.vivitoto.vanga.offline.mediacontainer.EpubExtractor
import io.github.vivitoto.vanga.offline.mediacontainer.divina.DivinaZipExtractor
import io.github.vivitoto.vanga.offline.mediacontainer.divina.EpubZipExtractor
import io.github.vivitoto.vanga.offline.mediacontainer.divina.ZipExtractor
import io.github.vivitoto.vanga.offline.sync.BookDownloadService
import io.github.vivitoto.vanga.offline.sync.DesktopDownloadManager
import io.github.vivitoto.vanga.offline.sync.PlatformDownloadManager
import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import io.github.vivitoto.vanga.offline.sync.repository.LogJournalRepository
import snd.komga.client.KomgaClientFactory
import snd.komga.client.user.KomgaUser

class DesktopOfflineModule(
    repositories: OfflineRepositories,
    onlineUser: StateFlow<KomgaUser?>,
    onlineServerUrl: StateFlow<String>,
    isOffline: StateFlow<Boolean>,
    komgaClientFactory: KomgaClientFactory,
) : OfflineModule(
    repositories = repositories,
    authenticatedUser = onlineUser,
    onlineServerUrl = onlineServerUrl,
    isOffline = isOffline,
    komgaClientFactory = komgaClientFactory,
) {
    private val zipExtractor = ZipExtractor()

    override fun createDivinaExtractors(): List<DivinaExtractor> {
        return listOf(DivinaZipExtractor(zipExtractor))
    }

    override fun createEpubExtractor(): EpubExtractor {
        return EpubZipExtractor(zipExtractor)
    }

    override fun createPlatformDownloadManager(
        downloadService: BookDownloadService,
        logJournalRepository: LogJournalRepository,
        events: MutableSharedFlow<DownloadEvent>,
    ): PlatformDownloadManager {
        return DesktopDownloadManager(
            bookDownloadService = downloadService,
            logsJournalRepository = logJournalRepository,
            sharedEvents = events
        )
    }
}