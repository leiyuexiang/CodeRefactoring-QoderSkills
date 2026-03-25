# 编译验证与依赖修复规则

## 概述

独立工程重构完成后，由于根POM从平台父POM切换到 `spring-boot-starter-parent`，原平台父POM提供的传递性依赖不再可用。必须执行编译验证，识别并补全所有缺失依赖。

## 执行流程

### Step 0: Java import 预扫描（编译前主动检测）

**此步骤在 `mvn compile` 之前执行**，通过扫描 Java 源码的 import 语句，提前识别可能缺失的依赖，减少编译-修复的迭代次数。

#### 0.1 扫描方法

对每个叶子模块，使用 Grep 搜索 `src/main/java` 下所有 `.java` 文件的 import 语句：

```bash
# 搜索所有非项目包的 import
grep -rh "^import " {module}/src/main/java/ | sort -u | grep -v "^import grp\." | grep -v "^import java\."
```

#### 0.2 常见缺失 import → 依赖映射表

| import 包前缀 | 所需 artifactId | groupId | Spring Boot 管理 | 需加入根POM DM |
|--------------|----------------|---------|:---------------:|:-------------:|
| `org.springframework.jdbc.*` | `spring-jdbc` | `org.springframework` | 是 | 否 |
| `org.springframework.web.*` | `spring-web` | `org.springframework` | 是 | 否 |
| `org.springframework.context.*` | `spring-context` | `org.springframework` | 是 | 否 |
| `org.springframework.format.*` | `spring-context` | `org.springframework` | 是 | 否 |
| `org.springframework.stereotype.*` | `spring-context` | `org.springframework` | 是 | 否 |
| `org.springframework.transaction.*` | `spring-tx` | `org.springframework` | 是 | 否 |
| `org.springframework.mock.web.*` | `spring-test` (scope=compile) | `org.springframework` | 是 | 否 |
| `com.fasterxml.jackson.annotation.*` | `jackson-annotations` | `com.fasterxml.jackson.core` | 是 | 否 |
| `io.swagger.annotations.*` | `swagger-annotations` | `io.swagger` | **否** | **是** |
| `org.apache.commons.lang3.*` | `commons-lang3` | `org.apache.commons` | 是 | 否 |
| `org.apache.commons.lang.*` | `commons-lang` | `commons-lang` | **否** | **是** |
| `org.apache.commons.collections.*` | `commons-collections` | `commons-collections` | **否** | **是** |
| `org.apache.commons.fileupload.*` | `commons-fileupload` | `commons-fileupload` | **否** | **是** |
| `org.assertj.core.*` | `assertj-core` | `org.assertj` | 是 | 否 |
| `javax.servlet.*` | `javax.servlet-api` (scope=provided) | `javax.servlet` | 是 | 否 |
| `org.mybatis.*` | `mybatis` | `org.mybatis` | **否** | **是** |
| `org.projectlombok.*` | `lombok` | `org.projectlombok` | 是 | 否 |

#### 0.3 预扫描操作步骤

1. 对每个叶子模块执行 import 扫描
2. 对照上表，识别每个模块需要但 POM 中未声明的依赖
3. 将未被 Spring Boot/Cloud BOM 管理的依赖添加到根 POM `dependencyManagement`
4. 将所需依赖添加到对应叶子模块的 `pom.xml`
5. **注意**：对于 `javax.servlet-api`，在非 Web 启动模块中使用 `scope=provided`；对于 `spring-test` 用于生产代码时使用 `scope=compile`

### Step 1: 全量编译收集错误

```bash
mvn compile 2>&1
```

记录所有 `[ERROR]` 行，按模块分组。

### Step 2: 错误分类与诊断

#### 2.1 平台工具包缺失

**典型错误特征：**
- `找不到符号` / `程序包xxx不存在`
- 涉及包名 `grp.pt.core.*`、`grp.pt.util.*`、`grp.pt.util.poi.annotation.*`、`grp.pt.util.model.*`

**缺失类与来源映射：**

| 缺失类/包 | 来源 artifactId | groupId |
|-----------|----------------|---------|
| `grp.pt.core.ReturnData` | `grp-util-com` | `grp.pt` |
| `grp.pt.core.ResultData` | `grp-util-com` | `grp.pt` |
| `grp.pt.core.ResultPage` | `grp-util-com` | `grp.pt` |
| `grp.pt.core.SystemQueryParam` | `grp-util-com` | `grp.pt` |
| `grp.pt.util.DateUtil` | `grp-util-com` | `grp.pt` |
| `grp.pt.util.StringUtil` | `grp-util-com` | `grp.pt` |
| `grp.pt.util.CollectionUtils` | `grp-util-com` | `grp.pt` |
| `grp.pt.util.ResultPage` | `grp-util-com` | `grp.pt` |
| `grp.pt.util.poi.annotation.ExcelAttribute` | `grp-util-com` | `grp.pt` |
| `grp.pt.util.model.Session` | `grp-util-com` | `grp.pt` |

**修复方法：** 在对应模块的 `pom.xml` 中添加：
```xml
<dependency>
    <groupId>grp.pt</groupId>
    <artifactId>grp-util-com</artifactId>
</dependency>
```

#### 2.2 Spring 框架类缺失

**典型错误特征：**
- `org.springframework.jdbc.core` 不存在
- `org.springframework.web` 不存在
- `org.springframework.mock.web` 不存在

**缺失类与依赖映射：**

| 缺失包 | 需添加的依赖 | 说明 |
|--------|-------------|------|
| `org.springframework.jdbc.*` | `spring-jdbc`（或 `spring-boot-starter-jdbc`） | API 模块推荐用轻量 `spring-jdbc` |
| `org.springframework.web.*` | `spring-web`（或 `spring-boot-starter-web`） | API 模块推荐用轻量 `spring-web` |
| `org.springframework.context.*` | `spring-context` | `@Component`、`@Bean`、`@Value` 等 |
| `org.springframework.format.*` | `spring-context` | `@DateTimeFormat` 等格式化注解 |
| `org.springframework.stereotype.*` | `spring-context` | `@Component`、`@Service` 等 |
| `org.springframework.transaction.*` | `spring-tx` | 事务管理 |
| `org.springframework.mock.web.*` | `spring-test`（scope=compile） | 生产代码中使用 MockMultipartFile 等 |

> **建议**：API 模块（`*-api`）优先使用轻量级 `spring-jdbc`、`spring-web`、`spring-context`，而非完整的 Spring Boot Starter，以减少传递性依赖污染。

#### 2.3 第三方库缺失

**典型错误特征：**
- `io.swagger.annotations` 不存在
- `org.apache.commons.lang3` 不存在
- `org.assertj.core.util` 不存在

**常见第三方依赖：**

| 缺失包 | artifactId | groupId | 需在根POM DM中声明版本 |
|--------|-----------|---------|---------------------|
| `io.swagger.annotations.*` | `swagger-annotations` | `io.swagger` | 是 (1.6.2) |
| `org.apache.commons.lang3.*` | `commons-lang3` | `org.apache.commons` | 否（Spring Boot 管理） |
| `org.apache.commons.lang.*` | `commons-lang` | `commons-lang` | 是 (2.6) |
| `org.apache.commons.collections.*` | `commons-collections` | `commons-collections` | 是 (3.2.2) |
| `org.apache.commons.fileupload.*` | `commons-fileupload` | `commons-fileupload` | 是 (1.4) |
| `org.assertj.core.util.*` | `assertj-core` | `org.assertj` | 否（Spring Boot 管理） |
| `com.fasterxml.jackson.*` | `jackson-annotations` | `com.fasterxml.jackson.core` | 否（Spring Boot 管理） |
| `org.mybatis.*` | `mybatis` | `org.mybatis` | 是 (3.5.6) |

#### 2.4 制品重命名兼容（重要）

原平台可能使用了已过时或已重命名的 Maven 制品。重构后需要检查并修正：

| 原 artifactId | 新 artifactId | 说明 |
|---------------|--------------|------|
| `org.apache.cxf:cxf-api` | `org.apache.cxf:cxf-core` | CXF 3.x 起 `cxf-api` 重命名为 `cxf-core` |

**检测方法：** 编译时出现 `xxx was not found in repository` 类错误时，优先检查该制品是否已被重命名。

**修复：** 同时修改根 POM `dependencyManagement` 和叶子模块 POM 中的 artifactId。

### Step 3: 依赖修复策略

#### 3.1 根POM dependencyManagement 优先

所有新发现需要版本管理的依赖，先添加到根 POM 的 `<dependencyManagement>` 中：

```xml
<dependencyManagement>
    <dependencies>
        <!-- 新增缺失依赖 -->
        <dependency>
            <groupId>xxx</groupId>
            <artifactId>yyy</artifactId>
            <version>z.z.z</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 3.2 叶子模块添加依赖（不含 version）

在需要该依赖的叶子模块 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>xxx</groupId>
    <artifactId>yyy</artifactId>
</dependency>
```

#### 3.3 API 模块必须补充的基础依赖清单

**重要**：从平台父POM独立后，API 模块（`*-api`）通常需要以下基础依赖：

```xml
<!-- 平台工具包（提供 ReturnData、ExcelAttribute、DateUtil 等） -->
<dependency>
    <groupId>grp.pt</groupId>
    <artifactId>grp-util-com</artifactId>
</dependency>
<!-- Spring Web（用于 @RequestMapping 等注解） -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
</dependency>
<!-- Spring Context（用于 @Component 等注解） -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
</dependency>
<!-- Spring JDBC（如 API 中有 RowMapper 等数据库相关类） -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
</dependency>
<!-- Jackson 注解 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
</dependency>
<!-- Swagger 注解 -->
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-annotations</artifactId>
</dependency>
<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
<!-- Commons Lang3 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
<!-- Servlet API（scope=provided） -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <scope>provided</scope>
</dependency>
```

#### 3.4 Server-COM 模块常见补充依赖

Server-COM（`*-server-com`）模块通常还需要：

```xml
<!-- Spring JDBC -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
</dependency>
<!-- Commons Lang3 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
<!-- Commons Lang (2.x legacy) -->
<dependency>
    <groupId>commons-lang</groupId>
    <artifactId>commons-lang</artifactId>
</dependency>
<!-- Commons Collections -->
<dependency>
    <groupId>commons-collections</groupId>
    <artifactId>commons-collections</artifactId>
</dependency>
<!-- Commons FileUpload -->
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
</dependency>
<!-- Servlet API -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <scope>provided</scope>
</dependency>
<!-- AssertJ（如生产代码使用了 Lists.newArrayList 等） -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>compile</scope>
</dependency>
<!-- Spring Test（如生产代码使用了 MockMultipartFile 等） -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <scope>compile</scope>
</dependency>
```

#### 3.5 Server/Controller 模块常见补充依赖

Server（`*-server`）模块通常还需要：

```xml
<!-- Spring JDBC -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
</dependency>
<!-- Swagger 注解 -->
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-annotations</artifactId>
</dependency>
<!-- Servlet API -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <scope>provided</scope>
</dependency>
```

### Step 4: 逐模块验证

按 Reactor 构建顺序逐一验证：

```bash
# 1. 先编译 API 层
mvn compile -pl {module-path}/*-api -am

# 2. 再编译实现层
mvn compile -pl {module-path}/*-server-com -am

# 3. 编译控制层
mvn compile -pl {module-path}/*-server -am

# 4. 全量编译
mvn compile

# 5. 全量打包
mvn package -DskipTests
```

### Step 5: 验证标准

- `mvn compile` 输出 `BUILD SUCCESS`，0 个 ERROR
- `mvn package -DskipTests` 输出 `BUILD SUCCESS`
- 所有模块状态均为 `SUCCESS`
- WARNING 允许存在（如 `sun.misc.BASE64Decoder` 过时 API 警告、`unchecked` 警告等）

### Step 6: 工程启动运行验证

**此步骤在编译验证通过后执行**，确认重构后的工程可以正常启动运行。

#### 6.1 识别启动类

在聚合层模块（`*-springcloud`）中查找 SpringBoot 启动类：

```bash
# 搜索包含 SpringApplication.run 的 Java 文件
grep -rl "SpringApplication.run" {aggregation-module}/src/main/java/
```

#### 6.2 启动前检查

1. **确认配置文件存在**：检查 `src/main/resources/application.yml`（或 `.properties`、`bootstrap.yml`）
2. **确认数据源配置**：如需数据库，确保连接信息正确或添加 `spring.autoconfigure.exclude` 跳过
3. **确认注册中心配置**：如使用 Nacos/Eureka，确保地址可达或添加排除配置

#### 6.3 执行启动验证

```bash
# 方式一：使用 spring-boot:run
mvn spring-boot:run -pl {aggregation-module-path}

# 方式二：先打包再运行 jar
mvn package -DskipTests
java -jar {aggregation-module-path}/target/{artifact-name}.jar
```

#### 6.4 启动成功标准

- 控制台出现 `Started XxxApplication in x.xx seconds`
- 无 `APPLICATION FAILED TO START` 错误
- HTTP 端口正常监听（如 `Tomcat started on port(s): 8080`）

#### 6.5 常见启动失败与修复

| 失败特征 | 原因 | 修复方法 |
|---------|------|---------|
| `Failed to configure a DataSource` | 数据源未配置 | 添加 `spring.autoconfigure.exclude=DataSourceAutoConfiguration` |
| `Connection refused: connect (Nacos)` | 注册中心不可达 | 添加 `spring.cloud.nacos.discovery.enabled=false` |
| `ClassNotFoundException` | 打包方式不正确 | 确认聚合层模块有 `spring-boot-maven-plugin` |
| `No qualifying bean of type` | Bean 扫描路径不对 | 检查 `@SpringBootApplication(scanBasePackages=...)` |

> **注意**：启动验证依赖外部基础设施（数据库、注册中心等），如环境不具备完整条件，可仅验证 `mvn package -DskipTests` 通过即可，启动验证作为可选步骤记录在报告中。
