package io.github.vivitoto.vanga.ui.dialogs.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockPerson
import androidx.compose.material.icons.filled.RecentActors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dokar.chiptextfield.Chip
import com.dokar.chiptextfield.m3.ChipTextField
import com.dokar.chiptextfield.rememberChipTextFieldState
import kotlinx.coroutines.launch
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.LocalViewModelFactory
import io.github.vivitoto.vanga.ui.OptionsStateHolder
import io.github.vivitoto.vanga.ui.StateHolder
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.dialogs.tabs.DialogTab
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabDialog
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabItem
import io.github.vivitoto.vanga.ui.dialogs.user.UserEditDialogViewModel.AgeRestriction
import io.github.vivitoto.vanga.ui.settings.SettingsCheckboxRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.user.KomgaUser

@Composable
fun UserEditDialog(
    user: KomgaUser,
    afterConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val vm = remember { viewModelFactory.getUserEditDialogViewModel(user) }
    val coroutineScope = rememberCoroutineScope()

    TabDialog(
        title = "编辑用户",
        currentTab = vm.currentTab,
        tabs = vm.tabs(),
        confirmationText = "保存更改",
        onConfirm = {
            coroutineScope.launch {
                vm.saveChanges()
                afterConfirm()
                onDismiss()
            }
        },
        onTabChange = { vm.currentTab = it },
        onDismissRequest = onDismiss
    )
}

class UserRolesTab(private val vm: UserEditDialogViewModel) : DialogTab {
    override fun options() = TabItem(
        title = "角色",
        icon = Icons.Default.RecentActors
    )

    @Composable
    override fun Content() {
        UserRolesContent(
            user = vm.user,
            administrator = StateHolder(vm.administratorRole, vm::administratorRole::set),
            pageStreaming = StateHolder(vm.pageStreamingRole, vm::pageStreamingRole::set),
            fileDownload = StateHolder(vm.fileDownloadRole, vm::fileDownloadRole::set),
        )
    }

    @Composable
    private fun UserRolesContent(
        user: KomgaUser,
        administrator: StateHolder<Boolean>,
        pageStreaming: StateHolder<Boolean>,
        fileDownload: StateHolder<Boolean>
    ) {
        Column {
            SettingsSectionCard(
                title = "角色",
                description = user.email,
            ) {
                SettingsCheckboxRow(
                    title = "管理员",
                    checked = administrator.value,
                    onCheckedChange = { administrator.setValue(it) },
                )
                SettingsCheckboxRow(
                    title = "页面流式传输",
                    checked = pageStreaming.value,
                    onCheckedChange = { pageStreaming.setValue(it) },
                )
                SettingsCheckboxRow(
                    title = "文件下载",
                    checked = fileDownload.value,
                    onCheckedChange = { fileDownload.setValue(it) },
                )
            }
        }
    }
}

class UserSharedLibrariesTab(private val vm: UserEditDialogViewModel) : DialogTab {

    override fun options() = TabItem(
        title = "已共享书库",
        icon = Icons.Default.Share
    )

    @Composable
    override fun Content() {
        UserSharedLibrariesContent(
            shareAll = vm.shareAllLibraries,
            onShareAllChange = vm::shareAllLibraries::set,
            allLibraries = vm.libraries,
            sharedLibraries = vm.sharedLibraries,
            onLibraryCheck = vm::addSharedLibrary,
            onLibraryUncheck = vm::removeSharedLibrary
        )
    }

    @Composable
    private fun UserSharedLibrariesContent(
        shareAll: Boolean,
        onShareAllChange: (Boolean) -> Unit,
        allLibraries: List<KomgaLibrary>,
        sharedLibraries: Set<KomgaLibraryId>,
        onLibraryCheck: (KomgaLibraryId) -> Unit,
        onLibraryUncheck: (KomgaLibraryId) -> Unit,
    ) {
        Column {
            SettingsSectionCard("共享书库") {
                SettingsCheckboxRow(
                    title = "全部书库",
                    checked = shareAll,
                    onCheckedChange = onShareAllChange,
                )

                HorizontalDivider()

                allLibraries.forEach { library ->

                    SettingsCheckboxRow(
                        title = library.name,
                        checked = sharedLibraries.contains(library.id),
                        onCheckedChange = { isChecked ->
                            if (!shareAll) {
                                if (isChecked) onLibraryCheck(library.id) else onLibraryUncheck(library.id)
                            }
                        },
                        enabled = !shareAll
                    )

                }
            }

        }
    }
}

class UserContentRestrictionTab(private val vm: UserEditDialogViewModel) : DialogTab {

    override fun options() = TabItem(
        title = "内容限制",
        icon = Icons.Default.LockPerson
    )

    @Composable
    override fun Content() {
        UserContentRestrictionContent(
            restriction = OptionsStateHolder(vm.ageRestriction, AgeRestriction.entries, vm::ageRestriction::set),
            age = StateHolder(vm.ageRating, vm::ageRating::set),
            labelsAllow = StateHolder(vm.labelsAllow, vm::labelsAllow::set),
            labelsExclude = StateHolder(vm.labelsExclude, vm::labelsExclude::set)

        )

    }

    @Composable
    private fun UserContentRestrictionContent(
        restriction: OptionsStateHolder<AgeRestriction>,
        age: StateHolder<Int>,
        labelsAllow: StateHolder<Set<String>>,
        labelsExclude: StateHolder<Set<String>>,
    ) {
        val strings = LocalStrings.current.userEdit

        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(strings.contentRestrictions)
            Column(verticalArrangement = Arrangement.spacedBy(40.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    DropdownChoiceMenu(
                        selectedOption = LabeledEntry(restriction.value, strings.forAgeRestriction(restriction.value)),
                        options = restriction.options.map { LabeledEntry(it, strings.forAgeRestriction(it)) },
                        onOptionChange = { restriction.onValueChange(it.value) },
                        label = { Text(strings.ageRestriction) },
                        inputFieldModifier = Modifier.weight(1f)
                    )
                    TextField(
                        value = age.value.toString(),
                        onValueChange = {
                            val newValue = it.toIntOrNull()
                            if (newValue != null) age.setValue(newValue)
                        },
                        label = { Text(strings.age) },
                        modifier = Modifier.weight(1f),
                        enabled = restriction.value != AgeRestriction.NONE
                    )
                }

                val labelsAllowState = rememberChipTextFieldState(labelsAllow.value.map { Chip(it) })
                LaunchedEffect(labelsAllowState, labelsAllow.value) {
                    snapshotFlow { labelsAllowState.chips.map { it.text }.toSet() }
                        .collect { labelsAllow.setValue(it) }
                }
                ChipTextField(
                    state = labelsAllowState,
                    label = { Text(strings.labelsAllow) },
                    onSubmit = { text -> Chip(text) },
                    readOnlyChips = true,
                    modifier = Modifier.fillMaxWidth()
                )

                val labelsExcludeState = rememberChipTextFieldState(labelsExclude.value.map { Chip(it) })
                LaunchedEffect(labelsExcludeState, labelsExclude.value) {
                    snapshotFlow { labelsExcludeState.chips.map { it.text }.toSet() }
                        .collect { labelsExclude.setValue(it) }
                }
                ChipTextField(
                    state = labelsExcludeState,
                    label = { Text(strings.labelsExclude) },
                    onSubmit = { text -> Chip(text) },
                    readOnlyChips = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
