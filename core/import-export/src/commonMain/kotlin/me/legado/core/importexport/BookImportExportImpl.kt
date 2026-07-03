package me.legado.core.importexport

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.legado.core.data.model.Book
import me.legado.core.data.repository.BookRepository
import me.legado.core.data.repository.BookSourceRepository
import me.legado.core.storage.PlatformFileSystem

/**
 * 书籍导入导出实现
 */
class BookImportExportImpl(
    private val bookRepository: BookRepository,
    private val bookSourceRepository: BookSourceRepository,
    private val fileSystem: PlatformFileSystem,
    private val json: Json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }
) : BookImportExport {

    override suspend fun exportBooks(books: List<Book>): String {
        val exportDtos = books.map { book ->
            BookExportDto(
                bookUrl = book.bookUrl,
                title = book.title,
                author = book.author,
                coverUrl = book.coverUrl,
                intro = book.intro,
                chapterCount = book.chapterCount,
                lastChapter = book.lastChapter,
                lastReadTime = book.lastReadTime,
                readConfig = book.readConfig?.toExportDto()
            )
        }
        return json.encodeToString(exportDtos)
    }

    override suspend fun importBooks(jsonStr: String): Result<List<Book>> {
        return runCatching {
            val dtos = json.decodeFromString<List<BookExportDto>>(jsonStr)
            val books = dtos.mapNotNull { dto ->
                dto.toBook()
            }
            
            // 批量插入书籍
            books.forEach { book ->
                bookRepository.insertBook(book)
            }
            
            books
        }
    }

    override suspend fun exportBookSources(sources: List<Any>): String {
        // 简化实现：直接序列化
        return json.encodeToString(sources)
    }

    override suspend fun importBookSources(jsonStr: String): Result<List<Any>> {
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            json.decodeFromString<List<Map<String, Any>>>(jsonStr) as List<Any>
        }
    }

    override suspend fun backupAll(): BackupData {
        val books = bookRepository.selectAllBooks()
        val bookSourcesJson = exportBookSources(emptyList()) // TODO: 获取实际书源
        
        return BackupData(
            books = books.map { it.toExportDto() },
            bookSources = bookSourcesJson,
            version = "1.0",
            timestamp = System.currentTimeMillis()
        )
    }

    override suspend fun restoreAll(backupData: BackupData): Result<Unit> {
        return runCatching {
            // 恢复书籍
            backupData.books.forEach { dto ->
                dto.toBook()?.let { book ->
                    bookRepository.insertBook(book)
                }
            }
            
            // 恢复书源
            if (backupData.bookSources.isNotEmpty()) {
                importBookSources(backupData.bookSources)
            }
        }
    }

    /**
     * 从文件导入书籍
     */
    suspend fun importFromFile(filePath: String): ImportResult {
        return runCatching {
            val content = fileSystem.readFile(filePath)
            val result = importBooks(content)
            result.fold(
                onSuccess = { books -> ImportResult.Success(books.size) },
                onFailure = { error -> ImportResult.Error(error.message ?: "未知错误") }
            )
        }.getOrElse { 
            ImportResult.Error(it.message ?: "读取文件失败") 
        }
    }

    /**
     * 导出到文件
     */
    suspend fun exportToFile(books: List<Book>, filePath: String): Result<Unit> {
        return runCatching {
            val jsonContent = exportBooks(books)
            fileSystem.writeFile(filePath, jsonContent)
        }
    }
}

/**
 * 扩展函数：Book -> BookExportDto
 */
private fun Book.toExportDto(): BookExportDto {
    return BookExportDto(
        bookUrl = this.bookUrl,
        title = this.title,
        author = this.author,
        coverUrl = this.coverUrl,
        intro = this.intro,
        chapterCount = this.chapterCount,
        lastChapter = this.lastChapter,
        lastReadTime = this.lastReadTime,
        readConfig = this.readConfig?.toExportDto()
    )
}

/**
 * 扩展函数：BookExportDto -> Book
 */
private fun BookExportDto.toBook(): Book? {
    return try {
        Book(
            bookUrl = bookUrl,
            title = title,
            author = author,
            coverUrl = coverUrl,
            intro = intro,
            chapterCount = chapterCount,
            lastChapter = lastChapter,
            lastReadTime = lastReadTime,
            readConfig = readConfig?.toReadConfig()
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * 扩展函数：ReadConfig -> ReadConfigExportDto
 */
// private fun ReadConfig.toExportDto(): ReadConfigExportDto {
//     return ReadConfigExportDto(
//         theme = this.theme,
//         fontSize = this.fontSize,
//         lineSpacing = this.lineSpacing,
//         paragraphSpacing = this.paragraphSpacing,
//         pageAnim = this.pageAnim,
//         showTitle = this.showTitle,
//         hideStatusBar = this.hideStatusBar
//     )
// }

/**
 * 扩展函数：ReadConfigExportDto -> ReadConfig
 */
// private fun ReadConfigExportDto.toReadConfig(): ReadConfig {
//     return ReadConfig(
//         theme = this.theme,
//         fontSize = this.fontSize,
//         lineSpacing = this.lineSpacing,
//         paragraphSpacing = this.paragraphSpacing,
//         pageAnim = this.pageAnim,
//         showTitle = this.showTitle,
//         hideStatusBar = this.hideStatusBar
//     )
// }
