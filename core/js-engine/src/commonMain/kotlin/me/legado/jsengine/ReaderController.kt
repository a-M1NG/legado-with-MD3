package me.legado.jsengine

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 阅读控制器 - 管理阅读状态和翻页逻辑
 */
class ReaderController {
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    
    private val _content = MutableStateFlow<List<String>>(emptyList())
    val content: StateFlow<List<String>> = _content.asStateFlow()
    
    var bookUrl: String? = null
    var chapterUrl: String? = null
    
    fun loadChapter(content: List<String>) {
        _content.value = content
        _currentPage.value = 0
    }
    
    fun nextPage(pageSize: Int): Boolean {
        val maxPage = (_content.value.size - 1) / pageSize
        return if (_currentPage.value < maxPage) {
            _currentPage.value++
            true
        } else {
            false
        }
    }
    
    fun prevPage(): Boolean {
        return if (_currentPage.value > 0) {
            _currentPage.value--
            true
        } else {
            false
        }
    }
    
    fun jumpToPage(page: Int, pageSize: Int): Boolean {
        val maxPage = (_content.value.size - 1) / pageSize
        val targetPage = page.coerceIn(0, maxPage)
        if (targetPage != _currentPage.value) {
            _currentPage.value = targetPage
            return true
        }
        return false
    }
}

/**
 * 阅读页面内容分页
 */
fun List<String>.paginate(pageSize: Int): List<List<String>> {
    return chunked(pageSize)
}

/**
 * 阅读主题配置
 */
data class ReaderTheme(
    val backgroundColor: String,
    val textColor: String,
    val fontSize: Float,
    val lineSpacing: Float,
    val paragraphSpacing: Float
) {
    companion object {
        val LIGHT = ReaderTheme(
            backgroundColor = "#FFFFFF",
            textColor = "#000000",
            fontSize = 16f,
            lineSpacing = 1.5f,
            paragraphSpacing = 1.0f
        )
        
        val DARK = ReaderTheme(
            backgroundColor = "#1A1A1A",
            textColor = "#CCCCCC",
            fontSize = 16f,
            lineSpacing = 1.5f,
            paragraphSpacing = 1.0f
        )
        
        val EYE_PROTECTION = ReaderTheme(
            backgroundColor = "#CCE8CF",
            textColor = "#333333",
            fontSize = 16f,
            lineSpacing = 1.5f,
            paragraphSpacing = 1.0f
        )
    }
}
