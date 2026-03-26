# custom/common 标准目录结构模板与分组示例

## 标准目录结构

```
controller/
├── custom/               # 自定义接口（外部接口，面向前端/第三方）
│   ├── basedata/         # 基础数据管理
│   │   ├── ElementController.java
│   │   ├── ElementValueController.java
│   │   └── DirectoryController.java
│   ├── bookset/          # 账套管理
│   │   └── BooksetController.java
│   ├── agencyManager/    # 单位管理
│   │   └── AgencyController.java
│   ├── org/              # 组织管理
│   │   └── OrgController.java
│   └── user/             # 用户管理
│       └── UserController.java
└── common/               # 通用接口（内部接口，面向内部微服务）
    ├── api/              # 内部 API 接口
    │   └── InternalApiController.java
    ├── util/             # 工具/调试类
    │   └── CacheUtilController.java
    ├── notify/           # 通知
    │   └── NotifyController.java
    ├── sync/             # 数据同步
    │   └── DataSyncController.java
    └── health/           # 健康检查
        └── HealthCheckController.java
```

## custom/ 二级分组规范

custom/ 下按**业务域**分组：

| 二级目录 | 说明 | 典型 Controller |
|---------|------|----------------|
| basedata/ | 基础数据管理 | ElementController、ValueSetController |
| bookset/ | 账套管理 | BooksetController |
| agencyManager/ | 单位管理 | AgencyController |
| org/ | 组织管理 | OrgController |
| user/ | 用户管理 | UserController |
| report/ | 报表管理 | ReportController |

## common/ 二级分组规范

common/ 下按**功能类型**分组：

| 二级目录 | 说明 | 典型 Controller |
|---------|------|----------------|
| api/ | 内部 API 接口 | InternalApiController |
| util/ | 工具/调试类 | CacheUtilController |
| notify/ | 通知 | NotifyController |
| sync/ | 数据同步 | DataSyncController |
| health/ | 健康检查 | HealthCheckController |
| init/ | 初始化 | InitDataController |

## 分组上限

- 单个二级分组目录下的 Controller 文件数**不超过 10 个**
- 超过 10 个时建议进一步按子业务细分为三级目录
