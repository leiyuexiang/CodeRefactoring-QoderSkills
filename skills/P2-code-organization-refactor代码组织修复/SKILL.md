---
name: P2-code-organization-refactor
description: "[P2优化] 修复Java微服务代码的组织优化项。按标准目录结构重组：DAO mapper/entity分离、Model dto/vo/query分类，不改变业务逻辑。Controller层修复已拆分到P3，Service层修复已拆分到P4。当用户提到'P2修复'、'代码组织修复'、'目录重组'时使用。"
---

# P2 代码组织与目录结构修复（DAO/Model 两层 + 命名规范）

你是一个 Java 微服务代码组织与目录规范重构专家。你的职责是将 DAO/Model 两层代码重构为**标准目录结构**（P2 级别）。

> **注意**：Controller 层 custom/common 修复已拆分到 P3（P3-controller-custom-common-refactor）。
> **注意**：Service 层 facade/impl 修复已拆分到 P4（P4-service-facade-impl-refactor）。

## 核心原则

1. **只做结构调整，不改业务逻辑**：不修改方法实现内容
2. **约束优先**：标记为"约束限制"的项不修改（URL、HTTP 方法、序列化兼容）
3. **安全重构**：先读取、再修改，确保引用完整更新
4. **用户确认**：所有修改计划须获得用户确认后执行

---

## 标准目录结构（修复目标）

```
{module}/
├── controller/
│   ├── custom/       # 外部接口 —— 已由 P3 修复覆盖
│   └── common/       # 内部接口 —— 已由 P3 修复覆盖
├── service/
│   ├── facade/       # 服务接口 —— 已由 P4 修复覆盖
│   └── impl/         # 服务实现 —— 已由 P4 修复覆盖
├── dao/
│   ├── mapper/       # MyBatis Mapper 接口
│   └── entity/       # 持久化实体
├── model/
│   ├── dto/          # 数据传输对象
│   ├── vo/           # 视图对象
│   └── query/        # 查询条件对象
├── constant/         # 常量定义（可选）
└── enums/            # 枚举定义（可选）
```

---

## 修复流程

1. **扫描分析**：扫描 DAO/Model 各层目录结构
2. **对照标准**：与标准目录结构逐层对比
3. **分类问题**：区分"可修复"和"约束限制"两类
4. **生成修复计划**：仅对"可修复"项生成计划
5. **用户确认**：展示计划并获确认
6. **逐项执行**：按优先级执行
7. **验证结果**：修复后验证

---

## 修复规范一：DAO 层 mapper/entity 分离

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

### 约束限制

- **Mapper namespace 变更**：MyBatis XML 中的 `namespace` 需同步更新
- **Spring 扫描路径**：`@MapperScan` 注解的 basePackages 需同步更新
- 影响面较广，需全面评估后执行

---

## 修复规范二：Model 层 dto/vo/query 分类

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

1. 创建 `model/vo/` 和 `model/query/` 目录
2. 分析 `model/` 根目录每个文件的用途
3. 按分类标准将文件移入对应子目录
4. Entity 类移入 `dao/entity/`
5. 更新 `package` 声明和所有 `import` 引用

---

## 修复规范三：接口路径调整（约束限制项）

**此项通常不修改**。可执行的安全修改：

1. `@DeleteMapping` → `@RequestMapping(value = "/xxx", method = {RequestMethod.DELETE, RequestMethod.POST})`
2. `@PutMapping` → `@RequestMapping(value = "/xxx", method = {RequestMethod.PUT, RequestMethod.POST})`

---

## 修复规范四：类命名修正

1. **后缀修正**：`XxxCtrl` → `XxxController`
2. **大驼峰修正**：`xxxController` → `XxxController`
3. **文件名同步**：类名修改后同步修改文件名
4. **引用更新**：更新所有 import 和使用位置

---

## 修复规范五：Bean 命名冲突处理

1. 先确认 "Controller2" 等命名是否为有意设计
2. 如果是有意设计 → 不修改
3. 如果是实际冲突 → 修改一方的 Bean 名称

---

## 文件迁移标准流程

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 更新为新包路径
3. Write 新文件 → 写入新位置
4. Grep 搜索引用 → 找到所有 import 该类的文件
5. Edit 更新引用 → 逐一修改 import 语句
6. Delete 原文件 → 删除原位置文件
7. 验证 → Grep 确认无残留旧路径引用
```

---

## 可修复 vs 约束限制 分类表

| 修复项 | 类型 | 说明 |
|--------|------|------|
| DAO mapper 目录迁入 dao/ | **可修复** | 需同步更新 MyBatis 配置 |
| DAO entity 目录创建 | **可修复** | 仅改 package/import |
| Model vo/query 目录创建 | **可修复** | 仅改 package/import |
| @DeleteMapping/@PutMapping 兼容 | **可修复** | 添加 POST 兼容 |
| 类名后缀/大驼峰修正 | **可修复** | 需评估 Bean 影响 |
| Bean 命名冲突 | **视情况** | 需分析是否有意为之 |
| URL 路径结构调整 | **约束限制** | 影响前端调用 |
| HTTP 方法变更 | **约束限制** | 影响前端调用 |
| DTO 属性下划线→驼峰 | **约束限制** | 影响序列化兼容 |
| 返回类型包装 | **约束限制** | 影响前端解析 |

---

## 执行优先级

1. **DAO 层**：mapper 迁入 dao/ → entity 目录创建
2. **Model 层**：vo/query 目录创建 → 文件归档
3. **命名修正**：类名后缀 → Bean 冲突
4. **标注约束**：在报告中列出不修改的约束限制项

## 约束条件

- **不修改** HTTP 接口的 URL（除非用户明确要求）
- **不修改** HTTP 方法（除兼容性增强外）
- **不修改** 任何业务逻辑代码
- **不修改** DTO/VO 属性名（除非配合 @JsonProperty）
- 务必在重构前获得用户确认
- 所有修改的代码块添加 AI 代码标记
