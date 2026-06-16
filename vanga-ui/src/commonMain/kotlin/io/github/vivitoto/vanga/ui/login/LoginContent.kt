package io.github.vivitoto.vanga.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component3
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.VangaShape
import io.github.vivitoto.vanga.ui.LocalPlatform
import io.github.vivitoto.vanga.ui.common.components.AppCircularProgressIndicator
import io.github.vivitoto.vanga.ui.common.components.OutlinedHttpTextField
import io.github.vivitoto.vanga.ui.common.components.withTextFieldNavigation
import io.github.vivitoto.vanga.ui.platform.PlatformType
import io.github.vivitoto.vanga.ui.platform.PlatformType.DESKTOP
import io.github.vivitoto.vanga.ui.platform.PlatformType.MOBILE
import io.github.vivitoto.vanga.ui.platform.cursorForHand


@Composable
fun LoginContent(
    url: String,
    onUrlChange: (String) -> Unit,
    user: String,
    onUserChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    userLoginError: String?,
    autoLoginError: String?,
    onAutoLoginRetry: () -> Unit,
    onLogin: () -> Unit,
    offlineIsAvailable: Boolean,
    onOfflineSelect: () -> Unit,
    canGoOfflineAsCurrentUser: Boolean,
    goOfflineAsCurrentUser: () -> Unit,
) {

    var showAutoLoginError by remember { mutableStateOf(true) }
    if (autoLoginError != null && showAutoLoginError) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                autoLoginError,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(onClick = { showAutoLoginError = false }) { Text("使用其他账号登录") }
                if (canGoOfflineAsCurrentUser) {
                    Button(onClick = goOfflineAsCurrentUser) { Text("进入离线模式") }
                }

                Button(onClick = onAutoLoginRetry) { Text("重试") }
            }
        }
    } else {
        val platform = LocalPlatform.current
        when (platform) {
            MOBILE, DESKTOP -> Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 460.dp)
                        .padding(horizontal = 24.dp),
                    shape = VangaShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f),
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        VangaLoginMark()
                        Text("登录 Vanga", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "连接你的 Komga 服务器，继续阅读和管理漫画库。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        LoginForm(
                            url = url,
                            onUrlChange = onUrlChange,
                            user = user,
                            onUserChange = onUserChange,
                            password = password,
                            onPasswordChange = onPasswordChange,
                            errorMessage = userLoginError,
                            onLogin = onLogin,
                            offlineIsAvailable = offlineIsAvailable,
                            onOfflineSelect = onOfflineSelect,
                            textFieldsModifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            PlatformType.WEB_KOMF -> Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val uriHandler = LocalUriHandler.current
                Column {
                    Text("Vanga Web 阅读客户端")
                    Text(
                        "需要在 Komga CORS 配置中允许当前域名和端口",
                        color = MaterialTheme.colorScheme.secondary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://komga.org/docs/installation/configuration/#komga_cors_allowed_origins--komgacorsallowed-origins-origins")
                        }.padding(2.dp).cursorForHand()
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LoginForm(
                        url = url,
                        onUrlChange = onUrlChange,
                        user = user,
                        onUserChange = onUserChange,
                        password = password,
                        onPasswordChange = onPasswordChange,
                        errorMessage = userLoginError,
                        onLogin = onLogin,
                        offlineIsAvailable = offlineIsAvailable,
                        onOfflineSelect = onOfflineSelect,
                        textFieldsModifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

    }

}

@Composable
private fun VangaLoginMark() {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "V",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
fun ColumnScope.LoginForm(
    url: String,
    onUrlChange: (String) -> Unit,
    user: String,
    onUserChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    errorMessage: String?,
    onLogin: () -> Unit,
    offlineIsAvailable: Boolean,
    onOfflineSelect: () -> Unit,
    textFieldsModifier: Modifier
) {

    val coroutineScope = rememberCoroutineScope()
    val (first, second, third) = remember { FocusRequester.createRefs() }

    OutlinedHttpTextField(
        value = url,
        onValueChange = onUrlChange,
        label = { Text("Komga 服务器地址") },
        modifier = textFieldsModifier
            .withTextFieldNavigation()
            .focusRequester(first)
            .focusProperties { next = second },
        placeholder = { Text("komga.example.com") },
        singleLine = true,
    )

    OutlinedTextField(
        value = user,
        onValueChange = onUserChange,
        label = { Text("Komga 用户名或邮箱") },
        modifier = textFieldsModifier
            .withTextFieldNavigation()
            .focusRequester(second)
            .focusProperties { next = third },
        placeholder = { Text("admin@example.org") },
        singleLine = true,
    )

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        visualTransformation = PasswordVisualTransformation(),
        label = { Text("密码") },
        placeholder = { Text("••••••••") },
        modifier = textFieldsModifier
            .withTextFieldNavigation(
                onEnterPress = { coroutineScope.launch { onLogin() } }
            )
            .focusRequester(third),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
    )

    if (errorMessage != null) {
        Text(errorMessage, style = TextStyle(color = MaterialTheme.colorScheme.error))
    }

    Row(
        modifier = textFieldsModifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (offlineIsAvailable) {
            TextButton(onClick = onOfflineSelect) { Text("离线阅读") }
        }
        Button(onClick = { onLogin() }) { Text("登录 Komga") }
    }

    Spacer(Modifier.imePadding())
}

@Composable
fun LoginLoadingContent(onCancel: () -> Unit) {
    var showCancelButton by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(5000)
        showCancelButton = true
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AppCircularProgressIndicator(size = 32.dp, strokeWidth = 3.dp)
        if (showCancelButton) {
            Spacer(Modifier.height(100.dp))
            Button(onClick = onCancel) { Text("取消登录") }
        }

    }
}
