# Service 接口/实现/非Service文件 分类判断指南

## 分类判断流程

```
Service 目录下的 Java 文件
    │
    ├── 检查是否为 interface 类型
    │   ├── 是 interface + 类名含 Service → Service 接口 → facade/
    │   ├── 是 interface + @FeignClient 注解 → Feign 客户端 → 保留原位
    │   └── 是 interface + 其他 → 非 Service 接口 → 保留原位
    │
    ├── 检查是否为 class 类型
    │   ├── 类名含 ServiceImpl + @Service 注解 → Service 实现 → impl/
    │   ├── 类名含 ServiceImpl（无注解） → Service 实现 → impl/
    │   ├── @Service 注解 + implements *Service → Service 实现 → impl/
    │   └── 其他 class → 非 Service 文件 → 保留原位
    │
    └── 非 interface 非 class → 保留原位
```

## 详细分类标准

### Service 接口判定（强制）

| 条件 | 判定 | 目标目录 |
|------|------|---------|
| `interface` + 类名以 `Service` 结尾（无论是否有I前缀） | Service 接口 | `service/facade/` |
| `interface` + 类名以 `I` 开头 + `Service` 结尾 | Service 接口 | `service/facade/` |
| `interface` + `@FeignClient` 注解 | Feign 客户端 | 保留原位 |
| `interface` + 其他用途 | 非 Service | 保留原位 |

**强制规则**：
- 所有业务子目录（如 `config/*/service/`、`view/*/service/`）下的Service接口**必须迁移**到 `service/facade/`
- 不判断"是否需要迁移"，只判断"是否符合Service接口定义"
- 类名以 `Service` 结尾即判定为Service接口，无需考虑是否有 `I` 前缀

### Service 实现判定

| 条件 | 判定 | 目标目录 | 备注 |
|------|------|---------|------|
| class + 类名以 ServiceImpl 结尾 | Service 实现 | service/impl/ | - |
| class + @Service 注解 + implements I*Service | Service 实现 | service/impl/ | - |
| class + @Service 注解 + 无 Service 接口 | **保留原位** | 不迁移 | 在报告中标注 |
| class + 类名以 Service 结尾（非 ServiceImpl） + 无 @Service | **保留原位** | 不迁移 | 在报告中标注 |
| class + 类名以 Service 结尾（非 ServiceImpl） + 有 @Service | Service 实现 | service/impl/ | 在报告中标注建议改名 |

**规则优先级**：按上表从上到下匹配，命中第一条即停止。不允许"综合分析判断"。

### 非 Service 文件判定

| 文件类型 | 特征 | 处理方式 |
|---------|------|---------|
| 常量类 | 类名含 Constant/Constants，全 static final 字段 | 保留在 `{business}/constant/` |
| 枚举类 | `enum` 类型 | 保留在 `{business}/enums/` |
| 异常类 | extends Exception/RuntimeException | 保留在 `{business}/exception/` |
| 工具类 | 类名含 Util/Utils/Helper，全 static 方法 | 保留在 `{business}/util/` |
| Feign 客户端 | `@FeignClient` 注解的 interface | 保留在 `feign/` 或原位 |
| 业务模型 | DTO/VO/内部模型 | 保留在 `{business}/model/` |

## 边界情况处理

| 场景 | 处理方式 |
|------|---------|
| Service 接口无 `I` 前缀（如 `ElementService`） | 仍归入 `facade/`，不强制改名 |
| 一个文件中同时定义接口和实现（内部类） | 不迁移，在报告中标注 |
| 抽象 Service 基类（`AbstractXxxService`） | 归入 `impl/`，作为实现层基础 |
| Service 实现不继承接口（直接 `@Service`） | 归入 `impl/`，在报告中标注建议提取接口 |
| 跨模块共享的 Service 接口 | 评估影响范围后执行，在报告中标注 |
