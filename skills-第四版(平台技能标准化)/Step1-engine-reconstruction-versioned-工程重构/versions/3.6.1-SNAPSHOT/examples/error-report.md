# 编译错误报告输出示例 - 3.6.1-SNAPSHOT

> 与 3.6.0-SNAPSHOT 基线版本一致。

```
# 重构后编译错误报告

## 错误汇总
- 编译错误总数：{count}
- 可自动修复：{auto_fix}
- 需人工处理：{manual_fix}

## 错误详情
| 文件 | 行号 | 错误类型 | 错误信息 | 修复建议 |
|------|------|----------|----------|----------|
| ElementService.java | 15 | import | package grp.pt.xxx does not exist | 包路径已变更，需手动检查 |
```

> 错误自动修复策略详见 → [scripts/refactor-rules.md](../scripts/refactor-rules.md) 第四节
