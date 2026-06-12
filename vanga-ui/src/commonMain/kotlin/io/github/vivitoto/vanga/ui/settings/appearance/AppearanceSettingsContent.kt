package io.github.vivitoto.vanga.ui.settings.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.settings.model.AppTheme
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.common.components.AppSliderDefaults
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import kotlin.math.roundToInt

@Composable
fun AppearanceSettingsContent(
    cardWidth: Dp,
    onCardWidthChange: (Dp) -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val strings = LocalStrings.current.settings

        Text("主题", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Text("调整 Vanga 的明暗模式。", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)

        DropdownChoiceMenu(
            label = { Text("主题模式") },
            selectedOption = LabeledEntry(currentTheme, strings.forAppTheme(currentTheme)),
            options = AppTheme.entries.map { LabeledEntry(it, strings.forAppTheme(it)) },
            onOptionChange = { onThemeChange(it.value) },
            inputFieldModifier = Modifier.widthIn(min = 250.dp)
        )

        HorizontalDivider()

        Text("封面卡片", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Text("控制首页、书库和搜索结果里的封面大小。", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        Text("封面宽度：${cardWidth.value.roundToInt()} dp", modifier = Modifier.padding(top = 10.dp))
        Slider(
            value = cardWidth.value,
            onValueChange = { onCardWidthChange(it.roundToInt().dp) },
            steps = 19,
            valueRange = 150f..350f,
            colors = AppSliderDefaults.colors(),
            modifier = Modifier.cursorForHand().padding(end = 20.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 400.dp, max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                Modifier
                    .width(cardWidth)
                    .aspectRatio(0.703f)
            ) {

            }


        }

    }

}
