# S2 修复规范

## 修复规范一：目录命名修正（imp → impl）

### 操作步骤

1. 识别所有 `imp/` 目录下的 Java 文件
2. 在 `impl/` 目录下创建对应文件（更新 package 声明）
3. 使用 Grep 搜索全代码库中引用旧包路径的 import 语句
4. 逐一更新 import 语句
5. 删除原 `imp/` 目录下的文件
6. 删除空的 `imp/` 目录

### 关键操作

```
文件：XxxServiceImpl.java
修改：package grp.xx.service.imp → package grp.xx.service.impl

引用方：
修改：import grp.xx.service.imp.XxxServiceImpl → import grp.xx.service.impl.XxxServiceImpl
```

### 注意事项

- 修改前先用 Grep 统计所有受影响的文件数量
- 某些 `imp` 目录可能有特殊含义（如 `import` 的缩写），需结合上下文判断
- 确保 `impl/` 目录不存在同名文件冲突

---

## 修复规范二：DAO 层归位

### 实现类移入 impl/

#### 操作步骤

1. 识别 `dao/` 根目录中的实现类（`@Repository` 注解或 `Impl`/`DaoImpl` 后缀）
2. 移动到 `dao/impl/` 下
3. 更新 package 声明和所有 import 引用
4. 接口文件（`IXxxDao`）保留在 `dao/` 根目录

#### 注意事项

- Mapper 接口（MyBatis `@Mapper`）属于接口层，应保留在 `dao/mapper/` 或 `dao/` 根目录
- Entity 类应在 `dao/entity/` 或 `model/` 下
- JDBC 直接实现类（如 `JdbcXxxDaoImpl`）也应移入 `dao/impl/`

---

## 修复规范三：DTO/VO/Query 归类

### DTO 类移入 model/dto/

#### 操作步骤

1. 在 `model/` 根目录中搜索类名以 `DTO` 结尾的文件
2. 创建 `model/dto/` 目录（如不存在）
3. 将 DTO 文件移入 `model/dto/`
4. 更新 package 声明和所有 import 引用

### VO 类移入 model/vo/

操作步骤同 DTO，目标目录为 `model/vo/`

### Query 类移入 model/query/

操作步骤同 DTO，目标目录为 `model/query/`

### 注意事项

- 某些项目中 DTO 同时承担 VO 功能，此时不强制拆分
- 如果 DTO 类在 `common` 公共模块中被多模块引用，移动后需确保所有模块的 import 都已更新
- Entity 类不属于 DTO，不应移入 `model/dto/`

---

## 修复规范四：创建缺失目录

当核心目录缺失时，仅创建目录即可（不创建空文件）：

```
{module}/
├── controller/    (如缺失则创建)
├── service/
│   └── impl/      (如缺失则创建)
├── dao/
│   └── impl/      (如缺失则创建)
└── model/
    ├── dto/       (如缺失则创建)
    └── vo/        (如缺失则创建)
```

---

## 修复规范五：DAO 层 mapper/entity 分离

### 修复策略

1. 将独立的 `mapper/` 包（如 `grp.pt.mapper`）迁入 `dao/mapper/`
2. 创建 `dao/entity/` 目录，将持久化实体归入

### 操作步骤

1. 创建 `dao/mapper/` 目录
2. 将 `grp.pt.mapper` 下所有 Mapper 接口移入 `dao/mapper/`
3. 创建 `dao/entity/` 目录
4. 将 Entity 类从其他位置移入 `dao/entity/`
5. 更新 `package` 声明和所有 `import` 引用
6. 更新 MyBatis XML 中的 namespace 引用
7. 更新 `@MapperScan` 注解的 basePackages
8. 删除空的原目录

### 注意事项

- **Mapper namespace 变更**：MyBatis XML 中的 `namespace` 需同步更新
- **Spring 扫描路径**：`@MapperScan` 注解的 basePackages 需同步更新
- 影响面较广，需全面评估后执行

---

## 修复规范六：Model 层 dto/vo/query 分类

### 修复策略

在 `model/` 下创建 `dto/`、`vo/`、`query/` 子目录，将现有文件按类型归档。

### 分类标准

| 类型 | 目标目录 | 判定依据 |
|------|---------|---------|
| DTO | `model/dto/` | 类名含 DTO 后缀，或用于服务间数据传递 |
| VO | `model/vo/` | 类名含 VO 后缀，或用于 Controller 返回 |
| Query | `model/query/` | 类名含 Query/Param 后缀，或用于查询条件封装 |
| Entity | `dao/entity/` | 类名含 Entity 后缀，或映射数据库表 |

### 操作步骤

1. 创建 `model/vo/` 和 `model/query/` 目录（如不存在）
2. 分析 `model/` 根目录每个文件的用途
3. 按分类标准将文件移入对应子目录
4. Entity 类移入 `dao/entity/`
5. 更新 `package` 声明和所有 `import` 引用

### 注意事项

- 某些项目中 DTO 同时承担 VO 功能，此时不强制拆分
- 如果 DTO 类在公共模块中被多模块引用，移动后需确保所有模块的 import 都已更新
- Entity 类不属于 DTO，不应移入 `model/dto/`

---

## 执行操作规范

1. **按层级分批处理**：先处理 DAO 层（命名修正→归位→mapper/entity分离），再处理 Model 层（dto/vo/query归类），最后处理缺失目录
2. **每批处理内按文件逐个操作**：避免大规模并行修改导致混乱
3. **import 联动更新**：每迁移一个文件后立即更新所有引用方
4. **标记注释**：所有修改的代码块添加 AI 代码标记

> 文件迁移标准流程详见 → [examples/migration-flow.md](../examples/migration-flow.md)
