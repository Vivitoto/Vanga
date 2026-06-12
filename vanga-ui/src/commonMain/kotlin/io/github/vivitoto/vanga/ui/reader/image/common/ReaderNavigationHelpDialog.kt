package io.github.vivitoto.vanga.ui.reader.image.common

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.settings.model.ContinuousReadingDirection
import io.github.vivitoto.vanga.settings.model.ContinuousReadingDirection.LEFT_TO_RIGHT
import io.github.vivitoto.vanga.settings.model.ContinuousReadingDirection.RIGHT_TO_LEFT
import io.github.vivitoto.vanga.settings.model.ContinuousReadingDirection.TOP_TO_BOTTOM
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.EXPANDED
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.FULL
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.MEDIUM

@Composable
fun PagedReaderHelpDialog(
    onDismissRequest: () -> Unit,
) {
    AppDialog(
        modifier = Modifier.fillMaxWidth(.9f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        content = {
            when (LocalWindowWidth.current) {
                COMPACT, MEDIUM, EXPANDED -> Column { PagedDialogContent() }
                FULL -> Row { PagedDialogContent(Modifier.weight(1f)) }
            }
        }
    )
}

@Composable
fun ContinuousReaderHelpDialog(
    readingDirection: ContinuousReadingDirection,
    onDismissRequest: () -> Unit,
) {
    val orientation = remember(readingDirection) {
        when (readingDirection) {
            TOP_TO_BOTTOM -> Vertical
            LEFT_TO_RIGHT, RIGHT_TO_LEFT -> Horizontal
        }
    }
    AppDialog(
        modifier = Modifier.fillMaxWidth(.9f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        content = {
            when (LocalWindowWidth.current) {
                COMPACT, MEDIUM, EXPANDED -> Column { ContinuousDialogContent(orientation) }
                FULL -> Row { ContinuousDialogContent(orientation, Modifier.weight(1f)) }
            }
        }
    )
}

@Composable
private fun PagedDialogContent(
    elementsModifier: Modifier = Modifier,
) {
    val platform = LocalPlatform.current
    KeyDescriptionColumn(
        "阅读导航",
        mapOf(
            listOf("←") to "上一页",
            listOf("→") to "下一页",
            listOf("Home") to "第一页",
            listOf("End") to "最后一页",
            if (platform == PlatformType.WEB_KOMF) {
                listOf("Shift", "滚轮") to "缩放"
            } else {
                listOf("Ctrl", "滚轮") to "缩放"
            }
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "触控 / 鼠标",
        mapOf(
            listOf("点左/右") to "上一页/下一页",
            listOf("左右滑动") to "上一页/下一页",
            listOf("左边缘右滑") to "返回详情页",
            listOf("点中间") to "显示/隐藏菜单",
            listOf("拖动") to "缩放后移动画面",
            listOf("双指捏合") to "围绕手指缩放",
            listOf("滚轮") to "翻页或滚动内容"
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "阅读设置",
        mapOf(
            listOf("L") to "从左到右",
            listOf("R") to "从右到左",
            listOf("C") to "切换缩放模式",
            listOf("D") to "切换页面布局",
            listOf("O") to "切换双页偏移",
            listOf("U") to "切换图片拉伸适配",
            listOf("F11") to "进入/退出全屏"
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "菜单",
        mapOf(
            listOf("M") to "显示/隐藏菜单",
            listOf("H") to "显示/隐藏帮助",
            listOf("ALT", "←") to "返回系列页面"
        ),
        elementsModifier
    )
}

@Composable
private fun ContinuousDialogContent(
    orientation: Orientation,
    elementsModifier: Modifier = Modifier,
) {
    val platform = LocalPlatform.current
    val scrollDirection = when (orientation) {
        Vertical -> mapOf(
            listOf("↑") to "向上滚动",
            listOf("↓") to "向下滚动",
        )

        Horizontal -> mapOf(
            listOf("←") to "向左滚动",
            listOf("→") to "向右滚动",
        )
    }
    KeyDescriptionColumn(
        "阅读导航",
        scrollDirection + mapOf(
            listOf("Home") to "第一页",
            listOf("End") to "最后一页",
            if (platform == PlatformType.WEB_KOMF) {
                listOf("Shift", "滚轮") to "缩放"
            } else {
                listOf("Ctrl", "滚轮") to "缩放"
            }
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "触控 / 鼠标",
        mapOf(
            listOf("点左/右") to "前进/后退滚动",
            listOf("左右滑动") to "前进/后退滚动",
            listOf("左边缘右滑") to "返回详情页",
            listOf("点中间") to "显示/隐藏菜单",
            listOf("拖动") to "缩放后移动画面",
            listOf("双指捏合") to "围绕手指缩放",
            listOf("滚轮") to "滚动内容"
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "阅读设置",
        mapOf(
            listOf("V") to "从上到下",
            listOf("L") to "从左到右",
            listOf("R") to "从右到左",
            listOf("U") to "切换图片拉伸适配",
            listOf("F11") to "进入/退出全屏"
        ),
        elementsModifier
    )

    KeyDescriptionColumn(
        "菜单",
        mapOf(
            listOf("M") to "显示/隐藏菜单",
            listOf("H") to "显示/隐藏帮助",
            listOf("ALT", "←") to "返回系列页面"
        ),
        elementsModifier
    )
}

@Composable
private fun KeyDescriptionColumn(
    title: String,
    keyToDescription: Map<List<String>, String>,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Row {
            Text(
                text = "操作",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "说明",
                modifier = Modifier.weight(1f)
            )
        }

        keyToDescription.forEach { (keys, description) ->
            HorizontalDivider()
            Row {

                ShortcutKeys(keys, Modifier.weight(1f))
                Text(description, Modifier.weight(1f))
            }
        }

    }
}

@Composable
private fun ShortcutKeys(keys: List<String>, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ShortcutKey(keys.first())
        keys.drop(1).forEach { key ->
            Text(" + ")
            ShortcutKey(key)
        }
    }
}

@Composable
private fun ShortcutKey(label: String) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontWeight = FontWeight.Bold
        )
    }
}
