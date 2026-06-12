package io.github.vivitoto.vanga.updates

data class UpdateProgress(
    val total: Long,
    val completed: Long,
    val description: String? = null,
)
