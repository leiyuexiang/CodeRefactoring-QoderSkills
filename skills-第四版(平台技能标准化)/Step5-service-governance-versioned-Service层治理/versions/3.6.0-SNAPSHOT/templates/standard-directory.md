# facade/impl 标准目录结构模板

## Service 层标准目录结构

```
service/
├── facade/                       # 服务接口定义（统一存放所有 I*Service）
│   ├── IElementService.java
│   ├── IBooksetService.java
│   ├── IAgencyService.java
│   ├── IOrgService.java
│   └── IUserService.java
├── impl/                         # 服务实现（统一存放所有 *ServiceImpl）
│   ├── ElementServiceImpl.java
│   ├── BooksetServiceImpl.java
│   ├── AgencyServiceImpl.java
│   ├── OrgServiceImpl.java
│   └── UserServiceImpl.java
└── {business}/                   # 非 Service 文件保留原业务子包
    ├── constant/                 # 业务常量
    ├── enums/                    # 业务枚举
    ├── exception/                # 业务异常
    ├── util/                     # 业务工具
    └── model/                    # 业务内部模型
```

## 接口命名规范

| 类型 | 命名规则 | 示例 |
|------|---------|------|
| Service 接口 | `I` + 业务名 + `Service` | `IElementService` |
| Service 实现 | 业务名 + `ServiceImpl` | `ElementServiceImpl` |

## 重构前常见结构（不规范）

### 模式一：按业务子目录分散

```
service/
├── basedata/
│   ├── IElementService.java       # 接口和实现混放
│   └── impl/
│       └── ElementServiceImpl.java
├── bookset/
│   ├── IBooksetService.java
│   └── impl/
│       └── BooksetServiceImpl.java
└── agencyManager/
    ├── IAgencyService.java
    └── impl/
        └── AgencyServiceImpl.java
```

### 模式二：接口在根目录

```
service/
├── IElementService.java           # 接口散放在根目录
├── IBooksetService.java
└── impl/
    ├── ElementServiceImpl.java
    └── BooksetServiceImpl.java
```

### 模式三：混合模式

```
service/
├── IUserService.java              # 部分在根目录
├── basedata/
│   ├── IElementService.java       # 部分在业务子目录
│   ├── constant/
│   └── impl/
│       └── ElementServiceImpl.java
└── impl/
    └── UserServiceImpl.java       # 部分实现已在 impl/
```

## 非 Service 文件处理规则

| 文件类型 | 处理方式 | 说明 |
|---------|---------|------|
| Service 接口 | 迁入 `facade/` | 本次修复范围 |
| Service 实现 | 迁入 `impl/` | 本次修复范围 |
| 常量类 (constant/) | **保留原位** | 不在本次迁移范围 |
| 枚举类 (enums/) | **保留原位** | 不在本次迁移范围 |
| 异常类 (exception/) | **保留原位** | 不在本次迁移范围 |
| 工具类 (util/) | **保留原位** | 不在本次迁移范围 |
| Feign 客户端 | **保留原位** | 不属于 Service 接口 |
