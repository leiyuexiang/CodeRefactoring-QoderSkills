# Step8 公共模块提取 - 迁移执行规范

## 迁移映射总表

### 来源模块 → 目标模块路径映射

以 element 模块为例（其他模块按此模式替换 `{module}` 占位符）：

| 文件类型 | 来源路径 | 目标路径 | Package 变化 |
|----------|---------|---------|-------------|
| util 工具类 | `grp-capability-element/element-service/src/main/java/grp/pt/util/XxxUtil.java` | `grp-common-element/src/main/java/grp/pt/util/XxxUtil.java` | 无变化 |
| cache 缓存类 | `grp-capability-element/element-service/src/main/java/grp/pt/cache/XxxCache.java` | `grp-common-element/src/main/java/grp/pt/cache/XxxCache.java` | 无变化 |
| constant 常量类 | `grp-capability-element/element-service/src/main/java/grp/pt/constant/XxxConstant.java` | `grp-common-element/src/main/java/grp/pt/constant/XxxConstant.java` | 无变化 |
| enums 枚举类 | `grp-capability-element/element-service/src/main/java/grp/pt/enums/XxxEnum.java` | `grp-common-element/src/main/java/grp/pt/enums/XxxEnum.java` | 无变化 |
| exception 异常类 | `grp-capability-element/element-service/src/main/java/grp/pt/exception/XxxException.java` | `grp-common-element/src/main/java/grp/pt/exception/XxxException.java` | 无变化 |
| config 配置类 | `grp-capability-element/element-service/src/main/java/grp/pt/config/XxxConfig.java` | `grp-common-element/src/main/java/grp/pt/config/XxxConfig.java` | 无变化 |

**关键结论**: 由于来源模块和 common 模块使用相同的 Java package 前缀（`grp.pt`），**package 声明通常不需要修改，import 语句也不需要更新**。这是 Step8 迁移的核心特点——文件只需物理移动（模块间），逻辑引用（package/import）保持不变。

### 通用路径映射公式

```
来源: grp-capability-{module}/{sub-module}/src/main/java/{base-package}/{type}/{ClassName}.java
目标: grp-common-{module}/src/main/java/{base-package}/{type}/{ClassName}.java

其中:
  {module}       = 业务模块名（如 element、user、order）
  {sub-module}   = 能力层子模块名（如 element-service、element-controller）
  {base-package} = Java package 基础路径（如 grp/pt）
  {type}         = 公共包类型（util|cache|constant|enums|exception|config）
  {ClassName}    = Java 类名（保持不变）
```

### 多来源模块处理

当同一个 `{type}` 包在多个子模块中都存在时（如 `element-service` 和 `element-controller` 都有 `util/`）：

| 来源模块 | 来源路径 | 目标路径 |
|----------|---------|---------|
| element-service | `element-service/src/main/java/grp/pt/util/XxxUtil.java` | `grp-common-element/src/main/java/grp/pt/util/XxxUtil.java` |
| element-controller | `element-controller/src/main/java/grp/pt/util/YyyUtil.java` | `grp-common-element/src/main/java/grp/pt/util/YyyUtil.java` |

**注意**: 如果两个模块中存在同名文件（如都有 `DateUtil.java`），属于文件名冲突，执行安全红线 S-06 停止迁移该文件。

---

## 迁移前预检清单

在开始迁移之前，必须完成以下预检（任一项 FAIL 则中止）：

| 序号 | 预检项 | 检查方法 | 通过条件 |
|------|--------|---------|---------|
| P-01 | common 模块目录存在 | `Glob: grp-common-{module}/pom.xml` | 文件存在 |
| P-02 | 父 POM 包含 common 模块 | `Grep: <module>grp-common-{module}</module>` in 父 pom.xml | 有匹配 |
| P-03 | 检查报告已生成 | check-rules.md 的检查流程已执行 | 有 EXTRACT/EVALUATE/RETAIN 分类结果 |
| P-04 | 用户已确认迁移清单 | 用户已查看报告并确认 | 用户明确确认 |

---

## 标准 7 步迁移流程

对每个确认迁移的文件，**严格按以下步骤逐一执行**，不可跳步、不可批量：

### Step 1: Read（读取来源文件）

```
操作: 使用 Read 工具读取来源文件完整内容
路径: grp-capability-{module}/{sub-module}/src/main/java/{base-package}/{type}/{ClassName}.java

目的: 
  1. 获取文件完整内容（用于后续验证）
  2. 确认文件确实存在
  3. 记录文件的 package 声明行
```

### Step 2: Analyze（分析 package 声明）

检查 package 声明是否需要修改：

```
读取文件第一行有效代码（非注释行），提取 package 声明:
  package grp.pt.{type};

对比目标路径的 package:
  grp-common-{module}/src/main/java/grp/pt/{type}/ → package grp.pt.{type};

判定:
  来源 package == 目标 package → 无需修改（绝大多数情况）
  来源 package != 目标 package → 需修改 package 声明
```

**预期结果**: 在标准四层架构中，来源和目标的 package 路径相同，**无需修改 package 声明**。

### Step 3: Copy（复制文件到目标模块）

```
操作: 使用 Bash 的 copy/cp 命令将文件复制到目标位置
命令: copy "来源路径" "目标路径"  (Windows)
      cp "来源路径" "目标路径"    (Linux/Mac)

目标路径: grp-common-{module}/src/main/java/{base-package}/{type}/{ClassName}.java

前置操作: 如果目标目录不存在，先创建:
  mkdir -p grp-common-{module}/src/main/java/{base-package}/{type}/  (Linux/Mac)
  mkdir "grp-common-{module}\src\main\java\{base-package}\{type}"   (Windows，注意反斜杠)
```

**编码保留（强制要求）**:
- **必须使用 Bash 的 `copy`/`cp` 命令复制文件**，不使用 Read → Write 方式重建文件
- 原因：copy/cp 命令保留原文件的字节流（包括 BOM 标记、编码格式）
- 如果 package 声明需要修改，**复制后再用 Edit 工具修改 package 行**（Edit 工具只替换指定内容，不影响文件其他部分的编码）

### Step 4: Grep（搜索引用）

```
操作: 在整个工程范围内搜索所有引用该类的 import 语句
Grep 模式: import {full.package.name}.{ClassName};
Grep 范围: 所有 .java 文件（排除目标文件本身）
Grep 类型: type: "java"

示例:
  搜索 import grp.pt.util.DateUtil;
  搜索 import grp.pt.constant.ElementConstant;
```

**结果记录**: 记录所有匹配文件的路径和行号，用于 Step 5 判断。

### Step 5: UpdateImport（更新引用）

根据 Step 2 的分析结果：

- **如果 package 路径未变**（绝大多数情况）→ **无需修改任何 import 语句**，跳过此步骤
- **如果 package 路径改变** → 使用 Edit 工具批量更新所有引用文件的 import 语句：
  ```
  old_string: import {旧package}.{ClassName};
  new_string: import {新package}.{ClassName};
  replace_all: true
  ```

### Step 6: Delete（删除来源文件）

```
操作: 使用 DeleteFile 工具删除来源模块中的原文件
路径: grp-capability-{module}/{sub-module}/src/main/java/{base-package}/{type}/{ClassName}.java

后续: 
  - 如果该 {type} 目录下已无其他 .java 文件，使用 Bash 删除空目录
  - 检查方法: Glob 搜索 {type}/*.java，无结果则删除目录
```

### Step 7: Verify（验证）

逐项验证以下 3 个条件，全部通过才继续下一个文件：

| 验证项 | 验证方法 | 通过条件 |
|--------|---------|---------|
| V-01 | Read 目标文件，检查 package 声明 | package 声明与目标目录路径一致 |
| V-02 | Grep 搜索 `import.*{ClassName}` | 所有引用文件的 import 路径正确（指向可达的 package） |
| V-03 | 确认来源文件已删除 | Read 来源路径返回文件不存在 |

**验证失败处理**:
- V-01 失败 → 使用 Edit 修正 package 声明
- V-02 失败 → 使用 Edit 修正 import 语句
- V-03 失败 → 使用 DeleteFile 再次删除

---

## 迁移顺序（强制）

按依赖关系从底层到上层，**严格按以下顺序迁移**：

```
迁移顺序:
  1. constant/   → 常量类无外部依赖，最先迁移
  2. enums/      → 枚举类通常无外部依赖，可能引用 constant
  3. exception/  → 异常类通常无外部依赖
  4. util/       → 工具类可能依赖 constant/enums
  5. cache/      → 缓存类可能依赖 util/constant
  6. config/     → 配置类可能依赖以上所有，最后迁移
```

**顺序理由**: 先迁移被依赖的类（constant/enums），再迁移依赖方（util/cache/config），避免出现中间态的编译错误。

---

## POM 依赖处理规范

### 时机

POM 依赖调整在**所有文件迁移完成后**统一执行（不在每个文件迁移后单独执行）。

### Step A: 来源模块添加 common 依赖

在来源模块（如 `element-service`）的 `pom.xml` 中添加：

```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>grp-common-{module}</artifactId>
    <version>${project.version}</version>
</dependency>
```

**检查方法**: 先 Grep 搜索 `grp-common-{module}` in `element-service/pom.xml`，如已存在则跳过。

**注意**: 如果来源模块使用 `${revision}` 作为版本号：
```xml
<dependency>
    <groupId>${project.groupId}</groupId>
    <artifactId>grp-common-{module}</artifactId>
    <version>${revision}</version>
</dependency>
```

### Step B: Controller 模块添加 common 依赖

如果 Controller 模块（如 `element-controller`）也引用了被迁移的类，同样需要添加 common 依赖。

**检查方法**: Grep 搜索 `import grp.pt.(util|cache|constant|enums|exception|config)` in `element-controller/src/`，有匹配则添加依赖。

### Step C: common 模块 POM 检查

检查 common 模块的 `pom.xml` 中是否包含被迁移文件所需的第三方库。

**检查方法**: 对每个已迁移的文件，提取其 import 语句中的第三方库：

| 文件中的 import | 需要的依赖 | 检查 common pom.xml |
|----------------|-----------|-------------------|
| `import org.apache.commons.lang3.*` | `commons-lang3` | Grep `commons-lang3` in pom.xml |
| `import com.fasterxml.jackson.*` | `jackson-databind` | Grep `jackson-databind` in pom.xml |
| `import org.springframework.data.redis.*` | `spring-data-redis` | Grep `spring-data-redis` in pom.xml |
| `import com.google.common.*` | `guava` | Grep `guava` in pom.xml |
| `import cn.hutool.*` | `hutool` | Grep `hutool` in pom.xml |
| `import lombok.*` | `lombok` | Grep `lombok` in pom.xml |

**原则**: common 模块应尽量轻量，只添加被迁移文件实际依赖的库。

### Step D: 父 POM 检查

确认 `grp-common-{module}` 已在父 POM 的 `<modules>` 中声明。

**检查方法**: Grep 搜索 `<module>grp-common-{module}</module>` in 父 `pom.xml`。

---

## 特殊情况处理

### 情况一：文件名冲突

**触发条件**: common 模块中已存在同名同路径文件

**检查方法**: `Glob: grp-common-{module}/src/main/java/{base-package}/{type}/{ClassName}.java`

**处理方式**:
1. **停止迁移该文件**
2. 在报告中标记冲突：`[冲突] {ClassName}.java 在 common 模块中已存在`
3. 由用户决定：
   - 合并两个文件内容
   - 保留 common 中的版本，删除来源模块的版本
   - 保留来源模块的版本，覆盖 common 中的版本

### 情况二：深层嵌套的包路径

**触发条件**: 公共代码不在 `grp/pt/{type}/` 直接目录下，而是在更深层（如 `grp/pt/element/util/`）

**处理方式**:
1. 保持原有的包路径结构迁移：
   - 来源: `element-service/src/main/java/grp/pt/element/util/XxxUtil.java`
   - 目标: `grp-common-element/src/main/java/grp/pt/element/util/XxxUtil.java`
2. package 声明不变（`package grp.pt.element.util;`）
3. import 语句不变

### 情况三：文件间的内部依赖

**触发条件**: 待迁移的文件 A 依赖待迁移的文件 B

**处理方式**:
1. 按迁移顺序（constant → enums → exception → util → cache → config）自然解决
2. 如果同一 type 内部有依赖（如 UtilA import UtilB），两者都迁移则无影响
3. 如果 A 被判定为 EXTRACT 但 B 被判定为 RETAIN：
   - 检查 A 对 B 的依赖是否是 import 级别
   - 如果是：A 迁移后仍可通过 Maven 依赖链引用 B（来源模块依赖 common，common 的依赖者也能访问来源模块的类）
   - 如果形成循环依赖：将 A 改为 RETAIN

### 情况四：空目录清理

**触发条件**: 某个 type 目录下的所有文件都已迁移

**处理方式**:
1. 确认目录为空：`Glob: {type}/*.java` 无结果
2. 使用 Bash 删除空目录：`rmdir {type目录路径}`
3. 注意：仅删除目标 type 目录，不删除其父目录

---

## 迁移完成报告模板

所有文件迁移完成后，输出以下报告：

```
## 迁移完成报告

### 迁移统计

| 包名 | 迁移文件数 | 保留文件数 | 冲突文件数 |
|------|-----------|-----------|-----------|
| constant/ | X | Y | Z |
| enums/ | X | Y | Z |
| exception/ | X | Y | Z |
| util/ | X | Y | Z |
| cache/ | X | Y | Z |
| config/ | X | Y | Z |
| **合计** | **XX** | **YY** | **ZZ** |

### POM 变更

| 模块 | 变更内容 |
|------|---------|
| {sub-module}/pom.xml | 添加 grp-common-{module} 依赖 |
| grp-common-{module}/pom.xml | 添加 {第三方库} 依赖 |
| 父 pom.xml | 确认 modules 包含 grp-common-{module} |

### 验证结果

| 验证项 | 状态 |
|--------|------|
| 所有目标文件 package 声明正确 | OK/FAIL |
| 所有 import 引用路径正确 | OK/FAIL |
| 所有来源文件已删除 | OK/FAIL |
| 空目录已清理 | OK/FAIL |
```
