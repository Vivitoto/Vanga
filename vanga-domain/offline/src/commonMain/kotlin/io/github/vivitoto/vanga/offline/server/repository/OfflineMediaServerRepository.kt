package io.github.vivitoto.vanga.offline.server.repository

import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServer
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import snd.komga.client.user.KomgaUserId

interface OfflineMediaServerRepository {
    suspend fun save(server: OfflineMediaServer)
    suspend fun get(id: OfflineMediaServerId): OfflineMediaServer
    suspend fun find(id: OfflineMediaServerId): OfflineMediaServer?
    suspend fun findAll(): List<OfflineMediaServer>
    suspend fun findByUrl(url: String): OfflineMediaServer?
    suspend fun findByUserId(userId: KomgaUserId): OfflineMediaServer?

    suspend fun delete(id: OfflineMediaServerId)
}