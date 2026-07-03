package me.legado.core

/**
 * JS 引擎接口 - KMP 共享代码
 * 用于跨平台执行书源规则中的 JavaScript 代码
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
