package io.github.vivitoto.vanga.favorites

import kotlin.time.Instant

interface FavoriteSyncSettingsRepository {
    suspend fun get(scope: LocalFavoritesScope): FavoriteSyncSettings
    suspend fun save(scope: LocalFavoritesScope, settings: FavoriteSyncSettings)
    suspend fun putLastSyncedAt(scope: LocalFavoritesScope, timestamp: Instant?)
}

data class FavoriteSyncSettings(
    val enabled: Boolean = false,
    val webDavUrl: String = "",
    val username: String = "",
    val password: String = "",
    val remotePath: String = "Vanga/favorites",
    val lastSyncedAt: Instant? = null,
) {
    val isConfigured: Boolean
        get() = webDavUrl.isNotBlank() && remotePath.isNotBlank()
}
