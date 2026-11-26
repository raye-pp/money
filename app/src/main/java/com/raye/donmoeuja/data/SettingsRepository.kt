package com.raye.donmoeuja.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "home_settings")

data class HomeSettings(
    val start: LocalDate,
    val end: LocalDate,
    val budget: Long,
    val summaryCollapsed: Boolean,
)

/** 홈의 기간 · 예산 · 접힘 상태 (앱을 껐다 켜도 유지) */
class SettingsRepository(private val context: Context) {
    private object Keys {
        val START = longPreferencesKey("period_start")
        val END = longPreferencesKey("period_end")
        val BUDGET = longPreferencesKey("budget")
        val COLLAPSED = booleanPreferencesKey("summary_collapsed")
    }

    val settings: Flow<HomeSettings> = context.dataStore.data.map { p ->
        val today = LocalDate.now()
        HomeSettings(
            start = p[Keys.START]?.let(LocalDate::ofEpochDay) ?: today.withDayOfMonth(1),
            end = p[Keys.END]?.let(LocalDate::ofEpochDay) ?: today.withDayOfMonth(today.lengthOfMonth()),
            budget = p[Keys.BUDGET] ?: 0L,
            summaryCollapsed = p[Keys.COLLAPSED] ?: false,
        )
    }

    suspend fun setPeriod(start: LocalDate, end: LocalDate) {
        context.dataStore.edit {
            it[Keys.START] = start.toEpochDay()
            it[Keys.END] = end.toEpochDay()
        }
    }

    suspend fun setBudget(budget: Long) {
        context.dataStore.edit { it[Keys.BUDGET] = budget }
    }

    suspend fun setSummaryCollapsed(collapsed: Boolean) {
        context.dataStore.edit { it[Keys.COLLAPSED] = collapsed }
    }
}
