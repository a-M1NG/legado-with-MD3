package me.legado.core.cache

import kotlinx.coroutines.flow.Flow
import me.legado.core.data.model.Chapter

/**
 * 书籍缓存管理器接口
 */
interface BookCacheManager {
    /**
     * 预加载章节内容
     */
    suspend fun preloadChapters(bookUrl: String, chapterUrls: List<String>)
    
    /**
     * 获取缓存的章节内容
     */
    suspend fun getCachedChapter(bookUrl: String, chapterUrl: String): String?
    
    /**
     * 检查章节是否已缓存
     */
    suspend fun isChapterCached(bookUrl: String, chapterUrl: String): Boolean
    
    /**
     * 缓存章节内容
     */
    suspend fun cacheChapter(bookUrl: String, chapter: Chapter, content: String)
    
    /**
     * 清除书籍的所有缓存
     */
    suspend fun clearBookCache(bookUrl: String)
    
    /**
     * 清除所有缓存
     */
    suspend fun clearAllCache()
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStats(): CacheStats
    
    /**
     * 删除过期缓存
     */
    suspend fun deleteExpiredCache()
    
    /**
     * 监听缓存变化
     */
    fun observeCacheStats(): Flow<CacheStats>
}

/**
 * 缓存统计信息
 */
data class CacheStats(
    val totalSize: Long,           // 总大小 (字节)
    val chapterCount: Int,         // 缓存章节数
    val bookCount: Int,            // 缓存书籍数
    val oldestCacheTime: Long?,    // 最早缓存时间
    val newestCacheTime: Long?     // 最新缓存时间
) {
    val totalSizeMB: Double get() = totalSize / 1024.0 / 1024.0
    val totalSizeGB: Double get() = totalSizeMB / 1024.0
}

/**
 * 缓存配置
 */
data class CacheConfig(
    val maxCacheSizeMB: Long = 500,        // 最大缓存大小 (MB)
    val cacheExpireDays: Long = 30,        // 缓存过期天数
    val autoPreloadCount: Int = 3,         // 自动预加载章节数
    val enableWifiOnly: Boolean = false,   // 仅 WiFi 下缓存
    val enableAutoClear: Boolean = true    // 启用自动清理
)

/**
 * 缓存策略
 */
enum class CacheStrategy {
    CACHE_FIRST,      // 优先使用缓存
    NETWORK_FIRST,    // 优先使用网络
    CACHE_ONLY,       // 仅使用缓存
    NETWORK_ONLY      // 仅使用网络
}

/**
 * 预加载任务
 */
data class PreloadTask(
    val bookUrl: String,
    val chapterUrls: List<String>,
    val priority: Priority = Priority.NORMAL,
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class Priority {
        LOW, NORMAL, HIGH
    }
}

/**
 * 缓存结果
 */
sealed class CacheResult {
    data class Hit(val content: String, val fromCache: Boolean = true) : CacheResult()
    data class Miss(val reason: String) : CacheResult()
    data class Error(val exception: Exception) : CacheResult()
}
