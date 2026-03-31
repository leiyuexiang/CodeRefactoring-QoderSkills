# S9 项目结构治理规则（3.6.0-SNAPSHOT）

## 概述

本文档定义了前端项目目录结构检查与治理的完整规则集。基于 framework-web2-server 项目分包规范和阿里前端开发规范，对 Vue 3 + TypeScript 项目的顶层目录、分层架构、组件分层、引用方向等进行标准化约束。

**本技能提供两大功能：**
1. **项目结构检查**：扫描项目目录，发现不合规的结构问题
2. **项目结构治理**：根据检查结果，自动或辅助调整目录结构；或通过脚手架模板初始化标准项目

## 使用场景

| 用户意图 | 触发关键词 | 执行功能 |
|---------|-----------|----------|
| 验证项目目录结构是否合规 | "项目结构检查"、"目录规范"、"分层架构审查" | 功能一：结构检查 |
| 初始化新项目标准结构 | "项目初始化"、"脚手架生成"、"搭建项目" | 功能二：脚手架生成 |
| 治理现有项目结构问题 | "项目结构治理"、"目录整改"、"结构优化" | 功能一 + 功能二：检查 + 治理 |

---

## 检查项总览

| 编号 | 检查项 | 严重级别 | 说明 |
|------|--------|---------|------|
| S9-01 | 顶层目录完整性 | ERROR | src/ 下必须包含 7 个标准目录 |
| S9-02 | assets 目录结构 | WARNING | 静态资源应按 styles/icons/images 划分 |
| S9-03 | components 分层 | ERROR | 必须分为 common/layout/business 三层 |
| S9-04 | 复合组件目录形式 | WARNING | 多文件组件应使用目录形式组织 |
| S9-05 | framework 目录结构 | WARNING | 框架层应按 router/store/plugins 等拆分 |
| S9-06 | services 目录结构 | WARNING | 服务层应包含 http/ 和 api/ 子目录 |
| S9-07 | composables 命名 | ERROR | 文件名必须以 use- 前缀开头 |
| S9-08 | 禁止跨层引用 | ERROR | 各层之间引用必须遵循依赖方向 |

---

## 标准项目分层架构

```
src/
├── assets/          # 静态资源层（全局样式、图标、图片）
│   ├── styles/      # 全局样式（variables.scss、index.scss）
│   ├── icons/       # 图标资源
│   └── images/      # 图片资源
├── components/      # 共享组件层
│   ├── common/      # 通用组件（与业务无关，可跨项目复用）
│   ├── layout/      # 布局组件（页面骨架结构）
│   └── business/    # 业务组件（与业务绑定但跨模块共享）
├── composables/     # 全局组合式函数（use-xxx.ts）
├── modules/         # 业务模块层（按业务域划分）
├── framework/       # 框架层
│   ├── router/      # 路由配置（含 modules/ 子目录）
│   ├── store/       # 状态管理（含 modules/ 子目录）
│   ├── plugins/     # 插件注册
│   ├── directives/  # 全局指令
│   └── config/      # 全局配置
├── services/        # 服务层
│   ├── http/        # HTTP 客户端（Axios 实例 + 拦截器）
│   └── api/         # 公共 API 定义
├── utils/           # 工具函数层
└── types/           # 全局类型定义
```

---

## 合法引用方向

```
modules/ → components/   ✅    components/ → modules/   ❌
modules/ → composables/  ✅    utils/      → modules/   ❌
modules/ → services/     ✅    services/   → modules/   ❌
modules/ → utils/        ✅    modules/A → modules/B/内部  ❌
modules/ → types/        ✅
components/ → composables/ ✅
components/ → utils/     ✅
framework/ → modules/    ✅（仅路由注册）
```

---

## 功能一：项目结构检查流程

### Phase 1: 扫描项目结构
1. 扫描 `src/` 下所有目录和文件
2. 识别各层级目录是否存在

### Phase 2: 逐项检查
按 S9-01 至 S9-08 的规则逐项检查，详见 `scripts/check-rules.md`

### Phase 3: 生成检查报告
输出格式参考 `examples/` 目录中的样例

### Phase 4: 建议修复方案
针对每个违规项提供具体修复建议

---

## 功能二：脚手架生成流程

### Phase 1: 确认项目信息
1. 确认项目名称和类型
2. 确认要生成的标准目录

### Phase 2: 生成标准结构
使用 `templates/project-scaffold-template.md` 模板生成项目基础设施代码，包括：
- 全局样式变量定义
- HTTP 客户端配置
- 工具函数库
- 全局类型定义

### Phase 3: 验证结构
使用 S9-01 至 S9-08 规则验证生成的结构是否合规

---

## 安全约束

1. **不改变业务逻辑**：仅调整目录结构和文件位置
2. **用户确认机制**：执行治理前必须生成计划，等待用户确认
3. **备份提醒**：执行目录调整前确认用户已备份
4. **引用更新**：移动文件后必须同步更新所有 import 路径

---

## 文件索引

### 示例文件
| 文件 | 说明 |
|------|------|
| [examples/project-structure-examples.md](examples/project-structure-examples.md) | 项目目录结构样例（整体分层、模块映射） |

### 模板文件
| 文件 | 说明 |
|------|------|
| [templates/project-scaffold-template.md](templates/project-scaffold-template.md) | 项目脚手架模板（全局样式、HTTP客户端、工具函数、类型定义） |

### 规则文件
| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | 项目结构检查规则（8 条规则的详细定义） |
