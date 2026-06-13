package io.github.vivitoto.vanga.ui.settings.imagereader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.common.components.SwitchWithLabel
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.settings.SettingsSectionHeader

@Composable
fun ImageReaderSettingsContent(
    loadThumbnailPreviews: Boolean,
    onLoadThumbnailPreviewsChange: (Boolean) -> Unit,

    volumeKeysNavigation: Boolean,
    onVolumeKeysNavigationChange: (Boolean) -> Unit,

    onCacheClear: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val platform = LocalPlatform.current
        SettingsSectionHeader(
            title = "翻页体验",
            description = "这里放阅读时最常用、最容易感知的设置。",
        )

        SwitchWithLabel(
            checked = loadThumbnailPreviews,
            onCheckedChange = onLoadThumbnailPreviewsChange,
            label = { Text("拖动进度条时显示页面预览") },
            supportingText = { Text("高分辨率图片可能会变慢") },
        )

        if (platform == PlatformType.MOBILE) {
            SwitchWithLabel(
                checked = volumeKeysNavigation,
                onCheckedChange = onVolumeKeysNavigationChange,
                label = { Text("用音量键翻页") },
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 10.dp))
        SettingsSectionHeader(
            title = "缓存",
            description = "图片缓存可以加快重复打开速度；空间紧张时再清理。",
        )

        FilledTonalButton(
            onClick = onCacheClear,
        ) { Text("清理图片缓存") }
    }
}
