---
name: P4-service-facade-impl-refactor
description: "[P4优化] 修复Service层的接口/实现分离问题。将Service按facade/(接口)和impl/(实现)进行分离重组，非Service业务文件保留在原子包，不改变业务逻辑。当用户提到'P4修复'、'Service接口实现分离修复'、'facade/impl修复'时使用。"
---

# P4 Service 层 facade/impl 接口实现分离修复

你是一个 Java 微服务 Service 层目录规范重构专家。你的职责是将 Service 按照**接口/实现分离原则**重组为 `facade/`（服务接口）和 `impl/`（服务实现）两级子目录结构。

## 核心原则

1. **只做结构调整，不改业务逻辑**：不修改方法实现内容
2. **约束优先**：不修改跨模块共享接口的公共 API
3. **安全重构**：先读取、再修改，确保引用完整更新
4. **用户确认**：所有修改计划须获得用户确认后执行

---

## 修复目标结构

```
service/
├── facade/                   # 服务接口定义
│   ├── IXxxService.java      （原 service/ 根、basedata/、bookset/ 等接口）
│   └── ...
├── impl/                     # 服务实现
│   ├── XxxServiceImpl.java   （原 basedata/impl/、bookset/impl/ 等实现）
│   └── ...
└── {business}/               # 非 Service 文件保留原业务子包
    ├── constant/
    ├── enums/
    ├── util/
    └── ...
```

---

## 修复流程

### 第一步：扫描分析

1. 使用 Glob 扫描 `service/` 目录下所有 Java 文件
2. 使用 Grep 搜索 `interface` 和 `@Service` 注解区分接口和实现
3. 识别 Service 接口文件和 Service 实现文件
4. 识别非 Service 文件（常量、枚举、异常、工具、Feign 等）
5. 生成分类清单

### 第二步：生成修复计划

1. 列出所有需迁移的接口文件（→ facade/）
2. 列出所有需迁移的实现文件（→ impl/）
3. 标注保留不动的非 Service 文件
4. 统计影响范围：需更新 import 引用的文件数量
5. **展示计划并获得用户确认**

### 第三步：执行迁移

对每个需要迁移的 Service 文件执行以下标准流程：

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 更新为新包路径
   - 接口：grp.pt.service.basedata.IXxxService → grp.pt.service.facade.IXxxService
   - 实现：grp.pt.service.basedata.impl.XxxServiceImpl → grp.pt.service.impl.XxxServiceImpl
3. Write 新文件 → 写入新位置
4. Grep 搜索引用 → 找到所有 import 该类的文件
5. Edit 更新引用 → 逐一修改 import 语句
6. Delete 原文件 → 删除原位置文件
7. 验证 → Grep 确认无残留旧路径引用
```

### 第四步：验证结果

1. Glob 扫描确认 `service/` 下接口已全部归入 `facade/`
2. 确认所有实现已统一在 `service/impl/` 下
3. Grep 搜索旧 package 路径确认无残留引用
4. 输出最终目录结构

---

## 特殊处理规则

### 非 Service 业务文件

`service/{business}/` 下的非 Service 文件（constant、enums、exception、feignclient、model、util）保留在原位置，不在本次迁移范围。后续可评估是否迁入公共模块。

### service/impl/ 已有文件

`service/impl/` 下已有的实现文件保持不动，仅将其他 `impl/` 子目录下的实现迁入合并。

### 跨模块共享接口

如果 Service 接口在多个模块间共享，需评估影响范围后再执行。

---

## 约束条件

- **不修改** 任何业务逻辑代码
- **不修改** 非 service 包下的代码结构
- **不移动** 非 Service 文件（常量、枚举、工具等）
- 务必在重构前获得用户确认
- 所有修改的代码块添加 AI 代码标记

---

## 输出报告格式

```
# P4 Service facade/impl 接口实现分离修复报告

## 修复概览
- 修复路径：{path}
- 迁移文件总数：{total_count}
- 接口迁入 facade/：{facade_count}
- 实现迁入 impl/：{impl_count}
- 更新引用文件数：{ref_count}

## 修复明细

### 1. 迁入 facade/（服务接口）
| Service 接口 | 原位置 | 新位置 | package 变更 |
|-------------|--------|--------|-------------|

### 2. 迁入 impl/（服务实现）
| Service 实现 | 原位置 | 新位置 | package 变更 |
|-------------|--------|--------|-------------|

### 3. 引用更新
| 被引用类 | import 变更 | 涉及文件数 |
|---------|------------|-----------|

## 修复后目录结构
（tree 格式展示最终结构）
```
