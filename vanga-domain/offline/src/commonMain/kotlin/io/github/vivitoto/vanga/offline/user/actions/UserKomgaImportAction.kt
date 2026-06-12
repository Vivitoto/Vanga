package io.github.vivitoto.vanga.offline.user.actions

import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import io.github.vivitoto.vanga.offline.user.model.toOfflineUser
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository
import snd.komga.client.user.KomgaUser

class UserKomgaImportAction(
    private val userRepository: OfflineUserRepository,
    private val transactionTemplate: TransactionTemplate
) : OfflineAction {

    suspend fun execute(user: KomgaUser, serverId: OfflineMediaServerId): OfflineUser {
        return transactionTemplate.execute {
            val offlineUser = user.toOfflineUser(serverId)
            userRepository.save(offlineUser)
            offlineUser
        }
    }
}