package io.github.vivitoto.vanga.offline.user.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import io.github.vivitoto.vanga.db.TransactionTemplate
import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.readprogress.OfflineReadProgressRepository
import io.github.vivitoto.vanga.offline.settings.OfflineSettingsRepository
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository
import snd.komga.client.sse.KomgaEvent
import snd.komga.client.user.KomgaUserId

class UserDeleteAction(
    private val userRepository: OfflineUserRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val settingsRepository: OfflineSettingsRepository,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
) : OfflineAction {

    suspend fun execute(userId: KomgaUserId) {
        return transactionTemplate.execute {
            userRepository.find(userId) ?: return@execute
            if (settingsRepository.getUserId().first() == userId) {
                settingsRepository.putUserId(OfflineUser.ROOT)
            }

            readProgressRepository.deleteByUserId(userId)
            userRepository.delete(userId)
            komgaEvents.emit(KomgaEvent.SessionExpired(userId))
        }
    }
}