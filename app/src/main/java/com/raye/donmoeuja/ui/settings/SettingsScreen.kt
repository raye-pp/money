@file:OptIn(ExperimentalMaterial3Api::class)

package com.raye.donmoeuja.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raye.donmoeuja.data.Category
import com.raye.donmoeuja.data.CategoryResult
import com.raye.donmoeuja.data.MoneyRepository
import com.raye.donmoeuja.ui.AppViewModels
import com.raye.donmoeuja.ui.ConfirmDialog
import com.raye.donmoeuja.ui.SectionTitle
import com.raye.donmoeuja.ui.UNCATEGORIZED
import com.raye.donmoeuja.ui.theme.Accent
import com.raye.donmoeuja.ui.theme.Ink
import kotlinx.coroutines.launch

private data class PendingDelete(val category: Category, val usage: Int)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel(factory = AppViewModels.Factory)) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    var newName by remember { mutableStateOf("") }
    var addError by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<Category?>(null) }
    var pendingDelete by remember { mutableStateOf<PendingDelete?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            item { SectionTitle("카테고리", Modifier.padding(bottom = 4.dp)) }
            items(categories, key = { it.id }) { c ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                ) {
                    Text(c.name, modifier = Modifier.weight(1f))
                    TextButton(onClick = { editing = c }) {
                        Text("수정", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = {
                        scope.launch { pendingDelete = PendingDelete(c, viewModel.usageCount(c)) }
                    }) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
            item {
                Column(Modifier.padding(top = 16.dp)) {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = {
                                newName = it.replace("\n", "").take(MoneyRepository.MAX_CATEGORY_NAME)
                                addError = null
                            },
                            placeholder = { Text("새 카테고리 이름") },
                            singleLine = true,
                            isError = addError != null,
                            supportingText = { Text(addError ?: "최대 ${MoneyRepository.MAX_CATEGORY_NAME}자") },
                            modifier = Modifier.weight(1f),
                        )
                        Button(
                            onClick = {
                                viewModel.add(newName) { r ->
                                    when (r) {
                                        CategoryResult.Ok -> { newName = "" }
                                        is CategoryResult.Error -> { addError = r.message }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink),
                            border = BorderStroke(1.5.dp, Ink),
                            modifier = Modifier.padding(top = 8.dp),
                        ) { Text("추가", fontWeight = FontWeight.Bold) }
                    }
                    Text(
                        "카테고리는 1단계로만 관리돼요. 지출과 수입이 같은 목록을 함께 써요.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }

    editing?.let { c ->
        RenameDialog(
            category = c,
            onSubmit = { name, onError ->
                viewModel.rename(c, name) { r ->
                    when (r) {
                        CategoryResult.Ok -> { editing = null }
                        is CategoryResult.Error -> onError(r.message)
                    }
                }
            },
            onDismiss = { editing = null },
        )
    }

    pendingDelete?.let { p ->
        ConfirmDialog(
            title = "‘${p.category.name}’ 삭제",
            message = if (p.usage > 0) {
                "이 카테고리를 쓴 내역 ${p.usage}건은 지워지지 않고 ‘$UNCATEGORIZED’로 남아요."
            } else {
                "이 카테고리를 삭제할까요?"
            },
            confirmText = "삭제",
            onConfirm = { viewModel.delete(p.category) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun RenameDialog(
    category: Category,
    onSubmit: (String, (String) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(category.name) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("카테고리 이름 바꾸기") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.replace("\n", "").take(MoneyRepository.MAX_CATEGORY_NAME)
                    error = null
                },
                singleLine = true,
                isError = error != null,
                supportingText = { Text(error ?: "기존 내역에도 새 이름으로 보여요") },
            )
        },
        confirmButton = { TextButton(onClick = { onSubmit(name) { error = it } }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
