---
name: Step2-module-rename-工程架构微调
description: "[Step2] 在 Step1（四层架构工程重构）执行完成后，对能力层模块进行二次命名微调。将 `{module}-server` 重命名为 `{module}-controller`，将 `{module}-server-com` 重命名为 `{module}-service`，并同步更新所有 POM 引用和 Java package/import。执行链路：Step1→Step2（当前）→Step3→Step4→Step5→Step6→Step7→Step8→Step9。当用户提到'架构微调'、'模块重命名'、'server改controller'、'server-com改service'时使用。"
---

# 工程架构微调（Post-Refactor Naming Adjustment）

## 概述

本技能是 **engine-reconstruction-versioned（四层架构工程重构）** 的后置补充技能。

四层架构工程重构完成后，能力层（Capability）下的模块命名使用的是标准模板名称：
- `{module}-server` — Controller 层
- `{module}-server-com` — Service 业务实现层

本技能将这两个模块重命名为更语义化的名称：
- `{module}-server` **→** `{module}-controller`
- `{module}-server-com` **→** `{module}-service`

其余模块（`grp-{module}-api`、`{module}-server-springcloud`、`{module}-feign-com`、`grp-common-{module}` 等）保持不变。

## 前置条件

1. **必须先执行 `engine-reconstruction-versioned` 技能**，确保工程已经是标准四层架构结构
2. 工程为 Maven 多模块项目
3. 能力层容器 `grp-capability-{module}/` 下存在 `{module}-server` 和/或 `{module}-server-com` 模块
4. 用户已自行备份工程（或确认已备份）

## 作用范围

本技能**只处理工程架构级别**的变更，具体范围：

| 变更类型 | 说明 |
|----------|------|
| 目录重命名 | `{module}-server/` → `{module}-controller/`，`{module}-server-com/` → `{module}-service/` |
| POM artifactId | 被重命名模块自身的 `<artifactId>` |
| POM modules 声明 | 能力层容器 `grp-capability-{module}/pom.xml` 中的 `<module>` 列表 |
| POM dependencies | 全工程范围内所有引用被重命名模块的 `<dependency>` |
| POM dependencyManagement | 根 POM 中 `<dependencyManagement>` 里的 artifactId |
| Java package 声明 | 因模块目录变更导致的 `package` 语句更新 |
| Java import 语句 | 因依赖模块包路径变更导致的 `import` 语句更新 |

**不变更：** 业务逻辑代码、配置文件（yml/properties）中的业务配置、数据库配置等。

## 执行流程

### Step 0: 工程扫描与校验

1. 扫描当前工程根目录的 `pom.xml`，提取 `<version>` 和 `<groupId>`
2. 扫描所有 `*-module/` 业务模块目录
3. 对每个业务模块，检查 `grp-capability-{module}/` 下是否存在：
   - `{module}-server/` 目录 → 标记为待重命名为 `{module}-controller`
   - `{module}-server-com/` 目录 → 标记为待重命名为 `{module}-service`
4. 如果两个模块都不存在（可能已经重命名过），输出提示并跳过该模块
5. 生成微调计划，列出所有待执行的重命名操作

### Step 1: 用户确认

**必须等待用户确认后才执行重命名操作。**

输出微调计划供用户审核，格式参见 → [examples/adjustment-plan.md](examples/adjustment-plan.md)

### Step 2: 目录重命名

按以下顺序执行目录重命名：

```
# 1. 重命名 Service 层（先改被依赖方）
mv  grp-capability-{module}/{module}-server-com/  →  grp-capability-{module}/{module}-service/

# 2. 重命名 Controller 层
mv  grp-capability-{module}/{module}-server/  →  grp-capability-{module}/{module}-controller/
```

**重要**：先重命名 `{module}-server-com`（被依赖方），再重命名 `{module}-server`（依赖方），避免中间状态的引用错误。

### Step 3: 更新 POM 文件

按照重构规则更新所有相关 POM 文件。

完整 POM 更新规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)

更新顺序：

1. **被重命名模块自身的 POM**
   - `{module}-controller/pom.xml`：更新 `<artifactId>` 为 `{module}-controller`
   - `{module}-service/pom.xml`：更新 `<artifactId>` 为 `{module}-service`

2. **能力层容器 POM**
   - `grp-capability-{module}/pom.xml`：更新 `<modules>` 中的模块名

3. **根 POM**
   - 根 `pom.xml`：更新 `<dependencyManagement>` 中的 artifactId

4. **全局依赖引用**
   - 遍历所有 `pom.xml`，将 `<artifactId>{module}-server</artifactId>` 替换为 `<artifactId>{module}-controller</artifactId>`
   - 遍历所有 `pom.xml`，将 `<artifactId>{module}-server-com</artifactId>` 替换为 `<artifactId>{module}-service</artifactId>`

### Step 4: 更新 Java 文件

仅修改 `package` 声明和 `import` 语句，不触碰其他任何行。

完整 Java 更新规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)

替换映射表（以 `element` 模块为例）：

| 旧包路径片段 | 新包路径片段 |
|-------------|-------------|
| `{module}.server.` （非 springcloud 包下） | `{module}.controller.` |
| `{module}.server.com.` | `{module}.service.` |

**注意**：
- 替换时需精确匹配，避免误替换 `{module}-server-springcloud` 相关的包路径
- 先替换 `{module}.server.com.` → `{module}.service.`（长匹配优先）
- 再替换 `{module}.server.` → `{module}.controller.`（短匹配后执行）

### Step 5: 编译验证

执行 Maven 编译验证：

```bash
mvn compile -pl {module}-module -am 2>&1
```

如果编译失败，按以下策略处理：

| 错误类型 | 处理方式 |
|----------|---------|
| `package X does not exist` | 检查 import 替换是否遗漏，补充替换 |
| `cannot find symbol` | 检查 dependency 的 artifactId 是否更新完整 |
| `Could not find artifact` | 检查 dependencyManagement 中 artifactId 是否同步 |

### Step 6: 输出完成报告

输出微调完成报告，格式参见 → [examples/adjustment-report.md](examples/adjustment-report.md)

---

## 模块重命名映射总表

| 原模块名（engine-reconstruction 输出） | 微调后模块名 | 所属层级 |
|---------------------------------------|-------------|---------|
| `{module}-server` | `{module}-controller` | 能力层 `grp-capability-{module}/` |
| `{module}-server-com` | `{module}-service` | 能力层 `grp-capability-{module}/` |
| `grp-{module}-api` | **不变** | 能力层 `grp-capability-{module}/` |
| `{module}-server-springcloud` | **不变** | 聚合层 `grp-aggregation-{module}/` |
| `{module}-server-huawei` | **不变** | 聚合层 `grp-aggregation-{module}/` |
| `{module}-server-tencent` | **不变** | 聚合层 `grp-aggregation-{module}/` |
| `{module}-server-pivotal` | **不变** | 聚合层 `grp-aggregation-{module}/` |
| `{module}-feign-com` | **不变** | 体验层 `grp-experience-{module}/` |
| `grp-common-{module}` | **不变** | 公共层 |

## 微调后目标结构

完整目标结构 → [templates/target-structure.md](templates/target-structure.md)

```
{module}-module/
├── pom.xml
├── grp-common-{module}/                  # 公共层（不变）
├── grp-capability-{module}/              # 能力层容器（不变）
│   ├── pom.xml
│   ├── grp-{module}-api/                 # API 定义（不变）
│   ├── {module}-controller/              # ★ 原 {module}-server → 重命名
│   └── {module}-service/                 # ★ 原 {module}-server-com → 重命名
├── grp-aggregation-{module}/             # 聚合层容器（不变）
│   ├── pom.xml
│   └── {module}-server-springcloud/      # SC 适配（不变）
└── grp-experience-{module}/              # 体验层容器（不变）
    ├── pom.xml
    └── {module}-feign-com/               # Feign SDK（不变）
```

## 编码防护规范（强制前置）

在执行任何检查或修复操作之前，必须读取并遵守全局编码防护规范：
→ [shared/encoding-guard.md](../shared/encoding-guard.md)

该规范定义了 Windows 环境下防止中文编码被 PowerShell 破坏的事前防护措施。核心要求：
- 文件搜索使用 Grep/Glob 工具，禁止 Bash `grep`/`find`
- 文件读取使用 Read 工具，禁止 Bash `cat`/`type`/`Get-Content`
- 文件修改使用 Edit 工具，禁止 Bash `sed`/`awk`/PowerShell 替换
- 仅 A 类操作（copy/mv/mkdir/rmdir）允许通过 Bash 执行

## 安全约束

1. **只在 engine-reconstruction-versioned 执行完成后使用**，不可单独对非标准工程执行
2. **只做重命名操作**，不新增、不删除、不移动模块到其他层级
3. **不改变业务逻辑**，只调整 pom.xml 和 Java 的 package/import
4. **必须用户确认后执行**，不可静默执行
5. **先改被依赖方**（service），再改依赖方（controller），保证中间状态可控
6. **编码保留**：修改或迁移文件时必须保持原文件的字符编码格式（如 UTF-8、UTF-8 with BOM、GBK 等）。原文件为 UTF-8 with BOM（首3字节为 EF BB BF）时，新文件必须保留 BOM。优先使用 Edit 工具进行精确替换（自动保留编码），避免使用 Write 工具重写整个文件导致编码丢失或中文乱码。物理目录移动时使用 Bash 的 `mv` 命令（保留编码）

## 文件索引

| 文件 | 说明 |
|------|------|
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | POM 和 Java 文件的详细更新规则 |
| [templates/target-structure.md](templates/target-structure.md) | 微调后的目标目录结构定义 |
| [examples/adjustment-plan.md](examples/adjustment-plan.md) | 微调计划输出示例 |
| [examples/adjustment-report.md](examples/adjustment-report.md) | 微调完成报告输出示例 |
