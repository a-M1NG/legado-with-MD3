package me.legado.jsengine

import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import me.legado.core.Book
import me.legado.core.BookSource
import me.legado.core.Chapter
import me.legado.core.RuleBookInfo
import me.legado.core.RuleContent
import me.legado.core.RuleSearch
import me.legado.core.RuleToc

/**
 * CSS 选择器解析器 - 用于解析 HTML 内容
 */
class CssSelectorParser {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * 使用 CSS 选择器提取文本
     */
    fun selectText(html: String, selector: String): String? {
        // 简化实现，实际需要完整的 HTML 解析器 (如 ksoup)
        return when {
            selector.startsWith("text:") -> {
                val regex = Regex(selector.removePrefix("text:"))
                regex.find(html)?.value
            }
            selector.contains("@") -> {
                // 属性选择器 class@text
                val parts = selector.split("@")
                if (parts.size == 2) {
                    selectAttr(html, parts[0], parts[1])
                } else null
            }
            selector.startsWith(".") || selector.startsWith("#") || selector.contains(" ") -> {
                // CSS 类/ID/后代选择器
                extractByCssSelector(html, selector)
            }
            else -> {
                // 简单标签选择器
                extractByTag(html, selector)
            }
        }
    }
    
    /**
     * 使用 CSS 选择器提取属性
     */
    fun selectAttr(html: String, selector: String, attr: String): String? {
        val element = extractElement(html, selector)
        return extractAttribute(element, attr)
    }
    
    /**
     * 使用 CSS 选择器提取多个元素
     */
    fun selectAll(html: String, selector: String): List<String> {
        // 简化实现 - 实际需要完整 HTML 解析
        val results = mutableListOf<String>()
        return results
    }
    
    private fun extractByTag(html: String, tag: String): String? {
        val regex = Regex("<$tag[^>]*>(.*?)</$tag>", RegexOption.DOT_MATCHES_ALL)
        return regex.find(html)?.groupValues?.get(1)?.trim()
    }
    
    private fun extractElement(html: String, selector: String): String {
        // 简化实现
        return html
    }
    
    private fun extractAttribute(element: String, attr: String): String? {
        val regex = Regex("$attr=[\"']([^\"']+)[\"']")
        return regex.find(element)?.groupValues?.get(1)
    }
    
    private fun extractByCssSelector(html: String, selector: String): String? {
        // 简化 CSS 选择器实现
        return when {
            selector.startsWith(".") -> {
                val className = selector.removePrefix(".")
                Regex("class=[\"'][^\"']*$className[\"'][^>]*>(.*?)</", RegexOption.DOT_MATCHES_ALL)
                    .find(html)?.groupValues?.get(1)?.trim()
            }
            selector.startsWith("#") -> {
                val id = selector.removePrefix("#")
                Regex("id=[\"']$id[\"'][^>]*>(.*?)</", RegexOption.DOT_MATCHES_ALL)
                    .find(html)?.groupValues?.get(1)?.trim()
            }
            else -> extractByTag(html, selector)
        }
    }
}

/**
 * 书源规则执行器
 */
class RuleExecutor(
    private val jsEngine: JsEngine,
    private val cssParser: CssSelectorParser = CssSelectorParser()
) {
    
    /**
     * 执行规则 (支持 CSS 选择器和 JS)
     */
    suspend fun executeRule(html: String, rule: String?, context: JsContext): String? {
        if (rule.isNullOrBlank()) return null
        
        return when {
            // JS 规则
            rule.startsWith("<js>") || rule.startsWith("@js:") -> {
                val jsCode = rule
                    .removePrefix("<js>")
                    .removeSuffix("</js>")
                    .removePrefix("@js:")
                    .trim()
                evalJs(jsCode, html, context)?.toString()
            }
            // JSON 规则
            rule.startsWith("$.") || rule.startsWith("$[") -> {
                parseJsonPath(html, rule)
            }
            // CSS 选择器
            rule.contains(" ") || rule.startsWith(".") || rule.startsWith("#") -> {
                cssParser.selectText(html, rule)
            }
            // 正则规则
            rule.startsWith("regex:") -> {
                val regexStr = rule.removePrefix("regex:")
                Regex(regexStr).find(html)?.value
            }
            // 默认：直接返回
            else -> {
                rule
            }
        }
    }
    
    /**
     * 执行 JS 代码
     */
    private suspend fun evalJs(jsCode: String, html: String, context: JsContext): Any? {
        val bindings = mapOf(
            "html" to html,
            "baseUrl" to context.sourceUrl,
            "sourceName" to context.sourceName,
            "result" to ""
        ) + context.variables
        
        return jsEngine.execute(jsCode, bindings)
    }
    
    /**
     * 解析 JSONPath
     */
    private fun parseJsonPath(json: String, path: String): String? {
        return try {
            val tree = json.parseToJsonElement()
            // 简化实现，实际需要完整的 JSONPath 解析器
            tree.toString()
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 书源搜索执行器
 */
class SearchExecutor(
    private val sourceParser: SourceParser
) {
    /**
     * 执行搜索
     */
    suspend fun search(source: BookSource, keyword: String): List<SearchBook> {
        return try {
            sourceParser.parseSearch(source, keyword)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 批量搜索 (多书源)
     */
    suspend fun searchMultiple(sources: List<BookSource>, keyword: String): Map<String, List<SearchBook>> {
        return sources.associateWith { source ->
            try {
                sourceParser.parseSearch(source, keyword)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

/**
 * 书籍详情加载器
 */
class BookInfoLoader(
    private val sourceParser: SourceParser
) {
    /**
     * 加载书籍详情
     */
    suspend fun loadBookInfo(source: BookSource, bookUrl: String): BookInfo {
        return sourceParser.parseBookInfo(source, bookUrl)
    }
    
    /**
     * 加载目录列表
     */
    suspend fun loadToc(source: BookSource, bookUrl: String): List<Chapter> {
        return sourceParser.parseToc(source, bookUrl)
    }
    
    /**
     * 加载章节内容
     */
    suspend fun loadContent(source: BookSource, contentUrl: String): String {
        return sourceParser.parseContent(source, contentUrl)
    }
}
