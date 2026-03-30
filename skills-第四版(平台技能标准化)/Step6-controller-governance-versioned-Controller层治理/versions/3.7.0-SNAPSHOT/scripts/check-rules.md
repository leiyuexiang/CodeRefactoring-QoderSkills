# S4 检查规则清单

## 检查级别定义

| 级别 | 含义 | 处理方式 |
|------|------|---------|
| **FAIL** | 不符合规范，必须修复 | 修复阶段自动处理 |
| **WARN** | 建议优化，不强制修复 | 仅在报告中标注，不自动处理 |
| **INFO** | 信息提示 | 仅在报告中展示 |

---

## 检查前置约束（CHECK-PREREQUISITE）

> **检查流程必须使用与修复流程相同的精确正则模式和门控检查算法，确保检查结果与修复结果100%一致。**

| 编号 | 约束 | 说明 |
|------|------|------|
| CP-01 | **使用 GATE-REGEX 精确正则** | 检查时必须使用 [classification-guide.md 的 GATE-REGEX 表](../templates/classification-guide.md) 中定义的精确正则模式，不得自行构造简化正则 |
| CP-02 | **使用 GATE-ALGORITHM 伪代码** | 门控检查必须按 [classification-guide.md 的 GATE-ALGORITHM](../templates/classification-guide.md) 逐步执行，不得跳步 |
| CP-03 | **使用 CLASSIFICATION-PIPELINE 伪代码** | 分类必须按 [classification-guide.md 的 CLASSIFICATION-PIPELINE](../templates/classification-guide.md) 伪代码逐步执行，不得凭语义推断 |
| CP-04 | **使用 SCAN-TOOL-PATTERNS 工具模式** | 扫描时必须使用 [refactor-rules.md 的 SCAN-TOOL-PATTERNS](refactor-rules.md) 中定义的精确工具调用模式 |
| CP-05 | **使用 SCAN-PER-FILE 流程** | 每个文件的门控检查必须按 [refactor-rules.md 的 SCAN-PER-FILE](refactor-rules.md) 标准流程执行 |

---

## S4-01：custom/common 一级目录存在性

**检查目标**：`{modulePrefix}/controller/` 下是否存在 `custom/` 和 `common/` 两个一级子目录。

**精确检查步骤**：
```
Step 1: 确定 modulePrefix（从已有 Controller 文件的 package 中提取，或从工程结构推断）
Step 2: Glob(pattern="*/", path="{modulePrefix对应的controller目录}")
Step 3: 检查 Glob 结果中是否包含 "custom/" 目录
Step 4: 检查 Glob 结果中是否包含 "common/" 目录
```

**判定标准**：
- `controller/` 下不存在 `custom/` 子目录 → **FAIL**
- `controller/` 下不存在 `common/` 子目录 → **FAIL**
- 两个目录都存在 → **PASS**

---

## S4-02：Controller 归属正确性

**检查目标**：每个 Controller 文件是否位于三级确定性分类链计算出的正确位置。

**精确检查步骤**：
```
Step 1: 采用四轮逐区扫描策略（详见 refactor-rules.md 步骤 1.1.1~1.1.4）
        → 每轮使用 SCAN-TOOL-PATTERNS 中的精确 Grep 模式
        → 每个文件按 SCAN-PER-FILE 流程执行门控检查
        → 第 4 轮交叉验证确保 N = Nfull

Step 2: 对每个通过门控的 Controller 文件：
        a. 提取 className = 文件名去掉 ".java" 后缀
        b. 提取 originalPackage = 文件中 "package xxx.yyy.zzz;" 声明的包路径
        c. 执行 CLASSIFICATION-PIPELINE 伪代码中的 classifyController(filePath, className, originalPackage)
           → 得到 { level, rule, category, subdirectory }
        d. 执行 computeTargetPackage(originalPackage, category, subdirectory)
           → 得到 targetPackage
        e. 对比 originalPackage 与 targetPackage

Step 3: 判定结果
        → originalPackage == targetPackage → PASS
        → originalPackage != targetPackage → FAIL
```

**判定标准**：

| 情况 | 级别 |
|------|------|
| 文件当前 package 与计算出的目标 package 一致 | **PASS** |
| 文件当前 package 与计算出的目标 package 不一致 | **FAIL** |

**注意**：S4-02 不使用 WARN 级别。分类结果由确定性链唯一确定，文件要么在正确位置，要么不在。没有"部分正确"的情况。

### 检查报告输出格式

```
| Controller 类 | 当前位置 | 分类级别 | 命中规则 | 目标位置 | 状态 |
```

- **分类级别**：L1 / L2 / L3，表示该文件被哪一级分类链命中
- **命中规则**：具体命中的映射条目（如 "L2-P1: className含Sso→sso"），格式必须与 CLASSIFICATION-PIPELINE 伪代码的返回值一致

---

## S4-03：二级业务分组容量

**检查目标**：`custom/` 和 `common/` 下每个二级子目录的文件数量是否合理。

**精确检查步骤**：
```
Step 1: Glob(pattern="**/*.java", path="{controllerDir}/custom/")
Step 2: Glob(pattern="**/*.java", path="{controllerDir}/common/")
Step 3: 按二级子目录分组统计文件数量
Step 4: 对每个子目录：
        → 使用 GATE-REGEX-01/02 验证每个文件是否为有效 Controller
        → 统计有效 Controller 数量
Step 5: 检查是否有文件直接在 custom/ 或 common/ 根目录（未进入二级子目录）
```

**判定标准**：
- 单个二级子目录下 Controller 文件数量严格 > 10 → **WARN**
- `custom/` 或 `common/` 根目录下直接存在 Controller 文件 → **WARN**
- 文件数量 ≤ 10 且无根目录残留 → **PASS**

**处理方式**：WARN 仅在检查报告中标注。本工具**不执行**自动拆分操作。

---

## S4-04：非 controller 包下的 Controller

**检查目标**：是否有 Controller 类放在 `controller` 包以外的位置。

**精确检查步骤**：
```
Step 1: 使用逐区扫描策略（与修复流程一致），分别扫描：
        → config/ 下每个子目录（使用 SCAN-PER-FILE 流程）
        → config2/ 下每个子目录（使用 SCAN-PER-FILE 流程）
        → controller/ 及其他目录（使用 SCAN-PER-FILE 流程）
        → 全量交叉验证

Step 2: 对每个通过门控的 Controller 文件：
        a. Grep(pattern="^package\s+", path="{filePath}") → 提取 package 路径
        b. 检查 package 路径中是否包含 ".controller." 或以 ".controller" 结尾

Step 3: 排除合法的非 controller 包文件：
        → 使用 GATE-REGEX-05 排除 @FeignClient
        → 使用 GATE-REGEX-09 排除 rpc/feignclient 包路径
```

**特别注意**：S4-04 扫描必须覆盖所有子目录，包括 `config/`、`config2/`、`view/` 等非 controller 路径，**不得仅扫描已有 controller 包的目录**。

**扫描策略要求**：执行时必须采用逐区扫描策略（与修复流程 [refactor-rules.md](refactor-rules.md) 步骤 1.1.1~1.1.4 一致），分别扫描 `config/`、`config2/`、`controller/` 及其他目录，不得仅执行单次全局 Grep 后直接输出结果。每个分区的扫描结果必须独立输出后汇总。

**判定标准**：
- Controller 类的 package 中不包含 `controller` 段 → **FAIL**
- Controller 类在合法排除列表中 → 排除，不检查
- Controller 类的 package 包含 `controller` 段 → **PASS**

---

## S4-05：controller 包下的非 Controller 类

**检查目标**：是否有非 Controller 类（无 `@Controller` / `@RestController` 注解）位于 `controller` 包下。

**精确检查步骤**：
```
Step 1: Glob(pattern="**/*.java", path="{controllerDir}")
Step 2: 对每个文件执行 SCAN-PER-FILE 流程中的门控检查
Step 3: 门控结果为 "EXCLUDE" 的文件标记为非 Controller 类
        → 必须记录具体排除原因（GC-EXCL-01~06 中的哪一条）
```

**判定标准**：
- controller 包下存在非 Controller 类 → **INFO**（仅提示，不要求迁移）
- controller 包下所有文件都是 Controller → **PASS**

**处理方式**：INFO 级别，仅在检查报告中展示。非 Controller 类**不迁移**，保留原位。

---

## 检查结果一致性保证

> **检查流程与修复流程使用完全相同的算法和工具模式，确保检查发现的问题与修复会处理的问题100%一致。**

| 共用组件 | 检查中的用途 | 修复中的用途 |
|---------|-------------|-------------|
| GATE-REGEX 精确正则 | 识别有效 Controller | 确定迁移候选 |
| GATE-ALGORITHM 门控伪代码 | 过滤非 Controller | 过滤非 Controller |
| CLASSIFICATION-PIPELINE 分类伪代码 | 计算目标位置（用于对比） | 计算目标位置（用于迁移） |
| SCAN-TOOL-PATTERNS 工具模式 | 扫描文件 | 扫描文件 |
| SCAN-PER-FILE 单文件流程 | 逐文件门控 | 逐文件门控 |
| 四轮逐区扫描 + 交叉验证 | 确保扫描完整 | 确保扫描完整 |
