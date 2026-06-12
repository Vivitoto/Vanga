package io.github.vivitoto.vanga.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import io.github.vivitoto.vanga.settings.model.EpubReaderType
import io.github.vivitoto.vanga.settings.model.TtsuReaderSettings

interface EpubReaderSettingsRepository {
    fun getReaderType(): Flow<EpubReaderType>
    suspend fun putReaderType(type: EpubReaderType)

    suspend fun getKomgaReaderSettings(): JsonObject
    suspend fun putKomgaReaderSettings(settings: JsonObject)

    suspend fun getTtsuReaderSettings(): TtsuReaderSettings
    suspend fun putTtsuReaderSettings(settings: TtsuReaderSettings)
}