package com.raye.donmoeuja.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EntryType(val label: String) { EXPENSE("지출"), INCOME("수입") }

enum class PaymentMethod(val label: String) { CASH("현금"), CARD("카드") }

@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
)

/**
 * 지출/수입 한 건.
 * - occurredAt: 사용자가 정한 날짜·시간 (기간 필터 기준)
 * - createdAt: 처음 저장한 시각 (목록 정렬 기준)
 * - categoryId: 카테고리를 삭제하면 null → '미분류'로 표시
 * - attachmentFile: 앱 내부 저장소(files/attachments)에 복사된 파일 이름
 */
@Entity(
    tableName = "entries",
    indices = [Index("categoryId"), Index("occurredAt"), Index("createdAt")],
)
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurredAt: Long,
    val type: EntryType,
    val paymentMethod: PaymentMethod?,
    val amount: Long,
    val categoryId: Long?,
    val summary: String,
    val detail: String,
    val attachmentFile: String?,
    val attachmentName: String?,
    val attachmentMime: String?,
    val createdAt: Long,
)

data class EntryWithCategory(
    @Embedded val entry: Entry,
    val categoryName: String?,
)

data class PaymentTotal(
    val paymentMethod: PaymentMethod?,
    val total: Long,
)

data class CategoryTotal(
    val categoryName: String?,
    val total: Long,
)
