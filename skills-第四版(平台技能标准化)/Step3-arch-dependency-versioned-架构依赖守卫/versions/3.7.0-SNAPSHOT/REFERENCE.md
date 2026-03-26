# S1 架构依赖检查与修复 - 3.7.0-SNAPSHOT 版本规则

## 版本变更说明

基于 `3.6.0-SNAPSHOT` 基线版本的新特性版本（主版本升级）。

### 相比 3.6.x 系列的变更点

> **TODO**: 请在此处补充 3.7.0-SNAPSHOT 版本相比 3.6.x 的具体差异。
> 以下为常见变更示例，根据实际情况修改：

1. **[待补充]** 新增检查规则（如 P0-06 等）
2. **[待补充]** 现有检查规则判定标准调整
3. **[待补充]** 修复策略变更
4. **[待补充]** Service 接口/实现命名规范变更
5. **[待补充]** 安全约束调整

---

## 概述

本文件为 **3.7.0-SNAPSHOT** 版本的 S1 架构依赖检查与修复规则。

包含两大核心功能：

1. **依赖检查**：扫描 Java 微服务代码中严重的分层架构依赖违规问题（S1 级别），输出结构化检查报告。
2. **依赖修复**：修复检查发现的架构违规，消除非法依赖、补建 Service 中间层、修正注入方式等，不改变业务逻辑。

## 检查项总览

| 编号 | 检查项 | 严重级别 | 说明 |
|------|--------|---------|------|
| S1-01 | Controller→Controller 直接依赖 | FAIL | 同层耦合、循环依赖风险 |
| S1-02 | Controller 直接依赖 DAO/Mapper | FAIL | 跳过 Service 层，逻辑散落 |
| S1-03 | Controller 注入 ServiceImpl 而非接口 | FAIL | 违反面向接口编程原则 |
| S1-04 | Entity 泄露到 Controller 层 | WARN | 暴露数据库结构，安全风险 |
| S1-05 | 跨模块直接类引用 | WARN | 模块间耦合，违反依赖原则 |

完整检查规则详情 → [scripts/check-rules.md](scripts/check-rules.md)

---

## 功能一：依赖检查流程

与 3.6.0-SNAPSHOT 基线版本结构一致，具体差异见 [scripts/check-rules.md](scripts/check-rules.md)。

> **TODO**: 如有差异请在此覆盖说明。

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：依赖修复流程

与 3.6.0-SNAPSHOT 基线版本结构一致，具体差异见 [scripts/refactor-rules.md](scripts/refactor-rules.md)。

> **TODO**: 如有差异请在此覆盖说明。

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
接口设计规范 → [scripts/interface-design-rules.md](scripts/interface-design-rules.md)
代码违规模式与修复示例 → [examples/violation-patterns.md](examples/violation-patterns.md)
安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)
完整性校验 → [scripts/completeness-check.md](scripts/completeness-check.md)
Service 接口/实现模板 → [templates/](templates/) 目录

---

## 文件索引

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/check-report.md](examples/check-report.md) | 依赖检查报告输出示例 |
| [examples/violation-patterns.md](examples/violation-patterns.md) | 代码违规模式与修复前后对比示例 |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/service-interface.java](templates/service-interface.java) | Service 接口模板 |
| [templates/service-impl.java](templates/service-impl.java) | ServiceImpl 实现类模板 |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S1 检查规则清单 |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S1 修复规范（含决策树 + AI 标记规范 + 接口设计引用） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则（S-01 ~ S-08） |
| [scripts/interface-design-rules.md](scripts/interface-design-rules.md) | 接口设计规范（直接代理原则，D-01 ~ D-07） |
| [scripts/completeness-check.md](scripts/completeness-check.md) | 完整性校验清单（V-01 ~ V-06） |
