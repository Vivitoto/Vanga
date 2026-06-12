package io.github.vivitoto.vanga.offline.sync.repository

import io.github.vivitoto.vanga.offline.sync.model.LogEntryId
import io.github.vivitoto.vanga.offline.sync.model.OfflineLogEntry
import snd.komga.client.common.Page

interface LogJournalRepository {
    suspend fun save(entry: OfflineLogEntry)
    suspend fun get(id: LogEntryId): OfflineLogEntry

    suspend fun findAll(limit: Int, offset: Long): Page<OfflineLogEntry>
    suspend fun findAllByType(type: OfflineLogEntry.Type, limit: Int, offset: Long): Page<OfflineLogEntry>

    suspend fun deleteAll()}