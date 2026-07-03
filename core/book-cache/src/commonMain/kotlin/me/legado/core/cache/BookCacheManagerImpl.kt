package me.legado.core.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.legado.core.data.model.Chapter
import me.legado.core.data.repository.CacheRepository
import me.legado.core.storage.PlatformFileSystem
import kotlin.math.max

/**
 * 书籍缓存管理器实现
 */
class BookCacheManagerImpl(
    private val cacheRepository: CacheRepository,
    private val fileSystem: PlatformFileSystem,
    private val config: CacheConfig = CacheConfig()
) : BookCacheManager {

    private val mutex = Mutex()
    private val _cacheStats = MutableStateFlow(CacheStats(0, 0, 0, null, null))

    override fun observeCacheStats(): Flow<CacheStats> = _cacheStats.asStateFlow()

    override suspend fun preloadChapters(bookUrl: String, chapterUrls: List<String>) {
        // 预加载逻辑：可以批量获取并缓存章节内容
        // 实际实现需要配合网络请求和解析器
        chapterUrls.forEach { chapterUrl ->
            // 检查是否已缓存
            if (!isChapterCached(bookUrl, chapterUrl)) {
                // TODO: 从网络获取并缓存
            }
        }
    }

    override suspend fun getCachedChapter(bookUrl: String, chapterUrl: String): String? {
        return mutex.withLock {
            val cacheKey = generateCacheKey(bookUrl, chapterUrl)
            cacheRepository.selectCacheByKey(cacheKey)?.content
        }
    }

    override suspend fun isChapterCached(bookUrl: String, chapterUrl: String): Boolean {
        val cacheKey = generateCacheKey(bookUrl, chapterUrl)
        val cache = cacheRepository.selectCacheByKey(cacheKey)
        
        if (cache == null) return false
        
        // 检查是否过期
        val expireTime = cache.createdAt + config.cacheExpireDays * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() < expireTime
    }

    override suspend fun cacheChapter(bookUrl: String, chapter: Chapter, content: String) {
        mutex.withLock {
            val cacheKey = generateCacheKey(bookUrl, chapter.url)
            
            // 检查缓存大小限制
            if (config.enableAutoClear) {
                checkAndClearCacheIfNeeded()
            }
            
            // 插入或更新缓存
            cacheRepository.insertCache(
                key = cacheKey,
                value = content,
                bookUrl = bookUrl,
                chapterUrl = chapter.url,
                createdAt = System.currentTimeMillis()
            )
            
            // 更新统计信息
            updateCacheStats()
        }
    }

    override suspend fun clearBookCache(bookUrl: String) {
        mutex.withLock {
            cacheRepository.deleteCacheByBookUrl(bookUrl)
            updateCacheStats()
        }
    }

    override suspend fun clearAllCache() {
        mutex.withLock {
            cacheRepository.deleteAllCache()
            updateCacheStats()
        }
    }

    override suspend fun getCacheStats(): CacheStats {
        return updateCacheStats()
    }

    override suspend fun deleteExpiredCache() {
        mutex.withLock {
            val expireTime = System.currentTimeMillis() - config.cacheExpireDays * 24 * 60 * 60 * 1000
            cacheRepository.deleteExpiredCache(expireTime)
            updateCacheStats()
        }
    }

    /**
     * 生成缓存键
     */
    private fun generateCacheKey(bookUrl: String, chapterUrl: String): String {
        // 使用 MD5 或其他哈希算法生成唯一键
        val combined = "$bookUrl::$chapterUrl"
        return combined.hashCode().toString()
    }

    /**
     * 检查并清理缓存
     */
    private suspend fun checkAndClearCacheIfNeeded() {
        val stats = getCacheStats()
        
        // 如果超过最大缓存大小，删除最旧的缓存
        if (stats.totalSizeMB > config.maxCacheSizeMB) {
            // 删除最旧的 10% 缓存
            cacheRepository.deleteOldestCache(stats.chapterCount / 10)
        }
    }

    /**
     * 更新缓存统计信息
     */
    private suspend fun updateCacheStats(): CacheStats {
        val allCaches = cacheRepository.selectAllCache()
        
        val stats = if (allCaches.isEmpty()) {
            CacheStats(0, 0, 0, null, null)
        } else {
            val totalSize = allCaches.sumOf { it.value.length.toLong() * 2 } // 估算 UTF-16 大小
            val bookCount = allCaches.map { it.bookUrl }.distinct().size
            val oldestTime = allCaches.minOfOrNull { it.createdAt }
            val newestTime = allCaches.maxOfOrNull { it.createdAt }
            
            CacheStats(
                totalSize = totalSize,
                chapterCount = allCaches.size,
                bookCount = bookCount,
                oldestCacheTime = oldestTime,
                newestCacheTime = newestTime
            )
        }
        
        _cacheStats.value = stats
        return stats
    }

    /**
     * 获取缓存的章节列表
     */
    suspend fun getCachedChapters(bookUrl: String): List<String> {
        return cacheRepository.selectCachesByBookUrl(bookUrl)
            .map { it.chapterUrl }
            .distinct()
    }

    /**
     * 批量缓存章节
     */
    suspend fun batchCacheChapters(
        bookUrl: String,
        chapters: List<Chapter>,
        contents: Map<String, String>
    ) {
        mutex.withLock {
            chapters.forEach { chapter ->
                contents[chapter.url]?.let { content ->
                    cacheChapter(bookUrl, chapter, content)
                }
            }
        }
    }

    /**
     * 导出缓存到文件
     */
    suspend fun exportCache(bookUrl: String, outputPath: String): Result<Unit> {
        return runCatching {
            val cachedChapters = getCachedChapters(bookUrl)
            val sb = StringBuilder()
            
            cachedChapters.forEach { chapterUrl ->
                getCachedChapter(bookUrl, chapterUrl)?.let { content ->
                    sb.appendLine(content)
                    sb.appendLine()
                }
            }
            
            fileSystem.writeFile(outputPath, sb.toString())
        }
    }

    /**
     * 从文件导入缓存
     */
    suspend fun importCache(bookUrl: String, inputPath: String): Result<Int> {
        return runCatching {
            val content = fileSystem.readFile(inputPath)
            // 简化实现：将整个文件作为一个章节缓存
            val chapter = Chapter(
                url = "$bookUrl::imported",
                title = "导入的章节",
                index = 0,
                bookUrl = bookUrl,
                isVip = false,
                isPaid = false
            )
            cacheChapter(bookUrl, chapter, content)
            1
        }
    }
}
