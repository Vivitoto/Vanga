package io.github.vivitoto.vanga.offline.api

import io.github.vivitoto.vanga.komga.api.KomgaTaskApi

class OfflineTaskApi : KomgaTaskApi {
    override suspend fun emptyTaskQueue(): Int = 0
}