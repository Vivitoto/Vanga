package io.github.vivitoto.vanga.ui.dialogs.komf.identify

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import io.github.vivitoto.vanga.AppNotification
import io.github.vivitoto.vanga.AppNotifications
import snd.komf.api.KomfServerLibraryId
import snd.komf.client.KomfMetadataClient

class KomfLibraryIdentifyViewmodel(
    private val libraryId: KomfServerLibraryId,
    private val appNotifications: AppNotifications,
    private val komfMetadataClient: KomfMetadataClient,
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    fun autoIdentify() {
        appNotifications.runCatchingToNotifications(scope) {
            komfMetadataClient.matchLibrary(libraryId)
            appNotifications.add(AppNotification.Normal("已启动书库自动识别"))
        }
    }
}