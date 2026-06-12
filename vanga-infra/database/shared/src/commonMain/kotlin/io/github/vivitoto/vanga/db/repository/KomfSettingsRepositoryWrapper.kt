package io.github.vivitoto.vanga.db.repository

import kotlinx.coroutines.flow.Flow
import io.github.vivitoto.vanga.db.KomfSettings
import io.github.vivitoto.vanga.db.SettingsStateWrapper
import io.github.vivitoto.vanga.settings.KomfSettingsRepository

class KomfSettingsRepositoryWrapper(
    private val wrapper: SettingsStateWrapper<KomfSettings>,
) : KomfSettingsRepository {

    override fun getKomfEnabled(): Flow<Boolean> {
        return wrapper.mapState { it.enabled }
    }

    override suspend fun putKomfEnabled(enabled: Boolean) {
        wrapper.transform { it.copy(enabled = enabled) }
    }

    override fun getKomfUrl(): Flow<String> {
        return wrapper.mapState { it.remoteUrl }
    }

    override suspend fun putKomfUrl(url: String) {
        wrapper.transform { it.copy(remoteUrl = url) }
    }

}