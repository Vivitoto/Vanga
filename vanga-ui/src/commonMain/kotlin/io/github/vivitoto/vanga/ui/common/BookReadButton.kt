package io.github.vivitoto.vanga.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.komga.api.model.VangaBook
import io.github.vivitoto.vanga.ui.VangaButtonShape
import io.github.vivitoto.vanga.ui.platform.cursorForHand
import snd.komga.client.book.MediaProfile.EPUB
import io.github.vivitoto.vanga.webview.webviewIsAvailable

@Composable
fun readIsSupported(book: VangaBook) = book.media.mediaProfile != EPUB || webviewIsAvailable()

@Composable
fun BookReadButton(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    fillContentWidth: Boolean = false,
    onRead: () -> Unit,
    onIncognitoRead: () -> Unit,
    onDropdownOpenChange: (Boolean) -> Unit = {}
) {
    val containerColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onPrimary
    val buttonHeight = if (compact) 40.dp else 48.dp
    val readHorizontalPadding = if (compact) 8.dp else 12.dp
    val dropdownMinWidth = if (compact) 40.dp else 48.dp

    Surface(
        shape = VangaButtonShape,
        modifier = modifier.semantics { role = Role.Button }.pointerHoverIcon(PointerIcon.Hand),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Row(
            Modifier
                .height(buttonHeight)
                .then(if (fillContentWidth) Modifier.fillMaxWidth() else Modifier),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReadButton(
                modifier = Modifier
                    .defaultMinSize(minWidth = if (compact) dropdownMinWidth else 0.dp, minHeight = buttonHeight)
                    .padding(horizontal = readHorizontalPadding)
                    .fillMaxHeight(),
                showLabel = !compact,
                onRead = onRead,
            )
            VerticalDivider(color = contentColor.copy(alpha = 0.32f))
            IncognitoDropDown(
                modifier = Modifier
                    .defaultMinSize(minWidth = dropdownMinWidth, minHeight = buttonHeight)
                    .fillMaxHeight(),
                onIncognitoRead = onIncognitoRead,
                onDropdownOpenChange = onDropdownOpenChange
            )
        }
    }
}

@Composable
private fun ReadButton(
    modifier: Modifier,
    showLabel: Boolean,
    onRead: () -> Unit,
) {
    Row(
        modifier = Modifier.clickable { onRead() }.then(modifier),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
            contentDescription = null,
        )
        if (showLabel) {
            Spacer(Modifier.width(10.dp))
            Text("阅读")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncognitoDropDown(
    modifier: Modifier,
    onIncognitoRead: () -> Unit,
    onDropdownOpenChange: (Boolean) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = {
            onDropdownOpenChange(it)
            isExpanded = it
        },
    ) {

        Box(
            modifier = Modifier
                .clickable { isExpanded = true }
                .menuAnchor(PrimaryNotEditable)
                .then(modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ExpandMore, null)
        }
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = {
                onDropdownOpenChange(false)
                isExpanded = false
            },
            modifier = Modifier.width(150.dp)
        ) {
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Default.VisibilityOff, contentDescription = null) },
                text = { Text("无痕阅读") },
                onClick = {
                    isExpanded = false
                    onDropdownOpenChange(false)
                    onIncognitoRead()
                },
                modifier = Modifier.cursorForHand()
            )
        }
    }
}
