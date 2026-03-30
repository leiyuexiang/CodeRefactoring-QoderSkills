# Step8 公共模块提取 - 迁移执行规范

## 标准 7 步迁移流程

对每个确认迁移的文件，严格按以下步骤执行：

### Step 1: Read（读取来源文件）

```
读取来源文件完整内容
来源路径: grp-capability-{module}/{sub-module}/src/main/java/{package}/{type}/{ClassName}.java
```

### Step 2: Modify（修改 package 声明）

检查 package 声明是否需要修改：
- **如果来源和目标的 package 路径相同**（如都是 `grp.pt.util`）→ 无需修改
- **如果 package 路径不同** → 修改 `package` 声明为目标路径

```java
// 修改前（来源模块）
package grp.pt.util;

// 修改后（目标模块，如果 package 变化）
package grp.pt.util;  // 通常不变，因为 common 模块保持相同的 package 结构
```

### Step 3: Write（写入目标模块）

```
将文件写入 common 模块对应目录
目标路径: grp-common-{module}/src/main/java/{package}/{type}/{ClassName}.java
```

**编码保留（强制）**：
- 必须保持原文件的字符编码格式（如 UTF-8、UTF-8 with BOM、GBK 等）
- 原文件为 UTF-8 with BOM（首3字节为 EF BB BF）时，新文件必须保留 BOM
- **推荐方式**：先通过 Bash 的 `cp`/`copy` 命令复制文件到目标位置（保留编码），再用 Edit 修改 package 声明行
- **备选方式**：使用 Write 工具写入内容时注意编码一致性，但需注意 Write 工具可能丢失 BOM

### Step 4: Grep（搜索引用）

```
在整个工程范围内搜索所有引用该类的 import 语句
搜索模式: import {full.package.name}.{ClassName};
搜索范围: 所有 .java 文件
```

### Step 5: Edit（更新引用）

- **如果 package 路径未变** → 无需修改 import（常见情况）
- **如果 package 路径改变** → 批量更新所有引用文件的 import 语句

### Step 6: Delete（删除来源文件）

```
删除来源模块中的原文件
删除路径: grp-capability-{module}/{sub-module}/src/main/java/{package}/{type}/{ClassName}.java
```

注意：如果整个包目录已清空，同时删除空目录

### Step 7: Verify（验证）

1. 检查目标文件 package 声明与目录路径一致
2. 检查所有引用文件的 import 语句正确
3. 确认无编译错误（如有条件，执行 `mvn compile`）

---

## POM 依赖处理规范

### 来源模块添加 common 依赖

在来源模块（如 `element-service`）的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>grp-common-{module}</artifactId>
    <version>${project.version}</version>
</dependency>
```

### common 模块 POM 检查

检查 common 模块的 `pom.xml` 中是否包含被迁移文件所依赖的第三方库：
- 如果工具类使用了 `commons-lang3` → common 模块需添加此依赖
- 如果缓存类使用了 `spring-data-redis` → common 模块需添加此依赖
- 原则：common 模块应尽量轻量，只添加必要的依赖

### 父 POM 检查

确认 `grp-common-{module}` 已在父 POM 的 `<modules>` 中声明。

---

## 迁移顺序建议

按依赖关系从底层到上层迁移：

1. **constant/** → 常量类无依赖，最先迁移
2. **enums/** → 枚举类通常无依赖
3. **exception/** → 异常类通常无依赖
4. **util/** → 工具类可能依赖 constant/enums
5. **cache/** → 缓存类可能依赖 util/constant
6. **config/** → 配置类可能依赖以上所有，最后迁移

---

## 特殊情况处理

### 情况一：工具类依赖 DAO 层

如果 util 类中 `@Autowired` 了 DAO/Mapper：
- **不迁移此类**，标记为"建议保留"
- 或者将 DAO 依赖部分抽取为参数传入，再迁移纯工具方法

### 情况二：config 类包含 @ComponentScan

如果 config 类使用了 `@ComponentScan(basePackages = "grp.pt.xxx")`：
- 迁移后需确认扫描路径仍然有效
- 通常 common 模块的 config 会被主启动类扫描到（因为 package 前缀相同）

### 情况三：文件名冲突

如果 common 模块中已存在同名文件：
- 停止迁移该文件
- 在报告中标记冲突，由用户决定如何合并
