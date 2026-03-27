# S4 检查规则清单

## 检查级别定义

| 级别 | 含义 | 处理方式 |
|------|------|---------|
| **FAIL** | 不符合规范，必须修复 | 修复阶段自动处理 |
| **WARN** | 建议优化，不强制修复 | 仅在报告中标注，不自动处理 |
| **INFO** | 信息提示 | 仅在报告中展示 |

---

## S4-01：custom/common 一级目录存在性

**检查目标**：`{modulePrefix}/controller/` 下是否存在 `custom/` 和 `common/` 两个一级子目录。

**检查方法**：
- 使用 Glob 扫描 `controller/` 下的一级子目录
- 检查是否存在 `custom/` 目录
- 检查是否存在 `common/` 目录

**判定标准**：
- `controller/` 下不存在 `custom/` 子目录 → **FAIL**
- `controller/` 下不存在 `common/` 子目录 → **FAIL**
- 两个目录都存在 → **PASS**

---

## S4-02：Controller 归属正确性

**检查目标**：每个 Controller 文件是否位于三级确定性分类链计算出的正确位置。

**检查方法**：
1. 采用**四轮逐区扫描策略**定位所有 Controller 文件（详见 [scripts/refactor-rules.md 步骤 1.1.1~1.1.4](refactor-rules.md)）：
   - 第 1 轮：扫描 `config/` 下每个子目录
   - 第 2 轮：扫描 `config2/` 下每个子目录
   - 第 3 轮：扫描 `controller/` 及其他目录
   - 第 4 轮：全量交叉验证（分区总计 = 全量 Grep 结果）
2. 排除包含 `@FeignClient` 注解的文件
3. 排除被注释的注解（`//@RestController` 等）
4. 对每个 Controller 文件，运行三级确定性分类链（详见 [templates/classification-guide.md](../templates/classification-guide.md)）：
   - Level 1：精确类名映射表查找
   - Level 2：关键词模式匹配
   - Level 3：默认归 custom + 业务分组提取（**严格按 extractBusinessGroup 公式，禁止语义归并**）
5. 使用包路径转换公式计算目标 package 路径
6. 对比文件当前实际位置与计算出的目标位置

**判定标准**：

| 情况 | 级别 |
|------|------|
| 文件当前 package 与计算出的目标 package 一致 | **PASS** |
| 文件当前 package 与计算出的目标 package 不一致 | **FAIL** |

**注意**：S4-02 不使用 WARN 级别。分类结果由确定性链唯一确定，文件要么在正确位置，要么不在。没有“部分正确”的情况。

### 检查报告输出格式

```
| Controller 类 | 当前位置 | 分类级别 | 命中规则 | 目标位置 | 状态 |
```

- **分类级别**：L1 / L2 / L3，表示该文件被哪一级分类链命中
- **命中规则**：具体命中的映射条目（如 "L2: className 包含 Sso → sso"）

---

## S4-03：二级业务分组容量

**检查目标**：`custom/` 和 `common/` 下每个二级子目录的文件数量是否合理。

**检查方法**：
- 统计 `custom/` 和 `common/` 下每个子目录中包含 `@Controller` 或 `@RestController` 注解的 Java 文件数
- 检查是否有文件直接位于 `custom/` 或 `common/` 根目录（未进入二级子目录）

**判定标准**：
- 单个二级子目录下 Controller 文件数量严格 > 10 → **WARN**
- `custom/` 或 `common/` 根目录下直接存在 Controller 文件 → **WARN**
- 文件数量 ≤ 10 且无根目录残留 → **PASS**

**处理方式**：WARN 仅在检查报告中标注。本工具**不执行**自动拆分操作。

---

## S4-04：非 controller 包下的 Controller

**检查目标**：是否有 Controller 类放在 `controller` 包以外的位置。

**检查方法**：
1. 使用 Grep 在 `src/main/java/` **整个目录树**中搜索包含 `@Controller` 或 `@RestController` 注解（非注释行，即不以 `//` 开头）的 Java 文件
2. 排除以下合法的非 controller 包文件：
   - 包含 `@FeignClient` 注解的接口（Feign 客户端）
   - package 路径中包含 `rpc` 或 `feignclient` 的文件
3. 检查剩余文件的 package 声明是否包含 `controller` 段

**特别注意**：S4-04 扫描必须覆盖所有子目录，包括 `config/`、`config2/`、`view/` 等非 controller 路径，**不得仅扫描已有 controller 包的目录**。

**扫描策略要求**：执行时必须采用逐区扫描策略（与修复流程 [refactor-rules.md](refactor-rules.md) 步骤 1.1.1~1.1.4 一致），分别扫描 `config/`、`config2/`、`controller/` 及其他目录，不得仅执行单次全局 Grep 后直接输出结果。每个分区的扫描结果必须独立输出后汇总。

**判定标准**：
- Controller 类的 package 中不包含 `controller` 段 → **FAIL**
- Controller 类在合法排除列表中 → 排除，不检查
- Controller 类的 package 包含 `controller` 段 → **PASS**

---

## S4-05：controller 包下的非 Controller 类

**检查目标**：是否有非 Controller 类（无 `@Controller` / `@RestController` 注解）位于 `controller` 包下。

**检查方法**：
1. 使用 Glob 扫描 `controller/` 包下所有 Java 文件
2. 使用 Grep 检查每个文件是否包含 `@Controller` 或 `@RestController` 注解（**排除注释行**，即以 `//` 开头的行不算有效注解）
3. 不含有效注解的文件标记为非 Controller 类

**判定标准**：
- controller 包下存在非 Controller 类 → **INFO**（仅提示，不要求迁移）
- controller 包下所有文件都是 Controller → **PASS**

**处理方式**：INFO 级别，仅在检查报告中展示。非 Controller 类**不迁移**，保留原位。
