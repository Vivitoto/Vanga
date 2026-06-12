package io.github.vivitoto.vanga.komga.api

interface KomgaTaskApi {
    suspend fun emptyTaskQueue(): Int
}