# Legado KMP 重构项目

基于 Kotlin Multiplatform 的跨平台阅读应用重构项目，支持 Android、iOS、Desktop (Windows/macOS/Linux) 和 Web 平台。

## 项目结构

```
legado-kmp/
├── core/                      # KMP 核心模块
│   ├── common/               # 共享代码 (数据模型、业务逻辑)
│   │   ├── src/
│   │   │   ├── commonMain/   # 所有平台共享的代码
│   │   │   ├── androidMain/  # Android 特定实现
│   │   │   ├── iosMain/      # iOS 特定实现
│   │   │   ├── desktopMain/  # Desktop (JVM) 特定实现
│   │   │   └── jsMain/       # Web (JS) 特定实现
│   │   └── build.gradle.kts
│   ├── data/                 # 数据层 (数据库、存储)
│   ├── network/              # 网络层 (HTTP 客户端)
│   ├── storage/              # 本地存储抽象
│   └── js-engine/            # JS 引擎适配层
│
├── platforms/                # 平台特定应用
│   ├── android/              # Android App
│   ├── ios/                  # iOS App
│   ├── desktop/              # Desktop App (Compose Desktop)
│   └── web/                  # Web App (Compose for Web)
│
├── modules/                  # 遗留模块 (逐步迁移中)
│   ├── book/
│   ├── rhino/
│   └── web/
│
└── app/                      # 当前 Android 主应用 (兼容模式)
```

## 技术栈

### 共享层 (KMP)
- **语言**: Kotlin 2.0+
- **并发**: Kotlin Coroutines & Flow
- **序列化**: kotlinx.serialization
- **网络**: Ktor Client
- **数据库**: SQLDelight
- **依赖注入**: Koin

### UI 框架
- **Android**: Jetpack Compose
- **iOS**: SwiftUI / Compose Multiplatform
- **Desktop**: Compose Desktop
- **Web**: Compose for Web

### 平台特定实现
- **Android**: Rhino JS 引擎
- **iOS**: JavaScriptCore
- **Desktop**: GraalVM JS 引擎
- **Web**: Native JavaScript

## 核心功能

### 已完成
- ✅ KMP 项目结构搭建
- ✅ 跨平台数据模型定义 (Book, Chapter, BookSource 等)
- ✅ 平台抽象层 (文件系统、JS 引擎接口)
- ✅ Android/Desktop/iOS/Web 平台基础实现

### 进行中
- 🔄 JS 引擎完整实现 (各平台)
- 🔄 数据库层迁移 (SQLDelight)
- 🔄 网络请求层迁移 (Ktor)

### 待开发
- ⏳ 书源规则解析引擎
- ⏳ 文本渲染引擎
- ⏳ UI 组件库 (Compose Multiplatform)
- ⏳ 书架、阅读页等核心页面

## 快速开始

### 前置要求
- JDK 21+
- Android Studio Hedgehog+
- Xcode 15+ (iOS 开发)
- Gradle 8.5+

### 构建命令

```bash
# 编译所有平台
./gradlew assemble

# Android Debug
./gradlew :platforms:android:assembleDebug

# Desktop
./gradlew :platforms:desktop:run

# iOS (需要 macOS)
./gradlew :platforms:ios:linkDebugFrameworkIosArm64

# Web
./gradlew :platforms:web:jsBrowserDevelopmentRun
```

## 迁移策略

采用"绞杀者模式"渐进式重构：

1. **Phase 1** (1-2 个月): 核心业务逻辑 KMP 化
   - 数据模型迁移
   - 网络层、存储层抽象
   - JS 引擎适配

2. **Phase 2** (2-3 个月): UI 层重构
   - Compose Multiplatform 设计系统
   - 核心页面重写 (书架、阅读页)

3. **Phase 3** (1-2 个月): 多平台适配
   - iOS 原生能力对接
   - Desktop 打包发布
   - Web 优化

4. **Phase 4** (持续): 功能完善与优化
   - 性能优化
   - 新特性开发
   - 平台差异化体验

## 贡献指南

欢迎贡献！请参考以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交变更 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交 Pull Request

## 许可证

本项目继承原 Legado 项目的 GPL-3.0 许可证。

## 相关链接

- [原 Legado 项目](https://github.com/gedoor/legado)
- [Kotlin Multiplatform 文档](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
