package io.github.vivitoto.vanga.ui.settings.komf

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.common.components.HttpTextField
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.common.components.PasswordTextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavableTextField(
    currentValue: String,
    onValueSave: (String) -> Unit,
    label: @Composable () -> Unit,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    valueChangePolicy: (String) -> Boolean = { true },
    useEditButton: Boolean = false,
    isPassword: Boolean = false,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        var isChanged by remember { mutableStateOf(false) }
        var editable by remember { mutableStateOf(!useEditButton) }
        var textFieldValue by remember(currentValue) { mutableStateOf(currentValue) }

        if (isPassword) {
            PasswordTextField(
                value = textFieldValue,
                enabled = editable,
                onValueChange = {
                    if (valueChangePolicy(it)) {
                        textFieldValue = it
                        isChanged = true
                    }
                },
                supportingText = supportingText,
                isError = isError,
                label = label,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp).animateContentSize()
            )
        } else {
            TextField(
                value = textFieldValue,
                enabled = editable,
                onValueChange = {
                    if (valueChangePolicy(it)) {
                        textFieldValue = it
                        isChanged = true
                    }
                },
                label = label,
                supportingText = supportingText,
                isError = isError,
                singleLine = true,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp).animateContentSize()
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            if (!editable) {
                ElevatedButton(
                    onClick = {
                        editable = true
                        textFieldValue = ""
                    },
                ) {
                    Text("编辑")
                }
            }

            AnimatedVisibility(editable) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ElevatedButton(
                        enabled = useEditButton || isChanged,
                        onClick = {
                            if (useEditButton) editable = false
                            isChanged = false
                            textFieldValue = currentValue
                        },
                    ) {
                        Text("放弃")
                    }
                    FilledTonalButton(
                        onClick = {
                            onValueSave(textFieldValue)
                            if (useEditButton) editable = false
                            isChanged = false
                        },
                        enabled = isChanged,
                    ) {
                        Text("保存")
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavableHttpTextField(
    label: String,
    currentValue: String,
    onValueSave: (String) -> Unit,
    confirmationText: String = "保存",
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        var isChanged by remember { mutableStateOf(false) }
        var textFieldValue by remember(currentValue) { mutableStateOf(currentValue) }
        HttpTextField(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                isChanged = true
            },
            label = { Text(label) },
            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
            isError = isError,
            supportingText = supportingText
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ElevatedButton(
                enabled = isChanged,
                onClick = {
                    isChanged = false
                    textFieldValue = currentValue
                },
            ) {
                Text("放弃")
            }
            FilledTonalButton(
                onClick = {
                    onValueSave(textFieldValue)
                    isChanged = false
                },
                enabled = isChanged || isError,
            ) {
                Text(confirmationText)
            }
        }
    }

}

val komfLanguageTagsSuggestions = listOf(
    LabeledEntry("en", "英语 (en)"),
    LabeledEntry("ja", "日语 (ja)"),
    LabeledEntry("ja-ro", "日语罗马字 (ja-ro)"),
    LabeledEntry("ko", "韩语 (ko)"),
    LabeledEntry("ko-ro", "韩语罗马字 (ko-ro)"),
    LabeledEntry("zh", "简体中文 (zh)"),
    LabeledEntry("zh-hk", "繁体中文 (zh-hk)"),
    LabeledEntry("zh-ro", "中文罗马字 (zh-ro)"),
    LabeledEntry("pt-br", "巴西葡萄牙语 (pt-br)"),
    LabeledEntry("es", "卡斯蒂利亚西班牙语 (es)"),
    LabeledEntry("es-la", "拉美西班牙语 (es-la)"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionField(
    label: String,
    languageValue: String,
    onLanguageValueChange: (String) -> Unit,
    onLanguageValueSave: () -> Unit,
) {
    var isChanged by remember { mutableStateOf(false) }
    val suggestedOptions = derivedStateOf {
        komfLanguageTagsSuggestions.filter { (_, label) -> label.lowercase().contains(languageValue.lowercase()) }
    }
    val focusManager = LocalFocusManager.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        var isExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = languageValue,
                onValueChange = {
                    onLanguageValueChange(it)
                    isChanged = true
                },
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(PrimaryEditable)
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                suggestedOptions.value.forEach {
                    DropdownMenuItem(
                        text = { Text(it.label) },
                        onClick = {
                            isExpanded = false
                            focusManager.clearFocus()
                            onLanguageValueChange(it.value)
                            onLanguageValueSave()
                            isChanged = false
                        }
                    )
                }

            }
        }
        FilledTonalButton(
            onClick = {
                onLanguageValueSave()
                isChanged = false
            },
            enabled = isChanged,
        ) {
            Text("保存")
        }
    }
}
