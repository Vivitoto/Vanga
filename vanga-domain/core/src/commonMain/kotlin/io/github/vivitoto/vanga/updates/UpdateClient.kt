package io.github.vivitoto.vanga.updates

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

private const val vangaBaseUrl = "https://api.github.com/repos/Vivitoto/Vanga"

class UpdateClient(
    private val ktor: HttpClient,
    private val ktorWithoutCache: HttpClient
) {

    suspend fun getVangaReleases(): List<GithubRelease> {
        return ktor.get("$vangaBaseUrl/releases") {
            parameter("per_page", 5)
        }.body()
    }

    suspend fun getVangaLatestRelease(): GithubRelease {
        return ktor.get("$vangaBaseUrl/releases/latest").body()
    }

    suspend fun streamFile(url: String, block: suspend (response: HttpResponse) -> Unit) {
        ktorWithoutCache.prepareGet(url).execute(block)
    }
}

@Serializable
data class GithubRelease(
    val id: Int,
    @SerialName("published_at")
    val publishedAt: Instant,
    @SerialName("tag_name")
    val tagName: String,
    val name: String? = null,
    @SerialName("html_url")
    val htmlUrl: String,
    val body: String,
    val assets: List<GithubReleaseAsset>
)

@Serializable
data class GithubReleaseAsset(
    val id: Int,
    val name: String,
    @SerialName("content_type")
    val contentType: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String
)