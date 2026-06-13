package io.github.vivitoto.vanga.favorites

import snd.komga.client.book.KomgaBookId
import snd.komga.client.series.KomgaSeriesId
import kotlin.time.Instant

/**
 * App-owned favorites storage.
 *
 * Vanga favorites are local state scoped by server and user. Sync metadata is
 * stored per item so WebDAV can merge multiple devices without losing deletes.
 */
interface LocalFavoritesRepository {
    suspend fun getSeriesIds(scope: LocalFavoritesScope): List<KomgaSeriesId>
    suspend fun addSeries(scope: LocalFavoritesScope, seriesId: KomgaSeriesId)
    suspend fun removeSeries(scope: LocalFavoritesScope, seriesId: KomgaSeriesId)

    suspend fun getBookIds(scope: LocalFavoritesScope): List<KomgaBookId>
    suspend fun addBook(scope: LocalFavoritesScope, bookId: KomgaBookId)
    suspend fun removeBook(scope: LocalFavoritesScope, bookId: KomgaBookId)

    suspend fun getSeriesItems(scope: LocalFavoritesScope, includeDeleted: Boolean = false): List<LocalFavoriteItem>
    suspend fun getBookItems(scope: LocalFavoritesScope, includeDeleted: Boolean = false): List<LocalFavoriteItem>
    suspend fun upsertSeriesItems(scope: LocalFavoritesScope, items: List<LocalFavoriteItem>)
    suspend fun upsertBookItems(scope: LocalFavoritesScope, items: List<LocalFavoriteItem>)
}

data class LocalFavoriteItem(
    val id: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val deleted: Boolean,
)

data class LocalFavoritesScope(
    val serverUrl: String,
    val ownerLabel: String,
) {
    val serverHash: String get() = favoriteStableHash(serverUrl)
    val ownerHash: String get() = favoriteStableHash(ownerLabel)
}

fun localFavoritesScope(serverUrl: String?, ownerLabel: String?): LocalFavoritesScope {
    return LocalFavoritesScope(
        serverUrl = normalizeFavoriteServerUrl(serverUrl),
        ownerLabel = ownerLabel?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: "default-user",
    )
}

fun normalizeFavoriteServerUrl(serverUrl: String?): String {
    return serverUrl
        ?.trim()
        ?.trimEnd('/')
        ?.takeIf { it.isNotBlank() }
        ?: "default-server"
}

fun normalizeWebDavUrl(webDavUrl: String): String {
    val trimmed = webDavUrl.trim().trimEnd('/')
    if (trimmed.isBlank()) return ""
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        return trimmed.replaceFirst("HTTP://", "http://").replaceFirst("HTTPS://", "https://")
    }
    return "https://$trimmed"
}

fun favoriteStableHash(value: String): String {
    var hash = 0xcbf29ce484222325UL
    val prime = 0x100000001b3UL
    value.encodeToByteArray().forEach { byte ->
        hash = hash xor byte.toUByte().toULong()
        hash *= prime
    }
    return hash.toString(16).padStart(16, '0')
}
