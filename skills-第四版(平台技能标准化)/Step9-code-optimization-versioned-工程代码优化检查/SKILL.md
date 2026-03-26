---
name: Step9-code-optimization-versioned-工程代码优化检查
description: >-
  [Step9] 优化 grp-capability-element 工程下 element-server-com 模块中 @Service 和 @Repository 类的代码质量（多版本适配）。
  根据工程 pom.xml 中的 version 自动选择对应版本的检查与修复规则。
  包括修复 SQL 注入漏洞（值拼接参数化、动态表名/列名白名单校验）、增强日志记录（Lombok @Slf4j）、
  清理冗余代码（StringBuffer→StringBuilder、冗余变量、嵌套条件简化）。
  当用户要求优化要素模块代码、修复 SQL 注入、增强日志、清理代码、代码审查优化时使用此技能。
  适用于使用 BaseDAO/JdbcTemplate 持久层的 Spring Boot Java 项目。
---

# 工程代码优化检查与修复（多版本适配）

## 概述

本技能是工程代码优化检查与修复的**多版本适配版本**。不同工程版本的代码优化规范可能存在差异，本技能会根据工程 `pom.xml` 中声明的 `<version>` 自动选择对应版本的检查与修复规则执行。

## 支持的版本

| 工程版本 | 规则目录 | 说明 |
|----------|----------|------|
| `3.6.0-SNAPSHOT` | [versions/3.6.0-SNAPSHOT/](versions/3.6.0-SNAPSHOT/) | 基线版本，完整工程代码优化检查与修复规则 |
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

按照加载的版本规则执行具体的代码优化检查或修复操作。规则文件中包含：
- 工作流程（5 步）
- SQL 注入修复规则
- 日志增强规则
- 代码优化规则
- 安全约束
- 所有相关的 examples/、templates/、scripts/ 引用

---

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 修复 SQL 注入安全漏洞 | "SQL注入修复"、"安全修复"、"参数化查询" | 版本检测 → SQL 注入修复 |
| 增强日志记录 | "日志增强"、"添加日志"、"@Slf4j" | 版本检测 → 日志增强 |
| 清理冗余代码 | "代码清理"、"代码优化"、"冗余清理" | 版本检测 → 代码清理 |
| 全量优化（三合一） | "工程代码优化"、"模块优化"、"代码质量优化" | 版本检测 → 全量优化 |

## 前置条件

- 工程为 Java 微服务项目（Spring Boot/Cloud）
- 使用 BaseDAO/JdbcTemplate 持久层
- 项目根目录（或所在 module）存在 `pom.xml`（用于版本检测）
- Lombok 依赖已在项目中声明

## 目录结构

```
工程代码优化检查-versioned/
├── SKILL.md                          # 本文件 - 版本检测与路由入口
└── versions/
    ├── 3.6.0-SNAPSHOT/               # 基线版本
    │   ├── REFERENCE.md                  # 该版本完整规则
    │   ├── scripts/                  # 检查/修复规则脚本
    │   │   ├── sql-injection-rules.md
    │   │   ├── logging-rules.md
    │   │   ├── code-optimization-rules.md
    │   │   └── safety-constraints.md
    │   ├── examples/                 # 示例文件
    │   │   ├── change-report.md
    │   │   └── workflow-demo.md
    │   └── templates/                # 模板文件
    │       ├── report-template.md
    │       └── skip-files.md
    ├── 3.6.1-SNAPSHOT/               # 增量修订版本
    │   ├── REFERENCE.md
    │   ├── scripts/
    │   │   ├── sql-injection-rules.md
    │   │   ├── logging-rules.md
    │   │   ├── code-optimization-rules.md
    │   │   └── safety-constraints.md
    │   ├── examples/
    │   │   ├── change-report.md
    │   │   └── workflow-demo.md
    │   └── templates/
    │       ├── report-template.md
    │       └── skip-files.md
    └── 3.7.0-SNAPSHOT/               # 新特性版本
        ├── REFERENCE.md
        ├── scripts/
        │   ├── sql-injection-rules.md
        │   ├── logging-rules.md
        │   ├── code-optimization-rules.md
        │   └── safety-constraints.md
        ├── examples/
        │   ├── change-report.md
        │   └── workflow-demo.md
        └── templates/
            ├── report-template.md
            └── skip-files.md
```

## 添加新版本

如需支持新的工程版本：

1. 在 `versions/` 下创建新的版本目录，如 `versions/3.8.0-SNAPSHOT/`
2. 复制最接近的版本目录内容作为基础
3. 修改 `REFERENCE.md` 中该版本特有的规则差异
4. 在本文件（SKILL.md）的「支持的版本」表格中添加新条目
5. 更新版本映射规则中的匹配逻辑
