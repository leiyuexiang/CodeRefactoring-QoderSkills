# S3 修复规范

## 修复规范一：Service 层接口/实现分离

### 实现类移入 impl/

#### 操作步骤

1. 识别 `service/` 根目录或其他非标准位置的 Service 实现类
2. 判断标准：包含 `@Service` 注解，或类名以 `Impl`/`ServiceImpl` 结尾
3. 移动到 `service/impl/` 下
4. 更新 package 声明和所有 import 引用

#### 注意事项

- 接口文件应保留在 `service/` 根目录或 `service/facade/` 下
- 如果接口和实现在同一目录，只移动实现类
- 检查是否有 Spring `@ComponentScan` 需要调整扫描路径

---

## 修复规范二：创建 facade/impl 目录

### 操作步骤

1. 创建 `service/facade/` 目录（如不存在）
2. 创建 `service/impl/` 目录（如不存在）

---

## 修复规范三：Service 接口迁入 facade/

### 操作步骤

1. 识别所有 Service 接口（`interface` 类型 + 类名含 `Service` 后缀，排除 `@FeignClient`）
2. 对每个接口文件执行标准迁移流程

### 标准迁移流程

```
1. Read 原文件 → 获取完整内容
2. 修改 package 声明 → 更新为 service.facade
   - 例：grp.pt.service.basedata.IElementService → grp.pt.service.facade.IElementService
   - 例：grp.pt.service.IUserService → grp.pt.service.facade.IUserService
3. Write 新文件 → 写入 service/facade/ 下
   ⚠️ 编码保留（强制）：
   - 必须保持原文件的字符编码格式（如 UTF-8、UTF-8 with BOM、GBK 等）
   - 原文件为 UTF-8 with BOM（首3字节为 EF BB BF）时，新文件必须保留 BOM
   - 推荐方式：先通过 Bash 的 cp/copy 命令复制文件到目标位置（保留编码），再用 Edit 修改 package 声明行
   - 备选方式：使用 Write 工具写入内容时注意编码一致性
4. Grep 搜索引用 → 找到所有 import 该类的文件
5. Edit 更新引用 → 逐一修改 import 语句
6. Delete 原文件 → 删除原位置文件
7. 验证 → Grep 确认无残留旧路径引用
```

### 常见迁移映射

| 原位置 | 目标位置 |
|--------|---------|
| `service/basedata/IElementService` | `service/facade/IElementService` |
| `service/bookset/IBooksetService` | `service/facade/IBooksetService` |
| `service/IUserService` | `service/facade/IUserService` |

---

## 修复规范四：Service 实现迁入 impl/

### 操作步骤

1. 识别所有 Service 实现类（`@Service` 注解 + `ServiceImpl` 后缀）
2. 排除已在 `service/impl/` 下的文件（无需移动）
3. 对每个需迁移的实现文件执行标准迁移流程

### 常见迁移映射

| 原位置 | 目标位置 |
|--------|---------|
| `service/basedata/impl/ElementServiceImpl` | `service/impl/ElementServiceImpl` |
| `service/bookset/impl/BooksetServiceImpl` | `service/impl/BooksetServiceImpl` |
| `service/agencyManager/impl/AgencyServiceImpl` | `service/impl/AgencyServiceImpl` |

### 注意事项

- `service/impl/` 下已有的实现文件保持不动
- 仅将其他分散的 `impl/` 子目录下的实现迁入合并
- 迁移后实现类的 import 需同步更新（接口路径已变为 `service.facade.*`）

---

## 修复规范五：非 Service 文件处理

### 处理原则

非 Service 文件（constant、enums、exception、util、model、feign）**不在本次迁移范围**，保留在原业务子包。

### 注意事项

- 如果 `service/basedata/` 下所有 Service 接口和实现都已迁出，但仍存在 `constant/`、`enums/` 等非 Service 子目录，则 `basedata/` 目录保留
- 非 Service 文件后续可评估是否迁入公共模块，但不在 S3 修复范围内
- 在修复报告中列出保留的非 Service 文件及其位置

---

## 修复规范六：清理空目录

### 操作步骤

1. 迁移完成后检查原业务子目录（如 `basedata/impl/`）是否为空
2. 如为空则删除
3. 检查上层目录（如 `basedata/`）除非 Service 文件外是否为空
4. 仅 Service 文件全部迁出且无其他子目录时才删除

---

## 执行操作规范

1. **先迁接口后迁实现**：先将所有 Service 接口迁入 `facade/`，再迁移实现类
2. **每批处理内按文件逐个操作**：避免大规模并行修改导致混乱
3. **import 联动更新**：每迁移一个文件后立即更新所有引用方
4. **标记注释**：所有修改的代码块添加 AI 代码标记

> 文件迁移标准流程详见 → [examples/migration-flow.md](../examples/migration-flow.md)
