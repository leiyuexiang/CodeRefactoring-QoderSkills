# 接口设计确定性规范

## 核心原则：直接代理

新建 Service 接口的唯一目的是**解除 Controller 对 DAO/Impl 的直接依赖**，不是重新设计业务接口。Service 层在此场景中仅充当"调用中转层"。

---

## 规则 D-01：方法签名一致

新建 Service 接口方法的签名**必须**与被代理的 DAO/Impl 原方法完全一致：

- 方法名相同
- 参数列表相同（类型、顺序、个数）
- 返回类型相同
- throws 声明相同

**禁止行为**：
- 不得更改方法名（如将 `queryForOne` 改为 `queryUserByCode`）
- 不得更改参数类型（如将 `String sql, Object[] params` 改为 `String code`）
- 不得更改返回类型（如将 `int` 改为 `void`）

**唯一例外**：当同一个 Controller 注入了多个同类型 DAO（如两个不同的 `BaseDAO` 实例），为避免方法签名冲突，允许在方法名上添加区分前缀（如 `queryUserForOne` / `queryBaseForOne`），但参数和返回值仍必须一致。

---

## 规则 D-02：禁止 SQL 逻辑移动

Controller 中的 SQL 拼接逻辑**必须保留在原位**（Controller 中），不得移入 Service 层。

**正确做法**（SQL 留在 Controller）：

```java
// Controller
String sql = "SELECT * FROM user WHERE code = ?";
Map<String, Object> result = synchronizeService.queryForOne(sql, new Object[]{code});
```

```java
// Service 接口
Map<String, Object> queryForOne(String sql, Object[] params);
```

**错误做法**（SQL 被移入 Service）：

```java
// Controller
Map<String, Object> result = synchronizeService.queryUserByCode(code);
```

```java
// Service 实现类
public Map<String, Object> queryUserByCode(String code) {
    String sql = "SELECT * FROM user WHERE code = ?";  // SQL 不应出现在这里
    return userDao.queryForOne(sql, new Object[]{code});
}
```

**原因**：移动 SQL 逻辑属于"改变业务逻辑的组织方式"，违反安全约束 S-03。

---

## 规则 D-03：禁止方法合并/拆分

一个 DAO 方法对应一个 Service 接口方法，**不得合并多个 DAO 调用为一个方法，不得拆分一个 DAO 方法为多个方法**。

**禁止行为**：
- 将 Controller 中的 `userDao.queryForOne()` + `userDao.execute()` 合并为 `service.queryAndExecute()`
- 将 Controller 中的 `userDao.batchExecute(sql, keys, list)` 拆分为 `service.batchInsertUserExt(list)`（参数被重新设计）

**唯一例外**：当 Controller 中存在包含多行 DAO 调用的 private 方法（如 `insertAgency()`），该方法整体作为一个 Service 方法迁移是允许的（方法名和参数保持一致）。

---

## 规则 D-04：接口命名规范

| 原类名 | 接口名 | 说明 |
|--------|--------|------|
| `XxxDao` | `IXxxDelegateService` | DAO 委托服务 |
| `XxxService`（Impl 类） | `IXxxService` | 加 `I` 前缀 |
| `XxxServiceImpl`（Impl 类） | `IXxxService` | 去掉 `Impl`，加 `I` 前缀 |

如果项目中已存在命名为 `IXxxService` 的接口，则直接复用该接口（在其中追加缺失方法），不新建。

---

## 规则 D-05：方法注释要求

每个接口方法**必须**包含中文 JavaDoc 注释，至少一行功能说明：

```java
/**
 * 根据 SQL 查询单条记录
 */
Map<String, Object> queryForOne(String sql, Object[] params);
```

**禁止**无注释的接口方法：

```java
// 错误：缺少 JavaDoc
Map<String, Object> queryForOne(String sql, Object[] params);
```

---

## 规则 D-06：import 最小化

接口文件只 import 方法签名中出现的类型（参数类型、返回类型、throws 异常类型），不得 import 实现细节类（如 DAO 类、工具类、常量类等）。

---

## 规则 D-07：已有接口复用

当 Controller 中注入的 ServiceImpl 已有对应接口（如 `IUserService`）时：

1. **优先复用已有接口**，不新建
2. 检查 Controller 中调用的所有方法是否已在接口中声明
3. 若接口缺少方法 → 在接口末尾追加缺失的方法声明（签名与 Impl 一致）
4. 追加的方法同样必须有 JavaDoc 注释
