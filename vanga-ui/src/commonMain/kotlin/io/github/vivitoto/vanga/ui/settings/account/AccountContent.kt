package io.github.vivitoto.vanga.ui.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.dialogs.user.PasswordChangeDialog
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsValueRow
import snd.komga.client.user.KomgaUser

@Composable
fun AccountSettingsContent(user: KomgaUser) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsSectionCard(
            title = "账号信息",
        ) {
            SettingsValueRow(
                title = "邮箱",
                value = user.email,
            )
            RolesDetails(user)
            PasswordDetails(user)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RolesDetails(user: KomgaUser) {
    SettingsRow(
        title = "角色",
        trailing = {
            FlowRow(
                modifier = Modifier.widthIn(max = 360.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                user.roles.forEach { role ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(role) }
                    )
                }
            }
        }
    )
}

@Composable
private fun PasswordDetails(user: KomgaUser) {

    var showPasswordDialog by remember { mutableStateOf(false) }
    SettingsRow(
        title = "密码",
        trailing = {
            FilledTonalButton(
                onClick = { showPasswordDialog = true },
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text("修改")
            }
        },
    )
    if (showPasswordDialog) {
        PasswordChangeDialog(user = user, onDismiss = { showPasswordDialog = false })
    }
}
