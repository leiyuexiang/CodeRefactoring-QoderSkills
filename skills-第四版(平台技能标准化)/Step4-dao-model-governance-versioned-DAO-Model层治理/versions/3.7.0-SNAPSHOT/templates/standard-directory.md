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
│   ├── vo/                        # 视图对象（含 VO/Vo/BO/Bo 后缀）
│   │   ├── XxxVO.java
│   │   └── XxxBO.java
│   ├── query/                     # 查询条件对象
│   │   └── XxxQuery.java
│   └── po/                        # 持久化/无后缀兜底对象
│       └── XxxPO.java
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

### DAO 层文件分类规则（确定性，仅基于文件名）

| 优先级 | 条件 | 分类 | 位置 |
|--------|------|------|------|
| 1 | 已在 dao/impl/ 下 | 实现类 | 保持原位 |
| 2 | 已在 dao/mapper/ 下 | Mapper接口 | 保持原位 |
| 3 | 已在 dao/entity/ 下 | 实体类 | 保持原位 |
| 4 | 文件名以 `Impl.java` 结尾 | 实现类 | → dao/impl/ |
| 5 | 文件名以 `Mapper.java` 结尾 | Mapper接口 | 保持或 → dao/mapper/ |
| 6 | 文件名以 `Entity.java` 结尾 | 实体类 | → dao/entity/ |
| 7 | 文件名以 `I` 开头且第2字符为大写 | DAO接口 | 保持在 dao/ 根目录 |
| 8 | **兜底** | 实现类（推定） | → dao/impl/ |

## Model 层标准结构

```
model/
├── dto/                           # 数据传输对象
│   └── XxxDTO.java                # 类名以 DTO/Dto 结尾
├── vo/                            # 视图对象
│   ├── XxxVO.java                 # 类名以 VO/Vo 结尾
│   └── XxxBO.java                 # 类名以 BO/Bo 结尾
├── query/                         # 查询条件对象
│   └── XxxQuery.java              # 类名以 Query/Param/QO/Qo 结尾
└── po/                            # 持久化/无后缀兜底对象
    └── XxxPO.java                 # 类名以 PO/Po 结尾，或无法匹配其他后缀的兜底
```

### Model 分类判定标准（确定性优先级匹配链）

> **确定性原则**：分类完全依据类名后缀进行机械匹配，禁止通过阅读文件内容、分析类的用途来决定分类。按优先级从高到低逐级匹配，首次命中即确定归属。

**后缀匹配算法精确定义**：

"类名以 X 结尾"的含义：去除文件扩展名 `.java` 后，剩余的类名字符串的**最后 N 个字符**完全等于 X（**区分大小写**），其中 N 等于 X 的字符长度。每个优先级的多个后缀按指定顺序逐一检查，首次匹配即停止。

| 优先级 | 后缀（按检查顺序） | 目标目录 | 示例 |
|--------|----------|---------|------|
| 1 | `DTO` → `Dto` | `model/dto/` | UserDTO.java → model/dto/、SysLogInfoDto.java → model/dto/ |
| 2 | `VO` → `Vo` → `BO` → `Bo` | `model/vo/` | UserVO.java → model/vo/、ElementBo.java → model/vo/、UiViewBO.java → model/vo/ |
| 3 | `Query` → `Param` → `QO` → `Qo` | `model/query/`（QO/Qo 按 HAS_QO_DIR 判定） | UserQuery.java → model/query/、ServerQueryParam.java → model/query/ |
| 4 | `Entity` | `dao/entity/` | UserEntity.java → dao/entity/ |
| 5 | `PO` → `Po` | `model/po/` | UserPO.java → model/po/ |
| 6 | **兜底**：以上均不匹配 | `model/po/` | Module.java → model/po/、SysConfig.java → model/po/ |

### 禁止行为

- **禁止**通过阅读文件内容来判断分类
- **禁止**通过分析类的用途、继承关系来判断分类
- **禁止**将无后缀文件留在 `model/` 根目录

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
├── query/        (如缺失则创建)
└── po/           (如缺失则创建)
```
