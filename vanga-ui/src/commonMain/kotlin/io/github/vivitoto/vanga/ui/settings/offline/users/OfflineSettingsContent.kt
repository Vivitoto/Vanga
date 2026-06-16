package io.github.vivitoto.vanga.ui.settings.offline.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServer
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.settings.SettingsCard
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSectionHeader
import io.github.vivitoto.vanga.ui.settings.SettingsValueRow
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserId

@Composable
fun OfflineUserSettingsContent(
    currentUser: KomgaUser?,
    onlineServerUrl: String?,
    serverUsers: Map<OfflineMediaServer, List<OfflineUser>>,
    isOffline: Boolean,
    goOnline: () -> Unit,
    loginAs: (KomgaUserId) -> Unit,
    onServerDelete: (OfflineMediaServerId) -> Unit,
    onUserDelete: (KomgaUserId) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        SettingsSectionCard(
            title = "离线身份",
        ) {
            SettingsValueRow("当前用户", currentUser?.email ?: "无")
            SettingsValueRow("当前状态", if (isOffline) "离线" else "在线")
            SettingsValueRow(
                title = "服务器",
                value = if (currentUser?.id == OfflineUser.ROOT || onlineServerUrl == null) "无" else onlineServerUrl,
            )
            val canGoOffline = remember(isOffline, serverUsers, currentUser) {
                when {
                    isOffline -> false
                    currentUser == null -> false
                    else -> serverUsers.values.flatten().map { it.id }.contains(currentUser.id)
                }
            }

            if (isOffline) {
                SettingsRow(
                    title = "在线模式",
                    trailing = {
                        FilledTonalButton(onClick = { goOnline() }) { Text("回到在线") }
                    }
                )
            } else if (canGoOffline) {
                SettingsRow(
                    title = "离线阅读",
                    trailing = {
                        FilledTonalButton(onClick = { currentUser?.let { loginAs(it.id) } }) { Text("离线阅读") }
                    }
                )
            }
        }

        SettingsSectionHeader(
            title = "离线数据",
        )

        if (serverUsers.isEmpty()) {
            SettingsCard {
                Text("暂无离线用户数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        for ((server, users) in serverUsers) {
            ServerCard(
                server = server,
                users = users,
                onServerDelete = onServerDelete,
                goOffline = loginAs,
                onUserDelete = onUserDelete,
                expandByDefault = serverUsers.size == 1
            )
        }

        if (serverUsers.size > 1) {
            RootUserCard({ loginAs(OfflineUser.ROOT) })
        }

    }
}

@Composable
fun ServerCard(
    server: OfflineMediaServer,
    users: List<OfflineUser>,
    onServerDelete: ((OfflineMediaServerId) -> Unit)?,
    goOffline: (KomgaUserId) -> Unit,
    onUserDelete: (KomgaUserId) -> Unit,
    expandByDefault: Boolean,
) {

    var showUsers by remember { mutableStateOf(expandByDefault || users.size == 1) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    SettingsCard(
        modifier = Modifier
            .clickable { showUsers = !showUsers }
            .pointerHoverIcon(PointerIcon.Hand),
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = server.url,
                    textDecoration = TextDecoration.Underline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${users.size} 个离线用户",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (onServerDelete != null) {
                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(Icons.Default.Delete, null)
                }

                if (showDeleteConfirmation) {
                    ConfirmationDialog(
                        body = "确定删除这个服务器的全部离线数据吗？",
                        confirmText = "删除下载文件和用户数据",
                        onDialogConfirm = { onServerDelete(server.id) },
                        onDialogDismiss = { showDeleteConfirmation = false }
                    )
                }
            }
            Icon(if (showUsers) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
        }

        if (showUsers) {
            for (user in users) {
                HorizontalDivider()
                UserCard(
                    user = user,
                    goOffline = goOffline,
                    onUserDelete = onUserDelete,
                )
            }
        }
    }


}

@Composable
private fun UserCard(
    user: OfflineUser,
    goOffline: (KomgaUserId) -> Unit,
    onUserDelete: (KomgaUserId) -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Person,
            null,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(user.email)
        }

        FilledTonalButton(onClick = { goOffline(user.id) }) {
            Text("切换")
        }

        IconButton(
            onClick = { showDeleteConfirmation = true },
        ) {
            Icon(Icons.Default.Delete, null)
        }
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            body = "确定删除这个离线用户的数据吗？",
            confirmText = "删除用户数据和阅读进度",
            onDialogConfirm = { onUserDelete(user.id) },
            onDialogDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
fun RootUserCard(goOffline: () -> Unit) {
    SettingsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.SupervisorAccount,
                        null,
                        tint = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    Text("全部离线内容")
                }

                Text("可访问所有已下载书籍的特殊离线入口")
                Text("阅读进度不会同步")
            }


            FilledTonalButton(onClick = { goOffline() }) {
                Text("切换")
            }
        }
    }
}
