# S2 检查规则清单

## 全局前置规则：独立业务域模块豁免

在执行 S2-01 至 S2-08 所有检查之前，先识别项目中的**独立业务域模块**并将其排除在 Step4 的检查范围之外。

**独立业务域模块判定标准**（同时满足以下**全部**条件）：
1. 该目录不在任何 `config/{business}/` 结构内
2. 该目录自身包含以下结构中的至少 2 层：`entity/`（或 `model/`）、`mapper/`（或 `dao/`）、`service/`
3. 该目录不是 `controller/`、`service/`、`dao/`、`model/` 这四个标准层级目录之一

**处理方式**：
- 在检查报告中标注为 **INFO**（信息），提示"发现独立业务域模块 {dir}/，其内部结构不在 Step4 治理范围内"
- 该目录及其所有子目录不参与 S2-01 至 S2-08 的任何检查

**示例**：
- `grp/pt/frame/view/` 包含 `entity/` + `mapper/` + `service/` → 判定为独立业务域模块 → 豁免
- `grp/pt/frame/config/bpm/` 仅包含 `dao/` + `service/` → 位于 `config/` 内 → 非独立模块 → 正常检查

---

## S2-01：目录命名规范（imp→impl）

**检查目标**：所有包含实现类的目录是否使用标准 `impl` 命名。

**违规模式**：
- `service/imp/` （应为 `service/impl/`）
- `dao/imp/` （应为 `dao/impl/`）
- `controller/imp/` （应为适当的功能子目录）
- `service/serviceImp/` 等合成词变体（应为 `service/impl/`）

**检查方法**：

**第一级（独立目录名精确匹配）**：
- 使用 Glob 搜索所有名为 `imp` 的目录（模式 `**/imp/`）
- 排除合法的 `imp` 开头包名（如 `import`）
- 检查是否存在 `imp`/`implement`/`impls` 等非标准变体

**第二级（合成词模式匹配）**：
- 使用 Glob 搜索目录名以 `Imp` 结尾的目录（模式 `**/*Imp/`）
- 过滤掉以 `Impl` 结尾的目录（这些是合法命名）
- 检查剩余匹配目录内是否包含 `.java` 文件
- 满足条件的目录判定为违规（如 `serviceImp/` 内含 Java 实现类）

**判定标准**：
- 存在 `*/imp/` 目录包含 Java 实现类 → **FAIL**
- 存在 `*/implement/` 等非标准命名 → **FAIL**
- 存在 `*Imp/`（非 `*Impl/`）目录包含 Java 实现类 → **FAIL**（合成词变体）

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

扫描 `dao/` 目录，对 `dao/` 根目录中的每个 `.java` 文件，按以下**确定性分类规则表**逐条匹配（匹配即停止）：

| 优先级 | 条件（仅基于文件名和所在目录，不读文件内容） | 分类 | 判定 |
|--------|------|------|------|
| 1 | 文件已在 `dao/impl/` 目录下 | 实现类 | **PASS**（已在正确位置） |
| 2 | 文件已在 `dao/mapper/` 目录下 | Mapper接口 | **PASS**（已在正确位置） |
| 3 | 文件已在 `dao/entity/` 目录下 | 实体类 | **PASS**（已在正确位置） |
| 4 | 文件在 `dao/` 根目录，文件名以 `Impl.java` 结尾 | 实现类 | **FAIL**（应移入 `dao/impl/`） |
| 5 | 文件在 `dao/` 根目录，文件名以 `Mapper.java` 结尾 | Mapper接口 | **WARN**（建议移入 `dao/mapper/`） |
| 6 | 文件在 `dao/` 根目录，文件名以 `Entity.java` 结尾 | 实体类 | **FAIL**（应移入 `dao/entity/`） |
| 7 | 文件在 `dao/` 根目录，文件名以 `I` 开头且第2个字符为大写字母（如 `IXxxDao.java`） | DAO接口 | **PASS**（接口保留在 `dao/` 根目录） |
| 8 | **兜底**：`dao/` 根目录中不匹配以上任何条件的 `.java` 文件 | 实现类（推定） | **FAIL**（应移入 `dao/impl/`） |

**重要说明**：
- 此分类完全基于文件名和目录位置，**不读取文件内容**，**不检查注解**（如 `@Repository`），确保多次执行的确定性
- 兜底规则（优先级8）将 `dao/` 根目录中所有无法匹配为接口/Mapper/Entity 的文件一律推定为实现类，覆盖了如 `BpmDao.java`、`YearDao.java` 等带 `@Repository` 但无 `Impl` 后缀的文件

---

## S2-03：DTO/VO/Query 分类归档

**检查目标**：数据传输对象是否按类型归入正确的子目录。

**标准结构**：
```
model/
├── dto/          # 数据传输对象（XxxDTO）
├── vo/           # 视图对象（XxxVO/XxxBO）
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
├── mapper/       # MyBatis Mapper 接口（唯一合法位置）
└── entity/       # 持久化实体
```

**检查方法**：
- 使用 Glob 扫描 `dao/` 下的子目录结构
- 检查 `mapper/` 是否作为独立包（如 `grp.pt.mapper`、`grp.frame.mapper`）而非 `dao/mapper/`
- 检查 `dao/` 下是否存在 `entity/` 子目录
- 检查 DAO 接口文件是否直接放在 `dao/` 根目录
- **冗余检查**：如果 `dao/mapper/` 已存在且包含 Mapper 文件，检查是否在 `dao/mapper/` 之外的位置（如独立 `mapper/` 包）同时存在 Mapper 接口文件副本

**判定标准**：
- `mapper/` 作为独立包（如 `grp.pt.mapper`）而非 `dao/mapper/` → **FAIL**
- `dao/mapper/` 已存在有效 Mapper 文件，但同时在其他位置（如独立 `mapper/` 包）存在 Mapper 副本 → **FAIL**（冗余 Mapper 必须删除）
- `dao/` 下不存在 `entity/` 子目录 → **FAIL**
- DAO 接口文件直接放在 `dao/` 根目录 → **WARN**
- `dao/imp/` 残留目录 → **FAIL**

---

## S2-07：Model 层 dto/vo/query/po 分类

**检查目标**：Model 层是否按照 `dto/`、`vo/`、`query/`、`po/` 四类分离，`model/` 根目录不得存在散落文件。

**标准结构**：
```
model/
├── dto/          # 数据传输对象（类名以 DTO/Dto 结尾）
├── vo/           # 视图对象（类名以 VO/Vo/BO/Bo 结尾）
├── query/        # 查询条件对象（类名以 Query/Param/QO/Qo 结尾）
└── po/           # 持久化/普通数据对象（类名以 PO/Po 结尾，以及所有无标准后缀的兜底分类）
```

### 后缀匹配算法（确定性定义）

**"类名以 X 结尾"的精确含义**：去除文件扩展名 `.java` 后，剩余的类名字符串的**最后 N 个字符**完全等于 X（**区分大小写**），其中 N 等于 X 的字符长度。

**示例**：
- `SysLogInfoDto.java` → 类名 `SysLogInfoDto` → 最后 3 字符 `Dto` = `Dto` ✓ → 匹配优先级1
- `GapConfigDataCheckModelVO.java` → 最后 2 字符 `VO` = `VO` ✓ → 匹配优先级2
- `ServerQueryParam.java` → 最后 5 字符 `Param` = `Param` ✓ → 匹配优先级3
- `OracleEntity.java` → 最后 6 字符 `Entity` = `Entity` ✓ → 匹配优先级4

### QO/Qo 归属判定（Phase 1 冻结快照）

在 Phase 1（扫描分析）阶段，一次性检查 `model/qo/` 目录是否存在。将结果记录为布尔标志 `HAS_QO_DIR`，在整个检查/修复流程中保持不变（冻结快照）。

- 若 `HAS_QO_DIR = true`（Phase 1 时 `model/qo/` 已存在）：QO/Qo 后缀文件应在 `model/qo/`
- 若 `HAS_QO_DIR = false`（Phase 1 时 `model/qo/` 不存在）：QO/Qo 后缀文件应在 `model/query/`

**禁止**在检查/修复过程中动态重新判定 `model/qo/` 是否存在。

**检查方法**：
- 扫描 `model/` 目录下的所有 Java 文件
- 按以下**确定性优先级**逐条检查每个文件是否在正确位置（匹配即停止）：
  1. 类名以 `DTO` 结尾 → 应在 `model/dto/`；不匹配则检查是否以 `Dto` 结尾 → 应在 `model/dto/`
  2. 类名以 `VO` 结尾 → 应在 `model/vo/`；不匹配则依次检查 `Vo`、`BO`、`Bo` → 应在 `model/vo/`
  3. 类名以 `Query` 结尾 → 应在 `model/query/`；不匹配则依次检查 `Param`、`QO`（按 `HAS_QO_DIR` 判定目标目录）、`Qo`（按 `HAS_QO_DIR` 判定目标目录）
  4. 类名以 `Entity` 结尾 → 应在 `dao/entity/`
  5. 类名以 `PO` 结尾 → 应在 `model/po/`；不匹配则检查是否以 `Po` 结尾 → 应在 `model/po/`
  6. **兜底规则**：以上均不匹配 → 应在 `model/po/`
- 检查 `model/` 根目录是否有散落的未分类文件

### 业务功能子包例外（精确判定算法）

`model/` 下的一个直接子目录被判定为"业务功能子包"（保持不动、不参与分类检查），当且仅当**同时满足**以下全部条件：

1. **目录名不在保留名单中**：目录名不是 `dto`、`vo`、`query`、`qo`、`po`、`entity` 中的任何一个（不区分大小写）
2. **包含至少 2 个 `.java` 文件**：统计该目录下**仅直接子级**（非递归）的 `.java` 文件数量 ≥ 2（使用 `Glob("model/{subdir}/*.java")` 而非递归搜索）

**计数方法**：仅统计该子目录的直接子级 `.java` 文件，不统计更深层子目录中的文件，不统计非 `.java` 文件。

**示例**：
- `model/bpm/` 含 `Bpm.java`, `BpmGroup.java`, `BpmNode.java`, `BpmNodeModule.java` → 4 个直接 .java 文件 ≥ 2 → 业务子包 ✓ → 保持不动
- `model/agent/` 仅含 `AgentRightEntity.java` → 1 个直接 .java 文件 < 2 → **不是**业务子包 → 其中文件参与分类检查
- `model/dto/` → 名称在保留名单中 → 非业务子包（是标准分类目录）→ 正常检查

**判定标准**：
- `model/` 下不存在 `po/` 子目录 → **WARN**
- `model/` 下不存在 `vo/` 子目录 → **FAIL**
- `model/` 下不存在 `query/` 子目录 → **FAIL**
- **`model/` 根目录存在未分类文件 → FAIL**（必须归入对应子目录）
- Entity 类放在 model/ 下而非 dao/entity/ → **FAIL**（Entity 属于 DAO 层持久化对象，放在 model/ 下是层级违规）

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
