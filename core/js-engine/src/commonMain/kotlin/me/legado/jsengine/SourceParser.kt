package me.legado.jsengine

import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import me.legado.core.BookSource
import me.legado.core.Chapter

/**
 * 书源解析器实现 - KMP 共享代码
 */
class SourceParserImpl(
    private val jsEngineFactory: JsEngineFactory,
    private val httpClient: HttpClientWrapper
) : SourceParser {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override suspend fun parseSearch(source: BookSource, keyWord: String): List<SearchBook> {
        val rule = source.ruleSearch ?: throw IllegalArgumentException("No search rule")
        val searchUrl = rule.searchUrl.replace("{{keyWord}}", keyWord)
        
        val html = httpClient.get(searchUrl, source.header)
        return parseSearchResult(html, rule, source.bookSourceName)
    }
    
    override suspend fun parseBookInfo(source: BookSource, bookUrl: String): BookInfo {
        val rule = source.ruleBookInfo ?: throw IllegalArgumentException("No book info rule")
        
        // 执行 init JS
        if (!rule.init.isNullOrBlank()) {
            executeJs(rule.init!!, JsContext(source.bookSourceUrl, source.bookSourceName))
        }
        
        val html = httpClient.get(bookUrl, source.header)
        
        val name = evalRule(html, rule.name) ?: ""
        val author = evalRule(html, rule.author) ?: ""
        val coverUrl = evalRule(html, rule.coverUrl)
        val intro = evalRule(html, rule.intro)
        val lastChapter = evalRule(html, rule.latestChapter)
        val wordCountStr = evalRule(html, rule.wordCount)
        val wordCount = wordCountStr?.toLongOrNull() ?: 0L
        
        return BookInfo(
            name = name,
            author = author,
            coverUrl = coverUrl,
            intro = intro,
            chapters = emptyList(),
            lastChapter = lastChapter,
            wordCount = wordCount
        )
    }
    
    override suspend fun parseToc(source: BookSource, bookUrl: String): List<Chapter> {
        val rule = source.ruleToc ?: throw IllegalArgumentException("No TOC rule")
        
        val html = httpClient.get(bookUrl, source.header)
        val chapterListHtml = evalRule(html, rule.chapterList) ?: ""
        
        // 解析章节列表 (简化版本，实际需要更复杂的 HTML 解析)
        return parseChapterList(chapterListHtml, rule, bookUrl)
    }
    
    override suspend fun parseContent(source: BookSource, contentUrl: String): String {
        val rule = source.ruleContent ?: throw IllegalArgumentException("No content rule")
        
        val html = httpClient.get(contentUrl, source.header)
        var content = evalRule(html, rule.content) ?: ""
        
        // 处理替换规则
        if (!rule.replaceRegex.isNullOrBlank()) {
            content = content.replace(Regex(rule.replaceRegex!!), "")
        }
        
        return content
    }
    
    override suspend fun evalJs(script: String, context: JsContext): Any? {
        return executeJs(script, context)
    }
    
    private suspend fun executeJs(script: String, context: JsContext): Any? {
        val engine = jsEngineFactory.createEngine()
        try {
            // 注册全局变量
            val bindings = mapOf(
                "baseUrl" to context.sourceUrl,
                "sourceName" to context.sourceName
            ) + context.variables
            
            // 添加常用 JS 函数
            engine.registerFunction("httpGet") { args ->
                val url = args[0] as? String ?: ""
                httpClient.get(url, null)
            }
            
            return engine.execute(script, bindings)
        } finally {
            engine.dispose()
        }
    }
    
    private fun parseSearchResult(html: String, rule: me.legado.core.RuleSearch, origin: String): List<SearchBook> {
        // 简化实现，实际需要 HTML 解析器
        val books = mutableListOf<SearchBook>()
        
        // 这里应该使用 CSS Selector 或 XPath 解析 HTML
        // 示例代码结构
        /*
        val bookListNodes = html.select(rule.bookList!!)
        for (node in bookListNodes) {
            books.add(
                SearchBook(
                    name = node.selectFirst(rule.name)?.text() ?: "",
                    author = node.selectFirst(rule.author)?.text() ?: "",
                    coverUrl = node.selectFirst(rule.coverUrl)?.attr("src"),
                    intro = node.selectFirst(rule.intro)?.text(),
                    bookUrl = node.selectFirst(rule.bookUrl)?.attr("href") ?: "",
                    origin = origin
                )
            )
        }
        */
        
        return books
    }
    
    private fun parseChapterList(html: String, rule: me.legado.core.RuleToc, bookUrl: String): List<Chapter> {
        // 简化实现
        return emptyList()
    }
    
    private fun evalRule(html: String, rule: String?): String? {
        if (rule.isNullOrBlank()) return null
        
        // 判断是否是 JS 规则
        return if (rule.startsWith("<js>") || rule.startsWith("@js:")) {
            val jsCode = rule.removePrefix("<js>").removePrefix("</js>")
                .removePrefix("@js:")
            executeJs(jsCode, JsContext("", "")).toString()
        } else {
            // CSS Selector 规则 (简化)
            rule
        }
    }
}

/**
 * HTTP 客户端包装器 (Expect/Actual)
 */
expect class HttpClientWrapper() {
    suspend fun get(url: String, header: String?): String
    suspend fun post(url: String, body: String, header: String?): String
}
