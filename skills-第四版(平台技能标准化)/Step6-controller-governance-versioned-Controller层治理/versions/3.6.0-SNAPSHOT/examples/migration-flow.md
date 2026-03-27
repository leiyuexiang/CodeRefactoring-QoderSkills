# Controller 文件迁移标准流程与操作示例

## 标准迁移步骤

每个 Controller 文件迁移遵循以下 9 步流程：

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 更新为目标包路径
3. Write 新文件 → 写入目标位置
4. Grep 搜索引用 → 找到所有 import 该类的文件
5. Edit 更新引用 → 逐一修改 import 语句
5.5. 检查 @ComponentScan → 更新 basePackages 引用
5.6. 检查 Spring XML 配置 → 更新 component-scan 引用
6. Delete 原文件 → 删除原位置文件
7. 验证 → Grep 确认无残留旧路径引用
```

---

## 操作示例一：L3 默认归 custom（业务 Controller）

### 分类信息

```
文件：ElementController.java
分类级别：L3（默认归 custom）
命中规则：默认 custom, businessGroup=basedata
业务分组提取：extractBusinessGroup("grp.pt.frame.config.basedata.controller") → "basedata"
```

### 迁移路径

```
源位置：grp.pt.frame.config.basedata.controller.ElementController
目标位置：grp.pt.frame.controller.custom.basedata.ElementController

modulePrefix = grp.pt.frame（config 之前的所有段）
category = custom（L3 默认）
subdirectory = basedata（extractBusinessGroup 提取）
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.frame.config.basedata.controller;
// 新：package grp.pt.frame.controller.custom.basedata;

// 所有引用方的 import 语句：
// 旧：import grp.pt.frame.config.basedata.controller.ElementController;
// 新：import grp.pt.frame.controller.custom.basedata.ElementController;
```

---

## 操作示例二：L2 关键词匹配归 common/monitor（监控 Controller）

### 分类信息

```
文件：ServerInfoController.java
分类级别：L2
命中规则：L2-P3: className 包含 ServerInfo → monitor
```

### 迁移路径

```
源位置：grp.pt.frame.config.monitoringcenter.controller.ServerInfoController
目标位置：grp.pt.frame.controller.common.monitor.ServerInfoController

modulePrefix = grp.pt.frame
category = common（L2 命中）
subdirectory = monitor（L2-P3 规则指定）
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.frame.config.monitoringcenter.controller;
// 新：package grp.pt.frame.controller.common.monitor;

// 所有引用方的 import 语句：
// 旧：import grp.pt.frame.config.monitoringcenter.controller.ServerInfoController;
// 新：import grp.pt.frame.controller.common.monitor.ServerInfoController;
```

---

## 操作示例三：L2 关键词匹配归 common/sso

### 分类信息

```
文件：SsoController.java
分类级别：L2
命中规则：L2-P1: className 包含 Sso → sso
```

### 迁移路径

```
源位置：grp.pt.frame.config.sso.controller.SsoController
目标位置：grp.pt.frame.controller.common.sso.SsoController

modulePrefix = grp.pt.frame
category = common
subdirectory = sso（L2-P1 规则指定）
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.frame.config.sso.controller;
// 新：package grp.pt.frame.controller.common.sso;

// 所有引用方的 import 语句：
// 旧：import grp.pt.frame.config.sso.controller.SsoController;
// 新：import grp.pt.frame.controller.common.sso.SsoController;
```

---

## 操作示例四：非 Controller 类（跳过）

### 分类信息

```
文件：GapModuleOperLog.java
注解：无 @Controller / @RestController
处理：SKIP — 不迁移，保留原位
```

### 说明

该文件位于 controller 包下但不是 Controller 类（无 @Controller 或 @RestController 注解），根据规则**不迁移**，仅在检查报告中以 INFO 级别提示。

---

## 执行顺序

1. **创建目录**：先创建 `controller/custom/` 和 `controller/common/` 及其二级子目录
2. **common 类型优先**：先迁移 L1/L2 命中的 common 文件（数量较少）
3. **custom 类型其次**：再迁移 L3 默认的 custom 文件（数量较多）
4. **同类型内按字母序**：同一类型内按 className 字母顺序处理
5. **验证清理**：确认无残留引用，清理空目录（遵守 S-09 约束）

---

## 操作示例五：交叉 import 联动更新（S-12 约束）

### 场景说明

当已迁移的 Controller A 的 import 中引用了另一个也需迁移的 Controller B 时，A 的 import 必须同步更新为 B 的目标 package 路径。这是实际发生过的遗漏案例。

### 分类信息

```
文件 A：UiViewCatelogController.java
分类级别：L3（默认归 custom）
业务分组提取：extractBusinessGroup("grp.pt.frame.config2.catelog.controller") → "catelog"
迁移目标：grp.pt.frame.controller.custom.catelog

文件 B：YearController.java
分类级别：L3（默认归 custom）
业务分组提取：extractBusinessGroup("grp.pt.frame.config.year.controller") → "year"
迁移目标：grp.pt.frame.controller.custom.year
```

### 问题场景

文件 A（UiViewCatelogController）在其 import 中引用了文件 B（YearController）：

```java
// UiViewCatelogController.java 的 import 列表中
import grp.pt.frame.config.year.controller.YearController;  // ← 旧路径
```

### 正确处理

迁移 A 时（步骤 5: Edit 更新 import 引用），除了更新 A 自身的 package 声明，还需要检查 A 内部的 import 语句：

```java
// 修改 A 的 package 声明
// 旧：package grp.pt.frame.config2.catelog.controller;
// 新：package grp.pt.frame.controller.custom.catelog;

// 同时更新 A 内部对 B 的 import（双向引用更新）
// 旧：import grp.pt.frame.config.year.controller.YearController;
// 新：import grp.pt.frame.controller.custom.year.YearController;
```

### 错误处理（已发生过的遗漏）

仅更新了 A 的 package 声明，但遗漏了 A 内部对 B 的 import 更新：

```java
// ❌ 错误：A 已迁移到新位置，但 import 仍指向 B 的旧路径
package grp.pt.frame.controller.custom.catelog;
import grp.pt.frame.config.year.controller.YearController;  // ← 残留旧路径！
```

这会导致编译失败（如果 B 也已迁移）或引用错误。

### Phase 5 验证

在 Phase 5 的 5.1.2 内部交叉引用验证中，此类遗漏会被检出：

```
| 文件 | 残留旧 import | 应更新为 |
|------|-------------|---------|
| UiViewCatelogController.java | import grp.pt.frame.config.year.controller.YearController | import grp.pt.frame.controller.custom.year.YearController |
```
