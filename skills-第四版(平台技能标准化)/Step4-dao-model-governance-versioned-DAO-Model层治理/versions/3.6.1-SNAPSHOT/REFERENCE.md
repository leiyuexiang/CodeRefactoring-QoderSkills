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
| S2-01 | 目录命名规范（imp→impl） | FAIL | 非标准命名影响协作一致性，含合成词变体检测 |
| S2-02 | DAO 层接口/实现分离 | FAIL/WARN | 基于文件名确定性分类规则表判定 |
| S2-03 | DTO/VO/Query 分类归档 | FAIL/WARN | 按类型归入正确子目录 |
| S2-04 | 核心四层目录完整性 | WARN | controller/service/dao/model |
| S2-05 | resources/mapper 目录对应 | WARN | MyBatis XML 按模块分组 |
| S2-06 | DAO 层 mapper/entity 分离 | FAIL | Mapper 和 Entity 应分离到子目录，禁止冗余副本 |
| S2-07 | Model 层 dto/vo/query/po 分类 | FAIL | 按 6 级优先级匹配链归入子目录，含 BO→vo 归类，无后缀文件兜底归入 po/ |
| S2-08 | 公共模块结构 | WARN | config/util/exception 等标准子目录 |

完整检查规则详情 → [scripts/check-rules.md](scripts/check-rules.md)

---

## 功能一：DAO-Model 层检查流程

### Step 0: 识别独立业务域模块

在执行任何检查前，先识别并记录项目中的独立业务域模块（如 `view/`），将其排除在检查范围之外。

判定标准详见 → [scripts/check-rules.md](scripts/check-rules.md) 全局前置规则

### Step 1: 确定检查范围与冻结快照

用户提供目录路径或模块名称。同时：
- 一次性检查 `model/qo/` 目录是否存在，记录为 `HAS_QO_DIR` 布尔标志
- 一次性确定 import 搜索根目录（顶级父 POM 所在目录）
- 以上判定结果在整个流程中保持冻结，不再重新判定

### Step 2: 扫描目录结构

逐层扫描 DAO/Model 各层目录结构：
- 使用 Glob 扫描目录结构和文件名
- DAO 层分类仅依据文件名和目录位置（不读取文件内容检查注解）
- Model 分类仅依据文件名后缀的 endsWith 匹配（不读取文件内容）

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
2. **安全迁移**：先读取原文件 → 冲突预检 → 在新位置创建文件 → 更新引用 → 删除原文件
3. **逐步执行**：按优先级逐项修复，每完成一项向用户确认
4. **保持可编译**：迁移后确保所有 import 路径和 package 声明正确
5. **确定性原则**：Model 分类完全依据类名后缀的 endsWith 机械匹配（6 级优先级链），DAO 分类完全依据文件名和目录位置（8 级分类规则表），禁止通过阅读文件内容或分析类用途来决定分类
6. **职责边界**：Step4 仅负责 DAO-Model 层目录治理，禁止创建 Service 类、禁止修改 Controller 逻辑、禁止修改 POM 依赖
7. **独立业务域模块豁免**：满足豁免标准的模块内部文件不做任何迁移

### Phase 1: 扫描分析与冻结快照

扫描 DAO/Model 目录结构，识别不合规项。同时：
- 识别并记录独立业务域模块（豁免列表）
- 记录 `HAS_QO_DIR` 标志（model/qo/ 是否存在）
- 确定 import 搜索根目录（顶级父 POM 所在目录）
- 以上所有判定结果在后续 Phase 中保持冻结

### Phase 2: 生成修复计划

列出所有需要迁移的文件和目录变更。

### Phase 3: 用户确认

展示修复计划，**必须获得确认后才开始执行修复操作**。

### Phase 4: 逐项执行修复

按以下 **5 大修复步骤**和优先级顺序执行（每步完成后再进入下一步，不重复）：

1. **目录命名修正**（对应修复规范一）：`imp` → `impl`，包括合成词变体如 `serviceImp` → `impl`
2. **DAO 层归位**（对应修复规范二 + 修复规范五，合并执行）：按确定性分类规则表处理 `dao/` 根目录文件（实现类移入 `impl/`），Mapper 迁入 `dao/mapper/`，Entity 归入 `dao/entity/`，冗余 Mapper 副本清理
3. **Model 层文件分类**（对应修复规范三 + 修复规范六，合并为单一步骤）：按 6 级优先级匹配链将 `model/` 根目录散落文件归入 dto/vo/query/po 子目录，BO 归入 vo/，无后缀文件兜底归入 po/
4. **创建缺失标准子目录**（对应修复规范四）：仅创建目录，不移动文件
5. **全局验证无残留**：Grep 搜索所有旧包路径，确认无遗漏引用

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
标准目录结构模板 → [templates/standard-directory.md](templates/standard-directory.md)
文件迁移标准流程（8步） → [examples/migration-flow.md](examples/migration-flow.md)

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
- **迁移前必须冲突预检**（目标位置同名文件检测）
- **独立业务域模块内部不做迁移**

---

## 文件索引

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/check-report.md](examples/check-report.md) | DAO-Model 层检查报告输出示例 |
| [examples/migration-flow.md](examples/migration-flow.md) | 文件迁移标准流程（8步）与操作示例 |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/standard-directory.md](templates/standard-directory.md) | 标准目录结构模板（DAO层+Model层+公共模块），含确定性分类规则表 |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S2 检查规则清单（全局前置规则 + 8 项详细检查方法与判定标准） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S2 修复规范（全局前置规则 + 6 大修复策略与执行步骤 + import 搜索范围定义） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则（含冲突预检规则与独立业务域模块豁免） |
