package com.raye.donmoeuja.ui.edit

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raye.donmoeuja.data.Category
import com.raye.donmoeuja.data.Entry
import com.raye.donmoeuja.data.EntryType
import com.raye.donmoeuja.data.MoneyRepository
import com.raye.donmoeuja.data.PaymentMethod
import com.raye.donmoeuja.ui.sanitizeAmount
import com.raye.donmoeuja.ui.toLocalDateTime
import com.raye.donmoeuja.ui.toMillis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

sealed interface AttachmentState {
    val displayName: String
    val mime: String?

    /** 이미 저장된 첨부 */
    data class Existing(val fileName: String, override val displayName: String, override val mime: String?) :
        AttachmentState

    /** 이번에 새로 고른 첨부 (저장할 때 앱 내부로 복사) */
    data class Picked(val uri: Uri, override val displayName: String, override val mime: String?) :
        AttachmentState
}

class EditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MoneyRepository,
) : ViewModel() {

    private val entryId: Long = savedStateHandle.get<Long>("entryId") ?: -1L
    val isEdit: Boolean = entryId > 0
    private var original: Entry? = null

    // 날짜: 화면을 연 시각으로 자동 입력
    var occurredAt by mutableStateOf(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))
    var type by mutableStateOf(EntryType.EXPENSE)
    var payment by mutableStateOf(PaymentMethod.CASH)
    var amountText by mutableStateOf("")
        private set
    var categoryId by mutableStateOf<Long?>(null)
    var summary by mutableStateOf("")
        private set
    var detail by mutableStateOf("")
        private set
    var attachment by mutableStateOf<AttachmentState?>(null)
    var showErrors by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    val categories: StateFlow<List<Category>> =
        repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (isEdit) {
            viewModelScope.launch {
                repository.getEntry(entryId)?.let { e ->
                    original = e
                    occurredAt = e.occurredAt.toLocalDateTime()
                    type = e.type
                    payment = e.paymentMethod ?: PaymentMethod.CASH
                    amountText = e.amount.toString()
                    categoryId = e.categoryId
                    summary = e.summary
                    detail = e.detail
                    attachment = e.attachmentFile?.let {
                        AttachmentState.Existing(it, e.attachmentName ?: "첨부파일", e.attachmentMime)
                    }
                }
            }
        }
    }

    val amount: Long get() = amountText.toLongOrNull() ?: 0L
    val amountError: Boolean get() = amount <= 0L
    val categoryError: Boolean get() = categoryId == null

    fun onAmountChange(value: String) {
        amountText = sanitizeAmount(value)
    }

    fun onSummaryChange(value: String) {
        summary = value.replace("\n", "").take(MAX_SUMMARY)
    }

    fun onDetailChange(value: String) {
        detail = value.take(MAX_DETAIL)
    }

    fun pickAttachment(uri: Uri) {
        attachment = AttachmentState.Picked(
            uri = uri,
            displayName = repository.attachments.displayName(uri),
            mime = repository.attachments.mimeOf(uri),
        )
    }

    fun save(onSaved: () -> Unit) {
        // 필수: 구분(항상 선택됨) · 금액 · 카테고리
        val validCategory = categoryId?.takeIf { id -> categories.value.any { it.id == id } }
        if (amountError || validCategory == null) {
            if (validCategory == null) categoryId = null
            showErrors = true
            return
        }
        if (saving) return
        saving = true
        viewModelScope.launch {
            try {
                val old = original
                var fileName = old?.attachmentFile
                var name = old?.attachmentName
                var mime = old?.attachmentMime
                var fileToDelete: String? = null

                when (val a = attachment) {
                    null -> {
                        fileToDelete = fileName
                        fileName = null; name = null; mime = null
                    }
                    is AttachmentState.Existing -> Unit
                    is AttachmentState.Picked -> {
                        val saved = repository.attachments.import(a.uri)
                        fileToDelete = fileName
                        fileName = saved.fileName; name = saved.displayName; mime = saved.mime
                    }
                }

                val entry = Entry(
                    id = old?.id ?: 0L,
                    occurredAt = occurredAt.toMillis(),
                    type = type,
                    paymentMethod = if (type == EntryType.EXPENSE) payment else null,
                    amount = amount,
                    categoryId = validCategory,
                    summary = summary.trim(),
                    detail = detail.trim(),
                    attachmentFile = fileName,
                    attachmentName = name,
                    attachmentMime = mime,
                    createdAt = old?.createdAt ?: System.currentTimeMillis(),
                )
                repository.saveEntry(entry)
                repository.attachments.delete(fileToDelete)
                onSaved()
            } catch (e: Exception) {
                errorMessage = "저장하지 못했어요: ${e.message ?: "알 수 없는 오류"}"
            } finally {
                saving = false
            }
        }
    }

    companion object {
        const val MAX_SUMMARY = 15
        const val MAX_DETAIL = 200
    }
}
