# S5 检查规则清单

## 通用扫描规则

### 扫描范围定义

**必须扫描的目录**（基于四层架构）：
- Controller 层：`**/controller/**/*.java`、`**/custom/**/*.java`、`**/common/**/*.java`
- Service 层：`**/service/**/*.java`、`**/facade/**/*.java`
- DAO 层：`**/dao/**/*.java`、`**/mapper/**/*.java`、`**/repository/**/*.java`
- Model 层：`**/model/**/*.java`、`**/entity/**/*.java`、`**/dto/**/*.java`、`**/vo/**/*.java`、`**/query/**/*.java`

**排除目录**：
- `**/test/**`、`**/tests/**`
- `**/target/**`、`**/build/**`
- `**/generated/**`、`**/generated-sources/**`
- `**/.git/**`

### 文件过滤规则

- 只扫描 `.java` 文件
- 跳过 `package-info.java`
- 跳过 `module-info.java`

### 类类型识别规则

**仅检查以下类定义**（精确正则）：
```regex
^\s*public\s+(class|interface|abstract\s+class|enum)\s+(\w+)
```

**跳过以下类型**：
- 内部类（non-top-level class）：如果文件名与类名不一致，则为内部类，跳过
- 匿名类
- 测试类（文件路径包含 `test/` 或 `tests/` 或类名以 `Test` 结尾或以 `Test` 开头）

### 幂等性检查

**已合规项跳过规则**：扫描时对每个检查项，如果当前项已完全符合规范，在报告中标记 `PASS` 并跳过，不生成修复建议。避免重复执行时产生多余输出。

---

## S5-01：接口路径规范

**检查目标**：Controller 接口路径是否符合四级路径规范。

**标准路径结构**：`/{一级}/{二级模块}/{三级操作}/{四级明细}`

### 精确扫描正则

**Step 1：提取类级路径前缀**
```regex
@RequestMapping\s*\(\s*(?:value\s*=\s*)?["']([^"']+)["']
```
在类定义上方搜索此正则，提取类级路径前缀。

**Step 2：提取方法级路径**
```regex
@(GetMapping|PostMapping|DeleteMapping|PutMapping|PatchMapping|RequestMapping)\s*\(
```
匹配到注解后，提取其中的路径值：
```regex
(?:value\s*=\s*)?["']([^"']+)["']
```

**Step 3：组合完整路径**
- 完整路径 = 类级前缀 + 方法级路径
- 统计路径层级数：`split("/").filter(非空).length`

### 判定标准

| 检查项 | 正则/方法 | 判定 | 分类 |
|--------|----------|------|------|
| 路径层级不等于 4 级 | 完整路径 split("/") 后非空段数 ≠ 4 | WARN | 约束限制 |
| 使用 `@DeleteMapping` | `@DeleteMapping` | WARN | 可修复 |
| 使用 `@PutMapping` | `@PutMapping` | WARN | 可修复 |
| 使用 `@PatchMapping` | `@PatchMapping` | WARN | 约束限制 |
| 单层路径 > 40 字符 | 路径段长度检查 | WARN | 约束限制 |
| 单 Controller 接口数 > 15 | 统计匹配到的接口方法数 | WARN | 约束限制 |
| 类级 `@RequestMapping` 缺失 | 类定义前未找到 `@RequestMapping` | WARN | 约束限制 |

### 边界场景处理

| 场景 | 处理方式 |
|------|---------|
| `@RequestMapping` 无路径值（如 `@RequestMapping(method = POST)`） | 视为路径为空串 "" |
| 方法级使用 `@RequestMapping` 且未指定 method | 标记 WARN：HTTP 方法不明确 |
| 类上多个 `@RequestMapping`（理论上不合法） | 取第一个值 |
| 路径使用变量（如 `${api.prefix}`） | 标记 WARN：无法静态解析，记录但不判定路径层级 |
| 路径使用 PathVariable（如 `/user/{id}`） | `{id}` 视为一个路径层级 |

---

## S5-02：类命名规范

**检查目标**：各层类名是否符合命名规范。

### 精确扫描正则

**提取类名**：
```regex
^\s*public\s+(?:abstract\s+)?(?:class|interface)\s+(\w+)
```

**层级判定**（根据文件路径确定所属层）：

| 文件路径匹配模式 | 所属层 | 要求后缀 |
|---------------|--------|---------|
| `**/controller/**` 或 `**/custom/**` 或 `**/common/**` | Controller 层 | `Controller` |
| `**/facade/**` 且为 interface | Service 接口层 | `Service` |
| `**/facade/**` 且为 class | Service 接口层 | `Service`（异常情况，应为 interface） |
| `**/service/**/impl/**` 或类名以 `Impl` 结尾 | Service 实现层 | `ServiceImpl` |
| `**/service/**`（非 impl 子目录） | Service 层 | `Service` 或 `ServiceImpl` |
| `**/dao/**` 且为 interface | DAO 接口层 | `Dao` |
| `**/dao/**/impl/**` | DAO 实现层 | `DaoImpl` |
| `**/mapper/**` | Mapper 层 | `Mapper` |
| `**/entity/**` | Entity 层 | `Entity`（可选后缀） |
| `**/dto/**` | DTO 层 | `DTO` |
| `**/vo/**` | VO 层 | `VO` |
| `**/query/**` 或 `**/param/**` | 查询条件 | `Query` 或 `Param` |

### 判定标准

| 检查项 | 精确正则 | 判定 | 分类 |
|--------|---------|------|------|
| 首字母非大写 | `^[a-z]` | WARN | 可修复 |
| 不符合大驼峰 | `[a-z][A-Z]` 模式缺失或含 `_` | WARN | 可修复 |
| 使用黑名单缩写 | 见黑名单表 | WARN | 可修复 |
| 类名超长 | 字符数超出对应层的限制 | WARN | 约束限制 |
| 后缀不匹配所属层 | 类名后缀 vs 层要求后缀 | WARN | 可修复 |

### 不规范缩写黑名单（精确匹配）

使用以下正则精确检测（区分大小写、整词边界匹配）：

| 黑名单缩写 | 检测正则 | 应替换为 |
|-----------|---------|---------|
| `Ctrl` | 类名以 `Ctrl` 结尾 | `Controller` |
| `Svc` | 类名包含 `Svc` 且位于 service 层 | `Service` |
| `Repo` | 类名以 `Repo` 结尾 | `Repository` 或 `Dao` |
| `Mgr` | 类名以 `Mgr` 结尾 | `Manager` |
| `Util` 作为类后缀 | 类名以 `Util` 结尾（应为 `Utils`） | `Utils` |
| `Info` 替代 `DTO`/`VO` | 类名以 `Info` 结尾且在 dto/vo 目录 | `DTO` 或 `VO` |

### 长度限制对照表

| 分层 | 标准后缀 | 最大类名长度 | 说明 |
|------|---------|------------|------|
| Controller | Controller | 40 字符 | 含后缀 |
| Service 接口 | Service | 37 字符 | 含后缀 |
| Service 实现 | ServiceImpl | 41 字符 | 含后缀 |
| DAO 接口 | Dao | 33 字符 | 含后缀 |
| DAO 实现 | DaoImpl | 37 字符 | 含后缀 |
| Mapper | Mapper | 36 字符 | 含后缀 |
| Entity | Entity | 36 字符 | 含后缀（Entity 后缀可选） |
| DTO | DTO | 无硬性限制 | — |
| VO | VO | 无硬性限制 | — |
| Query/Param | Query/Param | 无硬性限制 | — |

### 边界场景处理

| 场景 | 处理方式 |
|------|---------|
| 抽象类 | 正常检查命名规范，不要求 abstract 前缀 |
| 枚举类 | 枚举类名使用大驼峰，不要求特定后缀，跳过后缀检查 |
| 工具类（`**/util/**` 或 `**/utils/**`） | 跳过后缀检查，但检查大驼峰 |
| 配置类（`**/config/**`） | 跳过后缀检查，但检查大驼峰 |
| 常量类（`**/constant/**` 或 `**/constants/**`） | 跳过后缀检查 |
| 异常类（`**/exception/**`） | 期望后缀 `Exception`，跳过层级后缀检查 |
| 同一文件多个 public 类（编译错误） | 以文件名对应的类为准 |
| I 前缀接口（如 `IElementService`） | `I` 前缀视为合规，不报告 |

> 详细后缀和长度规范见 → [templates/naming-convention.md](../templates/naming-convention.md)

---

## S5-03：属性命名规范

**检查目标**：类属性是否符合命名规范。

### 精确扫描正则

**提取字段声明**：
```regex
^\s*(private|protected)\s+\w[\w<>\[\],\s]*\s+(\w+)\s*[;=]
```
- 捕获组 1：访问修饰符
- 捕获组 2：字段名

**排除项**：
```regex
^\s*(private|protected)\s+static\s+final\s+
```
- `static final` 常量不检查小驼峰（应全大写下划线）
- 序列化字段 `serialVersionUID` 跳过

### 判定标准

| 检查项 | 精确检测方法 | 判定 | 分类 |
|--------|------------|------|------|
| 下划线命名 | 字段名匹配 `\w+_\w+` | WARN | 约束限制（DTO/VO 层）或 可修复（其他层） |
| 非小驼峰 | 字段名首字母大写 `^[A-Z]` | WARN | 可修复 |
| ID 后缀格式错误 | 字段名匹配 `\w+ID$` 或 `\w+_id$`（应为 `xxxId`） | WARN | 约束限制 |
| 布尔属性缺少 is 前缀 | 字段类型为 `boolean`/`Boolean` 且名称不匹配 `^is[A-Z]` | WARN | 约束限制 |
| 单字符变量名 | 字段名匹配 `^[a-z]$`（长度 = 1） | WARN | 可修复 |
| 常量未全大写 | `static final` 字段名不匹配 `^[A-Z][A-Z0-9_]*$` | WARN | 可修复 |

### DTO/VO 层特殊规则

- DTO/VO 属性使用下划线命名时 → 标记为 **约束限制**，不修复（影响 JSON 序列化兼容性）
- 非 DTO/VO 层属性使用下划线命名时 → 标记为 **可修复**

### 边界场景处理

| 场景 | 处理方式 |
|------|---------|
| Lombok `@Data` / `@Getter` / `@Setter` 生成的 getter/setter | 不影响字段名检查，正常检查字段 |
| `@JsonProperty("snake_case")` 已标注 | 视为已做兼容处理，字段名检查仍报告但标记"已兼容" |
| `transient` 字段 | 正常检查 |
| 泛型字段（如 `List<String> items`） | 正常提取字段名 `items` 检查 |
| 数组字段（如 `String[] names`） | 正常提取字段名 `names` 检查 |

---

## S5-04：接口参数规范

**检查目标**：接口方法参数是否符合命名规范。

### 精确扫描正则

**Step 1：定位 Controller 方法**
```regex
@(GetMapping|PostMapping|DeleteMapping|PutMapping|PatchMapping|RequestMapping)
```
匹配到的注解所修饰的方法声明。

**Step 2：提取方法参数列表**
```regex
public\s+\S+\s+\w+\s*\(([^)]*)\)
```
捕获组 1 为参数列表字符串。

**Step 3：逐个解析参数**
从参数列表中提取每个参数的名称和注解：
```regex
(?:@\w+(?:\([^)]*\))?\s+)*(\w[\w<>\[\],\s]*)\s+(\w+)
```

### 判定标准

| 检查项 | 精确检测方法 | 判定 | 分类 |
|--------|------------|------|------|
| 参数使用下划线命名 | 参数名匹配 `\w+_\w+` | WARN | 约束限制 |
| 分页参数不统一 | 存在 `page`/`size`/`limit`/`offset`/`pageNo` 但非 `pageNum`+`pageSize` | WARN | 约束限制 |
| 必填参数缺少校验注解 | 参数类型非基本类型，且无 `@NotNull`/`@NotBlank`/`@NotEmpty`/`@Valid`/`@Validated` 注解 | WARN | 约束限制 |

### 边界场景处理

| 场景 | 处理方式 |
|------|---------|
| `@RequestBody` 修饰的对象参数 | 只检查参数名本身，内部字段由 S5-03 检查 |
| `HttpServletRequest`/`HttpServletResponse` 参数 | 跳过检查 |
| `@PathVariable` 参数 | 正常检查命名 |
| `@RequestParam(name = "xxx")` 显式指定参数名 | 报告中记录实际传参名和 Java 参数名，只检查 Java 参数名 |
| Spring 特殊参数（`Model`、`BindingResult` 等） | 跳过检查 |

---

## S5-05：接口响应规范

**检查目标**：接口返回值是否符合统一响应格式。

### 精确扫描正则

**提取 Controller 方法的返回类型**：
```regex
public\s+([\w<>,\s\?]+)\s+\w+\s*\(
```
捕获组 1 为返回类型。

### 判定标准

| 检查项 | 精确检测方法 | 判定 | 分类 |
|--------|------------|------|------|
| 未使用 ReturnData/ReturnPage 包装 | 返回类型不匹配 `ReturnData<.*>` 且不匹配 `ReturnPage<.*>` | WARN | 约束限制 |
| 直接返回 Entity 对象 | 返回类型匹配 `\w+Entity` 或 `ReturnData<\w+Entity>` | WARN | 约束限制 |
| 返回 void | 返回类型为 `void` | PASS | — |
| 返回 ResponseEntity | 返回类型匹配 `ResponseEntity<.*>` | WARN | 约束限制 |

### 边界场景处理

| 场景 | 处理方式 |
|------|---------|
| `ReturnData<List<XxxVO>>` 嵌套泛型 | 视为合规 |
| `ReturnData<?>` 通配符 | 标记 WARN：建议指定具体类型 |
| 返回类型为 `Object` | 标记 WARN：建议使用具体类型 |
| 文件下载接口（返回 `void` + `HttpServletResponse`） | 跳过检查 |
| `@ResponseBody` 注解存在但无 `@RestController` | 正常检查 |

---

## S5-06：Bean 命名冲突排查

**检查目标**：是否存在 Spring Bean 命名冲突。

### 精确扫描正则

**Step 1：提取所有 Bean 定义**
```regex
@(Service|Controller|RestController|Repository|Component)\s*(?:\(\s*["']([^"']*)["']\s*\))?
```
- 捕获组 1：注解类型
- 捕获组 2：自定义 Bean 名称（可能为空）

**Step 2：计算默认 Bean 名称**
- 如果注解有自定义名称 → 使用自定义名称
- 如果无自定义名称 → 类名首字母小写 = 默认 Bean 名称
  - 例：`ElementController` → `elementController`
  - 特殊规则：如果类名前两个字母都大写，则不转换 → `URLParser` → `URLParser`

**Step 3：检测冲突**
- 将所有 Bean 名称收集到一个全局列表
- 如果存在重复名称 → FAIL

### 判定标准

| 检查项 | 精确检测方法 | 判定 | 分类 |
|--------|------------|------|------|
| Bean 名称冲突 | 全局列表中有重复 Bean 名 | FAIL | 视情况 |
| "2" 后缀命名模式 | 类名匹配 `\w+2$` | WARN | 视情况 |
| 自定义 Bean 名与另一个默认 Bean 名冲突 | 交叉检查 | FAIL | 可修复 |

### "2"后缀确定性判定规则

不再依赖主观判断"是否有意设计"，而使用以下确定性规则：

| 判定条件 | 结论 | 处理方式 |
|---------|------|---------|
| 存在 `XxxController` 和 `XxxController2` 在**同一模块** | 实际冲突 | 可修复 |
| 存在 `XxxController` 和 `XxxController2` 在**不同模块** | 跨模块重名 | 可修复（通过 `@Controller("前缀名")` 指定 Bean 名） |
| 只存在 `XxxController2` 而无 `XxxController` | 遗留命名 | 可修复（去掉 "2" 后缀） |
| 类上有注释说明 "2" 的用途 | 有意设计 | 约束限制 |

### 边界场景处理

| 场景 | 处理方式 |
|------|---------|
| `@Bean` 方法定义的 Bean | 不在本检查范围（只检查类级别注解定义的 Bean） |
| `@Configuration` 类 | 跳过 Bean 命名冲突检查 |
| `@Conditional*` 条件 Bean | 记录但标记为"条件 Bean，可能不冲突" |
| 多 module 项目 | 需要跨 module 扫描 Bean 名称 |
