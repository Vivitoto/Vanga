package io.github.vivitoto.vanga.api

import io.github.vivitoto.vanga.komga.api.KomgaSettingsApi
import snd.komga.client.settings.KomgaSettingsClient
import snd.komga.client.settings.KomgaSettingsUpdateRequest

class RemoteSettingsApi(private val settingsClient: KomgaSettingsClient) : KomgaSettingsApi {
    override suspend fun getSettings() = settingsClient.getSettings()

    override suspend fun updateSettings(request: KomgaSettingsUpdateRequest) = settingsClient.updateSettings(request)
}