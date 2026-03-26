# Qoder 技能执行标准流程

## 概述

本文档定义了 Java 微服务工程重构的 **Qoder 技能执行标准流程**。整个流程包含 8 个技能，按固定顺序依次执行，从工程级架构重构逐步细化到代码级质量优化，形成完整的工程治理链路。

**核心原则**：所有技能均不改变业务逻辑，只调整工程结构、目录组织、命名规范和代码质量。

---

## 执行顺序总览

| 序号 | 技能名称 | 优先级 | 作用层级 | 核心职责 |
|------|----------|--------|---------|----------|
| 1 | engine-reconstruction-versioned 工程重构 | -- | 工程级 | 四层架构 Maven 工程检查与重构 |
| 2 | 工程架构微调 | -- | 工程级 | 能力层模块重命名（server->controller, server-com->service） |
| 3 | P0-arch-dependency 架构依赖 | P0 严重 | 代码级 | 分层架构依赖检查与修复 |
| 4 | P1-directory-structure 目录结构 | P1 重要 | 代码级 | 目录命名与文件分类检查与修复 |
| 5 | P2-code-organization 代码组织 | P2 优化 | 代码级 | DAO/Model 层代码组织检查与修复 |
| 6 | P3-controller-custom-common Controller接口分离 | P3 优化 | 代码级 | Controller 层 custom/common 接口分离 |
| 7 | P4-service-facade-impl Service接口实现分离 | P4 优化 | 代码级 | Service 层 facade/impl 接口实现分离 |
| 8 | 工程代码优化检查 | -- | 代码级 | SQL注入修复、日志增强、冗余代码清理 |

---

## 流程依赖关系

```
Step 1: 工程重构（四层架构搭建）
    │
    ▼
Step 2: 工程架构微调（模块重命名，依赖 Step 1 产出的标准结构）
    │
    ▼
Step 3: P0 架构依赖（修复分层依赖违规，依赖 Step 2 完成后的模块结构）
    │
    ▼
Step 4: P1 目录结构（修复目录命名与文件分类，依赖 Step 3 依赖关系已正确）
    │
    ▼
Step 5: P2 代码组织（DAO/Model 层重组，依赖 Step 4 目录结构已规范）
    │
    ▼
Step 6: P3 Controller 接口分离（依赖 Step 5 基础目录结构已完成）
    │
    ▼
Step 7: P4 Service 接口实现分离（依赖 Step 6 Controller 层已分离）
    │
    ▼
Step 8: 工程代码优化检查（代码质量优化，依赖 Step 1~7 结构全部就绪）
```

---

## 各步骤详细说明

### Step 1: engine-reconstruction-versioned 工程重构

**技能全名**：`/engine-reconstruction-versioned工程重构`

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

**技能全名**：`/工程架构微调`

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

### Step 3: P0-arch-dependency 架构依赖

**技能全名**：`/P0-arch-dependency-架构依赖`

**触发关键词**：P0检查、P0修复、架构依赖检查、架构依赖修复、分层依赖检查、分层依赖修复

**优先级**：P0 严重 -- 违反分层架构核心依赖规则

**核心职责**：
- 检查并修复 Controller -> Controller 直接依赖（同层耦合）
- 检查并修复 Controller 直接依赖 DAO/Mapper（跳过 Service 层）
- 检查并修复 Controller 注入 ServiceImpl 而非接口（违反面向接口编程）
- 检查 Entity 泄露到 Controller 层（暴露数据库结构）
- 检查跨模块直接类引用（模块间耦合）

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|---------|
| P0-01 | Controller->Controller 直接依赖 | FAIL |
| P0-02 | Controller 直接依赖 DAO/Mapper | FAIL |
| P0-03 | Controller 注入 ServiceImpl 而非接口 | FAIL |
| P0-04 | Entity 泄露到 Controller 层 | WARN |
| P0-05 | 跨模块直接类引用 | WARN |

**执行模式**：检查 -> 生成修复计划 -> 用户确认 -> 逐项修复 -> 验证结果

---

### Step 4: P1-directory-structure 目录结构

**技能全名**：`/P1-directory-structure-目录结构`

**触发关键词**：P1检查、P1修复、目录结构检查、目录结构修复、分类检查、包结构检查、目录整理

**优先级**：P1 重要 -- 目录命名和文件分类规范

**核心职责**：
- 修正目录命名（如 `imp` -> `impl`）
- Service 层接口/实现分离
- DAO 层接口/实现分离
- DTO/VO/Query 分类归档
- 核心四层目录完整性检查
- resources/mapper 目录对应检查

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|---------|
| P1-01 | 目录命名规范（imp->impl） | FAIL |
| P1-02 | Service 层接口/实现分离 | FAIL/WARN |
| P1-03 | DAO 层接口/实现分离 | FAIL/WARN |
| P1-04 | DTO/VO/Query 分类归档 | FAIL/WARN |
| P1-05 | 核心四层目录完整性 | WARN |
| P1-06 | resources/mapper 目录对应 | WARN |

**执行模式**：检查 -> 生成修复计划 -> 用户确认 -> 逐项修复（先 Service 层 -> DAO 层 -> Model 层） -> 验证结果

---

### Step 5: P2-code-organization 代码组织

**技能全名**：`/P2-code-organization-代码组织`

**触发关键词**：P2检查、P2修复、代码组织检查、代码组织修复、目录结构检查、目录重组

**优先级**：P2 优化 -- 代码功能正确但组织结构不够规范

**核心职责**：
- DAO 层 mapper/entity 分离
- Model 层 dto/vo/query 分类
- 公共模块结构检查（config/util/exception 等）
- 接口路径规范检查
- 类命名/属性命名规范检查
- 接口参数/响应格式规范检查
- Bean 命名冲突排查

> **注意**：Controller 层 custom/common 检查已拆分到 P3，Service 层 facade/impl 检查已拆分到 P4。

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|---------|
| P2-01 | DAO 层 mapper/entity 分离 | FAIL/WARN |
| P2-02 | Model 层 dto/vo/query 分类 | FAIL/WARN |
| P2-03 | 公共模块结构 | WARN |
| P2-04 | 接口路径规范 | WARN |
| P2-05 | 类命名规范 | WARN |
| P2-06 | 属性命名规范 | WARN |
| P2-07 | 接口参数规范 | WARN |
| P2-08 | 接口响应规范 | WARN |
| P2-09 | Bean 命名冲突 | FAIL/WARN |

**执行模式**：检查 -> 区分可修复/约束限制 -> 生成修复计划 -> 用户确认 -> 逐项修复 -> 验证结果

---

### Step 6: P3-controller-custom-common Controller接口分离

**技能全名**：`/P3-controller-custom-common-Controller接口分离`

**触发关键词**：P3检查、P3修复、Controller接口分离检查、Controller接口分离修复、custom/common检查、custom/common修复

**优先级**：P3 优化 -- Controller 接口分离

**核心职责**：
- 将 Controller 按外部/内部接口分离为 `custom/` 和 `common/` 两级子目录
- 在各目录内部按业务功能进行二级分组

**分类原则**：

| 分类依据 | 归属目录 |
|---------|---------|
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
|------|--------|---------|
| P3-01 | custom/common 一级目录存在性 | FAIL |
| P3-02 | Controller 归属正确性 | FAIL/WARN |
| P3-03 | 二级业务分组合理性 | WARN |
| P3-04 | 非 controller 包下的 Controller | FAIL |

**执行模式**：检查 -> 生成修复计划 -> 用户确认 -> 迁移 Controller 文件 -> 验证结果

---

### Step 7: P4-service-facade-impl Service接口实现分离

**技能全名**：`/P4-service-facade-impl-Service接口实现分离`

**触发关键词**：P4检查、P4修复、Service接口实现分离检查、Service接口实现分离修复、facade/impl检查、facade/impl修复

**优先级**：P4 优化 -- Service 接口实现分离

**核心职责**：
- 将 Service 接口归入 `facade/` 目录
- 将 Service 实现类归入 `impl/` 目录
- 非 Service 文件（常量、枚举、工具等）保留在原业务子包

**分类原则**：

| 文件类型 | 归属目录 | 判定依据 |
|---------|---------|---------|
| Service 接口 | `service/facade/` | interface 类型，类名含 Service 后缀 |
| Service 实现 | `service/impl/` | 类名含 ServiceImpl 后缀，或带 @Service 注解 |
| 非 Service 文件 | 保留原业务子包 | 常量、枚举、异常、工具等 |

**目标目录结构**：
```
service/
├── facade/                    # 服务接口定义
│   ├── IXxxService.java
│   └── ...
├── impl/                      # 服务实现
│   ├── XxxServiceImpl.java
│   └── ...
└── {business}/                # 非 Service 文件保留原业务子包
```

**检查项清单**：

| 编号 | 检查项 | 严重级别 |
|------|--------|---------|
| P4-01 | facade/ 目录存在性 | FAIL |
| P4-02 | Service 接口归属正确性 | FAIL/WARN |
| P4-03 | Service 实现归属正确性 | FAIL |
| P4-04 | 非 Service 文件处理 | WARN |

**执行模式**：检查 -> 生成修复计划 -> 用户确认 -> 迁移文件 -> 验证结果

---

### Step 8: 工程代码优化检查

**技能全名**：`/工程代码优化检查`

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
| Step 4 依赖 Step 3 | 目录结构调整需在分层依赖正确后进行，避免迁移引入新的依赖违规 |
| Step 5 依赖 Step 4 | DAO/Model 层代码组织依赖基础目录结构已规范 |
| Step 6 依赖 Step 5 | Controller 接口分离在代码组织完成后细化 |
| Step 7 依赖 Step 6 | Service 接口实现分离是最后的结构优化 |
| Step 8 依赖 Step 1~7 | 代码质量优化在所有结构调整完成后执行，避免路径变更导致优化失效 |

### 单步执行 vs 全流程执行

- **全流程执行**：按 Step 1 ~ Step 8 顺序逐步执行，适用于全新工程重构
- **单步执行**：可单独执行某个 Step，但需确保其前置 Step 已完成
- **检查+修复**：每个 P0~P4 技能均支持"仅检查"、"仅修复"、"检查并修复"三种模式

---

## 技能调用命令速查表

| 序号 | 调用命令 | 简要说明 |
|------|---------|---------|
| 1 | `/engine-reconstruction-versioned工程重构` | 四层架构工程重构 |
| 2 | `/工程架构微调` | 能力层模块重命名 |
| 3 | `/P0-arch-dependency-架构依赖` | P0 分层依赖检查与修复 |
| 4 | `/P1-directory-structure-目录结构` | P1 目录结构检查与修复 |
| 5 | `/P2-code-organization-代码组织` | P2 代码组织检查与修复 |
| 6 | `/P3-controller-custom-common-Controller接口分离` | P3 Controller 接口分离 |
| 7 | `/P4-service-facade-impl-Service接口实现分离` | P4 Service 接口实现分离 |
| 8 | `/工程代码优化检查` | 代码质量优化（SQL注入/日志/冗余） |

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
