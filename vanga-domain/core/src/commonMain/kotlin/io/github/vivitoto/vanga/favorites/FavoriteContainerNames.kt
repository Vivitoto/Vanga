package io.github.vivitoto.vanga.favorites

/**
 * Name strategy for Vanga's server-synced favorites containers.
 *
 * Komga has no native favorites API. Vanga stores favorite Series in a
 * regular Komga Collection and favorite Books in a regular Komga ReadList.
 * The generated names are intentionally human-readable because these
 * containers are visible in Komga Web UI.
 */
object FavoriteContainerNames {
    const val SERIES_FAVORITES_PREFIX = "Favorites"
    const val BOOK_FAVORITES_PREFIX = "Favorite Books"

    fun seriesFavorites(ownerLabel: String?): String = buildName(SERIES_FAVORITES_PREFIX, ownerLabel)

    fun bookFavorites(ownerLabel: String?): String = buildName(BOOK_FAVORITES_PREFIX, ownerLabel)

    private fun buildName(prefix: String, ownerLabel: String?): String {
        val normalizedOwner = ownerLabel?.trim()?.takeIf { it.isNotBlank() }
        return if (normalizedOwner == null) prefix else "$prefix - $normalizedOwner"
    }
}
