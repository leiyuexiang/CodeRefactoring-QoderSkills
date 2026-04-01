# S1 检查规则清单 - 3.6.1-SNAPSHOT

> **本文件为摘要版本。完整规则必须从基线版本读取：`versions/3.6.0-SNAPSHOT/scripts/check-rules.md`**
>
> **强制要求**：执行检查前，必须先读取 `versions/3.6.0-SNAPSHOT/scripts/check-rules.md` 获取以下完整内容：
> - 违规编号判定确定性规则（5级优先级链）
> - S1-01/S1-02/S1-03 的全部强制 Grep 全量扫描指令（含 @Autowired 和 @Resource 两种注入模式）
> - 各项检查的详细判定标准和风险说明

## 违规编号判定确定性规则（强制）

同一个违规在多次执行中**必须**被判定为相同的 S1 编号。判定优先级规则如下：

1. 先检查被注入类的**类型特征**，不是调用场景
2. 匹配规则按以下**严格优先级**顺序判定，命中第一个即停止：

```
判定优先级（从高到低）：

1. 被注入类满足 Controller 类判定标准（C-ID-1/C-ID-2/C-ID-3 任一条件）：
   类名包含 "Controller" 子串、或有 @RestController/@Controller 注解、或包路径含 .controller.
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

**重要：同一个违规不得根据上下文场景判定为不同编号**。

---

## S1-01：Controller→Controller 直接依赖

**违规模式**：Controller 类通过 `@Autowired` 或 `@Resource` 注入了另一个 Controller。

### Controller 类判定标准（强制确定性规则）

判定一个类是否为"Controller 类"时，满足**任一条件**即判定为 Controller 类：
- **C-ID-1**：类名包含 `Controller` 子串（如 `GXTPSControllerExt`、`UserLogController`）
- **C-ID-2**：类上标注了 `@RestController` 或 `@Controller` 注解
- **C-ID-3**：类所在包路径包含 `.controller.`

> 完整的判定标准详见基线版本 `versions/3.6.0-SNAPSHOT/scripts/check-rules.md`

**强制全量扫描指令（不可跳过，必须先于单文件分析执行）**：

```
# 扫描1：import 了 .controller. 包路径的文件
Grep pattern: import.*\.controller\.[A-Z]
Grep path: {controller-module-src-path}
Grep flags: -l

# 扫描2：@Autowired 注入类名含 Controller 的文件
Grep pattern: @Autowired[\s\S]{0,50}[A-Z][a-zA-Z]*Controller[a-zA-Z]*\b
Grep path: {controller-module-src-path}
Grep flags: -l

# 扫描3：@Resource 注入类名含 Controller 的文件
Grep pattern: @Resource[\s\S]{0,50}[A-Z][a-zA-Z]*Controller[a-zA-Z]*\b
Grep path: {controller-module-src-path}
Grep flags: -l
```

**判定标准**：
- Controller 类中存在 `import xxx.controller.XxxController` → **FAIL**
- Controller 类中存在 `@Autowired XxxController` 或 `@Resource XxxController` → **FAIL**
- Controller 方法中调用 `xxxController.someMethod()` → **FAIL**

---

## S1-02：Controller 直接依赖 DAO/Mapper

**违规模式**：Controller 类直接注入了 DAO 或 Mapper 接口，跳过了 Service 层。

**强制全量扫描指令（不可跳过，必须先于单文件分析执行）**：

```
# 扫描1：import 了 .dao. 包路径的文件
Grep pattern: import.*\.dao\.[A-Z]
Grep path: {controller-module-src-path}
Grep flags: -l

# 扫描2：import 了 .mapper. 包路径的文件
Grep pattern: import.*\.mapper\.[A-Z]
Grep path: {controller-module-src-path}
Grep flags: -l
```

**判定标准**：
- Controller 类中存在 `@Autowired XxxDao xxxDao` 或 `@Resource XxxDao xxxDao` → **FAIL**
- Controller 类中存在 `@Autowired XxxMapper xxxMapper` 或 `@Resource XxxMapper xxxMapper` → **FAIL**
- Controller 类中存在 `import xxx.dao.XxxDao` 或 `import xxx.mapper.XxxMapper` → **FAIL**

---

## S1-03：Controller 注入 ServiceImpl 而非 Service 接口

**违规模式**：Controller 注入的是 Service 的实现类而非接口。

**强制全量扫描指令（不可跳过，必须先于单文件分析执行）**：

```
# 扫描1：import 了 .service.impl. 包路径的文件
Grep pattern: import.*\.service\.impl\.
Grep path: {controller-module-src-path}
Grep flags: -l
```

**判定标准**：
- Controller 类中存在 `@Autowired XxxServiceImpl` → **FAIL**
- Controller 类中存在 `import xxx.service.impl.XxxServiceImpl` → **FAIL**
- Controller 类中存在 `@Autowired XxxService xxxService`（其中 XxxService 是具体类而非接口） → **FAIL**
- Controller 类中存在 `import xxx.service.impl.XxxService` → **FAIL**

**重要说明（确定性规则）**：
- 包名包含 `.service.impl.` 的类型，无论类名是否以 `Impl` 结尾，都判定为 S1-03
- S1-03 的修复范围必须覆盖所有匹配项，不允许选择性跳过

---

## S1-04：Entity 泄露到 Controller 层

**判定标准**：
- Controller 方法返回类型为 Entity 类 → **WARN**
- Controller 方法参数类型为 Entity 类 → **WARN**

---

## S1-05：跨模块直接类引用

**判定标准**：
- 跨模块引用非 Feign/API 接口的类 → **WARN**
