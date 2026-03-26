# Controller 文件迁移标准流程与操作示例

## 标准迁移步骤

每个 Controller 文件迁移遵循以下 7 步流程：

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 更新为新包路径
3. Write 新文件 → 写入新位置
4. Grep 搜索引用 → 找到所有 import 该类的文件
5. Edit 更新引用 → 逐一修改 import 语句
6. Delete 原文件 → 删除原位置文件
7. 验证 → Grep 确认无残留旧路径引用
```

## 操作示例：外部接口迁入 custom/

### 迁移文件

```
文件：ElementController.java
源位置：grp.pt.controller.basedata.ElementController
目标位置：grp.pt.controller.custom.basedata.ElementController
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.controller.basedata;
// 新：package grp.pt.controller.custom.basedata;

// 所有引用方的 import 语句：
// 旧：import grp.pt.controller.basedata.ElementController;
// 新：import grp.pt.controller.custom.basedata.ElementController;
```

## 操作示例：内部接口迁入 common/

### 迁移文件

```
文件：CacheUtilController.java
源位置：grp.pt.controller.util.CacheUtilController
目标位置：grp.pt.controller.common.util.CacheUtilController
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.controller.util;
// 新：package grp.pt.controller.common.util;

// 所有引用方的 import 语句：
// 旧：import grp.pt.controller.util.CacheUtilController;
// 新：import grp.pt.controller.common.util.CacheUtilController;
```

## 操作示例：根目录 Controller 迁移

### 迁移文件

```
文件：NotifyController.java
源位置：grp.pt.controller.NotifyController
目标位置：grp.pt.controller.common.notify.NotifyController
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.controller;
// 新：package grp.pt.controller.common.notify;

// 所有引用方的 import 语句：
// 旧：import grp.pt.controller.NotifyController;
// 新：import grp.pt.controller.common.notify.NotifyController;
```

## 执行优先级

1. **创建目录**：先创建 `custom/` 和 `common/` 及其二级分组目录
2. **外部接口**：迁移所有外部接口 Controller 到 `custom/` 下
3. **内部接口**：迁移所有内部接口 Controller 到 `common/` 下
4. **根目录残留**：处理 `controller/` 根目录下的残留文件
5. **验证清理**：确认无残留引用，删除空目录
