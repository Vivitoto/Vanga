package io.github.vivitoto.vanga.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import io.github.vivitoto.vanga.db.EpubReaderSettings
import io.github.vivitoto.vanga.db.SettingsStateWrapper
import io.github.vivitoto.vanga.settings.EpubReaderSettingsRepository
import io.github.vivitoto.vanga.settings.model.EpubReaderType
import io.github.vivitoto.vanga.settings.model.TtsuReaderSettings

class EpubReaderSettingsRepositoryWrapper(
    private val wrapper: SettingsStateWrapper<EpubReaderSettings>
) : EpubReaderSettingsRepository {

    override fun getReaderType(): Flow<EpubReaderType> {
        return wrapper.mapState { it.readerType }
    }

    override suspend fun putReaderType(type: EpubReaderType) {
        wrapper.transform { it.copy(readerType = type) }
    }

    override suspend fun getKomgaReaderSettings(): JsonObject {
        return wrapper.state.value.komgaReaderSettings
    }

    override suspend fun putKomgaReaderSettings(settings: JsonObject) {
        return wrapper.transform { it.copy(komgaReaderSettings = settings) }
    }

    override suspend fun getTtsuReaderSettings(): TtsuReaderSettings {
        return wrapper.state.value.ttsuReaderSettings
    }

    override suspend fun putTtsuReaderSettings(settings: TtsuReaderSettings) {
        return wrapper.transform { it.copy(ttsuReaderSettings = settings) }
    }
}