package me.legado.core

import java.io.File

/**
 * Desktop (JVM) 平台文件系统实现
 */
actual class PlatformFileSystem {
    private val userHome = System.getProperty("user.home")
    
    actual fun getCacheDir(): String {
        return File(System.getProperty("java.io.tmpdir"), "legado/cache").apply { mkdirs() }.absolutePath
    }
    
    actual fun getFilesDir(): String {
        return File(userHome, ".legado").apply { mkdirs() }.absolutePath
    }
    
    actual fun getExternalFilesDir(): String? {
        return getFilesDir()
    }
    
    actual fun readTextFile(path: String): String? {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun writeTextFile(path: String, content: String): Boolean {
        return try {
            File(path).writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun deleteFile(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun fileExists(path: String): Boolean {
        return File(path).exists()
    }
}

/**
 * Desktop 平台 JS 引擎工厂 (使用 GraalVM/Nashorn)
 */
actual class JsEngineFactoryImpl : JsEngineFactory {
    
    actual override fun createEngine(): JsEngine {
        return GraalJsEngine()
    }
}

internal class GraalJsEngine : JsEngine {
    override suspend fun execute(script: String, bindings: Map<String, Any?>): Any? {
        // TODO: 集成 GraalVM JS 引擎
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
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> "windows"
        os.contains("mac") -> "macos"
        os.contains("linux") -> "linux"
        else -> "unknown"
    }
}

actual fun isDebugMode(): Boolean {
    return System.getProperty("legado.debug") == "true"
}
