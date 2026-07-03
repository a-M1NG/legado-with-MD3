package me.legado.jsengine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.js.Json
import kotlin.js.jso

/**
 * Web 平台 JS 引擎实现 (基于 JavaScript eval)
 */
class WebJsEngine : JsEngine {
    private val handlers = mutableMapOf<String, suspend (Array<Any?>) -> Any?>()
    
    override suspend fun execute(script: String, bindings: Map<String, Any?>): Any? = withContext(Dispatchers.Default) {
        try {
            // Web 平台直接使用 js eval
            return@withContext jsEval(script, bindings)
        } catch (e: Exception) {
            throw JsEngineException("JS execution failed: ${e.message}", e)
        }
    }
    
    override suspend fun callFunction(functionName: String, vararg args: Any?): Any? = withContext(Dispatchers.Default) {
        try {
            val handler = handlers[functionName]
                ?: throw JsEngineException("Function $functionName not found")
            handler.invoke(args)
        } catch (e: Exception) {
            throw JsEngineException("Function call failed: ${e.message}", e)
        }
    }
    
    override fun registerFunction(name: String, handler: suspend (Array<Any?>) -> Any?) {
        handlers[name] = handler
    }
    
    override fun dispose() {
        handlers.clear()
    }
}

/**
 * JS eval 外部函数 (由 Kotlin/JS 提供)
 */
private external fun jsEval(script: String, bindings: Map<String, Any?>): Any?

/**
 * Web JS 引擎工厂
 */
class WebJsEngineFactory : JsEngineFactory {
    override fun createEngine(): JsEngine = WebJsEngine()
}
