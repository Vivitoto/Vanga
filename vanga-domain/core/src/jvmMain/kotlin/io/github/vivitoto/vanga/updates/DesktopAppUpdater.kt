package io.github.vivitoto.vanga.updates

import kotlinx.coroutines.flow.Flow
import org.jetbrains.skiko.URIManager

class DesktopAppUpdater(
    private val updateClient: UpdateClient
) : AppUpdater {
    override suspend fun getReleases(): List<AppRelease> {
        return updateClient.getVangaReleases().map {
            AppRelease(
                version = AppVersion.fromGithubRelease(it),
                publishDate = it.publishedAt,
                releaseNotesBody = it.body.replace("\r", ""),
                htmlUrl = it.htmlUrl,
                assetName = null,
                assetUrl = null
            )
        }
    }

    override suspend fun updateToLatest(): Flow<UpdateProgress>? {
        val latest = updateClient.getVangaLatestRelease()
        URIManager().openUri(latest.htmlUrl)
        return null
    }

    override fun updateTo(release: AppRelease): Flow<UpdateProgress>? {
        URIManager().openUri(release.htmlUrl)
        return null
    }
}