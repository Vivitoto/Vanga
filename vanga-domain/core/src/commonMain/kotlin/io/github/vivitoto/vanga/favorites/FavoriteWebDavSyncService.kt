package io.github.vivitoto.vanga.favorites

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.Instant

class FavoriteWebDavSyncService(
    private val settingsRepository: FavoriteSyncSettingsRepository,
    private val localFavoritesRepository: LocalFavoritesRepository,
    private val httpClient: HttpClient,
    private val serverUrlProvider: () -> String?,
    private val ownerLabelProvider: () -> String?,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    suspend fun syncNow(): FavoriteSyncResult {
        val scope = localFavoritesScope(
            serverUrl = serverUrlProvider(),
            ownerLabel = ownerLabelProvider(),
        )
        val settings = settingsRepository.get(scope)
        if (!settings.enabled) return FavoriteSyncResult.Disabled
        if (!settings.isConfigured) return FavoriteSyncResult.NotConfigured
        val remoteFileUrl = remoteFileUrl(settings, scope)

        ensureCollections(settings, scope)

        val localSeries = localFavoritesRepository.getSeriesItems(scope, includeDeleted = true)
        val localBooks = localFavoritesRepository.getBookItems(scope, includeDeleted = true)
        val remoteDocument = readRemote(settings, remoteFileUrl)

        val mergedSeries = mergeItems(localSeries, remoteDocument?.series.orEmpty().mapNotNull { it.toLocalFavoriteItemOrNull() })
        val mergedBooks = mergeItems(localBooks, remoteDocument?.books.orEmpty().mapNotNull { it.toLocalFavoriteItemOrNull() })

        localFavoritesRepository.upsertSeriesItems(scope, mergedSeries)
        localFavoritesRepository.upsertBookItems(scope, mergedBooks)

        val now = Clock.System.now()
        val document = FavoriteSyncDocument(
            updatedAt = now.toString(),
            serverHash = scope.serverHash,
            userHash = scope.ownerHash,
            series = mergedSeries.map { it.toRemoteEntry() },
            books = mergedBooks.map { it.toRemoteEntry() },
        )
        writeRemote(settings, remoteFileUrl, document)
        settingsRepository.putLastSyncedAt(scope, now)
        return FavoriteSyncResult.Success(
            seriesCount = mergedSeries.count { !it.deleted },
            bookCount = mergedBooks.count { !it.deleted },
            remoteUrl = remoteFileUrl,
        )
    }

    suspend fun pullFromRemote(): FavoriteSyncResult {
        val scope = localFavoritesScope(
            serverUrl = serverUrlProvider(),
            ownerLabel = ownerLabelProvider(),
        )
        val settings = settingsRepository.get(scope)
        if (!settings.enabled) return FavoriteSyncResult.Disabled
        if (!settings.isConfigured) return FavoriteSyncResult.NotConfigured
        val remoteFileUrl = remoteFileUrl(settings, scope)

        val localSeries = localFavoritesRepository.getSeriesItems(scope, includeDeleted = true)
        val localBooks = localFavoritesRepository.getBookItems(scope, includeDeleted = true)
        val remoteDocument = readRemote(settings, remoteFileUrl)

        val mergedSeries = mergeItems(localSeries, remoteDocument?.series.orEmpty().mapNotNull { it.toLocalFavoriteItemOrNull() })
        val mergedBooks = mergeItems(localBooks, remoteDocument?.books.orEmpty().mapNotNull { it.toLocalFavoriteItemOrNull() })

        localFavoritesRepository.upsertSeriesItems(scope, mergedSeries)
        localFavoritesRepository.upsertBookItems(scope, mergedBooks)

        settingsRepository.putLastSyncedAt(scope, Clock.System.now())
        return FavoriteSyncResult.Success(
            seriesCount = mergedSeries.count { !it.deleted },
            bookCount = mergedBooks.count { !it.deleted },
            remoteUrl = remoteFileUrl,
        )
    }

    suspend fun uploadToRemote(): FavoriteSyncResult {
        val scope = localFavoritesScope(
            serverUrl = serverUrlProvider(),
            ownerLabel = ownerLabelProvider(),
        )
        val settings = settingsRepository.get(scope)
        if (!settings.enabled) return FavoriteSyncResult.Disabled
        if (!settings.isConfigured) return FavoriteSyncResult.NotConfigured
        val remoteFileUrl = remoteFileUrl(settings, scope)

        ensureCollections(settings, scope)

        val localSeries = localFavoritesRepository.getSeriesItems(scope, includeDeleted = true)
        val localBooks = localFavoritesRepository.getBookItems(scope, includeDeleted = true)

        val now = Clock.System.now()
        val document = FavoriteSyncDocument(
            updatedAt = now.toString(),
            serverHash = scope.serverHash,
            userHash = scope.ownerHash,
            series = localSeries.map { it.toRemoteEntry() },
            books = localBooks.map { it.toRemoteEntry() },
        )
        writeRemote(settings, remoteFileUrl, document)
        settingsRepository.putLastSyncedAt(scope, now)
        return FavoriteSyncResult.Success(
            seriesCount = localSeries.count { !it.deleted },
            bookCount = localBooks.count { !it.deleted },
            remoteUrl = remoteFileUrl,
        )
    }

    suspend fun testConnection(): FavoriteSyncResult {
        val scope = localFavoritesScope(serverUrlProvider(), ownerLabelProvider())
        val settings = settingsRepository.get(scope)
        if (!settings.isConfigured) return FavoriteSyncResult.NotConfigured
        ensureCollections(settings, scope)
        return FavoriteSyncResult.ConnectionOk(remoteFileUrl(settings, scope))
    }

    private fun mergeItems(local: List<LocalFavoriteItem>, remote: List<LocalFavoriteItem>): List<LocalFavoriteItem> {
        return (local + remote)
            .groupBy { it.id }
            .values
            .map { candidates ->
                candidates.maxWith(
                    compareBy<LocalFavoriteItem> { it.updatedAt }
                        .thenBy { it.createdAt }
                        .thenBy { it.deleted }
                )
            }
            .sortedWith(compareBy<LocalFavoriteItem> { it.deleted }.thenBy { it.createdAt })
    }

    private suspend fun readRemote(settings: FavoriteSyncSettings, url: String): FavoriteSyncDocument? {
        val response = httpClient.request(url) {
            method = HttpMethod.Get
            addBasicAuth(settings)
        }
        return when (response.status) {
            HttpStatusCode.OK -> runCatching { json.decodeFromString<FavoriteSyncDocument>(response.bodyAsText()) }
                .getOrElse { throw FavoriteWebDavException("WebDAV 远端收藏文件格式无效") }
            HttpStatusCode.NotFound -> null
            else -> throw FavoriteWebDavException("WebDAV 下载收藏失败：HTTP ${response.status.value}")
        }
    }

    private suspend fun writeRemote(settings: FavoriteSyncSettings, url: String, document: FavoriteSyncDocument) {
        val body = json.encodeToString(FavoriteSyncDocument.serializer(), document)
        val response = httpClient.request(url) {
            method = HttpMethod.Put
            addBasicAuth(settings)
            setBody(TextContent(body, ContentType.Application.Json))
        }
        if (response.status.value !in 200..299) {
            throw FavoriteWebDavException("WebDAV 上传收藏失败：HTTP ${response.status.value}")
        }
    }

    private suspend fun ensureCollections(settings: FavoriteSyncSettings, scope: LocalFavoritesScope) {
        val base = settings.webDavUrl.trimEnd('/')
        val parts = settings.remotePath.trim('/').split('/').filter { it.isNotBlank() } + scope.serverHash
        var current = base
        for (part in parts) {
            current += "/${part.encodePathSegment()}"
            val response = httpClient.request(current) {
                method = HttpMethod("MKCOL")
                addBasicAuth(settings)
            }
            if (response.status.value !in listOf(200, 201, 204, 405)) {
                throw FavoriteWebDavException("WebDAV 创建目录失败：HTTP ${response.status.value}")
            }
        }
    }

    private fun remoteFileUrl(settings: FavoriteSyncSettings, scope: LocalFavoritesScope): String {
        val base = settings.webDavUrl.trimEnd('/')
        val path = settings.remotePath.trim('/').split('/').filter { it.isNotBlank() }
        val parts = path + scope.serverHash + "${scope.ownerHash}.json"
        return base + parts.joinToString(separator = "/", prefix = "/") { it.encodePathSegment() }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun io.ktor.client.request.HttpRequestBuilder.addBasicAuth(settings: FavoriteSyncSettings) {
        if (settings.username.isBlank() && settings.password.isBlank()) return
        val credentials = "${settings.username}:${settings.password}".encodeToByteArray()
        header(HttpHeaders.Authorization, "Basic ${Base64.encode(credentials)}")
    }

    private fun String.encodePathSegment(): String = buildString {
        for (byte in this@encodePathSegment.encodeToByteArray()) {
            val c = byte.toInt().toChar()
            if (c.isLetterOrDigit() || c == '-' || c == '_' || c == '.' || c == '~') {
                append(c)
            } else {
                append('%')
                append(byte.toUByte().toString(16).uppercase().padStart(2, '0'))
            }
        }
    }

}

sealed class FavoriteSyncResult {
    data object Disabled : FavoriteSyncResult()
    data object NotConfigured : FavoriteSyncResult()
    data class ConnectionOk(val remoteUrl: String) : FavoriteSyncResult()
    data class Success(
        val seriesCount: Int,
        val bookCount: Int,
        val remoteUrl: String,
    ) : FavoriteSyncResult()
}

class FavoriteWebDavException(message: String) : RuntimeException(message)

@Serializable
private data class FavoriteSyncDocument(
    val version: Int = 1,
    val app: String = "Vanga",
    val updatedAt: String,
    val serverHash: String,
    val userHash: String,
    val series: List<FavoriteSyncEntry> = emptyList(),
    val books: List<FavoriteSyncEntry> = emptyList(),
)

@Serializable
private data class FavoriteSyncEntry(
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    @SerialName("deleted") val deleted: Boolean = false,
) {
    fun toLocalFavoriteItemOrNull(): LocalFavoriteItem? = runCatching {
        LocalFavoriteItem(
            id = id,
            createdAt = Instant.parse(createdAt),
            updatedAt = Instant.parse(updatedAt),
            deleted = deleted,
        )
    }.getOrNull()
}

private fun LocalFavoriteItem.toRemoteEntry(): FavoriteSyncEntry = FavoriteSyncEntry(
    id = id,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
    deleted = deleted,
)
