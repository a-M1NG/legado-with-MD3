package me.legado.storage

import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.legado.core.Book
import me.legado.core.BookRepository
import me.legado.core.BookSource
import me.legado.core.Chapter
import me.legado.db.LegadoDatabase

/**
 * KMP 章节数据仓库实现
 */
class ChapterRepositoryImpl(private val driver: SqlDriver) {
    
    private val database = LegadoDatabase(driver)
    private val chapterQueries = database.chapterQueries
    
    /**
     * 获取书籍所有章节
     */
    fun getChaptersByBookUrl(bookUrl: String): Flow<List<Chapter>> {
        return chapterQueries.selectChaptersByBookUrl(bookUrl) { url, title, index, isVolume, resourceUrl, tag ->
            Chapter(
                url = url,
                title = title,
                index = index.toInt(),
                isVolume = isVolume == 1L,
                resourceUrl = resourceUrl,
                tag = tag
            )
        }.asFlow()
    }
    
    /**
     * 保存单个章节
     */
    suspend fun saveChapter(bookUrl: String, chapter: Chapter) {
        chapterQueries.insertChapter(
            book_url = bookUrl,
            url = chapter.url,
            title = chapter.title,
            index = chapter.index.toLong(),
            is_volume = if (chapter.isVolume) 1 else 0,
            resource_url = chapter.resourceUrl,
            tag = chapter.tag
        )
    }
    
    /**
     * 批量保存章节
     */
    suspend fun saveChapters(bookUrl: String, chapters: List<Chapter>) {
        database.transaction {
            chapters.forEach { chapter ->
                saveChapter(bookUrl, chapter)
            }
        }
    }
    
    /**
     * 删除书籍的所有章节
     */
    suspend fun deleteChaptersByBookUrl(bookUrl: String) {
        chapterQueries.deleteChaptersByBookUrl(bookUrl)
    }
    
    /**
     * 获取指定索引的章节
     */
    suspend fun getChapterByIndex(bookUrl: String, index: Int): Chapter? {
        return chapterQueries.selectChapterByIndex(bookUrl, index.toLong()) { url, title, idx, isVolume, resourceUrl, tag ->
            Chapter(
                url = url,
                title = title,
                index = idx.toInt(),
                isVolume = isVolume == 1L,
                resourceUrl = resourceUrl,
                tag = tag
            )
        }.executeAsOneOrNull()
    }
    
    /**
     * 获取章节总数
     */
    suspend fun getChapterCount(bookUrl: String): Int {
        return chapterQueries.countChaptersByBookUrl(bookUrl).executeAsOne().toInt()
    }
}

/**
 * 缓存数据仓库
 */
class CacheRepositoryImpl(private val driver: SqlDriver) {
    
    private val database = LegadoDatabase(driver)
    private val cacheQueries = database.cacheQueries
    
    /**
     * 获取缓存值
     */
    suspend fun get(key: String): String? {
        return cacheQueries.selectCacheByKey(key).executeAsOneOrNull()
    }
    
    /**
     * 设置缓存 (永久有效)
     */
    suspend fun set(key: String, value: String) {
        cacheQueries.insertCache(key, value, 0)
    }
    
    /**
     * 设置缓存 (带过期时间)
     */
    suspend fun setWithExpiry(key: String, value: String, expireTimeMillis: Long) {
        cacheQueries.insertCache(key, value, expireTimeMillis)
    }
    
    /**
     * 删除缓存
     */
    suspend fun delete(key: String) {
        cacheQueries.deleteCache(key)
    }
    
    /**
     * 清理所有过期缓存
     */
    suspend fun clearExpired() {
        cacheQueries.deleteExpiredCache()
    }
    
    /**
     * 清空所有缓存
     */
    suspend fun clearAll() {
        cacheQueries.deleteAllCache()
    }
}

/**
 * 书源数据仓库扩展
 */
class BookSourceRepositoryImpl(private val driver: SqlDriver) {
    
    private val database = LegadoDatabase(driver)
    private val sourceQueries = database.bookSourcesQueries
    
    /**
     * 获取所有启用的书源
     */
    fun getEnabledSources(): Flow<List<BookSource>> {
        return sourceQueries.selectEnabledBookSources { url, name, group, loginUrl, header, enabled, enabledExplore, searchWeight, exploreUrl, ruleSearch, ruleBookInfo, ruleToc, ruleContent, ruleExplore ->
            BookSource(
                bookSourceUrl = url,
                bookSourceName = name,
                bookSourceGroup = group,
                loginUrl = loginUrl,
                header = header,
                enabled = enabled == 1L,
                enabledExplore = enabledExplore == 1L,
                searchWeight = searchWeight.toInt(),
                exploreUrl = exploreUrl,
                ruleSearch = null,
                ruleBookInfo = null,
                ruleToc = null,
                ruleContent = null,
                ruleExplore = null
            )
        }.asFlow()
    }
    
    /**
     * 按组获取书源
     */
    fun getSourcesByGroup(group: String): Flow<List<BookSource>> {
        return sourceQueries.selectBookSourcesByGroup(group) { url, name, groupCol, loginUrl, header, enabled, enabledExplore, searchWeight, exploreUrl, ruleSearch, ruleBookInfo, ruleToc, ruleContent, ruleExplore ->
            BookSource(
                bookSourceUrl = url,
                bookSourceName = name,
                bookSourceGroup = groupCol,
                loginUrl = loginUrl,
                header = header,
                enabled = enabled == 1L,
                enabledExplore = enabledExplore == 1L,
                searchWeight = searchWeight.toInt(),
                exploreUrl = exploreUrl,
                ruleSearch = null,
                ruleBookInfo = null,
                ruleToc = null,
                ruleContent = null,
                ruleExplore = null
            )
        }.asFlow()
    }
    
    /**
     * 启用/禁用书源
     */
    suspend fun toggleSource(sourceUrl: String, enabled: Boolean) {
        sourceQueries.updateSourceEnabled(if (enabled) 1 else 0, sourceUrl)
    }
    
    /**
     * 删除书源
     */
    suspend fun deleteSource(sourceUrl: String) {
        sourceQueries.deleteBookSource(sourceUrl)
    }
}

// 辅助查询扩展
private fun app.cash.sqldelight.Query<Long>.countAsFlow(): Flow<Int> {
    return this.asFlow().map { it.executeAsOne().toInt() }
}
