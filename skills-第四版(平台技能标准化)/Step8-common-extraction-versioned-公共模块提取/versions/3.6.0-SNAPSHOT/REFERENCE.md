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

| 编号 | 检查项 | 严重级 | 说明 |
|------|--------|--------|------|
| S8-01 | util/ 包归属检查 | 规范 | 工具类是否仍在业务模块内 |
| S8-02 | cache/ 包归属检查 | 规范 | 缓存类是否仍在业务模块内 |
| S8-03 | constant/ 包归属检查 | 规范 | 常量类是否仍在业务模块内 |
| S8-04 | enums/ 包归属检查 | 规范 | 枚举类是否仍在业务模块内 |
| S8-05 | exception/ 包归属检查 | 规范 | 异常类是否仍在业务模块内 |
| S8-06 | config/ 包归属检查 | 规范 | 配置类是否仍在业务模块内 |

---

## 功能一：只读检查流程

### Phase 1: 扫描来源模块

1. 确定能力层模块路径（如 `grp-capability-element/element-service/`）
2. 扫描 `src/main/java/` 下的 6 类目标包（util/cache/constant/enums/exception/config）
3. 统计每个包下的文件数量和类列表

### Phase 2: 检查目标模块

1. 确定 common 模块路径（如 `grp-common-element/`）
2. 检查是否存在 `src/main/java/` 目录
3. 检查 common 模块的 `pom.xml` 是否存在

### Phase 3: 依赖分析

1. 对每个待提取类，分析其被引用情况（grep import 语句）
2. 标记哪些类被多个模块引用（优先提取）
3. 标记哪些类仅被当前模块引用（需评估是否提取）
4. 标记存在循环依赖风险的类（需特殊处理）

### Phase 4: 生成检查报告

按 [examples/check-report.md](examples/check-report.md) 格式输出报告，包含：
- 各包文件统计
- 每个文件的引用分析
- 提取建议（推荐提取 / 建议保留 / 需人工判断）

---

## 功能二：检查 + 迁移流程

### Phase 1-4: 同功能一

先执行完整的只读检查，生成报告。

### Phase 5: 确认迁移清单

1. 向用户展示检查报告
2. 用户确认哪些文件需要迁移
3. 排除标记为"建议保留"的文件（除非用户明确要求）

### Phase 6: 准备 common 模块

1. 如果 common 模块的 `src/main/java/` 目录不存在，创建之
2. 创建与来源模块对应的 package 目录结构
3. 如果 common 模块的 `pom.xml` 缺少必要依赖，提示用户添加

### Phase 7: 逐文件迁移

对每个确认迁移的文件，执行标准 7 步流程：

参见 [scripts/refactor-rules.md](scripts/refactor-rules.md)

```
Step 1: Read    - 读取来源文件完整内容
Step 2: Modify  - 修改 package 声明（如果 package 路径变化）
Step 3: Write   - 将文件写入 common 模块的对应目录
Step 4: Grep    - 搜索所有引用该类的 import 语句
Step 5: Edit    - 更新 import 路径（如果 package 变化），或无需修改（package 不变时）
Step 6: Delete  - 删除来源模块中的原文件
Step 7: Verify  - 验证编译无误（检查 import 引用完整性）
```

### Phase 8: POM 依赖调整

1. 在来源模块的 `pom.xml` 中添加对 common 模块的依赖（如果不存在）
2. 确保 common 模块被正确引入父 POM 的 `<modules>` 中
3. 检查 common 模块的 `pom.xml` 中是否需要额外依赖（如来源文件依赖的第三方库）

### Phase 9: 最终验证

1. 检查所有 import 语句是否正确
2. 确认无编译错误
3. 输出迁移完成报告

---

## 安全约束

详见 [scripts/safety-constraints.md](scripts/safety-constraints.md)

### 核心原则

- **C-01**: 不修改任何 Java 类的业务逻辑代码（方法体内容不变）
- **C-02**: package 声明与实际目录路径必须一致
- **C-03**: 每次迁移一个文件，迁移后立即验证编译正确性
- **C-04**: 所有操作可逆（保留原文件备份直到确认无误）

### 安全红线

- **S-01**: 禁止移动被 Spring `@Bean`/`@Component`/`@Configuration` 注解标注且在配置中通过 Bean 名称引用的类，除非同步更新所有引用
- **S-02**: 禁止移动包含 `@Autowired`/`@Resource` 注入本模块 Service 的类（存在跨模块依赖风险）
- **S-03**: 禁止修改类名（仅移动，不改名）
- **S-04**: 配置类（config/）迁移需特别注意 `@ComponentScan` 扫描路径是否覆盖

---

## 文件索引

| 文件路径 | 用途 |
|----------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | 6项检查规则详细定义 |
| [scripts/refactor-rules.md](scripts/refactor-rules.md) | 迁移执行规范（7步标准流程） |
| [scripts/safety-constraints.md](scripts/safety-constraints.md) | 安全约束与红线 |
| [templates/standard-directory.md](templates/standard-directory.md) | common模块标准目录结构 |
| [examples/check-report.md](examples/check-report.md) | 检查报告示例 |
| [examples/migration-flow.md](examples/migration-flow.md) | 迁移流程示例 |
