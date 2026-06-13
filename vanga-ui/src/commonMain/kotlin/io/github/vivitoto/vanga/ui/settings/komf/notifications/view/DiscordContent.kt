package io.github.vivitoto.vanga.ui.settings.komf.notifications.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.ktor.http.*
import io.github.vivitoto.vanga.ui.LocalWindowWidth
import io.github.vivitoto.vanga.ui.StateHolder
import io.github.vivitoto.vanga.ui.common.components.CheckboxWithLabel
import io.github.vivitoto.vanga.ui.common.components.HttpTextField
import io.github.vivitoto.vanga.ui.common.components.SwitchWithLabel
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.platform.WindowSizeClass.COMPACT
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import io.github.vivitoto.vanga.ui.settings.komf.notifications.DiscordState.EmbedFieldState
import io.github.vivitoto.vanga.ui.settings.komf.notifications.NotificationContextState
import snd.komf.api.notifications.EmbedField
import kotlin.math.max

@Composable
fun DiscordNotificationsContent(
    discordUploadSeriesCover: StateHolder<Boolean>,
    discordWebhooks: List<String>,
    onDiscordWebhookAdd: (String) -> Unit,
    onDiscordWebhookRemove: (String) -> Unit,

    titleTemplate: StateHolder<String>,
    titleUrlTemplate: StateHolder<String>,
    descriptionTemplate: StateHolder<String>,
    fieldTemplates: List<EmbedFieldState>,
    onFieldAdd: () -> Unit,
    onFieldDelete: (EmbedFieldState) -> Unit,
    footerTemplate: StateHolder<String>,

    titlePreview: String,
    titleUrlPreview: String,
    descriptionPreview: String,
    fieldPreviews: List<EmbedField>,
    footerPreview: String,

    notificationContextState: NotificationContextState,
    onTemplateRender: () -> Unit,
    onTemplateSend: () -> Unit,
    onTemplateSave: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        Text("Webhook 列表")
        discordWebhooks.forEach { webhook ->
            Row {
                TextField(
                    value = webhook,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { onDiscordWebhookRemove(webhook) }, modifier = Modifier.cursorForHand()) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }

        var showAddWebhookDialog by remember { mutableStateOf(false) }

        FilledTonalButton(
            onClick = { showAddWebhookDialog = true },
            modifier = Modifier.cursorForHand()
        ) {
            Text("添加 Webhook")
        }

        SwitchWithLabel(
            checked = discordUploadSeriesCover.value,
            onCheckedChange = { discordUploadSeriesCover.setValue(it) },
            label = { Text("上传作品封面") }
        )

        if (showAddWebhookDialog) {
            AddDiscordWebhookDialog(
                onDismissRequest = { showAddWebhookDialog = false },
                onWebhookAdd = onDiscordWebhookAdd
            )
        }

        HorizontalDivider()

        TemplatesContent(
            titleTemplate = titleTemplate,
            titleUrlTemplate = titleUrlTemplate,
            descriptionTemplate = descriptionTemplate,
            fieldTemplates = fieldTemplates,
            onFieldAdd = onFieldAdd,
            onFieldDelete = onFieldDelete,
            footerTemplate = footerTemplate,

            titlePreview = titlePreview,
            titleUrlPreview = titleUrlPreview,
            descriptionPreview = descriptionPreview,
            fieldPreview = fieldPreviews,
            footerPreview = footerPreview,

            notificationContextState = notificationContextState,
            onTemplateSend = onTemplateSend,
            onTemplateSave = onTemplateSave,
            onTemplateRender = onTemplateRender
        )

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun AddDiscordWebhookDialog(
    onDismissRequest: () -> Unit,
    onWebhookAdd: (String) -> Unit,
) {
    var newWebhook by remember { mutableStateOf("") }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value

    val isValidUrl = derivedStateOf { parseUrl(newWebhook) != null }
    val isDiscordUrl = derivedStateOf { newWebhook.startsWith("https://discord.com/api/webhooks/") }
    val isError = derivedStateOf { newWebhook.isNotBlank() && (!isValidUrl.value || !isDiscordUrl.value) }

    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        onDismissRequest = onDismissRequest,
        header = {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("添加新的 Discord Webhook", style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()
            }
        },
        content = {
            Column(
                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = newWebhook,
                    onValueChange = { newWebhook = it },
                    label = { Text("Webhook URL") },
                    placeholder = { Text("https://discord.com/api/webhooks/...") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError.value,
                    interactionSource = interactionSource,
                    supportingText = { if (isError.value) Text("Webhook URL 无效") },
                    visualTransformation = if (isFocused) VisualTransformation.None else PasswordVisualTransformation(),
                )
            }
        },
        controlButtons = {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.cursorForHand(),
                    content = { Text("取消") }
                )

                FilledTonalButton(
                    onClick = {
                        onWebhookAdd(newWebhook)
                        onDismissRequest()
                    },
                    modifier = Modifier.cursorForHand(),
                    enabled = isValidUrl.value
                ) {
                    Text("确认")
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplatesContent(
    titleTemplate: StateHolder<String>,
    titleUrlTemplate: StateHolder<String>,
    descriptionTemplate: StateHolder<String>,
    fieldTemplates: List<EmbedFieldState>,
    onFieldAdd: () -> Unit,
    onFieldDelete: (EmbedFieldState) -> Unit,
    footerTemplate: StateHolder<String>,

    titlePreview: String,
    titleUrlPreview: String,
    descriptionPreview: String,
    fieldPreview: List<EmbedField>,
    footerPreview: String,

    notificationContextState: NotificationContextState,
    onTemplateSend: () -> Unit,
    onTemplateSave: () -> Unit,
    onTemplateRender: () -> Unit,
) {
    var showNotificationContextDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("通知模板", style = MaterialTheme.typography.titleLarge)
        Column {
            Text("使用 Markdown 语法。模板使用 Apache Velocity 渲染")
            Text(
                "Discord Markdown 入门",
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://support.discord.com/hc/en-us/articles/210298617-Markdown-Text-101-Chat-Formatting-Bold-Italic-Underline")
                }.padding(2.dp).cursorForHand()
            )
            Text(
                "Velocity 模板语言语法参考",
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://velocity.apache.org/engine/2.3/vtl-reference.html")
                }.padding(2.dp).cursorForHand()
            )
        }

        var selectedTab by remember { mutableStateOf(0) }
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
            ) {
                Text("编写")
            }
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    onTemplateRender()
                    selectedTab = 1
                },
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
            ) {
                Text("预览")
            }
        }

        Layout(content = {
            TemplatesEditor(
                titleTemplate = titleTemplate,
                titleUrlTemplate = titleUrlTemplate,
                descriptionTemplate = descriptionTemplate,
                fieldTemplates = fieldTemplates,
                onFieldAdd = onFieldAdd,
                onFieldDelete = onFieldDelete,
                footerTemplate = footerTemplate
            )
            if (selectedTab == 1) {
                Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxHeight()) {
                    Column {
                        TemplatesPreview(
                            titlePreview = titlePreview,
                            titleUrlPreview = titleUrlPreview,
                            descriptionPreview = descriptionPreview,
                            fieldPreview = fieldPreview,
                            footerPreview = footerPreview
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        ) { measurables, constraints ->
            val editor = measurables[0].measure(constraints.copy(minHeight = 0))
            val preview = measurables.getOrNull(1)?.measure(
                constraints.copy(minHeight = 0, maxHeight = editor.height)
            )
            val maxHeight = preview?.height?.let { max(editor.height, it) } ?: editor.height
            layout(constraints.maxWidth, maxHeight) {
                editor.placeRelative(0, 0)
                preview?.placeRelative(0, 0)
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            if (LocalWindowWidth.current != COMPACT) {
                Spacer(Modifier.weight(1f))
            }
            ElevatedButton(
                onClick = { showNotificationContextDialog = true },
                modifier = Modifier.cursorForHand()
            ) {
                Text("通知上下文")

            }

            ElevatedButton(
                onClick = onTemplateSend,
                modifier = Modifier.cursorForHand()
            ) {
                Text("测试发送")
            }

            FilledTonalButton(
                onClick = onTemplateSave,
                enabled = true,
                modifier = Modifier.cursorForHand()
            ) {
                Text("保存")
            }
        }
    }
    if (showNotificationContextDialog) {
        NotificationContextDialog(
            notificationContextState,
            onDismissRequest = {
                showNotificationContextDialog = false
                onTemplateRender()
            })
    }


}

@Composable
private fun TemplatesEditor(
    titleTemplate: StateHolder<String>,
    titleUrlTemplate: StateHolder<String>,
    descriptionTemplate: StateHolder<String>,
    fieldTemplates: List<EmbedFieldState>,
    onFieldAdd: () -> Unit,
    onFieldDelete: (EmbedFieldState) -> Unit,
    footerTemplate: StateHolder<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextField(
            value = titleTemplate.value,
            onValueChange = { titleTemplate.setValue(it) },
            label = { Text("标题，最多 256 个字符") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        HttpTextField(
            value = titleUrlTemplate.value,
            onValueChange = { titleUrlTemplate.setValue(it) },
            label = { Text("标题 URL") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = descriptionTemplate.value,
            onValueChange = { descriptionTemplate.setValue(it) },
            label = { Text("描述，最多 4096 个字符") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        TemplateFieldsEditor(fieldTemplates, onFieldAdd, onFieldDelete)
        TextField(
            value = footerTemplate.value,
            onValueChange = { footerTemplate.setValue(it) },
            label = { Text("页脚，最多 2048 个字符") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TemplateFieldsEditor(
    fieldTemplates: List<EmbedFieldState>,
    onFieldAdd: () -> Unit,
    onFieldDelete: (EmbedFieldState) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        fieldTemplates.forEachIndexed { index, field ->
            var showField by remember { mutableStateOf(false) }
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showField = !showField }.cursorForHand()

                ) {
                    Icon(if (showField) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    Text("字段 ${index + 1}")
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onFieldDelete(field) }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
                AnimatedVisibility(
                    visible = showField,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    TemplateFieldEditor(field)
                }
                HorizontalDivider()
            }

        }

        FilledTonalButton(
            onClick = onFieldAdd,
            enabled = fieldTemplates.size < 25,
            modifier = Modifier.cursorForHand()
        ) {
            Text("添加字段")
        }
    }

}

@Composable
private fun TemplateFieldEditor(
    state: EmbedFieldState
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = state.nameTemplate,
                onValueChange = { state.nameTemplate = it },
                label = { Text("字段名称，最多 256 个字符") },
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            CheckboxWithLabel(
                checked = state.inline,
                onCheckedChange = { state.inline = it },
                label = { Text("行内显示") })
        }
        TextField(
            value = state.valueTemplate,
            onValueChange = { state.valueTemplate = it },
            label = { Text("字段值，最多 1024 个字符") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
    }

}

@Composable
private fun TemplatesPreview(
    titlePreview: String,
    titleUrlPreview: String,
    descriptionPreview: String,
    fieldPreview: List<EmbedField>,
    footerPreview: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Layout(content = {
            PreviewContent(
                titlePreview = titlePreview,
                titleUrlPreview = titleUrlPreview,
                descriptionPreview = descriptionPreview,
                fieldPreview = fieldPreview,
                footerPreview = footerPreview
            )
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            )

        }) { measurables, constraints ->
            val preview = measurables[0].measure(constraints)
            val colorSpacer = measurables[1].measure(constraints.copy(maxHeight = preview.height))

            layout(constraints.maxWidth, preview.height) {
                colorSpacer.placeRelative(0, 0)
                preview.placeRelative(colorSpacer.width, 0)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewContent(
    titlePreview: String,
    titleUrlPreview: String,
    descriptionPreview: String,
    fieldPreview: List<EmbedField>,
    footerPreview: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val linkColor = MaterialTheme.colorScheme.secondary
        if (titlePreview.isNotBlank()) {
            val titleState = remember(titlePreview, titleUrlPreview) {

                RichTextState().apply {
                    config.linkColor = linkColor
                    config.linkTextDecoration = TextDecoration.Underline
                    setMarkdown(if (titleUrlPreview.isNotBlank()) "[$titlePreview]($titleUrlPreview)" else titlePreview)
                }
            }
            RichText(
                state = titleState,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (descriptionPreview.isNotBlank()) {
            val contentState =
                remember(descriptionPreview) {
                    RichTextState().apply {
                        config.linkColor = linkColor
                        config.linkTextDecoration = TextDecoration.Underline
                        setMarkdown(descriptionPreview)
                    }
                }
            RichText(
                state = contentState,
            )
        }
        FlowRow(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            fieldPreview.forEach { field ->
                Column(
                    modifier = Modifier.widthIn(min = 200.dp).then(
                        if (!field.inline)
                            Modifier.fillMaxWidth() else Modifier.weight(1f)
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val nameState = remember(field.name) {
                        RichTextState().apply {
                            config.linkColor = linkColor
                            config.linkTextDecoration = TextDecoration.Underline
                            setMarkdown(field.name)
                        }
                    }
                    RichText(
                        state = nameState,
                        fontWeight = FontWeight.Bold
                    )
                    val valueState = remember(field.value) {
                        RichTextState().apply {
                            config.linkColor = linkColor
                            config.linkTextDecoration = TextDecoration.Underline
                            setMarkdown(field.value)
                        }
                    }
                    RichText(valueState)
                }

            }
        }

        if (footerPreview.isNotBlank()) {
            Text(footerPreview, style = MaterialTheme.typography.bodySmall)
        }
    }
}


