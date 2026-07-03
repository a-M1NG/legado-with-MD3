package me.legado.jsengine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.darwin.NSObject

/**
 * iOS 平台 HTTP 客户端实现 (NSURLSession)
 */
actual class HttpClientWrapper {
    actual suspend fun get(url: String, header: String?): String = withContext(Dispatchers.Main) {
        val nsUrl = NSURL.URLWithString(url) ?: return@withContext ""
        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        request.setHTTPMethod("GET")
        request.setValue("Mozilla/5.0", forHTTPHeaderField = "User-Agent")
        
        suspendCoroutine<String> { continuation ->
            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
                if (error != null) {
                    continuation.resumeWith(Result.failure(Exception(error.localizedDescription)))
                } else {
                    val body = data?.let { NSString(data = it, encoding = NSUTF8StringEncoding) } ?: ""
                    continuation.resumeWith(Result.success(body.toString()))
                }
            }
            task.resume()
        }
    }
    
    actual suspend fun post(url: String, body: String, header: String?): String = withContext(Dispatchers.Main) {
        val nsUrl = NSURL.URLWithString(url) ?: return@withContext ""
        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        request.setHTTPMethod("POST")
        request.setValue("application/json", forHTTPHeaderField = "Content-Type")
        request.setValue("Mozilla/5.0", forHTTPHeaderField = "User-Agent")
        request.HTTPBody = body.dataUsingEncoding(NSUTF8StringEncoding)
        
        suspendCoroutine<String> { continuation ->
            val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
                if (error != null) {
                    continuation.resumeWith(Result.failure(Exception(error.localizedDescription)))
                } else {
                    val body = data?.let { NSString(data = it, encoding = NSUTF8StringEncoding) } ?: ""
                    continuation.resumeWith(Result.success(body.toString()))
                }
            }
            task.resume()
        }
    }
}

// 协程桥接函数
private inline fun <T> suspendCoroutine(crossinline block: (kotlin.coroutines.Continuation<T>) -> Unit): T {
    throw NotImplementedError("This is a placeholder - needs proper Kotlin/Native coroutine support")
}
