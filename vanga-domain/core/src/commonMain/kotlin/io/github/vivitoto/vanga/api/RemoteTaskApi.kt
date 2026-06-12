package io.github.vivitoto.vanga.api

import io.github.vivitoto.vanga.komga.api.KomgaTaskApi
import snd.komga.client.task.KomgaTaskClient

class RemoteTaskApi(private val taskClient: KomgaTaskClient) : KomgaTaskApi {
    override suspend fun emptyTaskQueue()=taskClient.emptyTaskQueue()
}