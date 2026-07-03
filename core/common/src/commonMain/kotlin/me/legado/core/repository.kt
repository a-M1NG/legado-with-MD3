package me.legado.core

import kotlinx.coroutines.flow.Flow

/**
 * 书籍仓库接口 - KMP 共享代码
 */
interface BookRepository {
    fun getBooks(): Flow<List<Book>>
    suspend fun getBook(bookUrl: String): Book?
    suspend fun saveBook(book: Book)
    suspend fun deleteBook(bookUrl: String)
    suspend fun updateBook(book: Book)
    suspend fun getBookSource(sourceUrl: String): BookSource?
    suspend fun saveBookSource(source: BookSource)
    suspend fun getAllBookSources(): List<BookSource>
}

/**
 * 章节仓库接口
 */
interface ChapterRepository {
    suspend fun getChapterList(bookUrl: String): List<Chapter>
    suspend fun getChapter(bookUrl: String, chapterUrl: String): ChapterContent?
    suspend fun saveChapter(bookUrl: String, chapter: ChapterContent)
    suspend fun clearCache(bookUrl: String)
}

/**
 * 章节内容
 */
data class ChapterContent(
    val title: String,
    val content: String,
    val nextContentUrl: String? = null,
    val isVip: Boolean = false,
    val payAction: String? = null
)
