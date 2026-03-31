---
name: Step3-ui-component-standard-versioned-UI组件规范
description: "[Step3] 前端UI组件规范检查与代码生成工具（多版本适配）。根据工程package.json中的version自动选择对应版本的规范规则。支持3.6.0-SNAPSHOT、3.6.1-SNAPSHOT、3.7.0-SNAPSHOT等多版本。执行链路：Step1→Step2→Step3（当前）→Step4→Step5。当用户提到'UI组件规范'、'颜色检查'、'字体规范'、'按钮规范'、'表格规范'、'录入规范'、'生成组件'、'组件样例'时使用。"
---
# 前端 UI 组件规范检查与代码生成（多版本适配）

## 概述

本技能是前端 UI 组件规范检查与代码生成的**多版本适配版本**。基于《一体化系统界面规范》文档，对前端 UI 组件的颜色、字体、按钮、录入控件、表格等进行标准化约束与检查。同时提供组件级代码模板和标准样例，支持快速生成符合规范的组件代码。

## 支持的版本

| 工程版本 | 规则目录 | 说明 |
|----------|----------|------|
| `3.6.0-SNAPSHOT` | [versions/3.6.0-SNAPSHOT/](versions/3.6.0-SNAPSHOT/) | 基线版本，完整 UI 组件规范规则 |
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

### Step 1: 加载版本规则

根据 Step 0 确定的版本目录，读取该目录下的 `REFERENCE.md` 文件，获取该版本的完整检查与生成规则。

### Step 2: 执行检查或生成

按照加载的版本规则执行具体的 UI 组件规范检查或代码生成操作。

---

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查 UI 组件是否符合规范 | "UI组件规范"、"颜色检查"、"字体规范"、"按钮规范"、"表格规范"、"录入规范" | 版本检测 → 组件规范检查 |
| 查看组件标准样例 | "组件样例"、"按钮样例"、"表格样例"、"录入框样例" | 版本检测 → 展示样例 |
| 使用模板生成组件 | "生成组件"、"生成表格"、"生成表单"、"生成查询面板" | 版本检测 → 模板生成 |
| 组件规范全量检查 | "UI全量检查"、"前端规范审查" | 版本检测 → 5 类规则全量检查 |

## 前置条件

- **建议先执行 Step 1 和 Step 2**，确保项目结构和模块划分已规范
- 工程为 Vue 3 + TypeScript 前端项目
- 项目根目录存在 `package.json`（用于版本检测）

## 目录结构

```
Step3-ui-component-standard-versioned-UI组件规范/
├── SKILL.md                          # 本文件 - 版本检测与路由入口
└── versions/
    ├── 3.6.0-SNAPSHOT/               # 基线版本
    │   ├── REFERENCE.md              # 该版本完整规则
    │   ├── examples/                 # 组件样例（E01-E07）
    │   ├── templates/                # 组件级模板（T05-T10）
    │   └── scripts/                  # 检查规则（S01-S05）
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
