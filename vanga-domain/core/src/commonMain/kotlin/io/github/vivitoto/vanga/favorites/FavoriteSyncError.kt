package io.github.vivitoto.vanga.favorites

sealed class FavoriteSyncError(message: String, cause: Throwable? = null) : RuntimeException(message, cause) {
    class AdminRequired(cause: Throwable? = null) : FavoriteSyncError(
        "Server-synced favorites require a Komga account with ADMIN permissions",
        cause
    )

    class ContainerConflict(containerName: String) : FavoriteSyncError(
        "Multiple Komga containers matched favorites container name: $containerName"
    )

    class SyncFailed(operation: String, cause: Throwable? = null) : FavoriteSyncError(
        "Favorite sync failed while trying to $operation",
        cause
    )
}
