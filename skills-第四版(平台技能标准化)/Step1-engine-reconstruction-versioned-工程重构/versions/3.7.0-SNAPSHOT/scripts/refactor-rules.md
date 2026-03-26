# 重构执行详细规则

## 一、POM 文件更新规则

### 1.1 更新 `<parent>` 配置

```xml
<!-- 底座层子模块 -->
<parent>
    <groupId>{groupId}</groupId>
    <artifactId>grp-common-boot</artifactId>
    <version>{version}</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<!-- 能力层子模块 -->
<parent>
    <groupId>{groupId}</groupId>
    <artifactId>grp-capability-{module}</artifactId>
    <version>{version}</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<!-- 聚合层子模块 -->
<parent>
    <groupId>{groupId}</groupId>
    <artifactId>grp-aggregation-{module}</artifactId>
    <version>{version}</version>
    <relativePath>../pom.xml</relativePath>
</parent>
```

### 1.2 更新 `<modules>` 声明

- 根 POM: 列出 `grp-common-boot`（如有）+ 所有 `{module}-module`
- `{module}-module/pom.xml`: 列出 `grp-capability-{module}` + `grp-aggregation-{module}` [+ `grp-experience-{module}`]
- 各层容器 POM: 列出其下所有叶子模块

### 1.3 更新 `<artifactId>`

如果模块被重命名（如去掉 `2` 后缀），同步更新 artifactId。

### 1.4 更新 `<dependencies>`

- 所有引用被重命名模块的 dependency 的 artifactId 同步更新
- 遍历所有 pom.xml，全局替换旧 artifactId → 新 artifactId

### 1.5 清理叶子模块冗余 version 声明（独立工程新增）

根POM 已通过 `dependencyManagement` 统一管理所有依赖版本后：
- 遍历所有叶子模块 POM
- 对 `groupId=grp.pt` 的依赖，如果其版本已在根 POM `dependencyManagement` 管理，移除叶子模块中的显式 `<version>` 声明
- 对第三方依赖，如果已被 Spring Boot/Cloud BOM 或根 POM `dependencyManagement` 管理，同样移除显式版本

---

## 二、Java 文件更新规则

**仅修改以下两种行：**

### 2.1 `package` 声明

如果模块的包路径发生变更，更新 Java 文件的第一行 `package` 声明。

### 2.2 `import` 语句

如果被依赖模块的包路径变更，更新 import 中对应的包名。

### 2.3 查找替换规则

遍历所有 `*.java` 文件，按模块重命名映射表，执行全局文本替换：
- 仅替换 `package ` 开头的行
- 仅替换 `import ` 开头的行
- 不触碰其他任何行

---

## 三、配置文件更新规则

### 3.1 `application.yml` / `application.yaml` / `bootstrap.yml`

- 更新 `spring.application.name`（如有）
- 更新扫描路径（如 `mybatis.mapper-locations`）

### 3.2 不修改项

- 数据库连接配置
- Redis 配置
- 端口配置（除非有命名冲突）
- 任何业务配置项

---

## 四、编译错误处理策略

| 错误类型 | 自动修复方式 |
|----------|-------------|
| `package X does not exist` | 检查 import 替换是否遗漏，补充替换 |
| `cannot find symbol` | 检查 dependency 是否遗漏，补充 POM dependency |
| `Non-resolvable parent POM` | 检查 relativePath 是否正确 |
| `Could not find artifact` | 检查 artifactId 重命名是否一致；检查 dependencyManagement 是否遗漏 |

编译验证命令：

```bash
mvn compile -pl {refactored-module} -am 2>&1
```
