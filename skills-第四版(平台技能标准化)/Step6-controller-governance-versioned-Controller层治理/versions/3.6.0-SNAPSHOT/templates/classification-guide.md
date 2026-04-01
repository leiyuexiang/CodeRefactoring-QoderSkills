# Controller 确定性分类指南

## 确定性保证

本分类指南采用三级确定性分类链，**不包含任何主观判断**。给定相同的输入文件，分类结果始终一致。

---

## 门控检查（Gate Check）

在进入分类链之前，每个 Java 文件必须通过门控检查：

### 门控准入条件

| 条件 | 结果 |
|------|------|
| 文件包含 `@FeignClient` 注解 | **跳过** → 不是 Controller，不迁移（**最先检查，优先级最高**） |
| 文件包含 `@RestController` 注解（非注释行） | **通过** → 进入分类链 |
| 文件包含 `@Controller` 注解（非注释行） | **通过** → 进入分类链 |
| 文件不含上述任何注解 | **跳过** → 非 Controller 类，不迁移，保留原位 |

### 门控排除条件（显式排除清单）

> **以下条件为强制排除项，命中任一项即排除，不进入分类链。**

| 编号 | 排除条件 | 说明 | 真实案例 |
|------|---------|------|---------|
| GC-EXCL-01 | 文件包含 `@FeignClient` 注解 | Feign 客户端不是 Controller | — |
| GC-EXCL-02 | package 路径包含 `rpc` 或 `feignclient` | RPC/Feign 相关包 | — |
| GC-EXCL-03 | `@RestController`/`@Controller` 注解被注释（行首 `//` 或 `/* */` 块内） | 被注释的注解不算有效注解 | `JdSignController.java`（`//@Controller`）、`YySignController.java`（`//@RestController`） |
| GC-EXCL-04 | 文件**仅有** `@Service`/`@Component`/`@Repository` 而**无**有效的 `@RestController`/`@Controller` | 即使文件名含 "Controller" 也不是 Controller | `GXTPSControllerExt.java`（`@Service`）、`UserLogController.java`（`@Service`） |
| GC-EXCL-05 | 文件无任何 Spring 注解（类声明前无 `@RestController`、`@Controller`、`@Service` 等） | 普通 Java 类，不是 Spring Bean | `BpmController.java`（无任何注解） |
| GC-EXCL-06 | 文件内容全部被注释（整个类代码被 `//` 或 `/* */` 注释） | 备份文件或已废弃文件 | `UserControllerTrmp.java`（全文注释） |

### 门控检查精确正则模式（GATE-REGEX）

> **以下正则表达式为门控检查的唯一判定标准，AI 必须使用这些精确正则执行 Grep，不得自行构造替代正则。**

| 编号 | 用途 | Grep 正则模式 | 说明 |
|------|------|-------------|------|
| GATE-REGEX-01 | 检测有效 @RestController | `^\s*@RestController` | 行首可有空白，但不能有 `//` 或 `*` |
| GATE-REGEX-02 | 检测有效 @Controller | `^\s*@Controller\b` | `\b` 防止误匹配 @ControllerAdvice 等 |
| GATE-REGEX-03 | 检测被注释的注解 | `^\s*//.*@(Rest)?Controller` | 行首 `//` 注释内含注解 |
| GATE-REGEX-04 | 检测块注释内的注解 | `^\s*\*.*@(Rest)?Controller` | `/* */` 块注释内含注解 |
| GATE-REGEX-05 | 检测 @FeignClient | `^\s*@FeignClient` | 排除 Feign 客户端 |
| GATE-REGEX-06 | 检测 @Service | `^\s*@Service\b` | 识别仅有 @Service 的文件 |
| GATE-REGEX-07 | 检测 @Component | `^\s*@Component\b` | 识别仅有 @Component 的文件 |
| GATE-REGEX-08 | 检测 @Repository | `^\s*@Repository\b` | 识别仅有 @Repository 的文件 |
| GATE-REGEX-09 | 检测 package 路径含 rpc/feignclient | `package\s+.*\.(rpc|feignclient)\.` | 排除 RPC/Feign 包路径 |

### 门控检查可执行伪代码（GATE-ALGORITHM）

> **以下伪代码为门控检查的唯一执行流程，AI 必须严格按此顺序执行，不得跳步或重排。**

```
FUNCTION gateCheck(filePath):
    content = Read(filePath)
    
    // Step G1: 提取 package 声明
    packageLine = content 中匹配 "^package\s+(.*);$" 的行
    packagePath = packageLine 中提取的包路径
    
    // Step G2: 排除 RPC/Feign 包路径（GC-EXCL-02）
    IF packagePath 包含 ".rpc." 或 ".feignclient."
        RETURN { result: "EXCLUDE", reason: "GC-EXCL-02: package含rpc/feignclient" }
    
    // Step G3: 检查 @FeignClient（GC-EXCL-01，最高优先级排除）
    feignMatches = Grep(filePath, GATE-REGEX-05)
    IF feignMatches 非空
        RETURN { result: "EXCLUDE", reason: "GC-EXCL-01: 含@FeignClient" }
    
    // Step G4: 检查有效的 @RestController（非注释行）
    restControllerMatches = Grep(filePath, GATE-REGEX-01)
    commentedRestMatches = Grep(filePath, GATE-REGEX-03) + Grep(filePath, GATE-REGEX-04)
    hasValidRestController = (restControllerMatches 非空) AND 
                             (restControllerMatches 中存在至少一行不在 commentedRestMatches 中)
    
    // Step G5: 检查有效的 @Controller（非注释行）
    controllerMatches = Grep(filePath, GATE-REGEX-02)
    commentedControllerMatches = Grep(filePath, GATE-REGEX-03) + Grep(filePath, GATE-REGEX-04)
    hasValidController = (controllerMatches 非空) AND 
                         (controllerMatches 中存在至少一行不在 commentedControllerMatches 中)
    
    // Step G6: 判定结果
    IF hasValidRestController
        annotationLine = restControllerMatches 中第一个非注释行的行号
        RETURN { result: "ADMIT", annotation: "@RestController", line: annotationLine }
    ELSE IF hasValidController
        annotationLine = controllerMatches 中第一个非注释行的行号
        RETURN { result: "ADMIT", annotation: "@Controller", line: annotationLine }
    ELSE
        // Step G7: 记录排除原因
        IF commentedRestMatches 非空 OR commentedControllerMatches 非空
            RETURN { result: "EXCLUDE", reason: "GC-EXCL-03: 注解被注释" }
        serviceMatches = Grep(filePath, GATE-REGEX-06)
        componentMatches = Grep(filePath, GATE-REGEX-07)
        repositoryMatches = Grep(filePath, GATE-REGEX-08)
        IF serviceMatches 非空 OR componentMatches 非空 OR repositoryMatches 非空
            RETURN { result: "EXCLUDE", reason: "GC-EXCL-04: 仅有@Service/@Component/@Repository" }
        RETURN { result: "EXCLUDE", reason: "GC-EXCL-05: 无任何Spring注解" }
```

### 门控检查执行约束

**注意**：
- 门控检查通过 Grep 搜索注解来判断，**不依赖类名模式**
- **文件名包含 "Controller" 不等于它是 Controller**：判定标准唯一且仅为是否存在未被注释的 `@RestController` 或 `@Controller` 注解
- **被注释掉的注解（如 `//@RestController`、`// @Controller`）不算有效注解，视同不含注解，跳过该文件**
- Grep 时必须使用 GATE-REGEX 表中的精确正则，不得自行简化或替代
- **仅有 `@Service`、`@Component`、`@Repository` 的文件不是 Controller**：即使它们的文件名以 "Controller" 结尾，也必须排除
- 每个通过门控的文件必须记录有效注解的行号（如 "L30: @RestController"），每个被排除的文件必须记录排除原因
- **AI 必须对每个候选文件执行完整的 GATE-ALGORITHM 伪代码，不得基于文件名、目录位置或上下文推断跳过任何步骤**

---

## 三级确定性分类链

通过门控检查的 Controller 文件，依次经过三级分类链。**首个命中即停止**，不再继续后续级别。

```
Controller 文件（已通过门控）
    │
    ▼
Level 1: 精确类名映射（最高优先级）
    在 EXACT_CLASS_MAP 中查找 className
    命中 → 返回 {category, subdirectory}，停止
    未命中 → 进入 Level 2
    │
    ▼
Level 2: 关键词模式匹配（中优先级）
    扫描 className + 原始 package 路径（按表顺序逐条匹配）
    首个命中 → 返回 {common, mapped_subdirectory}，停止
    全未命中 → 进入 Level 3
    │
    ▼
Level 3: 默认归入 custom（最低优先级）
    → 返回 {custom, extractBusinessGroup(originalPackage)}
```

---

## Level 1: 精确类名映射表（EXACT_CLASS_MAP）

精确类名映射用于处理无法被 Level 2 关键词覆盖的特殊情况。当前为空表，预留扩展位。

| 精确类名 | 目标分类 | 目标子目录 | 备注 |
|---------|---------|-----------|------|
| （当前无特殊映射） | — | — | — |

**扩展规则**：
- 仅当 Level 2 关键词无法正确分类某个 Controller 时，才将其加入此表
- 此表为 append-only（只增不删），一旦加入则分类固定
- 精确匹配 Java 类名（不含包路径，不含 .java 后缀）

---

## Level 2: 关键词→common 映射表（KEYWORD_TO_COMMON_MAP）

按优先级从高到低排列。对每个 Controller，依次检查表中每行。**首个命中即停止**。

匹配逻辑：在指定的"匹配源"中进行大小写不敏感的包含匹配。

| 优先级 | 关键词 | 匹配源 | → common/ 子目录 | 匹配说明 |
|--------|--------|--------|-----------------|---------|
| 1 | `Sso` | className | `sso` | 类名包含 Sso（如 SsoConfigController） |
| 2 | `ssoplatform` | package | `sso` | 包路径包含 ssoplatform |
| 3 | `ServerInfo` | className | `monitor` | 类名包含 ServerInfo |
| 4 | `OracleServer` | className | `monitor` | 类名包含 OracleServer |
| 5 | `monitoringcenter` | package | `monitor` | 包路径包含 monitoringcenter |
| 6 | 正则 `^I[A-Z].*Controller\d*$` | className | `api` | I开头+大写字母+Controller结尾的接口（如 IMenuController、IUiViewController2） |

**关键规则**：
- Level 2 仅用于识别 **common** 类型。如果未命中任何关键词，文件落入 Level 3 归为 custom
- 匹配按表中优先级顺序执行，首个命中即停止
- 正则匹配（优先级6）用于识别所有 I*Controller 格式的接口定义类

### Level 2 不匹配的情况（全部落入 Level 3 归为 custom）

以下类型虽然看似"框架级"，但统一归入 custom（因为它们是面向前端的业务操作接口）：
- 缓存管理 Controller（CacheController、CacheConfigController）→ custom
- 数据库同步 Controller（DbSyncController、DbTableController）→ custom
- 日志查看 Controller（OperLogController、LoginFileController）→ custom
- 配置检查 Controller（ConfigDataCheckController）→ custom
- 查询配置 Controller（QueryConfigController）→ custom
- 短信认证 Controller（SmsAuthEnableController）→ custom
- 自定义SQL Controller（CustomSqlController）→ custom

---

## Level 3: 默认归入 custom + 业务分组提取

所有未被 Level 1 和 Level 2 命中的 Controller，一律归入 `custom/{businessGroup}/`。

### 绝对禁止语义归并/重命名/简化（L3-PROHIBIT）

> **以下三条为安全红线 S-11 的具体实施规则，违反任何一条等同于违反 C-06 确定性保证。**

| 编号 | 禁止规则 |
|------|---------|
| L3-PROHIBIT-01 | businessGroup **必须严格等于** `toLowerCase(packageSegments[indexOf("config"\|"config2") + 1])` 的结果。不允许基于类名含义、目录相似性、已存在目录名称等**任何理由**进行重命名、简化、合并或语义推断 |
| L3-PROHIBIT-02 | 即使工程中已存在名称相似的目录（如已有 `view/`），也**不得**将 `gapuiview` 归并到 `view/`。每个原始包段产生的 businessGroup **独立建目录** |
| L3-PROHIBIT-03 | 即使原始包名存在明显拼写错误（如 `eleprotalststemcontroller`），也**必须原样转小写**，不做任何语义修正、缩写或简化 |

### 业务分组名提取公式（extractBusinessGroup）

```
输入：原始 package 声明

步骤：
1. 将 package 按 "." 分割为段数组
2. 查找段 "config" 或 "config2" 的位置索引 idx
3. 取 idx + 1 位置的段作为 businessGroup
4. 将 businessGroup 转为全小写

公式：businessGroup = toLowerCase(packageSegments[indexOf("config"|"config2") + 1])
```

### 提取示例

| 原始 package | config/config2 位置 | 提取的段 | businessGroup |
|-------------|-------------------|---------|---------------|
| `grp.pt.frame.config.cachemanager.controller.common` | config (idx=3) | cachemanager | `cachemanager` |
| `grp.pt.frame.config2.work.controller.custom` | config2 (idx=3) | work | `work` |
| `grp.pt.frame.config.messageRemind.controller.custom` | config (idx=3) | messageRemind | `messageremind` |
| `grp.pt.frame.config.whiteList.controller.custom` | config (idx=3) | whiteList | `whitelist` |
| `grp.pt.frame.config.portalSystem.controller.custom` | config (idx=3) | portalSystem | `portalsystem` |
| `grp.pt.frame.config.personnelTransferInfo.controller.custom` | config (idx=3) | personnelTransferInfo | `personneltransferinfo` |
| `grp.pt.frame.config2.dataCheck.controller.custom` | config2 (idx=3) | dataCheck | `datacheck` |
| `grp.pt.frame.config.eleprotalststemcontroller.controller.custom` | config (idx=3) | eleprotalststemcontroller | `eleprotalststemcontroller` |

> **说明**：即使原始包名存在明显拼写错误（如 `eleprotalststemcontroller`），也严格按公式提取并转小写，**不做语义修正或简化重命名**。如需修正包名，应在前置步骤（S1~S3）中处理，S4 仅执行 Controller 归位。

### 反面示例（禁止行为）

以下为实际发生过的错误分类案例，**必须避免**：

| Controller 类 | 原始包段 | 错误做法（禁止） | 错误原因 | 正确做法 |
|--------------|---------|----------------|---------|---------|
| GapUiViewController | `config.gapuiview` | `custom/view/` | 语义推断：将 gapuiview "简化"为 view | `custom/gapuiview/` |
| GapUiViewGroupController | `config.gapuiviewgroup` | `custom/view/` | 关联归并：因与 GapUiViewController 相关而合并 | `custom/gapuiviewgroup/` |
| ElePortalSysController | `config.eleprotalststemcontroller` | `custom/eleportal/` | 拼写纠错：将拼写错误的包名"修正" | `custom/eleprotalststemcontroller/` |
| MailController | `config.mail` | `custom/notification/` | 同义替换：将 mail 替换为更通用的 notification | `custom/mail/` |

> **核心原则**：businessGroup 是公式输出值，不是语义值。AI 不得对公式输出做任何二次处理。

### 边界情况处理

> **以下边界情况的处理公式为确定性规则，必须严格执行，不得凭语义推断替代公式计算。**

| 场景 | 处理方式 | 确定性公式 |
|------|---------|-----------|
| package 中不含 config/config2，但含 `controller` 段 | 取 `controller` 段之前的最后一个段，转小写 | `businessGroup = toLowerCase(segments[indexOf("controller") - 1])` |
| package 中不含 config/config2，也不含 `controller` 段 | 取 package 最后一段，转小写 | `businessGroup = toLowerCase(segments[segments.length - 1])` |
| config/config2 之后没有业务段（直接是 controller） | 用 className 去掉 `Controller` 后缀，转小写 | `businessGroup = toLowerCase(className.replace("Controller", ""))` |
| 提取结果为 `icontroller` | 该包下的类应是 I*Controller 接口，已被 L2 正则命中归入 common/api，不会到达 L3。若存在不匹配 L2 正则的 Controller（如 HelperController），则按 L3 处理 | `businessGroup = "icontroller"` |
| **Controller 直接在 config/{business}/ 下（无 controller 子包）** | 正常处理：先过门控检查（有 @RestController/@Controller），再运行三级分类链 | 标准公式 |

### 边界情况示例（确保可重现）

| 原始 package | 适用场景 | businessGroup | 目标 package |
|-------------|---------|---------------|-------------|
| `grp.pt.frame.bpm.controller.custom` | 含 controller 段，不含 config | `bpm` | `grp.pt.frame.controller.custom.bpm` |
| `grp.pt.frame.view` | 不含 config 也不含 controller | `view` | `grp.pt.frame.controller.custom.view` |
| `grp.pt.frame.install` | 不含 config 也不含 controller | `install` | `grp.pt.frame.controller.custom.install` |
| `grp.pt.frame.config.ssoplatform` | 标准 config 路径，L2 命中 | → L2 `common/sso` | `grp.pt.frame.controller.common.sso` |

> **注意**：非 config/config2 包路径的 Controller 同样需要被步骤 1.1.3 的兜底扫描发现。如果 `view/` 目录下有合法 Controller（通过门控检查），它**必须被迁移**，不得遗漏。

---

## 包路径转换公式（集中重构模式）

确定分类结果 {category, subdirectory} 后，计算目标 package 路径：

### 转换算法

```
输入：
  - originalPackage: 原始 package 声明
  - category: custom 或 common
  - subdirectory: 业务分组名或功能域名

步骤：
1. 将 originalPackage 按 "." 分割为段数组
2. 查找段 "config" 或 "config2" 的位置索引 idx
3. modulePrefix = 段数组[0..idx-1] 拼接（即 config/config2 之前的部分）
4. targetPackage = modulePrefix + ".controller." + category + "." + subdirectory

输出：targetPackage
```

### 转换示例

| 原始 package | modulePrefix | 分类结果 | 目标 package |
|-------------|-------------|---------|-------------|
| `grp.pt.frame.config.user.controller.custom` | `grp.pt.frame` | {custom, user} | `grp.pt.frame.controller.custom.user` |
| `grp.pt.frame.config.icontroller` | `grp.pt.frame` | {common, api} | `grp.pt.frame.controller.common.api` |
| `grp.pt.frame.config.ssoplatform.controller.common` | `grp.pt.frame` | {common, sso} | `grp.pt.frame.controller.common.sso` |
| `grp.pt.frame.config2.monitoringcenter.controller.common` | `grp.pt.frame` | {common, monitor} | `grp.pt.frame.controller.common.monitor` |
| `grp.pt.frame.config2.admin.controller.custom` | `grp.pt.frame` | {custom, admin} | `grp.pt.frame.controller.custom.admin` |
| `grp.pt.frame.config.bpm.controller.custom` | `grp.pt.frame` | {custom, bpm} | `grp.pt.frame.controller.custom.bpm` |
| `grp.pt.frame.config2.userrole.controller.custom` | `grp.pt.frame` | {custom, userrole} | `grp.pt.frame.controller.custom.userrole` |
| `grp.pt.frame.view` | `grp.pt.frame` | {custom, view} | `grp.pt.frame.controller.custom.view` |

### 边界情况（含 modulePrefix 提取规则）

> **以下三种情况覆盖所有可能的包路径格式，确保任何来源的 Controller 都能计算出唯一确定的目标包路径。**

| 场景 | modulePrefix 提取方式 | 完整示例 |
|------|---------------------|---------|
| **情况 A**：package 含 `config` 或 `config2` 段 | `modulePrefix = segments[0..indexOf("config"\|"config2")-1]` | `grp.pt.frame.config.user.controller` → `grp.pt.frame` |
| **情况 B**：不含 config/config2，但含 `controller` 段 | `modulePrefix = segments[0..indexOf("controller")-1]` | `grp.pt.frame.bpm.controller.custom` → `grp.pt.frame.bpm`¹ |
| **情况 C**：不含 config/config2，也不含 `controller` 段 | `modulePrefix = segments[0..length-2]`（去掉最后一段） | `grp.pt.frame.view` → `grp.pt.frame` |
| 原始 package 已是 `xxx.controller.custom.yyy` 格式 | 仍按公式重算，结果与当前一致则标记 SKIP | `grp.pt.frame.controller.custom.user` → 无需迁移 |

¹ 注意：情况 B 中 modulePrefix 会包含原始 controller 段之前的所有段，可能与情况 A 的 modulePrefix 不同。需确认目标路径是否与工程整体 modulePrefix 一致。

---

## 完整分类管线可执行伪代码（CLASSIFICATION-PIPELINE）

> **以下伪代码为完整分类管线的唯一执行流程。AI 必须对每个通过门控检查的 Controller 文件严格按此算法执行，不得跳步、合并或简化。相同输入必须产出相同输出。**

```
FUNCTION classifyController(filePath, className, originalPackage):
    // ========================================
    // Phase A: Level 1 精确类名映射
    // ========================================
    IF className IN EXACT_CLASS_MAP
        entry = EXACT_CLASS_MAP[className]
        RETURN {
            level: "L1",
            rule: "L1: 精确映射 " + className + " → " + entry.subdirectory,
            category: entry.category,
            subdirectory: entry.subdirectory
        }
    
    // ========================================
    // Phase B: Level 2 关键词→common 映射（按优先级顺序逐条匹配）
    // ========================================
    classNameLower = toLowerCase(className)
    
    // L2-P1: 类名包含 Sso（大小写不敏感）
    IF classNameLower CONTAINS "sso"
        RETURN { level: "L2", rule: "L2-P1: className含Sso→sso", category: "common", subdirectory: "sso" }
    
    // L2-P2: 包路径包含 ssoplatform（大小写不敏感）
    IF toLowerCase(originalPackage) CONTAINS "ssoplatform"
        RETURN { level: "L2", rule: "L2-P2: package含ssoplatform→sso", category: "common", subdirectory: "sso" }
    
    // L2-P3: 类名包含 ServerInfo（大小写不敏感）
    IF classNameLower CONTAINS "serverinfo"
        RETURN { level: "L2", rule: "L2-P3: className含ServerInfo→monitor", category: "common", subdirectory: "monitor" }
    
    // L2-P4: 类名包含 OracleServer（大小写不敏感）
    IF classNameLower CONTAINS "oracleserver"
        RETURN { level: "L2", rule: "L2-P4: className含OracleServer→monitor", category: "common", subdirectory: "monitor" }
    
    // L2-P5: 包路径包含 monitoringcenter（大小写不敏感）
    IF toLowerCase(originalPackage) CONTAINS "monitoringcenter"
        RETURN { level: "L2", rule: "L2-P5: package含monitoringcenter→monitor", category: "common", subdirectory: "monitor" }
    
    // L2-P6: 类名匹配正则 ^I[A-Z].*Controller\d*$
    IF className MATCHES REGEX "^I[A-Z].*Controller\d*$"
        RETURN { level: "L2", rule: "L2-P6: className匹配I*Controller→api", category: "common", subdirectory: "api" }
    
    // ========================================
    // Phase C: Level 3 默认归入 custom
    // ========================================
    businessGroup = extractBusinessGroup(originalPackage, className)
    RETURN {
        level: "L3",
        rule: "L3: 默认custom, businessGroup=" + businessGroup,
        category: "custom",
        subdirectory: businessGroup
    }


FUNCTION extractBusinessGroup(originalPackage, className):
    segments = originalPackage.split(".")
    
    // 情况1: package 包含 "config" 或 "config2"
    configIdx = -1
    FOR i = 0 TO segments.length - 1:
        IF segments[i] == "config" OR segments[i] == "config2"
            configIdx = i
            BREAK  // 取第一个匹配的 config/config2
    
    IF configIdx >= 0
        IF configIdx + 1 < segments.length AND segments[configIdx + 1] != "controller"
            // 标准情况：config 后有业务段
            RETURN toLowerCase(segments[configIdx + 1])
        ELSE IF configIdx + 1 < segments.length AND segments[configIdx + 1] == "controller"
            // 边界情况：config 后直接是 controller，无业务段
            RETURN toLowerCase(className.replace("Controller", ""))
        ELSE
            // config 是最后一段（极端边界）
            RETURN toLowerCase(className.replace("Controller", ""))
    
    // 情况2: 不含 config/config2，但含 "controller"
    controllerIdx = -1
    FOR i = 0 TO segments.length - 1:
        IF segments[i] == "controller"
            controllerIdx = i
            BREAK
    
    IF controllerIdx >= 0 AND controllerIdx > 0
        RETURN toLowerCase(segments[controllerIdx - 1])
    
    // 情况3: 不含 config/config2 也不含 controller
    RETURN toLowerCase(segments[segments.length - 1])


FUNCTION computeTargetPackage(originalPackage, category, subdirectory):
    segments = originalPackage.split(".")
    
    // 情况A: 含 config 或 config2
    configIdx = -1
    FOR i = 0 TO segments.length - 1:
        IF segments[i] == "config" OR segments[i] == "config2"
            configIdx = i
            BREAK
    
    IF configIdx >= 0
        modulePrefix = segments[0..configIdx-1].join(".")
        RETURN modulePrefix + ".controller." + category + "." + subdirectory
    
    // 情况B: 不含 config/config2，含 controller
    controllerIdx = -1
    FOR i = 0 TO segments.length - 1:
        IF segments[i] == "controller"
            controllerIdx = i
            BREAK
    
    IF controllerIdx >= 0
        modulePrefix = segments[0..controllerIdx-1].join(".")
        RETURN modulePrefix + ".controller." + category + "." + subdirectory
    
    // 情况C: 不含 config/config2 也不含 controller
    modulePrefix = segments[0..segments.length-2].join(".")
    RETURN modulePrefix + ".controller." + category + "." + subdirectory
```

### 伪代码验证用例（CLASSIFICATION-TEST-CASES）

> **以下测试用例为伪代码的验证基准。AI 每次执行前可用这些用例自检，确保算法实现正确。**

| # | className | originalPackage | 预期 level | 预期 category | 预期 subdirectory | 预期 targetPackage |
|---|-----------|----------------|-----------|-------------|-----------------|------------------|
| TC-01 | ElementController | grp.pt.frame.config.basedata.controller | L3 | custom | basedata | grp.pt.frame.controller.custom.basedata |
| TC-02 | SsoConfigController | grp.pt.frame.config.ssoplatform | L2(P1) | common | sso | grp.pt.frame.controller.common.sso |
| TC-03 | ServerInfoController | grp.pt.frame.config.monitoringcenter.controller | L2(P3) | common | monitor | grp.pt.frame.controller.common.monitor |
| TC-04 | IMenuController | grp.pt.frame.config.icontroller | L2(P6) | common | api | grp.pt.frame.controller.common.api |
| TC-05 | CacheController | grp.pt.frame.config.cachemanager.controller.common | L3 | custom | cachemanager | grp.pt.frame.controller.custom.cachemanager |
| TC-06 | GapUiViewController | grp.pt.frame.config.gapuiview.controller | L3 | custom | gapuiview | grp.pt.frame.controller.custom.gapuiview |
| TC-07 | ElePortalSysController | grp.pt.frame.config.eleprotalststemcontroller | L3 | custom | eleprotalststemcontroller | grp.pt.frame.controller.custom.eleprotalststemcontroller |
| TC-08 | ViewInfoController | grp.pt.frame.view | L3 | custom | view | grp.pt.frame.controller.custom.view |
| TC-09 | OracleServerController | grp.pt.frame.config2.dbmonitor.controller | L2(P4) | common | monitor | grp.pt.frame.controller.common.monitor |
| TC-10 | AdminMenuController | grp.pt.frame.config2.admin.controller | L3 | custom | admin | grp.pt.frame.controller.custom.admin |
| TC-11 | BpmFlowController | grp.pt.frame.bpm.controller.custom | L3 | custom | bpm | grp.pt.frame.bpm.controller.custom.bpm |
| TC-12 | InstallController | grp.pt.frame.install | L3 | custom | install | grp.pt.frame.controller.custom.install |

---

## 常见 AI 错误目录（COMMON-MISTAKES）

> **以下为多次执行中实际发生的 AI 错误。每次执行 Step6 时，AI 应在开始前阅读此列表并在执行过程中避免这些错误。**

| 编号 | 错误类型 | 错误描述 | 正确做法 | 发生频率 |
|------|---------|---------|---------|---------|
| CM-01 | 语义归并 | 将 `gapuiview` 归入已有的 `view/` 目录 | 严格按公式输出 `gapuiview`，独立建目录 | 高 |
| CM-02 | 拼写纠错 | 将 `eleprotalststemcontroller` 简化为 `eleportal` | 原样转小写，不做修正 | 高 |
| CM-03 | 文件名推断 | 将 `BpmController.java` 视为 Controller 迁移（实际无注解） | 必须 Grep 验证注解，文件名不是判定标准 | 高 |
| CM-04 | 注释注解误判 | 将 `//@RestController` 视为有效注解 | 使用 GATE-REGEX-03/04 排除注释行中的注解 | 中 |
| CM-05 | @Service 误迁 | 迁移仅有 `@Service` 注解的 `UserLogController.java` | GATE-EXCLUDE-04：无有效 Controller 注解则不迁移 | 中 |
| CM-06 | 交叉引用遗漏 | 迁移 A 后未更新 A 内部对已迁移 B 的 import | S-12：双向引用更新，使用 Phase 2 计划表中的映射 | 高 |
| CM-07 | 文件内容截断 | 迁移时仅写入类签名和部分方法，省略了完整内容 | INTEGRITY-01：完整复制原文件，仅改 package 行 | 中 |
| CM-08 | Level 2 误判 | 将 CacheController 归入 common（因为看起来像框架功能） | Level 2 匹配表是封闭列表，Cache 不在其中，应归 L3 custom | 中 |
| CM-09 | 扫描遗漏 | 仅扫描 config/ 下有 controller/ 子包的目录，遗漏直接放在 config/{业务}/ 下的 Controller | 两种放置模式都必须覆盖 | 高 |
| CM-10 | modulePrefix 错误 | 情况 B (不含config但含controller) 时使用了错误的 modulePrefix | 严格按伪代码 computeTargetPackage 执行 | 低 |
