package io.github.vivitoto.vanga.ui.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorContent(
    exception: Throwable,
    onReload: (() -> Unit)? = null,
    onExit: (() -> Unit)? = null,
) {
    val messageString = remember(exception) {
        exception.message?.let { message -> "${exception::class.simpleName} $message" }
            ?: exception::class.simpleName ?: "未知错误"
    }
    ErrorContent(messageString, onReload, onExit)
}

@Composable
fun ErrorContent(
    message: String,
    onReload: (() -> Unit)? = null,
    onExit: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .78f),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "页面加载失败",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(10.dp))
                SelectionContainer {
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(22.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (onReload != null) {
                        FilledTonalButton(onClick = onReload) {
                            Text("重新加载")
                        }
                    }

                    if (onExit != null) {
                        FilledTonalButton(onClick = onExit) {
                            Text("退出")
                        }
                    }
                }
            }
        }
    }
}
