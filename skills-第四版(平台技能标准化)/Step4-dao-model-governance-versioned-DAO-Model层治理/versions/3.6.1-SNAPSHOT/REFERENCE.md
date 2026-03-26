# S2 DAO-Model 层治理检查与修复 - 3.6.0-SNAPSHOT 版本规则

## 版本说明

此为 **3.6.0-SNAPSHOT 基线版本**，包含完整的 S2 DAO-Model 层治理检查与修复规则。

---

## 概述

本文件为 **3.6.0-SNAPSHOT** 版本的 S2 DAO-Model 层治理检查与修复规则（基线版本）。

包含两大核心功能：

1. **DAO-Model 层检查**：扫描 Java 微服务代码中 DAO 层和 Model 层的目录结构与文件分类问题，输出结构化检查报告。
2. **DAO-Model 层修复**：修复检查发现的目录结构问题，修正命名、归位文件、分离 mapper/entity、补建缺失目录，不改变业务逻辑。

**检查项来源**：合并自原 P1（目录结构）的 DAO/Model 相关项 + 原 P2（代码组织）的 DAO/Model 相关项。

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查 DAO/Model 层是否符合规范 | "S2检查"、"DAO层治理检查"、"Model层检查" | 功能一：DAO-Model 层检查 |
| 修复不符合规范的 DAO/Model 层 | "S2修复"、"DAO层治理修复"、"Model层修复" | 功能二：DAO-Model 层修复 |
| 先检查再修复 | "S2检查并修复"、"DAO-Model层治理全流程" | 功能一 + 功能二 |

## 前置条件

- 工程为 Java 微服务项目（Spring Boot/Cloud）
- 采用 Controller → Service → DAO → Model 分层架构
- S1（架构依赖守卫）已完成
- 修复前用户需确认修复计划

## 检查项总览

| 编号 | 检查项 | 严重级别 | 说明 |
|------|--------|---------|------|
| S2-01 | 目录命名规范（imp→impl） | FAIL | 非标准命名影响协作一致性 |
| S2-02 | DAO 层接口/实现分离 | FAIL/WARN | 实现类应在 impl/ 下 |
| S2-03 | DTO/VO/Query 分类归档 | FAIL/WARN | 按类型归入正确子目录 |
| S2-04 | 核心四层目录完整性 | WARN | controller/service/dao/model |
| S2-05 | resources/mapper 目录对应 | WARN | MyBatis XML 按模块分组 |
| S2-06 | DAO 层 mapper/entity 分离 | FAIL/WARN | Mapper 和 Entity 应分离到子目录 |
| S2-07 | Model 层 dto/vo/query 分类 | FAIL/WARN | 按类型归入正确子目录 |
| S2-08 | 公共模块结构 | WARN | config/util/exception 等标准子目录 |

完整检查规则详情 → [scripts/check-rules.md](scripts/check-rules.md)

---

## 功能一：DAO-Model 层检查流程

### Step 1: 确定检查范围

用户提供目录路径或模块名称。

### Step 2: 扫描目录结构

逐层扫描 DAO/Model 各层目录结构：
- 使用 Glob 扫描目录结构
- 使用 Grep 搜索 `@Repository`、`@Mapper`、`class.*DTO`、`class.*VO` 等模式
- 使用 Read 读取关键文件确认类型

### Step 3: 逐项检查

按 S2 检查清单（8 项）逐项排查。

完整检查规则 → [scripts/check-rules.md](scripts/check-rules.md)

### Step 4: 输出检查报告

按标准格式输出结构化报告。

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：DAO-Model 层修复流程

### 核心原则

1. **只动目录和包路径，不改业务逻辑**：仅修改 package 声明和 import 语句
2. **安全迁移**：先读取原文件 → 在新位置创建文件 → 更新引用 → 删除原文件
3. **逐步执行**：按优先级逐项修复，每完成一项向用户确认
4. **保持可编译**：迁移后确保所有 import 路径和 package 声明正确

### Phase 1: 扫描分析

扫描 DAO/Model 目录结构，识别不合规项。

### Phase 2: 生成修复计划

列出所有需要迁移的文件和目录变更。

### Phase 3: 用户确认

展示修复计划，**必须获得确认后才开始执行修复操作**。

### Phase 4: 逐项执行修复

按 6 大修复规范和优先级执行：

1. 目录命名修正（imp→impl）
2. DAO 层归位（实现类移入 impl/）
3. DTO/VO/Query 归类
4. 创建缺失目录
5. DAO 层 mapper/entity 分离
6. Model 层 dto/vo/query 分类

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
标准目录结构模板 → [templates/standard-directory.md](templates/standard-directory.md)
文件迁移标准流程 → [examples/migration-flow.md](examples/migration-flow.md)

### Phase 5: 验证结果

修复完成后验证无残留引用：
- 使用 Grep 搜索旧包路径，确保无遗漏
- 检查 MyBatis XML namespace 是否已同步更新
- 检查 `@MapperScan` 扫描路径是否正确
- 确认修复后代码可编译

### 安全约束

完整安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

关键约束：
- **不修改** 任何类名、方法签名、接口定义
- **不修改** HTTP 接口的 URL、HTTP 方法
- **不修改** 任何业务逻辑代码
- 务必在重构前获得用户确认

---

## 文件索引

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/check-report.md](examples/check-report.md) | DAO-Model 层检查报告输出示例 |
| [examples/migration-flow.md](examples/migration-flow.md) | 文件迁移标准流程与操作示例 |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/standard-directory.md](templates/standard-directory.md) | 标准目录结构模板（DAO层+Model层+公共模块） |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S2 检查规则清单（8 项详细检查方法与判定标准） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S2 修复规范（6 大修复策略与执行步骤） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则 |
