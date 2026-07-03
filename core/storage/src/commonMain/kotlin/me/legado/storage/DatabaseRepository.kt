package me.legado.storage

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.flow.Flow
import me.legado.core.Book
import me.legado.core.BookRepository
import me.legado.core.BookSource
import me.legado.core.Chapter
import me.legado.core.ChapterContent
import me.legado.db.LegadoDatabase

/**
 * KMP 数据库仓库实现
 */
class DatabaseRepository(private val driver: SqlDriver) : BookRepository {
    
    private val database = LegadoDatabase(driver)
    private val bookQueries = database.bookQueries
    private val chapterQueries = database.chapterQueries
    private val sourceQueries = database.bookSourcesQueries
    
    override fun getBooks(): Flow<List<Book>> {
        return bookQueries.selectBooks { 
            book_url, name, author, cover_url, intro, chapter_list_size, 
            last_chapter, last_chapter_time, word_count, can_update, order, 
            origin, origin_name, variable, read_config ->
            Book(
                bookUrl = book_url,
                name = name,
                author = author,
                coverUrl = cover_url,
                intro = intro,
                chapterListSize = chapter_list_size.toInt(),
                lastChapter = last_chapter,
                lastChapterTime = last_chapter_time,
                wordCount = word_count,
                canUpdate = can_update == 1L,
                order = order.toInt(),
                origin = origin,
                originName = origin_name,
                variable = variable
            )
        }.asFlow()
    }
    
    override suspend fun getBook(bookUrl: String): Book? {
        return bookQueries.selectBookByUrl(bookUrl) { 
            book_url, name, author, cover_url, intro, chapter_list_size, 
            last_chapter, last_chapter_time, word_count, can_update, order, 
            origin, origin_name, variable, read_config ->
            Book(
                bookUrl = book_url,
                name = name,
                author = author,
                coverUrl = cover_url,
                intro = intro,
                chapterListSize = chapter_list_size.toInt(),
                lastChapter = last_chapter,
                lastChapterTime = last_chapter_time,
                wordCount = word_count,
                canUpdate = can_update == 1L,
                order = order.toInt(),
                origin = origin,
                originName = origin_name,
                variable = variable
            )
        }.executeAsOneOrNull()
    }
    
    override suspend fun saveBook(book: Book) {
        bookQueries.insertBook(
            book_url = book.bookUrl,
            name = book.name,
            author = book.author,
            cover_url = book.coverUrl,
            intro = book.intro,
            chapter_list_size = book.chapterListSize.toLong(),
            last_chapter = book.lastChapter,
            last_chapter_time = book.lastChapterTime,
            word_count = book.wordCount,
            can_update = if (book.canUpdate) 1 else 0,
            order = book.order.toLong(),
            origin = book.origin,
            origin_name = book.originName,
            variable = book.variable,
            read_config = null
        )
    }
    
    override suspend fun deleteBook(bookUrl: String) {
        bookQueries.deleteBook(bookUrl)
        chapterQueries.deleteChaptersByBookUrl(bookUrl)
    }
    
    override suspend fun updateBook(book: Book) {
        saveBook(book)
    }
    
    override suspend fun getBookSource(sourceUrl: String): BookSource? {
        return sourceQueries.selectBookSourceByUrl(sourceUrl) { 
            book_source_url, book_source_name, book_source_group, login_url, 
            header, enabled, enabled_explore, search_weight, explore_url, 
            rule_search, rule_book_info, rule_toc, rule_content, rule_explore ->
            BookSource(
                bookSourceUrl = book_source_url,
                bookSourceName = book_source_name,
                bookSourceGroup = book_source_group,
                loginUrl = login_url,
                header = header,
                enabled = enabled == 1L,
                enabledExplore = enabled_explore == 1L,
                searchWeight = search_weight.toInt(),
                exploreUrl = explore_url,
                ruleSearch = null,
                ruleBookInfo = null,
                ruleToc = null,
                ruleContent = null,
                ruleExplore = null
            )
        }.executeAsOneOrNull()
    }
    
    override suspend fun saveBookSource(source: BookSource) {
        // TODO: Implement proper serialization for rules
        sourceQueries.insertBookSource(
            book_source_url = source.bookSourceUrl,
            book_source_name = source.bookSourceName,
            book_source_group = source.bookSourceGroup,
            login_url = source.loginUrl,
            header = source.header,
            enabled = if (source.enabled) 1 else 0,
            enabled_explore = if (source.enabledExplore) 1 else 0,
            search_weight = source.searchWeight.toLong(),
            explore_url = source.exploreUrl,
            rule_search = null,
            rule_book_info = null,
            rule_toc = null,
            rule_content = null,
            rule_explore = null
        )
    }
    
    override suspend fun getAllBookSources(): List<BookSource> {
        return sourceQueries.selectAllBookSources { 
            book_source_url, book_source_name, book_source_group, login_url, 
            header, enabled, enabled_explore, search_weight, explore_url, 
            rule_search, rule_book_info, rule_toc, rule_content, rule_explore ->
            BookSource(
                bookSourceUrl = book_source_url,
                bookSourceName = book_source_name,
                bookSourceGroup = book_source_group,
                loginUrl = login_url,
                header = header,
                enabled = enabled == 1L,
                enabledExplore = enabled_explore == 1L,
                searchWeight = search_weight.toInt(),
                exploreUrl = explore_url,
                ruleSearch = null,
                ruleBookInfo = null,
                ruleToc = null,
                ruleContent = null,
                ruleExplore = null
            )
        }.executeAsList()
    }
}
