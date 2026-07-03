package me.legado.jsengine

import org.mozilla.javascript.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Android 平台 JS 引擎实现 (基于 Rhino)
 */
class AndroidJsEngine : JsEngine {
    private val context: Context = Context.enter()
    private val scope: Scriptable = context.initStandardObjects()
    private val handlers = ConcurrentHashMap<String, suspend (Array<Any?>) -> Any?>()
    
    init {
        context.optimizationLevel = -1
        context.languageVersion = Context.VERSION_ES6
    }
    
    override suspend fun execute(script: String, bindings: Map<String, Any?>): Any? = withContext(Dispatchers.IO) {
        try {
            bindings.forEach { (key, value) ->
                scope.put(key, scope, Context.javaToJS(value, scope))
            }
            context.evaluateString(scope, script, "script", 1, null)
        } catch (e: Exception) {
            throw JsEngineException("JS execution failed: ${e.message}", e)
        }
    }
    
    override suspend fun callFunction(functionName: String, vararg args: Any?): Any? = withContext(Dispatchers.IO) {
        try {
            val function = scope.get(functionName, scope) as? BaseFunction
                ?: throw JsEngineException("Function $functionName not found")
            
            val jsArgs = args.map { Context.javaToJS(it, scope) }.toTypedArray()
            function.call(context, scope, scope, jsArgs)
        } catch (e: Exception) {
            throw JsEngineException("Function call failed: ${e.message}", e)
        }
    }
    
    override fun registerFunction(name: String, handler: suspend (Array<Any?>) -> Any?) {
        handlers[name] = handler
        scope.put(name, scope, JsFunctionWrapper(handler, this))
    }
    
    override fun dispose() {
        Context.exit()
    }
}

/**
 * JS 函数包装器
 */
class JsFunctionWrapper(
    private val handler: suspend (Array<Any?>) -> Any?,
    private val engine: AndroidJsEngine
) : BaseFunction() {
    override fun call(context: Context, scope: Scriptable, thisObj: Scriptable, args: Array<Any?>): Any? {
        // 注意：这里需要处理异步调用
        return handler.invoke(args.map { it as? Any? }.toTypedArray())
            .let { 
                // 简化处理，实际需要使用协程桥接
                it 
            }
    }
}

/**
 * Android JS 引擎工厂
 */
class AndroidJsEngineFactory : JsEngineFactory {
    override fun createEngine(): JsEngine = AndroidJsEngine()
}

/**
 * JS 引擎异常
 */
class JsEngineException(message: String, cause: Throwable? = null) : Exception(message, cause)
