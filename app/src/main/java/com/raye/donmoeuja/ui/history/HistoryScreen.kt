@file:OptIn(ExperimentalMaterial3Api::class)

package com.raye.donmoeuja.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raye.donmoeuja.ui.AppViewModels
import com.raye.donmoeuja.ui.EmptyMessage
import com.raye.donmoeuja.ui.EntryListRow
import com.raye.donmoeuja.ui.formatNumber
import com.raye.donmoeuja.ui.theme.AccentSoft

@Composable
fun HistoryScreen(
    onOpenEntry: (Long) -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModels.Factory),
) {
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("기록", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.query.value = it.replace("\n", "") },
                    placeholder = { Text("요약 · 상세 내역 검색") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.query.value = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "검색어 지우기")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    HistoryFilter.entries.forEach { f ->
                        FilterChip(
                            selected = filter == f,
                            onClick = { viewModel.filter.value = f },
                            label = { Text(f.label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentSoft),
                        )
                    }
                }
            }

            val list = groups
            when {
                list == null -> Unit
                list.isEmpty() -> item {
                    EmptyMessage(if (query.isBlank()) "아직 기록이 없어요" else "‘${query.trim()}’에 맞는 기록이 없어요")
                }
                else -> list.forEach { group ->
                    item(key = "month-${group.month}") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                "${group.month.year}년 ${group.month.monthValue}월",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            val totals = buildList {
                                if (filter != HistoryFilter.INCOME) add("지출 ${formatNumber(group.expense)}")
                                if (filter != HistoryFilter.EXPENSE) add("수입 ${formatNumber(group.income)}")
                            }.joinToString(" · ")
                            Text(totals, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(group.entries, key = { it.entry.id }) { item ->
                        EntryListRow(item = item, onClick = { onOpenEntry(item.entry.id) })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
