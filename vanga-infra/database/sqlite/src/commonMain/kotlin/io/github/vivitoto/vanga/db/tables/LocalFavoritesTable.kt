package io.github.vivitoto.vanga.db.tables

import org.jetbrains.exposed.v1.core.Table

object LocalFavoritesTable : Table("LocalFavorites") {
    val serverUrl = text("server_url")
    val ownerLabel = text("owner_label")
    val itemType = varchar("item_type", 16)
    val itemId = text("item_id")
    val createdAt = text("created_at")
    val updatedAt = text("updated_at")
    val deleted = bool("deleted")

    override val primaryKey = PrimaryKey(serverUrl, ownerLabel, itemType, itemId)
}
