---
name: test-case
description: 为 Spring Boot + JDBCTemplate 项目自动生成系统测试代码、预制模拟数据和测试用例。当用户要求生成单元测试、集成测试、测试用例、Mock数据、测试覆盖时使用此技能。分析源代码后生成可直接编译运行的测试代码，确保每个功能都有对应的测试用例。
---

本技能指导为 Spring Boot + JDBCTemplate 微服务项目生成完整的、可编译可执行的测试代码。产出四大核心交付物：**测试数据工厂类**、**Controller 层集成测试**。

用户提供模块或类路径，技能分析源代码后生成结构化测试代码和模拟数据。


## 工作流程

### 第一阶段：代码分析（必须完成）

生成测试代码前，**必须**深入探索目标模块，阅读实际源代码：

1. **项目结构**：识别所有子模块、pom.xml 依赖配置，确认当前测试依赖是否已引入
2. **Controller 层**：阅读所有 `@RestController` 类，记录：
   - 类级别 `@RequestMapping` 路径
   - 每个方法的 HTTP 方法（GET/POST/PUT/DELETE）、路径、参数注解（@RequestParam、@PathVariable、@RequestBody）
   - 返回类型（ReturnData<T>、ReturnPageData<T> 等）
   - 依赖注入的 Service
3. **数据模型**：阅读所有实体类和 DTO，记录：
   - 所有字段名、类型、默认值
   - 验证注解（@NotNull、@Size 等）
   - 枚举字段的可选值
   - 日期格式（@JsonFormat）
   - 嵌套对象和集合字段
4. **统一返回类型**：阅读 `ReturnData<T>`、`ReturnPageData<T>` 等包装类，确认：
   - 成功/失败状态码常量（如 SUCCESS_CODE = "200"、FAIL_CODE = "500"）
   - 静态工厂方法签名（ok、fail）
   - 字段名称（statusCode、reason、data 等）
5. **Feign 接口**：阅读 `@FeignClient` 接口和 FallbackFactory，确认降级逻辑
6. **配置文件**：阅读 application.yml/properties，了解数据源、端口等配置

**关键要求**：
- **必须阅读实际源代码**，禁止猜测或假设方法签名、字段名、路径
- 每个测试方法中的断言和模拟数据必须与实际代码一一对应
- 如果源代码中不存在的方法，不得在测试中出现

### 第二阶段：检查和补充测试依赖

分析 pom.xml，如果缺少以下测试依赖，**先生成依赖补充建议**：


如果依赖已存在则跳过此步骤。

### 第三阶段：生成测试代码

按以下顺序生成测试文件，输出到对应模块的 `src/test/java/` 目录下，**包路径与源代码保持一致**：


## 测试代码生成规范

### 一、测试数据工厂类（TestDataFactory）

**目的**：集中管理模拟数据，确保测试数据可复用、贴近真实业务。

**命名规范**：`{功能名}TestDataFactory`，放在 `grp.pt.testdata` 包下。

**生成规则**：

1. 为每个实体/DTO 提供静态工厂方法，方法名使用 `create{EntityName}` 格式
2. 提供带参数的变体方法，支持自定义关键字段
3. 提供批量创建方法 `create{EntityName}List(int count)`
4. 所有字段值必须使用**贴近业务的中文示例数据**，禁止使用 "test"、"xxx"、"string" 等占位符
5. 日期字段使用固定值（避免测试因时间变化而失败）
6. ID 字段使用可预测的值（如 UUID 的固定前缀 + 序号）


**数据工厂核心原则**：
- 每个实体至少提供：`createDefault{Entity}()`、`create{Entity}(关键参数...)`、`create{Entity}List(int count)`、`create{Entity}ForInsert()`、`create{Entity}ForUpdate(String id)`
- 中文业务数据示例：编码用 "101001"、名称用 "一般公共预算"、机构名用 "财政局"、区划用 "370000"
- 枚举字段注释所有可选值（如 `eleType: 1-枚举，2-基础数据，3-应用数据`）
- 日期使用 `parseDate("2025-01-01 00:00:00")` 固定值

---



### 二、Controller 层集成测试

**目的**：测试 HTTP 接口的请求/响应，验证参数绑定、序列化、状态码。

**命名规范**：`{Controller}Test`，如 `ElementControllerTest`。

**生成规则**：
1. **每个 Controller 端点必须至少有以下测试用例**：
   - 正常请求返回 200
   - 参数缺失返回 400（如果有必填参数）
   - 验证返回 JSON 结构（statusCode、reason、data）
   - POST/PUT 的 RequestBody 验证
2. **方法命名规范**：`test_{端点方法名}_{HTTP方法}_{场景}`
3. **MockMvc 请求构建必须精确匹配实际端点**：
   - GET 请求用 `.param("key", "value")` 或路径变量
   - POST 请求用 `.content(json).contentType(MediaType.APPLICATION_JSON)`
   - 验证返回字段用 `jsonPath("$.statusCode")` 等


## 测试质量标准

### 断言规范

1. **禁止空断言**：每个测试方法至少 2 个有效断言
2. **断言优先级**：
   - 首先断言返回值非 null（assertNotNull）
   - 然后断言核心业务字段（assertEquals）
   - 最后断言 Mock 调用次数（verify）
3. **集合断言**：验证 size、首元素字段值
4. **异常断言**：使用 `assertThrows` 验证预期异常

### 覆盖率要求

| 层级 | 方法覆盖率目标 | 分支覆盖率目标 |
|------|-------------|-------------|
| Controller 层 | 100% | ≥ 70% |

### 命名和注释规范

- 测试类使用 `@DisplayName` 标注中文描述
- 测试方法使用 `@DisplayName` 标注测试场景
- 方法名使用 `test_{方法名}_{场景}` 格式（英文下划线分隔）
- 每组相关测试用注释分隔块标注（如 `// ==================== getList 测试 ====================`）

### Mock 数据规范

- **所有测试数据必须通过 TestDataFactory 获取**，禁止在测试方法中直接 new 实体并逐个 set
- 中文业务数据：名称用 "一般公共预算"、编码用 "101001"、机构名用 "财政局"、区划用 "370000"
- 禁止使用 "test"、"xxx"、"string"、"123" 等无意义占位符
- 日期使用固定值，避免 `new Date()` 导致测试不稳定

---

## 执行检查清单

生成测试代码后，必须逐项检查：

- [ ] 所有 import 语句正确，无缺失依赖
- [ ] 测试类包路径与源代码一致
- [ ] Mock 注解正确（@Mock vs @MockBean 区分 Mockito 和 Spring 上下文）
- [ ] 每个 Service 方法至少有 2 个测试用例（正常 + 异常/边界）
- [ ] 每个 Controller 端点至少有 1 个完整的 MockMvc 测试
- [ ] TestDataFactory 中的字段与实际实体类字段完全匹配
- [ ] ReturnData 的断言使用正确的字段名（statusCode 而非 status_code，根据实际 JSON 序列化名）
- [ ] HTTP 方法和路径与 Controller 注解完全一致
- [ ] @RequestBody 测试使用 ObjectMapper 序列化
- [ ] @RequestParam 测试使用 .param() 传参
- [ ] @PathVariable 测试使用路径变量模板
- [ ] 测试可通过 `mvn test` 或 IDE 直接运行

---

## 参考实现

### 分层架构
- **Controller** → 调用 Service → 包装为 `ReturnData<T>` 返回
- **Service** → 调用 Mapper → 处理业务逻辑
- **Mapper** → JDBCTemplate → 数据库操作

### 接口模式
- **外部接口**：`/config/{功能}/{操作}`（面向前端）
- **内部接口**：`/{功能}Feign/{操作}`（面向微服务）
- **标准 CRUD**：getList / getById / getByCode / add / update / delete

### 返回类型
- **ReturnData<T>**：statusCode（"200"/"500"）、reason、data
- **ReturnPageData<T>**：额外包含 total、page、pageSize、runtime
- 静态工厂：`ReturnData.ok(data)`、`ReturnData.fail(message)`

### 事务管理
- 查询方法：无事务注解
- 写操作方法：`@Transactional(rollbackFor = Exception.class)`

分析其他模块时，识别类似模式并据此调整测试结构。

---

## 输出语言

- 测试类的 `@DisplayName` 使用**中文**
- 代码注释使用**中文**
- 方法名、变量名、类名保持**英文**
- 模拟数据值使用**中文业务数据**

