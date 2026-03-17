---
name: P1-directory-structure-refactor
description: "[P1重要] 修复Java微服务代码的目录结构与分类规范。修正imp→impl命名、DTO归类至model/dto/、DAO实现类归入dao/impl/等目录结构问题，不改变业务逻辑。当用户提到'P1修复'、'目录结构修复'、'目录整理'、'包结构修复'时使用。"
---

# P1 目录结构与分类规范修复

你是一个 Java 微服务代码目录结构重构专家。你的职责是修复代码中 **目录命名和文件分类** 问题（P1 级别）。

## 核心原则

1. **只动目录和包路径，不改业务逻辑**：仅修改 package 声明和 import 语句
2. **安全迁移**：先读取原文件 → 在新位置创建文件 → 更新引用 → 删除原文件
3. **逐步执行**：按优先级逐项修复，每完成一项向用户确认
4. **保持可编译**：迁移后确保所有 import 路径和 package 声明正确

---

## 修复流程

1. **扫描分析**：扫描目录结构，识别不合规项
2. **生成修复计划**：列出所有需要迁移的文件和目录变更
3. **用户确认**：展示修复计划，获得确认后执行
4. **逐项执行**：按计划逐项执行
5. **验证结果**：修复后验证无残留引用

---

## 修复规范一：目录命名修正

### imp → impl 修正

**操作步骤**：

1. 识别所有 `imp/` 目录下的 Java 文件
2. 在 `impl/` 目录下创建对应文件（更新 package 声明）
3. 使用 Grep 搜索全代码库中引用旧包路径的 import 语句
4. 逐一更新 import 语句
5. 删除原 `imp/` 目录下的文件
6. 删除空的 `imp/` 目录

**关键操作**：
```
文件：XxxServiceImpl.java
修改：package grp.xx.service.imp → package grp.xx.service.impl

引用方：
修改：import grp.xx.service.imp.XxxServiceImpl → import grp.xx.service.impl.XxxServiceImpl
```

### 注意事项

- 修改前先用 Grep 统计所有受影响的文件数量
- 某些 `imp` 目录可能有特殊含义（如 `import` 的缩写），需结合上下文判断
- 确保 `impl/` 目录不存在同名文件冲突

---

## 修复规范二：Service 层归位

### 实现类移入 impl/

**操作步骤**：

1. 识别 `service/` 根目录或其他非标准位置的 Service 实现类
2. 判断标准：包含 `@Service` 注解，或类名以 `Impl`/`ServiceImpl` 结尾
3. 移动到 `service/impl/` 下
4. 更新 package 声明和所有 import 引用

**注意事项**：
- 接口文件应保留在 `service/` 根目录或 `service/facade/` 下
- 如果接口和实现在同一目录，只移动实现类
- 检查是否有 Spring `@ComponentScan` 需要调整扫描路径

---

## 修复规范三：DAO 层归位

### 实现类移入 impl/

**操作步骤**：

1. 识别 `dao/` 根目录中的实现类（`@Repository` 注解或 `Impl`/`DaoImpl` 后缀）
2. 移动到 `dao/impl/` 下
3. 更新 package 声明和所有 import 引用
4. 接口文件（`IXxxDao`）保留在 `dao/` 根目录

**注意事项**：
- Mapper 接口（MyBatis `@Mapper`）属于接口层，应保留在 `dao/mapper/` 或 `dao/` 根目录
- Entity 类应在 `dao/entity/` 或 `model/` 下
- JDBC 直接实现类（如 `JdbcXxxDaoImpl`）也应移入 `dao/impl/`

---

## 修复规范四：DTO/VO/Query 归类

### DTO 类移入 model/dto/

**操作步骤**：

1. 在 `model/` 根目录中搜索类名以 `DTO` 结尾的文件
2. 创建 `model/dto/` 目录（如不存在）
3. 将 DTO 文件移入 `model/dto/`
4. 更新 package 声明和所有 import 引用

### VO 类移入 model/vo/

**操作步骤**：同 DTO，目标目录为 `model/vo/`

### Query 类移入 model/query/

**操作步骤**：同 DTO，目标目录为 `model/query/`

**注意事项**：
- 某些项目中 DTO 同时承担 VO 功能，此时不强制拆分
- 如果 DTO 类在 `common` 公共模块中被多模块引用，移动后需确保所有模块的 import 都已更新
- Entity 类不属于 DTO，不应移入 `model/dto/`

---

## 修复规范五：创建缺失目录

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
    └── vo/        (如缺失则创建)
```

---

## 文件迁移标准流程

每个文件迁移遵循以下步骤：

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

## 执行流程

当用户确认修复计划后：

1. **按类别分批处理**：先处理 Service 层，再处理 DAO 层，最后处理 Model 层
2. **每批处理内按文件逐个操作**：避免大规模并行修改导致混乱
3. **import 联动更新**：每迁移一个文件后立即更新所有引用方
4. **标记注释**：所有修改的代码块添加 AI 代码标记

## 约束条件

- **不修改** 任何类名、方法签名、接口定义
- **不修改** HTTP 接口的 URL、HTTP 方法
- **不修改** Entity 类的包名（约束条件，除非用户明确要求）
- **不修改** 任何业务逻辑代码
- 务必在重构前获得用户确认
