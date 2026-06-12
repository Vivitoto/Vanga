package io.github.vivitoto.vanga.ui.settings.announcements

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaAnnouncementsApi
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LoadState.Error
import io.github.vivitoto.vanga.ui.LoadState.Loading
import io.github.vivitoto.vanga.ui.LoadState.Success
import snd.komga.client.announcements.KomgaJsonFeed
import snd.komga.client.announcements.KomgaJsonFeed.KomgaAnnouncementId

class AnnouncementsViewModel(
    private val appNotifications: AppNotifications,
    private val announcementsApi: KomgaAnnouncementsApi
) : StateScreenModel<LoadState<KomgaJsonFeed>>(Loading) {

    init {
        screenModelScope.launch {

            appNotifications.runCatchingToNotifications {
                mutableState.value = Success(announcementsApi.getAnnouncements())
            }.onFailure { mutableState.value = Error(it) }

        }
    }

    fun markAsRead(id: KomgaAnnouncementId) {
        appNotifications.runCatchingToNotifications(screenModelScope) {
            announcementsApi.markAnnouncementsRead(listOf(id))
        }
    }

}