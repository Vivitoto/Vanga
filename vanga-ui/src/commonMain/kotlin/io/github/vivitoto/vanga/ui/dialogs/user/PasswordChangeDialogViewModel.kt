package io.github.vivitoto.vanga.ui.dialogs.user

import cafe.adriel.voyager.core.model.ScreenModel
import io.github.vivitoto.vanga.AppNotifications
import io.github.vivitoto.vanga.komga.api.KomgaUserApi
import snd.komga.client.user.KomgaUser

class PasswordChangeDialogViewModel(
    private val appNotifications: AppNotifications,
    private val userApi: KomgaUserApi,
    val user: KomgaUser?,
) : ScreenModel {

    suspend fun changePassword(newPassword: String) {
        appNotifications.runCatchingToNotifications {
            if (user == null) userApi.updateMyPassword(newPassword)
            else userApi.updatePassword(user.id, newPassword)
        }
    }
}