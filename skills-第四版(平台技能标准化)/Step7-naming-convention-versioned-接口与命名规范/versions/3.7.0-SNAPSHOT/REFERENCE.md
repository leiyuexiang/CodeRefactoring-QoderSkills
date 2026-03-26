# S5 接口与命名规范检查与修复 - 3.6.0-SNAPSHOT 版本规则

## 版本说明

此为 **3.6.0-SNAPSHOT 基线版本**，包含完整的 S5 接口与命名规范检查与修复规则。

---

## 概述

本文件为 **3.6.0-SNAPSHOT** 版本的 S5 接口与命名规范检查与修复规则（基线版本）。

包含两大核心功能：

1. **接口与命名规范检查**：扫描接口路径结构、类命名、属性命名、接口参数/响应、Bean 命名冲突等规范问题，区分"可修复"和"约束限制"项，输出结构化检查报告。
2. **接口与命名规范修复**：修复检查发现的可修复项（类命名修正、HTTP 方法兼容、Bean 冲突处理），不改变业务逻辑。

**检查项来源**：提取自原 P2（代码组织）的接口路径与命名规范相关项（P2-04~P2-09）。

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查接口与命名是否符合规范 | "S5检查"、"命名规范检查"、"接口规范检查" | 功能一：接口与命名规范检查 |
| 修复不符合规范的命名问题 | "S5修复"、"命名规范修复"、"接口规范修复" | 功能二：接口与命名规范修复 |
| 先检查再修复 | "S5检查并修复"、"接口与命名规范全流程" | 功能一 + 功能二 |

## 前置条件

- 工程为 Java 微服务项目（Spring Boot/Cloud）
- 采用 Controller → Service → DAO → Model 分层架构
- S4（Controller 层治理）已完成
- 修复前用户需确认修复计划

## 检查项总览

| 编号 | 检查项 | 严重级别 | 说明 |
|------|--------|---------|------|
| S5-01 | 接口路径规范 | WARN | 四级路径结构、HTTP 方法规范 |
| S5-02 | 类命名规范 | WARN | 后缀、大驼峰、长度限制 |
| S5-03 | 属性命名规范 | WARN | 小驼峰、ID 后缀、布尔前缀 |
| S5-04 | 接口参数规范 | WARN | 命名统一、校验注解 |
| S5-05 | 接口响应规范 | WARN | ReturnData/ReturnPage 包装 |
| S5-06 | Bean 命名冲突 | FAIL/WARN | 名称冲突排查 |

完整检查规则详情 → [scripts/check-rules.md](scripts/check-rules.md)

---

## 功能一：接口与命名规范检查流程

### Step 1: 确定检查范围

用户提供目录路径或模块名称。

### Step 2: 扫描文件

逐层扫描各层代码命名和接口规范：
- 使用 Grep 搜索命名模式（`@RequestMapping`、`public class`、`private` 等）
- 使用 Read 读取关键文件确认类型

### Step 3: 逐项检查

按 S5 检查清单（6 项）逐项排查，区分"可修复"和"约束限制"项。

完整检查规则 → [scripts/check-rules.md](scripts/check-rules.md)

### Step 4: 输出检查报告

按标准格式输出结构化报告。

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：接口与命名规范修复流程

### 核心原则

1. **只做命名和规范调整，不改业务逻辑**：不修改方法实现内容
2. **约束优先**：标记为"约束限制"的项不修改（URL、HTTP 方法、序列化兼容）
3. **安全重构**：先确认、再修改，确保引用完整更新
4. **用户确认**：所有修改计划须获得用户确认后执行

### Phase 1: 扫描分析

扫描各层代码命名和接口规范，对照标准逐层对比。

### Phase 2: 分类问题

区分"可修复"和"约束限制"两类。

可修复 vs 约束限制分类表 → [scripts/constraint-classification.md](scripts/constraint-classification.md)

### Phase 3: 生成修复计划

仅对"可修复"项生成修复计划。

### Phase 4: 用户确认

展示修复计划，**必须获得确认后才开始执行修复操作**。

### Phase 5: 逐项执行修复

按 3 大修复规范和优先级执行：

1. 接口路径调整（DELETE/PUT 兼容增强）
2. 类命名修正（后缀 + 大驼峰）
3. Bean 命名冲突处理

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
命名规范速查表 → [templates/naming-convention.md](templates/naming-convention.md)
修正流程示例 → [examples/migration-flow.md](examples/migration-flow.md)

### Phase 6: 验证结果

修复完成后验证：
- 使用 Grep 搜索旧类名，确保无遗漏
- 检查 import 语句和注入点是否已同步更新
- 确认修复后代码可编译

### 安全约束

完整安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

关键约束：
- **不修改** HTTP 接口的 URL（除非用户明确要求）
- **不修改** HTTP 方法（除兼容性增强外）
- **不修改** 任何业务逻辑代码
- **不修改** DTO/VO 属性名（除非配合 @JsonProperty）
- 务必在重构前获得用户确认

---

## 文件索引

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/check-report.md](examples/check-report.md) | 接口与命名规范检查报告输出示例 |
| [examples/migration-flow.md](examples/migration-flow.md) | 命名修正标准流程与操作示例 |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/naming-convention.md](templates/naming-convention.md) | 类命名/属性命名/路径规范速查表 |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S5 检查规则清单（6 项详细检查方法与判定标准） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S5 修复规范（3 大修复策略与执行步骤） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则 |
| [scripts/constraint-classification.md](scripts/constraint-classification.md) | 可修复 vs 约束限制分类表 |
