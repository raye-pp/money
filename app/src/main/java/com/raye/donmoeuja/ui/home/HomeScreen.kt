@file:OptIn(ExperimentalMaterial3Api::class)

package com.raye.donmoeuja.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raye.donmoeuja.data.Entry
import com.raye.donmoeuja.ui.AppViewModels
import com.raye.donmoeuja.ui.ConfirmDialog
import com.raye.donmoeuja.ui.EmptyMessage
import com.raye.donmoeuja.ui.EntryListRow
import com.raye.donmoeuja.ui.LabelValueRow
import com.raye.donmoeuja.ui.PeriodPickerDialog
import com.raye.donmoeuja.ui.SectionTitle
import com.raye.donmoeuja.ui.ThousandsTransformation
import com.raye.donmoeuja.ui.formatWon
import com.raye.donmoeuja.ui.sanitizeAmount
import com.raye.donmoeuja.ui.short
import com.raye.donmoeuja.ui.theme.Accent
import com.raye.donmoeuja.ui.theme.Ink

@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onOpenEntry: (Long) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModels.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPeriodPicker by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Entry?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("레이야 돈 모으자", fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = onAdd,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink),
                        border = BorderStroke(1.5.dp, Ink),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.padding(end = 12.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("추가하기", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        val s = state ?: return@Scaffold
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            item {
                SummaryCard(
                    state = s,
                    onPeriodClick = { showPeriodPicker = true },
                    onPrev = { viewModel.shiftPeriod(-1) },
                    onNext = { viewModel.shiftPeriod(1) },
                    onToggle = { viewModel.setCollapsed(!s.settings.summaryCollapsed) },
                    onBudgetClick = { showBudgetDialog = true },
                )
            }
            item {
                SectionTitle("목록 · 최신순", Modifier.padding(top = 20.dp, bottom = 4.dp))
            }
            if (s.entries.isEmpty()) {
                item { EmptyMessage("이 기간에 입력한 내역이 없어요.\n오른쪽 위 [추가하기]로 시작해 보세요.") }
            } else {
                items(s.entries, key = { it.entry.id }) { item ->
                    EntryListRow(
                        item = item,
                        onClick = { onOpenEntry(item.entry.id) },
                        onDelete = { pendingDelete = item.entry },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        if (showPeriodPicker) {
            PeriodPickerDialog(
                start = s.settings.start,
                end = s.settings.end,
                onConfirm = viewModel::setPeriod,
                onDismiss = { showPeriodPicker = false },
            )
        }
        if (showBudgetDialog) {
            BudgetDialog(
                current = s.settings.budget,
                onConfirm = { viewModel.setBudget(it) },
                onDismiss = { showBudgetDialog = false },
            )
        }
        pendingDelete?.let { entry ->
            ConfirmDialog(
                title = "삭제할까요?",
                message = "‘${entry.summary.ifBlank { formatWon(entry.amount) }}’ 내역을 지우면 되돌릴 수 없어요.",
                confirmText = "삭제",
                onConfirm = { viewModel.delete(entry) },
                onDismiss = { pendingDelete = null },
            )
        }
    }
}

@Composable
private fun SummaryCard(
    state: HomeUiState,
    onPeriodClick: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggle: () -> Unit,
    onBudgetClick: () -> Unit,
) {
    val settings = state.settings
    val collapsed = settings.summaryCollapsed
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("기간", color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onPrev) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "이전 기간")
                }
                Text(
                    "${settings.start.short()} ~ ${settings.end.short()}",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onPeriodClick)
                        .padding(vertical = 8.dp),
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "다음 기간")
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        if (collapsed) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                        contentDescription = if (collapsed) "펼치기" else "접기",
                    )
                }
            }
            AnimatedVisibility(visible = !collapsed) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 10.dp),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onBudgetClick),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("예산", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            if (settings.budget > 0) formatWon(settings.budget) else "눌러서 입력",
                            fontWeight = FontWeight.Bold,
                            color = if (settings.budget > 0) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    LabelValueRow("총지출", formatWon(state.totalExpense))
                    LabelValueRow("추가수입", formatWon(state.totalIncome))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    LabelValueRow(
                        "남은 예산",
                        (if (state.remaining < 0) "−" else "") + formatWon(kotlin.math.abs(state.remaining)),
                        valueColor = if (state.remaining < 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetDialog(current: Long, onConfirm: (Long) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(if (current > 0) current.toString() else "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("예산 입력") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = sanitizeAmount(it) },
                label = { Text("예산") },
                suffix = { Text("원") },
                singleLine = true,
                visualTransformation = ThousandsTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(text.toLongOrNull() ?: 0L)
                onDismiss()
            }) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
