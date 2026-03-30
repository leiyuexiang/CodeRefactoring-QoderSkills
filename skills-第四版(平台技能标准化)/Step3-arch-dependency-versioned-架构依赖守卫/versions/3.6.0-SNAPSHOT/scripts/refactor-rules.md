# S1 修复规范

## 修复规范一：消除 Controller→Controller 依赖

### 修复决策树

按以下优先级顺序判断修复策略：

```
1. 被注入的 Controller 仅用于 Logger 引用（如 XxxController.class 用于 getLogger）？
   → SKIP，标注为"仅 Logger 引用，非字段注入"，在报告中标记为 SKIP

2. 被注入的 Controller 有对应的 Service 接口，且该接口覆盖了所有被调用方法？
   → 策略 A：直接替换为 Service 接口注入

3. 被注入的 Controller 有对应的 Service 接口，但接口未覆盖部分被调用方法？
   → 策略 A + 在接口中追加缺失方法（签名与 Controller/Impl 方法一致）

4. 被注入的 Controller 方法包含复合逻辑（Session 处理、多 Service 编排等）？
   → 策略 B：提取 Private Helper 方法

5. 以上均不适用？
   → 标记为 SKIP 并说明原因，留待人工处理
```

## 修复策略 A：替换为 Service 接口注入（优先，最小改动）

**核心原则：只改注入类型和字段名，不改调用逻辑。**

执行步骤（按顺序，不可跳过）：

1. 查找被注入 Controller 是否有对应的 Service 接口（如 `WorkflowRunController` → `IWorkflowRunService`）
2. 如果有接口 → 检查 Controller 中调用的所有方法是否在接口中已声明
   - 全部已声明 → **直接将字段类型从 Controller 改为 Service 接口**，字段名同步修改
   - 部分缺失 → 在接口中追加缺失方法声明（签名与 Controller/Impl 方法一致），然后替换字段类型
3. 如果没有接口 → 标记为 SKIP，留待人工处理

**禁止行为**：
- 禁止将 Controller 的调用"封装"为新的 Service 方法（如将 `workflowRunController.createProcessInstance(a,b,c,d)` 封装为 `userTempService.createUserExamProcess(a)`）
- 禁止在调用处更改方法名、参数或调用方式
- 禁止因修复 S1-01 而在不相关的 Service 中新增方法

### 修复策略 B：提取 Private Helper 方法

当被调用 Controller 方法包含复合逻辑（Session 处理、多 Service 编排等）时：

1. 在当前 Controller 中创建 private 方法
2. 将被调用 Controller 方法的核心逻辑复制到 private 方法中
3. 将 Controller 调用替换为 Service 调用
4. 移除对被调用 Controller 的 `@Autowired` 注入

### 注意事项

- 修复后必须移除对 Controller 类的 `@Autowired` 声明和 `import` 语句
- 新注入的 Service 必须是接口类型（非 Impl）
- 确保所有调用点都已替换，无遗漏

### 移除代码注释规范

移除 `@Autowired` 注入或 `import` 时，遵循以下统一规范：

1. **直接删除**被移除的代码行，**不保留** `// removed:` 注释
2. 在**新增的替代注入处**添加一行中文注释说明替代关系，格式为：`// 替代原 {原类名} 注入，修复 {S1-0x} 违规`
3. 注释语言**统一使用中文**，不使用英文

**正确示例**：
```java
// @AI-Begin Rv7mK 20260325 @@Qoder
// 替代原 WorkflowRunController 注入，修复 S1-01 违规
@Autowired
private IWorkflowRunService workflowRunService;
// @AI-End Rv7mK 20260325 @@Qoder
```

**错误示例**：
```java
// removed: WorkflowRunController workflowRunController (S1-01 Controller→Controller violation)  ← 错误！不保留移除注释
```

---

## 修复规范二：Controller→DAO/Mapper 补建 Service 中间层

### 修复步骤

1. 在 `service/` 目录（或 `service/facade/`）下创建 Service 接口
2. 在 `service/impl/` 下创建 ServiceImpl 实现类
3. 将 Controller 中的 DAO **直接方法调用语句** 移入 ServiceImpl
4. Controller 改为注入 Service 接口

模板文件参见：
- Service 接口模板 → [templates/service-interface.java](../templates/service-interface.java)
- ServiceImpl 模板 → [templates/service-impl.java](../templates/service-impl.java)

### DAO 调用逻辑迁移边界

**迁移原则**：仅迁移 DAO/Mapper 的**直接方法调用语句**，SQL 拼接逻辑**必须留在 Controller 中**。

新建 Service 方法签名**必须与 DAO 原方法签名一致**（遵循 D-01 规则），不允许将 SQL 封装为业务方法。

**正确做法**（SQL 留在 Controller，Service 纯转发）：
```java
// Controller
String sql = "SELECT * FROM GAP_USER U WHERE U.CODE = ?";
Map<String, Object> result = delegateService.queryForOne(sql, new Object[]{code});

// Service 接口
Object queryForOne(String sql, Object[] params);

// ServiceImpl
@Override
public Object queryForOne(String sql, Object[] params) {
    return baseDAO.queryForOne(sql, params);  // 纯转发
}
```

**错误做法**（SQL 被封装到 Service 中）：
```java
// Controller  ← 错误！SQL 不应被封装
Map<String, Object> result = syncService.queryUserByCode(code);

// ServiceImpl  ← 错误！不应包含 SQL 拼接
public Map<String, Object> queryUserByCode(String code) {
    String sql = "SELECT * FROM GAP_USER U WHERE U.CODE = ?";
    return baseDAO.queryForOne(sql, new Object[]{code});
}
```

### 包路径确定性规则

新建 Service 接口和实现类的包路径按以下规则确定：

1. **Maven 模块位置**：新建文件**必须**放在 Service 层模块（`{module}-server-com` 或 `{module}-service`）中，**禁止**放在 Controller 层模块（`{module}-server` 或 `{module}-controller`）中（参见接口设计规范 D-08）
2. 使用与**被替换 DAO 相同的业务域子包**
   - 例如：DAO 在 `config.user.dao` → Service 在 `config.user.service`
   - 例如：DAO 在 `config2.agent.dao` → Service 在 `config2.agent.service`
3. 如果 Controller 跨域注入了多个不同包的 DAO → Service 放在**Controller 自身业务域**的 `service/` 包下
4. **禁止**使用业务场景名（如 bjca、synchronize 等临时前缀）作为 Service 的包路径

### 跨 Controller 独立创建规则（强制，不可跳过）

多个不同 Controller 注入了相同的 DAO 时，**必须为每个 Controller 独立创建 DelegateService**，禁止跨 Controller 复用（参见接口设计规范 D-04-B）。

### 确定性检查清单（修复前必读，不可跳过）

在创建新 Service 之前，**必须逐项确认以下内容并在修复计划中明确列出**，任何一项不明确则暂停并重新分析：

```
□ 1. 被替换的 DAO 类名是什么？（如 UserDao）
□ 2. DAO 的包路径是什么？（如 config.user.dao）
□ 3. 取 DAO 的业务域子包作为 Service 包路径（如 config.user -> config.user.service）
□ 4. 按 D-04 命名规则推导接口名（DAO类名去掉Dao后缀 + DelegateService，如 IUserDelegateService）
□ 5. 推导实现类名（接口名去掉I前缀 + Impl后缀，如 UserDelegateServiceImpl）
□ 6. Controller 中调用了 DAO 的哪些方法？逐一列出 DAO 原始方法签名
□ 7. 每个方法的签名是否与 DAO 原方法完全一致（名称、参数类型、返回类型）？
□ 8. 是否存在改名、拆分、合并、改返回类型等行为？如有则必须回退为原样
```

### 强制原样复制指令（FCC - Force Copy Convention）

创建 Service 接口和实现类时，**必须严格按以下步骤执行**，禁止任何优化或语义化改造：

1. **读取 DAO 源码**：先读取被替换 DAO 类的源码，提取 Controller 实际调用的每个方法的完整签名
2. **原样复制签名**：将方法签名**逐字符原样复制**到 Service 接口中（仅添加 JavaDoc 注释）
3. **纯转发实现**：实现类的方法体写为 `return dao.原方法名(原参数)`（纯转发，无任何附加逻辑）
4. **严禁以下行为**：
   - 不得更改方法名（如 `queryForOne` 改为 `queryUserForOne`）
   - 不得更改参数类型的泛型特化（如 `List` 改为 `List<UserExt>`）
   - 不得更改返回类型（如 `Object` 改为 `Map<String,Object>`，类型转换应在 Controller 中完成）
   - 不得拆分方法（如 `execute()` 拆分为 `executeSql()` + `executeAndReturn()`）
   - 不得合并方法（如多个 DAO 调用合并为一个 Service 方法）
5. **D-01 方法签名冲突处理**：当合并多个 DAO 时，先列出所有 DAO 的方法名，检查是否存在同名方法；若存在冲突，必须按 D-01 规则为冲突方法添加 DAO 来源前缀（如 `userDaoQueryForOne` / `baseDaoQueryForOne`），非冲突方法保持原名

---

## 修复规范三：Controller 注入 ServiceImpl → Service 接口

### S1-03 修复范围确定性规则（强制，消除歧义）

S1-03 的修复范围必须**严格按以下规则确定**，不允许自行判断是否跳过：

**必须修复的情况**：
- Controller 中通过 `@Autowired`/`@Resource` 注入的字段类型为 `XxxServiceImpl`（包名包含 `.impl.`）
- Controller 中 import 了 `xxx.service.impl.XxxServiceImpl`

**必须修复的判定规则**：
1. 扫描 Controller 中所有 `import xxx.service.impl.Xxx` 语句
2. 对每个匹配的 import，检查对应的 `@Autowired` 字段
3. **所有匹配项都必须修复**，不允许选择性跳过
4. 唯一允许跳过的情况：被注入的实现类没有任何 public 方法被 Controller 调用（即字段存在但未使用）

**跳过决策矩阵**：

| 场景 | Controller 是否调用了该字段的方法 | 操作 |
|------|------|------|
| `@Autowired UserService userService` + Controller 调用了 `userService.xxx()` | 是 | **必须修复** |
| `@Autowired ParameterValueService parameterService` + Controller 调用了 `parameterService.xxx()` | 是 | **必须修复** |
| `@Autowired UserTempService userTempService` + Controller 调用了 `userTempService.xxx()` | 是 | **必须修复** |
| `@Autowired XxxServiceImpl xxx` + Controller 未调用任何方法 | 否 | 标记 SKIP（字段未使用） |

### 修复操作

1. 将字段类型从 `XxxServiceImpl` 改为 `IXxxService`（或 `XxxService`）
2. 更新 `import` 语句：从 `import xxx.service.impl.XxxServiceImpl` 改为 `import xxx.service.IXxxService`
3. 如果不存在 Service 接口 → 先提取接口，再修改注入
4. 字段名保持不变（参见接口设计规范 D-09）

> ⚠️ **每完成 3~5 个 S1-03 项修复后，必须立即执行 V-03（接口方法完整性校验）+ V-04（编译验证）。不得等到全部修复完成再编译。**

### 接口方法提取范围规则（强制）

为 ServiceImpl 提取新接口时，接口中应包含的方法范围必须按以下规则确定：

1. **必须先 Grep 全量扫描所有调用者 Controller** —— 当同一个 ServiceImpl 可能被多个 Controller 注入时，必须先全量搜索迎找所有引用了该 Impl 的 Controller
   ```
   Grep pattern: 被注入Impl类名
   Grep path: {controller-module-src-path}
   Grep flags: -l
   ```
2. **仅提取所有 Controller 中实际调用的方法** —— 对每个 Controller 中 Grep 搜索 `{fieldName}.` 获取所有调用的方法名，只将这些方法声明到接口中
3. **禁止提取 Controller 未调用但 ServiceImpl 中存在的方法** —— 即使 ServiceImpl 有 50 个 public 方法，如果 Controller 只调用了其中 10 个，接口中也只声明这 10 个
4. **方法签名与 ServiceImpl 完全一致** —— 返回类型、方法名、参数列表、throws 均照時，不做任何更改
5. **多个 Controller 调用同一 ServiceImpl** → 取所有 Controller 调用方法的**并集**作为接口方法集

此规则确保每次执行的修改范围一致且最小化，对应安全约束 S-10。

---

## 修复规范四：Entity 泄露修复

### 修复操作

1. **返回值泄露**：Controller 方法返回 Entity → 在 Service 层添加 Entity→DTO/VO 转换
2. **参数泄露**：Controller 方法参数为 Entity → 创建对应的 DTO/Query 对象替换

### 注意事项

- 仅在 Entity 泄露明确影响接口安全性时才修复
- 如果项目约定 Entity 直接作为 DTO 使用（如继承自 HashMap），标记为 WARN 但不强制修改

---

## 修复顺序确定性规则（强制，消除歧义）

当一个 Controller 同时存在多种违规时，**必须**按以下固定顺序修复，不允许自行决定顺序：

```
修复优先级（从高到低，必须按此顺序）：

1. S1-01（Controller→Controller）—— 最先修复，因为后续 S1-02/S1-03 修复可能引入新的 Service 注入
2. S1-02（Controller→DAO）—— 第二修复，补建 DelegateService 中间层
3. S1-03（Controller→ServiceImpl）—— 最后修复，替换 Impl 为接口注入
4. S1-04（Entity 泄露）—— 仅标记 WARN，不在自动修复范围内
5. S1-05（跨模块引用）—— 仅标记 WARN，不在自动修复范围内
```

**跨 Controller 修复顺序**：当多个 Controller 都有违规时，按 Controller 类名的**字母表升序**逐个处理（如 `AgentRightController` 先于 `SynchronizeInfoController`）。

**同一 Controller 内同类违规排序**：同一 Controller 内多个同类违规（如注入了多个 ServiceImpl），按**被注入字段在源文件中的声明行号从小到大**排序修复。

---

## 执行操作规范

修复过程中的操作规范：

1. **逐文件处理**：每个违规 Controller 单独处理
2. **先读后改**：使用 Read 读取文件 → 分析依赖 → 使用 Edit 修改
3. **import 同步**：修改依赖后立即更新 import 语句
4. **验证搜索**：修复后使用 Grep 搜索旧的依赖引用，确保无遗漏
5. **标记注释**：所有修改的代码块添加 AI 代码标记

---

## import 语句排序确定性规则（强制）

修改或新建文件中的 import 语句**必须**按以下固定顺序排列，确保多次执行结果一致：

### 新建文件的 import 排序

```
1. java.* （JDK 核心包）—— 按字母表升序
2. javax.* （JDK 扩展包）—— 按字母表升序
3. org.springframework.* （Spring 框架包）—— 按字母表升序
4. 项目内部包（grp.* 或项目自定义顶级包）—— 按字母表升序
5. 其他第三方包 —— 按字母表升序
```

每组之间用**一个空行**分隔，组内**无空行**。

### 修改已有文件的 import 处理

1. **删除旧 import**：直接删除被替换类型的 import 行
2. **添加新 import**：在**同组 import 的字母表正确位置**插入新的 import 行
3. **保持现有顺序**：不得因为修复而重新排列其他未修改的 import 语句
4. **如果原文件 import 本身无序**：仅在删除和添加的范围内按上述规则排列，不重新排列全部 import

### import 排序示例

**正确**（新建 DelegateService 接口文件）：
```java
package grp.pt.frame.config.user.service;

import java.util.List;
import java.util.Map;

import grp.pt.frame.config.user.dao.UserDao;
```

**错误**（组间无空行分隔）：
```java
import java.util.List;
import grp.pt.frame.config.user.dao.UserDao;
import java.util.Map;
```

---

## 方法调用对象替换全场景规则（强制）

替换注入字段类型和字段名后，**必须**扫描 Controller 中**所有引用该字段的场景**并逐一替换，不得遗漏：

### 必须替换的场景

| 场景 | 原代码 | 替换后 |
|------|--------|--------|
| 普通方法调用 | `oldField.method(args)` | `newField.method(args)` |
| 链式调用 | `oldField.methodA().methodB()` | `newField.methodA().methodB()` |
| 赋值语句中 | `var result = oldField.method(args)` | `var result = newField.method(args)` |
| if/while 条件中 | `if (oldField.method())` | `if (newField.method())` |
| return 语句中 | `return oldField.method()` | `return newField.method()` |
| 方法参数中 | `foo(oldField.method())` | `foo(newField.method())` |
| 三元表达式中 | `a ? oldField.x() : b` | `a ? newField.x() : b` |
| for 循环中 | `for (X x : oldField.list())` | `for (X x : newField.list())` |
| try-catch 中 | `try { oldField.exec() }` | `try { newField.exec() }` |

### 替换验证

替换完成后，**必须**执行以下 Grep 搜索确认无遗漏：

```
Grep pattern: {oldFieldName}\.
Grep path: {当前Controller文件路径}
Grep output_mode: content
```

**通过标准**：搜索结果为 0 条（即旧字段名不再出现在任何方法调用中）。如果仍有残留，必须逐行修复。

---

## AI 标记一致性规范

### Tag 粒度规则

| 修复阶段 | Tag 粒度 | 说明 |
|----------|---------|------|
| Phase 5-A（S1-01 Controller→Controller） | 每个 FAIL 项一个 Tag | 同一 FAIL 涉及的所有文件共享同一 Tag |
| Phase 5-B（S1-02 Controller→DAO） | 每个 FAIL 项一个 Tag | 新建 Interface + Impl + Controller 修改共享 Tag |
| Phase 5-C（S1-03 ServiceImpl→Interface） | 全阶段一个 Tag | 同类批量替换使用统一 Tag |

**重要：不同 FAIL 项之间禁止复用同一 Tag 码**。每个 FAIL 项必须生成一个新的 5 位随机 Tag 码。Phase 5-C 所有 S1-03 修复共用同一个 Tag 码。

### 标记范围规则

1. **新建文件**：整个文件内容（package 声明后、第一条 import 前开始，文件末尾结束）包裹在一对 AI 标记中
   - **标记必须紧接在 package 声明之后、import 语句之前**（不得放在 import 之后、class 声明之前）
2. **修改已有文件**：仅修改的代码段落包裹在 AI 标记中，不修改的代码不添加标记
3. **import 语句修改**：将所有**连续修改的 import 行**合并为**一个**标记块（不逐行标记）
4. **字段注入修改**：将所有**连续修改的 `@Autowired` 字段**合并为**一个**标记块
5. **方法调用修改**：当方法调用处的**调用对象名**发生变化时（如 `oldService.xxx()` → `newService.xxx()`），该方法调用行**必须**包含在 AI 标记范围内
6. **连续修改合并**：同一方法体内的**连续修改行**合并为一个标记块
7. **非连续修改**：分散在不同位置的修改使用**独立标记块**，但共享同一个 Tag 码（同一 FAIL 项内）

### 标记位置示例

**正确 — 新建 Service 接口文件**（标记在 package 后、第一个 import 前）：
```java
package com.xxx.service;

// @AI-Begin Rv7mK 20260325 @@Qoder
import java.util.List;

/**
 * Xxx 服务接口
 */
public interface IXxxService {
    void doSomething();
}
// @AI-End Rv7mK 20260325 @@Qoder
```

**错误 — 新建文件中 标记放在 import 后、class 前**：
```java
package com.xxx.service;

import java.util.List;

// @AI-Begin Rv7mK 20260325 @@Qoder  ← 错误！标记应在 package 后第一行，不是 import 后
public interface IXxxService {
    void doSomething();
}
// @AI-End Rv7mK 20260325 @@Qoder
```

**正确 — 修改已有 Controller 的 import 和字段**：
```java
// @AI-Begin Rv7mK 20260325 @@Qoder
import com.xxx.service.IXxxService;
// @AI-End Rv7mK 20260325 @@Qoder

// ... 未修改的代码 ...

    // @AI-Begin Rv7mK 20260325 @@Qoder
    @Autowired
    private IXxxService xxxService;
    // @AI-End Rv7mK 20260325 @@Qoder
```

**错误 — 将整个文件包裹在 AI 标记中（已有文件仅修改少量内容时）**：
```java
// @AI-Begin Rv7mK 20260325 @@Qoder  ← 错误！不应包裹未修改代码
package com.xxx.controller;
// ... 整个文件 ...
// @AI-End Rv7mK 20260325 @@Qoder
```

---

## 接口设计规范引用

新建 Service 接口时，**必须**遵循接口设计规范 → [scripts/interface-design-rules.md](interface-design-rules.md)

核心要求：
- 方法签名必须与 ServiceImpl 中的 public 方法**完全一致**（返回类型、方法名、参数列表、throws）
- 禁止在接口中添加 ServiceImpl 中不存在的方法
- 禁止将 DAO/Mapper 层的 SQL 调用逻辑移入接口签名
- 禁止合并或拆分 ServiceImpl 中的方法
- 每个接口方法必须添加 JavaDoc 注释（按 D-05 标准化格式）
- 如果 ServiceImpl 已实现某个现有接口，优先在该接口上追加方法（仅适用于 S1-03，参见 D-07）
- 新建文件必须放在 Service 层模块（参见 D-08）
- 替换后字段名必须按确定性规则命名（参见 D-09）
