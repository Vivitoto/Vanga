package io.github.vivitoto.vanga.favorites

sealed interface FavoriteState<out T> {
    data object Uninitialized : FavoriteState<Nothing>
    data object Loading : FavoriteState<Nothing>
    data class Loaded<T>(val value: T, val stale: Boolean = false) : FavoriteState<T>
    data class Error(val error: Throwable) : FavoriteState<Nothing>
}
