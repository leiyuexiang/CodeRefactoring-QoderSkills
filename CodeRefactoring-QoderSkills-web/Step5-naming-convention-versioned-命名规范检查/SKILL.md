---
name: Step5-naming-convention-versioned-命名规范检查
description: "[Step5] 前端命名规范检查与修复工具（多版本适配）。根据工程package.json中的version自动选择对应版本的规范规则。支持3.6.0-SNAPSHOT、3.6.1-SNAPSHOT、3.7.0-SNAPSHOT等多版本。执行链路：Step1→Step2→Step3→Step4→Step5（当前）。当用户提到'命名规范'、'组件命名'、'文件命名'、'变量命名'、'CSS类名'、'命名检查'、'命名审查'时使用。"
---
# 前端命名规范检查与修复（多版本适配）

## 概述

本技能是前端命名规范检查与修复的**多版本适配版本**。基于阿里前端开发规范和《一体化系统界面规范》，对前端项目的组件命名、文件命名、变量命名、CSS 类名、事件函数命名等进行标准化约束与检查。不同工程版本可能存在命名规范差异，本技能会根据工程 `package.json` 中声明的 `version` 自动选择对应版本的检查规则执行。

## 支持的版本

| 工程版本 | 规则目录 | 说明 |
|----------|----------|------|
| `3.6.0-SNAPSHOT` | [versions/3.6.0-SNAPSHOT/](versions/3.6.0-SNAPSHOT/) | 基线版本，完整命名规范检查规则 |
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

根据 Step 0 确定的版本目录，读取该目录下的 `REFERENCE.md` 文件，获取该版本的完整检查规则。

### Step 2: 执行检查或修复

按照加载的版本规则执行具体的命名规范检查或修复操作。

---

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查命名是否符合规范 | "命名规范"、"命名检查"、"命名审查" | 版本检测 → 命名检查 |
| 修复命名违规 | "命名修复"、"重命名"、"命名整改" | 版本检测 → 检查 + 修复 |
| 检查特定类别命名 | "组件命名"、"文件命名"、"变量命名"、"CSS类名" | 版本检测 → 分类检查 |

## 前置条件

- **建议先执行 Step 1 至 Step 4**，确保项目结构、模块划分、组件规范和布局规范已就绪
- 工程为 Vue 3 + TypeScript 前端项目
- 项目根目录存在 `package.json`（用于版本检测）

## 目录结构

```
Step5-naming-convention-versioned-命名规范检查/
├── SKILL.md                          # 本文件 - 版本检测与路由入口
└── versions/
    ├── 3.6.0-SNAPSHOT/               # 基线版本
    │   ├── REFERENCE.md              # 该版本完整规则
    │   ├── examples/                 # 命名示例
    │   ├── templates/                # 模板（预留）
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
