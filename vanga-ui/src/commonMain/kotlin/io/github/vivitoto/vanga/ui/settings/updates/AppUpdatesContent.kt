package io.github.vivitoto.vanga.ui.settings.updates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import io.github.vivitoto.vanga.ui.common.components.SwitchWithLabel
import io.github.vivitoto.vanga.ui.dialogs.update.UpdateProgressDialog
import io.github.vivitoto.vanga.ui.platform.PlatformType
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        SwitchWithLabel(
            checked = checkForUpdates,
            onCheckedChange = onCheckForUpdatesChange,
            label = { Text("启动时检查更新") }
        )
        HorizontalDivider(Modifier.padding(bottom = 20.dp))
        VersionDetails(currentVersion, latestVersion, lastChecked, versionCheckInProgress)

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Bottom,
        ) {

            FilledTonalButton(
                onClick = { onCheckForUpdates() },
            ) { Text("检查更新") }

            if (LocalPlatform.current != PlatformType.WEB_KOMF &&
                latestVersion != null && currentVersion < latestVersion
            ) {
                FilledTonalButton(
                    onClick = { onUpdate() },
                ) { Text("更新") }
            }
        }

        if (releases.isNotEmpty()) {
            HorizontalDivider(Modifier.padding(vertical = 20.dp))
            Text("更新说明", style = MaterialTheme.typography.headlineMedium)
            releases.forEach {
                ReleaseDetails(it)
                HorizontalDivider()
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
            Text(release.version.toString(), style = MaterialTheme.typography.headlineMedium)
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
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("当前版本：", modifier = Modifier.widthIn(min = 200.dp))
            Text("$currentVersion")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (latestVersion != null) {
                Text("最新版本：", modifier = Modifier.widthIn(200.dp))
                Text("$latestVersion")

                if (lastChecked != null) {
                    lastChecked.toString()
                    val localDate = remember(lastChecked) {
                        lastChecked.toLocalDateTime(TimeZone.currentSystemDefault()).format(localDateFormat)
                    }
                    Text(
                        "检查时间：$localDate",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                if (versionCheckInProgress) {
                    AppCircularProgressIndicator(size = 20.dp, strokeWidth = 2.dp)
                }
            }
        }

    }
}
