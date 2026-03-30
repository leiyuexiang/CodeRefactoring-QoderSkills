# S5 接口与命名规范检查与修复 - 3.6.1-SNAPSHOT 版本规则

## 版本说明

此为 **3.6.1-SNAPSHOT 增量修订版本**，包含完整的 S5 接口与命名规范检查与修复规则。

---

## 概述

本文件为 **3.6.1-SNAPSHOT** 版本的 S5 接口与命名规范检查与修复规则（增量修订版本）。

包含两大核心功能：

1. **接口与命名规范检查**：扫描接口路径结构、类命名、属性命名、接口参数/响应、Bean 命名冲突等规范问题，区分"可修复"和"约束限制"项，输出结构化检查报告。
2. **接口与命名规范修复**：修复检查发现的可修复项（类命名修正、HTTP 方法兼容、Bean 冲突处理），不改变业务逻辑。

**检查项来源**：提取自原 P2（代码组织）的接口路径与命名规范相关项（P2-04~P2-09）。

**一致性目标**：本技能针对相同代码的多次执行，检查结果和修复输出一致率 ≥ 95%。

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

用户提供目录路径或模块名称。如用户未指定，默认扫描当前工程的所有 Java 模块。

**扫描目录规则**（必须严格遵守）：

| 扫描范围 | Glob 模式 |
|---------|-----------|
| Controller 层 | `**/controller/**/*.java`、`**/custom/**/*.java`、`**/common/**/*.java` |
| Service 层 | `**/service/**/*.java`、`**/facade/**/*.java` |
| DAO 层 | `**/dao/**/*.java`、`**/mapper/**/*.java` |
| Model 层 | `**/model/**/*.java`、`**/entity/**/*.java`、`**/dto/**/*.java`、`**/vo/**/*.java`、`**/query/**/*.java` |

**排除目录**：`**/test/**`、`**/tests/**`、`**/target/**`、`**/build/**`、`**/generated/**`

### Step 2: 扫描文件

逐层扫描各层代码命名和接口规范。对每个 `.java` 文件：

1. 使用 Grep 搜索命名模式，按 check-rules.md 中指定的**精确正则**
2. 使用 Read 读取关键文件确认类型
3. 根据文件路径确定所属层级

### Step 3: 逐项检查（严格按编号顺序）

按 S5-01 → S5-02 → S5-03 → S5-04 → S5-05 → S5-06 顺序逐项排查：

- 对每个检查项使用 check-rules.md 中定义的**精确正则和判定标准**
- 遇到边界场景时使用 check-rules.md 中定义的**边界场景处理规则**
- 需要决策时使用 [scripts/deterministic-rules.md](scripts/deterministic-rules.md) 中的**确定性决策树**
- 区分"可修复"和"约束限制"项

完整检查规则 → [scripts/check-rules.md](scripts/check-rules.md)
确定性决策树 → [scripts/deterministic-rules.md](scripts/deterministic-rules.md)

### Step 4: 输出检查报告（严格模板）

按以下**强制模板**输出结构化报告。报告格式**不得偏离此模板**：

```
# S5 接口与命名规范检查报告

## 检查概览
- 检查路径：{path}
- 扫描文件数：{total_files}
- 检查通过项：{pass_count}
- 可修复项（FAIL/WARN）：{fixable_count}
- 约束限制项：{constrained_count}

## 检查详情

### S5-01 接口路径规范（{pass/fail_count}）
| # | Controller | 路径 | 问题 | 状态 | 分类 |
|---|-----------|------|------|------|------|
| 1 | {类名} | {路径} | {问题描述} | {PASS/WARN/FAIL} | {可修复/约束限制} |

### S5-02 类命名规范（{pass/fail_count}）
| # | 文件路径 | 类名 | 问题 | 建议 | 状态 | 分类 |
|---|---------|------|------|------|------|------|
| 1 | {文件路径} | {类名} | {问题描述} | {建议名称} | {PASS/WARN} | {可修复/约束限制} |

### S5-03 属性命名规范（{pass/fail_count}）
| # | 文件路径 | 类名 | 属性 | 问题 | 状态 | 分类 |
|---|---------|------|------|------|------|------|
| 1 | {文件路径} | {类名} | {属性名} | {问题描述} | {PASS/WARN} | {可修复/约束限制} |

### S5-04 接口参数规范（{pass/fail_count}）
| # | Controller 方法 | 参数 | 问题 | 状态 | 分类 |
|---|---------------|------|------|------|------|
| 1 | {类名.方法名} | {参数名} | {问题描述} | {PASS/WARN} | {可修复/约束限制} |

### S5-05 接口响应规范（{pass/fail_count}）
| # | Controller 方法 | 返回类型 | 问题 | 状态 | 分类 |
|---|---------------|---------|------|------|------|
| 1 | {类名.方法名} | {返回类型} | {问题描述} | {PASS/WARN} | {可修复/约束限制} |

### S5-06 Bean 命名冲突（{pass/fail_count}）
| # | Bean 名称 | 冲突类（模块） | 状态 | 分类 |
|---|-----------|-------------|------|------|
| 1 | {Bean名} | {类1(模块1), 类2(模块2)} | {PASS/WARN/FAIL} | {可修复/约束限制} |

## 约束限制项汇总（不建议修改）
| # | 检查项 | 问题类型 | 具体问题 | 约束原因 |
|---|--------|---------|---------|---------|
| 1 | {S5-XX} | {问题类型} | {具体描述} | {约束原因} |

## 修复建议汇总
| # | 优先级 | 修复类型 | 具体建议 | 影响范围 |
|---|--------|---------|---------|---------|
| 1 | {高/中/低} | {修复类型} | {具体描述} | {影响文件数} |
```

**报告排序规则**（确定性）：
1. 各检查项内的记录按**文件路径字母序**排列
2. 同一文件内的记录按**行号升序**排列
3. 约束限制项按检查编号排列
4. 修复建议按优先级（高→中→低）排列

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：接口与命名规范修复流程

### 核心原则

1. **只做命名和规范调整，不改业务逻辑**：不修改方法实现内容
2. **约束优先**：标记为"约束限制"的项不修改（URL、HTTP 方法、序列化兼容）
3. **安全重构**：先确认、再修改，确保引用完整更新
4. **用户确认**：所有修改计划须获得用户确认后执行
5. **幂等性**：已合规项自动跳过，重复执行不产生额外变更
6. **确定性**：使用确定性决策树处理所有模糊场景

### Phase 1: 扫描分析

扫描各层代码命名和接口规范，对照标准逐层对比。
使用 check-rules.md 中的精确正则进行匹配。

### Phase 2: 分类问题

区分"可修复"和"约束限制"两类。**分类判定必须使用确定性决策树**。

可修复 vs 约束限制分类表 → [scripts/constraint-classification.md](scripts/constraint-classification.md)
确定性决策树 → [scripts/deterministic-rules.md](scripts/deterministic-rules.md)

### Phase 3: 生成修复计划

仅对"可修复"项生成修复计划。

**修复计划必须使用以下格式**：
```
## 修复计划

### 修复项 {N}：{修复类型}
- 文件：{完整文件路径}
- 当前：{当前代码片段}
- 目标：{修改后代码片段}
- 影响范围：{受影响的引用文件列表}
- 风险等级：{低/中/高}
```

### Phase 4: 用户确认

展示修复计划，**必须获得确认后才开始执行修复操作**。

### Phase 5: 逐项执行修复（严格顺序）

**修复顺序必须严格按以下优先级执行**：

```
优先级 1：修复规范一 — 接口路径调整（DELETE/PUT 兼容增强）
  执行顺序：先 @DeleteMapping → 再 @PutMapping
  文件顺序：按模块名字母序 → 文件路径字母序

优先级 2：修复规范二 — 类命名修正
  执行顺序：先后缀修正 → 再大驼峰修正
  文件顺序：按模块名字母序 → 文件路径字母序
  每改一个类立即更新所有引用

优先级 3：修复规范三 — Bean 命名冲突处理
  执行顺序：先 FAIL 级 → 再 WARN 级
```

**每个修复项的操作流程**：
```
1. 幂等性检查：目标状态是否已达成？ → 是则跳过
2. 冲突预检：新名称是否已存在？ → 是则报告等待用户决策
3. Read 读取目标文件
4. Edit 执行修改
5. Grep 验证旧内容已清除
6. Grep 搜索所有引用方
7. 逐一更新引用方
8. 再次 Grep 验证无残留
```

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
命名规范速查表 → [templates/naming-convention.md](templates/naming-convention.md)
修正流程示例 → [examples/migration-flow.md](examples/migration-flow.md)

### Phase 6: 验证结果

修复完成后验证：
- 使用 Grep 搜索旧类名，确保无遗漏（必须返回 0 结果）
- 检查 import 语句和注入点是否已同步更新
- 确认修复后代码可编译
- 如果是重复执行，确认无新增变更

### 安全约束

完整安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

关键约束：
- **不修改** HTTP 接口的 URL（除非用户明确要求）
- **不修改** HTTP 方法（除兼容性增强外）
- **不修改** 任何业务逻辑代码
- **不修改** DTO/VO 属性名（除非配合 @JsonProperty）
- **不引入** 新的 Bean 命名冲突
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
| [templates/naming-convention.md](templates/naming-convention.md) | 类命名/属性命名/路径规范速查表（含转换规则和边界场景） |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S5 检查规则清单（6 项详细检查方法、精确正则、边界场景处理） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S5 修复规范（3 大修复策略、注解转换规则、import 管理、注入点处理） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束、核心原则、幂等性保障规则 |
| [scripts/constraint-classification.md](scripts/constraint-classification.md) | 可修复 vs 约束限制分类表 |
| [scripts/deterministic-rules.md](scripts/deterministic-rules.md) | 确定性决策树、幂等性保障机制、一致性规则 |
