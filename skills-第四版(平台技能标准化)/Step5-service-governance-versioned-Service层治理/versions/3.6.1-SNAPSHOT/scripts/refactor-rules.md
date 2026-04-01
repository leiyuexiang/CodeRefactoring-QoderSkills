# S3 修复规范

## 执行确定性约束

### 文件处理顺序（强制）
1. 按模块的字母顺序处理（如 administrator → bpm → cache → ...）
2. 同一模块内，先处理接口（按文件名字母序），再处理实现（按文件名字母序）
3. 禁止并行处理多个文件

### 幂等性要求
同一份源工程，使用同一份规则，必须产出完全相同的结果。
如果发现两次执行结果不同，说明规则存在歧义，必须上报。

### 已存在的非标准目录处理规则
| 已存在目录 | 处理方式 |
|-----------|---------|
| service/serviceImp/ | 忽略，新建 service/impl/，将实现迁入 impl/ |
| service/imp/ | 忽略，新建 service/impl/，将实现迁入 impl/ |
| service/svc/ | 忽略，新建 service/impl/，将实现迁入 impl/ |
| service/facade/ 已存在 | 直接使用 |
| service/impl/ 已存在 | 直接使用 |

**禁止复用任何非标准命名的目录作为迁移目标。**

### 包路径构造公式（强制）

无论源文件位于 `service/` 下的何种嵌套层级，目标路径始终扁平化到 `facade/` 或 `impl/` 一级：

| 文件类型 | 源路径模式 | 目标路径 |
|---------|-----------|---------|
| Service 接口 | `service.{任意中间路径}.IXxxService` | `service.facade.IXxxService` |
| Service 接口 | `service.IXxxService` | `service.facade.IXxxService` |
| Service 实现 | `service.{任意中间路径}.impl.XxxServiceImpl` | `service.impl.XxxServiceImpl` |
| Service 实现 | `service.{任意中间路径}.XxxServiceImpl` | `service.impl.XxxServiceImpl` |

**公式**：`目标package = service的根package + facade（或impl） `，中间路径全部丢弃。

示例：
- `grp.pt.service.basedata.sub.IElementService` → `grp.pt.service.facade.IElementService`
- `grp.pt.service.module.core.impl.ElementServiceImpl` → `grp.pt.service.impl.ElementServiceImpl`

### 同名文件冲突检测（强制，在迁移前执行）

在开始迁移之前，必须执行以下冲突检测：

1. 扫描所有待迁入 `facade/` 的 Service 接口，提取文件名
2. 扫描所有待迁入 `impl/` 的 Service 实现，提取文件名
3. 检查目标目录中是否已存在同名文件
4. 检查待迁移文件列表中是否存在同名文件（来自不同业务子包的同名 Service）

**冲突处理策略**：
- 如果检测到同名文件冲突 → **立即停止**，向用户报告冲突详情
- 报告格式：列出冲突文件的全路径、所属模块、冲突原因
- 由用户决定处理方式（改名/合并/跳过），不得自行决策

### 通配符 import 处理（强制）

迁移后必须检测并处理通配符 import：

1. Grep 搜索 `import.*service\.{旧子包名}\.\*` 模式（如 `import grp.pt.service.basedata.*`）
2. 如果发现通配符 import：
   - 如果该通配符 import 的包下所有 Service 文件都已迁走：
     - 将通配符 import 替换为具体的类 import（按迁移后的新路径）
     - 保留对原包下仍存在的非 Service 类的通配符 import（如果有的话）
   - 如果该通配符 import 的包下仍有非 Service 文件：
     - 保留原通配符 import
     - 增加新路径的具体 import（如 `import grp.pt.service.facade.IXxxService;`）
3. Grep 搜索 `import.*service\.facade\.\*` 和 `import.*service\.impl\.\*`，确认无误

### 跨模块 import 搜索范围（强制）

import 更新的搜索范围必须覆盖以下所有位置：

1. **当前模块**：`当前模块/src/main/java/` 下所有 `.java` 文件
2. **依赖当前模块的其他模块**：如果 Service 接口被其他模块引用（通过 Maven 依赖），必须搜索所有依赖模块
3. **搜索策略**：以工程根目录为起点，Grep 搜索旧 import 路径，覆盖所有 `src/main/java/` 目录
4. **不搜索范围**：`src/test/java/` 下的测试代码暂不处理，但需在报告中标注

### Spring Bean 引用处理

迁移后检查以下 Spring Bean 引用是否需要更新：

1. `@Qualifier("beanName")` — package 变更通常不影响默认 Bean 名称，无需修改
2. `@Resource(name="beanName")` — 同上，默认按类名首字母小写，无需修改
3. `@Resource(type=XxxServiceImpl.class)` — 如果使用全限定类名，需搜索并更新
4. Spring XML 配置中的 `class="xxx.service.{旧路径}.XxxServiceImpl"` — 需搜索并更新
5. `@ComponentScan` 中的 `basePackages` — 如果指定了具体的旧子包路径，需更新

**搜索方式**：
- Grep 搜索全工程中的旧全限定类名（如 `grp.pt.service.basedata.impl.ElementServiceImpl`）
- 搜索范围包括 `.java`、`.xml`、`.yml`、`.yaml`、`.properties` 文件

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
