# Step8 公共模块提取 - 安全约束

## 核心原则

- **C-01**: 不修改任何 Java 类的业务逻辑代码（方法体内容不变）
- **C-02**: package 声明与实际目录路径必须一致
- **C-03**: 每次迁移一个文件，迁移后立即验证编译正确性
- **C-04**: 所有操作可逆（使用 copy 而非 move，删除在验证通过后执行）
- **C-05**: 严格按迁移顺序执行：constant → enums → exception → util → cache → config
- **C-06**: Grep 结果优先于语义理解（判定依据必须基于实际匹配结果，不依赖主观推断）

## 安全红线

### S-01: 禁止移动 Spring Bean 类（有条件）

**判定方法**: Grep 搜索 `@Component|@Service|@Repository|@Configuration` in 目标文件

**规则**:
- 如果文件包含上述注解，进一步检查是否被其他配置通过 Bean 名称引用
- Grep 搜索 `@Qualifier\("对应Bean名"\)` 或 `getBean\("对应Bean名"\)` in 整个工程
- **有名称引用** → 禁止移动（除非同步更新所有引用，但 Step8 不负责更新 Bean 名称引用）
- **无名称引用**（仅通过类型注入） → 允许移动（Spring 按类型注入不受文件位置影响，前提是 @ComponentScan 覆盖目标 package）

### S-02: 禁止移动注入本模块 Service/DAO 的类

**判定方法**:
1. Grep 搜索 `@Autowired|@Resource|@Inject` in 目标文件
2. 如果有匹配，进一步检查注入的类型：
   - Grep 搜索注入字段的类型声明行
   - 判断类型是否属于本模块的 Service/DAO 层

**具体判定逻辑**:
```
Step 1: Grep "@Autowired|@Resource|@Inject" in 目标文件
  ├─ 无匹配 → 通过，允许移动
  └─ 有匹配 → Step 2

Step 2: 检查注入字段的 import 语句
  Grep "import grp\.pt\..*\.service\." 或
       "import grp\.pt\.service\." 或
       "import grp\.pt\..*\.(dao|mapper)\." 或
       "import grp\.pt\.(dao|mapper)\." in 目标文件
  ├─ 有匹配 → 禁止移动（注入了本模块 Service/DAO）
  └─ 无匹配 → Step 3

Step 3: 检查注入的是否是通用组件
  注入类型属于通用组件白名单:
    RedisTemplate, StringRedisTemplate, RestTemplate, WebClient,
    JdbcTemplate, NamedParameterJdbcTemplate, ObjectMapper,
    ApplicationContext, Environment, MessageSource,
    ThreadPoolTaskExecutor, TaskScheduler
  ├─ 是 → 允许移动
  └─ 否 → 禁止移动（注入了非通用的其他组件）
```

### S-03: 禁止修改类名（仅移动，不改名）

**规则**: 迁移过程中严禁修改 Java 类名、文件名。文件在来源模块叫什么名字，在 common 模块中就叫什么名字。

### S-04: 配置类迁移需验证 @ComponentScan 覆盖

**判定方法**:
1. 文件被判定为 EXTRACT 后，如果是 config/ 下的文件
2. 检查主启动类（Application.java）的 `@ComponentScan` 或 `@SpringBootApplication(scanBasePackages)` 
3. 确认扫描路径能覆盖 common 模块中的 package

**具体判定逻辑**:
```
Step 1: 找到主启动类
  Grep "@SpringBootApplication" in 整个工程 → 找到 Application.java

Step 2: 检查扫描路径
  Grep "scanBasePackages|@ComponentScan" in Application.java
  ├─ 无显式配置 → 默认扫描启动类所在 package 及其子包
  │   ├─ 启动类 package 为 "grp.pt" → 覆盖 "grp.pt.*"，common 中的 config 可被扫描 → OK
  │   └─ 启动类 package 为 "grp.pt.element" → 覆盖 "grp.pt.element.*"
  │       ├─ common 中的 config package 为 "grp.pt.config" → 不被覆盖 → WARN
  │       └─ common 中的 config package 为 "grp.pt.element.config" → 被覆盖 → OK
  └─ 有显式配置 → 检查配置的扫描路径是否包含 common 中的 package 前缀
      ├─ 包含 → OK
      └─ 不包含 → WARN，在报告中标记需要用户手动添加扫描路径
```

### S-05: 禁止移动 @MapperScan 配置类

**判定方法**: Grep 搜索 `@MapperScan` in 目标文件

**规则**:
- 有匹配 → 绝对禁止移动，无例外
- 原因：@MapperScan 指定 MyBatis Mapper 接口的扫描路径，与 DAO 层物理位置强耦合

### S-06: 禁止覆盖 common 中已存在的同名类

**判定方法**: `Glob: grp-common-{module}/src/main/java/{base-package}/{type}/{ClassName}.java`

**规则**:
- 有匹配（文件已存在） → 停止迁移该文件，标记为冲突
- 无匹配 → 允许迁移

### S-07: 编码保留

**规则**: 迁移文件时必须保持原文件的字符编码格式

**执行方法**:
1. **强制使用 `copy`/`cp` 命令复制文件**（保留原始字节流，包括 BOM 标记）
2. 如需修改 package 声明，**复制后使用 Edit 工具**仅修改 package 行
3. **禁止使用 Read → Write 方式重建文件**（可能导致 BOM 丢失或编码变更）

**编码类型检测**（仅在需要确认时使用）:
```bash
# Windows: 使用 PowerShell 检查 BOM
powershell -Command "Get-Content '文件路径' -Encoding Byte -TotalCount 3"
# UTF-8 BOM: 239 187 191 (EF BB BF)

# Linux: 使用 file 命令
file "文件路径"
# 输出: UTF-8 Unicode (with BOM) text / UTF-8 Unicode text
```

### S-08: 禁止跨 module 边界移动非公共代码

**规则**: Step8 仅处理 6 类公共包（util/cache/constant/enums/exception/config）下的文件。不处理以下目录：
- `service/` — Service 层代码（由 Step5 处理）
- `controller/` — Controller 层代码（由 Step6 处理）
- `dao/` / `mapper/` — 数据访问层代码
- `entity/` / `model/` / `dto/` / `vo/` — 数据模型类（由 Step4 处理）

---

## 允许修改的范围（白名单）

| 允许操作 | 条件 | 说明 |
|----------|------|------|
| 复制文件 | 目标路径无同名文件 | 从来源模块复制到 common 模块 |
| 修改 package 声明 | 仅当 package 路径确实变化时 | 使用 Edit 工具仅修改 package 行 |
| 修改 import 语句 | 仅当被引用类的 package 确实变化时 | 使用 Edit 工具更新 import 行 |
| 修改 pom.xml | 仅添加依赖项 | 添加 common 模块依赖、添加第三方库依赖 |
| 删除来源文件 | 目标文件已验证正确后 | 使用 DeleteFile 工具 |
| 删除空目录 | 目录下无任何文件时 | 使用 Bash rmdir/rd |

## 不允许修改的范围（黑名单）

| 禁止操作 | 检测方法 | 说明 |
|----------|---------|------|
| 修改方法体 | diff 比较来源文件与目标文件 | 不允许修改任何业务逻辑 |
| 修改类名 | 比较文件名 | 仅移动，不改名 |
| 修改方法签名 | diff 比较 | 不允许修改参数/返回值 |
| 修改注解 | diff 比较 | 不允许修改/删除/添加注解 |
| 修改字段定义 | diff 比较 | 不允许修改字段类型/名称/访问修饰符 |
| 删除非空目录 | Glob 检查目录内容 | 目录内仍有文件时禁止删除 |
| 移动非公共代码 | 检查目录路径 | 仅处理 6 类公共包 |

---

## 约束执行优先级

当多个约束冲突时，按以下优先级处理（从高到低）：

1. **S-05** @MapperScan 红线 — 最高优先级，绝对不移动
2. **S-06** 文件名冲突 — 停止迁移该文件
3. **S-02** 注入本模块 Service/DAO — 不移动
4. **S-01** Spring Bean 名称引用 — 不移动
5. **S-04** @ComponentScan 覆盖 — 需验证后决定
6. **S-07** 编码保留 — 使用 copy 命令确保
7. **C-03** 逐文件迁移 — 不批量操作
