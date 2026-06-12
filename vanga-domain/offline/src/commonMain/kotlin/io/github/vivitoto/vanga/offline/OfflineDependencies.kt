package io.github.vivitoto.vanga.offline

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import io.github.vivitoto.vanga.offline.action.OfflineActions
import io.github.vivitoto.vanga.offline.api.OfflineKomgaApi
import io.github.vivitoto.vanga.offline.mediacontainer.BookContentExtractors
import io.github.vivitoto.vanga.offline.sync.BookDownloadService
import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import snd.komga.client.sse.KomgaEvent

data class OfflineDependencies(
    val actions: OfflineActions,
    val taskEmitter: OfflineTaskEmitter,
    val komgaEvents: SharedFlow<KomgaEvent>,
    val bookDownloadEvents: MutableSharedFlow<DownloadEvent>,
    val downloadService: BookDownloadService,

    val repositories: OfflineRepositories,
    val fileService: BookContentExtractors,
    val komgaApi: OfflineKomgaApi,
)