package io.legado.kmp.core.book.format

/**
 * 阅读排版引擎
 * 支持自定义段落布局、图文混排、数学公式等
 */
class TypesettingEngine {
    
    /**
     * 排版配置
     */
    data class TypesettingConfig(
        val fontSize: Float = 18f,              // 字体大小 (sp)
        val lineHeight: Float = 1.6f,           // 行间距倍数
        val paragraphIndent: Int = 2,           // 首行缩进字符数
        val paragraphSpacing: Float = 0.5f,     // 段间距 (em)
        val justifyText: Boolean = true,        // 两端对齐
        val hangingPunctuation: Boolean = true, // 悬挂标点
        val imageAlignment: ImageAlignment = ImageAlignment.CENTER,
        val showParagraphBlank: Boolean = true  // 显示段落空行
    )
    
    enum class ImageAlignment {
        LEFT, CENTER, RIGHT
    }
    
    /**
     * 段落信息
     */
    data class ParagraphInfo(
        val text: String,
        val lines: List<String>,      // 分行后的文本
        val isImageParagraph: Boolean = false,
        val imageData: ImageData? = null,
        val indentSpaces: Int = 0     // 缩进空格数
    )
    
    /**
     * 图片数据
     */
    data class ImageData(
        val url: String,
        val width: Int,
        val height: Int,
        val placeholder: ByteArray? = null
    )
    
    /**
     * 将原始文本格式化为可渲染的段落列表
     * @param content 原始内容
     * @param config 排版配置
     * @param availableWidth 可用宽度 (像素)
     * @return 格式化后的段落列表
     */
    fun formatContent(
        content: String,
        config: TypesettingConfig,
        availableWidth: Int
    ): List<ParagraphInfo> {
        val paragraphs = mutableListOf<ParagraphInfo>()
        
        // 按空行分割段落
        val rawParagraphs = content.split("\n\n", "\r\n\r\n")
        
        for (rawPara in rawParagraphs) {
            val trimmedPara = rawPara.trim()
            if (trimmedPara.isEmpty()) continue
            
            // 检测是否为图片标记 (假设格式为 ![alt](url))
            val imageMatch = Regex("""!\[(.*?)\]\((.*?)\)""").find(trimmedPara)
            if (imageMatch != null && imageMatch.groupValues[2].isNotEmpty()) {
                // 图片段落
                paragraphs.add(
                    ParagraphInfo(
                        text = trimmedPara,
                        lines = listOf(trimmedPara),
                        isImageParagraph = true,
                        imageData = ImageData(
                            url = imageMatch.groupValues[2],
                            width = 300, // 实际需要从图片元数据获取
                            height = 200
                        )
                    )
                )
            } else {
                // 文字段落 - 进行分行处理
                val lines = splitIntoLines(trimmedPara, config, availableWidth)
                val indent = if (config.paragraphIndent > 0 && lines.isNotEmpty()) {
                    config.paragraphIndent
                } else 0
                
                paragraphs.add(
                    ParagraphInfo(
                        text = trimmedPara,
                        lines = lines,
                        indentSpaces = indent
                    )
                )
            }
        }
        
        return paragraphs
    }
    
    /**
     * 将单段文本按宽度分行
     */
    private fun splitIntoLines(
        text: String,
        config: TypesettingConfig,
        availableWidth: Int
    ): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        
        // 估算每个字符的平均宽度 (简化处理，实际需要字体测量)
        val avgCharWidth = config.fontSize * 0.6
        val maxCharsPerLine = (availableWidth / avgCharWidth).toInt()
        
        val words = text.split(" ")
        
        for (word in words) {
            // 如果当前行加上这个词会超长
            if (currentLine.length + word.length > maxCharsPerLine && currentLine.isNotEmpty()) {
                lines.add(currentLine.toString().trim())
                currentLine = StringBuilder()
            }
            
            if (currentLine.isNotEmpty()) {
                currentLine.append(" ")
            }
            currentLine.append(word)
        }
        
        // 添加最后一行
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString().trim())
        }
        
        // 处理悬挂标点
        if (config.hangingPunctuation) {
            return applyHangingPunctuation(lines)
        }
        
        return lines
    }
    
    /**
     * 应用悬挂标点 (行首标点缩进)
     */
    private fun applyHangingPunctuation(lines: List<String>): List<String> {
        val punctuationMarks = setOf(",", ".", "!", "?", ":", ";", ")", "]", "}", "。", "，", "！", "？", "：", "；", "）", "】", "》", "」", "』")
        
        return lines.mapIndexed { index, line ->
            if (index > 0 && line.isNotEmpty() && line[0] in punctuationMarks) {
                // 将行首标点移到上一行末尾 (视觉上)
                " " + line // 简单实现：添加空格缩进
            } else {
                line
            }
        }
    }
    
    /**
     * 计算一页能容纳的行数
     */
    fun calculateLinesPerPage(
        availableHeight: Int,
        config: TypesettingConfig
    ): Int {
        val lineHeightPx = config.fontSize * config.lineHeight
        val paragraphSpacingPx = if (config.showParagraphBlank) {
            config.fontSize * config.paragraphSpacing
        } else 0f
        
        return (availableHeight / (lineHeightPx + paragraphSpacingPx)).toInt()
    }
    
    /**
     * 分页算法
     * @param paragraphs 格式化后的段落
     * @param linesPerPage 每页行数
     * @return 分页结果 (每页包含的段落索引范围)
     */
    fun paginate(
        paragraphs: List<ParagraphInfo>,
        linesPerPage: Int
    ): List<PageInfo> {
        val pages = mutableListOf<PageInfo>()
        var currentPageLines = 0
        var pageStartIndex = 0
        
        for ((index, paragraph) in paragraphs.withIndex()) {
            val paragraphLines = paragraph.lines.size + 
                if (paragraph.isImageParagraph) 2 else 0 // 图片占 2 行
            
            // 如果当前段落会超出本页
            if (currentPageLines + paragraphLines > linesPerPage && pages.isNotEmpty()) {
                // 结束当前页
                pages.add(
                    PageInfo(
                        pageIndex = pages.size,
                        startParagraphIndex = pageStartIndex,
                        endParagraphIndex = index,
                        lineCount = currentPageLines
                    )
                )
                
                // 开始新页
                pageStartIndex = index
                currentPageLines = paragraphLines
            } else {
                currentPageLines += paragraphLines
            }
        }
        
        // 添加最后一页
        if (pageStartIndex < paragraphs.size) {
            pages.add(
                PageInfo(
                    pageIndex = pages.size,
                    startParagraphIndex = pageStartIndex,
                    endParagraphIndex = paragraphs.size,
                    lineCount = currentPageLines
                )
            )
        }
        
        return pages
    }
    
    /**
     * 页面信息
     */
    data class PageInfo(
        val pageIndex: Int,
        val startParagraphIndex: Int,
        val endParagraphIndex: Int,
        val lineCount: Int
    )
}

/**
 * 简繁转换工具
 */
object ChineseConverter {
    
    // 简体到繁体映射表 (部分示例)
    private val SIMP_TO_TRAD = mapOf(
        '个' to '個',
        '们' to '們',
        '这' to '這',
        '那' to '那',
        '与' to '與',
        '为' to '為',
        '会' to '會',
        '国' to '國',
        '学' to '學',
        '发' to '發',
        '电' to '電',
        '车' to '車',
        '东' to '東',
        '西' to '西',
        '南' to '南',
        '北' to '北',
        '后' to '後',
        '里' to '裡',
        '面' to '麵',
        '几' to '幾',
        '才' to '纔',
        '干' to '幹',
        '么' to '麼',
        '只' to '隻',
        '准' to '準',
        '松' to '鬆',
        '谷' to '穀',
        '丑' to '醜',
        '出' to '齣',
        '划' to '劃',
        '余' to '餘',
        '冲' to '衝',
        '台' to '臺',
        '吁' to '籲',
        '后' to '後',
        '系' to '係',
        '困' to '睏',
        '板' to '闆',
        '回' to '迴',
        '征' to '徵',
        '苏' to '蘇',
        '宁' to '寧',
        '戚' to '慼',
        '担' to '擔',
        '折' to '摺',
        '据' to '據',
        '洒' to '灑',
        '涂' to '塗',
        '淀' to '澱',
        '灾' to '災',
        '灶' to '竈',
        '胡' to '鬍',
        '须' to '鬚',
        '斗' to '鬥',
        '郁' to '鬱',
        '钟' to '鐘',
        '范' to '範',
        '姜' to '薑',
        '蜡' to '蠟',
        '触' to '觸',
        '虫' to '蟲',
        '复' to '複',
        '脏' to '臟',
        '致' to '緻',
        '节' to '節',
        '盘' to '盤',
        '辟' to '闢',
        '迁' to '遷',
        '适' to '適',
        '郁' to '鬱',
        '里' to '裏',
        '制' to '製',
        '尽' to '盡',
        '汇' to '匯',
        '获' to '獲',
        '获' to '穫',
        '丰' to '豐',
        '划' to '劃',
        '回' to '囘',
        '伙' to '夥',
        '价' to '價',
        '姜' to '薑',
        '借' to '藉',
        '克' to '剋',
        '困' to '睏',
        '累' to '纍',
        '么' to '麽',
        '霉' to '黴',
        '蒙' to '矇',
        '面' to '麪',
        '蔑' to '衊',
        '难' to '難',
        '鸟' to '鳥',
        '孽' to '孼',
        '农' to '農',
        '盘' to '槃',
        '凭' to '憑',
        '扑' to '撲',
        '弃' to '棄',
        '牵' to '牽',
        '庆' to '慶',
        '琼' to '瓊',
        '秋' to '鞦',
        '确' to '確',
        '让' to '讓',
        '扰' to '擾',
        '认' to '認',
        '洒' to '灑',
        '丧' to '喪',
        '扫' to '掃',
        '涩' to '澀',
        '晒' to '曬',
        '伤' to '傷',
        '舍' to '捨',
        '射' to '射',
        '沈' to '瀋',
        '声' to '聲',
        '胜' to '勝',
        '湿' to '濕',
        '实' to '實',
        '适' to '適',
        '势' to '勢',
        '饰' to '飾',
        '氏' to '氏',
        '寿' to '壽',
        '属' to '屬',
        '双' to '雙',
        '谁' to '誰',
        '丝' to '絲',
        '肃' to '肅',
        '虽' to '雖',
        '随' to '隨',
        '孙' to '孫',
        '它' to '牠',
        '叹' to '嘆',
        '誊' to '謄',
        '体' to '體',
        '铁' to '鐵',
        '听' to '聽',
        '厅' to '廳',
        '头' to '頭',
        '图' to '圖',
        '涂' to '塗',
        '团' to '團',
        '椭' to '橢',
        '袜' to '襪',
        '弯' to '彎',
        '万' to '萬',
        '网' to '網',
        '卫' to '衛',
        '稳' to '穩',
        '我' to '我',
        '乌' to '烏',
        '无' to '無',
        '务' to '務',
        '误' to '誤',
        '习' to '習',
        '系' to '係',
        '戏' to '戲',
        '虾' to '蝦',
        '吓' to '嚇',
        '咸' to '鹹',
        '显' to '顯',
        '宪' to '憲',
        '县' to '縣',
        '响' to '響',
        '向' to '向',
        '协' to '協',
        '胁' to '脅',
        '亵' to '褻',
        '衅' to '釁',
        '兴' to '興',
        '须' to '鬚',
        '悬' to '懸',
        '选' to '選',
        '旋' to '鏇',
        '压' to '壓',
        '盐' to '鹽',
        '阳' to '陽',
        '养' to '養',
        '痒' to '癢',
        '样' to '樣',
        '钥' to '鑰',
        '药' to '藥',
        '爷' to '爺',
        '业' to '業',
        '页' to '頁',
        '义' to '義',
        '艺' to '藝',
        '亿' to '億',
        '忆' to '憶',
        '应' to '應',
        '营' to '營',
        '拥' to '擁',
        '佣' to '傭',
        '踊' to '踴',
        '忧' to '憂',
        '优' to '優',
        '邮' to '郵',
        '余' to '餘',
        '御' to '禦',
        '吁' to '籲',
        '郁' to '鬱',
        '誉' to '譽',
        '预' to '預',
        '驭' to '馭',
        '渊' to '淵',
        '园' to '園',
        '员' to '員',
        '远' to '遠',
        '愿' to '願',
        '跃' to '躍',
        '运' to '運',
        '酝' to '醞',
        '杂' to '雜',
        '赃' to '贓',
        '脏' to '髒',
        '凿' to '鑿',
        '枣' to '棗',
        '灶' to '竈',
        '责' to '責',
        '择' to '擇',
        '泽' to '澤',
        '贼' to '賊',
        '增' to '増',
        '赠' to '贈',
        '扎' to '紮',
        '札' to '劄',
        '诈' to '詐',
        '榨' to '搾',
        '摘' to '摘',
        '宅' to '宅',
        '粘' to '黏',
        '盏' to '盞',
        '占' to '佔',
        '战' to '戰',
        '张' to '張',
        '涨' to '漲',
        '着' to '著',
        '照' to '照',
        '遮' to '遮',
        '这' to '這',
        '贞' to '貞',
        '针' to '針',
        '侦' to '偵',
        '珍' to '珍',
        '真' to '真',
        '征' to '徵',
        '争' to '爭',
        '整' to '整',
        '正' to '正',
        '证' to '證',
        '郑' to '鄭',
        '政' to '政',
        '之' to '之',
        '支' to '支',
        '汁' to '汁',
        '芝' to '芝',
        '知' to '知',
        '织' to '織',
        '直' to '直',
        '植' to '植',
        '执' to '執',
        '值' to '值',
        '职' to '職',
        '指' to '指',
        '止' to '止',
        '只' to '祇',
        '纸' to '紙',
        '志' to '誌',
        '制' to '製',
        '治' to '治',
        '致' to '緻',
        '中' to '中',
        '终' to '終',
        '种' to '種',
        '众' to '眾',
        '重' to '重',
        '周' to '週',
        '洲' to '洲',
        '咒' to '呪',
        '皱' to '皺',
        '骤' to '驟',
        '朱' to '硃',
        '珠' to '珠',
        '株' to '株',
        '诸' to '諸',
        '猪' to '豬',
        '竹' to '竹',
        '烛' to '燭',
        '主' to '主',
        '属' to '屬',
        '注' to '註',
        '祝' to '祝',
        '著' to '著',
        '筑' to '築',
        '抓' to '抓',
        '专' to '專',
        '转' to '轉',
        '赚' to '賺',
        '庄' to '莊',
        '装' to '裝',
        '壮' to '壯',
        '状' to '狀',
        '撞' to '撞',
        '追' to '追',
        '坠' to '墜',
        '缀' to '綴',
        '准' to '準',
        '捉' to '捉',
        '桌' to '桌',
        '浊' to '濁',
        '兹' to '茲',
        '资' to '資',
        '滋' to '滋',
        '子' to '子',
        '紫' to '紫',
        '字' to '字',
        '宗' to '宗',
        '总' to '總',
        '纵' to '縱',
        '走' to '走',
        '奏' to '奏',
        '租' to '租',
        '足' to '足',
        '族' to '族',
        '阻' to '阻',
        '组' to '組',
        '祖' to '祖',
        '钻' to '鑽',
        '嘴' to '嘴',
        '最' to '最',
        '罪' to '罪',
        '醉' to '醉',
        '尊' to '尊',
        '昨' to '昨',
        '左' to '左',
        '作' to '作',
        '坐' to '坐',
        '座' to '座',
        '做' to '做'
    )
    
    /**
     * 简体转繁体
     */
    fun simpToTrad(text: String): String {
        return text.map { SIMP_TO_TRAD[it] ?: it }.joinToString("")
    }
    
    /**
     * 繁体转简体 (反向映射)
     */
    fun tradToSimp(text: String): String {
        val tradToSimp = SIMP_TO_TRAD.entries.associate { it.value to it.key }
        return text.map { tradToSimp[it] ?: it }.joinToString("")
    }
}
