---
name: four-layer-arch-refactor
description: 四层架构代码自动重构工具。将不符合四层架构规范的Maven工程自动重构为标准目录结构，只调整pom.xml、yaml配置和Java的import语句，不改变任何业务逻辑代码。当用户提到"架构重构"、"结构迁移"、"四层架构改造"、"代码重构"时使用。
---

# 四层架构代码自动重构

## 概述

将不符合四层架构规范（底座层/能力层/聚合层/体验层）的 Maven 工程，自动重构为标准目录结构。

**铁律：只调整以下三类文件，绝不改变业务逻辑：**
1. `pom.xml` — 调整 parent、modules、dependencies、groupId/artifactId、relativePath
2. `*.yml` / `*.yaml` / `*.properties` — 调整 spring.application.name、server.port、配置引用路径
3. `*.java` 的 `import` 语句和 `package` 声明 — 因模块/包路径变更而需要同步修改

## 目标架构定义

```
{project-root}/
├── pom.xml                              # 根POM (packaging=pom)
├── grp-common-boot/                     # 底座层容器
│   ├── pom.xml                          # packaging=pom
│   ├── grp-logger-com/                  # 日志模块
│   ├── grp-exception-com/               # 异常模块
│   ├── grp-util-com/                    # 工具模块
│   └── grp-database-com/               # 数据库模块
├── {module}-module/                     # 业务模块容器 (每个业务一个)
│   ├── pom.xml                          # packaging=pom
│   ├── grp-capability-{module}/         # 能力层容器
│   │   ├── pom.xml                      # packaging=pom
│   │   ├── grp-{module}-api/            # API定义 (可选)
│   │   ├── {module}-server/             # Controller层
│   │   └── {module}-server-com/         # 业务实现层
│   ├── grp-aggregation-{module}/        # 聚合层容器
│   │   ├── pom.xml                      # packaging=pom
│   │   ├── {module}-server-springcloud/ # SC适配
│   │   ├── {module}-server-huawei/      # 华为适配
│   │   ├── {module}-server-pivotal/     # Pivotal适配
│   │   └── {module}-server-tencent/     # 腾讯适配
│   └── grp-experience-{module}/         # 体验层容器 (可选)
│       ├── pom.xml                      # packaging=pom
│       └── {module}-feign-com/          # Feign SDK
```

---

## 安全约束 (红线)

执行重构前必须遵守以下约束，**任何一条违反都必须立即停止并报告**：

| 编号 | 约束 | 说明 |
|------|------|------|
| S-01 | **禁止修改 Java 方法体** | 不得修改任何方法的实现逻辑、变量、算法 |
| S-02 | **禁止修改 Java 类结构** | 不得增删字段、方法、注解（import/package 除外） |
| S-03 | **禁止修改 SQL/XML 映射** | MyBatis XML、SQL 文件内容不得改动 |
| S-04 | **禁止修改前端代码** | JS/CSS/HTML/Vue 等前端文件不得改动 |
| S-05 | **禁止修改 resources 资源** | 配置模板、i18n、静态资源等不得改动（yaml/properties 除外） |
| S-06 | **禁止删除任何文件** | 只能移动或重命名，不得删除源文件 |
| S-07 | **必须备份** | 重构前必须确认用户已备份，或提醒用户备份 |

**允许修改的范围：**
- `pom.xml`: parent、modules、artifactId、groupId、version、dependencies、relativePath、packaging
- `*.yml` / `*.yaml` / `*.properties`: spring.application.name、server.port、路径引用
- `*.java`: 仅 `package` 声明和 `import` 语句

---

## 重构执行流程

### Phase 0: 预检与备份提醒

1. 提醒用户备份整个工程目录
2. 扫描当前工程结构，列出所有一级和二级目录
3. 列出所有 `pom.xml` 文件
4. 识别当前架构组织方式（扁平/按模块）
5. 生成重构计划摘要供用户确认

### Phase 1: 分析源工程结构

扫描源工程，建立**模块映射表**：

```
| 源路径 | 目标层级 | 目标路径 | 操作类型 |
|--------|----------|----------|----------|
| grp-platform-common/ | 底座层 | grp-common-boot/ | 重命名 |
| grp-platform-common/grp-util-com/ | 底座层子模块 | grp-common-boot/grp-util-com/ | 移动 |
| grp-platform-server/element-server2/ | 能力层 | element-module/grp-capability-element/element-server/ | 移动+重命名 |
| grp-platform-springcloud/element-server2-springcloud/ | 聚合层 | element-module/grp-aggregation-element/element-server-springcloud/ | 移动+重命名 |
| grp-platform-feign/element-feign-com/ | 体验层 | element-module/grp-experience-element/element-feign-com/ | 移动 |
```

**分析规则：**
- 以 `grp-*-com/` 命名的通用模块 → 底座层 `grp-common-boot/`
- 以 `*-server/`、`*-server-com/`、`*-server2/`、`*-server2-com/` 命名 → 能力层 `grp-capability-{module}/`
- 以 `grp-*-api/` 命名 → 能力层 `grp-capability-{module}/`
- 以 `*-springcloud/`、`*-huawei/`、`*-tencent/`、`*-pivotal/` 命名 → 聚合层 `grp-aggregation-{module}/`
- 以 `*-feign-com/`、`*-feign-api/` 命名 → 体验层 `grp-experience-{module}/`

### Phase 2: 创建目标目录骨架

按以下顺序创建目录和容器 POM：

#### 2.1 根 POM
保留原有依赖管理，更新 `<modules>` 列表。

#### 2.2 底座层
```
grp-common-boot/
├── pom.xml          # packaging=pom, parent=根POM
└── [各通用模块]/     # parent=grp-common-boot
```

#### 2.3 业务模块 (对每个业务模块重复)
```
{module}-module/
├── pom.xml                          # packaging=pom, parent=根POM
├── grp-capability-{module}/
│   └── pom.xml                      # packaging=pom, parent={module}-module
├── grp-aggregation-{module}/
│   └── pom.xml                      # packaging=pom, parent={module}-module
└── grp-experience-{module}/         # 可选
    └── pom.xml                      # packaging=pom, parent={module}-module
```

### Phase 3: 移动源码目录

将各叶子模块（含 src/、resources/）移动到目标位置：

**执行规则：**
1. 使用 `mv` 或 `cp -r` 移动整个模块目录（含 src/、resources/、pom.xml）
2. 移动顺序：底座层 → 能力层 → 聚合层 → 体验层
3. 每移动一个模块后立即记录日志

**模块重命名映射（去掉版本号后缀）：**
- `element-server2` → `element-server`
- `element-server2-com` → `element-server-com`
- `element-server2-springcloud` → `element-server-springcloud`
- 以此类推，所有 `*2` 后缀统一去除
- `4A-*` → `grp-4a-*`（统一前缀+全小写）

### Phase 4: 更新 POM 文件

对每个移动后的模块，更新其 `pom.xml`：

#### 4.1 更新 `<parent>` 配置
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

#### 4.2 更新 `<modules>` 声明
- 根 POM: 列出 `grp-common-boot` + 所有 `{module}-module`
- `{module}-module/pom.xml`: 列出 `grp-capability-{module}` + `grp-aggregation-{module}` [+ `grp-experience-{module}`]
- 各层容器 POM: 列出其下所有叶子模块

#### 4.3 更新 `<artifactId>`
如果模块被重命名（如去掉 `2` 后缀），同步更新 artifactId。

#### 4.4 更新 `<dependencies>`
- 所有引用被重命名模块的 dependency 的 artifactId 同步更新
- 遍历所有 pom.xml，全局替换旧 artifactId → 新 artifactId

#### 4.5 移除子模块冗余 version 声明
子模块不应自行声明 `<version>`，通过 parent 继承。

### Phase 5: 更新 Java 文件

**仅修改以下两种行：**

#### 5.1 `package` 声明
如果模块的包路径发生变更（如 artifactId 变化导致默认包路径变化），更新 Java 文件的第一行 `package` 声明：
```java
// 旧
package com.example.element.server2.controller;
// 新
package com.example.element.server.controller;
```

#### 5.2 `import` 语句
如果被依赖模块的包路径变更，更新 import 中对应的包名：
```java
// 旧
import com.example.element.server2.com.service.ElementService;
// 新
import com.example.element.server.com.service.ElementService;
```

**查找替换规则：**
遍历所有 `*.java` 文件，按模块重命名映射表，执行全局文本替换：
- 仅替换 `package ` 开头的行
- 仅替换 `import ` 开头的行
- 不触碰其他任何行

### Phase 6: 更新配置文件

#### 6.1 `application.yml` / `application.yaml` / `bootstrap.yml`
- 更新 `spring.application.name`（如有）
- 更新扫描路径（如 `mybatis.mapper-locations`）
- 更新 `feign.client` 配置中引用的服务名

#### 6.2 `application.properties` / `bootstrap.properties`
- 同上述 yaml 规则

**不修改：**
- 数据库连接配置
- Redis 配置
- 端口配置（除非有命名冲突）
- 任何业务配置项

### Phase 7: 编译验证

重构完成后执行以下验证：

```bash
# 1. 在项目根目录执行编译
mvn compile -pl {refactored-module} -am 2>&1

# 2. 如果编译失败，收集所有错误
mvn compile 2>&1 | grep -E "ERROR|error:|cannot find symbol|package .* does not exist"
```

**错误处理策略：**

| 错误类型 | 自动修复方式 |
|----------|-------------|
| `package X does not exist` | 检查 import 替换是否遗漏，补充替换 |
| `cannot find symbol` | 检查 dependency 是否遗漏，补充 POM dependency |
| `Non-resolvable parent POM` | 检查 relativePath 是否正确 |
| `Could not find artifact` | 检查 artifactId 重命名是否一致 |

**对于无法自动修复的错误，输出错误报告：**

```
# 重构后编译错误报告

## 错误汇总
- 编译错误总数：{count}
- 可自动修复：{auto_fix}
- 需人工处理：{manual_fix}

## 错误详情
| 文件 | 行号 | 错误类型 | 错误信息 | 修复建议 |
|------|------|----------|----------|----------|
| ElementService.java | 15 | import | package grp.pt.xxx does not exist | 包路径已变更，需手动检查 |
```

---

## 容器 POM 模板

### 根 POM 模板 (modules 部分)
```xml
<modules>
    <module>grp-common-boot</module>
    <module>element-module</module>
    <module>framework-module</module>
    <module>engine-module</module>
    <module>workflow-module</module>
    <module>frs-module</module>
    <module>gateway-module</module>
    <module>oauth2-module</module>
    <module>framework-web-module</module>
</modules>
```

### 业务模块容器 POM 模板
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>{groupId}</groupId>
        <artifactId>{root-artifactId}</artifactId>
        <version>{version}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>{module}-module</artifactId>
    <packaging>pom</packaging>
    <modules>
        <module>grp-capability-{module}</module>
        <module>grp-aggregation-{module}</module>
        <!-- <module>grp-experience-{module}</module> -->
    </modules>
</project>
```

### 能力层容器 POM 模板
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>{groupId}</groupId>
        <artifactId>{module}-module</artifactId>
        <version>{version}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>grp-capability-{module}</artifactId>
    <packaging>pom</packaging>
    <modules>
        <module>grp-{module}-api</module>
        <module>{module}-server</module>
        <module>{module}-server-com</module>
    </modules>
</project>
```

### 聚合层容器 POM 模板
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>{groupId}</groupId>
        <artifactId>{module}-module</artifactId>
        <version>{version}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>grp-aggregation-{module}</artifactId>
    <packaging>pom</packaging>
    <modules>
        <module>{module}-server-springcloud</module>
        <module>{module}-server-huawei</module>
        <module>{module}-server-pivotal</module>
        <module>{module}-server-tencent</module>
    </modules>
</project>
```

### 体验层容器 POM 模板
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>{groupId}</groupId>
        <artifactId>{module}-module</artifactId>
        <version>{version}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <artifactId>grp-experience-{module}</artifactId>
    <packaging>pom</packaging>
    <modules>
        <module>{module}-feign-com</module>
    </modules>
</project>
```

---

## 模块识别与归类规则

### 按名称模式自动识别目标层级

| 名称模式 | 目标层级 | 目标容器 |
|----------|----------|----------|
| `grp-*-com` (logger/exception/util/database/cache/gray 等通用) | 底座层 | `grp-common-boot/` |
| `grp-{module}-api` | 能力层 | `grp-capability-{module}/` |
| `{module}-server` 或 `{module}-server{N}` | 能力层 | `grp-capability-{module}/` |
| `{module}-server-com` 或 `{module}-server{N}-com` | 能力层 | `grp-capability-{module}/` |
| `{module}-server-springcloud` 或 `{module}-server{N}-springcloud` | 聚合层 | `grp-aggregation-{module}/` |
| `{module}-server-huawei` 或 `{module}-server{N}-huawei` | 聚合层 | `grp-aggregation-{module}/` |
| `{module}-server-tencent` 或 `{module}-server{N}-tencent*` | 聚合层 | `grp-aggregation-{module}/` |
| `{module}-server-pivotal` 或 `{module}-server{N}-pivotal` | 聚合层 | `grp-aggregation-{module}/` |
| `{module}-feign-com` 或 `{module}-feign-api` | 体验层 | `grp-experience-{module}/` |

### 模块名提取规则

从源模块名中提取业务模块名 `{module}`：
1. 去掉版本号后缀（如 `element-server2` → `element`，取 `-server` 前的部分）
2. 去掉框架后缀（如 `element-server2-springcloud` → `element`）
3. 去掉 `-com` 后缀（如 `element-server2-com` → `element`）

### 无法自动识别的模块

对于不符合上述任何模式的模块（如 `demo`、`hzero-demo-cpy`、地域定制模块 `guangdong`、`shenzhen`），输出警告并跳过，由用户手动决定归类。

---

## 重构后验证清单

重构完成后，逐项验证：

| 编号 | 验证项 | 方法 |
|------|--------|------|
| V-01 | 所有 pom.xml 的 relativePath 正确 | 遍历检查 |
| V-02 | 所有容器 POM 的 packaging=pom | Grep 检查 |
| V-03 | 所有 modules 声明与实际目录一致 | 对比检查 |
| V-04 | 全局无旧 artifactId 残留引用 | Grep 全局搜索旧名称 |
| V-05 | Java package 声明与目录路径一致 | 遍历检查 |
| V-06 | Java import 无旧包路径残留 | Grep 全局搜索旧包名 |
| V-07 | mvn compile 通过（至少无结构性错误） | 执行编译 |

---

## 输出格式

### 重构计划确认

```
# 四层架构重构计划

## 当前结构
- 组织方式：按层扁平组织
- 模块总数：{count}
- 涉及业务模块：element, framework, engine, workflow, frs, gateway, oauth2, web

## 重构映射 (共 {N} 个移动操作)
| # | 源路径 | 目标路径 | 操作 |
|---|--------|----------|------|
| 1 | grp-platform-common/ | grp-common-boot/ | 重命名 |
| 2 | grp-platform-server/element-server2/ | element-module/grp-capability-element/element-server/ | 移动+重命名 |
...

## 新增容器 POM (共 {M} 个)
| 路径 | 类型 |
|------|------|
| element-module/pom.xml | 业务模块容器 |
| element-module/grp-capability-element/pom.xml | 能力层容器 |
...

确认后开始执行重构。
```

### 重构完成报告

```
# 四层架构重构完成报告

## 执行概要
- 移动模块数：{count}
- 创建容器 POM 数：{count}
- 更新 POM 文件数：{count}
- 更新 Java 文件数：{count}
- 更新配置文件数：{count}

## 编译验证
- 状态：通过/失败
- 错误数：{count}

## 编译错误（如有）
| 模块 | 文件 | 行号 | 错误类型 | 错误信息 |
|------|------|------|----------|----------|
...

## 需人工处理
| 模块 | 原因 | 建议 |
|------|------|------|
| guangdong | 无法自动识别归类 | 请手动归类 |
...
```

## 执行说明

1. 此 Skill 被调用后，先扫描工程结构并生成重构计划
2. **必须等待用户确认后才开始执行重构操作**
3. 按 Phase 0-7 顺序执行，每个 Phase 完成后输出进度
4. 遇到无法自动处理的模块，标记警告并跳过
5. 重构完成后自动执行 `mvn compile` 验证
6. 输出编译错误报告（如有），标注可自动修复和需人工处理的错误
7. 对可自动修复的错误（import/dependency 遗漏）尝试自动修复后重新编译
