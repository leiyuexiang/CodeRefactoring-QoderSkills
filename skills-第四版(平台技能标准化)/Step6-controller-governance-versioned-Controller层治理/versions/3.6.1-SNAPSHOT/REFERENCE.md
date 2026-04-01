# S4 Controller 接口分离检查与修复 - 3.6.1-SNAPSHOT 版本规则

## 版本变更说明

基于 `3.6.0-SNAPSHOT` 基线版本的增量修订版本。

### 相比 3.6.0-SNAPSHOT 的变更点

本版本与 3.6.0-SNAPSHOT 基线完全一致，无增量差异。所有规则、分类机制、目录结构、安全约束均继承基线版本。

---

## 概述

本文件为 **3.6.1-SNAPSHOT** 版本的 S4 Controller 接口分离检查与修复规则。

包含两大核心功能：

1. **Controller 接口分离检查**：扫描 Controller 层是否按规范正确划分为 `custom/` 和 `common/` 两级子目录，输出结构化检查报告。
2. **Controller 接口分离修复**：修复检查发现的分离问题，将 Controller 按确定性分类链重组到 `custom/`（业务接口）和 `common/`（框架级接口）下，不改变业务逻辑。

## 确定性保证

与 3.6.0-SNAPSHOT 基线一致：三级确定性分类链（Level 1 精确映射 → Level 2 关键词匹配 → Level 3 默认 custom），零依赖业务语义解读。

### 确定性实现机制（继承自 3.6.0-SNAPSHOT）

本版本继承 3.6.0-SNAPSHOT 的 **6 大确定性机制**：

| 机制 | 所在文件 | 作用 |
|------|---------|------|
| **GATE-REGEX 精确正则表** | [templates/classification-guide.md](templates/classification-guide.md) | 9 个精确正则模式，消除门控检查中的模糊判定 |
| **GATE-ALGORITHM 门控伪代码** | [templates/classification-guide.md](templates/classification-guide.md) | 可执行的门控检查算法，步骤严格有序 |
| **CLASSIFICATION-PIPELINE 分类伪代码** | [templates/classification-guide.md](templates/classification-guide.md) | 完整分类管线算法（含 classifyController + extractBusinessGroup + computeTargetPackage） |
| **SCAN-TOOL-PATTERNS 工具调用模式** | [scripts/refactor-rules.md](scripts/refactor-rules.md) | 8 个精确的 Grep/Glob 调用模式 |
| **SCAN-PER-FILE 单文件流程** | [scripts/refactor-rules.md](scripts/refactor-rules.md) | 每个文件的门控检查标准流程（SF-1~SF-5） |
| **IMPORT-TRACKING-MAP 引用映射表** | [scripts/refactor-rules.md](scripts/refactor-rules.md) | 系统化的交叉引用更新算法 |

## 重构策略

采用**集中重构**策略，与 3.6.0-SNAPSHOT 基线一致。禁止就地重构。

## 检查项总览

| 编号 | 检查项 | 严重级别 | 说明 |
|------|--------|---------|------|
| S4-01 | custom/common 一级目录存在性 | FAIL | controller/ 下必须有 custom/ 和 common/ |
| S4-02 | Controller 归属正确性 | FAIL | 文件位置必须与三级分类链计算结果一致 |
| S4-03 | 二级业务分组容量 | WARN | 单个子目录文件数 >10 时报告，不自动拆分 |
| S4-04 | 非 controller 包下的 Controller | FAIL | Controller 类不应在非 controller 包下 |
| S4-05 | controller 包下的非 Controller 类 | INFO | 非 Controller 类不迁移，仅提示 |

完整检查规则详情 → [scripts/check-rules.md](scripts/check-rules.md)

---

## 功能一：Controller 接口分离检查流程

与 3.6.0-SNAPSHOT 基线版本一致，详见 [scripts/check-rules.md](scripts/check-rules.md)。

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：Controller 接口分离修复流程

与 3.6.0-SNAPSHOT 基线版本一致，详见 [scripts/refactor-rules.md](scripts/refactor-rules.md)。

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
| [templates/standard-directory.md](templates/standard-directory.md) | custom/common 标准目录结构模板（集中重构模式） |
| [templates/classification-guide.md](templates/classification-guide.md) | 三级确定性分类指南（映射表 + 转换公式 + 可执行伪代码 + 测试用例） |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S4 检查规则清单（5 项详细检查方法与判定标准 + 精确工具调用模式） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S4 修复规范（统一迁移公式 + 9 步标准流程 + IMPORT-TRACKING-MAP + 批处理完整性规则） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则（18 条红线 + 6 条一致性约束） |
