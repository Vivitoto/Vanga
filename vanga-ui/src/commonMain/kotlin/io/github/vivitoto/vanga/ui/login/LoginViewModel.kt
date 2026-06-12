package io.github.vivitoto.vanga.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.utils.io.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotification
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.KomgaAuthenticationState
import io.github.vivitoto.vanga.komga.api.KomgaLibraryApi
import io.github.vivitoto.vanga.komga.api.KomgaUserApi
import io.github.vivitoto.vanga.offline.api.OfflineLibraryApi
import io.github.vivitoto.vanga.offline.server.repository.OfflineMediaServerRepository
import io.github.vivitoto.vanga.offline.settings.OfflineSettingsRepository
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository
import io.github.vivitoto.vanga.settings.CommonSettingsRepository
import io.github.vivitoto.vanga.settings.SecretsRepository
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LoadState.Uninitialized
import io.github.vivitoto.vanga.ui.error.formatExceptionMessage
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.platform.PlatformType.DESKTOP
import io.github.vivitoto.vanga.ui.platform.PlatformType.MOBILE
import io.github.vivitoto.vanga.ui.platform.PlatformType.WEB_KOMF

class LoginViewModel(
    private val settingsRepository: CommonSettingsRepository,
    private val secretsRepository: SecretsRepository,
    private val komgaUserApi: Flow<KomgaUserApi>,
    private val komgaLibraryApi: Flow<KomgaLibraryApi>,
    private val komgaAuthState: KomgaAuthenticationState,
    private val notifications: AppNotifications,
    private val platform: PlatformType,

    private val offlineUserRepository: OfflineUserRepository,
    private val offlineServerRepository: OfflineMediaServerRepository,
    private val offlineSettingsRepository: OfflineSettingsRepository,
    private val offlineLibraryApi: OfflineLibraryApi,
) : StateScreenModel<LoadState<Unit>>(Uninitialized) {

    var url by mutableStateOf("")
    var user by mutableStateOf("")
    var password by mutableStateOf("")
    var userLoginError by mutableStateOf<String?>(null)
    var autoLoginError by mutableStateOf<String?>(null)
    val offlineIsAvailable = MutableStateFlow(false)
    private val offlineUser = MutableStateFlow<OfflineUser?>(null)
    val canGoOfflineAsCurrentUser = offlineUser.map { it != null }

    fun initialize() {
        if (state.value !is Uninitialized) return

        screenModelScope.launch {
            url = settingsRepository.getServerUrl().first()
            user = settingsRepository.getCurrentUser().first()
            val offlineUsers = offlineUserRepository.findAll()
            val offlineServer = offlineServerRepository.findByUrl(url)

            offlineIsAvailable.value = offlineUsers.any { it.id != OfflineUser.ROOT }
            offlineUser.value = offlineServer?.let { server -> offlineUsers.firstOrNull { it.serverId == server.id } }
            val isOffline = offlineSettingsRepository.getOfflineMode().first()

            when (platform) {
                MOBILE, DESKTOP -> {
                    if (isOffline || secretsRepository.getCookie(url) != null) {
                        tryAutologin()
                    } else {
                        mutableState.value = LoadState.Error(RuntimeException("未登录"))
                    }
                }

                WEB_KOMF -> tryAutologin()
            }
        }
    }

    fun retryAutoLogin() {
        screenModelScope.launch {
            mutableState.value = LoadState.Loading
            tryAutologin()
        }
    }

    fun cancel() {
        screenModelScope.coroutineContext.cancelChildren()
        mutableState.value = LoadState.Error(RuntimeException("已取消登录尝试"))
        userLoginError = "已取消登录尝试"
    }

    fun loginWithCredentials() {
        screenModelScope.launch {
            userLoginError = null
            settingsRepository.putServerUrl(url)
            settingsRepository.putCurrentUser(user)
            tryUserLogin(user, password)
        }
    }

    fun offlineLogin() {
        notifications.runCatchingToNotifications(screenModelScope) {
            val user = offlineUser.value ?: return@runCatchingToNotifications
            offlineSettingsRepository.putOfflineMode(true)
            offlineSettingsRepository.putUserId(user.id)
            komgaAuthState.setStateValues(user.toKomgaUser(), offlineLibraryApi.getLibraries())
            mutableState.value = LoadState.Success(Unit)
        }
    }

    private suspend fun tryAutologin() {
        try {
            tryLogin()
        } catch (e: CancellationException) {
            throw e
        } catch (e: NoTransformationFoundException) {
            val message = "地址 $url 返回了意外响应"
            autoLoginError = message
            notifications.add(AppNotification.Error(message))
            mutableState.value = LoadState.Error(e)
        } catch (e: ClientRequestException) {
            if (e.response.status == Unauthorized) {
                autoLoginError = null
            } else {
                autoLoginError = "登录错误：${e::class.simpleName} ${e.message}"
                notifications.add(AppNotification.Error(e.message))
            }
            mutableState.value = LoadState.Error(e)
        } catch (e: Error) { // wasm fetch error
            val errorMessage = "登录错误：${e::class.simpleName} ${e.message}"
            mutableState.value = LoadState.Error(e)
            notifications.add(AppNotification.Error(errorMessage))
        } catch (e: Throwable) {
            val errorMessage = "登录错误：${e::class.simpleName} ${e.message}"
            autoLoginError = errorMessage
            mutableState.value = LoadState.Error(e)
            notifications.add(AppNotification.Error(errorMessage))
        }
    }

    private suspend fun tryUserLogin(username: String, password: String) {
        try {
            tryLogin(username, password)
        } catch (e: CancellationException) {
            throw e
        } catch (e: NoTransformationFoundException) {
            val message = "地址 $url 返回了意外响应"
            userLoginError = message
            mutableState.value = LoadState.Error(e)
        } catch (e: ClientRequestException) {
            userLoginError = if (e.response.status == Unauthorized) "用户名或密码错误"
            else "登录错误 ${e::class.simpleName}：${e.message}"
            mutableState.value = LoadState.Error(e)
        } catch (e: Throwable) {
            userLoginError = formatExceptionMessage(e)
            mutableState.value = LoadState.Error(e)
        }
    }

    private suspend fun tryLogin(
        username: String? = null,
        password: String? = null
    ) {
        val userApi = this.komgaUserApi.first()
        val libraryApi = this.komgaLibraryApi.first()
        val user =
            if (username != null && password != null) userApi.getMe(username, password, true)
            else userApi.getMe()

        val libraries = libraryApi.getLibraries()
        komgaAuthState.setStateValues(user, libraries)
        mutableState.value = LoadState.Success(Unit)
    }
}

sealed class LoginResult {
    data object Loading : LoginResult()
    data object Error : LoginResult()
    data object Success : LoginResult()
}