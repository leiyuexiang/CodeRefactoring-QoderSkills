# 单文件优化工作流演示

## 示例文件: ElementCompareDaoImpl.java

### Step 1: 读取文件，判断类型

```
文件：dao/basedata/impl/ElementCompareDaoImpl.java
行数：286 行（< 1000，需处理）
类型：DAO 层（@Repository 注解）
日志状态：无日志声明
```

### Step 2: 分析 SQL 注入风险

扫描发现以下风险点：

| 方法 | 风险类型 | 风险描述 |
|------|---------|---------|
| `findByYearAndMofDiv()` | 类型 A（值拼接） | year、mofDivCode 直接拼入 WHERE |
| `loadByTableName()` | 类型 B（动态表名） | tableName 直接拼入 FROM |
| `queryByCode()` | 类型 C（动态列名） | code 字段名直接拼入 SELECT |

### Step 3: 执行优化

#### 优先级 1 - SQL 注入修复

**修复 `findByYearAndMofDiv()` - 类型 A（值拼接→参数化）**:

```java
// 修复前
String sql = "SELECT * FROM bas_ele_compare WHERE FISCAL_YEAR = '"
    + year + "' AND MOF_DIV_CODE = '" + mofDivCode + "'";
baseDAO.queryForList(sql, new Object[]{}, mapper);

// 修复后
// @AI-Begin aB3xK 20260317 @@Qoder
String sql = "SELECT * FROM bas_ele_compare WHERE FISCAL_YEAR = ? AND MOF_DIV_CODE = ?";
baseDAO.queryForList(sql, new Object[]{year, mofDivCode}, mapper);
// @AI-End aB3xK 20260317 @@Qoder
```

**修复 `loadByTableName()` - 类型 B（动态表名→白名单）**:

```java
// 新增白名单和校验方法（类级别）
// @AI-Begin cD4eF 20260317 @@Qoder
private static final Set<String> ALLOWED_TABLES = Set.of(
    "BAS_ELE_COMPARE", "ELE_VALUE", "ELE_CATALOG"
);

private void validateTableName(String tableName) {
    if (tableName == null || !ALLOWED_TABLES.contains(tableName.toUpperCase())) {
        throw new IllegalArgumentException("非法表名: " + tableName);
    }
}
// @AI-End cD4eF 20260317 @@Qoder

// 在方法中添加校验
// @AI-Begin eF5gH 20260317 @@Qoder
validateTableName(tableName);
// @AI-End eF5gH 20260317 @@Qoder
```

#### 优先级 2 - 日志增强

```java
// 类级别添加 @Slf4j
// @AI-Begin gH6jK 20260317 @@Qoder
@Slf4j
// @AI-End gH6jK 20260317 @@Qoder
@Repository("elementCompareDaoImpl")
public class ElementCompareDaoImpl {

    // 方法入口日志
    public List<Map<String, Object>> findByYearAndMofDiv(String year, String mofDivCode) {
        // @AI-Begin jK7mN 20260317 @@Qoder
        log.debug("findByYearAndMofDiv - 执行, year={}, mofDivCode={}", year, mofDivCode);
        // @AI-End jK7mN 20260317 @@Qoder
        // ... 业务代码 ...
    }
}
```

#### 优先级 3 - 代码优化

```java
// StringBuffer → StringBuilder
// @AI-Begin mN8pQ 20260317 @@Qoder
StringBuilder sql = new StringBuilder("SELECT * FROM bas_ele_compare");
// @AI-End mN8pQ 20260317 @@Qoder

// 冗余变量消除
// @AI-Begin pQ9rS 20260317 @@Qoder
return baseDAO.queryForList(sql.toString(), args, mapper);
// @AI-End pQ9rS 20260317 @@Qoder
```

### Step 4: 自检

- [x] 类名 `ElementCompareDaoImpl` 未改变
- [x] 所有方法签名未改变
- [x] 无已有日志语句被修改
- [x] 3 处 SQL 注入风险点已全部修复
- [x] AI 标记格式正确

### Step 5: 记录变更

```
[已完成 1/45] ElementCompareDaoImpl.java
- SQL 注入修复: 3 处 (类型A×1, 类型B×1, 类型C×1)
- 日志增强: 添加 @Slf4j + 3 个方法
- 代码优化: StringBuffer→StringBuilder ×1, 冗余变量 ×1
```
