package io.github.vivitoto.vanga.db

import kotlinx.serialization.Serializable

@Serializable
data class KomfSettings(
    val enabled: Boolean = false,
    val remoteUrl: String = "http://localhost:8085",
)