package com.raye.donmoeuja.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val numberFormat = NumberFormat.getNumberInstance(Locale.KOREA)

fun formatNumber(value: Long): String = numberFormat.format(value)
fun formatWon(value: Long): String = "${formatNumber(value)}원"

private val shortDate = DateTimeFormatter.ofPattern("MM/dd")
private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val fullDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun LocalDate.short(): String = format(shortDate)
fun LocalDate.full(): String = format(fullDate)
fun LocalDateTime.full(): String = format(fullDateTime)

fun Long.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun LocalDateTime.toMillis(): Long = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Material 날짜 선택기는 UTC 자정 기준 밀리초를 사용 */
fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
fun Long.fromPickerMillis(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

const val MAX_AMOUNT_DIGITS = 12

/** 숫자만 남기고 앞의 0 제거, 최대 자릿수 제한 */
fun sanitizeAmount(input: String): String =
    input.filter { it.isDigit() }.trimStart('0').take(MAX_AMOUNT_DIGITS)

/** 입력 중인 금액에 1,000 단위 쉼표 표시 (값 자체는 숫자만) */
object ThousandsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val formatted = digits.toLong().let(::formatNumber)
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // 원본 offset 앞에 들어가는 쉼표 개수만큼 이동
                val commasBefore = ((digits.length - 1) / 3) - ((digits.length - offset - 1).coerceAtLeast(0) / 3)
                return (offset + if (offset == 0) 0 else commasBefore).coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val digitsBefore = formatted.take(offset).count { it.isDigit() }
                return digitsBefore.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
