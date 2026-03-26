---
name: Step4-dao-model-governance-versioned-DAO-Model层治理
description: "[Step4] Java微服务DAO-Model层治理检查与修复工具（多版本适配）。根据工程pom.xml中的version自动选择对应版本的检查与修复规则。检查并修复目录命名(imp→impl)、DAO层接口/实现分离、DTO/VO/Query分类归档、核心四层目录完整性、mapper XML目录对应、DAO mapper/entity分离、Model dto/vo/query分类、公共模块结构等问题，不改变业务逻辑。执行链路：Step1→Step2→Step3→Step4（当前）→Step5→Step6→Step7→Step8→Step9。当用户提到'Step4检查'、'Step4修复'、'DAO层治理'、'Model层治理'、'DAO-Model检查'时使用。"
---

# S2 DAO-Model 层治理（多版本适配）

## 概述

本技能是 S2 DAO-Model 层治理检查与修复的**多版本适配版本**。不同工程版本的 DAO/Model 层规范可能存在差异，本技能会根据工程 `pom.xml` 中声明的 `<version>` 自动选择对应版本的检查与修复规则执行。

**执行链路定位**：S2 是代码级重构的第二步（底层 DAO/Model 文件归位），前置依赖 S1（架构依赖守卫），后续为 S3（Service层治理）。执行链路：S1→**S2（当前）**→S3→S4→S5。

**检查项来源**：合并自原 P1（目录结构）的 DAO/Model 相关项 + 原 P2（代码组织）的 DAO/Model 相关项。

## 支持的版本

| 工程版本 | 规则目录 | 说明 |
|----------|----------|------|
| `3.6.0-SNAPSHOT` | [versions/3.6.0-SNAPSHOT/](versions/3.6.0-SNAPSHOT/) | 基线版本，完整 S2 DAO-Model 层治理检查与修复规则 |
| `3.6.1-SNAPSHOT` | [versions/3.6.1-SNAPSHOT/](versions/3.6.1-SNAPSHOT/) | 增量修订版本 |
| `3.7.0-SNAPSHOT` | [versions/3.7.0-SNAPSHOT/](versions/3.7.0-SNAPSHOT/) | 新特性版本 |

## 版本检测与路由流程

### Step 0: 自动检测工程版本

**此步骤在所有其他步骤之前执行，不可跳过。**

1. 读取当前工程根目录（或所在 module）的 `pom.xml` 文件
2. 提取 `<version>` 标签的值（如 `3.6.0-SNAPSHOT`）
3. 如果当前 POM 没有直接声明 `<version>`，则查找 `<parent>` 中的 `<version>`
4. 如果是子模块，可向上查找父 POM 获取版本号
5. 将提取到的版本号与下方版本映射表匹配

### 版本映射规则

```
提取到的版本号 → 匹配规则 → 加载的版本规则目录
───────────────────────────────────────────────
3.6.0-SNAPSHOT   → 精确匹配    → versions/3.6.0-SNAPSHOT/
3.6.1-SNAPSHOT   → 精确匹配    → versions/3.6.1-SNAPSHOT/
3.7.0-SNAPSHOT   → 精确匹配    → versions/3.7.0-SNAPSHOT/
3.6.x-SNAPSHOT   → 模糊匹配 3.6 → versions/3.6.1-SNAPSHOT/ (取3.6系列最新)
3.7.x-SNAPSHOT   → 模糊匹配 3.7 → versions/3.7.0-SNAPSHOT/ (取3.7系列最新)
其他版本          → 降级到默认   → versions/3.6.0-SNAPSHOT/ (基线版本)
```

**模糊匹配逻辑：**
- 精确匹配优先：如果版本号与某个版本目录完全匹配，直接使用
- 主版本+次版本匹配：取该系列下最新的版本目录（版本号数值最大的）
- 无匹配时降级到 `3.6.0-SNAPSHOT` 作为基线默认版本

### Step 0 执行示例

```
检测到工程版本: 3.6.0-SNAPSHOT
→ 精确匹配: versions/3.6.0-SNAPSHOT/
→ 加载该版本的 REFERENCE.md 作为执行规则

检测到工程版本: 3.6.2-SNAPSHOT
→ 无精确匹配
→ 模糊匹配 3.6 系列，最新为 3.6.1-SNAPSHOT
→ 加载 versions/3.6.1-SNAPSHOT/REFERENCE.md

检测到工程版本: 4.0.0-SNAPSHOT
→ 无精确匹配，无系列匹配
→ 降级到基线: versions/3.6.0-SNAPSHOT/REFERENCE.md
→ 输出警告: "当前工程版本 4.0.0-SNAPSHOT 无匹配规则，已降级使用 3.6.0-SNAPSHOT 基线规则"
```

### Step 1: 加载版本规则

根据 Step 0 确定的版本目录，读取该目录下的 `REFERENCE.md` 文件，获取该版本的完整检查与修复规则。

**加载路径模板：** `versions/{matched-version}/REFERENCE.md`

### Step 2: 执行检查或修复

按照加载的版本规则执行具体的 DAO-Model 层治理检查或修复操作。规则文件中包含：
- S2 检查项清单（8 项）
- 检查流程
- 修复流程与策略
- 安全约束
- 所有相关的 examples/、templates/、scripts/ 引用

---

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查 DAO/Model 层是否符合规范 | "S2检查"、"DAO层治理检查"、"Model层检查"、"DAO-Model检查" | 版本检测 → DAO-Model 层检查 |
| 修复不符合规范的 DAO/Model 层 | "S2修复"、"DAO层治理修复"、"Model层修复"、"DAO-Model修复" | 版本检测 → DAO-Model 层修复 |
| 先检查再修复 | "S2检查并修复"、"DAO-Model层治理全流程" | 版本检测 → 检查 + 修复 |

## 前置条件

- 工程为 Java 微服务项目（Spring Boot/Cloud）
- 采用 Controller → Service → DAO → Model 分层架构
- 项目根目录（或所在 module）存在 `pom.xml`（用于版本检测）
- S1（架构依赖守卫）已完成
- 修复前用户需确认修复计划

## 目录结构

```
S2-dao-model-governance-versioned-DAO-Model层治理/
├── SKILL.md                          # 本文件 - 版本检测与路由入口
└── versions/
    ├── 3.6.0-SNAPSHOT/               # 基线版本
    │   ├── REFERENCE.md                  # 该版本完整规则
    │   ├── scripts/                  # 检查/修复规则脚本
    │   │   ├── check-rules.md
    │   │   ├── refactor-rules.md
    │   │   └── safety-constraints.md
    │   ├── examples/                 # 示例文件
    │   │   ├── check-report.md
    │   │   └── migration-flow.md
    │   └── templates/                # 标准目录结构模板
    │       └── standard-directory.md
    ├── 3.6.1-SNAPSHOT/               # 增量修订版本
    │   ├── REFERENCE.md
    │   ├── scripts/
    │   │   ├── check-rules.md
    │   │   ├── refactor-rules.md
    │   │   └── safety-constraints.md
    │   ├── examples/
    │   │   ├── check-report.md
    │   │   └── migration-flow.md
    │   └── templates/
    │       └── standard-directory.md
    └── 3.7.0-SNAPSHOT/               # 新特性版本
        ├── REFERENCE.md
        ├── scripts/
        │   ├── check-rules.md
        │   ├── refactor-rules.md
        │   └── safety-constraints.md
        ├── examples/
        │   ├── check-report.md
        │   └── migration-flow.md
        └── templates/
            └── standard-directory.md
```

## 添加新版本

如需支持新的工程版本：

1. 在 `versions/` 下创建新的版本目录，如 `versions/3.8.0-SNAPSHOT/`
2. 复制最接近的版本目录内容作为基础
3. 修改 `REFERENCE.md` 中该版本特有的规则差异
4. 在本文件（SKILL.md）的「支持的版本」表格中添加新条目
5. 更新版本映射规则中的匹配逻辑
