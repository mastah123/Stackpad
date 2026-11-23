package com.stackpad.app.data.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.stackpad.app.domain.backup.AutoBackupRules
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** Writes one backup zip into the folder the user picked and removes old automatic backups. */
class AutoBackupWriter(
    private val context: Context,
    private val backup: BackupManager,
) {
    /** True when today's backup was written. Never throws (a lost folder permission just returns false). */
    suspend fun run(folder: Uri, today: LocalDate = LocalDate.now()): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val treeId = DocumentsContract.getTreeDocumentId(folder)
            val parent = DocumentsContract.buildDocumentUriUsingTree(folder, treeId)
            val existing = listFiles(resolver, folder, treeId)

            val name = AutoBackupRules.fileName(today)
            existing[name]?.let { DocumentsContract.deleteDocument(resolver, it) }
            val target = DocumentsContract.createDocument(resolver, parent, "application/zip", name)
                ?: return@withContext false
            val out = resolver.openOutputStream(target, "w") ?: return@withContext false
            out.use { backup.export(it) }

            val others = existing - name
            for (old in AutoBackupRules.toDelete(others.keys.toList() + name)) {
                others[old]?.let { DocumentsContract.deleteDocument(resolver, it) }
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    /** File name to document address, for the files directly inside the picked folder. */
    private fun listFiles(resolver: ContentResolver, folder: Uri, treeId: String): Map<String, Uri> {
        val result = LinkedHashMap<String, Uri>()
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(folder, treeId)
        val columns = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        )
        resolver.query(children, columns, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0) ?: continue
                val name = cursor.getString(1) ?: continue
                result[name] = DocumentsContract.buildDocumentUriUsingTree(folder, id)
            }
        }
        return result
    }
}
