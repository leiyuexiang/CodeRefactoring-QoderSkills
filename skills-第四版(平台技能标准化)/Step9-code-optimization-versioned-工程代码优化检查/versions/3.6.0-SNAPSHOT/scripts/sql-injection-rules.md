# SQL 注入修复指南

本项目持久层基于 `BaseDAO`（封装 JdbcTemplate），核心方法：
- `baseDAO.execute(sql, args)` / `baseDAO.queryForList(sql, args, mapper)` / `baseDAO.queryForOne(sql, args, mapper)`
- `baseDAO.getCount(sql, args)` / `baseDAO.executeBatch(sql, batchArgs)`
- 项目已有 `SqlUtil.inSqlCondition()` 用于生成 IN 子句占位符，可继续使用

---

## 类型 A：值参数拼接 → 参数化查询

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

---

## 类型 B：动态表名 → 白名单校验

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

---

## 类型 C：动态列名 → 正则白名单

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

---

## 修复原则

1. 所有来自方法参数的**字符串值、数值**都必须参数化（`?` + `Object[]`）
2. 动态表名/列名无法参数化时，**必须**在拼接前做白名单校验
3. 如代码中已有 `//fixme 存在sql 注入问题` 注释，修复后删除该 fixme 注释
4. `SqlUtil.inSqlCondition()` 等已有工具方法可继续使用
5. 不改变 SQL 的业务逻辑，仅修复安全问题
