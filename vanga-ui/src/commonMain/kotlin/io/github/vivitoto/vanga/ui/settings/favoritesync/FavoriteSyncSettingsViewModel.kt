package io.github.vivitoto.vanga.ui.settings.favoritesync

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.vivitoto.vanga.AppNotification
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.favorites.FavoriteSyncResult
import io.github.vivitoto.vanga.favorites.FavoriteSyncSettings
import io.github.vivitoto.vanga.favorites.FavoriteSyncSettingsRepository
import io.github.vivitoto.vanga.favorites.FavoriteWebDavSyncService
import io.github.vivitoto.vanga.favorites.localFavoritesScope
import io.github.vivitoto.vanga.favorites.normalizeWebDavUrl
import io.github.vivitoto.vanga.ui.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Instant

class FavoriteSyncSettingsViewModel(
    private val settingsRepository: FavoriteSyncSettingsRepository,
    private val syncService: FavoriteWebDavSyncService,
    private val notifications: AppNotifications,
    private val serverUrlProvider: () -> String?,
    private val ownerLabelProvider: () -> String?,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    val enabled = MutableStateFlow(false)
    val webDavUrl = MutableStateFlow("")
    val username = MutableStateFlow("")
    val password = MutableStateFlow("")
    val remotePath = MutableStateFlow("Vanga/favorites")
    val lastSyncedAt = MutableStateFlow<Instant?>(null)
    val busy = MutableStateFlow(false)

    suspend fun initialize() {
        if (state.value != LoadState.Uninitialized) return
        mutableState.value = LoadState.Loading
        load()
        mutableState.value = LoadState.Success(Unit)
    }

    fun onEnabledChange(value: Boolean) = update { copy(enabled = value) }
    fun onWebDavUrlChange(value: String) = update { copy(webDavUrl = value) }
    fun onUsernameChange(value: String) = update { copy(username = value) }
    fun onPasswordChange(value: String) = update { copy(password = value) }
    fun onRemotePathChange(value: String) = update { copy(remotePath = value.ifBlank { "Vanga/favorites" }) }

    fun testConnection() {
        notifications.runCatchingToNotifications(screenModelScope) {
            try {
                busy.value = true
                saveCurrent()
                when (val result = syncService.testConnection()) {
                    FavoriteSyncResult.Disabled -> notifications.add(AppNotification.Normal("收藏同步未启用"))
                    FavoriteSyncResult.NotConfigured -> notifications.add(AppNotification.Error("请先填写 WebDAV 地址和远端目录"))
                    is FavoriteSyncResult.ConnectionOk -> notifications.add(AppNotification.Success("WebDAV 连接正常"))
                    is FavoriteSyncResult.Success -> notifications.add(AppNotification.Success("WebDAV 连接正常"))
                }
            } finally {
                busy.value = false
            }
        }
    }

    fun syncToLocal() {
        notifications.runCatchingToNotifications(screenModelScope) {
            try {
                busy.value = true
                saveCurrent()
                val result = syncService.pullFromRemote()
                load()
                when (result) {
                    FavoriteSyncResult.Disabled -> notifications.add(AppNotification.Normal("收藏同步未启用"))
                    FavoriteSyncResult.NotConfigured -> notifications.add(AppNotification.Error("请先填写 WebDAV 地址和远端目录"))
                    is FavoriteSyncResult.ConnectionOk -> notifications.add(AppNotification.Success("WebDAV 连接正常"))
                    is FavoriteSyncResult.Success -> notifications.add(
                        AppNotification.Success("已同步至本地：${result.seriesCount} 个系列，${result.bookCount} 本书")
                    )
                }
            } finally {
                busy.value = false
            }
        }
    }

    fun pullFromRemote() = syncToLocal()

    fun syncToCloud() {
        notifications.runCatchingToNotifications(screenModelScope) {
            try {
                busy.value = true
                saveCurrent()
                val result = syncService.uploadToRemote()
                load()
                when (result) {
                    FavoriteSyncResult.Disabled -> notifications.add(AppNotification.Normal("收藏同步未启用"))
                    FavoriteSyncResult.NotConfigured -> notifications.add(AppNotification.Error("请先填写 WebDAV 地址和远端目录"))
                    is FavoriteSyncResult.ConnectionOk -> notifications.add(AppNotification.Success("WebDAV 连接正常"))
                    is FavoriteSyncResult.Success -> notifications.add(
                        AppNotification.Success("已同步至云端：${result.seriesCount} 个系列，${result.bookCount} 本书")
                    )
                }
            } finally {
                busy.value = false
            }
        }
    }

    fun syncNow() {
        notifications.runCatchingToNotifications(screenModelScope) {
            try {
                busy.value = true
                saveCurrent()
                when (val result = syncService.syncNow()) {
                    FavoriteSyncResult.Disabled -> notifications.add(AppNotification.Normal("收藏同步未启用"))
                    FavoriteSyncResult.NotConfigured -> notifications.add(AppNotification.Error("请先填写 WebDAV 地址和远端目录"))
                    is FavoriteSyncResult.ConnectionOk -> notifications.add(AppNotification.Success("WebDAV 连接正常"))
                    is FavoriteSyncResult.Success -> {
                        load()
                        notifications.add(AppNotification.Success("已更新本机并上传到 WebDAV：${result.seriesCount} 个系列，${result.bookCount} 本书"))
                    }
                }
            } finally {
                busy.value = false
            }
        }
    }

    private fun update(transform: FavoriteSyncSettings.() -> FavoriteSyncSettings) {
        screenModelScope.launch {
            val next = currentSettings().transform()
            settingsRepository.save(currentScope(), next)
            apply(next)
        }
    }

    private suspend fun saveCurrent() {
        settingsRepository.save(currentScope(), currentSettings())
    }

    private suspend fun load() {
        apply(settingsRepository.get(currentScope()))
    }

    private fun currentScope() = localFavoritesScope(
        serverUrl = serverUrlProvider(),
        ownerLabel = ownerLabelProvider(),
    )

    private fun apply(settings: FavoriteSyncSettings) {
        enabled.value = settings.enabled
        webDavUrl.value = settings.webDavUrl
        username.value = settings.username
        password.value = settings.password
        remotePath.value = settings.remotePath.ifBlank { "Vanga/favorites" }
        lastSyncedAt.value = settings.lastSyncedAt
    }

    private fun currentSettings(): FavoriteSyncSettings = FavoriteSyncSettings(
        enabled = enabled.value,
        webDavUrl = normalizeWebDavUrl(webDavUrl.value),
        username = username.value,
        password = password.value,
        remotePath = remotePath.value.trim().ifBlank { "Vanga/favorites" },
        lastSyncedAt = lastSyncedAt.value,
    )
}
