@file:OptIn(ExperimentalMaterial3Api::class)

package com.raye.donmoeuja.ui.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raye.donmoeuja.ui.AppViewModels
import com.raye.donmoeuja.ui.LabelValueRow
import com.raye.donmoeuja.ui.PeriodPickerDialog
import com.raye.donmoeuja.ui.SectionTitle
import com.raye.donmoeuja.ui.UNCATEGORIZED
import com.raye.donmoeuja.ui.formatWon
import com.raye.donmoeuja.ui.short

@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel(factory = AppViewModels.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("통계", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        val s = state ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("기간", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "${s.start.short()} ~ ${s.end.short()}  ▾",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showPicker = true }
                            .padding(vertical = 6.dp),
                    )
                    TextButton(onClick = viewModel::resetToHomePeriod) { Text("홈 기간으로") }
                }
                Text("기본값은 홈에서 설정한 기간이에요", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            StatCard {
                LabelValueRow("총지출", formatWon(s.totalExpense))
                LabelValueRow("총수입", formatWon(s.totalIncome))
            }

            StatCard {
                SectionTitle("결제수단별 (지출)")
                if (s.paymentTotals.isEmpty()) {
                    NoData()
                } else {
                    val max = s.paymentTotals.maxOf { it.total }
                    s.paymentTotals.forEach {
                        BarRow(it.paymentMethod?.label ?: "—", it.total, max)
                    }
                }
            }

            StatCard {
                SectionTitle("카테고리별 (지출)")
                if (s.categoryTotals.isEmpty()) {
                    NoData()
                } else {
                    // 0원 카테고리는 쿼리에서 제외됨. 큰 금액 순
                    val max = s.categoryTotals.maxOf { it.total }
                    s.categoryTotals.forEach {
                        BarRow(it.categoryName ?: UNCATEGORIZED, it.total, max)
                    }
                }
            }
        }

        if (showPicker) {
            PeriodPickerDialog(
                start = s.start,
                end = s.end,
                onConfirm = viewModel::setPeriod,
                onDismiss = { showPicker = false },
            )
        }
    }
}

@Composable
private fun StatCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            content()
        }
    }
}

@Composable
private fun BarRow(label: String, amount: Long, max: Long) {
    val fraction = if (max > 0) (amount.toFloat() / max).coerceIn(0.02f, 1f) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LabelValueRow(label, formatWon(amount))
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(5.dp)),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp)),
            )
        }
    }
}

@Composable
private fun NoData() {
    Text("이 기간에 지출 내역이 없어요", color = MaterialTheme.colorScheme.onSurfaceVariant)
}
