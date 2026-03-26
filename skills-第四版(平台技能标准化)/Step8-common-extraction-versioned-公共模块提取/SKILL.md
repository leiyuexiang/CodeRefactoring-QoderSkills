---
name: Step8-common-extraction-versioned-公共模块提取
description: "[Step8] Java微服务公共模块提取工具（多版本适配）。根据工程pom.xml中的version自动选择对应版本的检查与修复规则。将能力层模块中的公共代码包（util、cache、constant、enums、exception、config）提取到grp-{module}-common模块下，统一管理公共代码，不改变业务逻辑。执行链路：Step1→Step2→Step3→Step4→Step5→Step6→Step7→Step8（当前）→Step9。当用户提到'Step8检查'、'Step8修复'、'公共模块提取'、'common提取'、'util提取'、'公共代码归集'时使用。"
---

# 公共模块提取（多版本适配）

## 概述

本技能是公共模块提取的**多版本适配版本**。将能力层模块（如 element-service）中的公共代码包（util、cache、constant、enums、exception、config）提取到 `grp-{module}-common` 模块下，实现公共代码统一管理。不同工程版本可能存在目录结构差异，本技能会根据工程 `pom.xml` 中声明的 `<version>` 自动选择对应版本的规则执行。

## 支持的版本

| 工程版本 | 规则目录 | 说明 |
|----------|----------|------|
| `3.6.0-SNAPSHOT` | [versions/3.6.0-SNAPSHOT/](versions/3.6.0-SNAPSHOT/) | 基线版本 |
| `3.6.1-SNAPSHOT` | [versions/3.6.1-SNAPSHOT/](versions/3.6.1-SNAPSHOT/) | 增量修订版本 |
| `3.7.0-SNAPSHOT` | [versions/3.7.0-SNAPSHOT/](versions/3.7.0-SNAPSHOT/) | 新特性版本 |

## 版本检测与路由流程

### Step 0: 自动检测工程版本

**此步骤在所有其他步骤之前执行，不可跳过。**

1. 读取当前工程根目录的 `pom.xml` 文件
2. 提取 `<version>` 标签的值（如 `3.6.0-SNAPSHOT`）
3. 如果根 POM 没有直接声明 `<version>`，则使用 `3.6.0-SNAPSHOT` 版本
4. 将提取到的版本号与下方版本映射表匹配

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

### Step 1: 加载版本规则

根据 Step 0 确定的版本目录，读取该目录下的 `REFERENCE.md` 文件，获取该版本的完整检查与提取规则。

**加载路径模板：** `versions/{matched-version}/REFERENCE.md`

### Step 2: 执行检查或提取

按照加载的版本规则执行具体的公共代码检查或提取操作。规则文件中包含：
- 提取范围定义（6类公共代码包）
- 检查规则清单
- 提取执行流程
- 安全约束
- 所有相关的 examples/、templates/、scripts/ 引用

---

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查哪些公共代码需要提取 | "公共模块检查"、"common检查"、"Step8检查" | 版本检测 → 公共代码检查 |
| 将公共代码提取到common模块 | "公共模块提取"、"common提取"、"Step8修复" | 版本检测 → 公共代码提取 |
| 先检查再提取 | "检查并提取公共代码"、"Step8全流程" | 版本检测 → 检查 + 提取 |

## 前置条件

- Step7（接口与命名规范）已执行完成
- 工程为 Maven 多模块项目
- 项目中存在 `grp-{module}-common` 模块（或 `grp-common-{module}`）
- 重构前用户需自行备份工程（或确认已备份）

## 目录结构

```
Step8-common-extraction-versioned-公共模块提取/
├── SKILL.md                          # 本文件 - 版本检测与路由入口
└── versions/
    ├── 3.6.0-SNAPSHOT/               # 基线版本
    │   ├── REFERENCE.md              # 该版本完整规则
    │   ├── scripts/
    │   │   ├── check-rules.md        # 检查规则（哪些包应提取）
    │   │   ├── refactor-rules.md     # 提取迁移规范
    │   │   └── safety-constraints.md # 安全约束
    │   ├── templates/
    │   │   └── standard-directory.md # common模块标准目录结构
    │   └── examples/
    │       ├── check-report.md       # 检查报告示例
    │       └── migration-flow.md     # 迁移流程示例
    ├── 3.6.1-SNAPSHOT/               # 增量修订版本
    │   └── ...
    └── 3.7.0-SNAPSHOT/               # 新特性版本
        └── ...
```

## 添加新版本

如需支持新的工程版本：

1. 在 `versions/` 下创建新的版本目录，如 `versions/3.8.0-SNAPSHOT/`
2. 复制最接近的版本目录内容作为基础
3. 修改 `REFERENCE.md` 中该版本特有的规则差异
4. 在本文件（SKILL.md）的「支持的版本」表格中添加新条目
