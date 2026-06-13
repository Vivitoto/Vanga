package io.github.vivitoto.vanga.ui.settings.favoritesync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.common.components.CheckboxWithLabel
import io.github.vivitoto.vanga.ui.common.components.withTextFieldNavigation
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
    onSyncNow: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("收藏同步", style = MaterialTheme.typography.titleMedium)
            Text(
                "收藏默认保存在本机。启用 WebDAV 后，Vanga 会按当前 Komga 服务器和账号同步对应收藏，避免多用户互相覆盖。",
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        CheckboxWithLabel(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            label = { Text("启用 WebDAV 收藏同步") },
        )

        TextField(
            value = webDavUrl,
            onValueChange = onWebDavUrlChange,
            label = { Text("WebDAV 地址") },
            placeholder = { Text("https://example.com/dav") },
            supportingText = { Text("填写 WebDAV 根地址。同步文件会保存到下方远端目录里。") },
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

        Text(
            text = "上次同步：${lastSyncedAt?.toString() ?: "从未同步"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onTestConnection, enabled = !busy) {
                Text("测试连接")
            }
            Spacer(Modifier.width(12.dp))
            FilledTonalButton(onClick = onSyncNow, enabled = !busy) {
                Text(if (busy) "同步中…" else "立即同步")
            }
        }

        Spacer(Modifier.height(80.dp))
    }
}
