# 自包含根POM生成规则

## 概述

本文件定义了如何将重构后的工程从"依赖外部平台父POM"转换为"自包含独立项目"。
核心目标：生成一个带完整 `dependencyManagement` 的根POM，使项目可独立编译启动。

## 为什么需要自包含根POM

原始平台工程的模块 POM 通常继承自平台级父POM（如 `grp-platform-server`、`grp-platform-api`），
这些父POM在独立项目中不存在，导致：
- Maven 无法解析 parent POM
- 依赖版本无法继承
- Spring Boot / Spring Cloud 的 BOM 管理缺失
- Application 无法启动

## 生成流程

### Step 1: 收集依赖信息

遍历所有**叶子模块**（非 `packaging=pom`）的 `pom.xml`：

```
对每个叶子模块 POM：
  1. 读取 <dependencies> 中的所有 dependency
  2. 记录: groupId, artifactId, version（如有）, scope, exclusions
  3. 将依赖分为三类：
     a) 内部reactor模块 — artifactId 与本项目中某个叶子模块的 artifactId 一致
     b) 平台内部组件 — groupId = "grp.pt" 但不在 reactor 内
     c) 第三方依赖 — groupId != "grp.pt"
```

### Step 2: 确定版本号

| 依赖分类 | 版本确定规则 |
|----------|-------------|
| 内部 reactor 模块 | 使用 `${grp-pt.version}` |
| 平台内部组件（无显式版本） | 使用 `${grp-pt.version}` |
| 平台内部组件（有显式版本） | 如果版本 = `${grp-pt.version}` 或等于工程版本，用 `${grp-pt.version}`；否则保留原值 |
| 第三方依赖（Spring Boot 管理） | 不加入 dependencyManagement，由 spring-boot-starter-parent 管理 |
| 第三方依赖（Spring Cloud 管理） | 不加入 dependencyManagement，由 spring-cloud-dependencies BOM 管理 |
| 第三方依赖（其他，有显式版本） | 保留原始版本号 |
| 第三方依赖（其他，无显式版本） | 需手动确认或使用常见版本 |

### Step 3: 判断 Spring Boot 管理范围

以下依赖由 `spring-boot-starter-parent` 管理，**不需要**加入 dependencyManagement：
- `org.springframework.boot:*`
- `org.springframework:*`
- `org.projectlombok:lombok`
- `org.apache.httpcomponents:httpclient`
- `org.mybatis:mybatis`（如果使用 mybatis-spring-boot-starter）
- 等等

### Step 4: 判断 Spring Cloud 管理范围

以下依赖由 `spring-cloud-dependencies` BOM 管理：
- `org.springframework.cloud:*`
- `io.github.openfeign:*`

以下依赖由 `spring-cloud-alibaba-dependencies` BOM 管理：
- `com.alibaba.cloud:*`

### Step 5: 构建根POM

使用模板 [templates/standalone-root-pom.xml](../templates/standalone-root-pom.xml)，填充：

1. **基本信息**
   - `parent` → `spring-boot-starter-parent` (版本 `2.3.12.RELEASE`)
   - `groupId` → 工程原始 groupId
   - `artifactId` → `{project-name}-parent`
   - `version` → 工程版本
   - `packaging` → `pom`

2. **modules**
   - 列出所有顶级子目录（`{module}-module`、`grp-common-boot` 等）

3. **properties**
   ```xml
   <properties>
       <grp-pt.version>{工程版本}</grp-pt.version>
       <spring-cloud.version>Hoxton.SR12</spring-cloud.version>
       <spring-cloud-alibaba.version>2.2.7.RELEASE</spring-cloud-alibaba.version>
       <maven.compiler.source>8</maven.compiler.source>
       <maven.compiler.target>8</maven.compiler.target>
   </properties>
   ```

4. **dependencyManagement**
   - Spring Cloud BOM (type=pom, scope=import)
   - Spring Cloud Alibaba BOM (type=pom, scope=import)
   - 所有内部 reactor 模块
   - 所有平台内部组件
   - 所有需要版本管理的第三方依赖

5. **repositories**
   ```xml
   <repositories>
       <repository>
           <id>nexus</id>
           <name>nexus</name>
           <url>http://nexus.ctjsoft.com:8081/nexus/content/groups/public/</url>
           <releases><enabled>true</enabled></releases>
           <snapshots><enabled>true</enabled></snapshots>
       </repository>
   </repositories>
   ```

### Step 6: 清理叶子模块冗余版本

根POM 的 `dependencyManagement` 生成后，遍历所有叶子模块 POM：
- 对 `groupId=grp.pt` 的依赖，如果其版本已在根 POM 管理中，**移除叶子模块中的显式 `<version>` 声明**
- 对第三方依赖，如果已被 Spring Boot/Cloud BOM 或根 POM dependencyManagement 管理，同样移除显式版本

## 注意事项

1. `spring-boot-starter-parent` 的版本应与原工程使用的 Spring Boot 版本一致（通常为 `2.3.12.RELEASE`）
2. `spring-cloud.version` 应与 Spring Boot 版本兼容（`2.3.x` 对应 `Hoxton.SR12`）
3. `spring-cloud-alibaba.version` 应与 Spring Cloud 版本兼容（`Hoxton` 对应 `2.2.x`）
4. 如果原工程使用了不同的 Spring Boot 版本，需要相应调整上述版本号
5. Nexus 仓库地址应根据实际环境调整
