package com.raye.donmoeuja.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raye.donmoeuja.data.AttachmentStore
import com.raye.donmoeuja.data.Entry
import com.raye.donmoeuja.data.EntryWithCategory
import com.raye.donmoeuja.data.MoneyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MoneyRepository,
) : ViewModel() {
    val entryId: Long = checkNotNull(savedStateHandle.get<Long>("entryId"))
    val attachments: AttachmentStore get() = repository.attachments

    val entry: StateFlow<EntryWithCategory?> =
        repository.entry(entryId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(entry: Entry, onDone: () -> Unit) = viewModelScope.launch {
        repository.deleteEntry(entry)
        onDone()
    }
}
