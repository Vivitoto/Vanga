package io.github.vivitoto.vanga.ui.dialogs.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.LoadState
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.common.components.PasswordTextField
import io.github.vivitoto.vanga.ui.common.components.withTextFieldNavigation
import io.github.vivitoto.vanga.ui.dialogs.AppDialog
import io.github.vivitoto.vanga.ui.settings.SettingsCheckboxRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard

@Composable
fun UserAddDialog(
    onDismiss: () -> Unit,
    afterConfirm: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getUserAddDialogViewModel() }
    if (vm.state.collectAsState().value is LoadState.Success) {
        onDismiss()
    }
    UserAddDialog(
        email = vm.email,
        emailValidation = vm.emailValidationError,
        onEmailChange = vm::onEmailChange,
        password = vm.password,
        passwordValidation = vm.passwordValidationError,
        onPasswordChange = vm::onPasswordChange,
        administratorRole = vm.administratorRole,
        onAdministratorRoleChange = vm::administratorRole::set,
        pageStreamingRole = vm.pageStreamingRole,
        onPageStreamingRoleChange = vm::pageStreamingRole::set,
        fileDownloadRole = vm.fileDownloadRole,
        onFileDownloadRoleChange = vm::fileDownloadRole::set,

        isValid = vm.isValid,

        onUserAdd = vm::addUser,
        afterConfirm = afterConfirm,
        onDismissRequest = onDismiss,
    )
}

@Composable
fun UserAddDialog(
    email: String,
    emailValidation: String?,
    onEmailChange: (String) -> Unit,
    password: String,
    passwordValidation: String?,
    onPasswordChange: (String) -> Unit,

    administratorRole: Boolean,
    onAdministratorRoleChange: (Boolean) -> Unit,
    pageStreamingRole: Boolean,
    onPageStreamingRoleChange: (Boolean) -> Unit,
    fileDownloadRole: Boolean,
    onFileDownloadRoleChange: (Boolean) -> Unit,

    isValid: Boolean,

    onUserAdd: suspend () -> Unit,
    afterConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AppDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Text(
                text = "添加用户",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
            )
        },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.padding(20.dp)
            ) {

                TextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("邮箱") },
                    supportingText = {
                        if (emailValidation != null)
                            Text(text = emailValidation, color = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.fillMaxWidth().withTextFieldNavigation()
                )

                PasswordTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text("密码") },
                    isError = passwordValidation != null,
                    supportingText = { passwordValidation?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                SettingsSectionCard("角色") {
                    SettingsCheckboxRow(
                        title = "管理员",
                        checked = administratorRole,
                        onCheckedChange = onAdministratorRoleChange,
                    )

                    SettingsCheckboxRow(
                        title = "页面流式传输",
                        checked = pageStreamingRole,
                        onCheckedChange = onPageStreamingRoleChange,
                    )

                    SettingsCheckboxRow(
                        title = "文件下载",
                        checked = fileDownloadRole,
                        onCheckedChange = onFileDownloadRoleChange,
                    )
                }
            }
        },

        controlButtons = {
            val coroutineScope = rememberCoroutineScope()
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(10.dp),
            ) {
                ElevatedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text("取消")
                }

                FilledTonalButton(
                    onClick = {
                        coroutineScope.launch {
                            onUserAdd()
                            afterConfirm()
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                ) {
                    Text("添加")
                }
            }
        }
    )
}
