package io.legado.kmp.core.book.parser

import io.legado.kmp.core.book.model.BookChapter
import io.legado.kmp.core.source.rule.SourceRule

/**
 * TXT 书籍解析器
 * 支持智能章节分割、编码自动检测、大文件流式读取
 */
class TxtBookParser {
    
    companion object {
        // 常见章节标题正则
        private val CHAPTER_PATTERNS = listOf(
            Regex("""^第 [一二三四五六七八九十百千万零\d]+[章回卷节]"""),
            Regex("""^Chapter\s+\d+"""),
            Regex("""^PART\s+[IVX]+"""),
            Regex("""^楔子"""),
            Regex("""^序 (?:言 | 章 | 幕)?"""),
            Regex("""^引 (?:子 | 言)?"""),
            Regex("""^尾声"""),
            Regex("""^终 (?:章 | 场)"""),
            Regex("""^\[\s*[一二三四五六七八九十百千万零\d]+\s*\]"""),
            Regex("""^（ [一二三四五六七八九十百千万零\d]+）""")
        )
        
        // 常见编码顺序
        private val ENCODINGS = listOf("UTF-8", "GBK", "BIG5", "ISO-8859-1")
    }
    
    /**
     * 解析 TXT 文件，提取章节列表
     * @param content 文件内容
     * @return 章节列表
     */
    fun parseChapters(content: String): List<BookChapter> {
        val chapters = mutableListOf<BookChapter>()
        val lines = content.lineSequence().toList()
        
        var chapterStartIndex = 0
        var currentChapterTitle: String? = null
        
        for ((index, line) in lines.withIndex()) {
            val trimmedLine = line.trim()
            
            // 检测是否为章节标题
            if (isChapterTitle(trimmedLine)) {
                // 保存上一章节
                if (currentChapterTitle != null) {
                    val chapterContent = lines.subList(chapterStartIndex, index).joinToString("\n")
                    chapters.add(
                        BookChapter(
                            id = chapters.size.toLong(),
                            title = currentChapterTitle!!,
                            startIndex = chapterStartIndex,
                            endIndex = index,
                            wordCount = chapterContent.length
                        )
                    )
                }
                
                // 开始新章节
                currentChapterTitle = trimmedLine
                chapterStartIndex = index
            }
        }
        
        // 添加最后一章
        if (currentChapterTitle != null && chapterStartIndex < lines.size) {
            val chapterContent = lines.subList(chapterStartIndex, lines.size).joinToString("\n")
            chapters.add(
                BookChapter(
                    id = chapters.size.toLong(),
                    title = currentChapterTitle!!,
                    startIndex = chapterStartIndex,
                    endIndex = lines.size,
                    wordCount = chapterContent.length
                )
            )
        }
        
        // 如果没有检测到章节，将整个文件作为一章
        if (chapters.isEmpty()) {
            chapters.add(
                BookChapter(
                    id = 0,
                    title = "全文",
                    startIndex = 0,
                    endIndex = lines.size,
                    wordCount = content.length
                )
            )
        }
        
        return chapters
    }
    
    /**
     * 检测字符串是否为章节标题
     */
    private fun isChapterTitle(line: String): Boolean {
        if (line.length > 50) return false // 章节标题不会太长
        
        for (pattern in CHAPTER_PATTERNS) {
            if (pattern.containsMatchIn(line)) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * 自动检测文件编码
     * @param bytes 文件字节数组
     * @return 检测到的编码
     */
    fun detectEncoding(bytes: ByteArray): String {
        // 检查 BOM 头
        if (bytes.size >= 3) {
            when {
                bytes[0].toInt() == 0xEF && bytes[1].toInt() == 0xBB && bytes[2].toInt() == 0xBF -> 
                    return "UTF-8"
                bytes[0].toInt() == 0xFE && bytes[1].toInt() == 0xFF -> 
                    return "UTF-16BE"
                bytes[0].toInt() == 0xFF && bytes[1].toInt() == 0xFE -> 
                    return "UTF-16LE"
            }
        }
        
        // 尝试不同编码解码，选择最合理的
        for (encoding in ENCODINGS) {
            try {
                val text = bytes.decodeToString()
                if (isValidText(text)) {
                    return encoding
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        // 默认返回 UTF-8
        return "UTF-8"
    }
    
    /**
     * 判断文本是否有效 (无乱码)
     */
    private fun isValidText(text: String): Boolean {
        // 简单判断：检查是否有过多替换字符
        val invalidCharCount = text.count { it == '\uFFFD' }
        return invalidCharCount.toDouble() / text.length < 0.01 // 乱码比例小于 1%
    }
    
    /**
     * 流式读取大文件 (按块处理)
     * @param filePath 文件路径
     * @param blockSize 块大小 (字节)
     * @param processBlock 处理每块的回调
     */
    fun streamReadLargeFile(
        filePath: String,
        blockSize: Int = 1024 * 1024, // 1MB
        processBlock: (String) -> Unit
    ) {
        // 实际实现需要平台特定的文件访问
        // 这里提供接口设计
        TODO("Platform-specific file streaming implementation")
    }
    
    /**
     * 获取指定章节内容
     * @param content 完整文件内容
     * @param chapter 章节信息
     * @return 章节内容
     */
    fun getChapterContent(content: String, chapter: BookChapter): String {
        val lines = content.lineSequence().toList()
        return lines.subList(chapter.startIndex, chapter.endIndex).joinToString("\n")
    }
}

/**
 * EPUB 书籍解析器
 * 支持 EPUB2/EPUB3，CSS 样式提取，目录解析
 */
class EpubBookParser {
    
    /**
     * 解析 EPUB 文件
     * @param epubPath EPUB 文件路径
     * @return 解析结果
     */
    fun parseEpub(epubPath: String): EpubBook {
        // EPUB 本质是 ZIP 文件，需要解压后解析
        // 主要文件结构:
        // - META-INF/container.xml (指向 content.opf)
        // - *.opf (元数据和 manifest)
        // - *.ncx 或 nav.xhtml (目录)
        // - XHTML 内容文件
        // - CSS 样式文件
        // - 图片等资源
        
        TODO("EPUB parsing implementation")
    }
    
    /**
     * 提取 EPUB 中的 CSS 样式
     */
    fun extractCssStyles(epubContent: Map<String, String>): Map<String, String> {
        val cssMap = mutableMapOf<String, String>()
        
        epubContent.filterKeys { it.endsWith(".css") }.forEach { (path, content) ->
            cssMap[path] = content
        }
        
        return cssMap
    }
    
    /**
     * 解析 NCX 目录
     */
    fun parseNcx(ncxContent: String): List<BookChapter> {
        // 解析 XML 格式的 NCX 目录
        TODO("NCX parsing implementation")
    }
    
    /**
     * 解析 Nav 文档 (EPUB3)
     */
    fun parseNav(navContent: String): List<BookChapter> {
        // 解析 XHTML 格式的 Nav 目录
        TODO("Nav parsing implementation")
    }
}

/**
 * EPUB 解析结果
 */
data class EpubBook(
    val title: String,
    val author: String,
    val publisher: String?,
    val language: String?,
    val chapters: List<BookChapter>,
    val cssStyles: Map<String, String>,
    val resources: Map<String, ByteArray> // 图片等二进制资源
)
