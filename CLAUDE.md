# CLAUDE.md
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
这是一个 Minecraft NeoForge 1.21.1 的纯客户端模组，是 ExtendedAE Plus 的客户端版本（ExtendedAE Plus Client，简称 EAEP Client）。模组使用 Kotlin 编写，依赖 FishLib 库。

## Development Environment Setup

### Prerequisites
- JDK 21 (NeoForge 21.1.206 requires Java 21)
- Gradle 8.5+ (使用 Gradle Wrapper)
- IntelliJ IDEA (推荐) 或 VS Code

### IDE Configuration
- **IntelliJ IDEA**: 
  - 导入为 Gradle 项目
  - 自动应用 NeoForge 开发环境配置
- **VS Code**: 使用 `.vscode/launch.json` 提供的调试配置

### Debug Launch Configurations (VS Code)
预先配置的调试配置：
- **Client**: 运行客户端开发环境 (含模组加载)
- **Data**: 运行数据生成任务
- **GameTestServer**: 运行GameTest测试服务器
- **Server**: 运行服务端测试环境 (仅功能测试)

## Build Commands

### 核心构建命令
```bash
# 完整构建模组
./gradlew build

# 运行客户端开发环境 (热重载支持)
./gradlew runClient

# 数据生成任务
./gradlew runData

# 调试测试
./gradlew runGameTestServer

# 发布构建
./gradlew publishToMavenLocal
```

### 子项目 ExtendedAE_Plus 构建
注意：这是独立的 NeoForge Java 项目，使用不同的构建系统：
```bash
cd ExtendedAE_Plus
./gradlew build  # 子项目使用 Groovy DSL
```

### 快速开发迭代
```bash
# 增量构建，跳过测试
./gradlew build -x test

# 仅编译客户端
./gradlew compileKotlinFixedClient
```

## Architecture Overview

### 模组架构分层
客户端模组采用插件式架构，核心分为以下层：

1. **API层**：提供与JC2D、AE2集成接口
2. **Service层**：核心功能实现（缓存、统计、事件管理）
3. **UI层**：覆盖AE2默认UI的自定义界面
4. **Integration层**：模组兼容性和整合

### 数据流架构
```
用户交互 → Mixin注入 → 事件监听器 → 缓存处理 → AE2/OE2 API调用
    ↓
界面显示 ← 统计聚合 ← 缓存同步 ← 数据持久化
```

### 扩展点设计
- **Mixin注入点**：有策略地在AE2 UI生命周期各阶段hook
- **配置系统**：基于FishLib的动态配置重载
- **集成管理**：面向接口的模块化集成架构

## Module Dependencies

### 运行时依赖
```
ExtendedAE Plus Client
├── AE2 (19.2.17)              # 核心依赖，所有UI框架
├── ExtendedAE Ex Pattern Provider # 扩展功能依赖
├── FishLib (1.0.6)            # 配置管理和集成框架
├── Kotlin for Forge           # Kotlin运行时支持
└── JEI/EMI/REI              # 配方查看器集成
```

### 开发依赖层级
```
编译时：
- NeoForge MDK 21.1.206
- Parchment 1.21.1-2024.11.13
- Mixins (0.8.5)
- MixinExtras

运行时：
- Minecraft 1.21.1
- Loading Overlay JIT
```

## Core Extension Points

### 1. AE2 UI Mixin注入
- **主入口**：`MixinEncodingTerminal.java` - 样板终端重写
- **辅助点**：确认界面、访问终端、存储终端等
- **模式**：Callback-based注入，保留原生行为

### 2. 缓存系统架构
- **供应器缓存**：`SupplierCache` - 样板供应器集合管理
- **切割刀缓存**：`CuttingToolCache` - 石英切割工具缓存
- **合成缓存**：`CraftingCache` - 合成计划缓存
- **统计**：`PatternUsageStats` - 使用频率统计

### 3. 配置系统
基于FishLib的声明式配置：
```kotlin
// 声明式配置，支持运行时重载
val config by spec("extendedae_plus_client") {
    boolean("enable_cache", true) {
        "Enable or disable caching mechanism"
    }
    int("cache_timeout", 30) {
        "Cache expiration timeout (seconds)" 
    }
}
```

## Development Patterns

### 1. Mixin开发模式
- 使用Java编写核心mixin（提供更稳定字节码处理）
- Java Mixin(@Mixin) → Kotlin Service → Feature实现

### 2. 事件处理模式
```kotlin
// FishLib自动注册模式
@InitObject
object ScreenEvents : IFScreenEvent {
    @EventHandler
    fun onScreenOpen(event: ScreenOpenEvent) {
        // 自动清理缓存逻辑
    }
}
```

### 3. 状态同步策略
显式状态同步，避免隐式依赖：
```kotlin
// 防御性编程：检查所有前置条件
if (AEClientHelper.isJeiActive()) {
    syncJEIInventory()
}
if (FTBTeamsHelper.isInstalled()) {
    syncTeamData()
}
```

## Quick Commands Reference

### 日常开发
- 构建增量：`./gradlew build` (200-300ms)
- 启动客户端：`./gradlew runClient` (15s-30s)
- 生成资产：`./gradlew runData` (10s)
- 全清理：`./gradlew clean && ./gradlew build` (60-90s)

### 调试模式
- 开启调试日志：`-Dfml.debug=true`
- 跳过数据生成：`-Dfml.skipData=true`
- 禁用缓存：修改config `enable_cache=false`

### 发布模式
```bash
# 发布快照版本
BUILD_TYPE=snapshot ./gradlew build
# 发布正式版本
BUILD_TYPE=release ./gradlew build
```

## Critical Gotchas

### 1. 纯客户端限制
⚠️ **重要**：所有代码必须标记为客户端专用 `@Mod(..., dist = [Dist.CLIENT])`
避免使用：服务端API、服务器存储、世界保存

### 2. Mixin兼容性
- 优先使用`@Shadow`而非`@Redirect`以减少冲突
- 使用`@WrapMethod`处理复杂的钩入逻辑
- 始终验证target类存在性

### 3. 配置热重载
- 配置修改后需调用：`FishLibConfig.reloadAll()`
- 缓存相关的配置变化需要显式清除：`SupplierCache.clear()`

### 4. Kotlin/NeoForge集成
- 使用Kotlin DSL模板时，确保Gradle版本匹配
- Kotlin for Forge插件需要在不同环境配置兼容

## CI/CD Notes
构建触发器（在提交信息中）：
- `skip-ci`：跳过CI构建
- `build-release`：正式构建（无版本号后缀）
- `snapshot`：快照构建（自动追加版本号）

环境变量：
- `GITHUB_RUN_NUMBER`：CI构建号注入
- `BUILD_TYPE`：构建类型控制（release/snapshot）