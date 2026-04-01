# S3 Service 层治理检查与修复 - 3.6.0-SNAPSHOT 版本规则

## 版本说明

此为 **3.6.0-SNAPSHOT 基线版本**，包含完整的 S3 Service 层治理检查与修复规则。

---

## 概述

本文件为 **3.6.0-SNAPSHOT** 版本的 S3 Service 层治理检查与修复规则（基线版本）。

包含两大核心功能：

1. **Service 层检查**：扫描 Service 层目录结构，检查接口/实现是否正确分离到 `facade/` 和 `impl/`，非 Service 文件是否合理放置，输出结构化检查报告。
2. **Service 层修复**：修复检查发现的 Service 层结构问题，将接口迁入 `facade/`、实现迁入 `impl/`，不改变业务逻辑。

**检查项来源**：合并自原 P1（目录结构）的 Service 接口/实现分离项 + 原 P4（Service 接口实现分离）全部检查项。

## 使用场景

| 场景 | 触发关键词 | 调用功能 |
|------|-----------|----------|
| 检查 Service 层是否符合规范 | "S3检查"、"Service层治理检查"、"Service层检查" | 功能一：Service 层检查 |
| 修复不符合规范的 Service 层 | "S3修复"、"Service层治理修复"、"Service层修复" | 功能二：Service 层修复 |
| 先检查再修复 | "S3检查并修复"、"Service层治理全流程" | 功能一 + 功能二 |

## 前置条件

- 工程为 Java 微服务项目（Spring Boot/Cloud）
- 采用 Controller → Service → DAO → Model 分层架构
- S2（DAO-Model 层治理）已完成
- 修复前用户需确认修复计划

## 编码防护规范（强制前置）

在执行任何检查或修复操作之前，必须读取并遵守全局编码防护规范：
→ [shared/encoding-guard.md](../../../shared/encoding-guard.md)

该规范定义了 Windows 环境下防止中文编码被 PowerShell 破坏的事前防护措施。核心要求：
- 文件搜索使用 Grep/Glob 工具，禁止 Bash `grep`/`find`
- 文件读取使用 Read 工具，禁止 Bash `cat`/`type`/`Get-Content`
- 文件修改使用 Edit 工具，禁止 Bash `sed`/`awk`/PowerShell 替换
- 仅 A 类操作（copy/mv/mkdir/rmdir）允许通过 Bash 执行

## 标准目录结构

```
service/
├── facade/                   # 服务接口定义（所有 I*Service 接口）
│   ├── IXxxService.java
│   ├── IYyyService.java
│   └── ...
├── impl/                     # 服务实现（所有 *ServiceImpl 实现类）
│   ├── XxxServiceImpl.java
│   ├── YyyServiceImpl.java
│   └── ...
└── {business}/               # 非 Service 文件保留原业务子包
    ├── constant/
    ├── enums/
    ├── util/
    └── ...
```

## 强制执行规范（不可违反）

### 目标目录路径构造公式

- Service 接口目标路径 = `{原service包路径}/facade/{接口文件名}`
- Service 实现目标路径 = `{原service包路径}/impl/{实现文件名}`

**绝对规则**：
1. 所有 Service 接口必须且只能放入 `service/facade/` 目录，不允许保留在 service 根目录
2. 所有 Service 实现必须且只能放入 `service/impl/` 目录
3. 如果源工程存在 `serviceImp/`、`imp/`、`svc/` 等非标准命名的目录，必须统一重命名/迁移到 `impl/`
4. 禁止复用源工程已有的非标准目录名称
5. facade/ 和 impl/ 是唯一合法的 Service 子目录名称

## 检查项总览

| 编号 | 检查项 | 严重级别 | 说明 |
|------|--------|---------|------|
| S3-01 | Service 层接口/实现分离规范 | FAIL/WARN | 接口和实现是否分离到独立目录 |
| S3-02 | facade/ 目录存在性 | FAIL | service/ 下必须有 facade/ 且包含所有 Service 接口 |
| S3-03 | Service 接口归属正确性 | FAIL/WARN | 接口应在 facade/ 下，不应散落在根目录或业务子目录 |
| S3-04 | Service 实现归属正确性 | FAIL | 实现类应统一在 service/impl/ 下 |
| S3-05 | 非 Service 文件处理 | WARN | 非 Service 业务文件不应混入 facade/ 或 impl/ |

完整检查规则详情 → [scripts/check-rules.md](scripts/check-rules.md)

---

## 功能一：Service 层检查流程

### Step 1: 确定检查范围

用户提供目录路径或模块名称。

### Step 2: 扫描文件

扫描 Service 层目录结构：
- 使用 Glob 扫描 `service/` 目录下所有子目录和 Java 文件
- 使用 Grep 搜索 `interface` 关键字和 `@Service` 注解区分接口和实现
- 使用 Read 读取关键文件确认类型

### Step 3: 逐项检查

按 S3 检查清单（5 项）逐项排查。

完整检查规则 → [scripts/check-rules.md](scripts/check-rules.md)

### Step 4: 输出检查报告

按标准格式输出结构化报告。

检查报告示例 → [examples/check-report.md](examples/check-report.md)

---

## 功能二：Service 层修复流程

### 核心原则

1. **只做结构调整，不改业务逻辑**：不修改方法实现内容
2. **约束优先**：不修改跨模块共享接口的公共 API
3. **安全重构**：先读取、再修改，确保引用完整更新
4. **用户确认**：所有修改计划须获得用户确认后执行

### Phase 1: 扫描分析

1. 使用 Glob 扫描 `service/` 目录下所有 Java 文件
2. 使用 Grep 搜索 `interface` 和 `@Service` 注解区分接口和实现
3. 识别 Service 接口文件和实现文件
4. 识别非 Service 文件（常量、枚举、异常、工具、Feign 等）
5. 生成分类清单

### Phase 1.5: 冲突预检（强制，不可跳过）

在生成修复计划之前，执行以下冲突检测：

1. 检查所有待迁入 `facade/` 的接口文件名是否有重复（来自不同业务子包的同名 Service）
2. 检查所有待迁入 `impl/` 的实现文件名是否有重复
3. 检查目标目录中是否已存在同名文件
4. 检查是否存在通配符 import（`import xxx.service.{子包}.*`）

如发现冲突 → 停止，向用户报告冲突详情并等待决策。

### Phase 2: 生成修复计划

1. 列出所有需迁移的接口文件（→ facade/），标注源路径和目标路径
2. 列出所有需迁移的实现文件（→ impl/），标注源路径和目标路径
3. 标注保留不动的非 Service 文件
4. 列出需更新的通配符 import 及其处理方式
5. 列出跨模块引用影响范围
6. 统计影响范围：迁移文件数、更新 import 文件数、跨模块影响数

### Phase 3: 用户确认

展示修复计划，**必须获得确认后才开始执行修复操作**。
确认内容必须包含：文件迁移清单、通配符 import 处理方式、跨模块影响范围。

### Phase 4: 逐项执行修复

按 6 大修复规范和优先级执行：

1. 创建 facade/ 和 impl/ 目录
2. Service 接口迁入 facade/
3. Service 实现统一迁入 impl/
4. 更新 package 声明和 import 引用
5. 处理边界情况（抽象基类、无接口实现等）
6. 清理空目录

完整修复规则 → [scripts/refactor-rules.md](scripts/refactor-rules.md)
标准目录结构模板 → [templates/standard-directory.md](templates/standard-directory.md)
分类判断指南 → [templates/classification-guide.md](templates/classification-guide.md)
文件迁移标准流程 → [examples/migration-flow.md](examples/migration-flow.md)

### Phase 5: 验证结果

修复完成后执行以下强制验证，任何一项不通过必须立即停止并报告：

#### 1. 目录结构验证
- Glob 扫描确认所有 service/facade/ 下只有 interface 文件（即 Service 接口已全部归入 facade/）
- Glob 扫描确认所有 service/impl/ 下只有 class 文件（即所有实现已统一在 impl/ 下）
- 确认不存在 Service 接口文件残留在 service/ 根目录或其他非 facade/ 子目录

#### 2. 引用完整性验证
- Grep 搜索所有旧 package 路径，确认零结果
- Grep 搜索所有旧 import 路径，确认零结果
- Grep 搜索通配符 import（`import.*service\.{旧子包}\.\*`），确认已正确处理
- Grep 搜索全工程中旧全限定类名（覆盖 `.java`、`.xml`、`.yml`、`.yaml`、`.properties`），确认零残留
- 确认修复后代码可编译

#### 3. 内容完整性验证
- 每个迁移文件：验证除 package/import 行外的内容与源文件完全一致
- 检查是否有中文字符被截断（搜索单独的 "?" 字符出现在中文语境中）

#### 4. 文件数量验证
- 迁移前后的 Service 接口文件总数必须相等
- 迁移前后的 Service 实现文件总数必须相等
- 非 Service 文件不应有增减

### 安全约束

完整安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

关键约束：
- **不修改** 任何业务逻辑代码
- **不修改** 非 service 包下的代码结构
- **不移动** 非 Service 文件（常量、枚举、工具等）
- 务必在重构前获得用户确认

---

## 文件索引

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/check-report.md](examples/check-report.md) | Service 层检查报告输出示例 |
| [examples/migration-flow.md](examples/migration-flow.md) | Service 文件迁移标准流程与操作示例 |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/standard-directory.md](templates/standard-directory.md) | facade/impl 标准目录结构模板 |
| [templates/classification-guide.md](templates/classification-guide.md) | Service 接口/实现/非Service文件分类判断指南 |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | S3 检查规则清单（5 项详细检查方法与判定标准） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | S3 修复规范（迁移策略与执行步骤） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 修复安全约束与核心原则 |
