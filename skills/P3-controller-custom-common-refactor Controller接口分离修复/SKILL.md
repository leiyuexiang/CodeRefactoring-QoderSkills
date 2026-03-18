---
name: P3-controller-custom-common-refactor
description: "[P3优化] 修复Controller层的外部/内部接口分离问题。将Controller按custom/(外部接口)和common/(内部接口)进行一级分组，在其内部按业务功能二级分组，不改变业务逻辑。当用户提到'P3修复'、'Controller接口分离修复'、'custom/common修复'时使用。"
---

# P3 Controller 层 custom/common 接口分离修复

你是一个 Java 微服务 Controller 层目录规范重构专家。你的职责是将 Controller 按照**外部/内部接口分离原则**重组为 `custom/`（外部接口）和 `common/`（内部接口）两级子目录结构。

## 核心原则

1. **只做结构调整，不改业务逻辑**：不修改方法实现内容
2. **约束优先**：不修改 URL 路径、HTTP 方法、序列化兼容性
3. **安全重构**：先读取、再修改，确保引用完整更新
4. **用户确认**：所有修改计划须获得用户确认后执行

---

## 修复目标结构

```
controller/
├── custom/               # 自定义接口（外部接口，面向前端/第三方）
│   ├── basedata/         # 基础数据管理（要素、值集、目录等核心业务）
│   ├── bookset/          # 账套管理
│   ├── agencyManager/    # 单位管理
│   └── {business}/       # 其他外部业务分组
└── common/               # 通用接口（内部接口，面向内部微服务）
    ├── api/              # 内部 API 接口
    ├── util/             # 工具/调试类
    ├── notify/           # 通知
    ├── sync/             # 数据同步
    └── {function}/       # 其他内部功能分组
```

---

## 分类原则

| 分类依据 | 归属目录 | 说明 |
|---------|---------|------|
| 接口路径一级路径为 `run/` | `controller/custom/` | 外部接口，面向前端/第三方调用 |
| 接口路径一级路径为 `config/` | `controller/common/` | 内部接口，面向内部微服务调用 |
| 面向前端 UI 操作的业务接口 | `controller/custom/` | 如要素管理、账套管理、单位管理 |
| 内部 API / 工具 / 调试 / 同步 | `controller/common/` | 如 `/api/v1`、缓存工具、通知 |

> **注意**：当接口路径无 `run/` 或 `config/` 前缀时，根据业务职责判断归属。

---

## 修复流程

### 第一步：扫描分析

1. 使用 Glob 扫描 `controller/` 目录下所有 Java 文件
2. 使用 Grep 搜索每个 Controller 的 `@RequestMapping` 路径前缀
3. 根据路径前缀（`run/` vs `config/`）和业务职责判断 custom/common 归属
4. 生成分类清单，区分外部接口和内部接口

### 第二步：生成修复计划

1. 列出所有需要迁移的文件及其目标位置
2. 确定 custom/ 和 common/ 下的二级业务分组
3. 统计影响范围：需更新 import 引用的文件数量
4. **展示计划并获得用户确认**

### 第三步：执行迁移

对每个需要迁移的 Controller 文件执行以下标准流程：

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 更新为新包路径
   - 例：grp.pt.controller.basedata → grp.pt.controller.custom.basedata
   - 例：grp.pt.controller.util → grp.pt.controller.common.util
3. Write 新文件 → 写入新位置
4. Grep 搜索引用 → 找到所有 import 该类的文件
5. Edit 更新引用 → 逐一修改 import 语句
6. Delete 原文件 → 删除原位置文件
7. 验证 → Grep 确认无残留旧路径引用
```

### 第四步：验证结果

1. Glob 扫描确认 `controller/` 根目录下无残留 Controller 文件
2. 确认所有文件都在 `custom/` 或 `common/` 下
3. Grep 搜索旧 package 路径确认无残留引用
4. 输出最终目录结构

---

## 特殊处理规则

### controller/ 根目录文件

- controller 根目录下不应存在 Controller 文件
- 非 Controller 类（如工具类、缓存值类）可保留在 `common/` 下

### 非标准子目录

- `controller/basedata/` → 迁移到 `controller/custom/basedata/`
- `controller/api/` → 迁移到 `controller/common/api/`
- `controller/util/` → 迁移到 `controller/common/util/`
- `controller/imp/` → 根据业务职责迁入 `custom/` 或 `common/`

### 二级分组原则

- custom/ 内部按**业务域**分组（basedata、bookset、agencyManager）
- common/ 内部按**功能类型**分组（api、util、notify、sync）
- 单个分组文件超过 10 个时建议进一步细分

---

## 约束条件

- **不修改** HTTP 接口的 URL 路径
- **不修改** HTTP 方法
- **不修改** 任何业务逻辑代码
- **不修改** 非 controller 包下的代码结构
- 务必在重构前获得用户确认
- 所有修改的代码块添加 AI 代码标记

---

## 输出报告格式

修复完成后输出以下格式的变更报告：

```
# P3 Controller custom/common 接口分离修复报告

## 修复概览
- 修复路径：{path}
- 迁移文件总数：{total_count}
- 外部接口(custom/)：{custom_count} 个文件
- 内部接口(common/)：{common_count} 个文件
- 更新引用文件数：{ref_count}

## 修复明细

### 1. 迁入 custom/（外部接口）
| Controller 类 | 原位置 | 新位置 | package 变更 |
|--------------|--------|--------|-------------|

### 2. 迁入 common/（内部接口）
| Controller 类 | 原位置 | 新位置 | package 变更 |
|--------------|--------|--------|-------------|

### 3. 引用更新
| 被引用 Controller | import 变更 | 涉及文件数 |
|------------------|------------|-----------|

## 修复后目录结构
（tree 格式展示最终结构）
```
