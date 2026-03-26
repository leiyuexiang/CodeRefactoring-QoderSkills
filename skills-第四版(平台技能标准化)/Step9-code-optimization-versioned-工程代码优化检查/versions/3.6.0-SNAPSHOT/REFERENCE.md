# 工程代码优化检查与修复 - 3.6.0-SNAPSHOT 版本规则

## 版本说明

此为 **3.6.0-SNAPSHOT 基线版本**，包含完整的工程代码优化检查与修复规则。

---

## 概述

本文件为 **3.6.0-SNAPSHOT** 版本的工程代码优化检查与修复规则。

针对 `element-module/grp-capability-element/element-server-com` 下 `@Service` 和 `@Repository` 类进行系统化代码质量优化，包含三大核心功能：

1. **SQL 注入修复**（优先级最高）：值拼接参数化、动态表名白名单校验、动态列名正则校验
2. **日志增强**：统一 `@Slf4j` 注解、方法入口/异常日志补全
3. **代码清理**：StringBuffer→StringBuilder、冗余变量消除、条件简化、空集合检查统一

## 目标目录

```
element-server-com/src/main/java/grp/pt/
├── dao/
│   ├── impl/                    # 通用 DAO
│   ├── basedata/impl/           # 基础数据 DAO (~33个)
│   ├── bookset/impl/            # 账簿集 DAO
│   ├── agencyManager/impl/      # 机构管理 DAO
│   └── synchronization/         # 同步 DAO
├── service/
│   ├── impl/                    # 通用 Service
│   ├── basedata/impl/           # 基础数据 Service (~37个)
│   ├── bookset/impl/            # 账簿集 Service
│   ├── agencyManager/impl/      # 机构管理 Service
│   ├── synchronization/impl/    # 同步 Service
│   └── cache/                   # 缓存 Service
└── util/                        # 工具类（仅涉及 BaseH2DAO 等含 SQL 的）
```

## 筛选规则

- **处理**: 带 `@Service` 或 `@Repository` 注解的 Java 类
- **跳过**: 文件总行数 > 1000 行的类（记录到报告的跳过清单中）
- **排除**: 接口文件、Mapper 接口、Controller、配置类、DTO/Model

## 不可变红线

完整安全约束 → [scripts/safety-constraints.md](scripts/safety-constraints.md)

关键红线：
1. **不修改**类名（包括 `@Service("xxx")` 和 `@Repository("xxx")` 中的别名）
2. **不修改**方法签名（方法名、参数列表、返回类型）
3. **不修改**已有的日志语句（即使格式不统一也保持原样）
4. **不修改**业务逻辑的算法流程（只优化表达方式，不改变业务语义）

---

## 工作流程

### Step 1: 文件扫描与筛选

1. 扫描目标目录下所有 `*ServiceImpl.java`、`*DaoImpl.java`、`*DAO.java`（带 @Repository）
2. 统计每个文件的行数
3. 将 >1000 行的文件标记为 **SKIP**，记录原因
4. 输出完整的待处理文件清单（含文件路径和行数）
5. 记录待处理文件总数，作为完成进度的基准
6. 输出待处理文件清单给用户确认后再继续

已知超大文件清单 → [templates/skip-files.md](templates/skip-files.md)

### Step 2: 单文件分析

对每个待处理文件执行：
1. 读取完整文件内容
2. 判断类型：Service 层 / DAO 层
3. 检查类级日志声明状态
4. **DAO 层额外检查**：识别 SQL 注入风险点
5. 识别可优化的冗余代码

### Step 3: 执行优化

按以下优先级执行，每修改一个代码块都添加 AI 标记：

| 优先级 | 优化项 | 适用层 | 详细规则 |
|--------|--------|--------|---------|
| 1 | SQL 注入修复 | DAO 层 | [scripts/sql-injection-rules.md](scripts/sql-injection-rules.md) |
| 2 | 日志增强 | Service + DAO | [scripts/logging-rules.md](scripts/logging-rules.md) |
| 3 | 代码逻辑优化 | Service + DAO | [scripts/code-optimization-rules.md](scripts/code-optimization-rules.md) |

### Step 4: 自检验证

每个文件修改完后自检：
- [ ] 类名未改变
- [ ] 所有方法签名（名称、入参、返回类型）未改变
- [ ] 已有的日志语句未被删除或修改
- [ ] 所有 SQL 注入风险点已修复
- [ ] AI 标记格式正确

### Step 5: 生成变更报告

所有文件处理完毕后，生成变更报告。

变更报告模板 → [templates/report-template.md](templates/report-template.md)
变更报告示例 → [examples/change-report.md](examples/change-report.md)

---

## 批量处理策略

1. **第一个文件处理完后**，请用户确认优化风格是否满意
2. 确认后，**必须自动继续处理所有剩余文件**，无需再次确认
3. 每处理完 5-10 个文件，输出进度报告（已处理/总数）
4. **所有待处理文件必须被处理完毕后才能生成最终报告**

### 处理顺序

1. 先 DAO 层，后 Service 层（SQL 注入修复集中在 DAO）
2. 按子包分批处理：
   - `dao/basedata/impl/` → `dao/impl/` → `dao/agencyManager/impl/` → `dao/bookset/impl/` → `dao/synchronization/`
   - `service/basedata/impl/` → `service/impl/` → `service/agencyManager/impl/` → `service/bookset/impl/` → `service/synchronization/impl/` → `service/cache/`

### 处理粒度

- **单次处理 1 个文件**，避免上下文过大
- 每处理完一个文件，立即记录变更到报告
- `util/BaseH2DAO.java` 虽非标准 DAO 但含 SQL 拼接，如行数 ≤1000 行也需处理

## 技术约束

1. **BaseDAO 来自依赖 jar**（`grp-database-com`），不在当前源码中，不可修改
2. `SqlUtil.inSqlCondition()` 来自依赖 jar，可继续使用
3. Lombok 依赖已在项目中声明，`@Slf4j` 可直接使用
4. 项目使用 `org.springframework.util.CollectionUtils`（优先使用）

---

## 文件索引

### 示例文件 (examples/)

| 文件 | 说明 |
|------|------|
| [examples/change-report.md](examples/change-report.md) | 变更报告输出示例 |
| [examples/workflow-demo.md](examples/workflow-demo.md) | 单文件优化工作流演示 |

### 模板文件 (templates/)

| 文件 | 说明 |
|------|------|
| [templates/report-template.md](templates/report-template.md) | 变更报告 Markdown 模板 |
| [templates/skip-files.md](templates/skip-files.md) | 已知超大文件清单（预期跳过） |

### 规则/脚本文件 (scripts/)

| 文件 | 说明 |
|------|------|
| [scripts/sql-injection-rules.md](scripts/sql-injection-rules.md) | SQL 注入修复指南（类型 A/B/C 三种修复策略） |
| [scripts/logging-rules.md](scripts/logging-rules.md) | 日志增强规则（类级声明 + 方法级策略） |
| [scripts/code-optimization-rules.md](scripts/code-optimization-rules.md) | 代码优化规则（5 项优化策略） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 不可变红线与安全约束 |
