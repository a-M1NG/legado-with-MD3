package me.legado.jsengine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.graalvm.polyglot.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Desktop 平台 JS 引擎实现 (基于 GraalVM)
 */
class DesktopJsEngine : JsEngine {
    private val context: Context = Context.newBuilder("js")
        .allowHostAccess(true)
        .allowNativeAccess(true)
        .build()
    private val scope: Value = context.eval("js", "{}")
    private val handlers = ConcurrentHashMap<String, suspend (Array<Any?>) -> Any?>()
    
    override suspend fun execute(script: String, bindings: Map<String, Any?>): Any? = withContext(Dispatchers.IO) {
        try {
            bindings.forEach { (key, value) ->
                context.getBindings("js").putMember(key, value)
            }
            val result = context.eval("js", script)
            if (result.isNullOrNothing()) null else result.asAny()
        } catch (e: Exception) {
            throw JsEngineException("JS execution failed: ${e.message}", e)
        }
    }
    
    override suspend fun callFunction(functionName: String, vararg args: Any?): Any? = withContext(Dispatchers.IO) {
        try {
            val function = scope.getMember(functionName)
                ?: throw JsEngineException("Function $functionName not found")
            
            val result = function.execute(*args)
            if (result.isNullOrNothing()) null else result.asAny()
        } catch (e: Exception) {
            throw JsEngineException("Function call failed: ${e.message}", e)
        }
    }
    
    override fun registerFunction(name: String, handler: suspend (Array<Any?>) -> Any?) {
        handlers[name] = handler
        // GraalVM 需要特殊处理异步函数
        context.getBindings("js").putMember(name, handler)
    }
    
    override fun dispose() {
        context.close()
    }
}

/**
 * Desktop JS 引擎工厂
 */
class DesktopJsEngineFactory : JsEngineFactory {
    override fun createEngine(): JsEngine = DesktopJsEngine()
}
