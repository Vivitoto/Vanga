package io.github.vivitoto.vanga.komga.api

import snd.komga.client.announcements.KomgaJsonFeed
import snd.komga.client.announcements.KomgaJsonFeed.KomgaAnnouncementId


interface KomgaAnnouncementsApi {
    suspend fun getAnnouncements(): KomgaJsonFeed
    suspend fun markAnnouncementsRead(announcements: List<KomgaAnnouncementId>)
}