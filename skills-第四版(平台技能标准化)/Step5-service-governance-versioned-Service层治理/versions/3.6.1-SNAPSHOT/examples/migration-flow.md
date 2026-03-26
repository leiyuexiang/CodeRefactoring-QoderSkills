# Service 文件迁移标准流程与操作示例

## 标准迁移步骤

每个 Service 文件迁移遵循以下 7 步流程：

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 更新为新包路径
3. Write 新文件 → 写入新位置
4. Grep 搜索引用 → 找到所有 import 该类的文件
5. Edit 更新引用 → 逐一修改 import 语句
6. Delete 原文件 → 删除原位置文件
7. 验证 → Grep 确认无残留旧路径引用
```

## 操作示例：Service 接口迁入 facade/

### 迁移文件

```
文件：IElementService.java
源位置：grp.pt.service.basedata.IElementService
目标位置：grp.pt.service.facade.IElementService
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.service.basedata;
// 新：package grp.pt.service.facade;

// 所有引用方的 import 语句：
// 旧：import grp.pt.service.basedata.IElementService;
// 新：import grp.pt.service.facade.IElementService;
```

### 额外同步更新

实现类中的接口 import 也需更新：

```java
// ElementServiceImpl.java 中
// 旧：import grp.pt.service.basedata.IElementService;
// 新：import grp.pt.service.facade.IElementService;
```

## 操作示例：Service 实现迁入 impl/

### 迁移文件

```
文件：ElementServiceImpl.java
源位置：grp.pt.service.basedata.impl.ElementServiceImpl
目标位置：grp.pt.service.impl.ElementServiceImpl
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.service.basedata.impl;
// 新：package grp.pt.service.impl;

// 所有引用方的 import 语句：
// 旧：import grp.pt.service.basedata.impl.ElementServiceImpl;
// 新：import grp.pt.service.impl.ElementServiceImpl;
```

## 操作示例：根目录 Service 接口迁移

### 迁移文件

```
文件：IUserService.java
源位置：grp.pt.service.IUserService
目标位置：grp.pt.service.facade.IUserService
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.service;
// 新：package grp.pt.service.facade;

// 所有引用方的 import 语句：
// 旧：import grp.pt.service.IUserService;
// 新：import grp.pt.service.facade.IUserService;
```

## 执行优先级

1. **创建目录**：先创建 `facade/` 和 `impl/` 目录（如不存在）
2. **接口迁移**：将所有 Service 接口迁入 `facade/`
3. **实现迁移**：将分散的 Service 实现统一迁入 `impl/`
4. **引用更新**：更新所有 import 引用
5. **验证清理**：确认无残留引用，删除空目录
