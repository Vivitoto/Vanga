package io.github.vivitoto.vanga.ui.login.offline

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.KomgaAuthenticationState
import io.github.vivitoto.vanga.offline.api.OfflineLibraryApi
import io.github.vivitoto.vanga.offline.server.actions.MediaServerDeleteAction
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServer
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.server.repository.OfflineMediaServerRepository
import io.github.vivitoto.vanga.offline.settings.OfflineSettingsRepository
import io.github.vivitoto.vanga.offline.user.actions.UserDeleteAction
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository
import io.github.vivitoto.vanga.ui.MainScreen
import snd.komga.client.user.KomgaUserId

class OfflineLoginViewModel(
    private val appNotifications: AppNotifications,
    private val offlineSettingsRepository: OfflineSettingsRepository,
    private val userRepository: OfflineUserRepository,
    private val serverRepository: OfflineMediaServerRepository,
    private val komgaAuthState: KomgaAuthenticationState,
    private val offlineLibraryApi: OfflineLibraryApi,
    private val serverDeleteAction: MediaServerDeleteAction,
    private val userDeleteAction: UserDeleteAction,
) : ScreenModel {

    val offlineUsers = MutableStateFlow<Map<OfflineMediaServer, List<OfflineUser>>>(emptyMap())
    private val navigator = MutableStateFlow<Navigator?>(null)

    suspend fun initialize(navigator: Navigator) {
        this.navigator.value = navigator
        loadServers()
    }

    private suspend fun loadServers() {
        val servers = serverRepository.findAll()
        val users = servers.associateWith { userRepository.findAllByServer(it.id) }
        offlineUsers.value = users
    }

    fun loginAs(userId: KomgaUserId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            val user = userRepository.get(userId)
            offlineSettingsRepository.putUserId(user.id)
            offlineSettingsRepository.putOfflineMode(true)
            komgaAuthState.setStateValues(user.toKomgaUser(), offlineLibraryApi.getLibraries())
            navigator.value?.replaceAll(MainScreen())
        }
    }

    fun onServerDelete(serverId: OfflineMediaServerId) {
        screenModelScope.launch {
            serverDeleteAction.execute(serverId)
            loadServers()
        }
    }

    fun onUserDelete(userId: KomgaUserId) {
        screenModelScope.launch {
            userDeleteAction.execute(userId)
            loadServers()
        }
    }
}