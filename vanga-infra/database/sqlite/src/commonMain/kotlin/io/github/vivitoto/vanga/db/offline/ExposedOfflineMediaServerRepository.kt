package io.github.vivitoto.vanga.db.offline

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import io.github.vivitoto.vanga.db.ExposedRepository
import io.github.vivitoto.vanga.db.offline.tables.OfflineMediaServerTable
import io.github.vivitoto.vanga.db.offline.tables.OfflineUserTable
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServer
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.server.repository.OfflineMediaServerRepository
import snd.komga.client.user.KomgaUserId

class ExposedOfflineMediaServerRepository(
    database: Database
) : ExposedRepository(database), OfflineMediaServerRepository {
    private val serverTable = OfflineMediaServerTable

    override suspend fun save(server: OfflineMediaServer) {
        transaction {
            serverTable.upsert {
                it[serverTable.id] = server.id.value
                it[serverTable.url] = server.url
            }
        }
    }

    override suspend fun get(id: OfflineMediaServerId): OfflineMediaServer {
        return find(id) ?: throw IllegalStateException("server id $id is not found")
    }

    override suspend fun find(id: OfflineMediaServerId): OfflineMediaServer? {
        return transaction {
            serverTable.selectAll()
                .where { serverTable.id.eq(id.value) }
                .firstOrNull()
                ?.toModel()
        }
    }

    override suspend fun findAll(): List<OfflineMediaServer> {
        return transaction {
            serverTable
                .selectAll()
                .map { it.toModel() }
        }
    }

    override suspend fun findByUrl(url: String): OfflineMediaServer? {
        return transaction {
            serverTable.selectAll()
                .where { serverTable.url.eq(url) }
                .firstOrNull()
                ?.toModel()
        }
    }

    override suspend fun findByUserId(userId: KomgaUserId): OfflineMediaServer? {
        return transaction {
            serverTable
                .join(
                    otherTable = OfflineUserTable,
                    joinType = JoinType.LEFT,
                    onColumn = serverTable.id,
                    otherColumn = OfflineUserTable.serverId,
                )
                .select(serverTable.columns)
                .where { OfflineUserTable.id.eq(userId.value) }
                .firstOrNull()
                ?.toModel()
        }
    }

    override suspend fun delete(id: OfflineMediaServerId) {
        transaction {
            serverTable.deleteWhere { serverTable.id.eq(id.value) }
        }
    }

    private fun ResultRow.toModel() = OfflineMediaServer(
        id = OfflineMediaServerId(this[serverTable.id]),
        url = this[serverTable.url]
    )
}