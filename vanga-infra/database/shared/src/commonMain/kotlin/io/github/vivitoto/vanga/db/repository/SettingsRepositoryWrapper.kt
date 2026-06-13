package io.github.vivitoto.vanga.db.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import io.github.vivitoto.vanga.db.AppSettings
import io.github.vivitoto.vanga.db.SettingsStateWrapper
import io.github.vivitoto.vanga.settings.CommonSettingsRepository
import io.github.vivitoto.vanga.settings.model.AppTheme
import io.github.vivitoto.vanga.settings.model.BooksLayout
import io.github.vivitoto.vanga.updates.AppVersion
import kotlin.time.Instant

class SettingsRepositoryWrapper(
    private val wrapper: SettingsStateWrapper<AppSettings>,
) : CommonSettingsRepository {

    override fun getServerUrl(): Flow<String> {
        return wrapper.state.map { it.serverUrl }.distinctUntilChanged()
    }

    override suspend fun putServerUrl(url: String) {
        wrapper.transform { it.copy(serverUrl = url) }
    }

    override fun getCardWidth(): Flow<Int> {
        return wrapper.state.map { it.cardWidth }.distinctUntilChanged()
    }

    override suspend fun putCardWidth(cardWidth: Int) {
        wrapper.transform { it.copy(cardWidth = cardWidth) }
    }

    override fun getCurrentUser(): Flow<String> {
        return wrapper.state.map { it.username }.distinctUntilChanged()
    }

    override suspend fun putCurrentUser(username: String) {
        wrapper.transform { it.copy(username = username) }
    }

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return wrapper.state.map { it.seriesPageLoadSize }.distinctUntilChanged()
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        wrapper.transform { it.copy(seriesPageLoadSize = size) }
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return wrapper.state.map { it.bookPageLoadSize }.distinctUntilChanged()
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        wrapper.transform { it.copy(bookPageLoadSize = size) }
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return wrapper.state.map { it.bookListLayout }.distinctUntilChanged()
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        wrapper.transform { it.copy(bookListLayout = layout) }
    }

    override fun getCheckForUpdatesOnStartup(): Flow<Boolean> {
        return wrapper.state.map { it.checkForUpdatesOnStartup }.distinctUntilChanged()
    }

    override suspend fun putCheckForUpdatesOnStartup(check: Boolean) {
        wrapper.transform { it.copy(checkForUpdatesOnStartup = check) }
    }

    override fun getLastUpdateCheckTimestamp(): Flow<Instant?> {
        return wrapper.state.map { it.updateLastCheckedTimestamp }.distinctUntilChanged()
    }

    override suspend fun putLastUpdateCheckTimestamp(timestamp: Instant) {
        wrapper.transform { it.copy(updateLastCheckedTimestamp = timestamp) }
    }

    override fun getLastCheckedReleaseVersion(): Flow<AppVersion?> {
        return wrapper.state.map { it.updateLastCheckedReleaseVersion }.distinctUntilChanged()
    }

    override suspend fun putLastCheckedReleaseVersion(version: AppVersion) {
        wrapper.transform { it.copy(updateLastCheckedReleaseVersion = version) }
    }

    override fun getDismissedVersion(): Flow<AppVersion?> {
        return wrapper.state.map { it.updateDismissedVersion }.distinctUntilChanged()
    }

    override suspend fun putDismissedVersion(version: AppVersion) {
        wrapper.transform { it.copy(updateDismissedVersion = version) }
    }

    override fun getAppTheme(): Flow<AppTheme> {
        return wrapper.state.map { it.appTheme }.distinctUntilChanged()
    }

    override suspend fun putAppTheme(theme: AppTheme) {
        wrapper.transform { it.copy(appTheme = theme) }
    }

    override fun getLibraryCoversBlurred(): Flow<Boolean> {
        return wrapper.state.map { it.libraryCoversBlurred }.distinctUntilChanged()
    }

    override suspend fun putLibraryCoversBlurred(blurred: Boolean) {
        wrapper.transform { it.copy(libraryCoversBlurred = blurred) }
    }

    override fun getCollectionCoversBlurred(): Flow<Boolean> {
        return wrapper.state.map { it.collectionCoversBlurred }.distinctUntilChanged()
    }

    override suspend fun putCollectionCoversBlurred(blurred: Boolean) {
        wrapper.transform { it.copy(collectionCoversBlurred = blurred) }
    }

    override fun getBookCoversBlurred(): Flow<Boolean> {
        return wrapper.state.map { it.bookCoversBlurred }.distinctUntilChanged()
    }

    override suspend fun putBookCoversBlurred(blurred: Boolean) {
        wrapper.transform { it.copy(bookCoversBlurred = blurred) }
    }

}
