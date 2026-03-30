# 文件迁移标准流程与操作示例

## 标准迁移步骤

每个文件迁移遵循以下 **8 步**流程：

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 更新为新包路径
3. 冲突预检 → Glob 检查目标位置是否存在同名文件
   - 若不存在 → 继续步骤 4
   - 若存在且内容一致（忽略 package 行和 AI 标记行） → 跳过步骤 4，直接执行步骤 5-8
   - 若存在且内容不同 → 终止本文件迁移，在报告中标注 CONFLICT，跳过此文件
4. Write 新文件 → 写入新位置
5. Grep 搜索引用 → 找到所有 import 该类的文件
6. Edit 更新引用 → 逐一修改 import 语句
7. Delete 原文件 → 删除原位置文件
8. 验证 → Grep 确认无残留旧路径引用
```

### 冲突预检详细说明（步骤 3）

**检查方法**：使用 `Glob("{target-dir}/{FileName}.java")` 检查目标文件是否存在。

**内容比较方法**（当目标文件存在时）：
1. 读取源文件和目标文件的全部内容
2. 忽略以下行的差异后逐行比较：
   - `package` 声明行（因包路径不同导致的预期差异）
   - AI 标记注释行（`// @AI-Begin`、`// @AI-End` 等）
3. 若剩余行完全一致 → 判定为"完全相同"
4. 若存在差异 → 判定为"内容不同"

**冲突处理策略**：

| 场景 | 条件 | 处理 |
|------|------|------|
| 无冲突 | 目标位置不存在同名文件 | 正常执行步骤 4-8 |
| 完全相同 | 目标位置存在同名文件，内容一致 | 跳过步骤 4（不写新文件），直接执行步骤 5-8（更新引用、删源文件、验证） |
| 内容不同 | 目标位置存在同名文件，内容不同 | **终止本文件迁移**，在修复报告中标注为 CONFLICT，不做任何操作 |

## 操作示例：imp → impl 迁移

### 迁移文件

```
文件：XxxDaoImpl.java
源位置：dao/imp/XxxDaoImpl.java
目标位置：dao/impl/XxxDaoImpl.java
```

### Step 1-3: 读取、修改、预检

```java
// 修改 package 声明
// 旧：package grp.xx.dao.imp;
// 新：package grp.xx.dao.impl;
// 预检：Glob("dao/impl/XxxDaoImpl.java") → 不存在 → 继续
```

### Step 4-6: 写入、搜索、更新引用

```java
// 所有引用方的 import 语句：
// 旧：import grp.xx.dao.imp.XxxDaoImpl;
// 新：import grp.xx.dao.impl.XxxDaoImpl;
```

### Step 7-8: 删除原文件并验证

使用 Grep 搜索 `grp.xx.dao.imp` 确认全局无残留。

## 操作示例：合成词变体 serviceImp → impl 迁移

### 迁移文件

```
文件：IndexMessageServiceImpl.java
源位置：service/serviceImp/IndexMessageServiceImpl.java
目标位置：service/impl/IndexMessageServiceImpl.java
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.xx.service.serviceImp;
// 新：package grp.xx.service.impl;

// 所有引用方的 import 语句：
// 旧：import grp.xx.service.serviceImp.IndexMessageServiceImpl;
// 新：import grp.xx.service.impl.IndexMessageServiceImpl;
```

## 操作示例：DTO 归类

### 迁移文件

```
文件：UserDTO.java
源位置：model/UserDTO.java
目标位置：model/dto/UserDTO.java
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.xx.model;
// 新：package grp.xx.model.dto;

// 所有引用方的 import 语句：
// 旧：import grp.xx.model.UserDTO;
// 新：import grp.xx.model.dto.UserDTO;
```

## 操作示例：BO 归类至 vo 目录

### 迁移文件

```
文件：ElementBo.java
源位置：model/ElementBo.java
目标位置：model/vo/ElementBo.java
判定规则：优先级2（类名以 Bo 结尾 → model/vo/）
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.xx.model;
// 新：package grp.xx.model.vo;

// 所有引用方的 import 语句：
// 旧：import grp.xx.model.ElementBo;
// 新：import grp.xx.model.vo.ElementBo;
```

## 操作示例：Mapper 迁入 dao/mapper/

### 迁移文件

```
文件：XxxMapper.java
源位置：grp/pt/mapper/XxxMapper.java
目标位置：grp/pt/dao/mapper/XxxMapper.java
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.pt.mapper;
// 新：package grp.pt.dao.mapper;

// MyBatis XML namespace 更新：
// 旧：namespace="grp.pt.mapper.XxxMapper"
// 新：namespace="grp.pt.dao.mapper.XxxMapper"

// @MapperScan 更新：
// 旧：@MapperScan("grp.pt.mapper")
// 新：@MapperScan("grp.pt.dao.mapper")
```

## 操作示例：PO 兜底归类（无标准后缀文件）

### 判定依据

文件 `Module.java` 位于 `model/` 根目录，类名无 DTO/VO/Query/Entity/Param 等标准后缀，按 6 级优先级匹配链的第 6 级（兜底规则）归入 `model/po/`。

> **注意**：此分类完全基于类名后缀机械匹配，禁止通过阅读文件内容来判断应归入 dto/ 还是 vo/ 等目录。

### 迁移文件

```
文件：Module.java
源位置：model/Module.java
目标位置：model/po/Module.java
判定规则：优先级6（兜底 → model/po/）
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.xx.model;
// 新：package grp.xx.model.po;

// 所有引用方的 import 语句：
// 旧：import grp.xx.model.Module;
// 新：import grp.xx.model.po.Module;
```

### 同类示例

以下文件均无标准后缀，统一按兜底规则归入 `model/po/`：

```
Tenant.java       → model/ → model/po/
SysConfig.java    → model/ → model/po/
OperLog.java      → model/ → model/po/
Department.java   → model/ → model/po/
ErrorLoggerInfo.java → model/ → model/po/（Info 非标准后缀，兜底）
LogInfo.java      → model/ → model/po/（Info 非标准后缀，兜底）
```

## 操作示例：DAO 兜底归类（无 Impl 后缀的实现类）

### 判定依据

文件 `BpmDao.java` 位于 `dao/` 根目录，不以 `Impl.java` 结尾、不以 `Mapper.java` 结尾、不以 `Entity.java` 结尾、不以 `I` 大写开头，按 DAO 分类规则表的第 8 优先级（兜底）推定为实现类，移入 `dao/impl/`。

### 迁移文件

```
文件：BpmDao.java
源位置：dao/BpmDao.java
目标位置：dao/impl/BpmDao.java
判定规则：优先级8（兜底推定为实现类 → dao/impl/）
```

### 变更内容

```java
// 修改 package 声明
// 旧：package grp.xx.dao;
// 新：package grp.xx.dao.impl;

// 所有引用方的 import 语句：
// 旧：import grp.xx.dao.BpmDao;
// 新：import grp.xx.dao.impl.BpmDao;
```

## 操作示例：冲突处理（目标位置已存在同名文件）

### 场景

`model/BusinessTableBo.java`（源文件）需要迁移到 `model/vo/BusinessTableBo.java`（目标位置），但目标位置已存在该文件。

### 步骤 3 冲突预检

```
1. Glob("model/vo/BusinessTableBo.java") → 存在
2. 读取两个文件内容，忽略 package 行和 AI 标记行
3. 比较结果：
   - 若内容一致 → 跳过写入，删除 model/BusinessTableBo.java，更新所有 import
   - 若内容不同 → 标注 CONFLICT，跳过此文件
```

## 操作示例：Mapper 冗余副本处理

### 判定依据

`dao/mapper/` 已存在 Mapper 文件，独立 `mapper/` 包中存在同名副本，属于冗余。仅保留 `dao/mapper/` 中的版本。

### 处理步骤

```
1. 确认 dao/mapper/XxxMapper.java 已存在
2. 对比两个文件内容是否一致（忽略 package 行和 AI 标记行）
3. 若一致 → 直接删除独立 mapper/ 包中的副本
4. 若不一致 → 以 dao/mapper/ 为准，删除独立 mapper/ 包中的副本
5. 更新所有 import 语句指向 dao/mapper/ 下的类
6. 清理空目录
```

## 处理顺序

按类别分批处理，避免交叉影响：

1. **第一批**：目录命名修正（imp→impl，含合成词变体如 serviceImp→impl）— 影响面最广，优先处理
2. **第二批**：DAO 层归位 — 按确定性分类规则表处理 dao/ 根目录文件（实现类移入 impl/），Mapper/Entity 分离，Mapper 冗余副本清理
3. **第三批**：Model 层归类 — 按 6 级优先级匹配链将文件归入 dto/vo/query/po 子目录（BO 归入 vo/，无后缀文件兜底归入 po/）
4. **第四批**：创建缺失目录（含 po/）— 仅创建目录，无文件移动
