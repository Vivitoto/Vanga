package io.github.vivitoto.vanga.db

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.Serializable
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

@Serializable
data class OfflineSettings(
    val isOfflineModeEnabled: Boolean = false,
    val downloadDirectory: PlatformFile,
    val userId: KomgaUserId = OfflineUser.ROOT,
    val serverId: OfflineMediaServerId? = null,
    val readProgressSyncDate: Instant? = null,
    val dataSyncDate: Instant? = null,
)
