package io.github.vivitoto.vanga.ui.platform

import androidx.compose.runtime.Composable

@Composable
expect fun BackPressHandler(onBackPressed: () -> Unit)

