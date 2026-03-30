# Step8 公共模块提取 - 检查规则

## 检查规则定义

### 通用检查方法论

每个检查项统一按以下 3 步执行：

1. **扫描定位**：使用 Glob 搜索目标包下的 `.java` 文件
2. **依赖分析**：使用 Grep 搜索每个文件的 import 和注解，判定依赖关系
3. **量化判定**：根据量化指标自动分类（EXTRACT / EVALUATE / RETAIN）

### 通用 Grep 模式库

以下 Grep 模式在所有检查项中通用：

| 用途 | Grep 搜索模式 | 说明 |
|------|--------------|------|
| 检测 Service 依赖 | `import grp\.pt\..*\.service\.` 或 `import grp\.pt\.service\.` | 是否依赖本模块 Service 层 |
| 检测 DAO/Mapper 依赖 | `import grp\.pt\..*\.(dao\|mapper)\.` 或 `import grp\.pt\.(dao\|mapper)\.` | 是否依赖本模块 DAO 层 |
| 检测自动注入 | `@Autowired\|@Resource\|@Inject` | 是否有 Spring 依赖注入 |
| 检测 Spring Bean 注解 | `@Component\|@Service\|@Repository\|@Configuration\|@Bean` | 是否是 Spring 管理的 Bean |
| 统计跨模块引用数 | `import.*{全限定类名}` | 在整个工程中搜索，按模块去重统计 |

### 量化判定标准（全局适用）

| 判定结果 | 条件 | 操作 |
|----------|------|------|
| **EXTRACT（推荐提取）** | 同时满足：(1) 无 Service/DAO 层 import；(2) 无 @Autowired/@Resource 注入 Service/DAO | 自动列入迁移清单 |
| **EVALUATE（需人工判断）** | 满足以下任一：(1) 有 Service/DAO import 但被 >=2 个不同模块引用；(2) 包含 @Autowired 但注入的是通用组件（如 RedisTemplate、RestTemplate） | 列入报告，等用户确认 |
| **RETAIN（建议保留）** | 满足以下任一：(1) 有 @Autowired/@Resource 注入本模块 Service/DAO 接口；(2) 仅被当前模块内部使用且有 Service/DAO 依赖 | 自动排除出迁移清单 |

---

### S8-01: util/ 包归属检查

**检查目标**: 工具类是否仍在业务模块（如 element-service）的 `util/` 包下

**Step 1 - 扫描定位**:
```
Glob 模式: grp-capability-{module}/{sub-module}/src/main/java/**/util/*.java
```

**Step 2 - 依赖分析**（对每个扫描到的 .java 文件执行）:

| 序号 | Grep 搜索模式 | 搜索范围 | 判定用途 |
|------|--------------|----------|----------|
| 1 | `import grp\.pt\..*\.service\.\|import grp\.pt\.service\.` | 当前文件 | 是否依赖 Service 层 |
| 2 | `import grp\.pt\..*\.(dao\|mapper)\.\|import grp\.pt\.(dao\|mapper)\.` | 当前文件 | 是否依赖 DAO 层 |
| 3 | `@Autowired\|@Resource\|@Inject` | 当前文件 | 是否有注入依赖 |
| 4 | `import.*{当前类的全限定名}` | 整个工程所有 .java 文件 | 统计被引用数（按模块去重） |

**Step 3 - 量化判定**:

```
判定流程:
├─ Grep#1 + Grep#2 = 无匹配（无 Service/DAO 依赖）
│   └─ → EXTRACT（推荐提取）
├─ Grep#3 = 有匹配（有注入依赖）
│   ├─ 注入的是通用组件（RedisTemplate/RestTemplate/JdbcTemplate/ObjectMapper 等）
│   │   └─ Grep#4 跨模块引用数 >= 2 → EVALUATE（需人工判断）
│   │   └─ Grep#4 跨模块引用数 < 2  → RETAIN（建议保留）
│   └─ 注入的是本模块 Service/DAO 接口
│       └─ → RETAIN（建议保留）
└─ Grep#1 或 Grep#2 = 有匹配（有 Service/DAO import 但无注入）
    ├─ Grep#4 跨模块引用数 >= 2 → EVALUATE（需人工判断）
    └─ Grep#4 跨模块引用数 < 2  → RETAIN（建议保留）
```

**通用组件白名单**（注入这些组件不影响提取判定）:
- `RedisTemplate`、`StringRedisTemplate`
- `RestTemplate`、`WebClient`
- `JdbcTemplate`、`NamedParameterJdbcTemplate`
- `ObjectMapper`
- `ApplicationContext`、`Environment`
- `MessageSource`

---

### S8-02: cache/ 包归属检查

**检查目标**: 缓存类是否仍在业务模块的 `cache/` 包下

**Step 1 - 扫描定位**:
```
Glob 模式: grp-capability-{module}/{sub-module}/src/main/java/**/cache/*.java
```

**Step 2 - 依赖分析**:

| 序号 | Grep 搜索模式 | 搜索范围 | 判定用途 |
|------|--------------|----------|----------|
| 1 | `import grp\.pt\..*\.service\.\|import grp\.pt\.service\.` | 当前文件 | 是否依赖 Service 层 |
| 2 | `import grp\.pt\..*\.(dao\|mapper)\.\|import grp\.pt\.(dao\|mapper)\.` | 当前文件 | 是否依赖 DAO 层（缓存加载器常见） |
| 3 | `@Autowired\|@Resource\|@Inject` | 当前文件 | 是否有注入依赖 |
| 4 | `import.*{当前类的全限定名}` | 整个工程所有 .java 文件 | 统计被引用数（按模块去重） |
| 5 | `@Cacheable\|@CacheEvict\|@CachePut\|@Caching` | 当前文件 | 是否使用 Spring Cache 注解 |

**Step 3 - 量化判定**:

```
判定流程:
├─ Grep#2 = 有匹配（依赖 DAO/Mapper，属于缓存加载器）
│   └─ → RETAIN（建议保留，缓存加载器与 DAO 强耦合）
├─ Grep#1 = 有匹配（依赖 Service 层）
│   └─ → RETAIN（建议保留）
├─ Grep#3 = 有匹配 且 注入的非通用组件
│   └─ → RETAIN（建议保留）
├─ Grep#3 = 无匹配 或 仅注入通用组件（如 RedisTemplate）
│   └─ → EXTRACT（推荐提取）
└─ 其他情况
    └─ Grep#4 跨模块引用数 >= 2 → EVALUATE（需人工判断）
    └─ Grep#4 跨模块引用数 < 2  → RETAIN（建议保留）
```

---

### S8-03: constant/ 包归属检查

**检查目标**: 常量类是否仍在业务模块的 `constant/` 包下

**Step 1 - 扫描定位**:
```
Glob 模式: grp-capability-{module}/{sub-module}/src/main/java/**/constant/*.java
             grp-capability-{module}/{sub-module}/src/main/java/**/constants/*.java
```
注意：`constant/` 和 `constants/` 两种目录名都要扫描。

**Step 2 - 依赖分析**:

| 序号 | Grep 搜索模式 | 搜索范围 | 判定用途 |
|------|--------------|----------|----------|
| 1 | `import grp\.pt\..*\.service\.\|import grp\.pt\.service\.` | 当前文件 | 是否依赖 Service 层（常量类极少出现） |
| 2 | `import grp\.pt\..*\.(dao\|mapper)\.\|import grp\.pt\.(dao\|mapper)\.` | 当前文件 | 是否依赖 DAO 层（常量类极少出现） |
| 3 | `@Autowired\|@Resource\|@Inject` | 当前文件 | 常量类不应有注入 |

**Step 3 - 量化判定**:

```
判定流程:
├─ Grep#1 + Grep#2 + Grep#3 = 全部无匹配
│   └─ → EXTRACT（推荐提取）—— 常量类绝大多数属于此类
├─ Grep#3 = 有匹配
│   └─ → RETAIN（建议保留，常量类不应有注入，可能是非典型常量类）
└─ Grep#1 或 Grep#2 = 有匹配
    └─ → EVALUATE（需人工判断，常量类引用 Service/DAO 非常规，需确认）
```

**特殊说明**: 常量类（仅含 `static final` 字段定义）通常无任何外部依赖，预期 >95% 的常量类判定为 EXTRACT。

---

### S8-04: enums/ 包归属检查

**检查目标**: 枚举类是否仍在业务模块的 `enums/` 包下

**Step 1 - 扫描定位**:
```
Glob 模式: grp-capability-{module}/{sub-module}/src/main/java/**/enums/*.java
             grp-capability-{module}/{sub-module}/src/main/java/**/enum/*.java
```
注意：`enums/` 和 `enum/` 两种目录名都要扫描。

**Step 2 - 依赖分析**:

| 序号 | Grep 搜索模式 | 搜索范围 | 判定用途 |
|------|--------------|----------|----------|
| 1 | `import grp\.pt\..*\.service\.\|import grp\.pt\.service\.` | 当前文件 | 是否有 Service 依赖 |
| 2 | `@Autowired\|@Resource\|@Inject` | 当前文件 | 枚举类不应有注入 |
| 3 | `implements.*Strategy\|implements.*Handler` | 当前文件 | 是否实现了策略/处理器接口（枚举策略模式） |

**Step 3 - 量化判定**:

```
判定流程:
├─ Grep#1 + Grep#2 + Grep#3 = 全部无匹配
│   └─ → EXTRACT（推荐提取）—— 枚举类绝大多数属于此类
├─ Grep#3 = 有匹配（枚举实现策略接口）
│   ├─ 策略接口定义在同一个模块中 → EVALUATE（需人工判断，可能需要一起迁移）
│   └─ 策略接口定义在 common 或其他公共模块 → EXTRACT（推荐提取）
├─ Grep#2 = 有匹配
│   └─ → RETAIN（建议保留，枚举类有注入是非典型用法）
└─ Grep#1 = 有匹配（import 了 Service 但无注入）
    └─ → EVALUATE（需人工判断）
```

**特殊说明**: 纯枚举类（仅含枚举值定义和简单方法）通常无任何外部依赖，预期 >95% 的枚举类判定为 EXTRACT。

---

### S8-05: exception/ 包归属检查

**检查目标**: 异常类是否仍在业务模块的 `exception/` 包下

**Step 1 - 扫描定位**:
```
Glob 模式: grp-capability-{module}/{sub-module}/src/main/java/**/exception/*.java
             grp-capability-{module}/{sub-module}/src/main/java/**/exceptions/*.java
```

**Step 2 - 依赖分析**:

| 序号 | Grep 搜索模式 | 搜索范围 | 判定用途 |
|------|--------------|----------|----------|
| 1 | `extends.*Exception\|extends.*RuntimeException\|extends.*Throwable` | 当前文件 | 确认是异常类 |
| 2 | `@ControllerAdvice\|@RestControllerAdvice\|@ExceptionHandler` | 当前文件 | 是否是全局异常处理器（非异常定义类） |
| 3 | `import grp\.pt\..*\.service\.\|import grp\.pt\.service\.` | 当前文件 | 异常处理器可能依赖 Service |

**Step 3 - 量化判定**:

```
判定流程:
├─ Grep#1 = 有匹配（是异常定义类）
│   └─ → EXTRACT（推荐提取）—— 异常定义类几乎无依赖
├─ Grep#2 = 有匹配（是全局异常处理器）
│   ├─ Grep#3 = 无匹配（不依赖 Service）
│   │   └─ → EXTRACT（推荐提取）
│   └─ Grep#3 = 有匹配（依赖 Service）
│       └─ → RETAIN（建议保留）
└─ Grep#1 + Grep#2 = 均无匹配（既非异常类也非异常处理器）
    └─ → EVALUATE（需人工判断，文件在 exception/ 目录下但不是典型异常类）
```

**特殊说明**: 
- 自定义异常类（如 BusinessException、XxxException）只继承 Exception/RuntimeException，无任何外部依赖，**100% 应提取**
- 全局异常处理器（@ControllerAdvice 标注的类）虽在 exception/ 目录下，但可能依赖 Service，需单独分析
- 异常错误码枚举/常量如果与异常类在同一目录，也应一并提取

---

### S8-06: config/ 包归属检查

**检查目标**: 配置类是否仍在业务模块的 `config/` 包下

**Step 1 - 扫描定位**:
```
Glob 模式: grp-capability-{module}/{sub-module}/src/main/java/**/config/*.java
             grp-capability-{module}/{sub-module}/src/main/java/**/configuration/*.java
```

**Step 2 - 依赖分析**:

| 序号 | Grep 搜索模式 | 搜索范围 | 判定用途 |
|------|--------------|----------|----------|
| 1 | `@MapperScan` | 当前文件 | 是否是 MyBatis Mapper 扫描配置 |
| 2 | `@ComponentScan` | 当前文件 | 是否有组件扫描配置 |
| 3 | `@EnableScheduling\|@EnableAsync\|@EnableCaching\|@EnableTransactionManagement` | 当前文件 | 是否启用了 Spring 特性 |
| 4 | `@Autowired\|@Resource\|@Inject` | 当前文件 | 是否注入了本模块 Service/DAO |
| 5 | `import grp\.pt\..*\.service\.\|import grp\.pt\.service\.` | 当前文件 | 是否依赖 Service 层 |
| 6 | `import grp\.pt\..*\.(dao\|mapper)\.\|import grp\.pt\.(dao\|mapper)\.` | 当前文件 | 是否依赖 DAO 层 |
| 7 | `@ConditionalOn\|@Profile` | 当前文件 | 是否有条件化配置 |

**Step 3 - 量化判定**:

```
判定流程:
├─ Grep#1 = 有匹配（包含 @MapperScan）
│   └─ → RETAIN（建议保留）—— 与 DAO 层强耦合，安全红线 S-05
├─ Grep#6 = 有匹配（依赖 DAO 层）
│   └─ → RETAIN（建议保留）
├─ Grep#5 = 有匹配（依赖 Service 层）
│   └─ → RETAIN（建议保留）
├─ Grep#4 = 有匹配 且 注入的是本模块 Service/DAO
│   └─ → RETAIN（建议保留）
├─ Grep#2 = 有匹配（包含 @ComponentScan）
│   ├─ 扫描路径包含本模块特定包（如 grp.pt.service、grp.pt.dao）
│   │   └─ → RETAIN（建议保留，迁移后扫描可能失效）
│   └─ 扫描路径为通用前缀（如 grp.pt）或无显式路径
│       └─ → EVALUATE（需人工判断）
├─ Grep#1~#6 = 全部无匹配（纯通用配置类）
│   └─ → EXTRACT（推荐提取）
│   典型类：Swagger/SpringDoc 配置、CORS 配置、Jackson 序列化配置、
│          WebMvcConfigurer 实现、ThreadPoolTaskExecutor 配置
└─ Grep#3 = 有匹配（启用 Spring 特性）且无 Service/DAO 依赖
    └─ → EXTRACT（推荐提取）—— @EnableCaching 等特性注解可安全迁移
```

**@ComponentScan 路径分析方法**:
1. 使用 Grep 搜索 `basePackages\s*=\s*\{?\s*"` 提取扫描路径值
2. 判断扫描路径是否包含模块特定关键词（service、dao、mapper、impl）：
   - 包含 → RETAIN（保留）
   - 不包含或路径为 `grp.pt` 通用前缀 → EVALUATE（人工判断）
3. 如无 `basePackages` 属性（使用默认扫描路径）→ EXTRACT（推荐提取）

---

## 检查报告汇总规则

检查完成后，按以下规则汇总：

### 汇总统计表

对每个包类型，统计三类判定结果的文件数：

| 包名 | 总文件数 | EXTRACT | EVALUATE | RETAIN |
|------|---------|---------|----------|--------|
| util/ | N | n1 | n2 | n3 |
| cache/ | N | n1 | n2 | n3 |
| ... | ... | ... | ... | ... |

### 文件明细表

对每个文件，输出判定依据：

| 文件 | 判定结果 | Service依赖 | DAO依赖 | 注入依赖 | 跨模块引用数 | 判定原因 |
|------|---------|------------|---------|---------|------------|---------|
| XxxUtil.java | EXTRACT | 无 | 无 | 无 | 3 | 纯工具类，无外部依赖 |
| YyyCache.java | RETAIN | 无 | 有 | @Autowired XxxMapper | 1 | 缓存加载器依赖DAO |

### 输出格式

参见 [../examples/check-report.md](../examples/check-report.md) 的完整报告模板。

---

## 判定一致性保证措施

为确保同一代码在多次执行中获得一致的判定结果，遵循以下原则：

1. **Grep 结果优先于语义理解**: 判定依据必须基于 Grep 搜索的实际匹配结果，不依赖对代码"含义"的主观理解
2. **量化阈值固定**: 跨模块引用数阈值固定为 2（>=2 个不同模块引用才考虑 EVALUATE）
3. **白名单/黑名单明确**: 通用组件白名单和安全红线黑名单在上方已明确列出
4. **判定流程严格顺序**: 每个检查项的判定流程按自上而下顺序执行，命中第一个分支即确定结果，不回溯
5. **模块去重计数方法**: 统计跨模块引用数时，按一级模块目录去重（如 `element-service` 和 `element-controller` 算两个不同模块）
