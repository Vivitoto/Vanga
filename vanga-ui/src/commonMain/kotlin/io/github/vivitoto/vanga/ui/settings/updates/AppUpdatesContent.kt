package io.github.vivitoto.vanga.ui.settings.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import io.github.vivitoto.vanga.DefaultDateTimeFormats.localDateFormat
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.common.components.AppCircularProgressIndicator
import io.github.vivitoto.vanga.ui.dialogs.update.UpdateProgressDialog
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSwitchRow
import io.github.vivitoto.vanga.ui.settings.SettingsValueRow
import io.github.vivitoto.vanga.updates.AppRelease
import io.github.vivitoto.vanga.updates.AppVersion
import io.github.vivitoto.vanga.updates.UpdateProgress
import kotlin.time.Instant

@Composable
fun AppUpdatesContent(
    checkForUpdates: Boolean,
    onCheckForUpdatesChange: (Boolean) -> Unit,
    currentVersion: AppVersion,
    releases: List<AppRelease>,

    latestVersion: AppVersion?,
    lastChecked: Instant?,
    onCheckForUpdates: () -> Unit,
    versionCheckInProgress: Boolean,

    onUpdate: () -> Unit,
    onUpdateCancel: () -> Unit,
    downloadProgress: UpdateProgress?,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        SettingsSectionCard(
            title = "版本更新",
        ) {
            SettingsSwitchRow(
                title = "启动时检查更新",
                checked = checkForUpdates,
                onCheckedChange = onCheckForUpdatesChange,
            )
            VersionDetails(currentVersion, latestVersion, lastChecked, versionCheckInProgress)

            SettingsRow(
                title = "手动检查",
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilledTonalButton(
                            onClick = { onCheckForUpdates() },
                        ) { Text("检查") }

                        if (LocalPlatform.current != PlatformType.WEB_KOMF &&
                            latestVersion != null && currentVersion < latestVersion
                        ) {
                            FilledTonalButton(
                                onClick = { onUpdate() },
                            ) { Text("更新") }
                        }
                    }
                }
            )
        }

        val latestRelease = remember(releases, latestVersion) {
            latestVersion?.let { version -> releases.firstOrNull { it.version == version } }
        }
        if (latestRelease != null) {
            SettingsSectionCard(
                title = "最新版本更新说明",
            ) {
                ReleaseDetails(latestRelease)
            }
        }

        if (downloadProgress != null) {
            UpdateProgressDialog(
                totalSize = downloadProgress.total,
                downloadedSize = downloadProgress.completed,
                onCancel = onUpdateCancel

            )
        }
    }
}

@Composable
private fun ReleaseDetails(release: AppRelease) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(release.version.toString(), style = MaterialTheme.typography.titleMedium)
            val publishDate = remember {
                release.publishDate.toLocalDateTime(TimeZone.currentSystemDefault()).format(localDateFormat)
            }
            Text("发布日期：$publishDate", style = MaterialTheme.typography.labelLarge)
        }
        val state = rememberRichTextState()
        state.config.apply {
            linkColor = MaterialTheme.colorScheme.secondary
            linkTextDecoration = TextDecoration.Underline
            codeSpanBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
            codeSpanStrokeColor = MaterialTheme.colorScheme.surfaceVariant
        }
        remember { state.setMarkdown(release.releaseNotesBody) }
        RichText(state)
    }
}

@Composable
private fun VersionDetails(
    currentVersion: AppVersion,
    latestVersion: AppVersion?,
    lastChecked: Instant?,
    versionCheckInProgress: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SettingsValueRow(
            title = "当前版本",
            value = "$currentVersion",
        )

        val localDate = remember(lastChecked) {
            lastChecked?.toLocalDateTime(TimeZone.currentSystemDefault())?.format(localDateFormat)
        }
        SettingsRow(
            title = "最新版本",
            supportingText = localDate?.let { "检查时间：$it" } ?: "尚未完成版本检查。",
            trailing = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = latestVersion?.toString() ?: "未知",
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (versionCheckInProgress) {
                        AppCircularProgressIndicator(size = 20.dp, strokeWidth = 2.dp)
                    }
                }
            },
        )

    }
}
