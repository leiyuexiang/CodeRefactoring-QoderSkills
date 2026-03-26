# S4 修复规范

## 修复规范一：创建 custom/common 目录

### 操作步骤

1. 创建 `controller/custom/` 目录
2. 创建 `controller/common/` 目录
3. 根据已有 Controller 的业务分类，预创建二级分组目录

---

## 修复规范二：外部接口迁入 custom/

### 操作步骤

1. 识别所有外部接口 Controller（路径以 `run/` 开头或面向前端 UI 操作）
2. 确定二级业务分组目录（如 basedata、bookset、agencyManager）
3. 对每个文件执行标准迁移流程

### 标准迁移流程

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 插入 custom 层级
   - 例：grp.pt.controller.basedata → grp.pt.controller.custom.basedata
3. Write 新文件 → 写入 controller/custom/{business}/ 下
4. Grep 搜索引用 → 找到所有 import 该类的文件
5. Edit 更新引用 → 逐一修改 import 语句
6. Delete 原文件 → 删除原位置文件
7. 验证 → Grep 确认无残留旧路径引用
```

### 常见迁移映射

| 原位置 | 目标位置 |
|--------|---------|
| `controller/basedata/` | `controller/custom/basedata/` |
| `controller/bookset/` | `controller/custom/bookset/` |
| `controller/agencyManager/` | `controller/custom/agencyManager/` |
| `controller/org/` | `controller/custom/org/` |

---

## 修复规范三：内部接口迁入 common/

### 操作步骤

1. 识别所有内部接口 Controller（路径以 `config/` 开头或内部 API/工具/同步类）
2. 确定二级功能分组目录（如 api、util、notify、sync）
3. 对每个文件执行标准迁移流程

### 常见迁移映射

| 原位置 | 目标位置 |
|--------|---------|
| `controller/api/` | `controller/common/api/` |
| `controller/util/` | `controller/common/util/` |
| `controller/notify/` | `controller/common/notify/` |
| `controller/sync/` | `controller/common/sync/` |

---

## 修复规范四：controller/ 根目录残留文件处理

### 操作步骤

1. 检查 `controller/` 根目录下是否存在 Controller 文件
2. 根据接口路径前缀和业务职责判断 custom/common 归属
3. 确定二级分组后执行迁移
4. 非 Controller 类（工具类、常量类）迁入 `common/` 下或移出 controller 包

---

## 修复规范五：非标准子目录处理

### 处理规则

| 原始目录 | 处理方式 |
|---------|---------|
| `controller/imp/` | 根据业务职责逐个判断迁入 custom/ 或 common/ |
| 其他非标准目录 | 分析目录下 Controller 的接口类型后迁移 |

### 注意事项

- 非标准目录可能包含非 Controller 类，需逐文件分析
- 迁移前需完整统计影响范围
- 空目录在迁移完成后删除

---

## 执行操作规范

1. **按分类分批处理**：先迁移 custom/（外部接口），再迁移 common/（内部接口），最后处理残留
2. **每批处理内按文件逐个操作**：避免大规模并行修改导致混乱
3. **import 联动更新**：每迁移一个文件后立即更新所有引用方
4. **标记注释**：所有修改的代码块添加 AI 代码标记

> 文件迁移标准流程详见 → [examples/migration-flow.md](../examples/migration-flow.md)
