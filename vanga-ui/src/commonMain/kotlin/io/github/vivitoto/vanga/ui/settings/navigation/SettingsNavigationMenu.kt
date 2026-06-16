package io.github.vivitoto.vanga.ui.settings.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RecentActors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import io.github.vivitoto.vanga.ui.LocalOfflineMode
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.platform.PlatformType.MOBILE
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.settings.account.AccountSettingsScreen
import io.github.vivitoto.vanga.ui.settings.analysis.MediaAnalysisScreen
import io.github.vivitoto.vanga.ui.settings.announcements.AnnouncementsScreen
import io.github.vivitoto.vanga.ui.settings.appearance.AppSettingsScreen
import io.github.vivitoto.vanga.ui.settings.authactivity.AuthenticationActivityScreen
import io.github.vivitoto.vanga.ui.settings.epub.EpubReaderSettingsScreen
import io.github.vivitoto.vanga.ui.settings.favoritesync.FavoriteSyncSettingsScreen
import io.github.vivitoto.vanga.ui.settings.imagereader.ImageReaderSettingsScreen
import io.github.vivitoto.vanga.ui.settings.komf.general.KomfSettingsScreen
import io.github.vivitoto.vanga.ui.settings.komf.jobs.KomfJobsScreen
import io.github.vivitoto.vanga.ui.settings.komf.notifications.KomfNotificationSettingsScreen
import io.github.vivitoto.vanga.ui.settings.komf.processing.KomfProcessingSettingsScreen
import io.github.vivitoto.vanga.ui.settings.komf.providers.KomfProvidersSettingsScreen
import io.github.vivitoto.vanga.ui.settings.offline.OfflineSettingsScreen
import io.github.vivitoto.vanga.ui.settings.server.ServerSettingsScreen
import io.github.vivitoto.vanga.ui.settings.updates.AppUpdatesScreen
import io.github.vivitoto.vanga.ui.settings.users.UsersScreen
import snd.komf.api.MediaServer.KOMGA
import snd.komga.client.user.KomgaUser
import io.github.vivitoto.vanga.webview.webviewIsAvailable

@Composable
fun SettingsNavigationMenu(
    hasMediaErrors: Boolean,
    komfEnabled: Boolean,
    updatesEnabled: Boolean,
    newVersionIsAvailable: Boolean,
    currentScreen: Screen,
    onNavigation: (Screen) -> Unit = {},
    onLogout: () -> Unit,
    user: KomgaUser?,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val isAdmin = remember(user) { user?.roleAdmin() ?: true }
    val advancedScreenSelected = currentScreen is KomfSettingsScreen ||
            currentScreen is KomfProcessingSettingsScreen ||
            currentScreen is KomfProvidersSettingsScreen ||
            currentScreen is KomfNotificationSettingsScreen ||
            currentScreen is KomfJobsScreen
    var showAdvancedSettings by remember(advancedScreenSelected) { mutableStateOf(advancedScreenSelected) }
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val isOffline = LocalOfflineMode.current.collectAsState().value
        if (!isOffline) {
            NavigationSectionLabel("账号与同步")
            NavigationButton(
                label = "我的账号",
                icon = Icons.Default.Person,
                onClick = { onNavigation(AccountSettingsScreen()) },
                isSelected = currentScreen is AccountSettingsScreen,
                color = contentColor,
            )

            NavigationButton(
                label = "登录记录",
                icon = Icons.Default.RecentActors,
                onClick = { onNavigation(AuthenticationActivityScreen(true)) },
                isSelected = currentScreen is AuthenticationActivityScreen && currentScreen.forMe,
                color = contentColor,
            )

            NavigationButton(
                label = "收藏同步",
                description = "用 WebDAV 在多设备间同步本机收藏",
                icon = Icons.Default.CloudSync,
                onClick = { onNavigation(FavoriteSyncSettingsScreen()) },
                isSelected = currentScreen is FavoriteSyncSettingsScreen,
                color = contentColor,
            )

            Spacer(Modifier.height(6.dp))
        }

        NavigationSectionLabel("阅读与显示")
        NavigationButton(
            label = "外观",
            icon = Icons.Default.Settings,
            onClick = { onNavigation(AppSettingsScreen()) },
            isSelected = currentScreen is AppSettingsScreen,
            color = contentColor,
        )
        NavigationButton(
            label = "图片阅读器",
            icon = Icons.Default.Image,
            onClick = { onNavigation(ImageReaderSettingsScreen()) },
            isSelected = currentScreen is ImageReaderSettingsScreen,
            color = contentColor,
        )
        if (webviewIsAvailable()) {
            NavigationButton(
                label = "EPUB 阅读器",
                icon = Icons.Default.MenuBook,
                onClick = { onNavigation(EpubReaderSettingsScreen()) },
                isSelected = currentScreen is EpubReaderSettingsScreen,
                color = contentColor,
            )
        }

        Spacer(Modifier.height(6.dp))

        NavigationSectionLabel("离线与存储")
        NavigationButton(
            label = "离线模式",
            icon = Icons.Default.Download,
            onClick = { onNavigation(OfflineSettingsScreen()) },
            isSelected = currentScreen is OfflineSettingsScreen,
            color = contentColor,
        )

        if (!isOffline) {
            Spacer(Modifier.height(6.dp))
            if (isAdmin) {
                NavigationSectionLabel("服务器管理")
                NavigationButton(
                    label = "服务器设置",
                    icon = Icons.Default.Settings,
                    onClick = { onNavigation(ServerSettingsScreen()) },
                    isSelected = currentScreen is ServerSettingsScreen,
                    color = contentColor,
                )

                NavigationButton(
                    label = "用户管理",
                    icon = Icons.Default.SupervisorAccount,
                    onClick = { onNavigation(UsersScreen()) },
                    isSelected = currentScreen is UsersScreen,
                    color = contentColor,
                )
                NavigationButton(
                    label = "媒体管理",
                    description = "扫描书库、清理垃圾和处理媒体问题",
                    icon = Icons.Default.BarChart,
                    onClick = { onNavigation(MediaAnalysisScreen()) },
                    isSelected = currentScreen is MediaAnalysisScreen,
                    error = hasMediaErrors,
                    color = contentColor,
                )

                NavigationButton(
                    label = "公告",
                    icon = Icons.Default.Info,
                    onClick = { onNavigation(AnnouncementsScreen()) },
                    isSelected = currentScreen is AnnouncementsScreen,
                    color = contentColor,
                )
                NavigationButton(
                    label = "服务器登录记录",
                    icon = Icons.Default.RecentActors,
                    onClick = { onNavigation(AuthenticationActivityScreen(false)) },
                    isSelected = currentScreen is AuthenticationActivityScreen && !currentScreen.forMe,
                    color = contentColor,
                )
                Spacer(Modifier.height(6.dp))
            }

            if (isAdmin) {
                NavigationButton(
                    label = if (showAdvancedSettings) "收起高级设置" else "高级设置",
                    description = "Komf 元数据、自动化任务和通知",
                    icon = Icons.Default.Extension,
                    onClick = { showAdvancedSettings = !showAdvancedSettings },
                    isSelected = advancedScreenSelected,
                    expanded = showAdvancedSettings,
                    color = contentColor,
                )
                AnimatedVisibility(showAdvancedSettings) {
                    Column(Modifier.padding(start = 18.dp)) {
                        NavigationButton(
                            label = "Komf 连接",
                            icon = Icons.Default.Extension,
                            onClick = { onNavigation(KomfSettingsScreen()) },
                            isSelected = currentScreen is KomfSettingsScreen,
                            color = contentColor,
                        )
                        AnimatedVisibility(komfEnabled) {
                            Column {
                                NavigationButton(
                                    label = "处理规则",
                                    description = "自动匹配和写入元数据的规则",
                                    icon = Icons.Default.Tune,
                                    onClick = { onNavigation(KomfProcessingSettingsScreen(KOMGA)) },
                                    isSelected = currentScreen is KomfProcessingSettingsScreen,
                                    color = contentColor,
                                )
                                NavigationButton(
                                    label = "数据源",
                                    description = "配置漫画元数据来源",
                                    icon = Icons.Default.LocalOffer,
                                    onClick = { onNavigation(KomfProvidersSettingsScreen()) },
                                    isSelected = currentScreen is KomfProvidersSettingsScreen,
                                    color = contentColor,
                                )
                                NavigationButton(
                                    label = "通知",
                                    icon = Icons.Default.Info,
                                    onClick = { onNavigation(KomfNotificationSettingsScreen()) },
                                    isSelected = currentScreen is KomfNotificationSettingsScreen,
                                    color = contentColor,
                                )
                                NavigationButton(
                                    label = "任务记录",
                                    icon = Icons.Default.Cached,
                                    onClick = { onNavigation(KomfJobsScreen()) },
                                    isSelected = currentScreen is KomfJobsScreen,
                                    color = contentColor,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        if (updatesEnabled) {
            if (isOffline) Spacer(Modifier.height(6.dp))
            NavigationSectionLabel("应用")
            NavigationButton(
                label = "版本更新",
                icon = Icons.Default.Cached,
                onClick = { onNavigation(AppUpdatesScreen()) },
                isSelected = currentScreen is AppUpdatesScreen,
                error = newVersionIsAvailable,
                color = contentColor,
            )
        }

        var showLogoutConfirmation by remember { mutableStateOf(false) }
        NavigationButton(
            label = "退出登录",
            icon = Icons.Default.LockReset,
            onClick = { showLogoutConfirmation = true },
            isSelected = false,
            color = contentColor,
        )
        if (showLogoutConfirmation) {
            ConfirmationDialog(
                title = "退出登录",
                body = "确定要退出当前账号吗？",
                buttonConfirm = "退出登录",
                buttonConfirmColor = MaterialTheme.colorScheme.errorContainer,

                onDialogConfirm = onLogout,
                onDialogDismiss = { showLogoutConfirmation = false })
        }
    }
}

@Composable
private fun NavigationSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun NavigationButton(
    label: String,
    description: String? = null,
    icon: ImageVector? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    warn: Boolean = false,
    error: Boolean = false,
    expanded: Boolean? = null,
    color: Color
) {
    val platform = LocalPlatform.current
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.surface
        platform == MOBILE -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.56f)
    }

    val minHeight = when {
        description != null && platform == MOBILE -> 66.dp
        description != null -> 60.dp
        platform == MOBILE -> 50.dp
        else -> 44.dp
    }

    Surface(
        onClick = { if (!isSelected) onClick() },
        shape = VangaShape,
        color = containerColor,
        modifier = Modifier
            .heightIn(min = minHeight)
            .fillMaxWidth()
            .cursorForHand()
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (platform == MOBILE) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(5.dp))
            if (error) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "需要处理",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                )
            } else if (warn) {
                Icon(
                    imageVector = Icons.Default.PriorityHigh,
                    contentDescription = "提醒",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                val trailingIcon = when (expanded) {
                    true -> Icons.Default.ExpandLess
                    false -> Icons.Default.ExpandMore
                    null -> Icons.Default.ChevronRight
                }
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }

}
