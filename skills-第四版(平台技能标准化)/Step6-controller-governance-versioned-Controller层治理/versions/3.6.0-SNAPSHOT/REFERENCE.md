# S4 Controller 接口分离检查与修复 - 3.6.0-SNAPSHOT 版本规则

## 版本说明

此为 **3.6.0-SNAPSHOT 基线版本**，包含完整的 S4 Controller 接口分离检查与修复规则。

## 确定性保证

本规则集保证**确定性输出**：给定相同的输入工程状态，分类结果、目标路径和迁移操作在任意次数执行中完全一致。所有分类由显式映射表和模式匹配驱动，零依赖业务语义解读。

### 确定性实现机制

本规则集通过以下 **6 大确定性机制** 确保多次执行的一致率达到 95% 以上：

| 机制 | 所在文件 | 作用 |
|------|---------|------|
| **GATE-REGEX 精确正则表** | [templates/classification-guide.md](templates/classification-guide.md) | 9 个精确正则模式，消除门控检查中的模糊判定 |
| **GATE-ALGORITHM 门控伪代码** | [templates/classification-guide.md](templates/classification-guide.md) | 可执行的门控检查算法，步骤严格有序 |
| **CLASSIFICATION-PIPELINE 分类伪代码** | [templates/classification-guide.md](templates/classification-guide.md) | 完整分类管线算法（含 classifyController + extractBusinessGroup + computeTargetPackage 三个函数） |
| **SCAN-TOOL-PATTERNS 工具调用模式** | [scripts/refactor-rules.md](scripts/refactor-rules.md) | 8 个精确的 Grep/Glob 调用模式，消除扫描中的工具使用差异 |
| **SCAN-PER-FILE 单文件流程** | [scripts/refactor-rules.md](scripts/refactor-rules.md) | 每个文件的门控检查标准流程（SF-1~SF-5），确保逐文件验证的一致性 |
| **IMPORT-TRACKING-MAP 引用映射表** | [scripts/refactor-rules.md](scripts/refactor-rules.md) | 系统化的交叉引用更新算法，不依赖 AI 记忆 |

### 执行前自检要求

> **AI 每次执行 Step6 之前，必须阅读并确认以下组件已理解：**

1. GATE-REGEX 精确正则表（9 个正则模式）
2. GATE-ALGORITHM 门控伪代码（G1~G7 步骤）
3. CLASSIFICATION-PIPELINE 分类伪代码（3 个函数）
4. CLASSIFICATION-TEST-CASES 验证用例（12 个测试用例）
5. COMMON-MISTAKES 常见错误目录（10 个已知错误）
6. SCAN-TOOL-PATTERNS 工具调用模式（8 个模式）
7. SCAN-PER-FILE 单文件流程（SF-1~SF-5）
8. IMPORT-TRACKING-MAP 引用映射表算法
9. BATCH-INTEGRITY 批处理完整性规则（6 条）
10. CONSISTENCY-CHECKLIST 一致性检查清单（6 项执行前自检 + 8 项执行中校验点）

## 编码防护规范（强制前置）

在执行任何检查或修复操作之前，必须读取并遵守全局编码防护规范：
→ [shared/encoding-guard.md](../../../shared/encoding-guard.md)

该规范定义了 Windows 环境下防止中文编码被 PowerShell 破坏的事前防护措施。核心要求：
- 文件搜索使用 Grep/Glob 工具，禁止 Bash `grep`/`find`
- 文件读取使用 Read 工具，禁止 Bash `cat`/`type`/`Get-Content`
- 文件修改使用 Edit 工具，禁止 Bash `sed`/`awk`/PowerShell 替换
- 仅 A 类操作（copy/mv/mkdir/rmdir）允许通过 Bash 执行

---

## 概述

本文件为 **3.6.0-SNAPSHOT** 版本的 S4 Controller 接口分离检查与修复规则。

包含两大核心功能：

1. **Controller 接口分离检查**：扫描 Controller 层是否按规范正确划分为 `custom/` 和 `common/` 两级子目录，输出结构化检查报告。
2. **Controller 接口分离修复**：修复检查发现的分离问题，将 Controller 按确定性分类链重组到 `custom/`（业务接口）和 `common/`（框架级接口）下，不改变业务逻辑。

## 重构策略

采用**集中重构**策略：将所有 Controller 从原有的分散业务包（如 `config.{业务}.controller`）中抽取到统一的 `{modulePrefix}.controller/{custom|common}/{业务}/` 包下。

**禁止**采用就地重构策略（即禁止在原业务包内部添加 controller/custom 子目录）。

## 分类机制

Controller 的 custom/common 分类由**三级确定性分类链**唯一确定，不包含任何主观判断：

| 级别 | 机制 | 说明 |
|------|------|------|
| Level 1 | 精确类名映射表 | 最高优先级，用于特殊情况 |
| Level 2 | 关键词模式匹配 | 中优先级，识别 common 类型（Sso/ServerInfo/I*Controller等） |
| Level 3 | 默认归入 custom | 最低优先级，自动提取业务分组名 |

完整分类规则 → [templates/classification-guide.md](templates/classification-guide.md)

## 标准目录结构

```
{modulePrefix}/controller/
├── custom/                    # 业务接口（Level 3 默认归属）
│   └── {businessGroup}/      # 从原包路径自动提取，全小写
└── common/                   # 框架级功能接口（仅 Level 1/2 命中）
    ├── api/                  # I*接口定义
    ├── sso/                  # 单点登录相关
    └── monitor/              # 监控/服务器信息
```

完整目录规范 → [templates/standard-directory.md](templates/standard-directory.md)

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

### Step 1: 确定检查范围

用户提供目录路径或模块名称。

### Step 2: 扫描文件

1. 采用**四轮逐区扫描策略**（config/ → config2/ → controller/+其他 → 全量交叉验证），详见 [scripts/refactor-rules.md 步骤 1.1.1~1.1.4](scripts/refactor-rules.md)
2. 使用 GATE-REGEX 精确正则模式和 SCAN-PER-FILE 标准流程执行门控检查
3. **强制验证：导入 [scripts/refactor-rules.md 步骤 1.1.4](scripts/refactor-rules.md) 中的汇总与交叉验证，确认 config/config2 每个子目录均已扫描，分区总数与全量验证一致**
4. 对每个 Controller 运行 CLASSIFICATION-PIPELINE 伪代码中的 classifyController 函数，计算目标位置

### Step 3: 逐项检查

按 S4 检查清单（5 项）逐项排查。

完整检查规则 → [scripts/check-rules.md](scripts/check-rules.md)

### Step 4: 输出检查报告

按标准格式输出结构化报告，**必须包含分类级别和命中规则**：

```
| Controller 类 | 当前位置 | 分类级别 | 命中规则 | 目标位置 | 状态 |
```

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：Controller 接口分离修复流程

### Phase 1: 扫描分析

扫描所有 Controller 文件，运行三级分类链，计算目标路径，生成迁移清单。**Phase 1 必须输出分区扫描结果表格**（详见 [scripts/refactor-rules.md 步骤 1.1.4](scripts/refactor-rules.md)），该表格是进入 Phase 2 的门控条件。

### Phase 2: 生成修复计划

格式化为结构化表格，包含分类级别和命中规则：

```
| 序号 | Controller 类 | 当前 package | 目标 package | 分类级别 | 命中规则 | 操作 |
```

### Phase 3: 用户确认

展示修复计划，**必须获得确认后才开始执行修复操作**。

用户确认方式：展示完整修复计划表格 + 统计信息，等待用户明确确认。

### Phase 4: 逐项执行迁移

对每个需迁移的文件执行标准 9 步迁移流程。**Phase 4 执行期间，每完成 10 个文件必须输出进度摘要**（详见 [scripts/refactor-rules.md P4-PROGRESS 规则](scripts/refactor-rules.md)）。**Phase 4 到 Phase 5 的过渡需通过完成校验门控**：实际迁移数必须等于计划迁移数。

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
文件迁移标准流程 → [examples/migration-flow.md](examples/migration-flow.md)

### Phase 5: 验证与清理

采用**三阶段验证**确保迁移完整性：
1. **外部引用验证**：Grep 确认无残留旧路径引用
2. **内部交叉引用验证**：检查已迁移 Controller 内部 import 是否仍引用旧路径（S-12 约束）
3. **全局 import 一致性验证**：全量 Grep 确认所有旧 package 引用已清除
4. 清理空目录（遵守 S-09 约束）
5. 输出验证报告

### 安全约束

完整安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

关键约束：
- **不修改** HTTP 接口的 URL 路径
- **不修改** HTTP 方法
- **不修改** 任何业务逻辑代码
- **不迁移** 非 Controller 类（无注解的文件保留原位）
- **不跨模块迁移** Controller
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
| [templates/standard-directory.md](templates/standard-directory.md) | custom/common 标准目录结构模板（集中重构模式） |
| [templates/classification-guide.md](templates/classification-guide.md) | 三级确定性分类指南（映射表 + 转换公式 + 可执行伪代码 + 测试用例） |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S4 检查规则清单（5 项详细检查方法与判定标准 + 精确工具调用模式） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S4 修复规范（统一迁移公式 + 9 步标准流程 + IMPORT-TRACKING-MAP + 批处理完整性规则） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则（18 条红线 + 6 条一致性约束） |
