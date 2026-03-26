# S1 修复规范

## 修复规范一：消除 Controller→Controller 依赖

### 修复决策树

按以下优先级顺序判断修复策略：

```
1. 被注入的 Controller 仅用于 Logger 引用（如 XxxController.class 用于 getLogger）？
   → SKIP，标注为"仅 Logger 引用，非字段注入"，在报告中标记为 SKIP

2. 被注入的 Controller 有对应的 Service 接口，且该接口覆盖了所有被调用方法？
   → 策略 A：直接替换为 Service 接口注入

3. 被注入的 Controller 有对应的 Service 接口，但接口未覆盖部分被调用方法？
   → 策略 A + 在接口中追加缺失方法（签名与 Controller/Impl 方法一致）

4. 被注入的 Controller 方法包含复合逻辑（Session 处理、多 Service 编排等）？
   → 策略 B：提取 Private Helper 方法

5. 以上均不适用？
   → 标记为 SKIP 并说明原因，留待人工处理
```

### 修复策略 A：调用下沉到 Service 层（优先）

1. 分析被调用 Controller 方法中的核心业务逻辑
2. 确认该逻辑是否已在某个 Service 接口中有对应方法
3. 如果有 → 直接替换为 Service 调用
4. 如果没有 → 将被调用 Controller 方法中的业务逻辑提取为当前 Controller 的 private 方法，内部调用 Service 层完成

### 修复策略 B：提取 Private Helper 方法

当被调用 Controller 方法包含复合逻辑（Session 处理、多 Service 编排等）时：

1. 在当前 Controller 中创建 private 方法
2. 将被调用 Controller 方法的核心逻辑复制到 private 方法中
3. 将 Controller 调用替换为 Service 调用
4. 移除对被调用 Controller 的 `@Autowired` 注入

### 注意事项

- 修复后必须移除对 Controller 类的 `@Autowired` 声明和 `import` 语句
- 新注入的 Service 必须是接口类型（非 Impl）
- 确保所有调用点都已替换，无遗漏

---

## 修复规范二：Controller→DAO/Mapper 补建 Service 中间层

### 修复步骤

1. 在 `service/` 目录（或 `service/facade/`）下创建 Service 接口
2. 在 `service/impl/` 下创建 ServiceImpl 实现类
3. 将 Controller 中的 DAO 调用逻辑移入 ServiceImpl
4. Controller 改为注入 Service 接口

模板文件参见：
- Service 接口模板 → [templates/service-interface.java](../templates/service-interface.java)
- ServiceImpl 模板 → [templates/service-impl.java](../templates/service-impl.java)

---

## 修复规范三：Controller 注入 ServiceImpl → Service 接口

### 修复操作

1. 将字段类型从 `XxxServiceImpl` 改为 `IXxxService`（或 `XxxService`）
2. 更新 `import` 语句：从 `import xxx.service.impl.XxxServiceImpl` 改为 `import xxx.service.IXxxService`
3. 如果不存在 Service 接口 → 先提取接口，再修改注入

---

## 修复规范四：Entity 泄露修复

### 修复操作

1. **返回值泄露**：Controller 方法返回 Entity → 在 Service 层添加 Entity→DTO/VO 转换
2. **参数泄露**：Controller 方法参数为 Entity → 创建对应的 DTO/Query 对象替换

### 注意事项

- 仅在 Entity 泄露明确影响接口安全性时才修复
- 如果项目约定 Entity 直接作为 DTO 使用（如继承自 HashMap），标记为 WARN 但不强制修改

---

## 执行操作规范

修复过程中的操作规范：

1. **逐文件处理**：每个违规 Controller 单独处理
2. **先读后改**：使用 Read 读取文件 → 分析依赖 → 使用 Edit 修改
3. **import 同步**：修改依赖后立即更新 import 语句
4. **验证搜索**：修复后使用 Grep 搜索旧的依赖引用，确保无遗漏
5. **标记注释**：所有修改的代码块添加 AI 代码标记

---

## AI 标记一致性规范

### Tag 粒度规则

| 修复阶段 | Tag 粒度 | 说明 |
|----------|---------|------|
| Phase 5-A（S1-01 Controller→Controller） | 每个 FAIL 项一个 Tag | 同一 FAIL 涉及的所有文件共享同一 Tag |
| Phase 5-B（S1-02 Controller→DAO） | 每个 FAIL 项一个 Tag | 新建 Interface + Impl + Controller 修改共享 Tag |
| Phase 5-C（S1-03 ServiceImpl→Interface） | 全阶段一个 Tag | 同类批量替换使用统一 Tag |

### 标记范围规则

1. **新建文件**：整个文件内容（package 声明后、class 声明前开始，文件末尾结束）包裹在一对 AI 标记中
2. **修改已有文件**：仅修改的代码段落包裹在 AI 标记中，不修改的代码不添加标记
3. **import 语句修改**：当仅修改 import 时，AI 标记包裹修改的 import 行

### 标记位置示例

**正确 — 新建 Service 接口文件**：
```java
package com.xxx.service;

// @AI-Begin Rv7mK 20260325 @@Qoder
public interface IXxxService {
    void doSomething();
}
// @AI-End Rv7mK 20260325 @@Qoder
```

**正确 — 修改已有 Controller 的 import 和字段**：
```java
// @AI-Begin Rv7mK 20260325 @@Qoder
import com.xxx.service.IXxxService;
// @AI-End Rv7mK 20260325 @@Qoder

// ... 未修改的代码 ...

    // @AI-Begin Rv7mK 20260325 @@Qoder
    @Autowired
    private IXxxService xxxService;
    // @AI-End Rv7mK 20260325 @@Qoder
```

**错误 — 将整个文件包裹在 AI 标记中（已有文件仅修改少量内容时）**：
```java
// @AI-Begin Rv7mK 20260325 @@Qoder  ← 错误！不应包裹未修改代码
package com.xxx.controller;
// ... 整个文件 ...
// @AI-End Rv7mK 20260325 @@Qoder
```

---

## 接口设计规范引用

新建 Service 接口时，**必须**遵循接口设计规范 → [scripts/interface-design-rules.md](interface-design-rules.md)

核心要求：
- 方法签名必须与 ServiceImpl 中的 public 方法**完全一致**（返回类型、方法名、参数列表、throws）
- 禁止在接口中添加 ServiceImpl 中不存在的方法
- 禁止将 DAO/Mapper 层的 SQL 调用逻辑移入接口签名
- 禁止合并或拆分 ServiceImpl 中的方法
- 每个接口方法必须添加 JavaDoc 注释
- 如果 ServiceImpl 已实现某个现有接口，优先在该接口上追加方法
