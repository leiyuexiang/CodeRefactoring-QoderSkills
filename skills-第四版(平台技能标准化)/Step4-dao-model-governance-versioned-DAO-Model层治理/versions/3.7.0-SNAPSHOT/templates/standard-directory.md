# S2 标准目录结构模板

## 业务模块标准目录结构（DAO-Model 层重点）

```
{module}/
├── controller/                    # Controller 层（详见 S4）
│   ├── custom/                    # 外部接口
│   └── common/                    # 内部接口
├── service/                       # Service 层（详见 S3）
│   ├── facade/                    # 服务接口
│   └── impl/                      # 服务实现
├── dao/                           # DAO 层 ← S2 重点
│   ├── mapper/                    # MyBatis Mapper 接口
│   │   └── XxxMapper.java
│   ├── entity/                    # 持久化实体
│   │   └── XxxEntity.java
│   ├── IXxxDao.java               # DAO 接口（传统模式）
│   └── impl/                      # DAO 实现类（传统模式）
│       └── XxxDaoImpl.java
├── model/                         # Model 层 ← S2 重点
│   ├── dto/                       # 数据传输对象
│   │   └── XxxDTO.java
│   ├── vo/                        # 视图对象
│   │   └── XxxVO.java
│   └── query/                     # 查询条件对象
│       └── XxxQuery.java
├── constant/                      # 常量定义（可选）
└── enums/                         # 枚举定义（可选）
```

## DAO 层标准结构

### MyBatis 模式（推荐）

```
dao/
├── mapper/
│   ├── XxxMapper.java             # Mapper 接口（@Mapper 注解）
│   └── YyyMapper.java
└── entity/
    ├── XxxEntity.java             # 持久化实体类
    └── YyyEntity.java
```

### 传统 DAO 模式

```
dao/
├── IXxxDao.java                   # DAO 接口
├── impl/
│   └── XxxDaoImpl.java            # 实现类（@Repository 注解）
├── mapper/
│   └── XxxMapper.java             # Mapper 接口（如混合使用）
└── entity/
    └── XxxEntity.java
```

## Model 层标准结构

```
model/
├── dto/                           # 数据传输对象
│   └── XxxDTO.java                # 类名以 DTO 结尾，用于服务间数据传递
├── vo/                            # 视图对象
│   └── XxxVO.java                 # 类名以 VO 结尾，用于 Controller 返回
└── query/                         # 查询条件对象
    └── XxxQuery.java              # 类名以 Query/Param 结尾，用于查询条件封装
```

### Model 分类判定标准

| 类型 | 目标目录 | 判定依据 |
|------|---------|---------|
| DTO | `model/dto/` | 类名含 DTO 后缀，或用于服务间数据传递 |
| VO | `model/vo/` | 类名含 VO 后缀，或用于 Controller 返回 |
| Query | `model/query/` | 类名含 Query/Param 后缀，或用于查询条件封装 |
| Entity | `dao/entity/` | 类名含 Entity 后缀，或映射数据库表（不应放在 model/ 下） |

## 公共模块标准目录结构

```
common/
├── config/                        # 配置类（@Configuration）
├── constant/                      # 全局常量
├── util/                          # 工具类
├── exception/                     # 异常处理
├── enums/                         # 全局枚举
├── aop/                           # 切面
├── feign/                         # 远程调用
│   ├── client/                    # Feign 客户端接口
│   └── fallback/                  # 降级实现
└── model/                         # 全局模型
    ├── dto/
    └── vo/
```

## 缺失目录补建规则

仅创建目录，不创建空文件：

```
dao/
├── mapper/       (如缺失则创建)
└── entity/       (如缺失则创建)

model/
├── dto/          (如缺失则创建)
├── vo/           (如缺失则创建)
└── query/        (如缺失则创建)
```
