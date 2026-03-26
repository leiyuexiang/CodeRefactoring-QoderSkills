# 不可变红线与安全约束

## 不可变红线

以下内容**绝对不可修改**，任何一条违反都必须立即停止并报告：

| 编号 | 红线 | 说明 |
|------|------|------|
| R-01 | **类名不可变** | 包括 `@Service("xxx")` 和 `@Repository("xxx")` 中的别名 |
| R-02 | **方法签名不可变** | 方法名、参数列表、返回类型不得改变 |
| R-03 | **已有日志不可变** | 已有的日志语句即使格式不统一也保持原样 |
| R-04 | **注解不可变** | `@Override`、`@Transactional`、`@Autowired` 等注解不得改变 |
| R-05 | **业务逻辑不可变** | 业务逻辑的算法流程不得改变（只优化表达方式） |
| R-06 | **成员变量不可变** | 成员变量声明和注入方式不得改变 |

## 允许修改的范围

| 修改项 | 说明 |
|--------|------|
| SQL 字符串拼接方式 | 值拼接 → 参数化占位符 |
| 新增验证方法 | `validateTableName()` / `validateColumnName()` |
| 新增类注解 | `@Slf4j`（仅当无日志声明时） |
| 新增 import | `lombok.extern.slf4j.Slf4j`、`java.util.Set` 等 |
| 新增日志语句 | 方法入口/异常捕获处添加日志 |
| StringBuffer → StringBuilder | 局部变量场景 |
| 冗余变量消除 | 仅赋值一次且立即返回的变量 |
| 条件表达式简化 | 提前 return 减少嵌套 |
| 空集合检查统一 | `CollectionUtils.isEmpty/isNotEmpty` |
| 删除注释代码块 | 超过 5 行的废弃代码注释 |
| 删除 fixme 注释 | SQL 注入修复后删除 `//fixme 存在sql 注入问题` |

## 自检清单

每个文件修改完后必须自检：

- [ ] 类名未改变（含 @Service/@Repository 别名）
- [ ] 所有方法签名（名称、入参、返回类型）未改变
- [ ] 已有的日志语句未被删除或修改
- [ ] 所有 SQL 注入风险点已修复
- [ ] 新增的日志语句格式正确
- [ ] AI 标记格式正确（每个修改代码块前后都有标记）
- [ ] import 语句完整（新增的注解/工具类已导入）

## 技术约束

| 约束 | 说明 |
|------|------|
| BaseDAO 不可修改 | 来自依赖 jar `grp-database-com`，不在当前源码中 |
| SqlUtil 可使用 | `SqlUtil.inSqlCondition()` 来自依赖 jar，可继续使用 |
| Lombok 可使用 | 项目已声明 Lombok 依赖，`@Slf4j` 可直接使用 |
| CollectionUtils 来源 | 优先使用 `org.springframework.util.CollectionUtils` |
