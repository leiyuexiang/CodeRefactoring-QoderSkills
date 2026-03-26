# S4 Controller 接口分离检查与修复 - 3.6.0-SNAPSHOT 版本规则

## 版本说明

此为 **3.6.0-SNAPSHOT 基线版本**，包含完整的 S4 Controller 接口分离检查与修复规则。

---

## 概述

本文件为 **3.6.0-SNAPSHOT** 版本的 S4 Controller 接口分离检查与修复规则。

包含两大核心功能：

1. **Controller 接口分离检查**：扫描 Controller 层是否按外部/内部接口分离原则正确划分为 `custom/` 和 `common/` 两级子目录，输出结构化检查报告。
2. **Controller 接口分离修复**：修复检查发现的分离问题，将 Controller 按 `custom/`（外部接口）和 `common/`（内部接口）重组，不改变业务逻辑。

## 检查优先级说明

**S4 级别 = Controller 接口分离**：Controller 层按外部/内部接口类型进行一级分组，然后在其中按业务功能进一步分组。不影响功能，属于"结构优化"。

## 标准目录结构

```
controller/
├── custom/               # 自定义接口（外部接口，面向前端/第三方）
│   ├── basedata/         # 基础数据管理（要素、值集、目录等核心业务）
│   ├── bookset/          # 账套管理
│   ├── agencyManager/    # 单位管理
│   └── {business}/       # 其他外部业务分组
└── common/               # 通用接口（内部接口，面向内部微服务）
    ├── api/              # 内部 API 接口
    ├── util/             # 工具/调试类
    ├── notify/           # 通知
    ├── sync/             # 数据同步
    └── {function}/       # 其他内部功能分组
```

## 分类原则

| 分类依据 | 归属目录 | 说明 |
|---------|---------|------|
| 接口路径一级路径为 `run/` | `controller/custom/` | 外部接口，面向前端/第三方调用 |
| 接口路径一级路径为 `config/` | `controller/common/` | 内部接口，面向内部微服务调用 |
| 面向前端 UI 操作的业务接口 | `controller/custom/` | 如要素管理、账套管理、单位管理 |
| 内部 API / 工具 / 调试 / 同步 | `controller/common/` | 如 `/api/v1`、缓存工具、通知 |

> **注意**：当接口路径无 `run/` 或 `config/` 前缀时，根据业务职责判断归属。

## 检查项总览

| 编号 | 检查项 | 严重级别 | 说明 |
|------|--------|---------|------|
| S4-01 | custom/common 一级目录存在性 | FAIL | controller/ 下必须有 custom/ 和 common/ |
| S4-02 | Controller 归属正确性 | FAIL/WARN | 文件不应在 controller/ 根目录或非标准子目录 |
| S4-03 | 二级业务分组合理性 | WARN | 文件超过 10 个应进一步分组 |
| S4-04 | 非 controller 包下的 Controller | FAIL | Controller 类不应在非 controller 包下 |

完整检查规则详情 → [scripts/check-rules.md](scripts/check-rules.md)

---

## 功能一：Controller 接口分离检查流程

### Step 1: 确定检查范围

用户提供目录路径或模块名称。

### Step 2: 扫描文件

扫描 Controller 层目录结构：
- 使用 Glob 扫描 `controller/` 目录下所有子目录和 Java 文件
- 使用 Grep 搜索 `@RequestMapping` 路径前缀
- 使用 Read 读取关键 Controller 文件判断接口类型

### Step 3: 逐项检查

按 S4 检查清单（4 项）逐项排查。

完整检查规则 → [scripts/check-rules.md](scripts/check-rules.md)

### Step 4: 输出检查报告

按标准格式输出结构化报告。

检查报告示例 → [examples/check-report.md](examples/check-report.md)

### 检查执行说明

1. 此功能被调用后，根据用户提供的路径扫描 Controller 层
2. 对照标准目录结构逐层检查
3. 对每条规则明确给出 PASS/FAIL/WARN 判定
4. 最终输出结构化的检查报告

---

## 功能二：Controller 接口分离修复流程

### 核心原则

1. **只做结构调整，不改业务逻辑**：不修改方法实现内容
2. **约束优先**：不修改 URL 路径、HTTP 方法、序列化兼容性
3. **安全重构**：先读取、再修改，确保引用完整更新
4. **用户确认**：所有修改计划须获得用户确认后执行

### Phase 1: 扫描分析

1. 使用 Glob 扫描 `controller/` 目录下所有 Java 文件
2. 使用 Grep 搜索每个 Controller 的 `@RequestMapping` 路径前缀
3. 根据路径前缀（`run/` vs `config/`）和业务职责判断 custom/common 归属
4. 生成分类清单

### Phase 2: 生成修复计划

仅对需要迁移的 Controller 生成修复计划，包括目标位置和二级业务分组。

### Phase 3: 用户确认

展示修复计划，**必须获得确认后才开始执行修复操作**。

### Phase 4: 逐项执行修复

按修复规范执行迁移操作。

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
文件迁移标准流程 → [examples/migration-flow.md](examples/migration-flow.md)

### Phase 5: 验证结果

修复完成后验证无残留引用：
- Glob 扫描确认 `controller/` 根目录下无残留 Controller 文件
- 确认所有文件都在 `custom/` 或 `common/` 下
- Grep 搜索旧 package 路径确认无残留引用

### 安全约束

完整安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

关键约束：
- **不修改** HTTP 接口的 URL 路径
- **不修改** HTTP 方法
- **不修改** 任何业务逻辑代码
- **不修改** 非 controller 包下的代码结构
- 务必在重构前获得用户确认

---

## 文件索引

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/check-report.md](examples/check-report.md) | Controller 接口分离检查报告输出示例 |
| [examples/migration-flow.md](examples/migration-flow.md) | Controller 文件迁移标准流程与操作示例 |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/standard-directory.md](templates/standard-directory.md) | custom/common 标准目录结构模板与分组示例 |
| [templates/classification-guide.md](templates/classification-guide.md) | 外部/内部接口分类判断指南 |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S4 检查规则清单（4 项详细检查方法与判定标准） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S4 修复规范（迁移策略与执行步骤） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则 |
