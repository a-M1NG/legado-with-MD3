package me.legado.jsengine

import kotlinx.coroutines.await
import kotlin.browser.window
import org.w3c.fetch.Headers
import org.w3c.fetch.RequestInit

/**
 * Web 平台 HTTP 客户端实现 (Fetch API)
 */
actual class HttpClientWrapper {
    actual suspend fun get(url: String, header: String?): String {
        val headers = Headers()
        headers.set("User-Agent", "Mozilla/5.0")
        
        val init = RequestInit(
            method = "GET",
            headers = headers
        )
        
        return window.fetch(url, init)
            .await()
            .text()
            .await()
    }
    
    actual suspend fun post(url: String, body: String, header: String?): String {
        val headers = Headers()
        headers.set("Content-Type", "application/json")
        headers.set("User-Agent", "Mozilla/5.0")
        
        val init = RequestInit(
            method = "POST",
            body = body,
            headers = headers
        )
        
        return window.fetch(url, init)
            .await()
            .text()
            .await()
    }
}
