package io.github.vivitoto.vanga.db.tables

import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import io.github.vivitoto.vanga.db.JsonDbDefault
import io.github.vivitoto.vanga.settings.model.TtsuReaderSettings

object EpubReaderSettingsTable : Table("EpubReaderSettings") {
    val bookId = text("book_id")
    val readerType = text("reader_type")
    val komgaSettingsJson = json<JsonObject>("komga_settings_json", JsonDbDefault)
    val ttsuSettingsJson = json<TtsuReaderSettings>("ttsu_settings_json", JsonDbDefault)

    override val primaryKey = PrimaryKey(bookId)
}
