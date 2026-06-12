package io.github.vivitoto.vanga.offline.api

import io.github.vivitoto.vanga.komga.api.KomgaActuatorApi

class OfflineActuatorApi : KomgaActuatorApi {
    override suspend fun shutdown() = Unit
}