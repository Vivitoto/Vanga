package io.github.vivitoto.vanga.ui.common.menus

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineMenuVisibilityTest {

    @Test
    fun onlineAdminActionsAreHiddenOffline() {
        assertTrue(showOnlineAdminActions(isAdmin = true, isOffline = false))
        assertFalse(showOnlineAdminActions(isAdmin = true, isOffline = true))
        assertFalse(showOnlineAdminActions(isAdmin = false, isOffline = false))
    }

    @Test
    fun komfActionsAreHiddenOfflineEvenWhenKomfIsEnabled() {
        assertTrue(showOnlineKomfActions(komfEnabled = true, isOffline = false))
        assertFalse(showOnlineKomfActions(komfEnabled = true, isOffline = true))
        assertFalse(showOnlineKomfActions(komfEnabled = false, isOffline = false))
    }

    @Test
    fun downloadActionsAreOnlineOnly() {
        assertTrue(showOnlineDownloadAction(showDownloadOption = true, isOffline = false))
        assertFalse(showOnlineDownloadAction(showDownloadOption = true, isOffline = true))
        assertFalse(showOnlineDownloadAction(showDownloadOption = false, isOffline = false))
    }

    @Test
    fun offlineLocalDeleteActionIsOfflineOnly() {
        assertTrue(showOfflineLocalDeleteAction(isOffline = true))
        assertFalse(showOfflineLocalDeleteAction(isOffline = false))
    }
}
