# S1 依赖检查报告输出示例 - 3.6.1-SNAPSHOT

> 与 3.6.0-SNAPSHOT 基线版本完全一致。完整示例请参见 `versions/3.6.0-SNAPSHOT/examples/check-report.md`。

```
# S1 架构依赖违规检查报告

## 检查概览
- 检查路径：{path}
- 工程版本：3.6.1-SNAPSHOT
- 检查 Controller 数：{count}
- 严重违规（FAIL）：{fail_count}
- 警告（WARN）：{warn_count}
- 通过（PASS）：{pass_count}

## 详细结果

### S1-01 Controller→Controller 依赖
| Controller 类 | 被依赖 Controller | 调用方法 | 状态 |
|--------------|-------------------|---------|------|

### S1-02 Controller→DAO/Mapper 直接依赖
| Controller 类 | 被依赖 DAO/Mapper | 状态 |
|--------------|-------------------|------|

### S1-03 Controller→ServiceImpl 注入
| Controller 类 | 注入的 ServiceImpl | 应改为 | 状态 |
|--------------|-------------------|--------|------|

### S1-04 Entity 泄露
| Controller 类 | 方法名 | Entity 类 | 使用方式（参数/返回值） | 状态 |
|--------------|--------|----------|---------------------|------|

### S1-05 跨模块直接类引用
| Controller 类 | 引用的外部类 | 所属模块 | 状态 |
|--------------|-------------|---------|------|

## 修复建议
1. [FAIL] ...
2. [WARN] ...
```
