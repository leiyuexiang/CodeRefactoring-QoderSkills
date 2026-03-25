---
name: test-case
description: 为 Spring Boot + MyBatis 项目自动生成系统测试代码、预制模拟数据和测试用例。当用户要求生成单元测试、集成测试、测试用例、Mock数据、测试覆盖时使用此技能。分析源代码后生成可直接编译运行的测试代码，确保每个功能都有对应的测试用例。
---

本技能指导为 Spring Boot + MyBatis 微服务项目生成完整的、可编译可执行的测试代码。产出四大核心交付物：**测试数据工厂类**、**Service 层单元测试**、**Controller 层集成测试**、**Mapper 层数据访问测试**。

用户提供模块或类路径，技能分析源代码后生成结构化测试代码和模拟数据。

---

## 适用技术栈

| 类别 | 技术 |
|------|------|
| 基础框架 | Spring Boot 2.x |
| ORM框架 | MyBatis（注解模式 + XML映射） |
| 微服务 | Spring Cloud（Feign + Hystrix） |
| 测试框架 | JUnit 5（Jupiter）+ Mockito + MockMvc |
| 构建工具 | Maven |
| Java版本 | 1.8+ |

---

## 工作流程

### 第一阶段：代码分析（必须完成）

生成测试代码前，**必须**深入探索目标模块，阅读实际源代码：

1. **项目结构**：识别所有子模块、pom.xml 依赖配置，确认当前测试依赖是否已引入
2. **Controller 层**：阅读所有 `@RestController` 类，记录：
   - 类级别 `@RequestMapping` 路径
   - 每个方法的 HTTP 方法（GET/POST/PUT/DELETE）、路径、参数注解（@RequestParam、@PathVariable、@RequestBody）
   - 返回类型（ReturnData<T>、ReturnPageData<T> 等）
   - 依赖注入的 Service
3. **Service 层**：阅读所有 Service 接口（`I*Service`）和实现类（`*ServiceImpl`），记录：
   - 所有公开方法签名
   - `@Transactional` 注解及其参数
   - 依赖注入的 Mapper 和其他 Service
   - 业务逻辑中的条件分支、异常处理、边界情况
4. **Mapper 层**：阅读所有 `@Mapper` 接口，记录：
   - 所有方法签名和 `@Param` 参数
   - SQL 注解（如 `@Select`、`@Insert`）或关联的 XML 映射文件
5. **数据模型**：阅读所有实体类和 DTO，记录：
   - 所有字段名、类型、默认值
   - 验证注解（@NotNull、@Size 等）
   - 枚举字段的可选值
   - 日期格式（@JsonFormat）
   - 嵌套对象和集合字段
6. **统一返回类型**：阅读 `ReturnData<T>`、`ReturnPageData<T>` 等包装类，确认：
   - 成功/失败状态码常量（如 SUCCESS_CODE = "200"、FAIL_CODE = "500"）
   - 静态工厂方法签名（ok、fail）
   - 字段名称（statusCode、reason、data 等）
7. **Feign 接口**：阅读 `@FeignClient` 接口和 FallbackFactory，确认降级逻辑
8. **配置文件**：阅读 application.yml/properties，了解数据源、端口等配置

**关键要求**：
- **必须阅读实际源代码**，禁止猜测或假设方法签名、字段名、路径
- 每个测试方法中的断言和模拟数据必须与实际代码一一对应
- 如果源代码中不存在的方法，不得在测试中出现

### 第二阶段：检查和补充测试依赖

分析 pom.xml，如果缺少以下测试依赖，**先生成依赖补充建议**：

```xml
<!-- 在 element-server-com/pom.xml 或对应模块的 pom.xml 中添加 -->
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Spring Boot Test（包含 MockMvc、TestRestTemplate） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>org.junit.vintage</groupId>
                <artifactId>junit-vintage-engine</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

    <!-- Mockito（spring-boot-starter-test 已包含，如需单独使用） -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-inline</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- MyBatis Test（可选，用于 Mapper 层测试） -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter-test</artifactId>
        <version>2.1.4</version>
        <scope>test</scope>
    </dependency>

    <!-- H2 内嵌数据库（Mapper 集成测试用） -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

如果依赖已存在则跳过此步骤。

### 第三阶段：生成测试代码

按以下顺序生成测试文件，输出到对应模块的 `src/test/java/` 目录下，**包路径与源代码保持一致**：

```
src/test/java/
└── grp/pt/
    ├── testdata/                           # 测试数据工厂（全局共享）
    │   └── {功能名}TestDataFactory.java
    ├── {功能名}/
    │   ├── service/
    │   │   └── impl/
    │   │       └── {功能名}ServiceImplTest.java
    │   ├── controller/
    │   │   ├── custom/
    │   │   │   └── {功能名}ControllerTest.java
    │   │   └── common/
    │   │       └── {功能名}FeignControllerTest.java
    │   └── dao/
    │       └── mapper/
    │           └── {功能名}MapperTest.java
    └── resources/
        └── application-test.yml            # 测试专用配置
```

---

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

**模板示例**（以 Element 为参考）：

```java
package grp.pt.testdata;

import grp.pt.element.model.dto.Element;
import grp.pt.element.model.dto.ElementValueDTO;
import java.util.*;

/**
 * Element 测试数据工厂
 * 提供可复用的模拟数据构建方法
 */
public final class ElementTestDataFactory {

    private ElementTestDataFactory() {
        // 工具类禁止实例化
    }

    /**
     * 创建标准的 Element 实例（基础数据类型）
     */
    public static Element createDefaultElement() {
        Element element = new Element();
        element.setId("ele-test-001");
        element.setCode("FUND_TYPE");
        element.setName("资金性质");
        element.setMaxLevel(3);
        element.setVdCode("");
        element.setLoadRule("3-2-2");
        element.setCodeRule("3-3-3");
        element.setCodeRuleRemark("一级3位-二级3位-三级3位");
        element.setEleSource("sys_element_fund_type");
        element.setClassName("grp.pt.element.model.FundType");
        element.setEleType("2"); // 基础数据
        element.setEleCateId(100L);
        element.setLeftTreeId(0L);
        element.setEleExtendType(1);
        element.setAcctExtendType(1);
        element.setViewType(1);
        element.setIsCache(1);
        element.setIsPreload(1);
        element.setIsDeleted(2); // 未删除
        element.setIsState(1);   // 启用
        element.setIsEnabled(1);
        element.setIsStandard(1);
        element.setIsAccElement(2);
        element.setByYear("1");
        element.setDispOrder(1);
        element.setRemark("资金性质代码集-测试数据");
        element.setMofDivCode("370000");
        element.setEleManageType("1");
        element.setCreateTime(parseDate("2025-01-01 00:00:00"));
        element.setUpdateTime(parseDate("2025-06-15 10:30:00"));
        element.setUpdateUser("admin");
        // 值集列表
        element.setElementValueList(createElementValueList(3));
        element.setElementValueTemplateList(new ArrayList<>());
        element.setElementValueTenantList(new ArrayList<>());
        return element;
    }

    /**
     * 创建自定义编码和名称的 Element
     */
    public static Element createElement(String id, String code, String name) {
        Element element = createDefaultElement();
        element.setId(id);
        element.setCode(code);
        element.setName(name);
        return element;
    }

    /**
     * 批量创建 Element 列表
     */
    public static List<Element> createElementList(int count) {
        List<Element> list = new ArrayList<>();
        String[] codes = {"FUND_TYPE", "BUDGET_LEVEL", "FUNC_CODE", "ECO_CODE", "ORG_CODE"};
        String[] names = {"资金性质", "预算级次", "功能分类", "经济分类", "单位编码"};
        for (int i = 0; i < count; i++) {
            int idx = i % codes.length;
            list.add(createElement(
                "ele-test-" + String.format("%03d", i + 1),
                codes[idx] + "_" + (i + 1),
                names[idx] + (i + 1)
            ));
        }
        return list;
    }

    /**
     * 创建 ElementValueDTO 列表
     */
    public static List<ElementValueDTO> createElementValueList(int count) {
        List<ElementValueDTO> list = new ArrayList<>();
        String[][] values = {
            {"val-001", "101", "一般公共预算", null, null, "1"},
            {"val-002", "102", "政府性基金预算", null, null, "1"},
            {"val-003", "10101", "税收收入", "val-001", "101", "2"}
        };
        for (int i = 0; i < Math.min(count, values.length); i++) {
            ElementValueDTO dto = new ElementValueDTO();
            dto.setId(values[i][0]);
            dto.setCode(values[i][1]);
            dto.setName(values[i][2]);
            dto.setParentId(values[i][3]);
            dto.setParentCode(values[i][4]);
            dto.setLevel(Integer.parseInt(values[i][5]));
            dto.setIsLeaf(i == values.length - 1 ? 1 : 2);
            dto.setIsEnabled(1);
            dto.setDispOrder(i + 1);
            dto.setTenantId(1L);
            dto.setYear(2025L);
            list.add(dto);
        }
        return list;
    }

    /**
     * 创建用于新增操作的 Element（无ID）
     */
    public static Element createElementForInsert() {
        Element element = createDefaultElement();
        element.setId(null);
        element.setCreateTime(null);
        element.setUpdateTime(null);
        return element;
    }

    /**
     * 创建用于更新操作的 Element
     */
    public static Element createElementForUpdate(String id) {
        Element element = createDefaultElement();
        element.setId(id);
        element.setName("资金性质（已修改）");
        element.setRemark("更新测试数据");
        return element;
    }

    private static Date parseDate(String dateStr) {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
        } catch (Exception e) {
            return new Date();
        }
    }
}
```

**数据工厂核心原则**：
- 每个实体至少提供：`createDefault{Entity}()`、`create{Entity}(关键参数...)`、`create{Entity}List(int count)`、`create{Entity}ForInsert()`、`create{Entity}ForUpdate(String id)`
- 中文业务数据示例：编码用 "101001"、名称用 "一般公共预算"、机构名用 "财政局"、区划用 "370000"
- 枚举字段注释所有可选值（如 `eleType: 1-枚举，2-基础数据，3-应用数据`）
- 日期使用 `parseDate("2025-01-01 00:00:00")` 固定值

---

### 二、Service 层单元测试

**目的**：测试业务逻辑，Mock 掉 Mapper 层依赖。

**命名规范**：`{ServiceImpl}Test`，如 `ElementServiceImplTest`。

**注解和框架**：
```java
@ExtendWith(MockitoExtension.class)  // JUnit 5 + Mockito
```

**生成规则**：

1. **每个 Service 公开方法必须至少有以下测试用例**：
   - 正常流程测试（happy path）
   - 空值/null 参数测试
   - 返回空结果测试
   - 异常抛出测试（如果方法有异常分支）

2. **方法命名规范**：`test_{方法名}_{场景描述}`，如：
   - `test_getList_returnsAllElements`
   - `test_getById_withValidId_returnsElement`
   - `test_getById_withNullId_returnsNull`
   - `test_add_withValidElement_insertsAndReturns`
   - `test_delete_withValidId_returnsTrue`
   - `test_delete_withNonExistentId_returnsFalse`

3. **必须使用 AAA 模式**（Arrange-Act-Assert）：
   ```java
   @Test
   @DisplayName("查询所有代码集 - 正常返回列表")
   void test_getList_returnsAllElements() {
       // Arrange - 准备数据和Mock
       List<Element> mockList = ElementTestDataFactory.createElementList(3);
       when(elementMapper.selectList()).thenReturn(mockList);

       // Act - 执行被测方法
       List<Element> result = elementService.getList();

       // Assert - 验证结果
       assertNotNull(result);
       assertEquals(3, result.size());
       assertEquals("FUND_TYPE_1", result.get(0).getCode());
       verify(elementMapper, times(1)).selectList();
   }
   ```

4. **Mock 注入模式**：
   ```java
   @Mock
   private ElementMapper elementMapper;  // Mock 数据层

   @InjectMocks
   private ElementServiceImpl elementService;  // 被测对象
   ```

5. **测试用例完整覆盖表**（以 Service 方法为基准）：

| Service 方法 | 测试场景 | 测试方法名 |
|-------------|---------|-----------|
| getList() | 正常返回列表 | test_getList_returnsAllElements |
| getList() | 返回空列表 | test_getList_returnsEmptyList |
| getById(id) | 有效ID返回 | test_getById_withValidId_returnsElement |
| getById(id) | 无效ID返回null | test_getById_withInvalidId_returnsNull |
| getByCode(code) | 有效编码返回 | test_getByCode_withValidCode_returnsElement |
| getByCode(code) | 不存在的编码 | test_getByCode_withNonExistentCode_returnsNull |
| add(element) | 正常新增 | test_add_withValidElement_insertsAndReturns |
| update(element) | 正常更新成功 | test_update_withValidElement_returnsTrue |
| update(element) | 更新不存在的记录 | test_update_withNonExistentId_returnsFalse |
| delete(id) | 正常删除成功 | test_delete_withValidId_returnsTrue |
| delete(id) | 删除不存在的记录 | test_delete_withNonExistentId_returnsFalse |
| addBatch(list) | 批量新增 | test_addBatch_withValidList_returnsResults |
| addBatch(list) | 空列表输入 | test_addBatch_withEmptyList_returnsEmpty |

**模板示例**：

```java
package grp.pt.element.service.impl;

import grp.pt.element.dao.mapper.ElementMapper;
import grp.pt.element.model.dto.Element;
import grp.pt.testdata.ElementTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElementServiceImpl 单元测试")
class ElementServiceImplTest {

    @Mock
    private ElementMapper elementMapper;

    @InjectMocks
    private ElementServiceImpl elementService;

    // ==================== getList 测试 ====================

    @Test
    @DisplayName("查询所有代码集 - 正常返回列表")
    void test_getList_returnsAllElements() {
        // Arrange
        List<Element> mockList = ElementTestDataFactory.createElementList(3);
        when(elementMapper.selectList()).thenReturn(mockList);

        // Act
        List<Element> result = elementService.getList();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(elementMapper, times(1)).selectList();
    }

    @Test
    @DisplayName("查询所有代码集 - 返回空列表")
    void test_getList_returnsEmptyList() {
        when(elementMapper.selectList()).thenReturn(Collections.emptyList());

        List<Element> result = elementService.getList();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(elementMapper, times(1)).selectList();
    }

    // ==================== getById 测试 ====================

    @Test
    @DisplayName("根据ID查询代码集 - 存在的ID")
    void test_getById_withValidId_returnsElement() {
        Element mockElement = ElementTestDataFactory.createDefaultElement();
        when(elementMapper.selectById("ele-test-001")).thenReturn(mockElement);

        Element result = elementService.getById("ele-test-001");

        assertNotNull(result);
        assertEquals("ele-test-001", result.getId());
        assertEquals("FUND_TYPE", result.getCode());
        assertEquals("资金性质", result.getName());
        verify(elementMapper).selectById("ele-test-001");
    }

    @Test
    @DisplayName("根据ID查询代码集 - 不存在的ID返回null")
    void test_getById_withInvalidId_returnsNull() {
        when(elementMapper.selectById("non-existent")).thenReturn(null);

        Element result = elementService.getById("non-existent");

        assertNull(result);
        verify(elementMapper).selectById("non-existent");
    }

    // ==================== add 测试 ====================

    @Test
    @DisplayName("新增代码集 - 正常新增")
    void test_add_withValidElement_insertsAndReturns() {
        Element newElement = ElementTestDataFactory.createElementForInsert();
        when(elementMapper.insert(any(Element.class))).thenReturn(1);

        Element result = elementService.add(newElement);

        assertNotNull(result);
        assertEquals("FUND_TYPE", result.getCode());
        verify(elementMapper, times(1)).insert(any(Element.class));
    }

    // ==================== update 测试 ====================

    @Test
    @DisplayName("更新代码集 - 正常更新")
    void test_update_withValidElement_returnsTrue() {
        Element updateElement = ElementTestDataFactory.createElementForUpdate("ele-test-001");
        when(elementMapper.update(any(Element.class))).thenReturn(1);

        boolean result = elementService.update(updateElement);

        assertTrue(result);
        verify(elementMapper, times(1)).update(any(Element.class));
    }

    @Test
    @DisplayName("更新代码集 - 不存在的记录返回false")
    void test_update_withNonExistentId_returnsFalse() {
        Element updateElement = ElementTestDataFactory.createElementForUpdate("non-existent");
        when(elementMapper.update(any(Element.class))).thenReturn(0);

        boolean result = elementService.update(updateElement);

        assertFalse(result);
        verify(elementMapper, times(1)).update(any(Element.class));
    }

    // ==================== delete 测试 ====================

    @Test
    @DisplayName("删除代码集 - 正常删除")
    void test_delete_withValidId_returnsTrue() {
        when(elementMapper.deleteById("ele-test-001")).thenReturn(1);

        boolean result = elementService.delete("ele-test-001");

        assertTrue(result);
        verify(elementMapper, times(1)).deleteById("ele-test-001");
    }

    @Test
    @DisplayName("删除代码集 - 不存在的记录返回false")
    void test_delete_withNonExistentId_returnsFalse() {
        when(elementMapper.deleteById("non-existent")).thenReturn(0);

        boolean result = elementService.delete("non-existent");

        assertFalse(result);
        verify(elementMapper, times(1)).deleteById("non-existent");
    }
}
```

---

### 三、Controller 层集成测试

**目的**：测试 HTTP 接口的请求/响应，验证参数绑定、序列化、状态码。

**命名规范**：`{Controller}Test`，如 `ElementControllerTest`。

**注解和框架**：
```java
@WebMvcTest(ElementController.class)  // 仅加载 Web 层
// 或
@SpringBootTest + @AutoConfigureMockMvc  // 完整上下文
```

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

4. **测试用例覆盖表**：

| 端点 | HTTP方法 | 测试场景 | 测试方法名 |
|------|---------|---------|-----------|
| /list | GET | 正常返回列表 | test_list_GET_returnsElements |
| /detail/{id} | GET | 有效ID | test_detail_GET_withValidId |
| /detail/{id} | GET | 无效ID | test_detail_GET_withInvalidId |
| /getByCode | GET | 有效编码 | test_getByCode_GET_withValidCode |
| /getByCode | GET | 缺少参数 | test_getByCode_GET_missingParam |
| /add | POST | 正常新增 | test_add_POST_withValidBody |
| /add | POST | 空请求体 | test_add_POST_withEmptyBody |
| /update | POST | 正常更新 | test_update_POST_withValidBody |
| /delete | POST | 正常删除 | test_delete_POST_withValidId |

**模板示例**：

```java
package grp.pt.element.controller.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import grp.pt.element.model.dto.Element;
import grp.pt.element.service.facade.IElementService;
import grp.pt.testdata.ElementTestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ElementController.class)
@DisplayName("ElementController 接口测试")
class ElementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IElementService elementService;

    // ==================== GET /config/element/list ====================

    @Test
    @DisplayName("GET /config/element/list - 正常返回代码集列表")
    void test_list_GET_returnsElements() throws Exception {
        List<Element> mockList = ElementTestDataFactory.createElementList(3);
        when(elementService.getList()).thenReturn(mockList);

        mockMvc.perform(get("/config/element/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value("200"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(3))
            .andExpect(jsonPath("$.data[0].code").exists());

        verify(elementService, times(1)).getList();
    }

    // ==================== GET /config/element/detail/{id} ====================

    @Test
    @DisplayName("GET /config/element/detail/{id} - 有效ID返回代码集详情")
    void test_detail_GET_withValidId() throws Exception {
        Element mockElement = ElementTestDataFactory.createDefaultElement();
        when(elementService.getById("ele-test-001")).thenReturn(mockElement);

        mockMvc.perform(get("/config/element/detail/{id}", "ele-test-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value("200"))
            .andExpect(jsonPath("$.data.id").value("ele-test-001"))
            .andExpect(jsonPath("$.data.code").value("FUND_TYPE"))
            .andExpect(jsonPath("$.data.name").value("资金性质"));

        verify(elementService).getById("ele-test-001");
    }

    // ==================== POST /config/element/add ====================

    @Test
    @DisplayName("POST /config/element/add - 正常新增代码集")
    void test_add_POST_withValidBody() throws Exception {
        Element newElement = ElementTestDataFactory.createElementForInsert();
        Element savedElement = ElementTestDataFactory.createDefaultElement();
        when(elementService.add(any(Element.class))).thenReturn(savedElement);

        mockMvc.perform(post("/config/element/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newElement)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value("200"))
            .andExpect(jsonPath("$.data.code").value("FUND_TYPE"));

        verify(elementService, times(1)).add(any(Element.class));
    }

    // ==================== POST /config/element/update ====================

    @Test
    @DisplayName("POST /config/element/update - 正常更新代码集")
    void test_update_POST_withValidBody() throws Exception {
        Element updateElement = ElementTestDataFactory.createElementForUpdate("ele-test-001");
        when(elementService.update(any(Element.class))).thenReturn(true);

        mockMvc.perform(post("/config/element/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateElement)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value("200"))
            .andExpect(jsonPath("$.data").value(true));

        verify(elementService, times(1)).update(any(Element.class));
    }

    // ==================== POST /config/element/delete ====================

    @Test
    @DisplayName("POST /config/element/delete - 正常删除代码集")
    void test_delete_POST_withValidId() throws Exception {
        when(elementService.delete("ele-test-001")).thenReturn(true);

        mockMvc.perform(post("/config/element/delete")
                .param("id", "ele-test-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value("200"))
            .andExpect(jsonPath("$.data").value(true));

        verify(elementService, times(1)).delete("ele-test-001");
    }
}
```

---

### 四、Mapper 层数据访问测试（可选）

**目的**：验证 SQL 映射正确性，使用 H2 内嵌数据库。

**命名规范**：`{Mapper}Test`，如 `ElementMapperTest`。

**注解**：
```java
@MybatisTest                          // MyBatis 专用测试注解
@AutoConfigureTestDatabase             // 使用内嵌数据库
@Sql("/sql/init-element-test-data.sql") // 初始化测试数据
```

**生成规则**：

1. 在 `src/test/resources/sql/` 下生成初始化 SQL 脚本
2. SQL 脚本包含建表语句和初始数据
3. 每个 Mapper 方法至少一个测试用例
4. 使用 `@Sql` 注解加载测试数据

**初始化 SQL 模板**：

```sql
-- src/test/resources/sql/init-element-test-data.sql

-- 建表语句（根据实际数据库表结构）
CREATE TABLE IF NOT EXISTS sys_element (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    max_level INT DEFAULT 1,
    vd_code VARCHAR(100) DEFAULT '',
    ele_type VARCHAR(10),
    ele_source VARCHAR(200),
    ele_cate_id BIGINT DEFAULT 0,
    is_deleted INT DEFAULT 2,
    is_state INT DEFAULT 1,
    is_enabled INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始化测试数据
INSERT INTO sys_element (id, code, name, max_level, ele_type, is_deleted, is_state)
VALUES ('ele-test-001', 'FUND_TYPE', '资金性质', 3, '2', 2, 1);

INSERT INTO sys_element (id, code, name, max_level, ele_type, is_deleted, is_state)
VALUES ('ele-test-002', 'BUDGET_LEVEL', '预算级次', 2, '2', 2, 1);

INSERT INTO sys_element (id, code, name, max_level, ele_type, is_deleted, is_state)
VALUES ('ele-test-003', 'FUNC_CODE', '功能分类', 4, '2', 2, 1);
```

**测试配置文件**：

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: false

mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    grp.pt: DEBUG
```

---

### 五、Feign 降级测试

**目的**：验证 FallbackFactory 降级逻辑。

**生成规则**：

1. 每个 FallbackFactory 的每个降级方法都要测试
2. 验证降级返回的 ReturnData 状态码为失败
3. 验证降级返回的 reason 包含友好错误信息

**模板示例**：

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ElementFeignClientFallbackFactory 降级测试")
class ElementFeignClientFallbackFactoryTest {

    private ElementFeignClientFallbackFactory factory = new ElementFeignClientFallbackFactory();

    @Test
    @DisplayName("降级 - getList 返回失败信息")
    void test_fallback_getList_returnsFail() {
        IElementFeignClient fallbackClient = factory.create(new RuntimeException("连接超时"));

        ReturnData<List<Element>> result = fallbackClient.getList();

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertNotNull(result.getReason());
    }
}
```

---

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
| Service 层 | 100% | ≥ 80% |
| Controller 层 | 100% | ≥ 70% |
| Mapper 层 | ≥ 80% | N/A |
| Feign Fallback | 100% | N/A |

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

`element-module` 项目（`d:\CTJSOFT\Qoder\refactor\element-module`）作为参考实现，观察到的关键模式：

### 分层架构
- **Controller** → 调用 Service → 包装为 `ReturnData<T>` 返回
- **Service** → 调用 Mapper → 处理业务逻辑
- **Mapper** → MyBatis 注解/XML → 数据库操作

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

