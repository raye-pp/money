package com.raye.donmoeuja.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raye.donmoeuja.data.CategoryTotal
import com.raye.donmoeuja.data.MoneyRepository
import com.raye.donmoeuja.data.PaymentTotal
import com.raye.donmoeuja.data.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class StatsUiState(
    val start: LocalDate,
    val end: LocalDate,
    val paymentTotals: List<PaymentTotal>,
    val categoryTotals: List<CategoryTotal>,
    val totalExpense: Long,
    val totalIncome: Long,
)

class StatsViewModel(
    private val repository: MoneyRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /** 통계 화면만의 기간. 처음엔 홈 기간으로 시작하고, 여기서 바꿔도 홈 기간은 그대로 */
    private val period = MutableStateFlow<Pair<LocalDate, LocalDate>?>(null)

    init {
        resetToHomePeriod()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatsUiState?> = period.filterNotNull()
        .flatMapLatest { (start, end) ->
            combine(
                repository.paymentTotals(start, end),
                repository.categoryTotals(start, end),
                repository.expenseSum(start, end),
                repository.incomeSum(start, end),
            ) { payments, categories, expense, income ->
                StatsUiState(start, end, payments, categories, expense, income)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setPeriod(start: LocalDate, end: LocalDate) {
        period.value = start to end
    }

    fun resetToHomePeriod() {
        viewModelScope.launch {
            val s = settingsRepository.settings.first()
            period.value = s.start to s.end
        }
    }
}
