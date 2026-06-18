package io.github.vivitoto.vanga.ui.settings.komf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsScreenContainer
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.komf.general.KomfSettingsScreen
import io.github.vivitoto.vanga.ui.settings.komf.jobs.KomfJobsScreen
import io.github.vivitoto.vanga.ui.settings.komf.notifications.KomfNotificationSettingsScreen
import io.github.vivitoto.vanga.ui.settings.komf.processing.KomfProcessingSettingsScreen
import io.github.vivitoto.vanga.ui.settings.komf.providers.KomfProvidersSettingsScreen
import snd.komf.api.MediaServer.KOMGA

class KomfAdvancedSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        SettingsScreenContainer(title = "高级设置") {
            SettingsSectionCard(
                title = "Komf",
                description = "配置元数据连接、处理规则、数据源、通知和任务。",
            ) {
                AdvancedSettingsRow(
                    title = "Komf 连接",
                    supportingText = "服务器地址、账号和连接开关",
                    icon = { Icon(Icons.Default.Extension, null) },
                    onClick = { navigator.push(KomfSettingsScreen()) },
                )
                AdvancedSettingsRow(
                    title = "处理规则",
                    supportingText = "自动匹配和写入元数据的规则",
                    icon = { Icon(Icons.Default.Tune, null) },
                    onClick = { navigator.push(KomfProcessingSettingsScreen(KOMGA)) },
                )
                AdvancedSettingsRow(
                    title = "数据源",
                    supportingText = "配置漫画元数据来源和每个数据源的独立设置",
                    icon = { Icon(Icons.Default.LocalOffer, null) },
                    onClick = { navigator.push(KomfProvidersSettingsScreen()) },
                )
                AdvancedSettingsRow(
                    title = "通知",
                    supportingText = "元数据任务通知设置",
                    icon = { Icon(Icons.Default.Info, null) },
                    onClick = { navigator.push(KomfNotificationSettingsScreen()) },
                )
                AdvancedSettingsRow(
                    title = "任务记录",
                    supportingText = "查看和管理 Komf 自动化任务",
                    icon = { Icon(Icons.Default.Cached, null) },
                    onClick = { navigator.push(KomfJobsScreen()) },
                )
            }
        }
    }
}

@Composable
private fun AdvancedSettingsRow(
    title: String,
    supportingText: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    SettingsRow(
        title = title,
        supportingText = supportingText,
        onClick = onClick,
        trailing = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                icon()
                Icon(Icons.Default.ChevronRight, null)
            }
        },
    )
}
