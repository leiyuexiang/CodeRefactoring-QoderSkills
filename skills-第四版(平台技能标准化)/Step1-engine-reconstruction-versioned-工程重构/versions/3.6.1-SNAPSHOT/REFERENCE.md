# 独立工程重构 - 3.6.0-SNAPSHOT 版本规则

## 概述

本文件为 **3.6.0-SNAPSHOT** 版本的独立工程重构规则。在原有四层架构重构基础上，**新增自包含根POM生成**，使重构后的项目可独立编译启动，无需依赖外部平台父POM。

包含三大核心功能：

1. **四层架构重构**：将扁平 Maven 工程重构为标准四层架构目录结构
2. **自包含根POM生成**：扫描所有依赖，生成带完整 `dependencyManagement` 的独立根POM
3. **POM链路更新**：更新所有模块的 parent、artifactId、dependency 引用

## 架构定义

| 层级 | 目录前缀 | 作用域 | 职责 |
|------|----------|--------|------|
| 全局底座层 (Global Foundation) | `grp-common-boot/` | 根目录下 | 通用基础设施（日志、工具、数据库等） |
| 模块底座层 (Module Foundation) | `grp-common-{module}/` | `{module}-module/` 下 | 模块级通用底座（业务抽取工具类） |
| 能力层 (Capability) | `grp-capability-{module}/` | `{module}-module/` 下 | 原子业务能力 |
| 聚合层 (Aggregation) | `grp-aggregation-{module}/` | `{module}-module/` 下 | 服务启动编排 |
| 体验层 (Experience) | `grp-experience-{module}/` | `{module}-module/` 下 | 预留扩展（BFF等） |

**重要**：每个 `{module}-module/` 下必须创建四个容器目录：`grp-common-{module}/`、`grp-capability-{module}/`、`grp-aggregation-{module}/`、`grp-experience-{module}/`（体验层如有对应模块则创建）。

目标目录结构详见 → [templates/target-structure.md](templates/target-structure.md)

---

## 安全约束（红线）

详见 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

**铁律：只调整以下三类文件，绝不改变业务逻辑：**
1. `pom.xml`
2. `*.yml` / `*.yaml` / `*.properties`
3. `*.java` 的 `import` 语句和 `package` 声明

## 编码防护规范（强制前置）

在执行任何检查或修复操作之前，必须读取并遵守全局编码防护规范：
→ [shared/encoding-guard.md](../../../shared/encoding-guard.md)

该规范定义了 Windows 环境下防止中文编码被 PowerShell 破坏的事前防护措施。核心要求：
- 文件搜索使用 Grep/Glob 工具，禁止 Bash `grep`/`find`
- 文件读取使用 Read 工具，禁止 Bash `cat`/`type`/`Get-Content`
- 文件修改使用 Edit 工具，禁止 Bash `sed`/`awk`/PowerShell 替换
- 仅 A 类操作（copy/mv/mkdir/rmdir）允许通过 Bash 执行

---

## 重构执行流程

### Phase 0: 预检与备份提醒

1. 提醒用户备份整个工程目录
2. 扫描当前工程结构，列出所有一级和二级目录
3. 列出所有 `pom.xml` 文件及其 `<parent>` 和 `<artifactId>`
4. 识别当前架构组织方式（扁平/按模块）
5. 生成重构计划摘要供用户确认

### Phase 1: 分析源工程结构

扫描源工程，建立模块映射表。

模块识别与归类规则 → [scripts/module-classification.md](scripts/module-classification.md)

### Phase 1.5: 依赖吸收判定

在模块归类完成后、创建目录骨架之前，执行依赖吸收判定：

1. 按业务域分组，统计每个业务域的叶子模块数量
2. 对于叶子模块数量 ≤ 2 的业务域，检查是否满足吸收条件（详见 [scripts/module-classification.md](scripts/module-classification.md) 第五节）
3. 满足条件的模块标记为"被吸收"，其目标位置改为主业务模块的对应层级容器
4. 不满足条件的模块保持独立 `{module}-module/` 容器
5. 将吸收结果写入重构计划摘要，供用户确认

### Phase 2: 创建目标目录骨架

按以下顺序创建目录和容器 POM：
1. 根目录
2. 全局底座层 `grp-common-boot/`（如有通用模块）
3. 业务模块容器 `{module}-module/`
4. **模块级底座层 `grp-common-{module}/`**（每个 module-module 下必须创建）
5. 能力层 `grp-capability-{module}/`
6. 聚合层 `grp-aggregation-{module}/`
7. 体验层 `grp-experience-{module}/`（预留，如有BFF等模块）

> **注意**：`{module}-feign-com` / `{module}-feign-api` 归入**能力层** `grp-capability-{module}/`，而非体验层。

容器 POM 模板 → [templates/](templates/) 目录
- 模块级底座层模板 → [templates/common-pom.xml](templates/common-pom.xml)

### Phase 3: 移动源码目录

将各叶子模块移动到目标位置。
- 移动顺序：底座层 → 能力层 → 聚合层 → 体验层
- 每移动一个模块后立即记录日志

### Phase 3.5: 清理旧源目录

**此步骤不可省略。** 所有叶子模块成功移动到目标位置后：

1. **验证完整性**：对比源目录和目标目录的文件数量，确保无遗漏
2. **删除旧目录**：删除根目录下所有已移动的旧源目录（如 `framework-server2/`、`4A-server-api/` 等）
3. **确认干净**：`ls` 根目录，确认仅剩目标结构目录（`{module}-module/`、`grp-common-boot/`、`logs/`、`pom.xml` 等），不存在任何旧源目录残留
4. **记录日志**：列出已删除的旧目录清单

> **为什么必须清理**：旧源目录和新目标目录并存会导致 IDE 混淆、误编辑旧代码、以及 Maven reactor 可能扫描到重复模块。

### Phase 4: 生成自包含根POM（核心新增）

**这是与原版技能的核心区别。** 详细规则 → [scripts/root-pom-generation.md](scripts/root-pom-generation.md)

#### 4.1 扫描所有叶子模块依赖

遍历所有叶子模块（非 `packaging=pom`）的 `pom.xml`，收集全部 `<dependencies>` 中的依赖：
- 记录 `groupId`、`artifactId`、`version`（如有显式声明）
- 区分**内部模块依赖**（reactor 内）和**外部依赖**

#### 4.2 构建 dependencyManagement

按以下规则构建 `<dependencyManagement>` 段：

1. **Spring Cloud BOM** — 引入 `spring-cloud-dependencies` (type=pom, scope=import)
2. **Spring Cloud Alibaba BOM** — 引入 `spring-cloud-alibaba-dependencies` (type=pom, scope=import)
3. **内部 reactor 模块** — 本项目内的所有叶子模块 artifactId，version 用 `${grp-pt.version}`
4. **平台内部组件** — `groupId=grp.pt` 但不在 reactor 内的，version 用 `${grp-pt.version}`（特殊版本的保持原值）
5. **第三方依赖** — 非 `grp.pt` 且不被 Spring Boot/Cloud BOM 管理的，使用原始声明的版本号

#### 4.3 生成根POM

使用模板 → [templates/standalone-root-pom.xml](templates/standalone-root-pom.xml)

关键要素：
- `<parent>` 指向 `spring-boot-starter-parent`（版本 `2.3.12.RELEASE`）
- `<groupId>` 使用工程原始 groupId（如 `grp.pt`）
- `<artifactId>` 使用 `{project-name}-parent`（从工程目录名或原 artifactId 推导）
- `<version>` 使用检测到的工程版本（如 `3.6.0-SNAPSHOT`）
- `<packaging>pom</packaging>`
- `<modules>` 列出所有顶级业务模块容器
- `<properties>` 包含 `grp-pt.version`、`spring-cloud.version`、`spring-cloud-alibaba.version`
- `<dependencyManagement>` 包含 Phase 4.2 构建的完整依赖管理
- `<repositories>` 包含 Nexus 私服仓库地址

### Phase 5: 更新 POM 文件

对每个移动/重命名后的叶子模块，更新其 `pom.xml`：
1. 更新 `<parent>` 指向正确的容器 POM
2. 更新 `<artifactId>`（如有重命名）
3. 更新 `<dependencies>` 中被重命名模块的引用
4. **移除叶子模块中 `grp.pt` 依赖的显式 `<version>` 声明**（已由根 POM 管理）

POM 更新详细规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)

### Phase 6: 更新 Java 文件

仅修改 `package` 声明和 `import` 语句（如包路径因模块重命名而变更）。

### Phase 7: 更新配置文件

更新 `application.yml` / `bootstrap.yml` / `*.properties` 中的路径引用（如有变更）。

### Phase 8: 编译验证与依赖修复（核心新增）

在重构完成后，执行编译验证确保项目可独立编译通过。**此步骤不可跳过。**

详细规则 → [scripts/compilation-verification.md](scripts/compilation-verification.md)

#### 8.0 Java import 预扫描（编译前主动检测）

在执行 `mvn compile` 之前，先扫描所有叶子模块的 Java 源码 import 语句，对照映射表提前识别缺失依赖并补充，减少编译-修复的迭代次数。

具体方法：
1. 对每个叶子模块的 `src/main/java/` 执行 Grep 搜索 `^import` 语句
2. 过滤掉项目内部包（`grp.pt.*`、`grp.frame.*`）和 JDK 标准包（`java.*`、`javax.xml.*`）
3. 对照 `compilation-verification.md` 中的 **Step 0 映射表**，补充缺失依赖到根POM和叶子模块POM

#### 8.1 运行全量编译

执行 `mvn compile` 收集所有编译错误。

#### 8.2 分析依赖缺失

对编译错误按模块分组，识别缺失的依赖包类型：
- **平台工具包缺失**（如 `grp.pt.core.*`、`grp.pt.util.*`）→ 添加 `grp-util-com` 依赖
- **Spring 框架类缺失**（如 `spring-jdbc`、`spring-web`、`spring-context`、`spring-test`）→ API 模块用轻量 `spring-*`，非 starter
- **第三方库缺失**（如 `commons-lang3`、`assertj-core`、`swagger-annotations`）→ 添加到 dependencyManagement 和模块 POM
- **测试框架类用于生产代码**（如 `assertj`、`spring-test`、`MockMultipartFile`）→ 添加 `scope=compile`
- **制品重命名不兼容**（如 `cxf-api` 在 CXF 3.x 重命名为 `cxf-core`）→ 同步修改根 POM 和叶子模块 POM 的 artifactId

#### 8.3 修复依赖

按照以下优先级修复：
1. 先在根 POM `dependencyManagement` 中声明版本
2. 再在对应叶子模块 `pom.xml` 中添加依赖（不含 version）
3. 每修复一个模块后立即重新编译验证

#### 8.4 逐模块编译验证

按 Reactor 构建顺序逐模块验证：
1. API 层模块（`*-api`）
2. 实现层模块（`*-server-com`）
3. 控制层模块（`*-server`）
4. 聚合层模块（`*-springcloud`）
5. 体验层模块（`*-feign-com`）

#### 8.5 全量编译确认

执行 `mvn package -DskipTests` 确认完整打包通过。

### Phase 9: 验证

#### 验证清单

| 编号 | 验证项 | 方法 |
|------|--------|------|
| V-01 | relativePath 正确 | 遍历检查 |
| V-02 | 容器 POM packaging=pom | Grep 检查 |
| V-03 | modules 声明与目录一致 | 对比检查 |
| V-04 | 无旧 artifactId 残留 | Grep 全局搜索旧名称 |
| V-05 | 根POM parent 为 spring-boot-starter-parent | 检查根 pom.xml |
| V-06 | 根POM 包含 dependencyManagement | 检查根 pom.xml |
| V-07 | 根POM 包含 repositories | 检查根 pom.xml |
| V-08 | mvn compile 全量通过 | 执行 `mvn compile` 确认 0 error |
| V-09 | mvn package 通过 | 执行 `mvn package -DskipTests` 确认打包成功 |
| V-10 | API 模块依赖完整 | 检查 `*-api` 模块是否包含 `grp-util-com`、`spring-web`、`spring-context`、`jackson-annotations` 等基础依赖 |
| V-11 | 旧源目录已清理 | `ls` 根目录确认无旧模块目录残留（如 `framework-server2/`、`4A-server-api/` 等） |
| V-12 | 依赖吸收正确 | 被吸收的模块已放入主业务模块容器，未创建多余的独立 `{module}-module/` |
| V-13 | 制品重命名兼容 | 检查无已过时/重命名的 Maven 制品（如 `cxf-api`） |

### Phase 10: 工程启动运行验证（可选）

**此步骤在编译验证全部通过后执行**，验证重构后的工程可正常启动。

详细规则 → [scripts/compilation-verification.md](scripts/compilation-verification.md) Step 6

#### 10.1 识别启动类

在聚合层模块（`*-springcloud`）中搜索包含 `@SpringBootApplication` 或 `SpringApplication.run` 的 Java 文件。

#### 10.2 执行启动验证

```bash
mvn spring-boot:run -pl {aggregation-module-path}
```

#### 10.3 启动成功标准

- 控制台出现 `Started XxxApplication in x.xx seconds`
- 无 `APPLICATION FAILED TO START` 错误
- HTTP 端口正常监听

#### 10.4 环境依赖说明

启动验证依赖外部基础设施（数据库、注册中心等），如环境不具备完整条件：
- 可仅验证 `mvn package -DskipTests` 通过
- 将启动验证标记为"待环境就绪后执行"
- 在重构报告中注明原因

---

## 文件索引

### 脚本/规则文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | 架构检查规则清单 |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 重构安全约束 |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | POM/Java/配置更新规则 |
| [scripts/module-classification.md](scripts/module-classification.md) | 模块识别与归类规则 |
| [scripts/root-pom-generation.md](scripts/root-pom-generation.md) | **自包含根POM生成规则（核心新增）** |
| [scripts/compilation-verification.md](scripts/compilation-verification.md) | **编译验证与依赖修复规则（核心新增）** |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/target-structure.md](templates/target-structure.md) | 目标架构目录结构 |
| [templates/standalone-root-pom.xml](templates/standalone-root-pom.xml) | **自包含根POM模板（核心新增）** |
| [templates/common-pom.xml](templates/common-pom.xml) | **模块级底座层容器POM模板（缺陷修复新增）** |
| [templates/module-pom.xml](templates/module-pom.xml) | 业务模块容器POM模板 |
| [templates/capability-pom.xml](templates/capability-pom.xml) | 能力层容器POM模板 |
| [templates/aggregation-pom.xml](templates/aggregation-pom.xml) | 聚合层容器POM模板 |
| [templates/experience-pom.xml](templates/experience-pom.xml) | 体验层容器POM模板 |

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/module-mapping-table.md](examples/module-mapping-table.md) | 模块映射表示例 |
| [examples/refactor-plan.md](examples/refactor-plan.md) | 重构计划确认示例 |
| [examples/refactor-report.md](examples/refactor-report.md) | 重构完成报告示例 |
