# S2 检查规则清单

## S2-01：目录命名规范（imp→impl）

**检查目标**：所有包含实现类的目录是否使用标准 `impl` 命名。

**违规模式**：
- `service/imp/` （应为 `service/impl/`）
- `dao/imp/` （应为 `dao/impl/`）
- `controller/imp/` （应为适当的功能子目录）

**检查方法**：
- 使用 Glob 搜索所有以 `imp` 命名的目录
- 排除合法的 `imp` 开头包名（如 `import`）
- 检查是否存在 `imp`/`implement`/`impls` 等非标准变体

**判定标准**：
- 存在 `*/imp/` 目录包含 Java 实现类 → **FAIL**
- 存在 `*/implement/` 等非标准命名 → **FAIL**

---

## S2-02：DAO 层接口/实现分离

**检查目标**：DAO 层是否按接口/实现分离。

**标准结构**：
```
dao/
├── IXxxDao.java           (接口)
└── impl/
    └── XxxDaoImpl.java    (实现类)
```

或 MyBatis 模式：
```
dao/
├── mapper/
│   └── XxxMapper.java     (Mapper 接口)
└── entity/
    └── XxxEntity.java     (实体类)
```

**检查方法**：
- 扫描 `dao/` 目录
- 检查实现类（`@Repository` 注解或 `Impl` 后缀）是否在 `dao/impl/` 下
- 检查 Mapper 接口是否在 `dao/mapper/` 或 `dao/` 根目录

**判定标准**：
- DAO 实现类直接放在 `dao/` 根目录 → **FAIL**
- DAO 接口和实现混放 → **WARN**

---

## S2-03：DTO/VO/Query 分类归档

**检查目标**：数据传输对象是否按类型归入正确的子目录。

**标准结构**：
```
model/
├── dto/          # 数据传输对象（XxxDTO）
├── vo/           # 视图对象（XxxVO）
└── query/        # 查询条件对象（XxxQuery）
```

**检查方法**：
- 扫描 `model/` 目录下的所有 Java 文件
- 检查以 `DTO` 结尾的类是否在 `model/dto/` 下
- 检查以 `VO` 结尾的类是否在 `model/vo/` 下
- 检查以 `Query` 结尾的类是否在 `model/query/` 下
- 检查 `model/` 根目录是否有散落的 DTO/VO/Query 文件

**判定标准**：
- DTO 类不在 `model/dto/` 下 → **FAIL**
- VO 类不在 `model/vo/` 下 → **FAIL**
- Query 类不在 `model/query/` 下 → **WARN**

---

## S2-04：核心四层目录完整性

**检查目标**：每个业务模块是否包含 Controller/Service/DAO/Model 四个核心目录。

**标准结构**：
```
{module}/
├── controller/
├── service/
├── dao/
└── model/
```

**检查方法**：
- 识别各业务模块的根目录
- 检查是否缺失核心目录
- 检查是否存在不规范的额外目录

**判定标准**：
- 缺失核心目录 → **WARN**
- 存在非标准目录（如 `bean/`、`pojo/` 等代替标准目录） → **WARN**

---

## S2-05：resources/mapper 目录对应

**检查目标**：`resources/mapper/` 下是否有与 Java 模块对应的 XML 映射文件目录。

**检查方法**：
- 扫描 `resources/mapper/` 目录
- 检查是否按模块分组

**判定标准**：
- MyBatis XML 文件散放在 `mapper/` 根目录 → **WARN**

---

## S2-06：DAO 层 mapper/entity 分离

**检查目标**：DAO 层是否按照 `mapper/`（MyBatis Mapper 接口）和 `entity/`（持久化实体）分离。

**标准结构**：
```
dao/
├── mapper/       # MyBatis Mapper 接口
└── entity/       # 持久化实体
```

**检查方法**：
- 使用 Glob 扫描 `dao/` 下的子目录结构
- 检查 `mapper/` 是否作为独立包（如 `grp.pt.mapper`）而非 `dao/mapper/`
- 检查 `dao/` 下是否存在 `entity/` 子目录
- 检查 DAO 接口文件是否直接放在 `dao/` 根目录

**判定标准**：
- `mapper/` 作为独立包（如 `grp.pt.mapper`）而非 `dao/mapper/` → **FAIL**
- `dao/` 下不存在 `entity/` 子目录 → **FAIL**
- DAO 接口文件直接放在 `dao/` 根目录 → **WARN**
- `dao/imp/` 残留目录 → **FAIL**

---

## S2-07：Model 层 dto/vo/query 分类

**检查目标**：Model 层是否按照 `dto/`、`vo/`、`query/` 三类分离。

**标准结构**：
```
model/
├── dto/          # 数据传输对象
├── vo/           # 视图对象
└── query/        # 查询条件对象
```

**检查方法**：
- 扫描 `model/` 目录下的所有 Java 文件
- 检查以 `DTO` 结尾的类是否在 `model/dto/` 下
- 检查以 `VO` 结尾的类是否在 `model/vo/` 下
- 检查以 `Query`/`Param` 结尾的类是否在 `model/query/` 下
- 检查 `model/` 根目录是否有散落的未分类文件

**判定标准**：
- `model/` 下不存在 `vo/` 子目录 → **FAIL**
- `model/` 下不存在 `query/` 子目录 → **FAIL**
- Model 根目录存在未分类文件 → **WARN**
- Entity 类放在 model/ 下而非 dao/entity/ → **WARN**

---

## S2-08：公共模块结构检查

**检查目标**：公共模块（common）是否包含标准子目录。

**标准子目录**：`config/`、`util/`、`exception/`、`constant/`、`enums/`、`aop/`、`feign/`

**检查方法**：
- 扫描 common 模块下的一级子目录
- 检查 `feign/` 下是否有 `client/` + `fallback/` 分离
- 检查配置类是否散落在非 `common/config/` 位置

**判定标准**：
- 缺少 `config/`、`util/`、`exception/` 等必要子目录 → **WARN**
- `feign/` 下不存在 `client/` + `fallback/` 分离 → **WARN**
- 配置类散落在非 `common/config/` 位置 → **WARN**
