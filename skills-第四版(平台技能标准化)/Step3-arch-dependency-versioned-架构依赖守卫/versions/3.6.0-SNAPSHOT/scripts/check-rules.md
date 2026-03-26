# S1 检查规则清单

## S1-01：Controller→Controller 直接依赖

**违规模式**：Controller 类通过 `@Autowired` 或 `@Resource` 注入了另一个 Controller。

**检查方法**：
- 在 Controller 类中搜索 `@Autowired` 或 `@Resource` 注解修饰的字段
- 检查被注入类型是否为另一个 `@RestController` 或 `@Controller` 类
- 检查 Controller 方法体中是否直接调用了其他 Controller 的方法

**判定标准**：
- Controller 类中存在 `import xxx.controller.XxxController` → **FAIL**
- Controller 类中存在 `@Autowired XxxController` → **FAIL**
- Controller 方法中调用 `xxxController.someMethod()` → **FAIL**

**风险说明**：Controller 之间直接依赖会导致同层耦合、循环依赖风险，违反"同层不依赖"原则。

---

## S1-02：Controller 直接依赖 DAO/Mapper

**违规模式**：Controller 类直接注入了 DAO 或 Mapper 接口，跳过了 Service 层。

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

**检查方法**：
- 在 Controller 类中搜索 `@Autowired` 字段的类型
- 检查是否直接注入了 `XxxServiceImpl` 而非 `XxxService`（接口）

**判定标准**：
- Controller 类中存在 `@Autowired XxxServiceImpl` → **FAIL**
- Controller 类中存在 `import xxx.service.impl.XxxServiceImpl` → **FAIL**

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
