package io.github.vivitoto.vanga.ui.settings.users

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import io.github.vivitoto.vanga.DefaultDateTimeFormats.localDateTimeFormat
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.common.components.DescriptionChips
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry.Companion.stringEntry
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.dialogs.user.PasswordChangeDialog
import io.github.vivitoto.vanga.ui.dialogs.user.UserAddDialog
import io.github.vivitoto.vanga.ui.dialogs.user.UserEditDialog
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import snd.komga.client.user.KomgaAuthenticationActivity
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserId

@Composable
fun UsersContent(
    currentUser: KomgaUser,
    users: Map<KomgaUser, KomgaAuthenticationActivity?>,
    onUserDelete: (KomgaUserId) -> Unit,
    onUserReloadRequest: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        users.forEach { (user, activity) ->
            UserCard(
                currentUser = currentUser,
                user = user,
                latestActivity = activity,
                onUserDelete = onUserDelete,
                onUserReloadRequest = onUserReloadRequest,
            )
        }
        var showUserAddDialog by remember { mutableStateOf(false) }

        FilledTonalButton(
            onClick = { showUserAddDialog = true },
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)

        ) {
            Text("新增用户")
        }

        if (showUserAddDialog) {
            UserAddDialog(onDismiss = { showUserAddDialog = false }, afterConfirm = onUserReloadRequest)
        }
    }
}

@Composable
private fun UserCard(
    currentUser: KomgaUser,
    user: KomgaUser,
    latestActivity: KomgaAuthenticationActivity?,
    onUserDelete: (KomgaUserId) -> Unit,
    onUserReloadRequest: () -> Unit,
) {
    var expandActions by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandActions = !expandActions }
            .cursorForHand(),
        shape = VangaShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserInfo(user, latestActivity)

                Spacer(Modifier.weight(1f))
                Icon(if (expandActions) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
            }


            Column(Modifier.animateContentSize()) {
                if (expandActions) {
                    UserRoles(user)

                    UserActions(
                        currentUser = currentUser,
                        user = user,
                        onUserDelete = onUserDelete,
                        onUserReloadRequest = onUserReloadRequest
                    )
                }
            }
        }

    }
}

@Composable
private fun UserRoles(user: KomgaUser) {
    DescriptionChips(
        label = "角色",
        chipValues = user.roles.map { stringEntry(it) },
    )
}

@Composable
private fun UserInfo(
    user: KomgaUser,
    latestActivity: KomgaAuthenticationActivity?
) {
    val isAdmin = remember(user) { user.roleAdmin() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        if (isAdmin)
            Icon(
                Icons.Default.SupervisorAccount,
                null,
                tint = MaterialTheme.colorScheme.tertiaryContainer
            )
        else
            Icon(Icons.Default.Person, null)

        Spacer(Modifier.width(20.dp))

        Column(Modifier.weight(1f)) {
            Text(user.email)

            val activityText = latestActivity?.let {
                "最近活动：${
                    it.dateTime.toLocalDateTime(TimeZone.currentSystemDefault()).format(localDateTimeFormat)
                }"
            }
                ?: "暂无最近活动"
            Text(
                activityText,
                style = MaterialTheme.typography.bodyMedium
            )

        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserActions(
    currentUser: KomgaUser,
    user: KomgaUser,
    onUserDelete: (KomgaUserId) -> Unit,
    onUserReloadRequest: () -> Unit,
) {
    val isSelf = remember { currentUser.id == user.id }

    var showEditDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp)

        if (!isSelf)
            FilledTonalButton(
                onClick = { showEditDialog = true },
                contentPadding = contentPadding,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(Icons.Default.Edit, null)
                Spacer(Modifier.width(10.dp))
                Text("编辑用户")
            }

        FilledTonalButton(
            onClick = { showChangePasswordDialog = true },
            contentPadding = contentPadding,
            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
        ) {
            Icon(Icons.Default.LockReset, null)
            Spacer(Modifier.width(10.dp))
            Text("修改密码")
        }


        if (!isSelf)
            FilledTonalButton(
                onClick = { showDeleteDialog = true },
                contentPadding = contentPadding,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(10.dp))
                Text("删除用户")
            }
    }

    if (showEditDialog) {
        UserEditDialog(user, onDismiss = { showEditDialog = false }, afterConfirm = onUserReloadRequest)
    }
    if (showChangePasswordDialog) {
        PasswordChangeDialog(
            user = if (user.id != currentUser.id) user else null,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "删除用户",
            body = "用户 ${user.email} 将从服务器删除，此操作不可撤销。要继续吗？",
            confirmText = "删除 \"${user.email}\"",
            buttonConfirm = "删除",
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer,
            onDialogConfirm = { onUserDelete(user.id) },
            onDialogDismiss = { showDeleteDialog = false }
        )
    }

}
