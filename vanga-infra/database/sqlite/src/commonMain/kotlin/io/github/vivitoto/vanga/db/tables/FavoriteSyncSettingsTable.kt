package io.github.vivitoto.vanga.db.tables

import org.jetbrains.exposed.v1.core.Table

object FavoriteSyncSettingsTable : Table("FavoriteSyncSettings") {
    val serverHash = text("server_hash")
    val userHash = text("user_hash")
    val version = integer("version")
    val enabled = bool("enabled")
    val webDavUrl = text("webdav_url")
    val username = text("username")
    val password = text("password")
    val remotePath = text("remote_path")
    val lastSyncedAt = text("last_synced_at").nullable()

    override val primaryKey = PrimaryKey(serverHash, userHash)
}
