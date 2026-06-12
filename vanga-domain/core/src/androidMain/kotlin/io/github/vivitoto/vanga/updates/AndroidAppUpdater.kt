package io.github.vivitoto.vanga.updates

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION
import android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
import android.os.Build
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
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
            var sessionId: Int? = null
            try {
                emit(UpdateProgress(0, 0))
                val sessionParams = PackageInstaller.SessionParams(MODE_FULL_INSTALL)
                val packageInstaller = context.packageManager.packageInstaller
                val createdSessionId = packageInstaller.createSession(sessionParams)
                sessionId = createdSessionId
                val session = packageInstaller.openSession(createdSessionId)

                session.use {
                    githubClient.streamFile(assetUrl) { response -> streamToSession(response, it) }

                    val receiverIntent = Intent(context, PackageInstallerStatusReceiver::class.java)
                    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }
                    val receiverPendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, flags)
                    it.commit(receiverPendingIntent.intentSender)
                }
            } catch (e: Exception) {
                sessionId?.let { context.packageManager.packageInstaller.abandonSession(it) }
                throw e
            } finally {
                inProgress.set(false)
            }
        }
    }

    private suspend fun FlowCollector<UpdateProgress>.streamToSession(
        response: HttpResponse,
        session: PackageInstaller.Session
    ) {
        val length = response.headers["Content-Length"]?.toLong() ?: 0L
        emit(UpdateProgress(length, 0))
        val channel = response.bodyAsChannel().counted()
        val sessionStream = session.openWrite("vanga", 0, -1)
        sessionStream.buffered().use { bufferedSessionStream ->
            while (!channel.isClosedForRead) {

                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.exhausted()) {
                    val bytes = packet.readByteArray()
                    bufferedSessionStream.write(bytes)
                }
                emit(UpdateProgress(length, channel.totalBytesRead))
            }
            bufferedSessionStream.flush()
            session.fsync(sessionStream)
        }
    }

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
}

class PackageInstallerStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirmationIntent != null) {
                    context.startActivity(confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }

            else -> {}
        }
    }
}
