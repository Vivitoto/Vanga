package io.github.vivitoto.vanga.db

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import io.github.vivitoto.vanga.settings.model.EpubReaderType
import io.github.vivitoto.vanga.settings.model.TtsuReaderSettings

@Serializable
data class EpubReaderSettings(
    val readerType: EpubReaderType = EpubReaderType.TTSU_EPUB,
    val komgaReaderSettings: JsonObject = buildJsonObject { },
    val ttsuReaderSettings: TtsuReaderSettings = TtsuReaderSettings(),
)