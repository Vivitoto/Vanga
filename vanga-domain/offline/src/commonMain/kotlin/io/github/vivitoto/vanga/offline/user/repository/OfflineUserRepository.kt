package io.github.vivitoto.vanga.offline.user.repository

import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import snd.komga.client.user.KomgaUserId

interface OfflineUserRepository {
    suspend fun save(user: OfflineUser)

    suspend fun get(id: KomgaUserId): OfflineUser
    suspend fun find(id: KomgaUserId): OfflineUser?
    suspend fun findAll(): List<OfflineUser>
    suspend fun findAllByServer(serverId: OfflineMediaServerId): List<OfflineUser>
    suspend fun delete(id: KomgaUserId)
}