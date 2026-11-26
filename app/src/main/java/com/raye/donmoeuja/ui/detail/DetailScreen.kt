@file:OptIn(ExperimentalMaterial3Api::class)

package com.raye.donmoeuja.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.raye.donmoeuja.data.EntryType
import com.raye.donmoeuja.ui.AppViewModels
import com.raye.donmoeuja.ui.ConfirmDialog
import com.raye.donmoeuja.ui.LabelValueRow
import com.raye.donmoeuja.ui.SectionTitle
import com.raye.donmoeuja.ui.TypeChip
import com.raye.donmoeuja.ui.UNCATEGORIZED
import com.raye.donmoeuja.ui.formatNumber
import com.raye.donmoeuja.ui.full
import com.raye.donmoeuja.ui.toLocalDateTime

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: DetailViewModel = viewModel(factory = AppViewModels.Factory),
) {
    val item by viewModel.entry.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var showImage by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상세", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    TextButton(onClick = { onEdit(viewModel.entryId) }, enabled = item != null) { Text("수정") }
                    TextButton(onClick = { confirmDelete = true }, enabled = item != null) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        val current = item ?: return@Scaffold
        val e = current.entry
        val file = e.attachmentFile
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypeChip(e.type)
                    Spacer(Modifier.width(8.dp))
                    Text(current.categoryName ?: UNCATEGORIZED, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    (if (e.type == EntryType.EXPENSE) "−" else "+") + formatNumber(e.amount) + "원",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabelValueRow("날짜", e.occurredAt.toLocalDateTime().full())
                    LabelValueRow("구분", e.type.label)
                    e.paymentMethod?.let { LabelValueRow("결제수단", it.label) }
                    LabelValueRow("카테고리", current.categoryName ?: UNCATEGORIZED)
                    LabelValueRow("요약", e.summary.ifBlank { "—" })
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionTitle("상세 내역")
                Text(e.detail.ifBlank { "—" }, lineHeight = 22.sp)
            }

            if (file != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionTitle("첨부")
                    if (e.attachmentMime?.startsWith("image/") == true) {
                        AsyncImage(
                            model = viewModel.attachments.fileOf(file),
                            contentDescription = e.attachmentName ?: "첨부 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .clickable { showImage = true },
                        )
                        Text("사진을 누르면 크게 볼 수 있어요", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(e.attachmentName ?: "첨부파일", modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = {
                                val intent = Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(viewModel.attachments.shareUri(file), e.attachmentMime ?: "*/*")
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                try {
                                    context.startActivity(Intent.createChooser(intent, "파일 열기"))
                                } catch (ex: ActivityNotFoundException) {
                                    Toast.makeText(context, "이 파일을 열 수 있는 앱이 없어요", Toast.LENGTH_SHORT).show()
                                }
                            }) { Text("열기") }
                        }
                    }
                }
            }
        }

        if (showImage && file != null) {
            Dialog(
                onDismissRequest = { showImage = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { showImage = false },
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = viewModel.attachments.fileOf(file),
                        contentDescription = e.attachmentName ?: "첨부 사진",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        if (confirmDelete) {
            ConfirmDialog(
                title = "삭제할까요?",
                message = "이 내역과 첨부 파일이 함께 지워지고 되돌릴 수 없어요.",
                confirmText = "삭제",
                onConfirm = {
                    viewModel.delete(e, onDone = onBack)
                },
                onDismiss = { confirmDelete = false },
            )
        }
    }
}
