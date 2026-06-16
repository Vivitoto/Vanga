package io.github.vivitoto.vanga.ui.dialogs.libraryedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.ui.LocalStrings
import io.github.vivitoto.vanga.ui.OptionsStateHolder
import io.github.vivitoto.vanga.ui.StateHolder
import io.github.vivitoto.vanga.ui.common.components.DropdownChoiceMenu
import io.github.vivitoto.vanga.ui.common.components.LabeledEntry
import io.github.vivitoto.vanga.ui.dialogs.tabs.DialogTab
import io.github.vivitoto.vanga.ui.dialogs.tabs.TabItem
import io.github.vivitoto.vanga.ui.settings.SettingsCheckboxRow
import io.github.vivitoto.vanga.ui.settings.SettingsSectionCard
import snd.komga.client.library.SeriesCover

internal class OptionsTab(
    private val vm: LibraryEditDialogViewModel,
) : DialogTab {

    override fun options() = TabItem(
        title = "选项",
        icon = Icons.Default.Tune
    )

    @Composable
    override fun Content() {
        OptionsTabContent(
            hashFiles = StateHolder(vm.hashFiles, vm::hashFiles::set),
            hashPages = StateHolder(vm.hashPages, vm::hashPages::set),
            analyzeDimensions = StateHolder(vm.analyzeDimensions, vm::analyzeDimensions::set),
            repairExtensions = StateHolder(vm.repairExtensions, vm::repairExtensions::set),
            convertToCbz = StateHolder(vm.convertToCbz, vm::convertToCbz::set),
            seriesCover = OptionsStateHolder(vm.seriesCover, SeriesCover.entries, vm::seriesCover::set),
        )
    }
}

@Composable
private fun OptionsTabContent(
    hashFiles: StateHolder<Boolean>,
    hashPages: StateHolder<Boolean>,
    analyzeDimensions: StateHolder<Boolean>,
    repairExtensions: StateHolder<Boolean>,
    convertToCbz: StateHolder<Boolean>,
    seriesCover: OptionsStateHolder<SeriesCover>,
) {
    val strings = LocalStrings.current.libraryEdit
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsSectionCard("文件处理") {
            SettingsCheckboxRow(
                title = strings.hashFiles,
                checked = hashFiles.value,
                onCheckedChange = hashFiles.setValue,
            )

            SettingsCheckboxRow(
                title = strings.hashPages,
                checked = hashPages.value,
                onCheckedChange = hashPages.setValue,
            )

            SettingsCheckboxRow(
                title = strings.analyzeDimensions,
                checked = analyzeDimensions.value,
                onCheckedChange = analyzeDimensions.setValue,
            )

            SettingsCheckboxRow(
                title = strings.repairExtensions,
                checked = repairExtensions.value,
                onCheckedChange = repairExtensions.setValue,
            )

            SettingsCheckboxRow(
                title = strings.convertToCbz,
                checked = convertToCbz.value,
                onCheckedChange = convertToCbz.setValue,
            )
        }

        DropdownChoiceMenu(
            selectedOption = LabeledEntry(
                seriesCover.value,
                strings.forSeriesCover(seriesCover.value)
            ),
            options = SeriesCover.entries.map { LabeledEntry(it, strings.forSeriesCover(it)) },
            onOptionChange = { seriesCover.onValueChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text(strings.seriesCover) }
        )

    }
}
