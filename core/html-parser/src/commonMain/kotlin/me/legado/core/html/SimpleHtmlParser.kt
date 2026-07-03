package me.legado.core.html

/**
 * 简化版 HTML 元素实现
 * 用于跨平台 HTML 解析
 */
data class SimpleHtmlElement(
    override val tagName: String,
    override val text: String,
    override val ownText: String,
    private val attributes: Map<String, String> = emptyMap(),
    private val childElements: List<SimpleHtmlElement> = emptyList(),
    private val parentElement: SimpleHtmlElement? = null
) : HtmlElement {

    override fun attr(name: String): String? = attributes[name]

    override fun attrs(): Map<String, String> = attributes.toMap()

    override fun select(cssSelector: String): List<HtmlElement> {
        // 简化实现：仅支持标签选择器
        return if (cssSelector == tagName || cssSelector == "*") {
            listOf(this) + childElements.flatMap { it.select(cssSelector) }
        } else {
            childElements.flatMap { it.select(cssSelector) }
        }
    }

    override fun selectFirst(cssSelector: String): HtmlElement? {
        return select(cssSelector).firstOrNull()
    }

    override fun children(): List<HtmlElement> = childElements

    override fun parent(): HtmlElement? = parentElement
}

/**
 * 简化版 HTML 解析器实现
 * 使用正则表达式进行基础解析（生产环境建议集成 ksoup 或类似库）
 */
class SimpleHtmlParser : HtmlParser {

    private val tagRegex = Regex("<([a-zA-Z][a-zA-Z0-9]*)([^>]*)>(.*?)</\\1>|<([a-zA-Z][a-zA-Z0-9]*)([^>]*)/>")
    private val attrRegex = Regex("""([a-zA-Z][a-zA-Z0-9\-]*)\s*=\s*["']([^"']*)["']""")

    override fun select(html: String, cssSelector: String): List<HtmlElement> {
        val elements = parseHtml(html)
        return filterBySelector(elements, cssSelector)
    }

    override fun selectFirst(html: String, cssSelector: String): HtmlElement? {
        return select(html, cssSelector).firstOrNull()
    }

    override fun extractText(html: String, cssSelector: String): String {
        return selectFirst(html, cssSelector)?.text.orEmpty()
    }

    override fun extractAttr(html: String, cssSelector: String, attrName: String): String? {
        return selectFirst(html, cssSelector)?.attr(attrName)
    }

    private fun parseHtml(html: String): List<SimpleHtmlElement> {
        val elements = mutableListOf<SimpleHtmlElement>()
        
        tagRegex.findAll(html).forEach { match ->
            val tagName = match.groupValues[1].ifEmpty { match.groupValues[4] }.lowercase()
            val attrString = match.groupValues[2].ifEmpty { match.groupValues[5] }
            val content = match.groupValues[3]
            
            val attributes = attrRegex.findAll(attrString)
                .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
            
            val children = if (content.isNotEmpty()) {
                parseHtml(content)
            } else {
                emptyList()
            }
            
            // 提取文本内容
            val textContent = content.replace(Regex("<[^>]*>"), "").trim()
            
            elements.add(
                SimpleHtmlElement(
                    tagName = tagName,
                    text = textContent,
                    ownText = textContent,
                    attributes = attributes,
                    childElements = children
                )
            )
        }
        
        return elements
    }

    private fun filterBySelector(elements: List<HtmlElement>, selector: String): List<HtmlElement> {
        val results = mutableListOf<HtmlElement>()
        
        when {
            // 标签选择器
            selector.matches(Regex("^[a-zA-Z][a-zA-Z0-9]*$")) -> {
                elements.forEach { element ->
                    if (element.tagName == selector.lowercase()) {
                        results.add(element)
                    }
                    results.addAll(filterBySelector(element.children(), selector))
                }
            }
            // 类选择器
            selector.startsWith(".") -> {
                val className = selector.substring(1)
                elements.forEach { element ->
                    if (element.attr("class")?.contains(className) == true) {
                        results.add(element)
                    }
                    results.addAll(filterBySelector(element.children(), selector))
                }
            }
            // ID 选择器
            selector.startsWith("#") -> {
                val id = selector.substring(1)
                elements.forEach { element ->
                    if (element.attr("id") == id) {
                        results.add(element)
                    }
                    results.addAll(filterBySelector(element.children(), selector))
                }
            }
            // 通配符
            selector == "*" -> {
                results.addAll(elements)
                elements.forEach { element ->
                    results.addAll(filterBySelector(element.children(), selector))
                }
            }
            // 属性选择器 [@attr] or [@attr=value]
            selector.startsWith("@") -> {
                val attrPart = selector.substring(1)
                elements.forEach { element ->
                    val match = when {
                        attrPart.contains("=") -> {
                            val parts = attrPart.split("=", limit = 2)
                            element.attr(parts[0]) == parts[0].trim('"', '\'')
                        }
                        else -> element.attr(attrPart) != null
                    }
                    if (match) {
                        results.add(element)
                    }
                    results.addAll(filterBySelector(element.children(), selector))
                }
            }
            // 组合选择器 (空格分隔)
            selector.contains(" ") -> {
                val parts = selector.split("\\s+".toRegex())
                var currentElements = elements
                for (part in parts) {
                    currentElements = filterBySelector(currentElements, part)
                }
                results.addAll(currentElements)
            }
            // 后代选择器 (>)
            selector.contains(">") -> {
                val parts = selector.split("\\s*>\\s*".toRegex())
                var currentElements = elements
                for ((index, part) in parts.withIndex()) {
                    currentElements = if (index == 0) {
                        filterBySelector(currentElements, part)
                    } else {
                        currentElements.flatMap { it.children() }
                            .filter { element ->
                                when {
                                    part.matches(Regex("^[a-zA-Z][a-zA-Z0-9]*$")) -> 
                                        element.tagName == part.lowercase()
                                    part.startsWith(".") -> 
                                        element.attr("class")?.contains(part.substring(1)) == true
                                    part.startsWith("#") -> 
                                        element.attr("id") == part.substring(1)
                                    else -> false
                                }
                            }
                    }
                }
                results.addAll(currentElements)
            }
        }
        
        return results.distinct()
    }
}
