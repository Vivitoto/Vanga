package io.github.vivitoto.vanga.updates

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean

class AndroidAppUpdater(
    private val githubClient: UpdateClient,
    private val context: Context,
) : AppUpdater {
    private var inProgress = AtomicBoolean(false)

    override suspend fun getReleases(): List<AppRelease> {
        return githubClient.getVangaReleases().map { it.toAppRelease() }
    }

    override suspend fun updateToLatest(): Flow<UpdateProgress>? {
        val latest = githubClient.getVangaLatestRelease().toAppRelease()
        return updateTo(latest)
    }

    override fun updateTo(release: AppRelease): Flow<UpdateProgress>? {
        if (!inProgress.compareAndSet(false, true)) return null
        val assetUrl = release.assetUrl
        if (assetUrl == null) {
            inProgress.set(false)
            return null
        }

        return flow {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var downloadId: Long? = null
            var downloadCompleted = false
            try {
                emit(UpdateProgress(0, 0, "准备下载更新"))
                val createdDownloadId = downloadManager.enqueue(release.toDownloadRequest(assetUrl))
                downloadId = createdDownloadId
                val downloadedUri = waitForDownload(downloadManager, createdDownloadId)
                downloadCompleted = true
                emit(UpdateProgress(1, 1, "下载完成，准备安装"))
                openDownloadedApk(downloadedUri)
            } finally {
                if (!downloadCompleted) downloadId?.let { downloadManager.remove(it) }
                inProgress.set(false)
            }
        }
    }

    private fun AppRelease.toDownloadRequest(assetUrl: String): DownloadManager.Request {
        val outputName = assetName ?: "vanga-v${version.toString().replace('.', '_')}.apk"
        return DownloadManager.Request(Uri.parse(assetUrl))
            .setTitle(outputName)
            .setDescription("Vanga ${version} 更新包")
            .setMimeType(APK_MIME_TYPE)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, outputName)
    }

    private suspend fun FlowCollector<UpdateProgress>.waitForDownload(
        downloadManager: DownloadManager,
        downloadId: Long,
    ): Uri {
        val query = DownloadManager.Query().setFilterById(downloadId)
        while (true) {
            downloadManager.query(query).use { cursor ->
                if (!cursor.moveToFirst()) throw IllegalStateException("更新下载任务不存在")

                val downloaded = cursor.longValue(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR).coerceAtLeast(0)
                val total = cursor.longValue(DownloadManager.COLUMN_TOTAL_SIZE_BYTES).coerceAtLeast(0)
                emit(UpdateProgress(total, downloaded, "正在下载更新"))

                when (cursor.intValue(DownloadManager.COLUMN_STATUS)) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        return downloadManager.getUriForDownloadedFile(downloadId)
                            ?: throw IllegalStateException("无法打开已下载的更新包")
                    }

                    DownloadManager.STATUS_FAILED -> {
                        val reason = cursor.intValue(DownloadManager.COLUMN_REASON)
                        throw IllegalStateException("更新下载失败：$reason")
                    }
                }
            }
            delay(500)
        }
    }

    private fun openDownloadedApk(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, APK_MIME_TYPE)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun Cursor.intValue(columnName: String): Int =
        getInt(getColumnIndexOrThrow(columnName))

    private fun Cursor.longValue(columnName: String): Long =
        getLong(getColumnIndexOrThrow(columnName))

    private fun GithubRelease.toAppRelease(): AppRelease {
        val asset = assets.firstOrNull { it.name.endsWith(".apk") }

        return AppRelease(
            version = AppVersion.fromGithubRelease(this),
            publishDate = publishedAt,
            releaseNotesBody = body.replace("\r", ""),
            htmlUrl = htmlUrl,
            assetName = asset?.name,
            assetUrl = asset?.browserDownloadUrl
        )
    }

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
