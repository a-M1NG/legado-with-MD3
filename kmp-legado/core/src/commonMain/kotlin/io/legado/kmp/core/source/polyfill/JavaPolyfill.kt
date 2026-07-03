package io.legado.kmp.core.source.polyfill

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.max
import kotlin.math.min

/**
 * JS 环境 Polyfill - 模拟 Java 类行为
 * 确保现有书源规则无需修改即可运行
 */
class JavaPolyfill {
    
    // ==================== java.time 模拟 ====================
    
    object time {
        /**
         * 获取当前时间戳 (毫秒)
         * 对应：java.time.Instant.now().toEpochMilli()
         */
        fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
        
        /**
         * 格式化时间
         * 对应：java.time.LocalDateTime.now().format()
         */
        fun formatTime(pattern: String): String {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            return when {
                pattern.contains("yyyy") -> pattern
                    .replace("yyyy", now.year.toString())
                    .replace("MM", now.monthNumber.toString().padStart(2, '0'))
                    .replace("dd", now.dayOfMonth.toString().padStart(2, '0'))
                    .replace("HH", now.hour.toString().padStart(2, '0'))
                    .replace("mm", now.minute.toString().padStart(2, '0'))
                    .replace("ss", now.second.toString().padStart(2, '0'))
                else -> pattern
            }
        }
        
        /**
         * 时间戳转日期字符串
         */
        fun timestampToDate(timestamp: Long, pattern: String = "yyyy-MM-dd"): String {
            val dateTime = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            return formatTimeWithPattern(dateTime, pattern)
        }
        
        private fun formatTimeWithPattern(dt: kotlinx.datetime.LocalDateTime, pattern: String): String {
            return pattern
                .replace("yyyy", dt.year.toString())
                .replace("MM", dt.monthNumber.toString().padStart(2, '0'))
                .replace("dd", dt.dayOfMonth.toString().padStart(2, '0'))
                .replace("HH", dt.hour.toString().padStart(2, '0'))
                .replace("mm", dt.minute.toString().padStart(2, '0'))
                .replace("ss", dt.second.toString().padStart(2, '0'))
        }
    }
    
    // ==================== java.util 模拟 ====================
    
    object util {
        /**
         * Base64 编码
         * 对应：java.util.Base64.getEncoder().encodeToString()
         */
        object Base64 {
            fun getEncoder(): Base64Encoder = Base64Encoder
            
            fun getDecoder(): Base64Decoder = Base64Decoder
        }
        
        object Base64Encoder {
            fun encodeToString(bytes: ByteArray): String {
                return bytes.encodeBase64()
            }
            
            fun encodeToString(str: String): String {
                return str.encodeToByteArray().encodeBase64()
            }
        }
        
        object Base64Decoder {
            fun decode(str: String): ByteArray {
                return str.decodeBase64()
            }
            
            fun decodeToString(str: String): String {
                return str.decodeBase64().decodeToString()
            }
        }
        
        /**
         * UUID 生成
         */
        fun randomUUID(): String {
            val chars = "0123456789abcdef"
            return buildString {
                repeat(8) { append(chars.random()) }
                append('-')
                repeat(4) { append(chars.random()) }
                append('-')
                repeat(4) { append(chars.random()) }
                append('-')
                repeat(4) { append(chars.random()) }
                append('-')
                repeat(12) { append(chars.random()) }
            }
        }
    }
    
    // ==================== java.net 模拟 ====================
    
    object net {
        /**
         * URL 编码
         * 对应：java.net.URLEncoder.encode()
         */
        object URLEncoder {
            fun encode(str: String, charset: String = "UTF-8"): String {
                // 简单实现，实际应使用 Ktor 的 encoding
                return str.replace(" ", "+")
                    .replace("&", "%26")
                    .replace("=", "%3D")
                    .replace("#", "%23")
                    .replace("+", "%2B")
                    .replace("/", "%2F")
                    .replace("?", "%3F")
            }
        }
        
        /**
         * URL 解码
         */
        object URLDecoder {
            fun decode(str: String, charset: String = "UTF-8"): String {
                return str.replace("+", " ")
                    .replace("%26", "&")
                    .replace("%3D", "=")
                    .replace("%23", "#")
                    .replace("%2B", "+")
                    .replace("%2F", "/")
                    .replace("%3F", "?")
            }
        }
    }
    
    // ==================== java.lang 模拟 ====================
    
    object lang {
        /**
         * String 工具
         */
        object String {
            fun valueOf(obj: Any?): String = obj?.toString() ?: "null"
            
            fun format(template: String, vararg args: Any?): String {
                var result = template
                args.forEachIndexed { index, arg ->
                    result = result.replace("{$index}", arg?.toString() ?: "null")
                }
                return result
            }
        }
        
        /**
         * Math 工具
         */
        object Math {
            fun abs(value: Int): Int = kotlin.math.abs(value)
            fun abs(value: Long): Long = kotlin.math.abs(value)
            fun abs(value: Float): Float = kotlin.math.abs(value)
            fun abs(value: Double): Double = kotlin.math.abs(value)
            
            fun max(a: Int, b: Int): Int = kotlin.math.max(a, b)
            fun max(a: Long, b: Long): Long = kotlin.math.max(a, b)
            fun max(a: Float, b: Float): Float = kotlin.math.max(a, b)
            fun max(a: Double, b: Double): Double = kotlin.math.max(a, b)
            
            fun min(a: Int, b: Int): Int = kotlin.math.min(a, b)
            fun min(a: Long, b: Long): Long = kotlin.math.min(a, b)
            fun min(a: Float, b: Float): Float = kotlin.math.min(a, b)
            fun min(a: Double, b: Double): Double = kotlin.math.min(a, b)
            
            fun floor(value: Double): Double = kotlin.math.floor(value)
            fun ceil(value: Double): Double = kotlin.math.ceil(value)
            fun round(value: Double): Long = kotlin.math.round(value)
            
            fun random(): Double = kotlin.random.Random.nextDouble()
            
            fun pow(base: Double, exponent: Double): Double = kotlin.math.pow(base, exponent)
            fun sqrt(value: Double): Double = kotlin.math.sqrt(value)
        }
    }
    
    // ==================== 辅助函数 ====================
    
    /**
     * Base64 编码实现 (简单版)
     */
    private fun ByteArray.encodeBase64(): String {
        val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val result = StringBuilder()
        var i = 0
        
        while (i < size) {
            val b1 = this[i].toInt() and 0xFF
            val b2 = if (i + 1 < size) this[i + 1].toInt() and 0xFF else 0
            val b3 = if (i + 2 < size) this[i + 2].toInt() and 0xFF else 0
            
            result.append(base64Chars[b1 shr 2])
            result.append(base64Chars[((b1 and 0x03) shl 4) or (b2 shr 4)])
            
            if (i + 1 < size) {
                result.append(base64Chars[((b2 and 0x0F) shl 2) or (b3 shr 6)])
            } else {
                result.append('=')
            }
            
            if (i + 2 < size) {
                result.append(base64Chars[b3 and 0x3F])
            } else {
                result.append('=')
            }
            
            i += 3
        }
        
        return result.toString()
    }
    
    /**
     * Base64 解码实现 (简单版)
     */
    private fun String.decodeBase64(): ByteArray {
        val base64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val cleanStr = this.replace("=", "")
        val result = ByteArrayOutputStream()
        var i = 0
        
        while (i < cleanStr.length) {
            val b1 = base64Chars.indexOf(cleanStr[i])
            val b2 = if (i + 1 < cleanStr.length) base64Chars.indexOf(cleanStr[i + 1]) else 0
            val b3 = if (i + 2 < cleanStr.length) base64Chars.indexOf(cleanStr[i + 2]) else 0
            val b4 = if (i + 3 < cleanStr.length) base64Chars.indexOf(cleanStr[i + 3]) else 0
            
            result.write((b1 shl 2) or (b2 shr 4))
            
            if (i + 2 < cleanStr.length) {
                result.write(((b2 and 0x0F) shl 4) or (b3 shr 2))
            }
            
            if (i + 3 < cleanStr.length) {
                result.write(((b3 and 0x03) shl 6) or b4)
            }
            
            i += 4
        }
        
        return result.toByteArray()
    }
    
    /**
     * 简单的字节数组输出流
     */
    private class ByteArrayOutputStream {
        private val buffer = mutableListOf<Byte>()
        
        fun write(byte: Int) {
            buffer.add(byte.toByte())
        }
        
        fun toByteArray(): ByteArray = buffer.toByteArray()
    }
}

/**
 * 全局 JS 对象注入
 * 在 JS 引擎初始化时调用
 */
fun injectGlobalVariables(jsEngine: JsEngine, context: SourceContext) {
    jsEngine.evaluate("""
        // 注入全局变量
        var baseUrl = "${context.baseUrl}";
        var result = null;
        var cookie = ${formatJsObject(context.cookies)};
        var header = ${formatJsObject(context.headers)};
        
        // 注入 java 对象
        var java = {
            time: {
                currentTimeMillis: function() { return ${JavaPolyfill.time.currentTimeMillis()} },
                formatTime: function(pattern) { return "${JavaPolyfill.time.formatTime("yyyy-MM-dd HH:mm:ss")}" }
            },
            util: {
                Base64: {
                    getEncoder: function() {
                        return {
                            encodeToString: function(str) {
                                // 实际调用平台原生 Base64
                                return __platformBase64Encode__(str);
                            }
                        };
                    }
                },
                randomUUID: function() { return "${JavaPolyfill.util.randomUUID()}" }
            },
            net: {
                URLEncoder: {
                    encode: function(str) { return encodeURIComponent(str); }
                },
                URLDecoder: {
                    decode: function(str) { return decodeURIComponent(str); }
                }
            },
            lang: {
                String: {
                    valueOf: function(obj) { return String(obj); },
                    format: function(template) { return template; }
                },
                Math: Math
            }
        };
        
        // 常用简写
        var Time = java.time;
        var Base64 = java.util.Base64;
        var UUID = java.util.randomUUID;
    """.trimIndent())
}

private fun formatJsObject(map: Map<String, String>): String {
    return map.entries.joinToString(", ", "{", "}") { "\"${it.key}\": \"${it.value}\"" }
}

/**
 * 书源上下文
 */
data class SourceContext(
    val baseUrl: String = "",
    val cookies: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap()
)
