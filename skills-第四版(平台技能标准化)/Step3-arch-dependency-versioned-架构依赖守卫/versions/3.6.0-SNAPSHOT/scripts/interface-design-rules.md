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

**方法签名冲突处理（强制确定性规则）**：当同一个 DelegateService 合并了多个 DAO（按 D-04-A 强制合并），且不同 DAO 中存在**方法签名相同**的方法（方法名 + 参数列表 + 返回类型完全一致），则**必须**为冲突的方法添加 DAO 来源前缀以区分，前缀规则如下：

- 具名 DAO（如 `UserDao`）：前缀 = DAO 类名首字母小写 + 去掉 `Dao/DAO` 后缀的部分，即 `{daoFieldName}`（如 `userDao` → 前缀 `userDao`，方法 `queryForOne` → `userDaoQueryForOne`）
- 通用 DAO（如 `BaseDAO`）：前缀 = `baseDao`（如方法 `queryForOne` → `baseDaoQueryForOne`）
- 非冲突方法（如 `insertUser` 仅存在于 UserDao）：**不加前缀**，保持原名

**判定标准（不可跳过）**：
1. 列出合并后所有 DAO 的方法签名
2. 检查是否存在**方法名相同**的方法（不论参数是否相同，只要方法名相同就视为冲突）
3. 存在冲突 → 所有冲突方法必须加前缀区分
4. 不存在冲突 → 保持原名不加前缀

**禁止行为**：
- 禁止对不冲突的方法添加前缀（如 `insertUser` 仅存在于一个 DAO，不得改为 `userDaoInsertUser`）
- 禁止仅对部分冲突方法添加前缀而遗漏其他冲突方法
- 禁止使用非 DAO 字段名的自定义前缀

### D-01 反例对照表

以下是常见的违规改名/改类型案例，**严禁在修复过程中出现**：

| DAO 原方法签名 | ✅ 正确的 Service 接口方法 | ❌ 错误的 Service 接口方法 | 错误类型 |
|-----------|---------------|---------------|----------|
| `Object queryForOne(String sql, Object[] params)` | `Object queryForOne(String sql, Object[] params)` | `Map<String,Object> queryUserForOne(String sql, Object[] params)` | 改名 + 改返回类型 |
| `int execute(String sql, Object[] params)` | `int execute(String sql, Object[] params)` | `void executeSql(String sql, Object[] params)` | 改名 + 改返回类型 |
| `int execute(String sql, Object[] params)` | `int execute(String sql, Object[] params)` | 拆分为 `executeSql()` + `executeAndReturn()` | 方法拆分 |
| `void batchExecute(String sql, String key, List params)` | `void batchExecute(String sql, String key, List params)` | `void batchInsertUserExt(String sql, String keys, List<UserExt> entities)` | 改名 + 参数泛型特化 |
| `void insertUser(UserPo userPo)` | `void insertUser(UserPo userPo)` | `void addUser(UserPo userPo)` | 改名 |
| `int updateUser(UserPo userPo)` | `int updateUser(UserPo userPo)` | `void updateUser(UserPo userPo)` | 改返回类型 |

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

## 规则 D-03：禁止方法合并/拆分/迁移

一个 DAO 方法对应一个 Service 接口方法，**不得合并多个 DAO 调用为一个方法，不得拆分一个 DAO 方法为多个方法，不得将 Controller 的 private 方法整体迁移到 Service 层**。

**禁止行为**：
- 将 Controller 中的 `userDao.queryForOne()` + `userDao.execute()` 合并为 `service.queryAndExecute()`
- 将 Controller 中的 `userDao.batchExecute(sql, keys, list)` 拆分为 `service.batchInsertUserExt(list)`（参数被重新设计）
- 将 Controller 中包含多行 DAO 调用的 private 方法（如 `insertAgency()`）整体迁移到 Service 层

**正确做法**： Controller 的 private 方法保留在原位，其内部的 DAO 调用改为通过 DelegateService 接口转发。

---

## 规则 D-04：接口命名规范

| 原类名 | 接口名 | 说明 |
|--------|--------|------|
| `XxxDao` | `IXxxDelegateService` | DAO 委托服务 |
| `XxxService`（Impl 类） | `IXxxService` | 加 `I` 前缀 |
| `XxxServiceImpl`（Impl 类） | `IXxxService` | 去掉 `Impl`，加 `I` 前缀 |

如果项目中已存在命名为 `IXxxService` 的接口，则直接复用该接口（在其中追加缺失方法），不新建。

### D-04 补充：命名确定性规则

当为消除 Controller→DAO 依赖而新建 Service 时，命名必须遵循以下确定性规则：

1. **接口名**：`I{原DAO类名去掉Dao/DAO后缀}DelegateService`
   - 例如：原注入 `UserDao` → 接口名 `IUserDelegateService`
   - 例如：原注入 `RoleGroupDao` → 接口名 `IRoleGroupDelegateService`
   - 例如：原注入 `BaseDAO`（泛型/通用DAO） → 接口名 `I{当前Controller对应业务名}DelegateService`（如 Controller 为 `SynchronizeInfoController` → 接口名 `ISynchronizeInfoDelegateService`）
2. 如一个 Controller 注入了**多个 DAO** → 所有 DAO 方法合并到**同一个**委托 Service 中，接口名以 Controller 业务名命名
3. **禁止**使用业务场景名（如 Bjca、Sync 等临时前缀）作为接口名
4. 实现类名：接口名去掉 `I` 前缀，加 `Impl` 后缀（如 `SynchronizeInfoDelegateServiceImpl`）

### D-04-A 补充：多 DAO 合并策略的强制判定规则（消除歧义）

当一个 Controller 注入了多个 DAO 时，**必须按以下确定性规则判定是合并还是拆分**，不允许自行决定：

**强制合并（一个 Controller 对应一个 DelegateService）**：
- 一个 Controller 注入了多个 DAO → **必须合并**到同一个 DelegateService
- 接口名以 Controller 业务名命名（去掉 Controller 后缀），不以单个 DAO 名命名
- 实现类内注入所有被替代的 DAO

**禁止拆分**：
- 禁止为同一 Controller 的多个 DAO 分别创建多个独立的 DelegateService
- 即使多个 DAO 属于不同业务域，也必须合并到一个 DelegateService 中

**决策矩阵**：

| 场景 | DAO 数量 | 操作 | 接口名 |
|------|----------|------|--------|
| Controller 注入 1 个具名 DAO（如 UserDao） | 1 | 按 DAO 名命名 | `IUserDelegateService` |
| Controller 注入 1 个通用 DAO（如 BaseDAO） | 1 | 按 Controller 业务名命名 | `I{ControllerBiz}DelegateService` |
| Controller 注入 N 个 DAO（N≥2） | N≥2 | **必须合并** | `I{ControllerBiz}DelegateService` |

### D-04-B 补充：跨 Controller 的 DelegateService 复用策略（强制确定性规则）

当多个不同 Controller 注入了相同的 DAO 时，**必须按以下确定性规则判定是复用还是各自创建**，不允许自行决定：

**强制独立创建（每个 Controller 一个 DelegateService）**：
- 每个 Controller 独立创建自己的 DelegateService，**不跨 Controller 共享**
- 即使两个 Controller 注入了完全相同的 DAO（如都注入了 `UserDao`），也必须分别创建各自的 DelegateService
- 接口名按 D-04 规则各自命名

**禁止复用**：
- 禁止一个 DelegateService 同时服务于多个 Controller
- 禁止因为多个 Controller 使用了相同的 DAO 就让它们共享 DelegateService
- 每个 DelegateService 的方法集合仅包含其对应 Controller 实际调用的 DAO 方法

**决策矩阵**：

| 场景 | 操作 | 说明 |
|------|------|------|
| ControllerA 注入 UserDao，ControllerB 也注入 UserDao | 各自独立创建 | ControllerA → `I{A业务名}DelegateService`，ControllerB → `I{B业务名}DelegateService` |
| ControllerA 注入 UserDao + BaseDAO，ControllerB 注入 UserDao | 各自独立创建 | ControllerA 合并两个 DAO，ControllerB 单独创建 |
| ControllerA 和 ControllerB 在同一个 `bjca` 业务域下 | 各自独立创建 | 即使同一业务域，也不共享 DelegateService |

**原因**：跨 Controller 复用 DelegateService 会导致接口方法集合不可预测（取决于扫描顺序和 Controller 数量），违反确定性原则。每个 Controller 独立创建 DelegateService 可确保多次执行结果完全一致。

**端到端示例**：

```
输入条件：
  SynchronizeInfoController 注入 UserDao + BaseDAO
  SynchronizeUserController 注入 UserDao

正确做法：
  1. SynchronizeInfoController → ISynchronizeInfoDelegateService（合并 UserDao + BaseDAO 方法）
  2. SynchronizeUserController → ISynchronizeUserDelegateService（仅 UserDao 方法）
  两个 DelegateService 各自独立，互不干扰

错误做法：
  - 让 SynchronizeUserController 也注入 ISynchronizeInfoDelegateService ← 禁止跨Controller复用！
  - 合并两个 Controller 的 DAO 调用到同一个 DelegateService ← 禁止！
```

### D-04 端到端命名推导示例

以下示例展示从输入到最终文件的完整推导过程，**修复时必须按此流程执行**：

**示例 1：单个 DAO 注入**

```
输入条件：
  Controller = SynchronizeInfoController
  注入字段 = UserDao（包路径: grp.pt.frame.config.user.dao）

推导过程：
  1. DAO 类名 = UserDao
  2. 去掉 Dao 后缀 = User
  3. 接口名 = I + User + DelegateService = IUserDelegateService
  4. 实现类名 = UserDelegateService + Impl = UserDelegateServiceImpl
  5. 包路径 = DAO包 config.user.dao -> Service包 config.user.service
  6. 实现类包 = config.user.service.impl

最终文件：
  接口: config/user/service/IUserDelegateService.java
  实现: config/user/service/impl/UserDelegateServiceImpl.java
```

**示例 2：通用 DAO 注入（BaseDAO）**

```
输入条件：
  Controller = DataCheckController
  注入字段 = BaseDAO（通用DAO，无业务前缀）
  Controller包路径 = grp.pt.frame.config2.dataCheck.controller

推导过程：
  1. DAO 类名 = BaseDAO（通用DAO，无法去后缀得到业务名）
  2. 按规则：以 Controller 业务名命名 -> DataCheck
  3. 接口名 = I + DataCheck + DelegateService = IDataCheckDelegateService
  4. 实现类名 = DataCheckDelegateServiceImpl
  5. 包路径 = Controller包 config2.dataCheck -> config2.dataCheck.service
  6. 实现类包 = config2.dataCheck.service.impl

最终文件：
  接口: config2/dataCheck/service/IDataCheckDelegateService.java
  实现: config2/dataCheck/service/impl/DataCheckDelegateServiceImpl.java
```

**示例 3：多个 DAO 注入（强制合并）**

```
输入条件：
  Controller = AgentRightController
  注入字段 = RoleGroupDao + UserMenuDao
  Controller包路径 = grp.pt.frame.config2.agent.controller

推导过程：
  1. 多个 DAO（2个） -> 按 D-04-A 强制合并到同一个委托 Service
  2. 以 Controller 业务名命名 -> AgentRight
  3. 接口名 = IAgentRightDelegateService
  4. 实现类名 = AgentRightDelegateServiceImpl
  5. 实现类内注入 RoleGroupDao 和 UserMenuDao
  6. 包路径 = Controller包 config2.agent -> config2.agent.service

最终文件：
  接口: config2/agent/service/IAgentRightDelegateService.java
  实现: config2/agent/service/impl/AgentRightDelegateServiceImpl.java

  ❌ 错误做法：
  - 分别创建 IRoleGroupDelegateService 和 IUserMenuDelegateService ← 禁止拆分！
```

**示例 4：一个 Controller 注入 具名DAO + 通用DAO（强制合并）**

```
输入条件：
  Controller = SynchronizeInfoController
  注入字段 = UserDao + BaseDAO
  Controller包路径 = grp.pt.frame.config.bjca.controller

推导过程：
  1. 多个 DAO（2个） -> 按 D-04-A 强制合并
  2. 以 Controller 业务名命名 -> SynchronizeInfo
  3. 接口名 = ISynchronizeInfoDelegateService
  4. 实现类名 = SynchronizeInfoDelegateServiceImpl
  5. 实现类内注入 UserDao 和 BaseDAO
  6. 包路径 = Controller 自身业务域 config.bjca -> config.bjca.service（禁止用 bjca 以外的路径）
     注意：由于多 DAO 跨域，使用 Controller 所在业务域的 service 包

最终文件：
  接口: config/bjca/service/ISynchronizeInfoDelegateService.java（注意：此处 bjca 是 Controller 所在的业务域，不是临时前缀）
  实现: config/bjca/service/impl/SynchronizeInfoDelegateServiceImpl.java

  ❌ 错误做法：
  - 创建 IUserDelegateService + 另一个 IUserDelegateService（注入两次同一接口） ← 禁止拆分！
  - 将 UserDao 方法和 BaseDAO 方法分到两个接口 ← 禁止拆分！
```

---

## 规则 D-05：方法注释要求（标准化格式）

每个接口方法**必须**包含中文 JavaDoc 注释。注释格式必须**严格统一**，确保多次执行结果一致：

### D-05-A 标准注释格式

**方法注释**：仅包含一行功能说明（`@param` 和 `@return` 可选但建议统一省略，除非方法参数含义不明确）：

```java
/**
 * 根据 SQL 查询单条记录
 */
Object queryForOne(String sql, Object[] params);
```

**类注释**（新建接口和实现类必须添加）：统一格式为两行——第一行写类的角色说明，第二行写修复原因：

```java
/**
 * {业务名} 代理服务接口
 * 为 Controller 层提供 {原DAO类名} 的转发访问，修复 S1-02 违规
 */
public interface IXxxDelegateService {
```

### D-05-B 禁止行为

- **禁止**无注释的接口方法
- **禁止**在部分方法上添加详细 `@param/@return`、另一部分方法只写一行说明（格式必须全部统一）
- **禁止**两次执行时，一次加详细注释、另一次加简略注释

### D-05-C 注释内容确定性规则

方法注释的功能说明**必须**按以下模板生成，不允许自由发挥：

| 方法类型 | 注释模板 | 示例 |
|----------|---------|------|
| 查询单条 | `根据 SQL 查询单条记录` | `queryForOne` |
| 查询列表 | `根据 SQL 查询记录列表` | `queryForList` |
| 执行 SQL | `执行 SQL 操作` | `execute` |
| 批量执行 | `批量执行 SQL 操作` | `batchExecute` |
| 插入 | `新增{实体名}` | `insertUser` → `新增用户` |
| 更新 | `更新{实体名}` | `updateUser` → `更新用户` |
| 删除 | `删除{实体名}` | `deleteUser` → `删除用户` |
| 按条件查询 | `根据{条件名}查询{结果名}` | `queryRoleListByUserId` → `根据用户ID查询角色列表` |
| 带DAO前缀的冲突方法 | `通用{操作说明}（{DAO来源名}）` | `userDaoQueryForOne` → `通用查询单条记录（UserDao）` |
| 其他 | `{方法名的中文直译}` | 按方法名语义直译 |

---

## 规则 D-06：import 最小化

接口文件只 import 方法签名中出现的类型（参数类型、返回类型、throws 异常类型），不得 import 实现细节类（如 DAO 类、工具类、常量类等）。

---

## 规则 D-08：新建文件的 Maven 模块位置（强制）

新建的 DelegateService 接口和实现类**必须**放在 Service 层模块（即 `{module}-server-com` / `{module}-service` 模块）中，**禁止**放在 Controller 层模块（即 `{module}-server` / `{module}-controller` 模块）中。

**判定规则**：

1. 如果工程中存在 `{module}-server-com` 模块（Service层模块），新建文件放在该模块下
2. 如果工程中存在 `{module}-service` 模块（Step2 重命名后），新建文件放在该模块下
3. **禁止**在 Controller 层模块中创建新的 Service 接口和实现类
4. 已有的被依赖类（如 Entity、DAO、原有Service）本身所在的模块不变

**原因**：DelegateService 的本质是 Service 层组件，应遵循分层原则放在 Service 层模块中。放在 Controller 层模块会导致分层架构被破坏。

**示例**：

```
工程结构：
  grp-capability-framework/
    framework-server/          ← Controller 层模块
    framework-server-com/      ← Service 层模块

✅ 正确：新建文件放在 framework-server-com 中
  framework-server-com/src/main/java/grp/pt/frame/config/user/service/IUserDelegateService.java
  framework-server-com/src/main/java/grp/pt/frame/config/user/service/impl/UserDelegateServiceImpl.java

❌ 错误：新建文件放在 framework-server 中
  framework-server/src/main/java/grp/pt/frame/config/user/service/IUserDelegateService.java
```

---

## 规则 D-09：替换后字段命名确定性规则（强制）

替换注入类型后，字段名**必须**按以下确定性规则命名，不允许保留原字段名或自由命名：

### S1-01（Controller→Controller 替换为 Service 接口）

字段名 = 接口名首字母小写（去掉 `I` 前缀后首字母小写）
- `IWorkflowRunService` → 字段名 `workflowRunService`
- `IUserLogService` → 字段名 `userLogService`

### S1-02（DAO 替换为 DelegateService）

字段名 = 接口名首字母小写（去掉 `I` 前缀后首字母小写）
- `IGXTPSDelegateService` → 字段名 `gXTPSDelegateService`
- `ISynchronizeInfoDelegateService` → 字段名 `synchronizeInfoDelegateService`
- `IAgentRightDelegateService` → 字段名 `agentRightDelegateService`

**非常重要 — 首字母小写算法：**去掉接口名中的 `I` 前缀，剩余部分首字母小写。具体示例：
- `IGXTPSDelegateService` 去掉 `I` 得 `GXTPSDelegateService`，首字母 `G` 小写 → `gXTPSDelegateService`
- `IUserLogDelegateService` 去掉 `I` 得 `UserLogDelegateService`，首字母 `U` 小写 → `userLogDelegateService`

**违规示例对比**：

| 接口类型 | 正确字段名 | 错误字段名 | 错误原因 |
|---|---|---|---|
| `IGXTPSDelegateService` | `gXTPSDelegateService` | `gxTpsDelegate` | 缩写，未按首字母小写算法 |
| `IGXTPSDelegateService` | `gXTPSDelegateService` | `gxtpsDelegateService` | 首字母算法错误（GXTPS全部小写） |
| `IUserLogDelegateService` | `userLogDelegateService` | `userLogDelegate` | 遗漏了 Service 后缀 |

**禁止行为**：
- 禁止保留原 DAO 字段名（如 `gxTpsDao`、`baseDAO`）
- 禁止使用缩写或非标准命名（如 `gxTPSDel`、`syncDel`）
- 禁止同一接口类型创建多个字段（如注入两次 `IUserDelegateService`）

### S1-03（ServiceImpl 替换为 Service 接口）

字段名保持原字段名不变（因为只是改了类型，字段语义不变）
- 原 `private UserService userService` → 改为 `private IUserService userService`（字段名不变）

---

## 规则 D-07：已有接口复用（仅适用于 S1-03）

**适用范围**：本规则仅适用于 S1-03（Controller 注入 ServiceImpl → Service 接口）场景。

**不适用于 S1-02**：在 S1-02（Controller→DAO）场景中，**必须新建独立的 DAO 委托 Service**（按 D-04 命名），禁止在已有业务 Service 接口上追加 DAO 委托方法。

**S1-03 场景规则**：

当 Controller 中注入的 ServiceImpl 已有对应接口（如 `IUserService`）时：

1. **优先复用已有接口**，不新建
2. 检查 Controller 中调用的所有方法是否已在接口中声明
3. 若接口缺少方法 → 在接口末尾追加缺失的方法声明（签名与 Impl 一致）
4. 追加的方法同样必须有 JavaDoc 注释

**决策矩阵（消除歧义）**：

| 场景 | 已有接口？ | 操作 | 说明 |
|------|-----------|------|------|
| S1-03: `UserService`(Impl) → `IUserService` | ✅ 存在 | 复用接口 + 追加缺失方法 | D-07 适用 |
| S1-03: `UserTempService`(Impl) | ❌ 不存在 | 新建 `IUserTempService` | D-04 适用 |
| S1-02: Controller 注入 `UserDao` | — | **必须新建** `IUserDelegateService` | D-04 适用，D-07 不适用 |
| S1-02: Controller 已有 `IAgentRightService` | ✅ 存在 | **仍然新建** `IAgentRightDelegateService` | D-07 不适用于 S1-02 |

### D-07 补充：追加方法排序规则

在已有接口末尾追加方法时，必须遵循以下排序规则：

1. 按方法在 **ServiceImpl 中的声明顺序**排列（从上到下）
2. 如无法确定 Impl 中的声明顺序（如方法来自多个 Impl），按 **Controller 中的首次调用顺序**排列
3. 同一次修复中追加的所有方法使用**同一个 AI 标记块**包裹
4. 追加方法块的位置：放在接口文件的**最后一个方法声明之后、类结束 `}` 之前**

---

## 规则 D-10：Controller 业务名提取确定性算法（强制）

当需要使用"Controller 业务名"命名 DelegateService 时（通用 DAO、多 DAO 合并等场景），**必须**按以下确定性算法提取，不允许自由发挥：

### 提取算法

```
输入: Controller 类名（如 SynchronizeInfoController）
输出: 业务名（如 SynchronizeInfo）

步骤:
1. 取 Controller 类的简单类名（不含包路径）
2. 去掉末尾的 "Controller" 后缀
3. 所得即为业务名，保持原始大小写不变
```

### 示例对照表

| Controller 类名 | 去掉 Controller 后缀 | 业务名 | 接口名 |
|---|---|---|---|
| `SynchronizeInfoController` | `SynchronizeInfo` | `SynchronizeInfo` | `ISynchronizeInfoDelegateService` |
| `DataCheckController` | `DataCheck` | `DataCheck` | `IDataCheckDelegateService` |
| `AgentRightController` | `AgentRight` | `AgentRight` | `IAgentRightDelegateService` |
| `BJCAController` | `BJCA` | `BJCA` | `IBJCADelegateService` |
| `GXTPSController` | `GXTPS` | `GXTPS` | `IGXTPSDelegateService` |
| `TenantController` | `Tenant` | `Tenant` | `ITenantDelegateService` |
| `UserMgmtController` | `UserMgmt` | `UserMgmt` | `IUserMgmtDelegateService` |

### 边缘 Case 处理

| 边缘情况 | 处理规则 | 示例 |
|----------|---------|------|
| 类名不以 `Controller` 结尾 | 直接使用完整类名作为业务名 | `UserApi` → 业务名 `UserApi` |
| 类名为 `Controller`（无业务前缀） | 使用类所在包的最后一级包名（首字母大写） | `grp.pt.config.user.Controller` → 业务名 `User` |
| 类名包含多个 `Controller` 子串 | 仅去掉**末尾**的 `Controller` | `ControllerConfigController` → 业务名 `ControllerConfig` |

---

## 规则 D-11：S1-03 已有接口查找路径确定性规则（强制）

修复 S1-03 时需要判断被注入的 ServiceImpl 是否已有对应 Service 接口。**必须**按以下确定性规则查找，不允许猜测：

### 查找步骤（按优先级顺序，命中第一个即停止）

```
输入: ServiceImpl 类（如 UserService，包路径 grp.pt.frame.config.user.service.impl.UserService）

1. 检查 ServiceImpl 类声明中的 implements 子句：
   - 如果 ServiceImpl implements IXxxService → 该接口即为目标接口
   - 直接读取 ServiceImpl 源码中的 class 声明行确认

2. 如果 implements 子句为空或仅实现框架接口（如 Serializable）：
   - 按命名规则推导接口名：
     a. XxxServiceImpl → IXxxService
     b. XxxService（不含Impl后缀的具体类） → IXxxService
   - 在 ServiceImpl 同模块的 service 包下搜索该接口文件：
     Grep pattern: public interface {推导的接口名}
     Grep path: {service-module-src-path}

3. 如果搜索无结果 → 判定为"无已有接口"，需要新建
```

### 查找路径确定性规则

| 查找顺序 | 查找位置 | 说明 |
|---------|---------|------|
| 1 | ServiceImpl 的 `implements` 声明 | 最可靠，直接从代码中获取 |
| 2 | ServiceImpl 同包的上级 `service/` 目录 | 如 `service.impl.UserService` → 查找 `service.IUserService` |
| 3 | Service 层模块的全局搜索 | `Grep pattern: public interface I{Xxx}Service` |
| 4 | 判定为不存在 | 需要新建接口 |

**禁止行为**：
- 禁止仅凭类名猜测接口是否存在（必须实际读取或搜索确认）
- 禁止在 Controller 层模块中搜索接口（接口应在 Service 层模块中）
- 禁止因为"可能存在"就跳过新建步骤
