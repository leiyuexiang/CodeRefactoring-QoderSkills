# S5 修复规范

## 修复规范一：接口路径调整（约束限制项）

**此项通常不修改**。可执行的安全兼容修改：

1. `@DeleteMapping` → `@RequestMapping(value = "/xxx", method = {RequestMethod.DELETE, RequestMethod.POST})`
2. `@PutMapping` → `@RequestMapping(value = "/xxx", method = {RequestMethod.PUT, RequestMethod.POST})`

### 操作步骤

1. 搜索所有 `@DeleteMapping` 和 `@PutMapping` 注解
2. 替换为 `@RequestMapping` 并添加 POST 兼容
3. 保持原 URL 路径不变

### 约束

- **不修改** 任何接口 URL 路径
- **不修改** GET/POST 接口的 HTTP 方法
- 仅对 DELETE/PUT 做 POST 兼容增强

---

## 修复规范二：类命名修正

### 操作步骤

1. **后缀修正**：`XxxCtrl` → `XxxController`
2. **大驼峰修正**：`xxxController` → `XxxController`
3. **文件名同步**：类名修改后同步修改文件名
4. **引用更新**：更新所有 import 和使用位置

### 注意事项

- 修改类名时需同步检查 Bean 名称是否冲突
- 文件名必须与类名一致
- 所有引用方（import、注入点、配置文件）均需更新

---

## 修复规范三：Bean 命名冲突处理

### 操作步骤

1. 先确认 "Controller2" 等命名是否为有意设计
2. 如果是有意设计 → **不修改**
3. 如果是实际冲突 → 修改一方的 Bean 名称

### 处理策略

| 场景 | 处理方式 |
|------|---------|
| 同名 Controller 在不同模块 | 添加 `@Controller("模块名XxxController")` 指定 Bean 名 |
| "2" 后缀命名 | 确认原因后决定是否重命名 |
| 不同类型同名 Bean | 修改冲突方，使用有意义的名称 |

---

## 执行操作规范

1. **按类别分批处理**：先处理接口路径兼容，再处理类命名，最后处理 Bean 冲突
2. **每批处理内按文件逐个操作**：避免大规模并行修改导致混乱
3. **import 联动更新**：每修改一个类名后立即更新所有引用方
4. **标记注释**：所有修改的代码块添加 AI 代码标记

> 命名规范速查表见 → [templates/naming-convention.md](../templates/naming-convention.md)
