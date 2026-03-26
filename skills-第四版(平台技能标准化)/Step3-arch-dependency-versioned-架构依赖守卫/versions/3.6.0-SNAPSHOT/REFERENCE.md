# S1 架构依赖检查与修复 - 3.6.0-SNAPSHOT 版本规则

## 概述

本文件为 **3.6.0-SNAPSHOT** 版本的 S1 架构依赖检查与修复规则（基线版本）。

包含两大核心功能：

1. **依赖检查**：扫描 Java 微服务代码中严重的分层架构依赖违规问题（S1 级别），输出结构化检查报告。
2. **依赖修复**：修复检查发现的架构违规，消除非法依赖、补建 Service 中间层、修正注入方式等，不改变业务逻辑。

## 检查优先级说明

**S1 级别 = 严重架构违规**：违反分层架构的核心依赖规则，可能导致代码耦合严重、无法独立测试、循环依赖等问题，必须优先修复。

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查代码是否存在分层依赖违规 | "S1检查"、"架构依赖检查"、"分层依赖检查" | 功能一：依赖检查 |
| 修复已发现的分层依赖违规 | "S1修复"、"架构依赖修复"、"分层依赖修复" | 功能二：依赖修复 |
| 先检查再修复 | "S1检查并修复"、"架构依赖全流程" | 功能一 + 功能二 |

## 前置条件

- 工程为 Java 微服务项目（Spring Boot/Cloud）
- 采用 Controller → Service → DAO 分层架构
- 修复前用户需确认修复计划

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

### Step 1: 确定检查范围

用户提供目录路径或模块名称。

### Step 2: 扫描关键文件

重点扫描 Controller 层的 `@Autowired`/`@Resource` 注入和 import 语句：
- 使用 Glob 扫描 Controller 目录
- 使用 Read 读取每个 Controller 文件
- 使用 Grep 搜索 `@Autowired`、`@Resource`、`import` 模式

### Step 3: 逐项检查

按 S1 检查清单（5 项）逐项排查。

完整检查规则 → [scripts/check-rules.md](scripts/check-rules.md)

### Step 4: 输出检查报告

按标准格式输出结构化报告。

检查报告示例 → [examples/check-report.md](examples/check-report.md)

### 检查执行说明

1. 此功能被调用后，根据用户提供的路径扫描 Controller 层
2. 对每个 Controller 文件逐一分析依赖关系
3. 对每条规则明确给出 PASS/FAIL/WARN 判定
4. 违规项必须附带具体文件、行号和修复建议
5. 最终输出结构化的检查报告

---

## 功能二：依赖修复流程

### 核心原则

1. **只修依赖关系，不改业务逻辑**：修复过程中不修改任何业务实现代码的行为
2. **安全重构**：每步操作前先读取原文件，确认内容再进行修改
3. **逐步执行**：按优先级逐项修复，每完成一项向用户确认
4. **保持可编译**：重构后确保 import 路径、包声明、方法签名保持正确

### Phase 1: 扫描分析

扫描 Controller 层所有文件，识别依赖违规。

### Phase 2: 生成修复计划

列出所有需要修复的项，包含文件位置、违规类型和修复方案。

### Phase 3: 用户确认

将修复计划展示给用户，**必须获得确认后才开始执行修复操作**。

### Phase 4: 逐项执行修复

按 4 大修复规范逐项执行：

1. 消除 Controller→Controller 依赖（含决策树判定）
2. Controller→DAO/Mapper 补建 Service 中间层
3. Controller 注入 ServiceImpl → Service 接口（遵循接口设计规范）
4. Entity 泄露修复

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
接口设计规范 → [scripts/interface-design-rules.md](scripts/interface-design-rules.md)
代码违规模式与修复示例 → [examples/violation-patterns.md](examples/violation-patterns.md)
Service 接口/实现模板 → [templates/](templates/) 目录

### Phase 5: 验证结果

修复完成后按完整性校验清单验证：

完整校验流程 → [scripts/completeness-check.md](scripts/completeness-check.md)

关键验证步骤：
- **V-01** ServiceImpl 残留搜索：Grep 搜索 Controller 中所有 `ServiceImpl` 引用
- **V-02** DAO 残留搜索：Grep 搜索 Controller 中所有 `Dao`/`Mapper` 引用
- **V-03** 接口方法完整性：对比 Controller 调用的方法与 Service 接口声明
- **V-04** 编译验证：`mvn compile` 确保无编译错误
- **V-05** 全量打包验证：`mvn package -DskipTests` 确保全模块通过
- **V-06** AI 标记完整性：搜索所有 `@AI-Begin` 确认标记成对存在

### 安全约束

完整安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

关键约束：
- **不修改** HTTP 接口的 URL、HTTP 方法、入参、出参
- **不修改** Entity 类的包名和类名
- **不修改** 任何业务逻辑的执行结果
- **禁止迁移** Controller 的 private 方法到 Service 层（S-09）
- **S1-03 接口方法范围最小化**：仅提取 Controller 实际调用的方法（S-10）
- **S1-03 修复范围全覆盖**：所有匹配的 ServiceImpl 注入都必须修复，不允许选择性跳过
- **多 DAO 强制合并**：同一 Controller 的多个 DAO 必须合并到一个 DelegateService（D-04-A）
- **新建文件放 Service 层模块**：DelegateService 必须放在 `{module}-server-com`/`{module}-service` 中（D-08）
- **字段命名确定性**：替换后字段名按确定性规则命名（D-09）
- **违规编号确定性**：同一违规必须始终判定为相同 S1 编号（按类型特征判定，非场景判定）
- 务必在重构前获得用户确认

### 修复执行说明

1. 此功能被调用后，先扫描并生成修复计划
2. **必须等待用户确认后才开始执行修复操作**
3. 逐文件处理：每个违规 Controller 单独处理
4. 先读后改：使用 Read 读取文件 → 分析依赖 → 使用 Edit 修改
5. import 同步：修改依赖后立即更新 import 语句
6. 验证搜索：修复后使用 Grep 搜索旧的依赖引用，确保无遗漏

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
| [templates/service-interface.java](templates/service-interface.java) | Service 接口模板（补建 Service 中间层用） |
| [templates/service-impl.java](templates/service-impl.java) | ServiceImpl 实现类模板（补建 Service 中间层用） |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S1 检查规则清单（5 项详细检查方法与判定标准） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S1 修复规范（4 大修复策略 + AI 标记规范 + 接口设计引用） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则（S-01 ~ S-10） |
| [scripts/interface-design-rules.md](scripts/interface-design-rules.md) | 接口设计规范（直接代理原则，D-01 ~ D-07） |
| [scripts/completeness-check.md](scripts/completeness-check.md) | 完整性校验清单（V-01 ~ V-06，确保修复无遗漏） |
