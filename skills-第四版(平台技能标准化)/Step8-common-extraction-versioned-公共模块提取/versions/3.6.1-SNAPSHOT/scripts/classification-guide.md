# Step8 公共模块提取 - 文件分类判定指南

## 概述

本文件提供统一的文件分类判定决策树，确保对同一文件在多次执行中获得一致的判定结果（EXTRACT / EVALUATE / RETAIN）。

所有判定**基于 Grep 搜索结果**，不依赖对代码"含义"的主观理解。

---

## 全局决策树

对每个扫描到的 .java 文件，按以下顺序执行判定（命中第一个分支即确定结果，不回溯）：

```
文件: {ClassName}.java

══════════════════════════════════════════════
 Gate 1: 安全红线检查（命中即 RETAIN，不可覆盖）
══════════════════════════════════════════════

 Q1: 文件是否包含 @MapperScan 注解?
     Grep: "@MapperScan" in 当前文件
     ├─ YES → ★ RETAIN（S-05 红线，绝对不移动）
     └─ NO  → 继续 Q2

 Q2: common 模块中是否已存在同名同路径文件?
     Glob: grp-common-{module}/src/main/java/{base-package}/{type}/{ClassName}.java
     ├─ YES → ★ RETAIN（S-06 红线，文件名冲突）
     └─ NO  → 继续 Q3

══════════════════════════════════════════════
 Gate 2: 依赖分析（按依赖类型分流）
══════════════════════════════════════════════

 Q3: 文件是否 import 了本模块 DAO/Mapper 层?
     Grep: "import grp\.pt\..*\.(dao|mapper)\." 或
           "import grp\.pt\.(dao|mapper)\." in 当前文件
     ├─ YES → ★ RETAIN（依赖 DAO 层，与数据访问强耦合）
     └─ NO  → 继续 Q4

 Q4: 文件是否 import 了本模块 Service 层?
     Grep: "import grp\.pt\..*\.service\." 或
           "import grp\.pt\.service\." in 当前文件
     ├─ YES → 继续 Q4a
     └─ NO  → 继续 Q5

   Q4a: 该文件被多少个不同模块引用?
        Grep: "import.*{全限定类名}" in 整个工程，按一级模块目录去重计数
        ├─ >= 2 个模块 → ★ EVALUATE（有 Service 依赖但被多模块使用，需人工判断）
        └─ < 2 个模块  → ★ RETAIN（有 Service 依赖且仅被本模块使用）

══════════════════════════════════════════════
 Gate 3: 注入依赖分析
══════════════════════════════════════════════

 Q5: 文件是否有 @Autowired/@Resource/@Inject 注解?
     Grep: "@Autowired|@Resource|@Inject" in 当前文件
     ├─ YES → 继续 Q5a
     └─ NO  → 继续 Gate 4

   Q5a: 注入的类型是否属于通用组件白名单?
        检查注入字段的类型声明，与白名单对比:

        通用组件白名单:
        ┌──────────────────────────────────────┐
        │ RedisTemplate, StringRedisTemplate   │
        │ RestTemplate, WebClient              │
        │ JdbcTemplate, NamedParameterJdbc...  │
        │ ObjectMapper                         │
        │ ApplicationContext, Environment      │
        │ MessageSource                        │
        │ ThreadPoolTaskExecutor, TaskScheduler│
        │ MongoTemplate                        │
        │ KafkaTemplate                        │
        │ RabbitTemplate, AmqpTemplate         │
        └──────────────────────────────────────┘

        判定方法: 检查文件中注入字段的 import 语句
          - import 来自 org.springframework.* / com.fasterxml.* 等框架包 → 属于通用组件
          - import 来自 grp.pt.* 的本项目包 → 不属于通用组件

        ├─ 全部属于白名单 → 继续 Q5b
        └─ 有任一不属于白名单 → ★ RETAIN（注入了非通用的项目内部组件）

   Q5b: 该文件被多少个不同模块引用?
        Grep: "import.*{全限定类名}" in 整个工程，按一级模块目录去重计数
        ├─ >= 2 个模块 → ★ EVALUATE（注入通用组件 + 多模块引用，需用户确认）
        └─ < 2 个模块  → ★ EXTRACT（注入通用组件 + 单模块引用，可安全提取）

══════════════════════════════════════════════
 Gate 4: config 类特殊检查
══════════════════════════════════════════════

 Q6: 文件是否在 config/ 目录下?
     ├─ NO  → ★ EXTRACT（非 config 类，无 Service/DAO 依赖，无注入 → 推荐提取）
     └─ YES → 继续 Q6a

   Q6a: 文件是否包含 @ComponentScan 注解?
        Grep: "@ComponentScan" in 当前文件
        ├─ YES → 继续 Q6b
        └─ NO  → ★ EXTRACT（无 @ComponentScan 的通用配置类 → 推荐提取）

   Q6b: @ComponentScan 是否指定了包含 service/dao/mapper 的扫描路径?
        Grep: "basePackages.*service\|basePackages.*dao\|basePackages.*mapper" in 当前文件
        ├─ YES → ★ RETAIN（扫描路径与业务模块耦合）
        └─ NO  → ★ EVALUATE（需人工判断迁移后扫描路径是否仍有效）

══════════════════════════════════════════════
```

---

## 判定结果说明

| 判定 | 含义 | 后续动作 |
|------|------|---------|
| **EXTRACT** | 推荐提取到 common 模块 | 自动列入迁移清单，执行 7 步迁移流程 |
| **EVALUATE** | 需人工判断 | 列入报告的"需人工判断"区域，等用户明确确认 |
| **RETAIN** | 建议保留在原模块 | 自动排除出迁移清单，列入报告的"保留"区域 |

---

## 跨模块引用计数方法

### 计数规则

1. 在整个工程目录中搜索：`import {full.package.name}.{ClassName}`
2. 收集所有匹配文件的路径
3. 提取每个匹配文件的一级模块目录名（即工程根目录下的第一级子目录）
4. 去重计数

### 示例

```
搜索: import grp.pt.util.DateUtil

匹配结果:
  grp-capability-element/element-service/src/.../XxxService.java       → 模块: element-service
  grp-capability-element/element-service/src/.../YyyService.java       → 模块: element-service (重复)
  grp-capability-element/element-controller/src/.../XxxController.java → 模块: element-controller
  grp-capability-element/bff-element/src/.../XxxBff.java               → 模块: bff-element

去重后模块数: 3 (element-service, element-controller, bff-element)
```

### 一级模块目录的识别

以 `grp-capability-{module}/` 下的直接子目录为一级模块，例如：
- `grp-capability-element/element-service/` → 模块名: element-service
- `grp-capability-element/element-controller/` → 模块名: element-controller
- `grp-capability-element/bff-element/` → 模块名: bff-element

---

## exception/ 目录的特殊分类

exception/ 目录下可能包含两类文件，需区分处理：

### 类型 A: 异常定义类

**识别方法**: Grep `extends.*Exception|extends.*RuntimeException|extends.*Throwable`

```java
public class BusinessException extends RuntimeException { ... }
public class ElementNotFoundException extends RuntimeException { ... }
```

**判定**: 几乎 100% 为 EXTRACT（异常定义类无外部依赖）

### 类型 B: 全局异常处理器

**识别方法**: Grep `@ControllerAdvice|@RestControllerAdvice|@ExceptionHandler`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBizException(BusinessException e) { ... }
}
```

**判定**: 需进一步检查是否依赖 Service 层：
- 无 Service 依赖 → EXTRACT
- 有 Service 依赖 → RETAIN

---

## 一致性保证清单

以下措施确保同一代码在多次执行中获得一致的判定结果：

| 序号 | 保证措施 | 说明 |
|------|---------|------|
| G-01 | Grep 结果优先 | 判定依据必须基于实际的 Grep/Glob 匹配结果，不凭借对代码功能的"理解" |
| G-02 | 决策树严格顺序 | 从 Gate 1 到 Gate 4 依次执行，命中第一个结果分支即确定，不回溯 |
| G-03 | 阈值固定 | 跨模块引用数阈值固定为 2（>= 2 才考虑 EVALUATE） |
| G-04 | 白名单明确 | 通用组件白名单在上方明确列出，不属于白名单的一律按非通用处理 |
| G-05 | 不使用模糊描述 | 禁止使用"业务耦合度高"、"可能影响"等主观描述作为判定依据 |
| G-06 | 重复执行幂等 | 对已迁移完成的工程再次执行检查，应返回空的待迁移清单 |
| G-07 | 同文件同结果 | 对同一个文件，无论由哪个 AI 会话执行检查，Grep 结果相同则判定结果相同 |
