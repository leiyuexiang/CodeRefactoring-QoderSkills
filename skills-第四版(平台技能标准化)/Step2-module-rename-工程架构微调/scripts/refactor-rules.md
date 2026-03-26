# 工程架构微调 — POM 和 Java 更新详细规则

## 一、重命名映射表

本技能涉及的固定映射关系（`{module}` 为实际业务模块名，如 element、workflow、framework 等）：

| 序号 | 原名称 | 新名称 | 说明 |
|------|--------|--------|------|
| R-01 | `{module}-server` | `{module}-controller` | Controller 层模块 |
| R-02 | `{module}-server-com` | `{module}-service` | Service 业务实现层模块 |

---

## 二、POM 文件更新规则

### 2.1 被重命名模块自身的 POM — artifactId 更新

**规则**：直接将 `<artifactId>` 标签值替换为新名称。

```xml
<!-- {module}-controller/pom.xml（原 {module}-server/pom.xml）-->
<!-- 旧 -->
<artifactId>{module}-server</artifactId>
<!-- 新 -->
<artifactId>{module}-controller</artifactId>
```

```xml
<!-- {module}-service/pom.xml（原 {module}-server-com/pom.xml）-->
<!-- 旧 -->
<artifactId>{module}-server-com</artifactId>
<!-- 新 -->
<artifactId>{module}-service</artifactId>
```

**注意**：`<parent>` 配置中的 `<artifactId>` 和 `<relativePath>` 不需要变更，因为父 POM 是 `grp-capability-{module}`，没有被重命名。

### 2.2 能力层容器 POM — modules 声明更新

**文件**：`grp-capability-{module}/pom.xml`

```xml
<!-- 旧 -->
<modules>
    <module>grp-{module}-api</module>
    <module>{module}-server</module>
    <module>{module}-server-com</module>
</modules>

<!-- 新 -->
<modules>
    <module>grp-{module}-api</module>
    <module>{module}-controller</module>
    <module>{module}-service</module>
</modules>
```

### 2.3 根 POM — dependencyManagement 更新

**文件**：根 `pom.xml`

在 `<dependencyManagement>` 中查找并替换：

```xml
<!-- 旧 -->
<dependency>
    <groupId>{groupId}</groupId>
    <artifactId>{module}-server</artifactId>
    <version>${version-property}</version>
</dependency>
<dependency>
    <groupId>{groupId}</groupId>
    <artifactId>{module}-server-com</artifactId>
    <version>${version-property}</version>
</dependency>

<!-- 新 -->
<dependency>
    <groupId>{groupId}</groupId>
    <artifactId>{module}-controller</artifactId>
    <version>${version-property}</version>
</dependency>
<dependency>
    <groupId>{groupId}</groupId>
    <artifactId>{module}-service</artifactId>
    <version>${version-property}</version>
</dependency>
```

### 2.4 全局依赖引用 — dependency 更新

**范围**：遍历工程下所有 `pom.xml` 文件

**规则**：在 `<dependencies>` 块中，查找引用被重命名模块的 dependency 并替换 artifactId。

典型需要更新的文件：

| POM 文件位置 | 需要更新的 dependency |
|-------------|---------------------|
| `{module}-controller/pom.xml` | 依赖 `{module}-service`（原 `{module}-server-com`） |
| `{module}-server-springcloud/pom.xml` | 依赖 `{module}-controller`（原 `{module}-server`） |
| 其他跨模块引用的 POM | 视实际依赖关系而定 |

**替换规则**：

```
全文搜索替换（仅在 <artifactId> 标签内）：
  {module}-server-com  →  {module}-service      （长匹配优先）
  {module}-server      →  {module}-controller    （短匹配后执行）
```

**重要**：替换顺序必须先替换 `{module}-server-com`（长字符串），再替换 `{module}-server`（短字符串），否则 `{module}-server-com` 会被误替换为 `{module}-controller-com`。

### 2.5 排除规则

以下 artifactId 中出现的 `{module}-server` 字样**不应被替换**：

| 排除模式 | 说明 |
|----------|------|
| `{module}-server-springcloud` | 聚合层 SC 适配模块，保持不变 |
| `{module}-server-huawei` | 聚合层华为适配模块，保持不变 |
| `{module}-server-tencent` | 聚合层腾讯适配模块，保持不变 |
| `{module}-server-pivotal` | 聚合层 Pivotal 适配模块，保持不变 |

**实现方式**：使用精确匹配而非前缀匹配。替换时确保 `<artifactId>` 标签的完整值等于 `{module}-server` 或 `{module}-server-com`，而非仅包含这个子串。

建议使用以下正则匹配：

```
# 精确匹配 {module}-server（不匹配 {module}-server-xxx）
<artifactId>{module}-server</artifactId>  →  <artifactId>{module}-controller</artifactId>

# 精确匹配 {module}-server-com
<artifactId>{module}-server-com</artifactId>  →  <artifactId>{module}-service</artifactId>
```

---

## 三、Java 文件更新规则

### 3.1 package 声明更新

如果模块的 Java 源码包路径中包含与模块名对应的路径段，需同步更新。

**典型包路径映射**（以 `element` 模块为例）：

| 原包路径 | 新包路径 |
|----------|---------|
| `*.element.server.com.*` | `*.element.service.*` |
| `*.element.server.*`（非 springcloud 子包） | `*.element.controller.*` |

**替换顺序**（长匹配优先）：

```
# Step 1: 先替换 server.com（长匹配）
旧: package xxx.{module}.server.com.yyy;
新: package xxx.{module}.service.yyy;

# Step 2: 再替换 server（短匹配）
旧: package xxx.{module}.server.yyy;
新: package xxx.{module}.controller.yyy;
```

### 3.2 import 语句更新

**范围**：遍历工程下所有 `*.java` 文件

使用与 package 声明相同的替换映射表，对 `import` 行执行替换。

```
# Step 1: 先替换 server.com
旧: import xxx.{module}.server.com.yyy.ClassName;
新: import xxx.{module}.service.yyy.ClassName;

# Step 2: 再替换 server
旧: import xxx.{module}.server.yyy.ClassName;
新: import xxx.{module}.controller.yyy.ClassName;
```

### 3.3 排除规则

以下包路径中的 `server` 字样**不应被替换**：

| 排除模式 | 说明 |
|----------|------|
| `*.{module}.server.springcloud.*` | 聚合层 SC 适配模块的包路径 |
| `org.springframework.*.server.*` | Spring 框架内部包路径 |

**实现方式**：

1. 只对 `{module}-controller/` 和 `{module}-service/` 目录下的 Java 文件执行 package 声明替换
2. 对全工程的 Java 文件执行 import 语句替换，但排除聚合层（`{module}-server-springcloud/`）自身的 package 声明
3. 聚合层模块中的 import 语句如果引用了能力层的类，也需要跟着更新

### 3.4 物理目录移动

Java 文件的包路径变更后，需同步移动 `src/main/java/` 下的目录结构：

```
# 以 element 模块为例

# Service 层
旧: {module}-service/src/main/java/.../element/server/com/
新: {module}-service/src/main/java/.../element/service/

# Controller 层
旧: {module}-controller/src/main/java/.../element/server/
新: {module}-controller/src/main/java/.../element/controller/
```

---

## 四、配置文件更新规则

### 4.1 一般情况下无需修改

`application.yml`、`bootstrap.yml`、`application.properties` 等配置文件通常不包含模块 artifactId 的直接引用，因此**一般无需修改**。

### 4.2 需要检查的场景

以下情况需要人工确认是否需要更新：

| 配置项 | 检查条件 |
|--------|---------|
| `mybatis.mapper-locations` | 如果路径中包含旧模块名，需更新 |
| `spring.application.name` | 通常是 `{module}-server`，是否需要改为 `{module}-controller` 取决于业务要求 |

**建议**：`spring.application.name` 保持原值不变，因为这是注册到注册中心的服务名，修改会影响服务发现。

---

## 五、验证清单

| 编号 | 验证项 | 方法 |
|------|--------|------|
| V-01 | 目录已正确重命名 | `ls` 检查能力层目录下的模块名 |
| V-02 | 被重命名模块的 artifactId 已更新 | 读取 POM 检查 |
| V-03 | 能力层容器 modules 声明已更新 | 读取 POM 检查 |
| V-04 | 根 POM dependencyManagement 已更新 | 读取 POM 检查 |
| V-05 | 全局无旧 artifactId 残留引用 | `grep -r "{module}-server</artifactId>" --include="pom.xml"` |
| V-06 | Java package 声明与目录路径一致 | 遍历检查 |
| V-07 | Java import 无旧包路径残留 | `grep -r "import.*{module}.server.com" --include="*.java"` 和 `grep -r "import.*{module}.server\." --include="*.java"` |
| V-08 | `mvn compile` 编译通过 | 执行编译 |
