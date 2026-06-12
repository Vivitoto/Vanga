package io.github.vivitoto.vanga.offline.sync.actions

import io.github.vivitoto.vanga.offline.action.OfflineAction
import io.github.vivitoto.vanga.offline.sync.model.OfflineLogEntry
import io.github.vivitoto.vanga.offline.sync.repository.LogJournalRepository

class SyncEntrySaveAction(
    private val logJournalRepository: LogJournalRepository,
) : OfflineAction {

    suspend fun execute(entry: OfflineLogEntry) {
        logJournalRepository.save(entry)
    }
}