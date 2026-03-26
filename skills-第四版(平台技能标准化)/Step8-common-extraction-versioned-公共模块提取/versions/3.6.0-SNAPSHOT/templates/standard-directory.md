# common 模块标准目录结构

## 完整结构模板

以 element 模块为例，`grp-common-element` 的标准目录结构：

```
grp-common-element/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── grp/
                └── pt/
                    ├── cache/                  # 缓存相关
                    │   ├── XxxCache.java
                    │   └── CacheManager.java
                    ├── config/                 # 通用配置类
                    │   ├── SwaggerConfig.java
                    │   └── CorsConfig.java
                    ├── constant/               # 常量定义
                    │   └── XxxConstant.java
                    ├── enums/                  # 枚举定义
                    │   └── XxxEnum.java
                    ├── exception/              # 异常定义
                    │   └── BusinessException.java
                    └── util/                   # 工具类
                        ├── DateUtil.java
                        ├── StringUtil.java
                        └── JsonUtil.java
```

## POM 模板

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>grp</groupId>
        <artifactId>element-module</artifactId>
        <version>${revision}</version>
    </parent>

    <artifactId>grp-common-element</artifactId>
    <packaging>jar</packaging>
    <description>Element公共模块 - 工具类、常量、枚举、异常、缓存、配置</description>

    <dependencies>
        <!-- 根据迁移的类所需依赖按需添加 -->
    </dependencies>
</project>
```

## 目录创建顺序

1. 先确认 `grp-common-element/pom.xml` 存在
2. 创建 `src/main/java/grp/pt/` 基础目录
3. 按迁移顺序创建子目录：constant → enums → exception → util → cache → config
4. 仅创建有文件迁入的目录，不创建空目录
