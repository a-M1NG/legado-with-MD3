package me.legado.core

/**
 * 期望的跨平台文件系统接口
 */
expect class PlatformFileSystem {
    fun getCacheDir(): String
    fun getFilesDir(): String
    fun getExternalFilesDir(): String?
    fun readTextFile(path: String): String?
    fun writeTextFile(path: String, content: String): Boolean
    fun deleteFile(path: String): Boolean
    fun fileExists(path: String): Boolean
}

/**
 * 期望的 JS 引擎工厂实现
 */
expect class JsEngineFactoryImpl : JsEngineFactory {
    override fun createEngine(): JsEngine
}

/**
 * 平台相关工具函数
 */
expect fun getPlatformName(): String

expect fun isDebugMode(): Boolean
