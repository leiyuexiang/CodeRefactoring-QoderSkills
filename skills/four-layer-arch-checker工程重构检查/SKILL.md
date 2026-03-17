---
name: four-layer-arch-checker
description: 四层架构Maven工程目录规范检查。扫描项目结构，验证底座层、能力层、聚合层、体验层的目录命名、POM层级、模块依赖关系是否符合规范。当用户提到"架构检查"、"目录规范"、"结构校验"、"四层架构审查"时使用。
---

# 四层架构工程目录规范检查

## 概述

对 Maven 工程执行四层架构规范检查，验证目录结构、命名规范、POM 配置、依赖关系是否符合标准。输出检查报告，列出所有违规项及修复建议。

## 架构定义

| 层级 | 目录前缀 | 职责 | 必需性 |
|------|----------|------|--------|
| 底座层 (Foundation) | `grp-common-boot/` | 通用基础设施（日志、异常、工具、数据库） | 必需 |
| 能力层 (Capability) | `grp-capability-{module}/` | 原子业务能力（Controller/Service/DAO） | 必需 |
| 聚合层 (Aggregation) | `grp-aggregation-{module}/` | 服务启动编排（多框架适配） | 必需 |
| 体验层 (Experience) | `grp-experience-{module}/` | Feign API SDK 封装 | 可选 |

## 检查流程

### Step 1: 扫描工程结构

首先使用 Glob/Bash 工具扫描项目的完整目录结构：
- 列出所有 `pom.xml` 文件
- 列出所有一级和二级目录
- 识别出所有 `*-module/` 业务模块

### Step 2: 逐项执行规范检查

按以下检查清单逐项验证，记录每条的通过/违规状态。

### Step 3: 输出检查报告

---

## 检查规则清单

### 一、根目录结构检查

| 编号 | 检查项 | 规则 |
|------|--------|------|
| R-01 | 根 POM 存在 | 项目根目录必须存在 `pom.xml` |
| R-02 | 底座层目录存在 | 必须存在 `grp-common-boot/` 目录 |
| R-03 | 业务模块命名 | 业务模块目录必须使用 `{module}-module/` 格式命名 |
| R-04 | 根 POM modules 声明 | 根 `pom.xml` 的 `<modules>` 必须包含所有子模块目录 |

### 二、底座层检查 (grp-common-boot)

| 编号 | 检查项 | 规则 |
|------|--------|------|
| F-01 | 底座层 POM 类型 | `grp-common-boot/pom.xml` 的 packaging 必须为 `pom` |
| F-02 | 通用模块命名 | 底座层子模块必须使用 `grp-*-com/` 命名模式（如 `grp-logger-com`, `grp-exception-com`, `grp-util-com`, `grp-database-com`） |
| F-03 | 底座层 modules 声明 | `grp-common-boot/pom.xml` 的 `<modules>` 必须包含其所有子模块 |

### 三、业务模块结构检查

对每个 `{module}-module/` 执行以下检查：

| 编号 | 检查项 | 规则 |
|------|--------|------|
| M-01 | 能力层目录存在 | 必须存在 `grp-capability-{module}/` 目录 |
| M-02 | 聚合层目录存在 | 必须存在 `grp-aggregation-{module}/` 目录 |
| M-03 | 体验层目录命名 | 若存在体验层，必须命名为 `grp-experience-{module}/` |
| M-04 | 模块 POM 存在 | `{module}-module/pom.xml` 必须存在，packaging 为 `pom` |
| M-05 | 模块 POM modules 声明 | 模块 POM 的 `<modules>` 必须包含其能力层和聚合层目录 |

### 四、能力层检查 (grp-capability-{module})

| 编号 | 检查项 | 规则 |
|------|--------|------|
| C-01 | 能力层 POM 类型 | `grp-capability-{module}/pom.xml` 的 packaging 必须为 `pom` |
| C-02 | API 定义模块命名 | 若存在 API 定义模块，必须命名为 `grp-{module}-api/` |
| C-03 | 接口层模块命名 | Controller 层模块必须命名为 `{module}-server/` |
| C-04 | 实现层模块命名 | 业务实现模块必须命名为 `{module}-server-com/` |
| C-05 | 能力层 modules 声明 | 能力层 POM 的 `<modules>` 必须包含其所有子模块 |
| C-06 | parent 配置 | 能力层子模块的 `<parent>` 必须指向 `grp-capability-{module}` |

### 五、聚合层检查 (grp-aggregation-{module})

| 编号 | 检查项 | 规则 |
|------|--------|------|
| A-01 | 聚合层 POM 类型 | `grp-aggregation-{module}/pom.xml` 的 packaging 必须为 `pom` |
| A-02 | SpringCloud 适配模块 | 必须存在 `{prefix}-springcloud/` 模块 |
| A-03 | 华为适配模块 | 必须存在 `{prefix}-huawei/` 模块 |
| A-04 | 腾讯适配模块 | 必须存在 `{prefix}-tencent/` 模块 |
| A-05 | Pivotal 适配模块 | 建议存在 `{prefix}-pivotal/` 模块（非强制） |
| A-06 | 适配模块命名一致性 | 所有适配模块前缀必须一致（如统一使用 `{module}-server-` 或 `{module}-serve-`） |
| A-07 | parent 配置 | 聚合层子模块的 `<parent>` 必须指向 `grp-aggregation-{module}` |

### 六、体验层检查 (grp-experience-{module}) — 可选

| 编号 | 检查项 | 规则 |
|------|--------|------|
| E-01 | 体验层 POM 类型 | `grp-experience-{module}/pom.xml` 的 packaging 必须为 `pom` |
| E-02 | Feign SDK 模块命名 | Feign SDK 模块应命名为 `{module}-feign-com/` |
| E-03 | parent 配置 | 体验层子模块的 `<parent>` 必须指向 `grp-experience-{module}` |

### 七、依赖关系检查

| 编号 | 检查项 | 规则 |
|------|--------|------|
| D-01 | 聚合层依赖能力层 | 聚合层模块的 POM 必须依赖对应的 `{module}-server` 或能力层模块 |
| D-02 | 能力层依赖底座层 | 能力层模块的 POM 应依赖 `grp-common-boot` 下的通用模块 |
| D-03 | 体验层依赖 API 模块 | 体验层的 Feign 模块应依赖 `grp-{module}-api` |
| D-04 | 禁止反向依赖 | 底座层不得依赖能力层；能力层不得依赖聚合层；下层不得依赖上层 |
| D-05 | 禁止跨模块直接依赖 | 业务模块间不得直接依赖实现层，只能通过 API/Feign SDK 模块依赖 |

### 八、POM 配置检查

| 编号 | 检查项 | 规则 |
|------|--------|------|
| P-01 | 版本统一管理 | 所有子模块版本号必须通过父 POM 的 `<version>` 或 `dependencyManagement` 管理，子模块不得自行声明版本号 |
| P-02 | packaging 类型 | 容器 POM（父工程、层级 POM）packaging 必须为 `pom`；叶子模块默认为 `jar` |
| P-03 | relativePath 正确 | 所有 `<parent>` 中的 `<relativePath>` 必须指向正确的父 POM 路径 |
| P-04 | groupId 一致性 | 同一工程内的 groupId 应保持一致 |

### 九、命名规范检查

| 编号 | 检查项 | 规则 |
|------|--------|------|
| N-01 | `grp-` 前缀 | 组织级容器目录、API 定义模块、底座层模块必须使用 `grp-` 前缀 |
| N-02 | `-com` 后缀 | 实现类模块使用 `-com` 后缀（如 `{module}-server-com`, `grp-util-com`） |
| N-03 | `-server` 后缀 | 接口/Controller 层模块使用 `-server` 后缀 |
| N-04 | 框架后缀 | 聚合层适配模块必须使用标准框架后缀：`-springcloud`, `-huawei`, `-pivotal`, `-tencent` |
| N-05 | 目录名全小写 | 所有模块目录名必须全小写，单词间用 `-` 连接 |

---

## 报告输出格式

检查完成后，按以下格式输出报告：

```
# 四层架构规范检查报告

## 检查概要
- 工程路径：{path}
- 检查时间：{time}
- 业务模块数：{count}
- 检查项总数：{total}
- 通过：{pass} | 违规：{fail} | 警告：{warn}

## 检查结果

### [通过] 根目录结构检查
- [PASS] R-01 根 POM 存在
- [PASS] R-02 底座层目录存在
...

### [违规] 要素模块 (element-module)
- [PASS] M-01 能力层目录存在
- [FAIL] A-06 适配模块命名一致性
  - 问题：聚合层模块前缀不一致，存在 `element-server-` 和 `element-serve-` 混用
  - 建议：统一使用 `element-server-` 前缀
...

## 违规汇总
| 编号 | 模块 | 问题 | 严重级别 | 修复建议 |
|------|------|------|----------|----------|
| A-06 | element-module | 命名不一致 | 高 | 统一前缀 |
...
```

## 严重级别定义

| 级别 | 说明 | 示例 |
|------|------|------|
| **高** | 违反架构核心规则，必须修复 | 缺少必需层级、依赖方向错误、packaging 类型错误 |
| **中** | 违反命名规范，建议修复 | 命名不符合约定、modules 声明缺失 |
| **低** | 建议优化项 | 缺少可选层级（体验层）、缺少 pivotal 适配 |

## 执行说明

1. 此 Skill 被调用后，自动扫描当前工程目录
2. 如用户指定了特定模块，只检查该模块；否则检查全部模块
3. 检查过程中需读取所有相关 `pom.xml` 的内容
4. 对每条规则明确给出 PASS/FAIL/WARN 判定
5. 违规项必须附带具体位置和修复建议
6. 最终输出结构化的检查报告
