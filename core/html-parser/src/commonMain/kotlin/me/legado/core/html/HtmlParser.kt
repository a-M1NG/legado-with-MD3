package me.legado.core.html

/**
 * HTML 解析器接口
 * 用于解析书源返回的 HTML 内容
 */
interface HtmlParser {
    /**
     * 根据 CSS 选择器提取元素
     * @param html HTML 字符串
     * @param cssSelector CSS 选择器
     * @return 匹配的元素列表
     */
    fun select(html: String, cssSelector: String): List<HtmlElement>
    
    /**
     * 根据 CSS 选择器提取第一个元素
     */
    fun selectFirst(html: String, cssSelector: String): HtmlElement?
    
    /**
     * 提取文本内容
     */
    fun extractText(html: String, cssSelector: String): String
    
    /**
     * 提取属性值
     */
    fun extractAttr(html: String, cssSelector: String, attrName: String): String?
}

/**
 * HTML 元素抽象
 */
interface HtmlElement {
    val tagName: String
    val text: String
    val ownText: String
    fun attr(name: String): String?
    fun attrs(): Map<String, String>
    fun select(cssSelector: String): List<HtmlElement>
    fun selectFirst(cssSelector: String): HtmlElement?
    fun children(): List<HtmlElement>
    fun parent(): HtmlElement?
}
