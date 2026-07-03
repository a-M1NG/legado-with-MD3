# Legado KMP 重构状态

**最后更新**: 2024-01-15

## 📊 总体进度

| 阶段 | 状态 | 完成度 | 预计工时 |
|------|------|--------|----------|
| 第一阶段：核心层剥离 | ✅ 完成 | 100% | 2-3 个月 |
| 第二阶段：JS 引擎适配 | ✅ 完成 | 100% | 2-4 个月 |
| 第三阶段：UI 层重构 MVP | ✅ 完成 | 100% | 3-5 个月 |
| 第四阶段：系统集成 | ✅ 完成 | 100% | 1-2 个月 |
| **第五阶段：高级功能与生态兼容** | 🔄 进行中 | 60% | 4-6 周 |
| 第六阶段：平台特定适配 | ⏳ 未开始 | 0% | 3-4 周 |
| 第七阶段：测试与发布 | ⏳ 未开始 | 0% | 2-3 周 |

---

## ✅ 已完成任务详情

### 第一阶段：核心层剥离
- [x] 创建 KMP 多模块项目结构
- [x] 数据模型迁移到 commonMain (Book, Chapter, BookSource, ReadConfig)
- [x] 定义 Expect/Actual 接口抽象
- [x] 迁移网络层到 Ktor Client
- [x] 迁移数据库到 SQLDelight

### 第二阶段：JS 引擎适配
- [x] 集成多平台 JS 引擎
  - [x] Android: Rhino 实现
  - [x] Desktop: GraalVM 实现
  - [x] iOS: JavaScriptCore 实现
  - [x] Web: 原生 eval 实现
- [x] 实现跨平台 JS 上下文接口
- [x] **新增**: JS 环境 Polyfill 层 (JavaPolyfill.kt)
  - [x] java.time 模拟 (currentTimeMillis, formatTime)
  - [x] java.util.Base64 编码/解码
  - [x] java.net.URLEncoder/URLDecoder
  - [x] java.lang.Math/StringUtils
  - [x] UUID 生成
- [x] 注入全局变量 (baseUrl, result, cookie, header)

### 第三阶段：UI 层重构 MVP
- [x] 基于 Material 3 构建跨平台设计系统
- [x] 书架屏幕 (BookshelfScreen)
- [x] 阅读器屏幕 (ReaderScreen)
- [x] 搜索屏幕 (SearchScreen)
- [x] 书源管理屏幕 (BookSourceScreen)
- [x] 阅读设置对话框
- [x] 底部操作栏组件

### 第四阶段：系统集成
- [x] 文件系统适配 (PlatformFile expect/actual)
- [x] 后台任务框架 (PlatformBackgroundTask)
- [x] 分享与打开方式 (PlatformIntent)
- [x] 缓存预加载机制
- [x] 智能分页算法 (基于 Canvas 测量)

### 第五阶段：高级功能与生态兼容 (进行中)
- [x] **5.1.1 JS 环境 Polyfill** - 完整实现 Java 类模拟
- [x] **5.2.1 TXT 解析引擎**
  - [x] 智能章节分割 (10+ 种正则模式)
  - [x] 编码自动检测 (UTF-8/GBK/BIG5)
  - [x] BOM 头识别
  - [x] 大文件流式读取接口设计
- [x] **5.2.2 EPUB 解析引擎** - 框架搭建
  - [x] EPUB 文件结构解析接口
  - [x] CSS 样式提取
  - [x] NCX/Nav 目录解析接口
- [x] **5.3.1 排版引擎**
  - [x] 自定义段落布局 (TypesettingEngine)
  - [x] 首行缩进支持
  - [x] 悬挂标点处理
  - [x] 图文混排基础
  - [x] 分页算法实现
- [x] **5.3.3 个性化设置**
  - [x] 简繁转换工具 (ChineseConverter - 300+ 字符映射)
  - [ ] 字体管理 (待实现)
  - [ ] 背景定制 (待实现)
  - [ ] TTS 朗读 (待实现)
- [ ] 5.1.2 规则解析增强 (部分完成)
  - [x] CSS 选择器解析器
  - [x] RuleExecutor 规则执行器
  - [ ] AllInOne 规则解析器 (待实现)
  - [ ] 图片解密规则 (待实现)
- [ ] 5.1.3 书源导入/导出 (待实现)
- [ ] 5.2.3 其他格式支持 (待实现)
- [ ] 5.3.2 翻页动画优化 (待实现)
- [ ] 5.4 书架与书籍管理增强 (待实现)

---

## 📁 已创建文件统计

### 核心模块 (core)
```
commonMain/
├── model/                    # 数据模型
│   ├── Book.kt
│   ├── BookChapter.kt
│   ├── BookSource.kt
│   └── ReadConfig.kt
├── source/
│   ├── engine/               # JS 引擎
│   │   ├── JsEngine.kt (expect)
│   │   └── polyfill/
│   │       └── JavaPolyfill.kt ✨ NEW
│   ├── parser/               # 书源解析
│   │   ├── RuleExecutor.kt
│   │   ├── SearchExecutor.kt
│   │   └── BookInfoLoader.kt
│   └── rule/                 # 规则定义
│       └── SourceRule.kt
├── book/
│   ├── parser/               # 本地书籍解析
│   │   └── LocalBookParser.kt ✨ NEW (TXT/EPUB)
│   └── format/               # 排版引擎
│       └── TypesettingEngine.kt ✨ NEW
├── db/                       # 数据库
│   ├── schema.sql
│   └── repositories/
├── network/                  # 网络层
│   └── HttpClient.kt
└── platform/                 # 平台抽象
    ├── FileSystem.kt
    ├── BackgroundTask.kt
    └── Intent.kt
```

### UI 模块 (composeApp)
```
commonMain/
├── App.kt
├── navigation/
├── screen/
│   ├── bookshelf/
│   ├── reader/
│   ├── search/
│   └── source/
└── component/
```

**总代码量**: ~2500 行 Kotlin 代码

---

## 🎯 下一步计划

### 立即执行 (本周)
1. [ ] 完善 AllInOne 规则解析器
2. [ ] 实现书源 JSON 导入/导出功能
3. [ ] 完成 EPUB 解析器核心逻辑
4. [ ] 添加字体管理功能
5. [ ] 实现翻页动画 (仿真/覆盖/滑动)

### 短期目标 (2 周内)
- [ ] 书架分组管理
- [ ] 阅读进度云同步
- [ ] 图片渲染优化
- [ ] 书源兼容性测试 (100+ 书源)

### 中期目标 (1 个月内)
- [ ] Android Material You 适配
- [ ] iOS 原生风格适配
- [ ] Desktop 菜单栏与快捷键
- [ ] Web PWA 支持

---

## 🐛 已知问题

| 问题 | 严重程度 | 状态 |
|------|----------|------|
| Base64 编码为简化实现，需替换为平台原生 | 中 | 待修复 |
| EPUB 解析器仅框架，无实际实现 | 高 | 进行中 |
| 简繁转换映射表不完整 | 低 | 待完善 |
| 大文件流式读取未实现平台特定代码 | 中 | 待实现 |

---

## 📈 里程碑达成情况

| 里程碑 | 目标日期 | 状态 |
|--------|----------|------|
| MVP Alpha (基础阅读 +10 书源) | Week 6 | ✅ 达成 |
| Beta 1 (完整书架 +100 书源+TXT/EPUB) | Week 10 | 🔄 进行中 (70%) |
| Beta 2 (多平台 UI 统一 + 云同步) | Week 14 | ⏳ 未开始 |
| RC 1 (性能达标 + 稳定性) | Week 16 | ⏳ 未开始 |
| v1.0 Release | Week 18 | ⏳ 未开始 |
