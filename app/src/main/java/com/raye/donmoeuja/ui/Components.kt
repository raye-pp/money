@file:OptIn(ExperimentalMaterial3Api::class)

package com.raye.donmoeuja.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextOverflow
import com.raye.donmoeuja.data.EntryWithCategory
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raye.donmoeuja.data.EntryType
import com.raye.donmoeuja.ui.theme.Accent
import com.raye.donmoeuja.ui.theme.Ink
import com.raye.donmoeuja.ui.theme.Paper
import java.time.LocalDate
import java.time.LocalTime

const val UNCATEGORIZED = "미분류"

/** 지출 / 수입 표시 칩 — 색과 글자로 함께 구분 */
@Composable
fun TypeChip(type: EntryType, modifier: Modifier = Modifier) {
    val (bg, fg) = when (type) {
        EntryType.EXPENSE -> Ink to Paper
        EntryType.INCOME -> Accent to Ink
    }
    Text(
        text = type.label,
        color = fg,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

fun signedAmount(type: EntryType, amount: Long): String = when (type) {
    EntryType.EXPENSE -> "−${formatNumber(amount)}"
    EntryType.INCOME -> "+${formatNumber(amount)}"
}

/** 눌러서 고르는 읽기 전용 입력칸 (날짜, 카테고리 등) */
@Composable
fun PickerField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    trailing: String = "▾",
) {
    val c = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = {},
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        readOnly = true,
        enabled = false,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        trailingIcon = { Text(trailing, color = c.onSurfaceVariant) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = c.onSurface,
            disabledBorderColor = if (isError) c.error else c.outline,
            disabledLabelColor = if (isError) c.error else c.onSurfaceVariant,
            disabledPlaceholderColor = c.onSurfaceVariant,
            disabledTrailingIconColor = c.onSurfaceVariant,
            disabledSupportingTextColor = if (isError) c.error else c.onSurfaceVariant,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(
                    confirmText,
                    color = if (destructive) MaterialTheme.colorScheme.error else Color.Unspecified,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

@Composable
fun PeriodPickerDialog(
    start: LocalDate,
    end: LocalDate,
    onConfirm: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = start.toPickerMillis(),
        initialSelectedEndDateMillis = end.toPickerMillis(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null,
                onClick = {
                    val s = state.selectedStartDateMillis?.fromPickerMillis()
                    val e = state.selectedEndDateMillis?.fromPickerMillis()
                    if (s != null && e != null) onConfirm(s, e)
                    onDismiss()
                },
            ) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    ) {
        DateRangePicker(
            state = state,
            title = { Text("기간 선택 (시작일 ~ 종료일)", modifier = Modifier.padding(start = 24.dp, top = 16.dp)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp),
        )
    }
}

@Composable
fun SingleDatePickerDialog(
    date: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = date.toPickerMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(it.fromPickerMillis()) }
                onDismiss()
            }) { Text("다음") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    ) {
        DatePicker(state = state)
    }
}

@Composable
fun TimePickerDialog(
    time: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("시간 선택") },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(LocalTime.of(state.hour, state.minute))
                onDismiss()
            }) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

/** 라벨 – 값 한 줄 */
@Composable
fun LabelValueRow(label: String, value: String, valueColor: Color = Color.Unspecified, bold: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = valueColor, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 목록 한 건 (2줄): 윗줄 구분 · 요약 · 금액 / 아랫줄 날짜 · 카테고리 · 결제수단 · 삭제 */
@Composable
fun EntryListRow(item: EntryWithCategory, onClick: () -> Unit, onDelete: (() -> Unit)? = null) {
    val e = item.entry
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TypeChip(e.type)
            Spacer(Modifier.width(8.dp))
            Text(
                e.summary.ifBlank { "(요약 없음)" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(signedAmount(e.type, e.amount), fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 32.dp)) {
            val meta = listOf(
                e.occurredAt.toLocalDateTime().toLocalDate().short(),
                item.categoryName ?: UNCATEGORIZED,
                e.paymentMethod?.label ?: "—",
            ).joinToString(" · ")
            Text(
                meta,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (onDelete != null) {
                TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text("삭제", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
            }
        }
    }
}
