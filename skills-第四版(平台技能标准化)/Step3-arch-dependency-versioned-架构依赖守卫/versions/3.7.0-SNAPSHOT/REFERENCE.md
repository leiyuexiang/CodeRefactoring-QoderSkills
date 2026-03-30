# S1 架构依赖检查与修复 - 3.7.0-SNAPSHOT 版本规则

## 版本变更说明

基于 `3.6.0-SNAPSHOT` 基线版本的新特性版本（主版本升级）。本版本的所有检查规则、修复规范、安全约束与 3.6.0-SNAPSHOT **完全一致**，无差异。

---

## ★★★ 强制基线文件加载指令（不可跳过）★★★

**3.7.0-SNAPSHOT 版本继承 3.6.0-SNAPSHOT 基线版本的全部规则。在执行任何检查或修复操作之前，必须先读取以下基线版本文件，获取完整的规则定义。本版本自身的简略文件仅作为索引，不包含完整规则内容。**

**必须读取的基线文件清单（按顺序逐一读取）**：

| 序号 | 文件路径（相对于技能根目录） | 内容说明 | 是否必读 |
|------|---------------------------|---------|---------|
| 1 | `versions/3.6.0-SNAPSHOT/scripts/check-rules.md` | 完整检查规则（含违规编号判定优先级、全量扫描指令） | **必读** |
| 2 | `versions/3.6.0-SNAPSHOT/scripts/refactor-rules.md` | 完整修复规范（含4大修复策略、决策树、FCC指令、修复顺序、import排序、方法调用替换） | **必读** |
| 3 | `versions/3.6.0-SNAPSHOT/scripts/safety-constraints.md` | 完整安全约束（S-01~S-17共17条红线） | **必读** |
| 4 | `versions/3.6.0-SNAPSHOT/scripts/interface-design-rules.md` | 完整接口设计规范（D-01~D-11共11条确定性规则） | **必读** |
| 5 | `versions/3.6.0-SNAPSHOT/scripts/completeness-check.md` | 完整校验清单（V-01~V-06） | **必读** |
| 6 | `versions/3.6.0-SNAPSHOT/templates/service-interface.java` | Service接口模板（含FCC指令和方法提取流程） | 修复S1-02时必读 |
| 7 | `versions/3.6.0-SNAPSHOT/templates/service-impl.java` | ServiceImpl模板（含纯转发规则） | 修复S1-02时必读 |
| 8 | `versions/3.6.0-SNAPSHOT/examples/violation-patterns.md` | 违规模式与修复前后对比示例 | 建议读取 |
| 9 | `versions/3.6.0-SNAPSHOT/examples/check-report.md` | 检查报告输出示例 | 建议读取 |

**执行流程**：
1. 先读取上述文件 1~5（强制必读），获取完整规则
2. 根据操作类型（检查/修复）读取文件 6~9
3. 按读取的完整规则执行操作
4. **严禁仅根据本版本目录下的简略文件执行操作**

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

完整检查规则详情 → **必须读取** `versions/3.6.0-SNAPSHOT/scripts/check-rules.md`

---

## 功能一：依赖检查流程

与 3.6.0-SNAPSHOT 基线版本**完全一致**。

**执行前必须先读取** `versions/3.6.0-SNAPSHOT/scripts/check-rules.md` 获取：
- 违规编号判定确定性规则（5级优先级链）
- S1-01/S1-02/S1-03 的强制全量 Grep 扫描指令
- 各项检查的详细判定标准

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：依赖修复流程

与 3.6.0-SNAPSHOT 基线版本**完全一致**。

**执行前必须先读取以下基线文件**：
- `versions/3.6.0-SNAPSHOT/scripts/refactor-rules.md` — 4大修复策略、决策树、FCC指令、修复顺序确定性规则、import排序规则、方法调用替换规则
- `versions/3.6.0-SNAPSHOT/scripts/interface-design-rules.md` — D-01~D-11接口设计确定性规范
- `versions/3.6.0-SNAPSHOT/scripts/safety-constraints.md` — S-01~S-17安全约束红线
- `versions/3.6.0-SNAPSHOT/scripts/completeness-check.md` — V-01~V-06完整性校验
- `versions/3.6.0-SNAPSHOT/templates/service-interface.java` — Service接口模板
- `versions/3.6.0-SNAPSHOT/templates/service-impl.java` — ServiceImpl模板

---

## 文件索引

### 本版本文件（仅作为索引，完整规则在基线版本中）

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | 规则摘要（完整版 → 基线 check-rules.md） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | 规则摘要（完整版 → 基线 refactor-rules.md） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 规则摘要（完整版 → 基线 safety-constraints.md） |
| [scripts/interface-design-rules.md](scripts/interface-design-rules.md) | 规则摘要（完整版 → 基线 interface-design-rules.md） |
| [scripts/completeness-check.md](scripts/completeness-check.md) | 规则摘要（完整版 → 基线 completeness-check.md） |

### 基线版本文件（必须读取）

| 文件 | 说明 |
|------|------|
| `versions/3.6.0-SNAPSHOT/scripts/check-rules.md` | **完整**检查规则清单 |
| `versions/3.6.0-SNAPSHOT/scripts/refactor-rules.md` | **完整**修复规范 |
| `versions/3.6.0-SNAPSHOT/scripts/safety-constraints.md` | **完整**安全约束（S-01~S-17） |
| `versions/3.6.0-SNAPSHOT/scripts/interface-design-rules.md` | **完整**接口设计规范（D-01~D-11） |
| `versions/3.6.0-SNAPSHOT/scripts/completeness-check.md` | **完整**校验清单（V-01~V-06） |
| `versions/3.6.0-SNAPSHOT/templates/service-interface.java` | Service接口模板 |
| `versions/3.6.0-SNAPSHOT/templates/service-impl.java` | ServiceImpl模板 |
| `versions/3.6.0-SNAPSHOT/examples/violation-patterns.md` | 违规模式与修复示例 |
| `versions/3.6.0-SNAPSHOT/examples/check-report.md` | 检查报告示例 |
