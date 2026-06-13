package io.github.vivitoto.vanga.db.favorites

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import io.github.vivitoto.vanga.db.ExposedRepository
import io.github.vivitoto.vanga.db.tables.LocalFavoritesTable
import io.github.vivitoto.vanga.favorites.LocalFavoriteItem
import io.github.vivitoto.vanga.favorites.LocalFavoritesRepository
import io.github.vivitoto.vanga.favorites.LocalFavoritesScope
import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId
import kotlin.time.Clock
import kotlin.time.Instant

class ExposedLocalFavoritesRepository(database: Database) : LocalFavoritesRepository, ExposedRepository(database) {
    override suspend fun getSeriesIds(scope: LocalFavoritesScope): List<KomgaSeriesId> =
        getItems(scope, SERIES, includeDeleted = false).map { KomgaSeriesId(it.id) }

    override suspend fun addSeries(scope: LocalFavoritesScope, seriesId: KomgaSeriesId) =
        setFavorite(scope, SERIES, seriesId.value, deleted = false)

    override suspend fun removeSeries(scope: LocalFavoritesScope, seriesId: KomgaSeriesId) =
        setFavorite(scope, SERIES, seriesId.value, deleted = true)

    override suspend fun getBookIds(scope: LocalFavoritesScope): List<KomgaBookId> =
        getItems(scope, BOOK, includeDeleted = false).map { KomgaBookId(it.id) }

    override suspend fun addBook(scope: LocalFavoritesScope, bookId: KomgaBookId) =
        setFavorite(scope, BOOK, bookId.value, deleted = false)

    override suspend fun removeBook(scope: LocalFavoritesScope, bookId: KomgaBookId) =
        setFavorite(scope, BOOK, bookId.value, deleted = true)

    override suspend fun getSeriesItems(scope: LocalFavoritesScope, includeDeleted: Boolean): List<LocalFavoriteItem> =
        getItems(scope, SERIES, includeDeleted)

    override suspend fun getBookItems(scope: LocalFavoritesScope, includeDeleted: Boolean): List<LocalFavoriteItem> =
        getItems(scope, BOOK, includeDeleted)

    override suspend fun upsertSeriesItems(scope: LocalFavoritesScope, items: List<LocalFavoriteItem>) =
        upsertItems(scope, SERIES, items)

    override suspend fun upsertBookItems(scope: LocalFavoritesScope, items: List<LocalFavoriteItem>) =
        upsertItems(scope, BOOK, items)

    private suspend fun getItems(
        scope: LocalFavoritesScope,
        itemType: String,
        includeDeleted: Boolean,
    ): List<LocalFavoriteItem> {
        return transaction {
            LocalFavoritesTable.selectAll()
                .where {
                    (LocalFavoritesTable.serverUrl.eq(scope.serverUrl)) and
                            (LocalFavoritesTable.ownerLabel.eq(scope.ownerLabel)) and
                            (LocalFavoritesTable.itemType.eq(itemType))
                }
                .let { query -> if (includeDeleted) query else query.andWhere { LocalFavoritesTable.deleted.eq(false) } }
                .orderBy(LocalFavoritesTable.createdAt)
                .map { it.toLocalFavoriteItem() }
        }
    }

    private suspend fun setFavorite(scope: LocalFavoritesScope, itemType: String, itemId: String, deleted: Boolean) {
        val now = Clock.System.now()
        transaction {
            val existing = findExisting(scope, itemType, itemId)
            upsertItem(
                scope = scope,
                itemType = itemType,
                item = LocalFavoriteItem(
                    id = itemId,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                    deleted = deleted,
                )
            )
        }
    }

    private suspend fun upsertItems(scope: LocalFavoritesScope, itemType: String, items: List<LocalFavoriteItem>) {
        if (items.isEmpty()) return
        transaction {
            items.forEach { item -> upsertItem(scope, itemType, item) }
        }
    }

    private fun upsertItem(scope: LocalFavoritesScope, itemType: String, item: LocalFavoriteItem) {
        LocalFavoritesTable.upsert {
            it[serverUrl] = scope.serverUrl
            it[ownerLabel] = scope.ownerLabel
            it[this.itemType] = itemType
            it[itemId] = item.id
            it[createdAt] = item.createdAt.toString()
            it[updatedAt] = item.updatedAt.toString()
            it[deleted] = item.deleted
        }
    }

    private fun findExisting(scope: LocalFavoritesScope, itemType: String, itemId: String): LocalFavoriteItem? {
        return LocalFavoritesTable.selectAll()
            .where {
                (LocalFavoritesTable.serverUrl.eq(scope.serverUrl)) and
                        (LocalFavoritesTable.ownerLabel.eq(scope.ownerLabel)) and
                        (LocalFavoritesTable.itemType.eq(itemType)) and
                        (LocalFavoritesTable.itemId.eq(itemId))
            }
            .firstOrNull()
            ?.toLocalFavoriteItem()
    }

    private fun ResultRow.toLocalFavoriteItem(): LocalFavoriteItem {
        return LocalFavoriteItem(
            id = get(LocalFavoritesTable.itemId),
            createdAt = Instant.parse(get(LocalFavoritesTable.createdAt)),
            updatedAt = Instant.parse(get(LocalFavoritesTable.updatedAt)),
            deleted = get(LocalFavoritesTable.deleted),
        )
    }

    private companion object {
        const val SERIES = "series"
        const val BOOK = "book"
    }
}
