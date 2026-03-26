# S4 Controller 接口分离检查与修复 - 3.6.1-SNAPSHOT 版本规则

## 版本变更说明

基于 `3.6.0-SNAPSHOT` 基线版本的增量修订版本。

### 相比 3.6.0-SNAPSHOT 的变更点

> **TODO**: 请在此处补充 3.6.1-SNAPSHOT 版本相比 3.6.0 的具体差异。
> 以下为常见变更示例，根据实际情况修改：

1. **[待补充]** 新增/调整的检查规则
2. **[待补充]** 修复策略变更
3. **[待补充]** 安全约束调整
4. **[待补充]** 目录结构规范变更
5. **[待补充]** 分类判断标准变更

---

## 概述

本文件为 **3.6.1-SNAPSHOT** 版本的 S4 Controller 接口分离检查与修复规则。

包含两大核心功能：

1. **Controller 接口分离检查**：扫描 Controller 层是否按外部/内部接口分离原则正确划分为 `custom/` 和 `common/` 两级子目录，输出结构化检查报告。
2. **Controller 接口分离修复**：修复检查发现的分离问题，将 Controller 按 `custom/`（外部接口）和 `common/`（内部接口）重组，不改变业务逻辑。

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

与 3.6.0-SNAPSHOT 基线版本一致，详见 [scripts/check-rules.md](scripts/check-rules.md)。

> **TODO**: 如有差异请在此覆盖说明。

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：Controller 接口分离修复流程

与 3.6.0-SNAPSHOT 基线版本一致，详见 [scripts/refactor-rules.md](scripts/refactor-rules.md)。

> **TODO**: 如有差异请在此覆盖说明。

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
文件迁移标准流程 → [examples/migration-flow.md](examples/migration-flow.md)
安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)
标准目录结构模板 → [templates/standard-directory.md](templates/standard-directory.md)
分类判断指南 → [templates/classification-guide.md](templates/classification-guide.md)

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
| [templates/standard-directory.md](templates/standard-directory.md) | custom/common 标准目录结构模板 |
| [templates/classification-guide.md](templates/classification-guide.md) | 外部/内部接口分类判断指南 |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S4 检查规则清单 |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S4 修复规范 |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则 |
