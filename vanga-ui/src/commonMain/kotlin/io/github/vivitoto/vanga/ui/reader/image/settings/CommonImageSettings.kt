package io.github.vivitoto.vanga.ui.reader.image.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.settings.model.ReaderFlashColor
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.common.components.AppSliderDefaults
import io.github.vivitoto.vanga.ui.common.components.SwitchWithLabel
import io.github.vivitoto.vanga.ui.platform.PlatformType
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommonImageSettings(
    stretchToFit: Boolean,
    onStretchToFitChange: (Boolean) -> Unit,
    cropBorders: Boolean,
    onCropBordersChange: (Boolean) -> Unit,

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
    val strings = LocalStrings.current
    val readerStrings = strings.reader
    val platform = LocalPlatform.current
    Column(modifier = modifier) {
        SwitchWithLabel(
            checked = stretchToFit,
            onCheckedChange = onStretchToFitChange,
            label = { Text(readerStrings.stretchToFit) },
            contentPadding = PaddingValues(horizontal = 10.dp)
        )

        if (LocalPlatform.current != PlatformType.WEB_KOMF) {
            SwitchWithLabel(
                checked = cropBorders,
                onCheckedChange = onCropBordersChange,
                label = { Text("裁切边框") },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
        }

        if (platform != PlatformType.DESKTOP) {
            SwitchWithLabel(
                checked = flashEnabled,
                onCheckedChange = onFlashEnabledChange,
                label = { Text("翻页时闪屏") },
                supportingText = { Text("用于减少墨水屏残影") },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
            AnimatedVisibility(flashEnabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(100.dp)) {
                            Text("闪屏时长", style = MaterialTheme.typography.labelLarge)
                            Text("$flashDuration ms", style = MaterialTheme.typography.labelMedium)
                        }
                        Slider(
                            value = flashDuration.toFloat(),
                            onValueChange = { onFlashDurationChange(it.roundToLong()) },
                            steps = 13,
                            valueRange = 100f..1500f,
                            colors = AppSliderDefaults.colors()
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(100.dp)) {
                            Text("闪屏间隔", style = MaterialTheme.typography.labelLarge)
                            val pagesText = remember(flashEveryNPages) {
                                "$flashEveryNPages 页"
                            }
                            Text(pagesText, style = MaterialTheme.typography.labelMedium)
                        }
                        Slider(
                            value = flashEveryNPages.toFloat(),
                            onValueChange = { onFlashEveryNPagesChange(it.roundToInt()) },
                            steps = 10,
                            valueRange = 1f..10f,
                            colors = AppSliderDefaults.colors()
                        )
                    }

                    Column {
                        Text("闪屏颜色")
                        FlowRow(
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
                }
            }
        }
    }
}
