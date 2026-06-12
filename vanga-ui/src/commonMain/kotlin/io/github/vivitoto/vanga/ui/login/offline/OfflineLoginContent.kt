package io.github.vivitoto.vanga.ui.login.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServer
import io.github.vivitoto.vanga.offline.server.model.OfflineMediaServerId
import io.github.vivitoto.vanga.offline.user.model.OfflineUser
import io.github.vivitoto.vanga.ui.settings.offline.users.RootUserCard
import io.github.vivitoto.vanga.ui.settings.offline.users.ServerCard
import snd.komga.client.user.KomgaUserId

@Composable
fun OfflineLoginContent(
    serverUsers: Map<OfflineMediaServer, List<OfflineUser>>,
    loginAs: (KomgaUserId) -> Unit,
    onServerDelete: (OfflineMediaServerId) -> Unit,
    onUserDelete: (KomgaUserId) -> Unit,
    onReturnToLogin: () -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.widthIn(max = 600.dp)
    ) {
        Text("离线阅读", style = MaterialTheme.typography.titleLarge)
        Text("选择已缓存的服务器和账号，在没有网络时继续阅读。", style = MaterialTheme.typography.bodyMedium)

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

        Button(onClick = onReturnToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("返回在线登录")
        }
    }
}
