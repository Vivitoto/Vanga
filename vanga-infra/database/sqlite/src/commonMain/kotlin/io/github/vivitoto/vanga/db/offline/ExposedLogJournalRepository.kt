package io.github.vivitoto.vanga.db.offline

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import io.github.vivitoto.vanga.db.ExposedRepository
import io.github.vivitoto.vanga.db.offline.tables.OfflineLogJournalTable
import io.github.vivitoto.vanga.offline.sync.model.LogEntryId
import io.github.vivitoto.vanga.offline.sync.model.OfflineLogEntry
import io.github.vivitoto.vanga.offline.sync.repository.LogJournalRepository
import snd.komga.client.common.Page
import kotlin.time.Instant
import kotlin.uuid.Uuid

class ExposedLogJournalRepository(database: Database) : ExposedRepository(database), LogJournalRepository {

    private val journalTable = OfflineLogJournalTable
    override suspend fun save(entry: OfflineLogEntry) {
        transaction {
            journalTable.insert {
                it[id] = entry.id.value.toHexDashString()
                it[message] = entry.message
                it[type] = entry.type.name
                it[timestamp] = entry.timestamp.epochSeconds
            }
        }
    }

    override suspend fun get(id: LogEntryId): OfflineLogEntry {
        return transaction {
            journalTable.selectAll()
                .where { journalTable.id.eq(id.value.toHexDashString()) }
                .first()
                .toModel()
        }
    }

    override suspend fun findAll(
        limit: Int,
        offset: Long
    ): Page<OfflineLogEntry> {
        return transaction {
            val count = journalTable.select(journalTable.id.count())
                .first()
                .get(journalTable.id.count())

            val result = journalTable.selectAll()
                .orderBy(journalTable.timestamp, SortOrder.DESC)
                .limit(limit)
                .offset(offset)
                .map { it.toModel() }

            page(result = result, count = count, limit = limit, offset = offset, sorted = true)
        }
    }

    override suspend fun findAllByType(
        type: OfflineLogEntry.Type,
        limit: Int,
        offset: Long
    ): Page<OfflineLogEntry> {
        return transaction {
            val count = journalTable.select(journalTable.id.count())
                .where { journalTable.type.eq(type.name) }
                .first()
                .get(journalTable.id.count())

            val result = journalTable.selectAll()
                .where { journalTable.type.eq(type.name) }
                .orderBy(journalTable.timestamp, SortOrder.DESC)
                .limit(limit)
                .offset(offset)
                .map { it.toModel() }

            page(result = result, count = count, limit = limit, offset = offset, sorted = true)
        }
    }

    override suspend fun deleteAll() {
        transaction { journalTable.deleteAll() }
    }

    private fun ResultRow.toModel() = OfflineLogEntry(
        id = LogEntryId(Uuid.parseHexDash(this[journalTable.id])),
        message = this[journalTable.message],
        type = OfflineLogEntry.Type.valueOf(this[journalTable.type]),
        timestamp = Instant.fromEpochSeconds(this[journalTable.timestamp])
    )
}