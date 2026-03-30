# S2 修复规范

## 全局前置规则：独立业务域模块豁免

在执行修复规范一至修复规范六之前，先识别项目中的**独立业务域模块**并将其排除在 Step4 的修复范围之外。

**独立业务域模块判定标准**（同时满足以下**全部**条件）：
1. 该目录不在任何 `config/{business}/` 结构内
2. 该目录自身包含以下结构中的至少 2 层：`entity/`（或 `model/`）、`mapper/`（或 `dao/`）、`service/`
3. 该目录不是 `controller/`、`service/`、`dao/`、`model/` 这四个标准层级目录之一

**处理方式**：
- **完全跳过**该目录及其所有子目录，不做任何文件迁移或目录重命名
- import 更新阶段：如果其他位置的文件迁移影响了该模块中文件的 import 引用，仍需更新这些 import（仅限 import 行）

---

## 修复规范一：目录命名修正（imp → impl）

### 操作步骤

**第一级（独立目录名 `imp`）**：

1. 识别所有 `imp/` 目录下的 Java 文件
2. 在 `impl/` 目录下创建对应文件（更新 package 声明）
3. 使用 Grep 搜索全代码库中引用旧包路径的 import 语句
4. 逐一更新 import 语句
5. 删除原 `imp/` 目录下的文件
6. 删除空的 `imp/` 目录

**第二级（合成词变体 `*Imp`，非 `*Impl`）**：

1. 使用 Glob 搜索目录名以 `Imp` 结尾的目录（模式 `**/*Imp/`），过滤掉以 `Impl` 结尾的
2. 对匹配的目录（如 `serviceImp/`），将其直接重命名为 `impl/`（注意：不是重命名为 `serviceImpl/`，而是统一为 `impl/`）
3. 更新 package 路径：如 `.serviceImp.` → `.impl.`
4. 使用 Grep 搜索全代码库中引用旧包路径的 import 语句
5. 逐一更新 import 语句
6. 删除原目录

### 关键操作

```
文件：XxxServiceImpl.java
修改：package grp.xx.service.imp → package grp.xx.service.impl

引用方：
修改：import grp.xx.service.imp.XxxServiceImpl → import grp.xx.service.impl.XxxServiceImpl
```

合成词变体示例：
```
文件：IndexMessageServiceImpl.java
修改：package grp.xx.service.serviceImp → package grp.xx.service.impl

引用方：
修改：import grp.xx.service.serviceImp.IndexMessageServiceImpl → import grp.xx.service.impl.IndexMessageServiceImpl
```

### 注意事项

- 修改前先用 Grep 统计所有受影响的文件数量
- 某些 `imp` 目录可能有特殊含义（如 `import` 的缩写），需结合上下文判断
- 确保 `impl/` 目录不存在同名文件冲突
- 合成词变体（如 `serviceImp`）统一重命名为 `impl`，不保留前缀

---

## 修复规范二：DAO 层归位

### 确定性分类规则表

对 `dao/` 根目录中的每个 `.java` 文件，按以下规则逐条匹配（**仅基于文件名和所在目录，不读文件内容**）：

| 优先级 | 条件 | 分类 | 操作 |
|--------|------|------|------|
| 1 | 文件已在 `dao/impl/` 目录下 | 实现类 | 保持原位，无需操作 |
| 2 | 文件已在 `dao/mapper/` 目录下 | Mapper接口 | 保持原位，无需操作 |
| 3 | 文件已在 `dao/entity/` 目录下 | 实体类 | 保持原位，无需操作 |
| 4 | 文件在 `dao/` 根目录，文件名以 `Impl.java` 结尾 | 实现类 | 移入 `dao/impl/` |
| 5 | 文件在 `dao/` 根目录，文件名以 `Mapper.java` 结尾 | Mapper接口 | 保持在 `dao/` 或移入 `dao/mapper/` |
| 6 | 文件在 `dao/` 根目录，文件名以 `Entity.java` 结尾 | 实体类 | 移入 `dao/entity/` |
| 7 | 文件在 `dao/` 根目录，文件名以 `I` 开头且第2个字符为大写字母 | DAO接口 | 保持在 `dao/` 根目录 |
| 8 | **兜底**：`dao/` 根目录中其他所有 `.java` 文件 | 实现类（推定） | 移入 `dao/impl/` |

### 操作步骤

1. 按上方分类规则表扫描 `dao/` 根目录中的所有 `.java` 文件
2. 对需要移动的文件（优先级4、6、8），移入对应子目录
3. 更新 package 声明和所有 import 引用
4. 接口文件（优先级7）保留在 `dao/` 根目录

### 注意事项

- 此分类**不依赖 `@Repository` 注解检测**，完全基于文件名和目录位置判断，确保确定性
- Mapper 接口（MyBatis `@Mapper`）按文件名后缀 `Mapper.java` 识别，应保留在 `dao/mapper/` 或 `dao/` 根目录
- Entity 类按文件名后缀 `Entity.java` 识别，应在 `dao/entity/`
- JDBC 直接实现类（如 `JdbcXxxDaoImpl.java`，以 `Impl.java` 结尾）也应移入 `dao/impl/`
- 兜底规则将 `BpmDao.java`、`YearDao.java` 等无明确后缀标识的文件推定为实现类

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
    ├── vo/        (如缺失则创建)
    ├── query/     (如缺失则创建)
    └── po/        (如缺失则创建)
```

---

## 修复规范五：DAO 层 mapper/entity 分离

### 前置判定：Mapper 位置状态评估

在执行 Mapper 迁移前，**必须先评估**当前 Mapper 所在位置状态：

| 状态 | 条件 | 操作 |
|------|------|------|
| 状态A：仅在独立包 | Mapper 文件仅存在于独立包（如 `grp.frame.mapper`、`grp.pt.mapper`），`dao/mapper/` 不存在或为空 | 执行迁移：独立包 → `dao/mapper/` |
| 状态B：仅在 dao/mapper/ | Mapper 文件仅存在于 `dao/mapper/`，无独立 mapper 包 | **无需操作，跳过本规范** |
| 状态C：两处同时存在 | `dao/mapper/` 和独立 mapper 包同时存在 Mapper 文件 | 保留 `dao/mapper/` 中的版本，删除独立包中的副本，更新所有 import 引用指向 `dao/mapper/` |

**关键约束**：
- **禁止反向迁移**：绝对不得将 `dao/mapper/` 中的文件迁出到其他位置
- **禁止新建独立 mapper 包**：不得在 `dao/` 之外新建任何 `mapper/` 目录
- 目标位置始终且只能是 `dao/mapper/`

### 修复策略

1. 将独立的 `mapper/` 包（如 `grp.pt.mapper`）迁入 `dao/mapper/`
2. 创建 `dao/entity/` 目录，将持久化实体归入

### 操作步骤

1. **执行前置判定**，确认当前为状态 A 或状态 C（状态 B 跳过）
2. 创建 `dao/mapper/` 目录（如不存在）
3. 将独立 mapper 包下所有 Mapper 接口移入 `dao/mapper/`
4. 创建 `dao/entity/` 目录
5. 将 Entity 类从其他位置移入 `dao/entity/`
6. 更新 `package` 声明和所有 `import` 引用
7. 更新 MyBatis XML 中的 namespace 引用
8. 更新 `@MapperScan` 注解的 basePackages
9. 删除空的原目录
10. **验证无残留**：Grep 搜索旧包路径，确认全局无遗漏引用

### 注意事项

- **Mapper namespace 变更**：MyBatis XML 中的 `namespace` 需同步更新
- **Spring 扫描路径**：`@MapperScan` 注解的 basePackages 需同步更新
- 影响面较广，需全面评估后执行

---

## 修复规范六：Model 层 dto/vo/query/po 分类

### 修复策略

在 `model/` 下创建 `dto/`、`vo/`、`query/`、`po/` 子目录，将 `model/` 根目录散落文件按类名后缀归档。

### 后缀匹配算法（确定性定义）

**"类名以 X 结尾"的精确含义**：去除文件扩展名 `.java` 后，剩余的类名字符串的**最后 N 个字符**完全等于 X（**区分大小写**），其中 N 等于 X 的字符长度。

### QO/Qo 归属判定（Phase 1 冻结快照）

在 Phase 1（扫描分析）阶段，一次性检查 `model/qo/` 目录是否存在。将结果记录为布尔标志 `HAS_QO_DIR`，在整个修复流程中保持不变（冻结快照）。

- 若 `HAS_QO_DIR = true`：QO/Qo 后缀文件 → `model/qo/`
- 若 `HAS_QO_DIR = false`：QO/Qo 后缀文件 → `model/query/`

**禁止**在修复过程中动态重新判定 `model/qo/` 是否存在。

### 分类标准（确定性优先级，匹配即停止）

| 优先级 | 类型 | 目标目录 | 判定依据（仅看类名后缀，不分析文件内容） | 后缀检查顺序 |
|--------|------|---------|---------|---------|
| 1 | DTO | `model/dto/` | 类名以 `DTO`/`Dto` 结尾 | 先检查 `DTO`，不匹配再检查 `Dto` |
| 2 | VO | `model/vo/` | 类名以 `VO`/`Vo`/`BO`/`Bo` 结尾 | 按 `VO` → `Vo` → `BO` → `Bo` 顺序逐一检查 |
| 3 | Query | `model/query/` | 类名以 `Query`/`Param`/`QO`/`Qo` 结尾 | 按 `Query` → `Param` → `QO` → `Qo` 顺序逐一检查；其中 QO/Qo 按 `HAS_QO_DIR` 判定目标目录 |
| 4 | Entity | `dao/entity/` | 类名以 `Entity` 结尾 | 单一后缀 |
| 5 | PO | `model/po/` | 类名以 `PO`/`Po` 结尾 | 先检查 `PO`，不匹配再检查 `Po` |
| **6** | **PO（兜底）** | **`model/po/`** | **以上均不匹配的所有 `model/` 根目录散落文件**（包括无后缀域对象如 Module.java、含 Pojo/Result/Info/Data/Config/Mapping 等非标准后缀的类） | 无需后缀匹配 |

### 操作步骤

1. 创建 `model/dto/`、`model/vo/`、`model/query/`、`model/po/` 目录（如不存在）
2. **确定性分类**：对 `model/` 根目录的每个 Java 文件，严格按上方分类标准的**优先级顺序逐条匹配**，每个优先级内按指定的**后缀检查顺序**逐一检查，首次匹配即停止，将文件移入对应子目录
3. Entity 类移入 `dao/entity/`
4. 更新 `package` 声明和所有 `import` 引用
5. **验证无残留**：Grep 搜索旧包路径，确认 `model/` 根目录无散落的 Java 文件

**禁止行为**：禁止通过阅读文件内容、分析类的用途来决定分类。分类必须且只能依据类名后缀的 endsWith 匹配。这确保了多次执行的确定性和一致性。

### 业务功能子包例外（精确判定算法）

`model/` 下的一个直接子目录被判定为"业务功能子包"（保持不动、不参与分类归档），当且仅当**同时满足**以下全部条件：

1. **目录名不在保留名单中**：目录名不是 `dto`、`vo`、`query`、`qo`、`po`、`entity` 中的任何一个（不区分大小写）
2. **包含至少 2 个 `.java` 文件**：统计该目录下**仅直接子级**（非递归）的 `.java` 文件数量 ≥ 2（使用 `Glob("model/{subdir}/*.java")` 而非递归搜索）

**示例**：
- `model/bpm/` 含 4 个直接 .java 文件 ≥ 2 → 业务子包 ✓ → 保持不动
- `model/agent/` 仅含 1 个直接 .java 文件 < 2 → **不是**业务子包 → 其中文件参与分类

### 注意事项

- 如果 DTO/VO 类在公共模块中被多模块引用，移动后需确保所有模块的 import 都已更新
- Entity 类不属于 DTO，不应移入 `model/dto/`

---

## 执行操作规范

1. **按层级分批处理**：先处理 DAO 层（命名修正→归位→mapper/entity分离），再处理 Model 层（dto/vo/query/po归类），最后处理缺失目录
2. **每批处理内按文件逐个操作**：避免大规模并行修改导致混乱。每个文件迁移前**必须执行冲突预检**（详见 safety-constraints.md S-06 和 migration-flow.md 步骤 3）
3. **import 联动更新**：每迁移一个文件后立即更新所有引用方
4. **标记注释**：所有修改的代码块添加 AI 代码标记

### import 搜索范围定义

"全代码库"的精确定义为：**当前被修复模块的顶级父 POM 所在目录及其所有子目录**。

**确定搜索根目录的算法**：
1. 从被修复文件所在的 Maven 模块 `pom.xml` 开始
2. 向上追溯 `<parent>` 关系，直到找到最顶层的 `pom.xml`（没有 `<parent>` 或 `<parent>` 指向远程仓库）
3. 该顶层 `pom.xml` 所在目录即为搜索根目录
4. 在搜索根目录下递归 Grep 搜索旧包路径

**排除目录**：搜索时排除 `target/`、`.git/`、`node_modules/`、`.qoder/` 目录

**此判定在 Phase 1 扫描阶段一次性确定，修复过程中保持不变。**

> 文件迁移标准流程详见 → [examples/migration-flow.md](../examples/migration-flow.md)
