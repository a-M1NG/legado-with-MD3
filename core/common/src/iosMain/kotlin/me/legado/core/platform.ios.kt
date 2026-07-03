package me.legado.core

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS 平台文件系统实现
 */
@OptIn(ExperimentalForeignApi::class)
actual class PlatformFileSystem {
    private val fileManager = NSFileManager.defaultManager
    
    actual fun getCacheDir(): String {
        val cacheUrl = fileManager.URLForDirectory(
            directory = NSCachesDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        return (cacheUrl?.path ?: "/tmp/legado/cache") + "/legado"
    }
    
    actual fun getFilesDir(): String {
        val docUrl = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        )
        return docUrl?.path ?: "/var/mobile/Containers/Data/Application/legado"
    }
    
    actual fun getExternalFilesDir(): String? {
        return getFilesDir()
    }
    
    actual fun readTextFile(path: String): String? {
        return try {
            NSString.create(contentsOfFile = path, encoding = NSUTF8StringEncoding) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    actual fun writeTextFile(path: String, content: String): Boolean {
        return try {
            val nsString = content as NSString
            nsString.writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun deleteFile(path: String): Boolean {
        return try {
            fileManager.removeItemAtPath(path, error = null)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun fileExists(path: String): Boolean {
        return fileManager.fileExistsAtPath(path)
    }
}

/**
 * iOS 平台 JS 引擎工厂 (使用 JavaScriptCore)
 */
actual class JsEngineFactoryImpl : JsEngineFactory {
    
    actual override fun createEngine(): JsEngine {
        return JavaScriptCoreEngine()
    }
}

internal class JavaScriptCoreEngine : JsEngine {
    override suspend fun execute(script: String, bindings: Map<String, Any?>): Any? {
        // TODO: 集成 JavaScriptCore
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
    return "ios"
}

actual fun isDebugMode(): Boolean {
    // iOS debug detection would require additional setup
    return false
}
