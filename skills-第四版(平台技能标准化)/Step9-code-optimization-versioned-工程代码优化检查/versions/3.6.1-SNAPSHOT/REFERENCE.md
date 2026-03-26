# 工程代码优化检查与修复 - 3.6.1-SNAPSHOT 版本规则

## 版本变更说明

基于 `3.6.0-SNAPSHOT` 基线版本的增量修订版本。

### 相比 3.6.0-SNAPSHOT 的变更点

> **TODO**: 请在此处补充 3.6.1-SNAPSHOT 版本相比 3.6.0 的具体差异。
> 以下为常见变更示例，根据实际情况修改：

1. **[待补充]** SQL 注入修复规则变更
2. **[待补充]** 日志增强规则变更
3. **[待补充]** 代码优化规则变更
4. **[待补充]** 安全约束调整
5. **[待补充]** 目标目录结构变更
6. **[待补充]** 跳过文件清单变更

---

## 概述

本文件为 **3.6.1-SNAPSHOT** 版本的工程代码优化检查与修复规则。

包含三大核心功能：
1. **SQL 注入修复**（优先级最高）
2. **日志增强**
3. **代码清理**

与 3.6.0-SNAPSHOT 基线版本一致，如有差异请在各规则文件中覆盖说明。

完整工作流程 → 参见 3.6.0-SNAPSHOT 基线版本 RULES.md

---

## 文件索引

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/change-report.md](examples/change-report.md) | 变更报告输出示例 |
| [examples/workflow-demo.md](examples/workflow-demo.md) | 单文件优化工作流演示 |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/report-template.md](templates/report-template.md) | 变更报告 Markdown 模板 |
| [templates/skip-files.md](templates/skip-files.md) | 已知超大文件清单 |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/sql-injection-rules.md](scripts/sql-injection-rules.md) | SQL 注入修复指南 |
| [scripts/logging-rules.md](scripts/logging-rules.md) | 日志增强规则 |
| [scripts/code-optimization-rules.md](scripts/code-optimization-rules.md) | 代码优化规则 |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 不可变红线与安全约束 |
