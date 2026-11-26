package com.raye.donmoeuja.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** 첨부 파일을 앱 내부 저장소(files/attachments)에 복사해서 보관 */
class AttachmentStore(private val context: Context) {

    data class Saved(val fileName: String, val displayName: String, val mime: String?)

    private val dir: File
        get() = File(context.filesDir, "attachments").apply { mkdirs() }

    fun fileOf(fileName: String): File = File(dir, fileName)

    fun displayName(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0)
                    if (!name.isNullOrBlank()) return name
                }
            }
        return uri.lastPathSegment ?: "첨부파일"
    }

    fun mimeOf(uri: Uri): String? = context.contentResolver.getType(uri)

    suspend fun import(uri: Uri): Saved = withContext(Dispatchers.IO) {
        val name = displayName(uri)
        val mime = mimeOf(uri)
        val safeName = name.replace(Regex("[^\\p{L}\\p{N}._-]"), "_").takeLast(80)
        val fileName = "${UUID.randomUUID()}_$safeName"
        val target = fileOf(fileName)
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("파일을 열 수 없어요")
        input.use { source -> target.outputStream().use { source.copyTo(it) } }
        Saved(fileName, name, mime)
    }

    suspend fun delete(fileName: String?) {
        if (fileName == null) return
        withContext(Dispatchers.IO) { fileOf(fileName).delete() }
    }

    fun shareUri(fileName: String): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fileOf(fileName))
}
