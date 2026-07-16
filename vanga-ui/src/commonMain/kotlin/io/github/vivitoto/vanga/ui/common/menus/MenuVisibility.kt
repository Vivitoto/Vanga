package io.github.vivitoto.vanga.ui.common.menus

internal fun showOnlineAdminActions(
    isAdmin: Boolean,
    isOffline: Boolean,
): Boolean = isAdmin && !isOffline

internal fun showOnlineKomfActions(
    komfEnabled: Boolean,
    isOffline: Boolean,
): Boolean = komfEnabled && !isOffline

internal fun showKomfBookIdentifyAction(
    komfEnabled: Boolean,
    isOffline: Boolean,
    hasBookContext: Boolean,
): Boolean = komfEnabled && !isOffline && hasBookContext

internal fun showOnlineDownloadAction(
    showDownloadOption: Boolean,
    isOffline: Boolean,
): Boolean = showDownloadOption && !isOffline

internal fun showOfflineLocalDeleteAction(
    isOffline: Boolean,
): Boolean = isOffline
