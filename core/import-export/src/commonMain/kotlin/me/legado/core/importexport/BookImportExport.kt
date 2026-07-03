package me.legado.core.importexport

import kotlinx.serialization.Serializable
import me.legado.core.data.model.Book
import me.legado.core.data.model.Chapter

/**
 * 书籍导入导出管理器
 */
interface BookImportExport {
    /**
     * 导出书籍到 JSON 字符串
     */
    suspend fun exportBooks(books: List<Book>): String
    
    /**
     * 从 JSON 字符串导入书籍
     */
    suspend fun importBooks(json: String): Result<List<Book>>
    
    /**
     * 导出书源到 JSON 字符串
     */
    suspend fun exportBookSources(sources: List<Any>): String
    
    /**
     * 从 JSON 字符串导入书源
     */
    suspend fun importBookSources(json: String): Result<List<Any>>
    
    /**
     * 备份所有数据
     */
    suspend fun backupAll(): BackupData
    
    /**
     * 恢复所有数据
     */
    suspend fun restoreAll(backupData: BackupData): Result<Unit>
}

/**
 * 备份数据结构
 */
@Serializable
data class BackupData(
    val books: List<BookExportDto> = emptyList(),
    val bookSources: String = "",
    val readConfig: String? = null,
    val version: String = "1.0",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 书籍导出 DTO
 */
@Serializable
data class BookExportDto(
    val bookUrl: String,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val intro: String?,
    val chapterCount: Int,
    val lastChapter: String?,
    val lastReadTime: Long?,
    val readConfig: ReadConfigExportDto?
)

/**
 * 阅读配置导出 DTO
 */
@Serializable
data class ReadConfigExportDto(
    val theme: Int,
    val fontSize: Int,
    val lineSpacing: Float,
    val paragraphSpacing: Float,
    val pageAnim: Int,
    val showTitle: Boolean,
    val hideStatusBar: Boolean
)

/**
 * 导入结果
 */
sealed class ImportResult {
    data class Success(val count: Int, val skipped: Int = 0) : ImportResult()
    data class Error(val message: String, val failedItems: List<String> = emptyList()) : ImportResult()
}

/**
 * 文件类型枚举
 */
enum class ExportFileType {
    BOOK_SINGLE,      // 单本书籍
    BOOK_BATCH,       // 批量书籍
    BOOK_SOURCE,      // 书源
    BACKUP_ALL,       // 完整备份
    LEGACY_TXT        // 旧版 TXT 格式
}
