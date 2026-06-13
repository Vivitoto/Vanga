package io.github.vivitoto.vanga.settings

import kotlinx.coroutines.flow.Flow
import io.github.vivitoto.vanga.settings.model.AppTheme
import io.github.vivitoto.vanga.settings.model.BooksLayout
import io.github.vivitoto.vanga.updates.AppVersion
import kotlin.time.Instant

interface CommonSettingsRepository {
    fun getServerUrl(): Flow<String>
    suspend fun putServerUrl(url: String)

    fun getCardWidth(): Flow<Int>
    suspend fun putCardWidth(cardWidth: Int)

    fun getCurrentUser(): Flow<String>
    suspend fun putCurrentUser(username: String)

    fun getSeriesPageLoadSize(): Flow<Int>
    suspend fun putSeriesPageLoadSize(size: Int)

    fun getBookPageLoadSize(): Flow<Int>
    suspend fun putBookPageLoadSize(size: Int)

    fun getBookListLayout(): Flow<BooksLayout>
    suspend fun putBookListLayout(layout: BooksLayout)

    fun getCheckForUpdatesOnStartup(): Flow<Boolean>
    suspend fun putCheckForUpdatesOnStartup(check: Boolean)

    fun getLastUpdateCheckTimestamp(): Flow<Instant?>
    suspend fun putLastUpdateCheckTimestamp(timestamp: Instant)

    fun getLastCheckedReleaseVersion(): Flow<AppVersion?>
    suspend fun putLastCheckedReleaseVersion(version: AppVersion)

    fun getDismissedVersion(): Flow<AppVersion?>
    suspend fun putDismissedVersion(version: AppVersion)

    fun getAppTheme(): Flow<AppTheme>
    suspend fun putAppTheme(theme: AppTheme)

    fun getLibraryCoversBlurred(): Flow<Boolean>
    suspend fun putLibraryCoversBlurred(blurred: Boolean)

    fun getCollectionCoversBlurred(): Flow<Boolean>
    suspend fun putCollectionCoversBlurred(blurred: Boolean)

    fun getBookCoversBlurred(): Flow<Boolean>
    suspend fun putBookCoversBlurred(blurred: Boolean)
}
