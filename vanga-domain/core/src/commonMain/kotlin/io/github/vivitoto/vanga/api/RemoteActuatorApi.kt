package io.github.vivitoto.vanga.api

import io.github.vivitoto.vanga.komga.api.KomgaActuatorApi
import snd.komga.client.actuator.KomgaActuatorClient

class RemoteActuatorApi(private val actuatorClient: KomgaActuatorClient) : KomgaActuatorApi {
    override suspend fun shutdown() = actuatorClient.shutdown()
}