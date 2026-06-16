package io.github.vivitoto.vanga.ui.reader.image.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.InputChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.settings.model.ReaderFlashColor
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.common.components.AppSliderDefaults
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.settings.SettingsRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import io.github.vivitoto.vanga.ui.settings.SettingsSwitchRow
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommonImageSettings(
    stretchToFit: Boolean,
    onStretchToFitChange: (Boolean) -> Unit,
    cropBorders: Boolean,
    onCropBordersChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val readerStrings = strings.reader
    val platform = LocalPlatform.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsSectionCard(title = "图片显示") {
            SettingsSwitchRow(
                title = readerStrings.stretchToFit,
                checked = stretchToFit,
                onCheckedChange = onStretchToFitChange,
            )

            if (platform != PlatformType.WEB_KOMF) {
                SettingsSwitchRow(
                    title = "裁切边框",
                    checked = cropBorders,
                    onCheckedChange = onCropBordersChange,
                )
            }
        }
    }
}

@Composable
fun ImageFlashSettings(
    flashEnabled: Boolean,
    onFlashEnabledChange: (Boolean) -> Unit,
    flashEveryNPages: Int,
    onFlashEveryNPagesChange: (Int) -> Unit,
    flashWith: ReaderFlashColor,
    onFlashWithChange: (ReaderFlashColor) -> Unit,
    flashDuration: Long,
    onFlashDurationChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalPlatform.current == PlatformType.DESKTOP) return

    SettingsSectionCard(
        title = "高级设置",
        modifier = modifier,
    ) {
        SettingsSwitchRow(
            title = "翻页闪屏",
            checked = flashEnabled,
            onCheckedChange = onFlashEnabledChange,
        )
        AnimatedVisibility(flashEnabled) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SettingsRow(
                    title = "闪屏时长",
                    supportingText = "$flashDuration ms",
                    stackTrailing = true,
                    trailing = {
                        Slider(
                            value = flashDuration.toFloat(),
                            onValueChange = { onFlashDurationChange(it.roundToLong()) },
                            modifier = Modifier.fillMaxWidth(),
                            steps = 13,
                            valueRange = 100f..1500f,
                            colors = AppSliderDefaults.colors()
                        )
                    }
                )

                val pagesText = remember(flashEveryNPages) {
                    "$flashEveryNPages 页"
                }
                SettingsRow(
                    title = "闪屏间隔",
                    supportingText = pagesText,
                    stackTrailing = true,
                    trailing = {
                        Slider(
                            value = flashEveryNPages.toFloat(),
                            onValueChange = { onFlashEveryNPagesChange(it.roundToInt()) },
                            modifier = Modifier.fillMaxWidth(),
                            steps = 10,
                            valueRange = 1f..10f,
                            colors = AppSliderDefaults.colors()
                        )
                    }
                )

                SettingsRow(
                    title = "闪屏颜色",
                    stackTrailing = true,
                    trailing = {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            InputChip(
                                selected = flashWith == ReaderFlashColor.BLACK,
                                onClick = { onFlashWithChange(ReaderFlashColor.BLACK) },
                                label = { Text("黑色") }
                            )
                            InputChip(
                                selected = flashWith == ReaderFlashColor.WHITE,
                                onClick = { onFlashWithChange(ReaderFlashColor.WHITE) },
                                label = { Text("白色") }
                            )
                            InputChip(
                                selected = flashWith == ReaderFlashColor.WHITE_AND_BLACK,
                                onClick = { onFlashWithChange(ReaderFlashColor.WHITE_AND_BLACK) },
                                label = { Text("黑白交替") }
                            )
                        }
                    }
                )
            }
        }
    }
}
