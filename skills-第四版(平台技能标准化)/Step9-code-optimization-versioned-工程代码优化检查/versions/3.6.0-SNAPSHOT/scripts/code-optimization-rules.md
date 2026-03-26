# 代码优化规则

以下优化按优先级从高到低排列：

---

## 1. StringBuffer → StringBuilder

非线程共享场景下，将 `StringBuffer` 替换为 `StringBuilder`：

```java
// 修复前
StringBuffer sql = new StringBuffer();
// 修复后
StringBuilder sql = new StringBuilder();
```

**判定标准**：
- `StringBuffer` 变量仅在方法内部使用（局部变量） → 替换
- `StringBuffer` 作为类成员变量或跨线程共享 → 不替换

---

## 2. 冗余变量消除

```java
// 修复前
List<Map<String, Object>> list = baseDAO.queryForList(sql, args);
return list;
// 修复后
return baseDAO.queryForList(sql, args);
```

**判定标准**：
- 变量**只被赋值一次且立即返回** → 消除
- 变量被后续引用（日志、条件判断等） → 保留
- 变量赋值后有 finally 块依赖 → 保留

---

## 3. 条件简化

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

**判定标准**：
- 嵌套 if 深度 ≥ 2 层且内部为主要业务逻辑 → 提前 return
- 方法有返回值时需确保 return 的值正确（如返回 null 或空集合）
- 不改变业务逻辑语义

---

## 4. 空集合检查统一

用 `CollectionUtils.isEmpty()` / `CollectionUtils.isNotEmpty()` 代替手动检查：

```java
// 修复前
if (list != null && list.size() > 0) {
// 修复后
if (CollectionUtils.isNotEmpty(list)) {
```

```java
// 修复前
if (list == null || list.size() == 0) {
// 修复后
if (CollectionUtils.isEmpty(list)) {
```

**import 选择**：
- 优先使用 `org.springframework.util.CollectionUtils`（项目已有）
- 如项目中已有 `org.apache.commons.collections4.CollectionUtils`，保持一致

---

## 5. 去除注释代码块

大段被注释掉的旧代码（超过 5 行的注释代码块）可以清理删除。

**判定标准**：
- 连续 5 行以上的 `//` 注释且内容为 Java 代码 → 删除
- `/* ... */` 包裹的大段废弃代码 → 删除
- 功能说明注释、TODO/FIXME 注释 → 保留
- 少于 5 行的注释代码 → 保留
