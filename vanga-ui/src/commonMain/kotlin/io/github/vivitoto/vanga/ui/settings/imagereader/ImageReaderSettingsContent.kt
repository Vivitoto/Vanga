package io.github.vivitoto.vanga.ui.settings.imagereader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSwitchRow

@Composable
fun ImageReaderSettingsContent(
    loadThumbnailPreviews: Boolean,
    onLoadThumbnailPreviewsChange: (Boolean) -> Unit,

    volumeKeysNavigation: Boolean,
    onVolumeKeysNavigationChange: (Boolean) -> Unit,

    onCacheClear: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val platform = LocalPlatform.current
        SettingsSectionCard(
            title = "翻页体验",
        ) {
            SettingsSwitchRow(
                title = "拖动进度条时显示页面预览",
                supportingText = "高分辨率图片可能会变慢。",
                checked = loadThumbnailPreviews,
                onCheckedChange = onLoadThumbnailPreviewsChange,
            )

            if (platform == PlatformType.MOBILE) {
                SettingsSwitchRow(
                    title = "用音量键翻页",
                    checked = volumeKeysNavigation,
                    onCheckedChange = onVolumeKeysNavigationChange,
                )
            }
        }

        SettingsSectionCard(
            title = "缓存",
        ) {
            SettingsRow(
                title = "图片缓存",
                supportingText = "清理后不会删除书籍，只会释放本机缓存空间。",
                trailing = {
                    FilledTonalButton(
                        onClick = onCacheClear,
                    ) {
                        Text("清理")
                    }
                }
            )
        }
    }
}
