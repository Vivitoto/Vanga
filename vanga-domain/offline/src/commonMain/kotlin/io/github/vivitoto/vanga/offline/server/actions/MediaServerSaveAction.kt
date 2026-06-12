package io.github.vivitoto.vanga.offline.server.actions

import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServer
import io.github.vivitoto.vanga.offline.server.repository.OfflineMediaServerRepository

class MediaServerSaveAction(
    private val mediaServerRepository: OfflineMediaServerRepository,
    private val transactionTemplate: TransactionTemplate
) : OfflineAction {
    suspend fun execute(serverUrl: String): OfflineMediaServer {
        return transactionTemplate.execute {
            val existing = mediaServerRepository.findByUrl(serverUrl)
            if (existing != null) return@execute existing

            val mediaServer = OfflineMediaServer(url = serverUrl)
            mediaServerRepository.save(mediaServer)
            return@execute mediaServer
        }
    }
}