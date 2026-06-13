package io.github.vivitoto.vanga.db.favorites

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import io.github.vivitoto.vanga.db.ExposedRepository
import io.github.vivitoto.vanga.db.tables.FavoriteSyncSettingsTable
import io.github.vivitoto.vanga.favorites.FavoriteSyncSettings
import io.github.vivitoto.vanga.favorites.FavoriteSyncSettingsRepository
import io.github.vivitoto.vanga.favorites.LocalFavoritesScope
import kotlin.time.Instant

class ExposedFavoriteSyncSettingsRepository(database: Database) : FavoriteSyncSettingsRepository, ExposedRepository(database) {
    override suspend fun get(scope: LocalFavoritesScope): FavoriteSyncSettings {
        return transaction {
            FavoriteSyncSettingsTable.selectAll()
                .where { FavoriteSyncSettingsTable.scopeMatches(scope) }
                .firstOrNull()
                ?.toFavoriteSyncSettings()
                ?: FavoriteSyncSettings()
        }
    }

    override suspend fun save(scope: LocalFavoritesScope, settings: FavoriteSyncSettings) {
        transaction { upsert(scope, settings) }
    }

    override suspend fun putLastSyncedAt(scope: LocalFavoritesScope, timestamp: Instant?) {
        transaction {
            val current = FavoriteSyncSettingsTable.selectAll()
                .where { FavoriteSyncSettingsTable.scopeMatches(scope) }
                .firstOrNull()
                ?.toFavoriteSyncSettings()
                ?: FavoriteSyncSettings()
            upsert(scope, current.copy(lastSyncedAt = timestamp))
        }
    }

    private fun upsert(scope: LocalFavoritesScope, settings: FavoriteSyncSettings) {
        FavoriteSyncSettingsTable.upsert {
            it[serverHash] = scope.serverHash
            it[userHash] = scope.ownerHash
            it[version] = VERSION
            it[enabled] = settings.enabled
            it[webDavUrl] = settings.webDavUrl
            it[username] = settings.username
            it[password] = settings.password
            it[remotePath] = settings.remotePath
            it[lastSyncedAt] = settings.lastSyncedAt?.toString()
        }
    }

    private fun ResultRow.toFavoriteSyncSettings(): FavoriteSyncSettings {
        return FavoriteSyncSettings(
            enabled = get(FavoriteSyncSettingsTable.enabled),
            webDavUrl = get(FavoriteSyncSettingsTable.webDavUrl),
            username = get(FavoriteSyncSettingsTable.username),
            password = get(FavoriteSyncSettingsTable.password),
            remotePath = get(FavoriteSyncSettingsTable.remotePath),
            lastSyncedAt = get(FavoriteSyncSettingsTable.lastSyncedAt)?.let { Instant.parse(it) },
        )
    }

    private fun FavoriteSyncSettingsTable.scopeMatches(scope: LocalFavoritesScope) =
        serverHash.eq(scope.serverHash) and userHash.eq(scope.ownerHash)

    private companion object {
        const val VERSION = 1
    }
}
