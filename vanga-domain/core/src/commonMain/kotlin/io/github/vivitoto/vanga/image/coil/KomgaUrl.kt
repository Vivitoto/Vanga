package io.github.vivitoto.vanga.image.coil

import io.ktor.http.*

fun removeEmptyPathSegments(url: String): String {
    val builder = URLBuilder(url)
    builder.pathSegments = builder.pathSegments.filter { it.isNotBlank() }
    return builder.buildString()
}