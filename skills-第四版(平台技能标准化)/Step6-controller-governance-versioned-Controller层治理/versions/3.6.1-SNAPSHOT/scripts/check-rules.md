# S4 检查规则清单 - 3.6.1-SNAPSHOT

## 与 3.6.0-SNAPSHOT 的差异

本版本检查规则与 3.6.0-SNAPSHOT 基线完全一致，无差异。

## 检查规则

本版本检查规则继承 3.6.0-SNAPSHOT 基线（S4-01 至 S4-05），包含三级确定性分类链驱动的归属检查。

| 编号 | 检查项 | 严重级别 |
|------|--------|---------|
| S4-01 | custom/common 一级目录存在性 | FAIL |
| S4-02 | Controller 归属正确性（确定性分类链） | FAIL |
| S4-03 | 二级业务分组容量 | WARN |
| S4-04 | 非 controller 包下的 Controller | FAIL |
| S4-05 | controller 包下的非 Controller 类 | INFO |

> 完整规则详情请参考 3.6.0-SNAPSHOT 版本的 [check-rules.md](../../3.6.0-SNAPSHOT/scripts/check-rules.md)。
