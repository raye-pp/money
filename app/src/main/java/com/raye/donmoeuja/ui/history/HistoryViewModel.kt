package com.raye.donmoeuja.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raye.donmoeuja.data.EntryType
import com.raye.donmoeuja.data.EntryWithCategory
import com.raye.donmoeuja.data.MoneyRepository
import com.raye.donmoeuja.ui.toLocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

enum class HistoryFilter(val label: String, val type: EntryType?) {
    ALL("전체", null),
    EXPENSE("지출", EntryType.EXPENSE),
    INCOME("수입", EntryType.INCOME),
}

data class MonthGroup(
    val month: YearMonth,
    val expense: Long,
    val income: Long,
    val entries: List<EntryWithCategory>,
)

/** 기록 탭: 기간과 상관없이 전체 내역을 월별로 */
class HistoryViewModel(repository: MoneyRepository) : ViewModel() {
    val filter = MutableStateFlow(HistoryFilter.ALL)
    val query = MutableStateFlow("")

    val groups: StateFlow<List<MonthGroup>?> = combine(repository.allEntries, filter, query) { list, f, q ->
        val keyword = q.trim()
        list
            .filter { f.type == null || it.entry.type == f.type }
            .filter {
                keyword.isEmpty() ||
                    it.entry.summary.contains(keyword, ignoreCase = true) ||
                    it.entry.detail.contains(keyword, ignoreCase = true)
            }
            // 목록이 날짜 최신순이라 groupBy 결과도 최신 달부터
            .groupBy { YearMonth.from(it.entry.occurredAt.toLocalDateTime()) }
            .map { (month, items) ->
                MonthGroup(
                    month = month,
                    expense = items.filter { it.entry.type == EntryType.EXPENSE }.sumOf { it.entry.amount },
                    income = items.filter { it.entry.type == EntryType.INCOME }.sumOf { it.entry.amount },
                    entries = items,
                )
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
