@file:OptIn(ExperimentalMaterial3Api::class)

package com.raye.donmoeuja.ui.edit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raye.donmoeuja.data.EntryType
import com.raye.donmoeuja.data.PaymentMethod
import com.raye.donmoeuja.ui.AppViewModels
import com.raye.donmoeuja.ui.PickerField
import com.raye.donmoeuja.ui.SectionTitle
import com.raye.donmoeuja.ui.SingleDatePickerDialog
import com.raye.donmoeuja.ui.ThousandsTransformation
import com.raye.donmoeuja.ui.TimePickerDialog
import com.raye.donmoeuja.ui.full
import com.raye.donmoeuja.ui.theme.Accent
import com.raye.donmoeuja.ui.theme.Ink

@Composable
fun EditScreen(
    onClose: () -> Unit,
    viewModel: EditViewModel = viewModel(factory = AppViewModels.Factory),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var categoryMenu by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::pickAttachment)
    }
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::pickAttachment)
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.errorMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEdit) "수정하기" else "추가하기", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 날짜
            PickerField(
                value = viewModel.occurredAt.full(),
                label = "날짜",
                onClick = { showDate = true },
            )

            // 지출 / 수입
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle("구분")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    EntryType.entries.forEachIndexed { i, t ->
                        SegmentedButton(
                            selected = viewModel.type == t,
                            onClick = { viewModel.type = t },
                            shape = SegmentedButtonDefaults.itemShape(i, EntryType.entries.size),
                        ) { Text(t.label) }
                    }
                }
            }

            // 결제수단: 지출일 때만
            if (viewModel.type == EntryType.EXPENSE) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionTitle("결제수단")
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        PaymentMethod.entries.forEachIndexed { i, p ->
                            SegmentedButton(
                                selected = viewModel.payment == p,
                                onClick = { viewModel.payment = p },
                                shape = SegmentedButtonDefaults.itemShape(i, PaymentMethod.entries.size),
                            ) { Text(p.label) }
                        }
                    }
                }
            }

            // 금액 (숫자만)
            val amountError = viewModel.showErrors && viewModel.amountError
            OutlinedTextField(
                value = viewModel.amountText,
                onValueChange = viewModel::onAmountChange,
                label = { Text("금액") },
                suffix = { Text("원") },
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) ({ Text("금액을 입력해 주세요") }) else null,
                visualTransformation = ThousandsTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            // 카테고리 (1단계)
            val categoryError = viewModel.showErrors && viewModel.categoryError
            Box {
                PickerField(
                    value = categories.firstOrNull { it.id == viewModel.categoryId }?.name ?: "",
                    label = "카테고리",
                    placeholder = "선택해 주세요",
                    onClick = { categoryMenu = true },
                    isError = categoryError,
                    supportingText = when {
                        categoryError -> "카테고리를 선택해 주세요"
                        categories.isEmpty() -> "설정에서 카테고리를 먼저 만들어 주세요"
                        else -> null
                    },
                )
                DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                    categories.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c.name) },
                            onClick = {
                                viewModel.categoryId = c.id
                                categoryMenu = false
                            },
                        )
                    }
                }
            }

            // 요약 15자
            OutlinedTextField(
                value = viewModel.summary,
                onValueChange = viewModel::onSummaryChange,
                label = { Text("요약") },
                singleLine = true,
                supportingText = { Text("${viewModel.summary.length}/${EditViewModel.MAX_SUMMARY}") },
                modifier = Modifier.fillMaxWidth(),
            )

            // 상세 내역 200자
            OutlinedTextField(
                value = viewModel.detail,
                onValueChange = viewModel::onDetailChange,
                label = { Text("상세 내역") },
                minLines = 3,
                supportingText = { Text("${viewModel.detail.length}/${EditViewModel.MAX_DETAIL}") },
                modifier = Modifier.fillMaxWidth(),
            )

            // 첨부 1개
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle("사진 / 파일 첨부 (1개)")
                val a = viewModel.attachment
                if (a != null) {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp),
                        ) {
                            Text(
                                a.displayName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { viewModel.attachment = null }) {
                                Icon(Icons.Filled.Close, contentDescription = "첨부 삭제")
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text(if (a == null) "+ 사진" else "사진으로 교체") }
                    OutlinedButton(
                        onClick = { filePicker.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f),
                    ) { Text(if (a == null) "+ 파일" else "파일로 교체") }
                }
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { viewModel.save(onSaved = onClose) },
                enabled = !viewModel.saving,
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink),
                border = BorderStroke(1.5.dp, Ink),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("저장", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(0.dp).height(16.dp))
        }
    }

    if (showDate) {
        SingleDatePickerDialog(
            date = viewModel.occurredAt.toLocalDate(),
            onConfirm = { d ->
                viewModel.occurredAt = viewModel.occurredAt.with(d)
                showTime = true
            },
            onDismiss = { showDate = false },
        )
    }
    if (showTime) {
        TimePickerDialog(
            time = viewModel.occurredAt.toLocalTime(),
            onConfirm = { t -> viewModel.occurredAt = viewModel.occurredAt.with(t) },
            onDismiss = { showTime = false },
        )
    }
}
