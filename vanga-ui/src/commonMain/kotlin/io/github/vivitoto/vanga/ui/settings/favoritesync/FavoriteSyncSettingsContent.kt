package io.github.vivitoto.vanga.ui.settings.favoritesync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import io.github.vivitoto.vanga.ui.common.components.withTextFieldNavigation
import io.github.vivitoto.vanga.ui.settings.SettingsCheckboxRow
import io.github.vivitoto.vanga.ui.settings.SettingsItemGap
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSectionGap
import io.github.vivitoto.vanga.ui.settings.SettingsValueRow
import kotlin.time.Instant

@Composable
fun FavoriteSyncSettingsContent(
    enabled: Boolean,
    webDavUrl: String,
    username: String,
    password: String,
    remotePath: String,
    lastSyncedAt: Instant?,
    busy: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onWebDavUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRemotePathChange: (String) -> Unit,
    onTestConnection: () -> Unit,
    onSyncToLocal: () -> Unit,
    onSyncToCloud: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SettingsSectionGap)) {
        SettingsSectionCard(
            title = "收藏同步",
            description = "收藏默认保存在本机。启用 WebDAV 后，Vanga 会按当前服务器和账号同步对应收藏，避免多用户互相覆盖。"
        ) {
            SettingsCheckboxRow(
                title = "启用 WebDAV 收藏同步",
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
            SettingsValueRow(
                title = "上次同步",
                value = lastSyncedAt?.toString() ?: "从未同步",
            )
            SettingsRow(
                title = "同步操作",
                supportingText = "同步至本地：从 WebDAV 合并到本机，不上传\n同步至云端：以本机收藏为准上传到 WebDAV",
                stackTrailing = true,
                trailing = {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(SettingsItemGap, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(SettingsItemGap),
                    ) {
                        OutlinedButton(onClick = onTestConnection, enabled = !busy) {
                            Text("测试连接")
                        }
                        OutlinedButton(onClick = onSyncToLocal, enabled = !busy) {
                            Text("同步至本地")
                        }
                        FilledTonalButton(onClick = onSyncToCloud, enabled = !busy) {
                            Text("同步至云端")
                        }
                    }
                }
            )
        }

        SettingsSectionCard(
            title = "WebDAV 连接",
            description = "同步文件会保存到远端目录下，并按服务器和账号分开存放。",
        ) {
            TextField(
                value = webDavUrl,
                onValueChange = onWebDavUrlChange,
                label = { Text("WebDAV 地址") },
                placeholder = { Text("https://example.com/dav") },
                supportingText = { Text("填写 WebDAV 根地址。") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )

            TextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("WebDAV 用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )

            TextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("WebDAV 密码 / App Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )

            TextField(
                value = remotePath,
                onValueChange = onRemotePathChange,
                label = { Text("远端目录") },
                placeholder = { Text("Vanga/favorites") },
                supportingText = { Text("实际文件路径会包含服务器和账号 hash：<远端目录>/<serverHash>/<userHash>.json") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().withTextFieldNavigation(),
            )
        }
    }
}
