package me.legado.jsengine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * iOS 平台 JS 引擎实现 (基于 JavaScriptCore)
 */
class IosJsEngine : JsEngine {
    private val handlers = mutableMapOf<String, suspend (Array<Any?>) -> Any?>()
    
    override suspend fun execute(script: String, bindings: Map<String, Any?>): Any? = withContext(Dispatchers.Default) {
        try {
            // iOS 使用 JavaScriptCore.framework
            return@withContext evaluateScript(script, bindings)
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
 * 外部函数：iOS JavaScriptCore 脚本执行
 */
private external fun evaluateScript(script: String, bindings: Map<String, Any?>): Any?

/**
 * iOS JS 引擎工厂
 */
class IosJsEngineFactory : JsEngineFactory {
    override fun createEngine(): JsEngine = IosJsEngine()
}
