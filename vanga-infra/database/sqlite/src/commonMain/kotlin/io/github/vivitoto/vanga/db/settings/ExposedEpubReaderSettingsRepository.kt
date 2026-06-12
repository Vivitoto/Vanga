package io.github.vivitoto.vanga.db.settings

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import io.github.vivitoto.vanga.db.EpubReaderSettings
import io.github.vivitoto.vanga.db.ExposedRepository
import io.github.vivitoto.vanga.db.defaultBookId
import io.github.vivitoto.vanga.db.tables.EpubReaderSettingsTable
import io.github.vivitoto.vanga.settings.model.EpubReaderType

class ExposedEpubReaderSettingsRepository(database: Database) : ExposedRepository(database) {

    suspend fun get(): EpubReaderSettings? {
        return transaction {
            EpubReaderSettingsTable.selectAll()
                .where { EpubReaderSettingsTable.bookId.eq(defaultBookId) }
                .firstOrNull()
                ?.let {
                    EpubReaderSettings(
                        readerType = EpubReaderType.valueOf(it[EpubReaderSettingsTable.readerType]),
                        komgaReaderSettings = it[EpubReaderSettingsTable.komgaSettingsJson],
                        ttsuReaderSettings = it[EpubReaderSettingsTable.ttsuSettingsJson]
                    )
                }
        }
    }

    suspend fun save(settings: EpubReaderSettings) {
        transaction {
            EpubReaderSettingsTable.upsert {
                it[bookId] = defaultBookId
                it[readerType] = settings.readerType.name
                it[komgaSettingsJson] = settings.komgaReaderSettings
                it[ttsuSettingsJson] = settings.ttsuReaderSettings
            }
        }
    }
}