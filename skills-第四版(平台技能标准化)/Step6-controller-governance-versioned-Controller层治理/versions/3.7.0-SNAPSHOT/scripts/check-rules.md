# S4 检查规则清单 - 3.7.0-SNAPSHOT

> **TODO**: 本文件继承自 3.6.0-SNAPSHOT 基线版本。请在此补充 3.7.0-SNAPSHOT 版本特有的检查规则差异。

## 与 3.6.x 系列的差异

> **TODO**: 请在此列出相比基线版本的规则变更：
> - 新增规则：无 / [待补充]
> - 调整规则：无 / [待补充]
> - 删除规则：无 / [待补充]

## 检查规则

本版本检查规则与 3.6.0-SNAPSHOT 基线版本一致（S4-01 至 S4-04），如有差异请在上方补充。

| 编号 | 检查项 | 严重级别 |
|------|--------|---------|
| S4-01 | custom/common 一级目录存在性 | FAIL |
| S4-02 | Controller 归属正确性 | FAIL/WARN |
| S4-03 | 二级业务分组合理性 | WARN |
| S4-04 | 非 controller 包下的 Controller | FAIL |

> 完整规则详情请参考 3.6.0-SNAPSHOT 版本的 [check-rules.md](../../3.6.0-SNAPSHOT/scripts/check-rules.md)，如本版本有差异则以本文件为准。
