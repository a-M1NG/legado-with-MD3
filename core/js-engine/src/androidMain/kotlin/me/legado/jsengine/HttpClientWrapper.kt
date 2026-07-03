package me.legado.jsengine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Android 平台 HTTP 客户端实现
 */
actual class HttpClientWrapper {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .build()
    
    actual suspend fun get(url: String, header: String?): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .apply {
                if (!header.isNullOrBlank()) {
                    // 解析 header JSON
                    try {
                        // 简化处理，实际需要解析 JSON
                        addHeader("User-Agent", "Mozilla/5.0")
                    } catch (e: Exception) {
                        addHeader("User-Agent", "Mozilla/5.0")
                    }
                } else {
                    addHeader("User-Agent", "Mozilla/5.0")
                }
            }
            .get()
            .build()
        
        client.newCall(request).execute().use { response ->
            response.body?.string() ?: ""
        }
    }
    
    actual suspend fun post(url: String, body: String, header: String?): String = withContext(Dispatchers.IO) {
        val mediaType = okhttp3.MediaType.parse("application/json; charset=utf-8")
        val requestBody = okhttp3.RequestBody.create(mediaType, body)
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("User-Agent", "Mozilla/5.0")
            .build()
        
        client.newCall(request).execute().use { response ->
            response.body?.string() ?: ""
        }
    }
}
