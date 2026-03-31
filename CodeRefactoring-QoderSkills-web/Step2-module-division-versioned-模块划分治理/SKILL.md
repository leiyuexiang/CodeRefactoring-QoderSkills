---
name: Step2-module-division-versioned-模块划分治理
description: "[Step2] 前端业务模块划分检查与治理工具（多版本适配）。根据工程package.json中的version自动选择对应版本的治理规则。支持3.6.0-SNAPSHOT、3.6.1-SNAPSHOT、3.7.0-SNAPSHOT等多版本。执行链路：Step1→Step2（当前）→Step3→Step4→Step5。当用户提到'模块划分检查'、'模块结构治理'、'业务模块拆分'、'模块依赖检查'、'子模块结构'、'创建业务模块'时使用。"
---
# 前端业务模块划分检查与治理（多版本适配）

## 概述

本技能是前端业务模块划分检查与治理的**多版本适配版本**。基于 framework-web2-server 项目分包规范，对前端项目的业务模块划分、模块内部结构、模块间依赖、模块大小等进行标准化约束与治理。不同工程版本可能存在模块规范差异，本技能会根据工程 `package.json` 中声明的 `version` 自动选择对应版本的治理规则执行。

## 支持的版本

| 工程版本 | 规则目录 | 说明 |
|----------|----------|------|
| `3.6.0-SNAPSHOT` | [versions/3.6.0-SNAPSHOT/](versions/3.6.0-SNAPSHOT/) | 基线版本，完整模块划分治理规则 |
| `3.6.1-SNAPSHOT` | [versions/3.6.1-SNAPSHOT/](versions/3.6.1-SNAPSHOT/) | 增量修订版本 |
| `3.7.0-SNAPSHOT` | [versions/3.7.0-SNAPSHOT/](versions/3.7.0-SNAPSHOT/) | 新特性版本 |

## 版本检测与路由流程

### Step 0: 自动检测工程版本

**此步骤在所有其他步骤之前执行，不可跳过。**

1. 读取当前工程根目录的 `package.json` 文件
2. 提取 `version` 字段的值
3. 如果无法提取版本号，则使用 `3.6.0-SNAPSHOT` 版本
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

**模糊匹配逻辑：**
- 精确匹配优先：如果版本号与某个版本目录完全匹配，直接使用
- 主版本+次版本匹配：取该系列下最新的版本目录（版本号数值最大的）
- 无匹配时降级到 `3.6.0-SNAPSHOT` 作为基线默认版本

### Step 0 执行示例

```
检测到工程版本: 3.6.0-SNAPSHOT
→ 精确匹配: versions/3.6.0-SNAPSHOT/
→ 加载该版本的 REFERENCE.md 作为执行规则

检测到工程版本: 3.7.0-SNAPSHOT
→ 精确匹配: versions/3.7.0-SNAPSHOT/
→ 加载 versions/3.7.0-SNAPSHOT/REFERENCE.md

检测到工程版本: 4.0.0-SNAPSHOT
→ 无精确匹配，无系列匹配
→ 降级到基线: versions/3.6.0-SNAPSHOT/REFERENCE.md
→ 输出警告: "当前工程版本 4.0.0-SNAPSHOT 无匹配规则，已降级使用 3.6.0-SNAPSHOT 基线规则"
```

### Step 1: 加载版本规则

根据 Step 0 确定的版本目录，读取该目录下的 `REFERENCE.md` 文件，获取该版本的完整检查与治理规则。

### Step 2: 执行检查或治理

按照加载的版本规则执行具体的模块划分检查或治理操作。规则文件中包含：
- 模块划分标准与决策矩阵
- 检查规则清单（S10 系列 10 条规则）
- 模块脚手架生成模板
- 安全约束
- 所有相关的 examples/、templates/、scripts/ 引用

---

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查模块划分是否合规 | "模块划分检查"、"模块结构检查"、"模块依赖检查" | 版本检测 → 模块检查 |
| 创建新业务模块 | "创建业务模块"、"新建模块"、"模块脚手架" | 版本检测 → 脚手架生成 |
| 治理模块划分问题 | "模块结构治理"、"业务模块拆分"、"模块整改" | 版本检测 → 检查 + 治理 |

## 前置条件

- **必须先执行 Step 1**，确保项目已具备标准顶层目录结构
- 工程为 Vue 3 + TypeScript 前端项目
- 项目根目录存在 `package.json`（用于版本检测）

## 目录结构

```
Step2-module-division-versioned-模块划分治理/
├── SKILL.md                          # 本文件 - 版本检测与路由入口
└── versions/
    ├── 3.6.0-SNAPSHOT/               # 基线版本
    │   ├── REFERENCE.md              # 该版本完整规则
    │   ├── examples/                 # 模块划分示例
    │   ├── templates/                # 模块脚手架模板
    │   └── scripts/                  # 检查规则脚本
    ├── 3.6.1-SNAPSHOT/               # 增量修订版本
    │   ├── REFERENCE.md
    │   ├── examples/
    │   ├── templates/
    │   └── scripts/
    └── 3.7.0-SNAPSHOT/               # 新特性版本
        ├── REFERENCE.md
        ├── examples/
        ├── templates/
        └── scripts/
```

## 添加新版本

如需支持新的工程版本：

1. 在 `versions/` 下创建新的版本目录，如 `versions/3.8.0-SNAPSHOT/`
2. 复制最接近的版本目录内容作为基础
3. 修改 `REFERENCE.md` 中该版本特有的规则差异
4. 在本文件（SKILL.md）的「支持的版本」表格中添加新条目
5. 更新版本映射规则中的匹配逻辑
