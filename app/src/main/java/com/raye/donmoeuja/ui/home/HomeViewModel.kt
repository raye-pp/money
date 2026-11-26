package com.raye.donmoeuja.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raye.donmoeuja.data.Entry
import com.raye.donmoeuja.data.EntryType
import com.raye.donmoeuja.data.EntryWithCategory
import com.raye.donmoeuja.data.HomeSettings
import com.raye.donmoeuja.data.MoneyRepository
import com.raye.donmoeuja.data.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val settings: HomeSettings,
    val entries: List<EntryWithCategory>,
    val totalExpense: Long,
    val totalIncome: Long,
) {
    /** 남은 예산 = 예산 − 총지출 + 추가수입 */
    val remaining: Long get() = settings.budget - totalExpense + totalIncome
}

class HomeViewModel(
    private val repository: MoneyRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState?> = settingsRepository.settings
        .flatMapLatest { settings ->
            // 홈 목록은 설정한 기간 안의 내역만
            repository.entriesInPeriod(settings.start, settings.end).map { list ->
                HomeUiState(
                    settings = settings,
                    entries = list,
                    totalExpense = list.filter { it.entry.type == EntryType.EXPENSE }.sumOf { it.entry.amount },
                    totalIncome = list.filter { it.entry.type == EntryType.INCOME }.sumOf { it.entry.amount },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setPeriod(start: LocalDate, end: LocalDate) = viewModelScope.launch {
        settingsRepository.setPeriod(start, end)
    }

    fun setBudget(budget: Long) = viewModelScope.launch {
        settingsRepository.setBudget(budget)
    }

    fun setCollapsed(collapsed: Boolean) = viewModelScope.launch {
        settingsRepository.setSummaryCollapsed(collapsed)
    }

    fun delete(entry: Entry) = viewModelScope.launch {
        repository.deleteEntry(entry)
    }
}
