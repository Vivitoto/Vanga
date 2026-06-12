package io.github.vivitoto.vanga.ui.settings.offline

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import coil3.PlatformContext
import kotlinx.coroutines.flow.SharedFlow
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.KomgaAuthenticationState
import io.github.vivitoto.vanga.offline.server.actions.MediaServerDeleteAction
import io.github.vivitoto.vanga.offline.server.repository.OfflineMediaServerRepository
import io.github.vivitoto.vanga.offline.settings.OfflineSettingsRepository
import io.github.vivitoto.vanga.offline.sync.model.DownloadEvent
import io.github.vivitoto.vanga.offline.sync.repository.LogJournalRepository
import io.github.vivitoto.vanga.offline.tasks.OfflineTaskEmitter
import io.github.vivitoto.vanga.offline.user.actions.UserDeleteAction
import io.github.vivitoto.vanga.offline.user.repository.OfflineUserRepository
import io.github.vivitoto.vanga.ui.settings.offline.downloads.OfflineDownloadsState
import io.github.vivitoto.vanga.ui.settings.offline.logs.OfflineLogsState
import io.github.vivitoto.vanga.ui.settings.offline.users.OfflineUsersState

class OfflineSettingsViewModel(
    private val authState: KomgaAuthenticationState,
    private val appNotifications: AppNotifications,
    private val offlineSettingsRepository: OfflineSettingsRepository,
    private val userRepository: OfflineUserRepository,
    private val serverRepository: OfflineMediaServerRepository,
    private val logJournalRepository: LogJournalRepository,
    private val serverDeleteAction: MediaServerDeleteAction,
    private val userDeleteAction: UserDeleteAction,
    private val platformContext: PlatformContext,

    private val taskEmitter: OfflineTaskEmitter,
    private val downloadEvents: SharedFlow<DownloadEvent>,
) : ScreenModel {

    val usersState = OfflineUsersState(
        authState = authState,
        appNotifications = appNotifications,
        offlineSettingsRepository = offlineSettingsRepository,
        userRepository = userRepository,
        serverRepository = serverRepository,
        coroutineScope = screenModelScope,
        userDeleteAction = userDeleteAction,
        serverDeleteAction = serverDeleteAction
    )

    val logsState = OfflineLogsState(
        logJournalRepository = logJournalRepository,
        coroutineScope = screenModelScope,
    )
    val downloadsSate = OfflineDownloadsState(
        downloadEvents = downloadEvents,
        taskEmitter = taskEmitter,
        settingsRepository = offlineSettingsRepository,
        platformContext = platformContext,
        coroutineScope = screenModelScope,
    )

    suspend fun initialize(navigator: Navigator) {
        usersState.initialize(navigator)
        logsState.initialize()
    }
}