package io.github.vivitoto.vanga.ui.settings.komf.providers

import io.github.vivitoto.vanga.settings.KomfSettingsRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import snd.komf.api.KomfAuthorRole
import snd.komf.api.KomfAuthorRole.COLORIST
import snd.komf.api.KomfAuthorRole.COVER
import snd.komf.api.KomfAuthorRole.INKER
import snd.komf.api.KomfAuthorRole.LETTERER
import snd.komf.api.KomfAuthorRole.PENCILLER
import snd.komf.api.KomfAuthorRole.WRITER
import snd.komf.api.KomfMediaType
import snd.komf.api.KomfNameMatchingMode
import snd.komf.api.KomfProviders
import snd.komf.api.config.ProviderConf

data class ProviderConfigSnapshot(
    val priority: Int = 1,
    val enabled: Boolean = false,
    val nameMatchingMode: KomfNameMatchingMode? = null,
    val mediaType: KomfMediaType? = null,
    val authorRoles: List<KomfAuthorRole> = listOf(WRITER),
    val artistRoles: List<KomfAuthorRole> = listOf(PENCILLER, INKER, COLORIST, LETTERER, COVER),
    val seriesMetadata: SeriesMetadataSnapshot = SeriesMetadataSnapshot(),
    val bookMetadata: BookMetadataSnapshot = BookMetadataSnapshot(),
)

data class SeriesMetadataSnapshot(
    val ageRating: Boolean = true,
    val authors: Boolean = true,
    val thumbnail: Boolean = true,
    val genres: Boolean = true,
    val links: Boolean = true,
    val publisher: Boolean = true,
    val useOriginalPublisher: Boolean = true,
    val releaseDate: Boolean = true,
    val status: Boolean = true,
    val summary: Boolean = true,
    val tags: Boolean = true,
    val title: Boolean = true,
    val totalBookCount: Boolean = true,
    val books: Boolean = true,
)

data class BookMetadataSnapshot(
    val authors: Boolean = true,
    val thumbnail: Boolean = true,
    val isbn: Boolean = true,
    val links: Boolean = true,
    val number: Boolean = true,
    val releaseDate: Boolean = true,
    val summary: Boolean = true,
    val tags: Boolean = true,
)

data class EHentaiConfigSnapshot(
    val providerConfig: ProviderConfigSnapshot = ProviderConfigSnapshot(),
    val useExhentai: Boolean = false,
    val cookieHeader: String? = null,
    val cookies: Map<String, String> = emptyMap(),
    val userAgent: String? = null,
)

data class KomfAverProvidersConfig(
    val defaultProviders: KomfAverProviderConfigs = KomfAverProviderConfigs(),
    val libraryProviders: Map<String, KomfAverProviderConfigs> = emptyMap(),
) {
    companion object {
        val Empty = KomfAverProvidersConfig()
    }
}

data class KomfAverProviderConfigs(
    val nhentai: ProviderConfigSnapshot = ProviderConfigSnapshot(),
    val ehentai: EHentaiConfigSnapshot = EHentaiConfigSnapshot(),
)

internal fun ProviderConf?.toProviderConfigSnapshot(): ProviderConfigSnapshot {
    val series = this?.seriesMetadata
    val book = this?.bookMetadata
    return ProviderConfigSnapshot(
        priority = this?.priority ?: 1,
        enabled = this?.enabled ?: false,
        nameMatchingMode = this?.nameMatchingMode,
        mediaType = this?.mediaType,
        authorRoles = this?.authorRoles?.toList() ?: listOf(WRITER),
        artistRoles = this?.artistRoles?.toList() ?: listOf(PENCILLER, INKER, COLORIST, LETTERER, COVER),
        seriesMetadata = SeriesMetadataSnapshot(
            ageRating = series?.ageRating ?: true,
            authors = series?.authors ?: true,
            thumbnail = series?.thumbnail ?: true,
            genres = series?.genres ?: true,
            links = series?.links ?: true,
            publisher = series?.publisher ?: true,
            useOriginalPublisher = series?.useOriginalPublisher ?: true,
            releaseDate = series?.releaseDate ?: true,
            status = series?.status ?: true,
            summary = series?.summary ?: true,
            tags = series?.tags ?: true,
            title = series?.title ?: true,
            totalBookCount = series?.totalBookCount ?: true,
            books = series?.books ?: true,
        ),
        bookMetadata = BookMetadataSnapshot(
            authors = book?.authors ?: true,
            thumbnail = book?.thumbnail ?: true,
            isbn = book?.isbn ?: true,
            links = book?.links ?: true,
            number = book?.number ?: true,
            releaseDate = book?.releaseDate ?: true,
            summary = book?.summary ?: true,
            tags = book?.tags ?: true,
        ),
    )
}

class KomfAverCompatibilityClient(
    private val ktor: HttpClient,
    private val komfSettingsRepository: KomfSettingsRepository,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var configPath = ConfigPaths.first()

    suspend fun getProvidersConfig(): KomfAverProvidersConfig {
        val root = requestConfigJson()
        val metadataProviders = root["metadataProviders"]?.jsonObjectOrNull() ?: return KomfAverProvidersConfig.Empty
        val defaultProviders = metadataProviders["defaultProviders"]
            ?.jsonObjectOrNull()
            ?.toAverProviderConfigs()
            ?: KomfAverProviderConfigs()
        val libraryProviders = metadataProviders["libraryProviders"]
            ?.jsonObjectOrNull()
            ?.mapValues { (_, value) -> value.jsonObjectOrNull()?.toAverProviderConfigs() ?: KomfAverProviderConfigs() }
            ?: emptyMap()

        return KomfAverProvidersConfig(defaultProviders, libraryProviders)
    }

    suspend fun updateProviderConfig(
        libraryId: String?,
        provider: KomfProviders,
        update: JsonObject,
    ) {
        val providerUpdate = buildJsonObject {
            put(provider.providerConfigJsonKey, update)
        }
        val metadataProvidersUpdate = buildJsonObject {
            if (libraryId == null) {
                put("defaultProviders", providerUpdate)
            } else {
                put(
                    "libraryProviders",
                    buildJsonObject {
                        put(libraryId, providerUpdate)
                    }
                )
            }
        }
        val request = buildJsonObject {
            put("metadataProviders", metadataProvidersUpdate)
        }

        requestConfigPatch(request)
    }

    suspend fun removeLibraryConfig(libraryId: String) {
        val request = buildJsonObject {
            put(
                "metadataProviders",
                buildJsonObject {
                    put(
                        "libraryProviders",
                        buildJsonObject {
                            put(libraryId, JsonNull)
                        }
                    )
                }
            )
        }

        requestConfigPatch(request)
    }

    private suspend fun requestConfigJson(): JsonObject {
        val baseUrl = komfSettingsRepository.getKomfUrl().first()
        var lastError: Throwable? = null
        for (path in configPath.firstConfigPath()) {
            val response = ktor.get(urlFor(baseUrl, path))
            when {
                response.status.isSuccess() -> {
                    configPath = path
                    return json.parseToJsonElement(response.bodyAsText()).jsonObject
                }

                response.status == HttpStatusCode.NotFound -> lastError = IllegalStateException("Komf config endpoint not found")
                else -> error("Komf config request failed: ${response.status.value} ${response.bodyAsText()}")
            }
        }
        throw lastError ?: IllegalStateException("Komf config endpoint not found")
    }

    private suspend fun requestConfigPatch(request: JsonObject) {
        val baseUrl = komfSettingsRepository.getKomfUrl().first()
        var lastError: Throwable? = null
        for (path in configPath.firstConfigPath()) {
            val response = ktor.patch(urlFor(baseUrl, path)) {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(request.toString())
            }
            when {
                response.status.isSuccess() -> {
                    configPath = path
                    return
                }

                response.status == HttpStatusCode.NotFound -> lastError = IllegalStateException("Komf config endpoint not found")
                else -> error("Komf config update failed: ${response.status.value} ${response.bodyAsText()}")
            }
        }
        throw lastError ?: IllegalStateException("Komf config endpoint not found")
    }

    private fun String.firstConfigPath(): List<String> = listOf(this) + ConfigPaths.filter { it != this }

    private fun urlFor(baseUrl: String, path: String): String = "${baseUrl.trimEnd('/')}/${path.trimStart('/')}"

    private fun JsonObject.toAverProviderConfigs(): KomfAverProviderConfigs {
        return KomfAverProviderConfigs(
            nhentai = this["nhentai"]?.jsonObjectOrNull()?.toProviderConfigSnapshot() ?: ProviderConfigSnapshot(),
            ehentai = this["ehentai"]?.jsonObjectOrNull()?.toEHentaiConfigSnapshot() ?: EHentaiConfigSnapshot(),
        )
    }

    private fun JsonObject.toEHentaiConfigSnapshot(): EHentaiConfigSnapshot {
        return EHentaiConfigSnapshot(
            providerConfig = toProviderConfigSnapshot(),
            useExhentai = boolean("useExhentai") ?: false,
            cookieHeader = string("cookieHeader"),
            cookies = this["cookies"]?.jsonObjectOrNull()
                ?.mapValues { (_, value) -> value.jsonPrimitive.content }
                ?: emptyMap(),
            userAgent = string("userAgent"),
        )
    }

    private fun JsonObject.toProviderConfigSnapshot(): ProviderConfigSnapshot {
        val series = this["seriesMetadata"]?.jsonObjectOrNull()
        val book = this["bookMetadata"]?.jsonObjectOrNull()
        return ProviderConfigSnapshot(
            priority = int("priority") ?: 1,
            enabled = boolean("enabled") ?: false,
            nameMatchingMode = string("nameMatchingMode")?.enumValueOrNull<KomfNameMatchingMode>(),
            mediaType = string("mediaType")?.enumValueOrNull<KomfMediaType>(),
            authorRoles = array("authorRoles")?.mapNotNull { it.jsonPrimitive.content.enumValueOrNull<KomfAuthorRole>() }
                ?: listOf(WRITER),
            artistRoles = array("artistRoles")?.mapNotNull { it.jsonPrimitive.content.enumValueOrNull<KomfAuthorRole>() }
                ?: listOf(PENCILLER, INKER, COLORIST, LETTERER, COVER),
            seriesMetadata = SeriesMetadataSnapshot(
                ageRating = series?.boolean("ageRating") ?: true,
                authors = series?.boolean("authors") ?: true,
                thumbnail = series?.boolean("thumbnail") ?: true,
                genres = series?.boolean("genres") ?: true,
                links = series?.boolean("links") ?: true,
                publisher = series?.boolean("publisher") ?: true,
                useOriginalPublisher = series?.boolean("useOriginalPublisher") ?: true,
                releaseDate = series?.boolean("releaseDate") ?: true,
                status = series?.boolean("status") ?: true,
                summary = series?.boolean("summary") ?: true,
                tags = series?.boolean("tags") ?: true,
                title = series?.boolean("title") ?: true,
                totalBookCount = series?.boolean("totalBookCount") ?: true,
                books = series?.boolean("books") ?: true,
            ),
            bookMetadata = BookMetadataSnapshot(
                authors = book?.boolean("authors") ?: true,
                thumbnail = book?.boolean("thumbnail") ?: true,
                isbn = book?.boolean("isbn") ?: true,
                links = book?.boolean("links") ?: true,
                number = book?.boolean("number") ?: true,
                releaseDate = book?.boolean("releaseDate") ?: true,
                summary = book?.boolean("summary") ?: true,
                tags = book?.boolean("tags") ?: true,
            ),
        )
    }

    private fun JsonObject.boolean(name: String): Boolean? = this[name]?.jsonPrimitive?.booleanOrNull
    private fun JsonObject.int(name: String): Int? = this[name]?.jsonPrimitive?.intOrNull
    private fun JsonObject.string(name: String): String? = this[name]?.takeUnless { it == JsonNull }?.jsonPrimitive?.content
    private fun JsonObject.array(name: String): List<JsonElement>? = (this[name] as? JsonArray)?.toList()
    private fun JsonElement.jsonObjectOrNull(): JsonObject? = this as? JsonObject

    private inline fun <reified T : Enum<T>> String.enumValueOrNull(): T? {
        return enumValues<T>().firstOrNull { it.name == this }
    }

    private fun HttpStatusCode.isSuccess(): Boolean = value in 200..299

    private companion object {
        val ConfigPaths = listOf("api/v1/config", "api/config", "config")
    }
}

internal fun jsonOptionalString(value: String?): JsonElement {
    return value?.let { JsonPrimitive(it) } ?: JsonNull
}
