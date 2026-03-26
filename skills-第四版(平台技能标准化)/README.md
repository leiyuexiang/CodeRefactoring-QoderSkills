# Qoder 技能执行标准流程

## 概述

本文档定义了 Java 微服务工程重构的 **Qoder 技能执行标准流程**。整个流程包含 9 个技能，按固定顺序依次执行，从工程级架构重构逐步细化到代码级质量优化，形成完整的工程治理链路。

**核心原则**：所有技能均不改变业务逻辑，只调整工程结构、目录组织、命名规范和代码质量。

---

## 执行顺序总览

| 序号 | 技能名称 | 优先级 | 作用层级 | 核心职责 |
|------|----------|--------|---------|----------|
| Step1 | 工程重构 | -- | 工程级 | 四层架构 Maven 工程检查与重构 |
| Step2 | 工程架构微调 | -- | 工程级 | 能力层模块重命名（server->controller, server-com->service） |
| Step3 | 架构依赖守卫 | 严重 | 代码级 | 分层架构依赖检查与修复 |
| Step4 | DAO-Model 层治理 | 重要 | 代码级 | DAO/Model 层目录结构与代码组织治理 |
| Step5 | Service 层治理 | 优化 | 代码级 | Service 层 facade/impl 接口实现分离 |
| Step6 | Controller 层治理 | 优化 | 代码级 | Controller 层 custom/common 接口分离 |
| Step7 | 接口与命名规范 | 优化 | 代码级 | 接口路径、类/属性命名、Bean 冲突检查与修复 |
| Step8 | 公共模块提取 | 优化 | 代码级 | 将 util/cache/constant/enums/exception/config 提取到 common 模块 |
| Step9 | 工程代码优化检查 | -- | 代码级 | SQL注入修复、日志增强、冗余代码清理 |

---

## 流程依赖关系

```
Step 1: 工程重构（四层架构搭建）
    │
    ▼
Step 2: 工程架构微调（模块重命名，依赖 Step 1 产出的标准结构）
    │
    ▼
Step 3: 架构依赖守卫（修复分层依赖违规，依赖 Step 2 完成后的模块结构）
    │
    ▼
Step 4: DAO-Model 层治理（DAO/Model 层目录归位，依赖 Step 3 依赖关系已正确）
    │
    ▼
Step 5: Service 层治理（Service 接口/实现分离，依赖 Step 4 底层结构已规范）
    │
    ▼
Step 6: Controller 层治理（Controller 接口分离，依趖 Step 5 Service 层已分离）
    │
    ▼
Step 7: 接口与命名规范（命名/路径/Bean 规范，依赖 Step 6 Controller 层已分离）
    │
    ▼
Step 8: 公共模块提取（将 util/cache 等提取到 common 模块，依赖 Step 7 命名规范已完成）
    │
    ▼
Step 9: 工程代码优化检查（代码质量优化，依赖 Step 1~8 结构全部就绪）
```

---

## 各步骤详细说明

### Step 1: 工程重构

**技能全名**：`/Step1-engine-reconstruction-versioned-工程重构`

**触发关键词**：架构检查、目录规范、结构校验、四层架构审查、架构重构、结构迁移、四层架构改造

**核心职责**：
- 将 Maven 多模块工程重构为标准四层架构：底座层、能力层、聚合层、体验层
- 根据工程 `pom.xml` 中的 `<version>` 自动选择对应版本的重构规则执行
- 支持 3.6.0-SNAPSHOT、3.6.1-SNAPSHOT、3.7.0-SNAPSHOT 等多版本适配

**执行流程**：
1. 自动检测工程版本（读取根 `pom.xml` 的 `<version>` 标签）
2. 按版本映射规则加载对应 REFERENCE.md
3. 执行架构检查或重构

**前置条件**：
- 工程为 Maven 多模块项目
- 根目录存在 `pom.xml`
- 用户已备份工程

**产出结构**：
```
{module}-module/
├── pom.xml
├── grp-common-{module}/                  # 公共层
├── grp-capability-{module}/              # 能力层容器
│   ├── grp-{module}-api/                 # API 定义
│   ├── {module}-server/                  # Controller 层
│   └── {module}-server-com/              # Service 业务实现层
├── grp-aggregation-{module}/             # 聚合层容器
│   └── {module}-server-springcloud/      # SC 适配
└── grp-experience-{module}/              # 体验层容器
    └── {module}-feign-com/               # Feign SDK
```

---

### Step 2: 工程架构微调

**技能全名**：`/Step2-module-rename-工程架构微调`

**触发关键词**：架构微调、模块重命名、server改controller、server-com改service

**核心职责**：
- 将 `{module}-server` 重命名为 `{module}-controller`
- 将 `{module}-server-com` 重命名为 `{module}-service`
- 同步更新所有 POM 引用（artifactId、modules、dependencies、dependencyManagement）
- 同步更新 Java 文件的 package 声明和 import 语句

**前置条件**：
- **必须先执行 Step 1**，确保工程已是标准四层架构结构

**执行流程**：
1. 工程扫描与校验（识别待重命名模块）
2. 用户确认微调计划
3. 目录重命名（先改被依赖方 service，再改依赖方 controller）
4. 更新 POM 文件（自身 POM -> 容器 POM -> 根 POM -> 全局依赖引用）
5. 更新 Java 文件（package + import）
6. 编译验证

**产出结构**：
```
grp-capability-{module}/
├── grp-{module}-api/                     # API 定义（不变）
├── {module}-controller/                  # 原 {module}-server
└── {module}-service/                     # 原 {module}-server-com
```

---

### Step 3: 架构依赖守卫

**技能全名**：`/Step3-arch-dependency-versioned-架构依赖守卫`

**触发关键词**：Step3检查、Step3修复、架构依赖检查、架构依赖修复、分层依赖检查、分层依赖修复

**优先级**：严重 -- 违反分层架构核心依赖规则

**核心职责**：
- 检查并修复 Controller -> Controller 直接依赖（同层耦合）
- 检查并修复 Controller 直接依赖 DAO/Mapper（跳过 Service 层）
- 检查并修复 Controller 注入 ServiceImpl 而非接口（违反面向接口编程）
- 检查 Entity 泄露到 Controller 层（暴露数据库结构）
- 检查跨模块直接类引用（模块间耦合）

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|--------|
| S1-01 | Controller->Controller 直接依赖 | FAIL |
| S1-02 | Controller 直接依赖 DAO/Mapper | FAIL |
| S1-03 | Controller 注入 ServiceImpl 而非接口 | FAIL |
| S1-04 | Entity 泄露到 Controller 层 | WARN |
| S1-05 | 跨模块直接类引用 | WARN |

**执行模式**：检查 -> 生成修复计划 -> 用户确认 -> 逐项修复 -> 验证结果

---

### Step 4: DAO-Model 层治理

**技能全名**：`/Step4-dao-model-governance-versioned-DAO-Model层治理`

**触发关键词**：Step4检查、Step4修复、DAO层治理检查、Model层检查、DAO层治理修复、Model层修复

**优先级**：重要 -- DAO/Model 层目录结构与代码组织

**核心职责**：
- 修正目录命名（如 `imp` -> `impl`）
- DAO 层接口/实现分离
- DTO/VO/Query 分类归档
- 核心四层目录完整性检查
- resources/mapper 目录对应检查
- DAO 层 mapper/entity 分离
- Model 层 dto/vo/query 分类
- 公共模块结构检查

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|--------|
| S2-01 | 目录命名规范（imp→impl） | FAIL |
| S2-02 | DAO 层接口/实现分离 | FAIL/WARN |
| S2-03 | DTO/VO/Query 分类归档 | FAIL/WARN |
| S2-04 | 核心四层目录完整性 | WARN |
| S2-05 | resources/mapper 目录对应 | WARN |
| S2-06 | DAO 层 mapper/entity 分离 | FAIL/WARN |
| S2-07 | Model 层 dto/vo/query 分类 | FAIL/WARN |
| S2-08 | 公共模块结构 | WARN |

**执行模式**：检查 -> 生成修复计划 -> 用户确认 -> 逐项修复 -> 验证结果

---

### Step 5: Service 层治理

**技能全名**：`/Step5-service-governance-versioned-Service层治理`

**触发关键词**：Step5检查、Step5修复、Service层治理检查、Service层检查、Service层治理修复、Service层修复

**优先级**：优化 -- Service 层接口实现分离

**核心职责**：
- Service 层接口/实现分离规范检查
- 将 Service 接口归入 `facade/` 目录
- 将 Service 实现类归入 `impl/` 目录
- 非 Service 文件（常量、枚举、工具等）保留在原业务子包

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|--------|
| S3-01 | Service 层接口/实现分离规范 | FAIL/WARN |
| S3-02 | facade/ 目录存在性 | FAIL |
| S3-03 | Service 接口归属正确性 | FAIL/WARN |
| S3-04 | Service 实现归属正确性 | FAIL |
| S3-05 | 非 Service 文件处理 | WARN |

**执行模式**：检查 -> 生成修复计划 -> 用户确认 -> 迁移文件 -> 验证结果

---

### Step 6: Controller 层治理

**技能全名**：`/Step6-controller-governance-versioned-Controller层治理`

**触发关键词**：Step6检查、Step6修复、Controller层治理检查、Controller接口分离检查、Controller层治理修复、custom/common检查

**优先级**：优化 -- Controller 接口分离

**核心职责**：
- 将 Controller 按外部/内部接口分离为 `custom/` 和 `common/` 两级子目录
- 在各目录内部按业务功能进行二级分组

**分类原则**：

| 分类依据 | 归属目录 |
|---------|--------|
| 接口路径一级路径为 `run/` | `controller/custom/`（外部接口） |
| 接口路径一级路径为 `config/` | `controller/common/`（内部接口） |
| 面向前端 UI 操作的业务接口 | `controller/custom/` |
| 内部 API / 工具 / 调试 / 同步 | `controller/common/` |

**目标目录结构**：
```
controller/
├── custom/                # 外部接口（面向前端/第三方）
│   ├── basedata/          # 基础数据管理
│   ├── bookset/           # 账套管理
│   └── {business}/        # 其他外部业务分组
└── common/                # 内部接口（面向内部微服务）
    ├── api/               # 内部 API 接口
    ├── util/              # 工具/调试类
    └── {function}/        # 其他内部功能分组
```

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|--------|
| S4-01 | custom/common 一级目录存在性 | FAIL |
| S4-02 | Controller 归属正确性 | FAIL/WARN |
| S4-03 | 二级业务分组合理性 | WARN |
| S4-04 | 非 controller 包下的 Controller | FAIL |

**执行模式**：检查 -> 生成修复计划 -> 用户确认 -> 迁移 Controller 文件 -> 验证结果

---

## Step 7: 接口与命名规范

**技能全名**：`/Step7-naming-convention-versioned-接口与命名规范`

**触发关键词**：Step7检查、Step7修复、命名规范检查、接口规范检查、命名规范修复、接口规范修复

**优先级**：优化 -- 接口路径与命名规范

**核心职责**：
- 接口路径规范检查（四级路径、HTTP 方法）
- 类命名规范检查（后缀、大驼峰、长度限制）
- 属性命名规范检查（小驼峰、ID 后缀、布尔前缀）
- 接口参数/响应格式规范检查
- Bean 命名冲突排查
- 区分"可修复"和"约束限制"项

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|--------|
| S5-01 | 接口路径规范 | WARN |
| S5-02 | 类命名规范 | WARN |
| S5-03 | 属性命名规范 | WARN |
| S5-04 | 接口参数规范 | WARN |
| S5-05 | 接口响应规范 | WARN |
| S5-06 | Bean 命名冲突 | FAIL/WARN |

**执行模式**：检查 -> 区分可修复/约束限制 -> 生成修复计划 -> 用户确认 -> 逐项修复 -> 验证结果

---

### Step 8: 公共模块提取

**技能全名**：`/Step8-common-extraction-versioned-公共模块提取`

**触发关键词**：Step8检查、Step8修复、公共模块提取、common提取、util提取、公共代码归集

**优先级**：优化 -- 公共代码统一管理

**核心职责**：
- 将能力层模块中的 6 类公共代码包提取到 `grp-{module}-common` 模块
- 提取范围：`util/`、`cache/`、`constant/`、`enums/`、`exception/`、`config/`
- 分析每个类的依赖关系，区分“推荐提取”、“需人工判断”、“建议保留”
- 同步调整 POM 依赖关系

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|--------|
| S8-01 | util/ 包归属检查 | WARN |
| S8-02 | cache/ 包归属检查 | WARN |
| S8-03 | constant/ 包归属检查 | WARN |
| S8-04 | enums/ 包归属检查 | WARN |
| S8-05 | exception/ 包归属检查 | WARN |
| S8-06 | config/ 包归属检查 | WARN |

**执行模式**：扫描来源模块 -> 依赖分析 -> 生成检查报告 -> 用户确认 -> 逐文件迁移 -> POM 调整 -> 验证结果

---

### Step 9: 工程代码优化检查

**技能全名**：`/Step9-code-optimization-versioned-工程代码优化检查`

**触发关键词**：SQL注入修复、安全修复、参数化查询、日志增强、代码清理、代码优化、工程代码优化、代码质量优化

**核心职责**：
- **SQL 注入修复**（优先级最高）：值拼接参数化、动态表名白名单校验、动态列名正则校验
- **日志增强**：统一 `@Slf4j` 注解、方法入口/异常日志补全
- **代码清理**：StringBuffer->StringBuilder、冗余变量消除、条件简化、空集合检查统一

**筛选规则**：
- 处理：带 `@Service` 或 `@Repository` 注解的 Java 类
- 跳过：文件总行数 > 1000 行的类
- 排除：接口文件、Mapper 接口、Controller、配置类、DTO/Model

**不可变红线**：
1. 不修改类名（包括 Bean 别名）
2. 不修改方法签名（方法名、参数列表、返回类型）
3. 不修改已有的日志语句
4. 不修改业务逻辑的算法流程

**执行流程**：
1. 文件扫描与筛选 -> 输出待处理文件清单
2. 用户确认后开始逐文件处理
3. 按优先级执行：SQL 注入修复 -> 日志增强 -> 代码清理
4. 每文件完成后自检验证
5. 全部完成后生成变更报告

**处理顺序**：先 DAO 层 -> 后 Service 层（SQL 注入修复集中在 DAO）

---

## 执行注意事项

### 通用安全约束

1. **不改变业务逻辑**：所有技能只做结构调整和质量优化，不修改业务代码行为
2. **用户确认机制**：每个技能执行修改前必须生成修复计划，等待用户确认后才执行
3. **备份提醒**：执行重构前确认用户已备份工程
4. **编译验证**：每步完成后执行 Maven 编译验证，确保工程可编译

### 执行顺序不可打乱的原因

| 依赖关系 | 说明 |
|---------|------|
| Step 2 依赖 Step 1 | 架构微调基于 Step 1 产出的标准四层架构结构 |
| Step 3 依赖 Step 2 | 架构依赖检查需在模块命名正确后执行 |
| Step 4 依赖 Step 3 | DAO/Model 层治理需在分层依赖正确后进行，避免迁移引入新的依赖违规 |
| Step 5 依赖 Step 4 | Service 层分离依赖底层 DAO/Model 结构已规范 |
| Step 6 依赖 Step 5 | Controller 接口分离在 Service 层已分离后细化 |
| Step 7 依赖 Step 6 | 命名规范是最后的规范优化，依赖所有结构调整完成 |
| Step 8 依赖 Step 7 | 公共模块提取需在命名规范完成后执行，避免提取后再改名 |
| Step 9 依赖 Step 1~8 | 代码质量优化在所有结构调整完成后执行，避免路径变更导致优化失效 |

### 单步执行 vs 全流程执行

- **全流程执行**：按 Step 1 ~ Step 9 顺序逐步执行，适用于全新工程重构
- **单步执行**：可单独执行某个 Step，但需确保其前置 Step 已完成
- **检查+修复**：每个 Step3~Step8 技能均支持“仅检查”、“仅修复”、“检查并修复”三种模式

---

## 技能调用命令速查表

| 序号 | 调用命令 | 简要说明 |
|------|---------|--------|
| 1 | `/Step1-engine-reconstruction-versioned-工程重构` | 四层架构工程重构 |
| 2 | `/Step2-module-rename-工程架构微调` | 能力层模块重命名 |
| 3 | `/Step3-arch-dependency-versioned-架构依赖守卫` | 分层依赖检查与修复 |
| 4 | `/Step4-dao-model-governance-versioned-DAO-Model层治理` | DAO/Model 层治理 |
| 5 | `/Step5-service-governance-versioned-Service层治理` | Service 层治理 |
| 6 | `/Step6-controller-governance-versioned-Controller层治理` | Controller 层治理 |
| 7 | `/Step7-naming-convention-versioned-接口与命名规范` | 接口与命名规范 |
| 8 | `/Step8-common-extraction-versioned-公共模块提取` | 公共代码提取到 common 模块 |
| 9 | `/Step9-code-optimization-versioned-工程代码优化检查` | 代码质量优化（SQL注入/日志/冗余） |

---

## 适用技术栈

- Java 8+
- Spring Boot 2.7+
- Spring Cloud
- MyBatis 3.5+
- Maven 多模块项目
- SLF4J + Logback

---

## 文档版本

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2026-03-24 | 初始版本，定义 8 步技能执行标准流程 |
| v2.0.0 | 2026-03-25 | 重组 P0~P4 为 S1~S5（按层治理 + 独立命名规范） |
| v3.0.0 | 2026-03-25 | 重命名为 Step1~Step9，新增 Step8 公共模块提取技能 |
