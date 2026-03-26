# S1 检查规则清单

## 违规编号判定确定性规则（强制）

同一个违规在多次执行中**必须**被判定为相同的 S1 编号。判定优先级规则如下：

1. 先检查被注入类的**类型特征**，不是调用场景
2. 匹配规则按以下**严格优先级**顺序判定，命中第一个即停止：

```
判定优先级（从高到低）：

1. 被注入类名以 "Controller" 结尾 + 被注入类有 @RestController/@Controller 注解
   → S1-01（Controller→Controller 依赖）

2. 被注入类名以 "Dao"/"DAO"/"Mapper" 结尾 + 或有 @Repository/@Mapper 注解
   → S1-02（Controller→DAO 依赖）

3. 被注入类名以 "ServiceImpl"/"Service" 结尾 + 包路径包含 ".impl." + 类上有 @Service 注解
   → S1-03（注入实现类而非接口）

4. 被注入类名以 "Service" 结尾 + 包路径包含 ".service.impl."
   → S1-03（注入实现类而非接口）

5. 被注入类名以 "Service" 结尾 + 包路径不包含 ".impl." + 是具体类而非接口
   → S1-03（注入实现类而非接口）
```

**重要：同一个违规不得根据上下文场景判定为不同编号**。例如 Controller 注入了 `UserLogController`，则必须判定为 S1-01（因为类名以 Controller 结尾），不得因为该类用 @Service 注解就判定为 S1-03。

---

## S1-01：Controller→Controller 直接依赖

**违规模式**：Controller 类通过 `@Autowired` 或 `@Resource` 注入了另一个 Controller。

**强制全量扫描指令（不可跳过，必须先于人工分析执行）**：

在开始逐文件分析之前，**必须**先执行以下两条 Grep 全量扫描，将扫描结果完整列出：

```
# 扫描1：全 controller 模块目录下所有 import 了 .controller. 包路径的文件
Grep pattern: import.*\.controller\.[A-Z][a-zA-Z]*Controller
Grep path: {controller-module-src-path}
Grep flags: -l（仅列出文件名）

# 扫描2：全 controller 模块目录下所有注入了 XxxController 类型字段的文件  
Grep pattern: @Autowired[\s\S]{0,50}[A-Z][a-zA-Z]*Controller
Grep path: {controller-module-src-path}
Grep flags: -l
```

**扫描结果必须全部列入检查报告**，不得遗漏任何一个文件。

**检查方法**：
- 在 Controller 类中搜索 `@Autowired` 或 `@Resource` 注解修饰的字段
- 检查被注入类型是否为另一个 `@RestController` 或 `@Controller` 类
- 检查 Controller 方法体中是否直接调用了其他 Controller 的方法
- **特别注意**：被注入类即使标注了 `@Service` 注解，只要类名以 `Controller` 结尾，仍判定为 S1-01

**判定标准**：
- Controller 类中存在 `import xxx.controller.XxxController` → **FAIL**
- Controller 类中存在 `@Autowired XxxController` → **FAIL**
- Controller 方法中调用 `xxxController.someMethod()` → **FAIL**

**风险说明**：Controller 之间直接依赖会导致同层耦合、循环依赖风险，违反"同层不依赖"原则。

---

## S1-02：Controller 直接依赖 DAO/Mapper

**违规模式**：Controller 类直接注入了 DAO 或 Mapper 接口，跳过了 Service 层。

**强制全量扫描指令（不可跳过，必须先于单文件分析执行）**：

在开始逐文件分析之前，**必须**先执行以下 Grep 全量扫描，将扫描结果完整列出：

```
# 扫描1：全 controller 模块目录下所有 import 了 .dao. 包路径的文件
Grep pattern: import.*\.dao\.[A-Z]
Grep path: {controller-module-src-path}
Grep flags: -l

# 扫描2：全 controller 模块目录下所有 import 了 .mapper. 包路径的文件
Grep pattern: import.*\.mapper\.[A-Z]
Grep path: {controller-module-src-path}
Grep flags: -l
```

**扫描结果必须全部列入检查报告**，不得遗漏任何一个文件。

**检查方法**：
- 在 Controller 类中搜索注入的类型
- 检查被注入类型是否为 `@Repository` 类、`@Mapper` 接口、或命名以 `Dao`/`Mapper` 结尾的类

**判定标准**：
- Controller 类中存在 `@Autowired XxxDao xxxDao` → **FAIL**
- Controller 类中存在 `@Autowired XxxMapper xxxMapper` → **FAIL**
- Controller 类中存在 `import xxx.dao.XxxDao` 或 `import xxx.mapper.XxxMapper` → **FAIL**

**风险说明**：跳过 Service 层会导致业务逻辑散落在 Controller 中，无法复用、无法统一事务管理。

---

## S1-03：Controller 注入 ServiceImpl 而非 Service 接口

**违规模式**：Controller 注入的是 Service 的实现类而非接口。

**强制全量扫描指令（不可跳过，必须先于单文件分析执行）**：

在开始逐文件分析之前，**必须**先执行以下 Grep 全量扫描，将扫描结果完整列出：

```
# 扫描1：全 controller 模块目录下所有 import 了 .service.impl. 包路径的文件
Grep pattern: import.*\.service\.impl\.
Grep path: {controller-module-src-path}
Grep flags: -l
```

**扫描结果必须全部列入检查报告**，不得遗漏任何一个文件。

**检查方法**：
- 在 Controller 类中搜索 `@Autowired` 字段的类型
- 检查是否直接注入了 `XxxServiceImpl` 而非 `XxxService`（接口）
- 检查是否注入了包路径包含 `.service.impl.` 的具体类
- 检查是否注入了类名以 `Service` 结尾但是具体类（而非接口）的类型

**判定标准**：
- Controller 类中存在 `@Autowired XxxServiceImpl` → **FAIL**
- Controller 类中存在 `import xxx.service.impl.XxxServiceImpl` → **FAIL**
- Controller 类中存在 `@Autowired XxxService xxxService`（其中 `XxxService` 是具体类而非接口） → **FAIL**
- Controller 类中存在 `import xxx.service.impl.XxxService` → **FAIL**

**重要说明（确定性规则）**：
- 包名包含 `.service.impl.` 的类型，无论类名是否以 `Impl` 结尾，都判定为 S1-03
- 类名以 `Service` 结尾但是具体类（非 interface）的，判定为 S1-03
- S1-03 的修复范围必须覆盖所有匹配项，不允许选择性跳过（参见修复规范三的 S1-03 修复范围确定性规则）

**风险说明**：直接依赖实现类违反了面向接口编程原则，无法通过替换实现类来扩展功能。

---

## S1-04：Entity 泄露到 Controller 层

**违规模式**：Controller 方法的返回值或参数中直接使用了持久化 Entity 对象。

**检查方法**：
- 检查 Controller 方法签名中的参数类型和返回类型
- 判断是否直接使用了 Entity 类（位于 `dao/entity/` 或 `model/entity/` 包下的类）

**判定标准**：
- Controller 方法返回类型为 Entity 类 → **WARN**（应使用 DTO/VO）
- Controller 方法参数类型为 Entity 类 → **WARN**（应使用 DTO/Query）

**风险说明**：Entity 泄露会暴露数据库表结构细节，存在安全风险，且修改数据库时会直接影响接口契约。

---

## S1-05：跨模块直接类引用

**违规模式**：一个模块的 Controller 直接 import 了另一个模块的内部类（非公共 API/Feign 接口）。

**检查方法**：
- 检查 Controller 的 import 列表
- 判断是否存在跨模块（不同 Maven module）的直接类引用
- 排除公共模块（common/api）和 Feign 接口

**判定标准**：
- 跨模块引用非 Feign/API 接口的类 → **WARN**

**风险说明**：跨模块直接引用内部类会导致模块间强耦合，破坏微服务的独立部署能力。
