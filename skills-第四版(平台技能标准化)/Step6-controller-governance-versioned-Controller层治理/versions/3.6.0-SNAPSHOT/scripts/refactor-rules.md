# S4 修复规范（统一迁移公式）

## 概述

本修复规范采用**统一迁移公式**，所有 Controller 文件使用同一套分类和迁移算法处理。不存在针对不同位置（根目录、非标准子目录等）的独立处理规范。

---

## 修复总流程

```
Phase 1: 扫描分析（确定迁移清单）
    ↓
Phase 2: 生成修复计划（表格形式）
    ↓
Phase 3: 用户确认
    ↓
Phase 4: 逐项执行迁移（标准迁移流程 × N）
    ↓
Phase 5: 验证与清理
```

---

## Phase 1: 扫描分析

### 步骤 1.0: 门控检查强制约束（GATE-ENFORCE）

> **以下规则为 Phase 1 全局前置约束，适用于所有扫描步骤（1.1.1~1.1.3）。任何候选文件必须同时满足全部准入条件并且不命中任何排除条件，才能计入 Controller 清单。**

#### 准入条件（必须满足至少一项）

| 编号 | 条件 | 验证方式 |
|------|------|---------|
| GATE-ADMIT-01 | 文件包含**未被注释**的 `@RestController` 注解 | Grep 搜索 `@RestController`，排除 `//` 或 `/*` 开头的行 |
| GATE-ADMIT-02 | 文件包含**未被注释**的 `@Controller` 注解 | Grep 搜索 `@Controller`，排除 `//` 或 `/*` 开头的行 |

#### 排除条件（命中任一项即排除）

| 编号 | 条件 | 说明 |
|------|------|------|
| GATE-EXCLUDE-01 | 文件包含 `@FeignClient` 注解 | Feign 客户端，不是 Controller |
| GATE-EXCLUDE-02 | package 路径包含 `rpc` 或 `feignclient` | RPC/Feign 相关包 |
| GATE-EXCLUDE-03 | `@RestController`/`@Controller` 注解被注释（行首有 `//` 或位于 `/* */` 块内） | 被注释的注解不算有效注解 |
| GATE-EXCLUDE-04 | 文件**仅有** `@Service`、`@Component`、`@Repository` 注解而**无** `@RestController`/`@Controller` | 即使文件名含 "Controller"，无有效 Controller 注解则不是 Controller |

#### 门控检查执行要求

| 编号 | 规则 |
|------|------|
| GATE-ENFORCE-01 | **文件名不等于判定标准**：文件名包含 "Controller" 不代表它是 Controller。判定标准**唯一且仅为**是否存在未被注释的 `@RestController` 或 `@Controller` 注解 |
| GATE-ENFORCE-02 | **逐文件 Grep 验证**：每个候选文件必须通过 Grep 工具实际验证注解存在性，不得凭文件名或目录位置推断 |
| GATE-ENFORCE-03 | **记录有效注解行号**：每个通过门控的文件必须在扫描报告中记录其有效注解的行号（如 "line 30: @RestController"），作为审计依据 |
| GATE-ENFORCE-04 | **排除项明细记录**：每个被排除的文件必须在报告中记录排除原因（如 "BpmController.java → 排除：无 @RestController/@Controller 注解"），确保可追溯 |

#### 门控反面示例（必须排除的文件）

以下为真实案例中**不应被迁移**但曾被错误迁移的文件：

| 文件名 | 实际注解 | 正确处理 | 错误处理 |
|--------|---------|---------|---------|
| `BpmController.java` | 无任何 Spring 注解 | 不迁移，保留原位 | ❌ 错误迁移到 `custom/bpm/` |
| `GXTPSControllerExt.java` | `@Service` | 不迁移，保留原位 | ❌ 错误迁移到 `custom/user/` |
| `UserLogController.java` | `@Service` | 不迁移，保留原位 | ❌ 错误迁移到 `custom/user/` |
| `JdSignController.java` | `//@Controller`（注释） | 不迁移，保留原位 | ❌ 错误迁移到 `custom/user/` |
| `YySignController.java` | `//@RestController`（注释） | 不迁移，保留原位 | ❌ 错误迁移到 `custom/user/` |
| `UserControllerTrmp.java` | `//@RestController`（注释） | 不迁移，保留原位 | ❌ 错误迁移到 `custom/user/` |

> **核心原则**：只迁移**真正的 Controller**（有活跃的 `@RestController` 或 `@Controller` 注解）。文件名含 "Controller" 但无有效注解的文件（如 Service 层、工具类、被注释的备份文件）**绝对不迁移**。

---

### 步骤 1.1: 四轮逐区扫描 Controller 文件（强制分区扫描）

**重要约束：必须采用四轮逐区扫描策略，不得使用单次全局 Grep 替代。每轮扫描必须独立输出结果后才进入下一轮。**

**重要约束：每轮扫描必须严格执行步骤 1.0 的门控检查，逐文件验证注解有效性，不得凭文件名推断。**

---

#### 步骤 1.1.1: 扫描 config/ 源目录

对 `config/` 下的**每个子目录**独立执行 Grep 搜索，查找包含 `@RestController` 或 `@Controller` 注解的 Java 文件。

**扫描范围**（两种文件放置模式都必须覆盖）：
- `config/{业务}/controller/` — 有 controller 子包的标准模式
- `config/{业务}/` — 无 controller 子包，Controller 直接在业务包下（如 `config/ssoplatform/SsoConfigController.java`、`config/eleprotalststemcontroller/ElePortalSysController.java`）
- `config/icontroller/` — I*Controller 接口文件

**排除条件**（与步骤 1.0 门控检查一致，GATE-ADMIT + GATE-EXCLUDE 全部适用）：
1. 排除包含 `@FeignClient` 注解的文件（GATE-EXCLUDE-01）
2. 排除 package 路径中包含 `rpc` 或 `feignclient` 的文件（GATE-EXCLUDE-02）
3. 排除被注释掉的注解（如 `//@RestController`）（GATE-EXCLUDE-03）
4. 排除仅有 `@Service`/`@Component`/`@Repository` 注解而无 `@RestController`/`@Controller` 的文件（GATE-EXCLUDE-04）

**必须输出明细表**（含注解行号，满足 GATE-ENFORCE-03/04）：
```
| config/ 子目录 | Controller 数量 | 文件列表（含注解行号） |
|---------------|----------------|----------------------|
| config/user/controller/ | 17 | ACFTUserController(L25:@RestController), AgencyUserController(L18:@RestController), ... |
| config/servermodule/controller/ | 6 | ModuleController(L12:@RestController), ... |
| config/ssoplatform/ | 3 | SsoConfigController(L20:@RestController), ... |
| config/icontroller/ | 0 | （无 @RestController/@Controller 注解，门控过滤，不计入）|
| ... | ... | ... |

门控排除文件：
| 文件 | 排除原因 |
|------|---------|
| BpmController.java | 无 @RestController/@Controller 注解（GATE-EXCLUDE-04） |
| UserLogController.java | 仅有 @Service 注解（GATE-EXCLUDE-04） |
| JdSignController.java | @Controller 被注释 //@Controller（GATE-EXCLUDE-03） |
| ... | ... |

config/ 扫描完成：共发现 Nc 个 Controller，分布在 Dc 个子目录中，排除 Ec 个非 Controller 文件
```

> **注意**：`config/icontroller/` 下的 I*Controller 文件通常为接口定义（无 `@RestController`/`@Controller` 注解），**门控检查不通过，不迁移，不计入 Nc**。若其中存在确实带注解的类，则照常通过门控进入分类链处理。

---

#### 步骤 1.1.2: 扫描 config2/ 源目录

对 `config2/` 下的每个子目录采用与步骤 1.1.1 相同的逻辑独立执行 Grep 搜索。

**扫描范围**（与步骤 1.1.1 一致，两种文件放置模式都必须覆盖）：
- `config2/{业务}/controller/` — 有 controller 子包的标准模式
- `config2/{业务}/` — 无 controller 子包，Controller 直接在业务包下

**排除条件**：与步骤 1.0 门控检查一致（GATE-ADMIT + GATE-EXCLUDE 全部适用）。

**必须输出明细表**（格式与步骤 1.1.1 一致，含注解行号和门控排除文件）：
```
| config2/ 子目录 | Controller 数量 | 文件列表（含注解行号） |
|----------------|----------------|----------------------|
| config2/admin/controller/ | 3 | AdminMenuController(L15:@RestController), ... |
| config2/agent/controller/ | 1 | AgentRightController(L22:@RestController) |
| ... | ... | ... |

config2/ 扫描完成：共发现 Nc2 个 Controller，分布在 Dc2 个子目录中，排除 Ec2 个非 Controller 文件
```

---

#### 步骤 1.1.3: 扫描 controller/ 及其他目录（确定性扫描列表）

扫描以下**全部**位置中的 Controller 文件（目录不存在时自动跳过，不报错）：

**必须扫描的确定性列表**：
1. `controller/custom/` — 已迁移的外部接口 Controller（若存在）
2. `controller/common/` — 已迁移的内部接口 Controller（若存在）
3. `controller/` 下非 custom/common 的残留子目录（若存在）
4. `view/` — 视图相关 Controller
5. `install/` — 安装相关 Controller
6. `bpm/` — 工作流相关 Controller
7. `src/main/java/` 下任何其他未被 1.1.1 和 1.1.2 覆盖的位置

**强制兜底扫描（SCAN-FALLBACK）**：
> 在完成上述 1~6 项扫描后，**必须**对整个 `src/main/java/` 目录执行一次 Grep 搜索 `@RestController`/`@Controller`（排除注释行和 @FeignClient），将结果与 1.1.1~1.1.3 前 6 项的分区结果取差集。差集中的文件即为"其他位置"的 Controller，必须补充到 Nother 中。此兜底扫描确保**零遗漏**，不依赖目录名的人工枚举。

**排除条件**：与步骤 1.0 门控检查一致（GATE-ADMIT + GATE-EXCLUDE 全部适用）。

> **注意**：工程首次执行时 `controller/` 下可能完全没有文件，Nother = 0 是完全正常的。

**必须输出**（含兜底扫描结果）：
```
controller/ 及其他目录扫描完成：共发现 Nother 个 Controller
├── controller/custom/ 已迁移：X 个
├── controller/common/ 已迁移：Y 个
├── view/ 等其他确定性目录：Z 个
├── 兜底扫描补充发现：W 个
└── Nother = X + Y + Z + W
（Nother = 0 时表示无已迁移或残留文件，属于正常情况）
```

---

#### 步骤 1.1.4: 汇总与交叉验证（强制门控）

**此步骤为强制门控，不可跳过。只有通过验证后才可进入步骤 1.2。**

1. 汇总分区结果：`N = Nc + Nc2 + Nother`
2. 对 `src/main/java/` 执行一次完整的 `@RestController`/`@Controller` Grep（排除 @FeignClient 和注释），得到 `Nfull`
3. 校验 `N = Nfull`

**必须输出分区扫描结果表格**：
```
【分区扫描结果 - 强制校验】
| 源目录              | Controller 数量 |
|--------------------|----------------|
| config/            | Nc             |
| config2/           | Nc2            |
| controller/及其他   | Nother         |
| 全量验证            | Nfull          |

分区总计: Nc + Nc2 + Nother = N
全量校验: Nfull = N ✅
```

**门控规则**：
- 如果 `N ≠ Nfull` → 逐一排查差异文件，补全到扫描清单后重新汇总，**不得跳过**
- 如果 `Nc = 0` 且工程存在 `config/` 目录 → 发出 **CRITICAL** 警告并停止："config/ 目录存在但未发现任何 Controller，极大概率为扫描遗漏，必须逐子目录排查"
- 如果 `N < 10` 且工程 config/ 或 config2/ 下的子目录数 > 5 → 发出 **WARNING**："Controller 总数异常偏低，请确认所有子目录已覆盖"

---

#### 步骤 1.1.5:（已合并至步骤 1.1.4）

> 原"全量扫描完整性验证"步骤已合并至步骤 1.1.4 的交叉验证流程中。保留此编号以保持向后引用兼容。四轮逐区扫描（1.1.1~1.1.3）+ 交叉验证（1.1.4）已完全覆盖原 1.1.5 的验证目的。

---

### 步骤 1.2: 对每个 Controller 运行三级确定性分类链

对步骤 1.1.1~1.1.3 得到并经 1.1.4 验证的**全部** Controller 文件：

1. 提取 className（不含包路径、不含 .java）
2. 提取原始 package 声明
3. 运行三级分类链（详见 [templates/classification-guide.md](../templates/classification-guide.md)）：
   - Level 1: 在精确类名映射表中查找
   - Level 2: 按关键词映射表顺序匹配
   - Level 3: 默认归 custom，提取 businessGroup（**严格按 extractBusinessGroup 公式，禁止语义归并，详见 L3-PROHIBIT-01~03**）
4. 得到分类结果 `{category, subdirectory}`

### 步骤 1.3: 计算目标包路径

使用包路径转换公式（详见 [templates/classification-guide.md](../templates/classification-guide.md)）：

```
情况 A: 原始 package 包含 "config" 或 "config2" 段
  1. 将原始 package 按 "." 分割
  2. 查找 "config" 或 "config2" 段的索引 idx
  3. modulePrefix = 段[0..idx-1] 拼接
  4. targetPackage = modulePrefix + ".controller." + category + "." + subdirectory

情况 B: 原始 package 不包含 "config"/"config2"，但包含 "controller" 段
  1. modulePrefix = "controller" 段之前的所有段拼接
  2. targetPackage = modulePrefix + ".controller." + category + "." + subdirectory

情况 C: 原始 package 既不包含 "config"/"config2" 也不包含 "controller" 段
  1. modulePrefix = package 去掉最后一段后的部分
  2. targetPackage = modulePrefix + ".controller." + category + "." + subdirectory
  3. 示例: grp.pt.frame.view → modulePrefix = grp.pt.frame
     → targetPackage = grp.pt.frame.controller.custom.view
```

> **注意**：三种情况的公式在 classification-guide.md 的"包路径转换公式"和"边界情况"章节中有详细定义。此处列出是为确保执行时不因包路径格式不同而遗漏。

### 步骤 1.4: 生成迁移清单

对比每个文件的当前 package 与目标 package：
- 一致 → 标记为 SKIP（无需迁移）
- 不一致 → 标记为 MIGRATE

**清单底部必须输出文件总数统计**：
```
全量扫描 Controller 总数：N
其中需要迁移（MIGRATE）：M
其中已在正确位置（SKIP）：K
（N = M + K，如不等则扫描有误，必须重新执行步骤 1.1）

来源分布：
  config/ 源：Nc 个
  config2/ 源：Nc2 个
  controller/ 及其他源：Nother 个
（Nc + Nc2 + Nother = N，如不等则扫描有遗漏，与步骤 1.1.4 校验口径一致）
```

---

## Phase 2: 生成修复计划

将迁移清单格式化为结构化表格，**必须包含以下列**：

```
| 序号 | Controller 类 | 当前 package | 目标 package | 分类级别 | 命中规则 | 操作 |
```

- **分类级别**：L1 / L2 / L3
- **命中规则**：具体匹配的条目（如 "L2-P6: className 匹配 ^I[A-Z].*Controller → api"）
- **操作**：MIGRATE 或 SKIP

同时输出统计信息：
```
总 Controller 文件数：N
需要迁移：M
无需迁移（已在正确位置）：K
非 Controller 文件（跳过）：J
```

---

## Phase 3: 用户确认

修复计划**必须获得用户确认后才开始执行**。

展示方式：
1. 输出 Phase 2 的完整修复计划表格
2. 输出统计信息
3. 等待用户明确确认（输入"确认"/"confirm"）

---

## Phase 4: 逐项执行迁移（标准迁移流程）

### Phase 4 进度播报规则

> **以下进度播报规则为强制要求，不可跳过。**

| 编号 | 规则 |
|------|------|
| P4-PROGRESS-01 | 每完成 **10 个文件**的迁移后，**必须**输出一次进度摘要（文件数 ≤ 10 时，完成后整体输出一次即可） |
| P4-PROGRESS-02 | 如果剩余文件数 > 0 但 AI 尝试进入 Phase 5，**必须停止**并输出警告："仍有 {N-X} 个文件未迁移，不得进入 Phase 5" |
| P4-PROGRESS-03 | Phase 4 完成判定条件：已处理数 = Phase 2 中 MIGRATE 总数 M，二者必须完全匹配 |

**进度摘要格式**：
```
迁移进度：已完成 X/M（Y%）
├── 本轮完成：[文件1, 文件2, ..., 文件10]
├── 累计完成：X 个
└── 剩余：M-X 个
```

### 标准迁移流程（9 步）

对每个标记为 MIGRATE 的 Controller 文件，执行以下 9 个步骤：

> **核心原则：复制而非生成（Copy-Not-Generate）**
> 迁移操作的本质是「读取原文件完整内容 → 仅修改 package 声明行 → 写入新位置」。
> **绝对禁止**在写入新文件时重新生成、简化或省略原文件中的任何内容（包括方法体、注释、import 语句、注解等）。
> 违反此原则等同于违反安全约束 S-03（不修改业务逻辑代码）。

```
步骤 1: Read 原文件（完整读取，不可省略）
    → 使用 read_file 工具获取完整文件内容（包括所有方法体、注释、import）
    → 记录原文件总行数 originalLineCount
    → ⚠️ 禁止仅读取部分内容或仅读取类签名

步骤 2: 修改 package 声明（仅此一行）
    → 将 package 行替换为目标 package
    → 仅修改 package 声明行，不修改其他任何内容
    → ⚠️ 禁止修改或删除方法体、字段、注解、注释中的任何字符

步骤 3: Write 新文件（完整写入 + 编码保留 + 行数校验）
    → 将修改后的完整内容写入目标路径
    → 目标路径 = 将 targetPackage 转换为目录结构 + "/" + ClassName.java
    → ⚠️ 编码保留：必须保持原文件的字符编码格式（如 UTF-8 BOM 则写入时也带 BOM）
    → ⚠️ 推荐使用 search_replace 工具（自动保留编码），而非 create_file（可能丢失 BOM）
    → ⚠️ 行数校验（强制）：写入后新文件行数与 originalLineCount 之差不得超过 ±2 行
      （仅允许 package 行长度变化和 import 增删导致的微小差异）
      如果差异 > 2 行，说明内容被意外删除或修改，**必须立即回退并重新执行步骤 1~3**

步骤 4: Grep 搜索引用
    → 在整个工程中搜索 import 原 package + ClassName 的所有文件
    → 记录所有引用方文件列表

步骤 5: Edit 更新 import 引用（双向更新）
    → 逐一修改引用方文件的 import 语句
    → 旧：import {原package}.{ClassName};
    → 新：import {目标package}.{ClassName};
    
    ⚠️ 双向引用更新（S-12 约束）：
    → 同时检查被迁移 Controller 自身的 import 语句中，是否引用了其他已迁移或待迁移 Controller 的旧路径
    → 如果存在，同步更新为对应的目标 package 路径
    → 即使被引用的 Controller B 尚未迁移，也应根据 Phase 2 计划表中 B 的目标 package 提前更新 A 中对 B 的 import 路径
    → 示例：UiViewCatelogController 已迁移，其 import 中引用 config.year.controller.YearController（也待迁移），
      则需将该 import 更新为 controller.custom.year.YearController

步骤 5.5: 检查 @ComponentScan 引用
    → Grep 搜索 @ComponentScan 注解中引用原 package 路径的配置类
    → 如果找到，更新 basePackages 值

步骤 5.6: 检查 Spring XML 配置引用
    → Grep 搜索 applicationContext*.xml、spring*.xml 中引用原 package 路径的位置
    → 如果找到，更新 component-scan base-package 值

步骤 6: Delete 原文件
    → 删除原位置的 Java 文件

步骤 7: 验证（含行数校验）
    → Grep 确认工程中不再存在对原 package 路径的引用
    → 如果发现残留引用，立即修复
    → ⚠️ 行数完整性校验（强制）：对比新文件行数与步骤 1 记录的 originalLineCount
      差值 > 2 则为 FAIL，必须回滚并重做
```

### 文件内容完整性保障规则（INTEGRITY）

> **以下规则为强制约束，违反任何一条即为严重缺陷。**

| 编号 | 规则 | 说明 |
|------|------|------|
| INTEGRITY-01 | **禁止重新生成文件内容** | 新文件必须是原文件的精确副本（仅 package 行和 import 行允许变更），禁止将原文件重写为空壳类、最小化实现或骨架代码 |
| INTEGRITY-02 | **行数校验** | 迁移后文件行数与原文件行数之差绝对值不得超过 2。差异 > 2 行时必须停止并报告错误 |
| INTEGRITY-03 | **编码保留** | 原文件为 UTF-8 with BOM（首3字节为 EF BB BF）时，新文件必须保留 BOM。推荐使用 search_replace 工具修改（自动保留编码），避免使用 create_file 工具重写整个文件 |
| INTEGRITY-04 | **内容哈希对比（推荐）** | 对原文件去除 package 行后的内容与新文件去除 package 行后的内容做对比，应当完全一致（仅 import 行因交叉引用更新可能不同） |

### 执行顺序

所有 MIGRATE 文件按以下顺序处理：
1. 先处理 common 类型（L1 和 L2 命中的文件数量较少）
2. 再处理 custom 类型（L3 默认的文件数量较多）
3. 在同一类型内，按 className 字母顺序处理

### 非 Controller 类处理规则

> **以下规则与步骤 1.0 门控检查互为补充。门控检查在扫描阶段过滤，此处在迁移阶段兜底。**

| 文件类型 | 处理方式 | 对应门控规则 |
|---------|---------|------------|
| 无 `@Controller`/`@RestController` 注解的 Java 类（如工具类、常量类、策略类） | **不迁移**，保留原位 | GATE-ADMIT |
| 文件名含 "Controller" 但无有效注解（如 `BpmController.java` 无注解） | **不迁移**，保留原位 | GATE-ENFORCE-01 |
| 仅有 `@Service`/`@Component`/`@Repository` 注解的文件（如 `UserLogController.java` 标注 `@Service`） | **不迁移**，保留原位 | GATE-EXCLUDE-04 |
| `@RestController`/`@Controller` 注解被注释掉的文件（如 `//@RestController`） | **不迁移**，保留原位 | GATE-EXCLUDE-03 |
| 仅有 `@FeignClient` 注解的接口 | **不迁移**，不是 Controller | GATE-EXCLUDE-01 |
| DAO/Service/Model 层文件 | **不迁移**，不属于 controller 包 | — |
| 整个文件内容被注释的备份文件（如 `UserControllerTrmp.java`） | **不迁移**，保留原位 | GATE-ADMIT |

### Phase 4 完成校验（Phase 4 → Phase 5 过渡门控）

**Phase 4 结束时必须输出以下完成校验，通过后才可进入 Phase 5**：
```
✅ Phase 4 完成校验：
  - Phase 2 计划迁移数：M
  - 实际已迁移数：X
  - 校验结果：M = X ✅（或 ❌ 差 Y 个，需回溯排查）
```

如果 `M ≠ X`，**禁止进入 Phase 5**，必须回溯查找遗漏文件并补充迁移。

---

## Phase 5: 验证与清理

### 5.1 完整性验证（四阶段）

#### 5.1.1 外部引用验证

1. 使用 Grep 搜索 `@RestController` 和 `@Controller` 注解，确认所有 Controller 都在 `controller/custom/` 或 `controller/common/` 下
2. 使用 Grep 搜索旧的 package 路径模式（如 `config.*.controller`），确认无残留引用

#### 5.1.2 内部交叉引用验证

对 `controller/custom/` 和 `controller/common/` 下的**每个已迁移 Controller 文件**，检查其 import 语句：
- 如果任何 import 仍包含 `config.{业务}.controller` 或 `config2.{业务}.controller` 等旧路径模式，标记为 **FAIL**
- 输出交叉引用检查表格：

```
| 文件 | 残留旧 import | 应更新为 |
|------|-------------|--------|
| UiViewCatelogController.java | import grp.pt.frame.config.year.controller.YearController | import grp.pt.frame.controller.custom.year.YearController |
```

如果存在 FAIL 项，**必须立即修复**后再继续。

#### 5.1.3 全局 import 一致性验证

在整个 `src/main/java/` 下 Grep 搜索所有已迁移 Controller 的旧 package 前缀：
1. 对于每个命中，检查是否是注释或字符串常量（排除误报）
2. 非注释/非字符串的命中必须修复

**输出验证报告**：
```
import 引用完整性验证：
├── 已检查文件数：N
├── 残留旧引用：X 处（必须为 0）
└── 详细列表：[若有残留则逐条列出]
```

#### 5.1.4 文件内容完整性验证（新增）

对每个已迁移 Controller 文件，执行行数完整性校验：
- 对比迁移后文件行数与 Phase 1 记录的原始行数
- 行数差异绝对值 > 2 则为 **FAIL**，说明文件内容被意外删除或重新生成

**输出验证表格**：
```
文件内容完整性校验：
| 文件 | 原始行数 | 迁移后行数 | 差值 | 结果 |
|------|---------|-----------|------|------|
| AdminMenuController.java | 159 | 159 | 0 | PASS ✅ |
| SynchronizeInfoController.java | 491 | 491 | 0 | PASS ✅ |
全部通过：✅ / 存在失败：❌（失败项必须回滚并重做）
```

### 5.2 空目录清理

迁移完成后，清理空目录：

**清理条件**（必须同时满足）：
- 目录位于原 `controller/` 相关路径下
- 目录下零 `.java` 文件
- 目录不是 `custom/` 或 `common/`（这两个即使为空也保留）

**清理方式**：从最深层目录开始向上逐级检查和删除。

### 5.3 输出验证报告

```
迁移完成统计：
  - 成功迁移：M 个文件
  - 跳过（已在正确位置）：K 个文件
  - 非 Controller 文件（未处理）：J 个文件
  - 清理空目录：D 个
  - 外部残留引用：0（如非0则需检查）
  - 内部交叉引用残留：0（如非0则需修复，见 S-12）
  - 全局 import 一致性：PASS（或 FAIL + 详情）
  - 文件内容完整性：PASS（或 FAIL + 行数异常文件列表，见 S-13）
  - 编码保留：PASS（或 FAIL + BOM丢失文件列表，见 S-14）
```
