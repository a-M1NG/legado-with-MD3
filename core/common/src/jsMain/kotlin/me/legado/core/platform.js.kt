package me.legado.core

/**
 * JS (Web) 平台文件系统实现
 * 注意：浏览器环境受沙箱限制，只能使用 IndexedDB/LocalStorage
 */
actual class PlatformFileSystem {
    
    actual fun getCacheDir(): String {
        return "/legado/cache"
    }
    
    actual fun getFilesDir(): String {
        return "/legado/files"
    }
    
    actual fun getExternalFilesDir(): String? {
        return null
    }
    
    actual fun readTextFile(path: String): String? {
        // TODO: 使用 IndexedDB 实现
        return null
    }
    
    actual fun writeTextFile(path: String, content: String): Boolean {
        // TODO: 使用 IndexedDB 实现
        return false
    }
    
    actual fun deleteFile(path: String): Boolean {
        // TODO: 使用 IndexedDB 实现
        return false
    }
    
    actual fun fileExists(path: String): Boolean {
        // TODO: 使用 IndexedDB 实现
        return false
    }
}

/**
 * Web 平台 JS 引擎工厂
 * 浏览器环境直接使用原生 JavaScript
 */
actual class JsEngineFactoryImpl : JsEngineFactory {
    
    actual override fun createEngine(): JsEngine {
        return NativeJsEngine()
    }
}

internal class NativeJsEngine : JsEngine {
    override suspend fun execute(script: String, bindings: Map<String, Any?>): Any? {
        // Web 环境下可以直接执行 JS
        // TODO: 实现安全的沙箱执行
        return null
    }
    
    override suspend fun callFunction(functionName: String, vararg args: Any?): Any? {
        TODO("Not yet implemented")
    }
    
    override fun registerFunction(name: String, handler: suspend (Array<Any?>) -> Any?) {
        TODO("Not yet implemented")
    }
    
    override fun dispose() {
        // Cleanup
    }
}

actual fun getPlatformName(): String {
    return "web"
}

actual fun isDebugMode(): Boolean {
    // Web debug detection
    return js("process.env.NODE_ENV === 'development'") as? Boolean ?: false
}
