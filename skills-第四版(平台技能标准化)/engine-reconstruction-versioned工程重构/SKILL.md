---
name: 工程重构
description: 四层架构Maven工程检查与重构工具（多版本适配）。根据工程pom.xml中的version自动选择对应版本的重构规则。支持3.6.0-SNAPSHOT、3.6.1-SNAPSHOT、3.7.0-SNAPSHOT等多版本。当用户提到"架构检查"、"目录规范"、"结构校验"、"四层架构审查"、"架构重构"、"结构迁移"、"四层架构改造"时使用。
---
# 独立工程重构（四层架构 + 自包含根POM）

## 概述

本技能是四层架构工程重构的**增强版本**，在原有 `engine-reconstruction-versioned工程重构` 基础上新增了 **自包含根POM生成** 能力。

### 与原技能的核心区别

| 对比项 | 原版（engine-reconstruction-versioned） | 本版（standalone） |
|--------|----------------------------------------|-------------------|
| 根POM parent | 继承平台父POM（如 `grp-platform-server`） | 使用 `spring-boot-starter-parent` |
| 依赖版本管理 | 由外部平台父POM提供 | 自包含 `dependencyManagement` |
| 独立编译/启动 | 需在完整平台环境中 | **可独立编译启动** |
| 私服仓库 | 由平台父POM配置 | 自包含 `<repositories>` |
| 适用场景 | 平台内模块重构 | **模块提取为独立可运行项目** |

## 支持的版本

| 工程版本 | 规则目录 | 说明 |
|----------|----------|------|
| `3.6.0-SNAPSHOT` | [versions/3.6.0-SNAPSHOT/](versions/3.6.0-SNAPSHOT/) | 基线版本 |

## 版本检测与路由流程

### Step 0: 自动检测工程版本

**此步骤在所有其他步骤之前执行，不可跳过。**

1. 读取当前工程根目录的 `pom.xml` 文件（或各子模块的 `pom.xml`）
2. 提取 `<version>` 标签的值（如 `3.6.0-SNAPSHOT`）
3. 如果没有直接声明 `<version>`，从 `<parent>` 中提取
4. 如果仍然找不到版本，使用 `3.6.0-SNAPSHOT` 作为默认版本

### 版本映射规则

```
提取到的版本号 → 匹配规则 → 加载的版本规则目录
───────────────────────────────────────────────
3.6.0-SNAPSHOT   → 精确匹配    → versions/3.6.0-SNAPSHOT/
3.6.x-SNAPSHOT   → 模糊匹配 3.6 → versions/3.6.0-SNAPSHOT/
其他版本          → 降级到默认   → versions/3.6.0-SNAPSHOT/ (基线版本)
```

### Step 1: 加载版本规则

根据 Step 0 确定的版本目录，读取该目录下的 `REFERENCE.md` 文件，获取该版本的完整检查与重构规则。

### Step 2: 执行重构

按照加载的版本规则执行架构重构操作。

---

## 使用场景

| 场景 | 触发关键词 |
|------|-----------|
| 将平台模块提取为独立可运行项目 | "独立工程重构"、"独立项目"、"standalone重构" |
| 四层架构重构 + 可独立启动 | "工程重构并可启动"、"独立四层架构" |

## 前置条件

- 工程为 Maven 多模块项目（或扁平多模块目录）
- 重构前用户需自行备份工程（或确认已备份）

## 目录结构

```
standalone-engine-reconstruction-独立工程重构/
├── SKILL.md                          # 本文件
└── versions/
    └── 3.6.0-SNAPSHOT/
        ├── REFERENCE.md              # 完整重构规则（含自包含根POM生成）
        ├── scripts/
        │   ├── check-rules.md        # 架构检查规则
        │   ├── safety-constraints.md # 安全约束
        │   ├── refactor-rules.md     # 重构执行规则
        │   ├── module-classification.md  # 模块归类规则
        │   └── root-pom-generation.md    # 自包含根POM生成规则（核心新增）
        ├── templates/
        │   ├── target-structure.md    # 目标目录结构
        │   ├── standalone-root-pom.xml   # 自包含根POM模板（核心新增）
        │   ├── module-pom.xml
        │   ├── capability-pom.xml
        │   ├── aggregation-pom.xml
        │   └── experience-pom.xml
        └── examples/
            ├── module-mapping-table.md
            ├── refactor-plan.md
            └── refactor-report.md
```
