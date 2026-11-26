package com.raye.donmoeuja.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raye.donmoeuja.data.Category
import com.raye.donmoeuja.data.CategoryResult
import com.raye.donmoeuja.data.MoneyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: MoneyRepository) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String, onResult: (CategoryResult) -> Unit) = viewModelScope.launch {
        onResult(repository.addCategory(name))
    }

    fun rename(category: Category, name: String, onResult: (CategoryResult) -> Unit) = viewModelScope.launch {
        onResult(repository.renameCategory(category, name))
    }

    suspend fun usageCount(category: Category): Int = repository.usageCount(category)

    fun delete(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}
