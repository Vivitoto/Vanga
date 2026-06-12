package io.github.vivitoto.vanga.api

import io.github.vivitoto.vanga.komga.api.KomgaAnnouncementsApi
import snd.komga.client.announcements.KomgaAnnouncementsClient
import snd.komga.client.announcements.KomgaJsonFeed.KomgaAnnouncementId

class RemoteAnnouncementsApi(private val announcementsClient: KomgaAnnouncementsClient) : KomgaAnnouncementsApi {
    override suspend fun getAnnouncements() = announcementsClient.getAnnouncements()

    override suspend fun markAnnouncementsRead(announcements: List<KomgaAnnouncementId>) =
        announcementsClient.markAnnouncementsRead(announcements)
}