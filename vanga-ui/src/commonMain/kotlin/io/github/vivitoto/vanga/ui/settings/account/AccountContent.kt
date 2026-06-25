package io.github.vivitoto.vanga.ui.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.dialogs.user.PasswordChangeDialog
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import snd.komga.client.user.KomgaUser

@Composable
fun AccountSettingsContent(user: KomgaUser) {
    var showPasswordDialog by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsSectionCard(title = "账号信息") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }

                // Email — centered
                ProvideTextStyle(MaterialTheme.typography.titleMedium) {
                    Text(
                        text = user.email,
                        textAlign = TextAlign.Center,
                    )
                }

                // Roles — label above, chips centered
                if (user.roles.isNotEmpty()) {
                    RolesSection(user.roles)
                }

                // Change password button — centered
                FilledTonalButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Icon(Icons.Default.LockReset, null)
                    Spacer(Modifier.width(8.dp))
                    Text("修改密码")
                }
            }
        }

        if (showPasswordDialog) {
            PasswordChangeDialog(
                user = user,
                onDismiss = { showPasswordDialog = false },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RolesSection(roles: List<String>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "角色",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            roles.forEach { role ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(role, style = MaterialTheme.typography.labelMedium) },
                )
            }
        }
    }
}
