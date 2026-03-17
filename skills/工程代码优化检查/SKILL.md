---
name: element-module-optimizer
description: >-
  优化 grp-capability-element 工程下 element-server-com 模块中 @Service 和 @Repository 类的代码质量。
  包括修复 SQL 注入漏洞（值拼接参数化、动态表名/列名白名单校验）、增强日志记录（Lombok @Slf4j）、
  清理冗余代码（StringBuffer→StringBuilder、冗余变量、嵌套条件简化）。
  当用户要求优化要素模块代码、修复 SQL 注入、增强日志、清理代码、代码审查优化时使用此技能。
  适用于使用 BaseDAO/JdbcTemplate 持久层的 Spring Boot Java 项目。
---

# Element Module Optimizer

优化 `element-module/grp-capability-element/element-server-com` 下 @Service 和 @Repository 类的代码质量。

## 范围定义

### 目标目录

```
element-server-com/src/main/java/grp/pt/
├── dao/
│   ├── impl/                    # 通用 DAO
│   ├── basedata/impl/           # 基础数据 DAO (~33个)
│   ├── bookset/impl/            # 账簿集 DAO
│   ├── agencyManager/impl/      # 机构管理 DAO
│   └── synchronization/         # 同步 DAO
├── service/
│   ├── impl/                    # 通用 Service
│   ├── basedata/impl/           # 基础数据 Service (~37个)
│   ├── bookset/impl/            # 账簿集 Service
│   ├── agencyManager/impl/      # 机构管理 Service
│   ├── synchronization/impl/    # 同步 Service
│   └── cache/                   # 缓存 Service
└── util/                        # 工具类（仅涉及 BaseH2DAO 等含 SQL 的）
```

### 筛选规则

- **处理**: 带 `@Service` 或 `@Repository` 注解的 Java 类
- **跳过**: 文件总行数 > 1000 行的类（记录到报告的跳过清单中）
- **排除**: 接口文件、Mapper 接口、Controller、配置类、DTO/Model

### 不可变红线

以下内容**绝对不可修改**：
1. 类名（包括 `@Service("xxx")` 和 `@Repository("xxx")` 中的别名）
2. 方法签名（方法名、参数列表、返回类型）
3. 已有的日志语句（即使格式不统一也保持原样）
4. `@Override`、`@Transactional`、`@Autowired` 等注解
5. 业务逻辑的算法流程（只优化表达方式，不改变业务语义）
6. 成员变量声明和注入方式

## 工作流

执行优化前，复制以下清单跟踪进度：

```
任务进度:
- [ ] Step 1: 扫描文件，统计行数，输出待处理/跳过清单
- [ ] Step 2: 逐文件分析（Service/DAO、日志状态、SQL 风险）
- [ ] Step 3: 执行优化（SQL 注入修复 → 日志增强 → 代码清理）
- [ ] Step 4: 自检验证（类名/签名未变、日志保留、SQL 修复）
- [ ] Step 5: 生成变更报告
```

### Step 1: 文件扫描与筛选

1. 扫描目标目录下所有 `*ServiceImpl.java`、`*DaoImpl.java`、`*DAO.java`（带 @Repository）
2. 统计每个文件的行数
3. 将 >1000 行的文件标记为 **SKIP**，记录原因
4. **【新增】输出完整的待处理文件清单，包含文件路径和行数**
5. **【新增】记录待处理文件总数，作为完成进度的基准**
6. 输出待处理文件清单给用户确认后再继续

### Step 2: 单文件分析

对每个待处理文件执行：
1. 读取完整文件内容
2. 判断类型：Service 层 / DAO 层
3. 检查类级日志声明：`@Slf4j` / `LoggerFactory.getLogger()` / `LogFactory.getLog()` / 无
4. **DAO 层额外检查**：
   - 识别所有 SQL 字符串构造代码
   - 标记 SQL 注入风险点（值拼接、动态表名、动态列名）
   - 检查是否已有 `//fixme 存在sql 注入问题` 标记
5. 识别可优化的冗余代码

### Step 3: 执行优化

按以下顺序优化，每修改一个代码块都添加 AI 标记（见"AI 代码标记"节）：

**优先级 1 - SQL 注入修复**（仅 DAO 层）→ 详见"SQL 注入修复指南"
**优先级 2 - 日志增强** → 详见"日志增强规则"
**优先级 3 - 代码逻辑优化** → 详见"代码优化规则"

### Step 4: 自检验证

每个文件修改完后自检：
- [ ] 类名未改变
- [ ] 所有方法签名（名称、入参、返回类型）未改变
- [ ] 已有的日志语句未被删除或修改
- [ ] 所有 SQL 注入风险点已修复
- [ ] AI 标记格式正确

### Step 5: 生成变更报告

所有文件处理完毕后，生成报告 → 详见"变更报告模板"

## SQL 注入修复指南

本项目持久层基于 `BaseDAO`（封装 JdbcTemplate），核心方法：
- `baseDAO.execute(sql, args)` / `baseDAO.queryForList(sql, args, mapper)` / `baseDAO.queryForOne(sql, args, mapper)`
- `baseDAO.getCount(sql, args)` / `baseDAO.executeBatch(sql, batchArgs)`
- 项目已有 `SqlUtil.inSqlCondition()` 用于生成 IN 子句占位符，可继续使用

### 类型 A：值参数拼接 → 参数化查询

**识别特征**：SQL 中用 `+` 将 Java 变量拼入 WHERE/SET 等条件值

**修复前**：
```java
String sql = "SELECT * FROM bas_ele_compare WHERE FISCAL_YEAR = '"
    + year + "' AND MOF_DIV_CODE = '" + mofDivCode + "'";
baseDAO.queryForList(sql, new Object[]{}, mapper);
```

**修复后**：
```java
String sql = "SELECT * FROM bas_ele_compare WHERE FISCAL_YEAR = ? AND MOF_DIV_CODE = ?";
baseDAO.queryForList(sql, new Object[]{year, mofDivCode}, mapper);
```

**另一常见模式 - StringBuilder 拼接**：

**修复前**：
```java
StringBuilder sql = new StringBuilder("SELECT * FROM ele_value WHERE is_deleted = 2");
sql.append(" AND ELE_CATALOG_CODE = '").append(eleCode).append("'");
sql.append(" ORDER BY ELE_CODE, DISP_ORDER");
```

**修复后**：
```java
StringBuilder sql = new StringBuilder("SELECT * FROM ele_value WHERE is_deleted = 2");
sql.append(" AND ELE_CATALOG_CODE = ?");
sql.append(" ORDER BY ELE_CODE, DISP_ORDER");
// 将 eleCode 放入参数数组
```

### 类型 B：动态表名 → 白名单校验

**识别特征**：`"SELECT * FROM " + tableName` 或 `"INSERT INTO " + eleSource`

JDBC 不支持表名参数化，必须使用白名单校验。在 DAO 类中添加私有验证方法：

```java
private static final Set<String> ALLOWED_TABLES = Set.of(
    "ELE_AGENCY", "ELE_CATALOG", "BAS_ELE_COMPARE",
    "ELE_VALUE", "ELE_UNION"
    // 根据该 DAO 实际使用的表名补充
);

private void validateTableName(String tableName) {
    if (tableName == null || !ALLOWED_TABLES.contains(tableName.toUpperCase())) {
        throw new IllegalArgumentException("非法表名: " + tableName);
    }
}
```

**修复前**：
```java
String sql = "SELECT * FROM " + tableName + " WHERE is_deleted = ? AND FISCAL_YEAR = ?";
```

**修复后**：
```java
validateTableName(tableName);
String sql = "SELECT * FROM " + tableName + " WHERE is_deleted = ? AND FISCAL_YEAR = ?";
```

**注意**：白名单中的表名要根据该 DAO 文件中实际使用的所有表名来收集。

### 类型 C：动态列名 → 正则白名单

**识别特征**：`"ORDER BY " + code` 或 `"SELECT a." + code + " AS NEW_CODE"`

列名同样不能参数化。使用正则验证合法列名格式：

```java
private void validateColumnName(String columnName) {
    if (columnName == null || !columnName.matches("^[A-Za-z_][A-Za-z0-9_]{0,63}$")) {
        throw new IllegalArgumentException("非法列名: " + columnName);
    }
}
```

**修复前**：
```java
String sql = "SELECT a." + code + " AS NEW_CODE, a." + name + " AS NEW_NAME FROM "
    + tableName + " a WHERE a.FISCAL_YEAR = '" + year + "'";
```

**修复后**：
```java
validateTableName(tableName);
validateColumnName(code);
validateColumnName(name);
String sql = "SELECT a." + code + " AS NEW_CODE, a." + name + " AS NEW_NAME FROM "
    + tableName + " a WHERE a.FISCAL_YEAR = ?";
baseDAO.queryForList(sql, new Object[]{year}, mapper);
```

### 修复原则

1. 所有来自方法参数的**字符串值、数值**都必须参数化（`?` + `Object[]`）
2. 动态表名/列名无法参数化时，**必须**在拼接前做白名单校验
3. 如代码中已有 `//fixme 存在sql 注入问题` 注释，修复后删除该 fixme 注释
4. `SqlUtil.inSqlCondition()` 等已有工具方法可继续使用
5. 不改变 SQL 的业务逻辑，仅修复安全问题

## 日志增强规则

### 类级别日志声明

| 现有状态 | 处理方式 |
|----------|----------|
| 无日志声明 | 添加 `@Slf4j` 注解 + `import lombok.extern.slf4j.Slf4j;` |
| 已有 `@Slf4j` | 不动，使用 `log` 变量 |
| 已有 `LoggerFactory.getLogger()` | 不改声明，使用已有的 `logger` 或 `log` 变量名 |
| 已有 `LogFactory.getLog()` | 不改声明，使用已有的 `log` 变量名 |

### 方法级日志策略

**Service 层方法**：
```java
// 方法入口
log.info("方法名 - 开始, 关键参数={}, 参数2={}", param1, param2);

// 异常捕获块
log.error("方法名 - 执行异常: {}", e.getMessage(), e);

// 重要业务分支
log.warn("方法名 - 异常分支描述, 参数={}", param);
```

**DAO 层方法**：
```java
// 方法入口（用 debug 避免生产环境日志过多）
log.debug("方法名 - 执行, 关键参数={}", param1);

// 异常捕获块
log.error("方法名 - 数据库操作异常: {}", e.getMessage(), e);
```

### 不添加日志的场景

- 简单 getter/setter 方法
- 纯委托方法（方法体仅 `return dao.xxx()` 一行）
- 循环体内部不添加 `log.info`（如需要可用 `log.debug`）
- 方法中**已有**任何 `log.info/debug/warn/error` 调用 → 整个方法不动

## 代码优化规则

以下优化按优先级从高到低排列：

### 1. StringBuffer → StringBuilder

非线程共享场景下，将 `StringBuffer` 替换为 `StringBuilder`：
```java
// 修复前
StringBuffer sql = new StringBuffer();
// 修复后
StringBuilder sql = new StringBuilder();
```

### 2. 冗余变量消除

```java
// 修复前
List<Map<String, Object>> list = baseDAO.queryForList(sql, args);
return list;
// 修复后
return baseDAO.queryForList(sql, args);
```

仅在变量**只被赋值一次且立即返回**时消除，如果变量被后续引用则保留。

### 3. 条件简化

嵌套 if 合并或提前 return 减少嵌套层级：
```java
// 修复前
if (list != null) {
    if (list.size() > 0) {
        // 大量业务逻辑
    }
}
// 修复后
if (CollectionUtils.isEmpty(list)) {
    return;
}
// 业务逻辑
```

### 4. 空集合检查统一

用 `CollectionUtils.isEmpty()` / `CollectionUtils.isNotEmpty()` 代替 `list != null && list.size() > 0`。
确保 import: `org.springframework.util.CollectionUtils` 或 `org.apache.commons.collections4.CollectionUtils`（根据项目已有 import 选择）。

### 5. 去除注释代码块

大段被注释掉的旧代码（超过 5 行的注释代码块）可以清理删除。

## AI 代码标记

遵循项目 `.qoder/rules/AICodeMarker.md` 规则：

对 Java 文件中每个**新增或修改的代码块**前后添加标记：

```java
// @AI-Begin XXXXX YYYYMMDD @@Qoder
// ... 修改的代码 ...
// @AI-End XXXXX YYYYMMDD @@Qoder
```

- `XXXXX`：5 位字母和数字随机组合（如 `aB3xK`）
- `YYYYMMDD`：北京时间当天日期（8 位数字）
- 标记包裹的是**修改的代码块**，不是整个文件
- 如果一个代码块只修改了一行代码，也要添加标记


### 批量处理策略

**【新增】强制完成规则**:
- 第一个文件处理完后，请用户确认优化风格是否满意
- 确认后，**必须自动继续处理所有剩余文件**，无需再次确认
- 每处理完 5-10 个文件，输出进度报告（已处理/总数）
- **所有待处理文件必须被处理完毕后才能生成最终报告**

### 强制完整遍历规则
1. **必须输出完整文件清单**
   - 在 Step 1 扫描后，输出所有符合条件的文件完整列表
   - 列表格式：`序号. 文件名 (行数) - 状态`
2. **处理进度追踪**
   - 每完成一个文件，输出：`[已完成 X/总数] 文件名`
   - 当所有文件处理完毕才能生成报告
3. **不允许跳过（除超行数限制外）**
   - 除 >1000 行的文件外，所有符合条件的文件必须被处理

### 处理顺序

1. 先 DAO 层，后 Service 层（SQL 注入修复集中在 DAO）
2. 按子包分批处理：
   - `dao/basedata/impl/` → `dao/impl/` → `dao/agencyManager/impl/` → `dao/bookset/impl/` → `dao/synchronization/`
   - `service/basedata/impl/` → `service/impl/` → `service/agencyManager/impl/` → `service/bookset/impl/` → `service/synchronization/impl/` → `service/cache/`

### 处理粒度

- **单次处理 1 个文件**，避免上下文过大
- 每处理完一个文件，立即记录变更到报告
- **第一个文件处理完后**，请用户确认优化风格是否满意，再批量处理其余文件

### 工具类特殊处理

`util/BaseH2DAO.java` 虽非标准 DAO 但含 SQL 拼接，如行数 ≤1000 行也需处理其 SQL 注入问题。

## 变更报告模板

报告文件保存路径：
`element-module/grp-capability-element/element-module-optimizer-report.md`

```markdown
# Element Module Optimizer 变更报告

- 执行时间: YYYY-MM-DD
- 处理文件数: X
- 跳过文件数: Y (超过1000行)
- 总变更数: Z

## 处理统计

| 类别 | 数量 |
|------|------|
| SQL 注入修复 | N 处 |
| 日志增强 | M 个方法 |
| 冗余代码清理 | K 处 |

## 跳过文件清单

| 文件路径 | 行数 | 跳过原因 |
|----------|------|----------|
| dao/basedata/impl/ElementValueDaoImpl.java | 2962 | 超过1000行 |
| ... | ... | ... |

## 变更详情

### 文件: dao/basedata/impl/XxxDaoImpl.java (XXX行)

**SQL 注入修复**:
- 方法 `findXxx()`: 值拼接 → 参数化查询 (第XX行)
- 方法 `loadYyy()`: 动态表名添加白名单校验 (第YY行)

**日志增强**:
- 类级别: 添加 @Slf4j 注解
- 方法 `insertZzz()`: 添加入口日志和异常日志

**代码优化**:
- 第XX行: StringBuffer → StringBuilder
- 第YY行: 消除冗余变量

---

### 文件: service/basedata/impl/XxxServiceImpl.java (XXX行)

**日志增强**:
- 类级别: 添加 @Slf4j 注解
- 方法 `processData()`: 添加入口日志和异常日志

**代码优化**:
- 第XX行: 嵌套条件简化
```

## 已知超大文件（预期跳过）

以下文件超过 1000 行，应自动标记为 SKIP：

| 文件 | 预估行数 |
|------|----------|
| ElementValueServiceImpl.java | ~4914 |
| ElementValueAgencyDaoImpl.java | ~4401 |
| ElementValueAgencyServiceImpl.java | ~2661 |
| ElementValueSpecialServiceImpl.java | ~2410 |
| ElementServiceImpl.java | ~1304 |
| BasMofDivServiceImpl.java | ~1171 |
| FieldServiceImpl.java | ~1045 |
| ElementValueDaoImpl.java | ~2962 |

实际处理时以文件真实行数为准。

## 技术约束

1. **BaseDAO 来自依赖 jar**（`grp-database-com`），不在当前源码中，不可修改
2. `SqlUtil.inSqlCondition()` 来自依赖 jar，可继续使用
3. Lombok 依赖已在项目中声明，`@Slf4j` 可直接使用
4. 项目使用 `org.springframework.util.CollectionUtils`（优先使用）
