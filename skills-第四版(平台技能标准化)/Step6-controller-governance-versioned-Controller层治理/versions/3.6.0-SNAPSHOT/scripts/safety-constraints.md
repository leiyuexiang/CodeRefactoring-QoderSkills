# 修复安全约束与核心原则

## 核心原则

| 编号 | 原则 | 说明 |
|------|------|------|
| C-01 | **只做结构调整，不改业务逻辑** | 仅修改 package 声明和 import 语句，不修改方法实现内容 |
| C-02 | **安全迁移** | 先读取原文件 → 在新位置创建文件 → 更新引用 → 删除原文件 |
| C-03 | **约束优先** | 不修改 URL 路径、HTTP 方法、序列化兼容性 |
| C-04 | **用户确认** | 所有修改计划须获得用户确认后执行 |
| C-05 | **保持可编译** | 迁移后确保所有 import 路径和 package 声明正确。**特别注意**：已迁移 Controller 内部 import 其他已迁移 Controller 时，引用路径必须更新为迁移后的目标 package，不得保留旧路径 |
| C-06 | **确定性保证** | 分类和迁移结果由确定性算法唯一确定，不依赖主观判断 |

## 安全约束（红线）

执行修复前必须遵守以下约束，**任何一条违反都必须立即停止并报告**：

| 编号 | 约束 | 说明 |
|------|------|------|
| S-01 | **不修改 HTTP 接口 URL** | 任何接口的 URL 路径不得改变 |
| S-02 | **不修改 HTTP 方法** | 任何接口的 HTTP 方法不得改变 |
| S-03 | **不修改业务逻辑代码** | 任何方法体内容不得改变 |
| S-04 | **不修改非 controller 包** | 不影响 service/dao/model 等其他包的结构（仅更新其中的 import 语句） |
| S-05 | **必须用户确认** | 重构前必须将修复计划展示给用户并获得确认 |
| S-06 | **无文件冲突** | 目标目录不得存在同名文件，迁移前必须检查 |
| S-07 | **不跨模块迁移** | Controller 仅在所属 Maven 模块内迁移，不得将文件移到其他 Maven 模块 |
| S-08 | **不自动拆分** | 当一个 Controller 类同时包含外部和内部接口方法时，不得自动拆分为两个类。在报告中标注 WARN，由用户人工决定 |
| S-09 | **空目录清理规则** | 迁移完成后，仅删除满足以下全部条件的目录：(a) 位于原 controller 相关路径下，(b) 包含零个 .java 文件，(c) 不是 `custom/` 或 `common/` 目录。`custom/` 和 `common/` 即使为空也不删除 |
| S-10 | **名称冲突防护** | 写入目标路径前，必须检查是否已存在同名文件。如存在冲突，立即停止迁移并报告错误，等待用户决策 |
| S-11 | **禁止语义归并** | custom/ 下的 businessGroup 目录名必须严格由 extractBusinessGroup 公式计算，不允许基于语义、类名含义、已有目录名称进行重命名或合并。违反此条等同于违反 C-06 确定性保证。详见 classification-guide.md Level 3 的 L3-PROHIBIT-01~03 |
| S-12 | **import 联动完整性** | 每个已迁移 Controller 文件内的 import 语句中，凡引用其他已迁移 Controller 的，必须全部更新为目标 package 路径。Phase 5 验证必须对此进行全量检查，发现残留即为 FAIL |
| S-13 | **禁止重新生成文件内容** | 迁移时必须复制原文件完整内容，禁止将原文件重写为空壳类、最小化实现或骨架代码。迁移后文件行数与原文件之差不得超过±2行，超过则为严重缺陷，必须立即停止并回滚 |
| S-14 | **编码保留** | 原文件为 UTF-8 with BOM 时，新文件必须保留 BOM。推荐使用 search_replace 工具而非 create_file 工具，以避免编码丢失导致中文乱码 |

## 允许修改的范围

- Controller Java 文件的 `package` 声明行
- 引用 Controller 的其他文件的 `import` 语句行
- 目录结构（创建 custom/common 及其二级子目录、移动文件、删除空目录）
- Spring 配置中的 `@ComponentScan` basePackages（如因包路径变更需要）
- Spring XML 配置中的 `component-scan` base-package（如因包路径变更需要）

## 不允许修改的范围

- 任何 Java 方法体
- 任何 `@RequestMapping` / `@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping` 等注解的路径值
- 任何类/接口/枚举的定义结构
- 任何 Service / DAO / Model 层的代码和结构
- 任何 XML 配置文件的业务内容
- 任何前端代码
- 任何 pom.xml
- 非 Controller 类的位置（无 @Controller/@RestController 注解的类不迁移）
