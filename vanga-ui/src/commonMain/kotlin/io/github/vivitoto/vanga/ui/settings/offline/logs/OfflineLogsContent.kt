package io.github.vivitoto.vanga.ui.settings.offline.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.vivitoto.vanga.DefaultDateTimeFormats.toSystemTimeString
import io.github.vivitoto.vanga.offline.sync.model.OfflineLogEntry
import io.github.vivitoto.vanga.ui.common.components.AppFilterChipDefaults
import io.github.vivitoto.vanga.ui.common.components.EmptyState
import io.github.vivitoto.vanga.ui.common.components.Pagination
import io.github.vivitoto.vanga.ui.dialogs.ConfirmationDialog
import io.github.vivitoto.vanga.ui.settings.SettingsSectionHeader
import io.github.vivitoto.vanga.ui.settings.offline.logs.OfflineLogsState.TaskTab

@Composable
fun OfflineLogsContent(
    logs: List<OfflineLogEntry>,
    totalPages: Int,
    currentPage: Int,
    onPageChange: (Int) -> Unit,
    selectedTab: TaskTab,
    onTabSelect: (TaskTab) -> Unit,
    onDelete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsSectionHeader(
            title = "离线日志",
            description = "用于排查离线下载或同步失败；日常阅读不用关注。"
        )

        StatusFilters(selectedTab, onTabSelect, onDelete)

        if (totalPages > 1) {
            Pagination(
                totalPages = totalPages,
                currentPage = currentPage,
                onPageChange = onPageChange,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (logs.isEmpty()) {
            EmptyState("暂无记录", body = "离线下载和同步日志会显示在这里。")
        } else {
            LogsContent(logs)

        }

        if (logs.size > 10) {
            Pagination(
                totalPages = totalPages,
                currentPage = currentPage,
                onPageChange = onPageChange,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatusFilters(
    selectedTab: TaskTab,
    onTabSelect: (TaskTab) -> Unit,
    onDelete: () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        FilterChip(
            selected = selectedTab == TaskTab.ERROR,
            onClick = { onTabSelect(TaskTab.ERROR) },
            label = { Text("错误") },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null
        )
        FilterChip(
            selected = selectedTab == TaskTab.INFO,
            onClick = { onTabSelect(TaskTab.INFO) },
            label = { Text("信息") },
            colors = AppFilterChipDefaults.filterChipColors(),
            border = null
        )

        var showDeleteDialog by remember { mutableStateOf(false) }

        Spacer(Modifier.weight(1f))
        FilledTonalButton(onClick = { showDeleteDialog = true }) { Text("清空记录") }

        if (showDeleteDialog) {
            ConfirmationDialog(
                body = "确定清空离线任务记录吗？",
                onDialogDismiss = { showDeleteDialog = false },
                onDialogConfirm = onDelete
            )
        }
    }
}

@Composable
private fun LogsContent(logs: List<OfflineLogEntry>) {
    SelectionContainer {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            logs.forEachIndexed { index, task ->
                if (index > 0) HorizontalDivider()
                LogEntryContent(task)
            }
        }
    }
}

@Composable
private fun LogEntryContent(logEntry: OfflineLogEntry) {
    Row(
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            when (logEntry.type) {
                OfflineLogEntry.Type.DEBUG -> {}
                OfflineLogEntry.Type.INFO -> Text(logEntry.message)
                OfflineLogEntry.Type.ERROR -> Text(
                    text = logEntry.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        }

        Text(logEntry.timestamp.toSystemTimeString())

    }
}
