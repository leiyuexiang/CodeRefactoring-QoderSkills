# 变更报告 Markdown 模板

报告文件保存路径：`element-module/grp-capability-element/element-module-optimizer-report.md`

```markdown
# Element Module Optimizer 变更报告

- 执行时间: {YYYY-MM-DD}
- 处理文件数: {processed_count}
- 跳过文件数: {skipped_count} (超过1000行)
- 总变更数: {total_changes}

## 处理统计

| 类别 | 数量 |
|------|------|
| SQL 注入修复 | {sql_fix_count} 处 |
| 日志增强 | {log_enhance_count} 个方法 |
| 冗余代码清理 | {code_clean_count} 处 |

## 跳过文件清单

| 文件路径 | 行数 | 跳过原因 |
|----------|------|----------|
| {file_path} | {line_count} | 超过1000行 |

## 变更详情

### 文件: {file_path} ({line_count}行)

**SQL 注入修复**:
- 方法 `{method_name}()`: {修复描述} (第{line}行)

**日志增强**:
- 类级别: {添加 @Slf4j 注解 / 已有日志声明}
- 方法 `{method_name}()`: {添加入口日志和异常日志}

**代码优化**:
- 第{line}行: {优化描述}

---
（每个文件一个章节，按处理顺序排列）
```

## 进度报告模板（每 5-10 个文件输出一次）

```
## 进度报告
- 已处理: {completed}/{total}
- 当前批次: {current_batch_name}
- SQL 注入修复累计: {sql_total} 处
- 日志增强累计: {log_total} 个方法
- 代码优化累计: {code_total} 处
```
