# Step8 公共模块提取 - 基线规则 (3.6.0-SNAPSHOT)

## 版本说明

- **适用版本**: 3.6.0-SNAPSHOT（基线）
- **前置步骤**: Step7（接口与命名规范）已完成
- **后续步骤**: Step9（工程代码优化检查）

## 概述

本规则定义将能力层模块（如 `element-service`、`element-controller`）中的**公共代码包**提取到 `grp-{module}-common` 模块下的检查与迁移规范。

### 提取范围（6 类公共包）

| 序号 | 包名 | 说明 | 典型类 |
|------|------|------|--------|
| 1 | `util/` | 工具类 | XxxUtil.java、XxxHelper.java |
| 2 | `cache/` | 缓存相关 | XxxCache.java、CacheManager.java |
| 3 | `constant/` | 常量定义 | XxxConstant.java、XxxConstants.java |
| 4 | `enums/` | 枚举定义 | XxxEnum.java、XxxType.java |
| 5 | `exception/` | 异常定义 | XxxException.java、BusinessException.java |
| 6 | `config/` | 配置类 | XxxConfig.java、XxxConfiguration.java |

### 来源模块与目标模块映射

以 element 模块为例：
```
来源: grp-capability-element/element-service/src/main/java/grp/pt/{util|cache|constant|enums|exception|config}/
目标: grp-common-element/src/main/java/grp/pt/{util|cache|constant|enums|exception|config}/
```

**关键特点**: 来源和目标使用相同的 Java package 前缀，因此：
- package 声明**通常不需要修改**
- import 语句**通常不需要更新**
- 文件只需物理移动（模块间），逻辑引用保持不变

## 使用场景

| 触发关键词 | 执行功能 |
|-----------|----------|
| "公共模块检查"、"common检查"、"Step8检查" | 功能一：只读检查 |
| "公共模块提取"、"common提取"、"Step8修复" | 功能二：检查 + 迁移 |

## 前置条件

1. **Step7 已完成**: 接口与命名规范治理已执行，类名/方法名已规范化
2. **grp-{module}-common 模块已存在**: Maven 多模块工程中存在 common 子模块
3. **已备份**: 用户已确认备份或在版本控制下

---

## 检查项总览

详细规则参见 [scripts/check-rules.md](scripts/check-rules.md)

| 编号 | 检查项 | 说明 | 判定方法 |
|------|--------|------|---------|
| S8-01 | util/ 包归属检查 | 工具类是否仍在业务模块内 | Grep 分析 Service/DAO/注入依赖 |
| S8-02 | cache/ 包归属检查 | 缓存类是否仍在业务模块内 | Grep 分析 DAO 依赖和注入 |
| S8-03 | constant/ 包归属检查 | 常量类是否仍在业务模块内 | Grep 分析依赖（预期 >95% EXTRACT） |
| S8-04 | enums/ 包归属检查 | 枚举类是否仍在业务模块内 | Grep 分析依赖（预期 >95% EXTRACT） |
| S8-05 | exception/ 包归属检查 | 异常类是否仍在业务模块内 | Grep 区分异常定义类/异常处理器 |
| S8-06 | config/ 包归属检查 | 配置类是否仍在业务模块内 | Grep 分析 @MapperScan/@ComponentScan/注入 |

### 判定结果分类

每个文件被判定为以下三类之一（判定逻辑详见 [scripts/classification-guide.md](scripts/classification-guide.md)）：

| 判定 | 含义 | 后续动作 |
|------|------|---------|
| **EXTRACT** | 推荐提取 | 自动列入迁移清单 |
| **EVALUATE** | 需人工判断 | 列入报告，等用户确认 |
| **RETAIN** | 建议保留 | 自动排除出迁移清单 |

---

## 功能一：只读检查流程

### Phase 1: 扫描来源模块

1. 确定能力层模块路径（如 `grp-capability-element/element-service/`）
2. 对 6 类目标包，使用 Glob 搜索 `.java` 文件：
   ```
   Glob: {module-path}/src/main/java/**/util/*.java
   Glob: {module-path}/src/main/java/**/cache/*.java
   Glob: {module-path}/src/main/java/**/constant/*.java
   Glob: {module-path}/src/main/java/**/constants/*.java  (兼容)
   Glob: {module-path}/src/main/java/**/enums/*.java
   Glob: {module-path}/src/main/java/**/enum/*.java        (兼容)
   Glob: {module-path}/src/main/java/**/exception/*.java
   Glob: {module-path}/src/main/java/**/exceptions/*.java  (兼容)
   Glob: {module-path}/src/main/java/**/config/*.java
   Glob: {module-path}/src/main/java/**/configuration/*.java (兼容)
   ```
3. 统计每个包下的文件数量和类列表

### Phase 2: 检查目标模块

1. 确定 common 模块路径（如 `grp-common-element/`）
2. 检查是否存在 `pom.xml`
3. 检查是否存在 `src/main/java/` 目录
4. 检查父 POM 是否包含该 common 模块

### Phase 3: 依赖分析（基于决策树）

对每个扫描到的文件，按 [scripts/classification-guide.md](scripts/classification-guide.md) 中的全局决策树执行判定：

1. **Gate 1**: 安全红线检查（@MapperScan、文件名冲突）
2. **Gate 2**: 依赖分析（Service/DAO import）
3. **Gate 3**: 注入依赖分析（@Autowired + 通用组件白名单）
4. **Gate 4**: config 类特殊检查（@ComponentScan）

每个文件获得一个确定的判定结果：EXTRACT / EVALUATE / RETAIN

### Phase 4: 生成检查报告

按 [examples/check-report.md](examples/check-report.md) 格式输出报告，包含：
- 各包文件统计（EXTRACT / EVALUATE / RETAIN 数量）
- 每个文件的判定结果和判定路径（Gate/Question 路径）
- 第三方依赖分析（common 模块需添加的依赖）
- 目标模块状态检查

---

## 功能二：检查 + 迁移流程

### Phase 1-4: 同功能一

先执行完整的只读检查，生成报告。

### Phase 5: 确认迁移清单

1. 向用户展示检查报告
2. EXTRACT 文件自动列入迁移清单
3. EVALUATE 文件等待用户逐一确认（是否迁移）
4. RETAIN 文件自动排除（除非用户明确要求迁移）
5. 用户确认最终迁移清单

### Phase 6: 准备 common 模块

1. 如果 common 模块的 `src/main/java/` 目录不存在，创建之
2. 按迁移顺序（constant → enums → exception → util → cache → config）创建目标目录
3. 仅创建有文件迁入的目录，不创建空目录

### Phase 7: 逐文件迁移

按迁移顺序（constant → enums → exception → util → cache → config），对每个确认迁移的文件执行标准 7 步流程：

参见 [scripts/refactor-rules.md](scripts/refactor-rules.md)

```
Step 1: Read     - 读取来源文件完整内容
Step 2: Analyze  - 分析 package 声明是否需修改（通常不需要）
Step 3: Copy     - 使用 Bash copy/cp 命令复制到目标位置（保留编码）
Step 4: Grep     - 搜索所有引用该类的 import 语句
Step 5: Update   - 更新 import 路径（通常不需要，因 package 不变）
Step 6: Delete   - 删除来源模块中的原文件
Step 7: Verify   - 验证 package 声明、import 引用、来源文件已删除
```

**关键约束**:
- 严格按上述顺序逐文件执行，不可批量操作
- 使用 copy/cp 命令复制文件（不使用 Read → Write）
- 每个文件迁移后立即验证

### Phase 8: POM 依赖调整

参见 [scripts/refactor-rules.md](scripts/refactor-rules.md) 的 POM 依赖处理规范：

1. 来源模块 pom.xml 添加 common 模块依赖（如不存在）
2. Controller 模块如有引用也需添加依赖
3. common 模块 pom.xml 添加迁移文件依赖的第三方库
4. 确认父 POM modules 包含 common 模块

### Phase 9: 最终验证

1. 检查所有目标文件的 package 声明正确
2. 检查所有 import 引用路径正确
3. 确认所有来源文件已删除
4. 确认空目录已清理
5. 输出迁移完成报告

---

## 安全约束

详见 [scripts/safety-constraints.md](scripts/safety-constraints.md)

### 核心原则

- **C-01**: 不修改任何 Java 类的业务逻辑代码（方法体内容不变）
- **C-02**: package 声明与实际目录路径必须一致
- **C-03**: 每次迁移一个文件，迁移后立即验证编译正确性
- **C-04**: 所有操作可逆（使用 copy 而非 move，删除在验证通过后执行）
- **C-05**: 严格按迁移顺序执行
- **C-06**: Grep 结果优先于语义理解

### 安全红线

- **S-01**: 禁止移动 Spring Bean 通过名称引用的类（除非同步更新所有引用）
- **S-02**: 禁止移动注入本模块 Service/DAO 的类（通用组件白名单例外）
- **S-03**: 禁止修改类名（仅移动，不改名）
- **S-04**: 配置类迁移需验证 @ComponentScan 覆盖
- **S-05**: 禁止移动 @MapperScan 配置类（最高优先级红线）
- **S-06**: 禁止覆盖 common 中已存在的同名类
- **S-07**: 编码保留（强制使用 copy/cp 命令）
- **S-08**: 仅处理 6 类公共包，不处理 service/controller/dao/entity

---

## 文件索引

| 文件路径 | 用途 |
|----------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | 6 项检查规则详细定义（含 Grep 模式和量化判定标准） |
| [scripts/classification-guide.md](scripts/classification-guide.md) | 文件分类判定指南（全局决策树，确保一致性） |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | 迁移执行规范（7 步标准流程 + 迁移映射表 + POM 处理） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 安全约束与红线（含精确判定逻辑和优先级） |
| [templates/standard-directory.md](templates/standard-directory.md) | common 模块标准目录结构 |
| [examples/check-report.md](examples/check-report.md) | 检查报告示例（含判定路径追踪） |
| [examples/migration-flow.md](examples/migration-flow.md) | 迁移流程示例（含具体 Before/After 代码） |
