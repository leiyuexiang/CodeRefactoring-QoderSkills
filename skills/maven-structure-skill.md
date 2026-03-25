---
name: maven-structure
description: 快速创建四层架构Maven模块，包含体验层(Feign)、聚合层(BFF)、能力层(Capability)、底座层(Foundation)的标准化模块结构。当用户需要创建新的Maven模块、添加业务模块、或提到"四层架构模块创建"时使用。
---

# Maven模块快速创建

## 四层架构说明

| 层级 | 后缀示例 | 职责 |
|------|----------|------|
| 体验层 | `-feign-com` | Feign接口封装，供其他服务调用 |
| 聚合层 | `-server-springcloud/huawei/tencent` | 服务启动编排，纯启动类 |
| 能力层 | `-server`, `-server-com`, `grp-*-api` | 业务实现(Controller/Service/DAO) |
| 底座层 | `grp-common`, `grp-util-com` | 通用基础设施 |

## 创建流程

### 1. 确认模块信息
- 模块名称（如：order、user、product）
- 需要哪些层级（默认全部四层）
- 是否需要三种框架适配（SpringCloud/华为/腾讯）

### 2. 目录结构模板

```
{module-name}/
├── {module-name}-feign-com/           # 体验层
│   ├── pom.xml
│   └── src/main/java/.../feign/
│       └── {Module}FeignClient.java
├── {module-name}-server-springcloud/  # 聚合层-SC
│   ├── pom.xml
│   └── src/main/java/.../
│       └── {Module}ServerSpringCloudApplication.java
├── {module-name}-server-huawei/       # 聚合层-华为
├── {module-name}-server-tencent/      # 聚合层-腾讯
├── {module-name}-server/              # 能力层-Controller
│   ├── pom.xml
│   └── src/main/java/.../controller/
│       └── {Module}Controller.java
├── grp-{module-name}-api/             # 能力层-API定义
│   ├── pom.xml
│   └── src/main/java/.../api/
│       ├── interfaces/{Module}Api.java
│       └── dto/
└── {module-name}-server-com/          # 能力层-业务实现
    ├── pom.xml
    └── src/main/java/.../
        ├── service/{Module}Service.java
        └── dao/{Module}Mapper.java
```

### 3. POM模板要点

**子模块parent配置**（注意relativePath）：
```xml
<parent>
    <groupId>com.example</groupId>
    <artifactId>four-layer-architecture-demo</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../../pom.xml</relativePath>
</parent>
```

**聚合层启动类**：
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class {Module}ServerSpringCloudApplication {
    public static void main(String[] args) {
        SpringApplication.run({Module}ServerSpringCloudApplication.class, args);
    }
}
```

### 4. 更新根POM

创建完成后，在根 `pom.xml` 的 `<modules>` 中添加：
```xml
<module>{module-name}/{module-name}-feign-com</module>
<module>{module-name}/{module-name}-server-springcloud</module>
<!-- ... 其他模块 -->
```

## 检查清单

- [ ] 所有pom.xml的relativePath正确
- [ ] 包名遵循 `com.example.{module}` 规范
- [ ] 聚合层模块依赖能力层模块
- [ ] 体验层依赖API定义模块
- [ ] 能力层依赖底座层模块

