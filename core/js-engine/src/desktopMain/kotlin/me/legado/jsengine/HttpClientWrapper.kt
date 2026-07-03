package me.legado.jsengine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.URI
import java.time.Duration

/**
 * Desktop 平台 HTTP 客户端实现 (Java 11+)
 */
actual class HttpClientWrapper {
    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(30))
        .build()
    
    actual suspend fun get(url: String, header: String?): String = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI(url))
            .timeout(Duration.ofSeconds(30))
            .header("User-Agent", "Mozilla/5.0")
            .GET()
            .build()
        
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response -> response.body() }
            .join()
    }
    
    actual suspend fun post(url: String, body: String, header: String?): String = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(URI(url))
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "application/json")
            .header("User-Agent", "Mozilla/5.0")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply { response -> response.body() }
            .join()
    }
}
