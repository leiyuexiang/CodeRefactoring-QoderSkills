# 架构检查规则清单

## 一、根目录结构检查

| 编号 | 检查项 | 规则 |
|------|--------|------|
| R-01 | 根 POM 存在 | 项目根目录必须存在 `pom.xml` |
| R-02 | 根 POM parent 为 spring-boot-starter-parent | **（独立工程新增）** 根POM的parent必须为 `spring-boot-starter-parent` |
| R-03 | 根 POM 包含 dependencyManagement | **（独立工程新增）** 根POM必须包含完整的 `dependencyManagement` |
| R-04 | 根 POM 包含 repositories | **（独立工程新增）** 根POM必须包含 Nexus 私服仓库配置 |
| R-05 | 业务模块命名 | 业务模块目录必须使用 `{module}-module/` 格式命名 |
| R-06 | 根 POM modules 声明 | 根 `pom.xml` 的 `<modules>` 必须包含所有子模块目录 |

## 二、底座层检查 (grp-common-boot) — 如有

| 编号 | 检查项 | 规则 |
|------|--------|------|
| F-01 | 底座层 POM 类型 | `grp-common-boot/pom.xml` 的 packaging 必须为 `pom` |
| F-02 | 通用模块命名 | 底座层子模块必须使用 `grp-*-com/` 命名模式 |
| F-03 | 底座层 modules 声明 | `grp-common-boot/pom.xml` 的 `<modules>` 必须包含其所有子模块 |

## 三、业务模块结构检查

对每个 `{module}-module/` 执行以下检查：

| 编号 | 检查项 | 规则 |
|------|--------|------|
| M-01 | 底座层目录存在 | **必须存在 `grp-common-{module}/` 目录**（模块级底座层） |
| M-02 | 能力层目录存在 | 必须存在 `grp-capability-{module}/` 目录 |
| M-03 | 聚合层目录存在 | 必须存在 `grp-aggregation-{module}/` 目录 |
| M-04 | 体验层目录命名 | 若存在体验层，必须命名为 `grp-experience-{module}/` |
| M-05 | 模块 POM 存在 | `{module}-module/pom.xml` 必须存在，packaging 为 `pom` |
| M-06 | 模块 POM modules 声明 | 模块 POM 的 `<modules>` 必须包含 `grp-common-{module}`、`grp-capability-{module}`、`grp-aggregation-{module}` |

## 四、能力层检查 (grp-capability-{module})

| 编号 | 检查项 | 规则 |
|------|--------|------|
| C-01 | 能力层 POM 类型 | `grp-capability-{module}/pom.xml` 的 packaging 必须为 `pom` |
| C-02 | API 定义模块命名 | 若存在 API 定义模块，必须命名为 `grp-{module}-api/` |
| C-03 | 接口层模块命名 | Controller 层模块必须命名为 `{module}-server/` |
| C-04 | 实现层模块命名 | 业务实现模块必须命名为 `{module}-server-com/` |
| C-05 | 能力层 modules 声明 | 能力层 POM 的 `<modules>` 必须包含其所有子模块 |
| C-06 | parent 配置 | 能力层子模块的 `<parent>` 必须指向 `grp-capability-{module}` |

## 五、聚合层检查 (grp-aggregation-{module})

| 编号 | 检查项 | 规则 |
|------|--------|------|
| A-01 | 聚合层 POM 类型 | `grp-aggregation-{module}/pom.xml` 的 packaging 必须为 `pom` |
| A-02 | SpringCloud 适配模块 | 必须存在 `{prefix}-springcloud/` 模块 |
| A-07 | parent 配置 | 聚合层子模块的 `<parent>` 必须指向 `grp-aggregation-{module}` |

## 六、体验层检查 (grp-experience-{module}) — 可选

| 编号 | 检查项 | 规则 |
|------|--------|------|
| E-01 | 体验层 POM 类型 | `grp-experience-{module}/pom.xml` 的 packaging 必须为 `pom` |
| E-02 | Feign SDK 模块命名 | Feign SDK 模块应命名为 `{module}-feign-com/` |
| E-03 | parent 配置 | 体验层子模块的 `<parent>` 必须指向 `grp-experience-{module}` |

## 七、POM 配置检查

| 编号 | 检查项 | 规则 |
|------|--------|------|
| P-01 | 版本统一管理 | 所有子模块版本号必须通过父 POM 的 `<version>` 或 `dependencyManagement` 管理 |
| P-02 | packaging 类型 | 容器 POM packaging 必须为 `pom`；叶子模块默认为 `jar` |
| P-03 | relativePath 正确 | 所有 `<parent>` 中的 `<relativePath>` 必须指向正确的父 POM 路径 |
| P-04 | groupId 一致性 | 同一工程内的 groupId 应保持一致 |

## 八、编译验证检查（独立工程新增）

| 编号 | 检查项 | 规则 |
|------|--------|------|
| V-01 | mvn compile 通过 | **（独立工程新增）** 执行 `mvn compile` 必须 BUILD SUCCESS，0 个 ERROR |
| V-02 | mvn package 通过 | **（独立工程新增）** 执行 `mvn package -DskipTests` 必须 BUILD SUCCESS |
| V-03 | API 模块基础依赖 | **（独立工程新增）** `*-api` 模块必须包含 `grp-util-com`、`spring-boot-starter-web`、`swagger-annotations`、`lombok` 等基础依赖 |
| V-04 | dependencyManagement 完整 | **（独立工程新增）** 根POM的 `dependencyManagement` 必须覆盖所有叶子模块中引用的外部依赖 |
| V-05 | 第三方依赖版本声明 | **（独立工程新增）** 非 Spring Boot/Cloud 管理的第三方依赖必须在根POM中显式声明版本 |

详细修复规则 → [scripts/compilation-verification.md](scripts/compilation-verification.md)
