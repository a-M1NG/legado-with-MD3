package me.legado.jsengine

/**
 * KMP JS 引擎通用接口
 * 用于执行书源规则中的 JavaScript 代码
 */
interface JsEngine {
    /**
     * 执行 JavaScript 代码
     * @param script JS 脚本
     * @param bindings 绑定变量
     * @return 执行结果
     */
    suspend fun execute(script: String, bindings: Map<String, Any?> = emptyMap()): Any?
    
    /**
     * 调用 JS 函数
     * @param functionName 函数名
     * @param args 参数
     * @return 函数返回值
     */
    suspend fun callFunction(functionName: String, vararg args: Any?): Any?
    
    /**
     * 注册全局函数
     */
    fun registerFunction(name: String, handler: suspend (Array<Any?>) -> Any?)
    
    /**
     * 释放资源
     */
    fun dispose()
}

/**
 * JS 引擎工厂接口
 */
interface JsEngineFactory {
    fun createEngine(): JsEngine
}

/**
 * JS 执行上下文
 */
data class JsContext(
    val sourceUrl: String,
    val sourceName: String,
    val variables: MutableMap<String, Any?> = mutableMapOf()
)

/**
 * JS 执行结果
 */
sealed class JsResult {
    data class Success(val value: Any?) : JsResult()
    data class Error(val message: String, val exception: Throwable? = null) : JsResult()
}

/**
 * 书源规则解析器
 */
interface SourceParser {
    /**
     * 解析搜索规则
     */
    suspend fun parseSearch(source: BookSource, keyWord: String): List<SearchBook>
    
    /**
     * 解析书籍详情
     */
    suspend fun parseBookInfo(source: BookSource, bookUrl: String): BookInfo
    
    /**
     * 解析目录列表
     */
    suspend fun parseToc(source: BookSource, bookUrl: String): List<Chapter>
    
    /**
     * 解析章节内容
     */
    suspend fun parseContent(source: BookSource, contentUrl: String): String
    
    /**
     * 执行 JS 规则
     */
    suspend fun evalJs(script: String, context: JsContext): Any?
}

/**
 * 搜索结果
 */
data class SearchBook(
    val name: String,
    val author: String,
    val coverUrl: String?,
    val intro: String?,
    val bookUrl: String,
    val origin: String
)

/**
 * 书籍详情
 */
data class BookInfo(
    val name: String,
    val author: String,
    val coverUrl: String?,
    val intro: String?,
    val chapters: List<Chapter>,
    val lastChapter: String?,
    val wordCount: Long
)
