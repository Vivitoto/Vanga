package io.github.vivitoto.vanga.offline.server.actions

import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.library.actions.LibraryDeleteAction
import io.github.vivitoto.vanga.offline.library.repository.OfflineLibraryRepository
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.server.repository.OfflineMediaServerRepository
import io.github.vivitoto.vanga.offline.user.actions.UserDeleteAction
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository

class MediaServerDeleteAction(
    private val mediaServerRepository: OfflineMediaServerRepository,
    private val libraryRepository: OfflineLibraryRepository,
    private val userRepository: OfflineUserRepository,
    private val transactionTemplate: TransactionTemplate,
    private val libraryDeleteAction: LibraryDeleteAction,
    private val userDeleteAction: UserDeleteAction,
) : OfflineAction {
    suspend fun execute(serverId: OfflineMediaServerId) {
        return transactionTemplate.execute {
            mediaServerRepository.find(serverId) ?: return@execute

            val libraries = libraryRepository.findAllByMediaServer(serverId)
            for (library in libraries) {
                libraryDeleteAction.execute(library.id)
            }

            val users = userRepository.findAllByServer(serverId)
            for (user in users) {
                userDeleteAction.execute(user.id)
            }

            mediaServerRepository.delete(serverId)
        }
    }
}