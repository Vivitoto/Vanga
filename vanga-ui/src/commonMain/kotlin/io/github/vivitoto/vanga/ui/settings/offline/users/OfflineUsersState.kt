package io.github.vivitoto.vanga.ui.settings.offline.users

import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.KomgaAuthenticationState
import io.github.vivitoto.vanga.offline.server.actions.MediaServerDeleteAction
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServer
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.server.repository.OfflineMediaServerRepository
import io.github.vivitoto.vanga.offline.settings.OfflineSettingsRepository
import io.github.vivitoto.vanga.offline.user.actions.UserDeleteAction
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository
import io.github.vivitoto.vanga.ui.login.LoginScreen
import snd.komga.client.user.KomgaUserId

class OfflineUsersState(
    private val authState: KomgaAuthenticationState,
    private val appNotifications: AppNotifications,
    private val offlineSettingsRepository: OfflineSettingsRepository,
    private val userRepository: OfflineUserRepository,
    private val serverRepository: OfflineMediaServerRepository,

    private val userDeleteAction: UserDeleteAction,
    private val serverDeleteAction: MediaServerDeleteAction,

    private val coroutineScope: CoroutineScope,
) {

    val isOffline = offlineSettingsRepository.getOfflineMode()
    val offlineUsers = MutableStateFlow<Map<OfflineMediaServer, List<OfflineUser>>>(emptyMap())
    val currentUser = authState.authenticatedUser
    val onlineServerUrl = authState.serverUrl

    private val navigator = MutableStateFlow<Navigator?>(null)

    suspend fun initialize(navigator: Navigator) {
        this.navigator.value = navigator
        loadServers()
    }

    fun goOnline() {
        coroutineScope.launch {
            offlineSettingsRepository.putOfflineMode(false)
            authState.reset()
            navigator.value?.let { navigator ->
                val rootNavigator = navigator.parent ?: navigator
                rootNavigator.replaceAll(LoginScreen())
            }
        }
    }

    private suspend fun loadServers() {
        val servers = serverRepository.findAll()
        val users = servers.associateWith { userRepository.findAllByServer(it.id) }
        offlineUsers.value = users
    }

    fun loginAs(userId: KomgaUserId) {
        appNotifications.runCatchingToNotifications(coroutineScope) {

            if (userId == OfflineUser.ROOT) {
                offlineSettingsRepository.putUserId(OfflineUser.ROOT)
            } else {
                val offlineUser = userRepository.get(userId)
                offlineSettingsRepository.putUserId(offlineUser.id)
            }

            offlineSettingsRepository.putOfflineMode(true)

            authState.reset()
            navigator.value?.let { navigator ->
                val rootNavigator = navigator.parent ?: navigator
                rootNavigator.replaceAll(LoginScreen())
            }
        }
    }

    fun onServerDelete(serverId: OfflineMediaServerId) {
        coroutineScope.launch {
            serverDeleteAction.execute(serverId)
            loadServers()
        }
    }

    fun onUserDelete(userId: KomgaUserId) {
        coroutineScope.launch {
            userDeleteAction.execute(userId)
            loadServers()
        }
    }
}