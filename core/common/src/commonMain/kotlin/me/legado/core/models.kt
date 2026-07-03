package me.legado.core

/**
 * 书籍数据模型 - KMP 共享代码
 */
data class Book(
    val bookUrl: String,
    val name: String,
    val author: String,
    val coverUrl: String? = null,
    val intro: String? = null,
    val chapterListSize: Int = 0,
    val lastChapter: String? = null,
    val lastChapterTime: Long = 0L,
    val wordCount: Long = 0L,
    val canUpdate: Boolean = true,
    val order: Int = 0,
    val origin: String = "",
    val originName: String = "",
    val variable: String? = null,
    val readConfig: ReadConfig? = null
)

/**
 * 章节数据模型
 */
data class Chapter(
    val url: String,
    val title: String,
    val index: Int,
    val isVolume: Boolean = false,
    val resourceUrl: String? = null,
    val tag: String? = null
)

/**
 * 阅读配置
 */
data class ReadConfig(
    val theme: Int = 0,
    val bgColor: String = "#FFFFFF",
    val textColor: String = "#000000",
    val fontScale: Float = 1.0f,
    val lineSpacing: Float = 1.0f,
    val paragraphSpacing: Float = 1.0f,
    val pageAnimation: PageAnimation = PageAnimation.COVER,
    val darkMode: Boolean = false
)

enum class PageAnimation {
    COVER,      // 覆盖
    SLIDE,      // 滑动
    FADE,       // 淡入淡出
    SCROLL,     // 滚动
    NONE        // 无动画
}

/**
 * 书源数据模型
 */
data class BookSource(
    val bookSourceUrl: String,
    val bookSourceName: String,
    val bookSourceGroup: String? = null,
    val loginUrl: String? = null,
    val header: String? = null,
    val enabled: Boolean = true,
    val enabledExplore: Boolean = true,
    val searchWeight: Int = 0,
    val exploreUrl: String? = null,
    val ruleSearch: RuleSearch? = null,
    val ruleBookInfo: RuleBookInfo? = null,
    val ruleToc: RuleToc? = null,
    val ruleContent: RuleContent? = null,
    val ruleExplore: RuleExplore? = null
)

data class RuleSearch(
    val checkKeyWord: String? = null,
    val searchUrl: String,
    val bookList: String? = null,
    val name: String? = null,
    val author: String? = null,
    val intro: String? = null,
    val kind: String? = null,
    val latestChapter: String? = null,
    val wordCount: String? = null,
    val coverUrl: String? = null,
    val bookUrl: String? = null
)

data class RuleBookInfo(
    val init: String? = null,
    val name: String? = null,
    val author: String? = null,
    val intro: String? = null,
    val kind: String? = null,
    val latestChapter: String? = null,
    val wordCount: String? = null,
    val coverUrl: String? = null,
    val tocUrl: String? = null
)

data class RuleToc(
    val chapterList: String? = null,
    val chapterName: String? = null,
    val chapterUrl: String? = null,
    val wordCount: String? = null,
    val updateTime: String? = null
)

data class RuleContent(
    val content: String,
    val replaceRegex: String? = null,
    val imageStyle: String? = null,
    val payAction: String? = null
)

data class RuleExplore(
    val bookList: String? = null,
    val name: String? = null,
    val author: String? = null,
    val intro: String? = null,
    val kind: String? = null,
    val latestChapter: String? = null,
    val wordCount: String? = null,
    val coverUrl: String? = null,
    val bookUrl: String? = null
)
