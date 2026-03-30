# S5 修复规范

## 修复执行总原则

1. **严格按顺序执行**：修复规范一 → 修复规范二 → 修复规范三
2. **每个文件处理完毕后立即验证**：Grep 确认无残留旧引用
3. **幂等性保证**：如果目标状态已达成，跳过该修复项
4. **所有修改的代码块添加 AI 代码标记**

---

## 修复规范一：接口路径调整（DELETE/PUT 兼容增强）

**此项仅修改 `@DeleteMapping` 和 `@PutMapping` 注解，不修改 URL 路径。**

### 转换规则总表

| 原注解形式 | 目标注解形式 |
|-----------|------------|
| `@DeleteMapping("/path")` | `@RequestMapping(value = "/path", method = {RequestMethod.DELETE, RequestMethod.POST})` |
| `@DeleteMapping(value = "/path")` | `@RequestMapping(value = "/path", method = {RequestMethod.DELETE, RequestMethod.POST})` |
| `@DeleteMapping(value = "/path", produces = "...")` | `@RequestMapping(value = "/path", method = {RequestMethod.DELETE, RequestMethod.POST}, produces = "...")` |
| `@DeleteMapping(value = "/path", consumes = "...", produces = "...")` | `@RequestMapping(value = "/path", method = {RequestMethod.DELETE, RequestMethod.POST}, consumes = "...", produces = "...")` |
| `@DeleteMapping` （无路径） | `@RequestMapping(method = {RequestMethod.DELETE, RequestMethod.POST})` |
| `@PutMapping("/path")` | `@RequestMapping(value = "/path", method = {RequestMethod.PUT, RequestMethod.POST})` |
| `@PutMapping(value = "/path")` | `@RequestMapping(value = "/path", method = {RequestMethod.PUT, RequestMethod.POST})` |
| `@PutMapping(value = "/path", produces = "...")` | `@RequestMapping(value = "/path", method = {RequestMethod.PUT, RequestMethod.POST}, produces = "...")` |
| `@PutMapping(value = "/path", consumes = "...", produces = "...")` | `@RequestMapping(value = "/path", method = {RequestMethod.PUT, RequestMethod.POST}, consumes = "...", produces = "...")` |
| `@PutMapping` （无路径） | `@RequestMapping(method = {RequestMethod.PUT, RequestMethod.POST})` |

### 转换算法（确定性步骤）

对每个匹配的注解，按以下精确步骤操作：

**Step 1：识别原注解类型**
```
IF 注解为 @DeleteMapping → 原HTTP方法 = RequestMethod.DELETE
IF 注解为 @PutMapping → 原HTTP方法 = RequestMethod.PUT
```

**Step 2：提取原注解所有属性**
```
解析注解括号内的所有属性键值对：
- value / 默认值 → 保留作为 value
- produces → 保留
- consumes → 保留
- headers → 保留
- params → 保留
- name → 保留
```

**Step 3：组装新注解**
```
@RequestMapping(
    value = "{原path值}",
    method = {原HTTP方法, RequestMethod.POST}
    [, produces = "{原produces值}"]
    [, consumes = "{原consumes值}"]
    [, headers = "{原headers值}"]
    [, params = "{原params值}"]
)
```

**Step 4：属性排列顺序（确定性）**
新注解属性始终按以下顺序排列：
1. `value`
2. `method`
3. `produces`（如有）
4. `consumes`（如有）
5. `headers`（如有）
6. `params`（如有）

### import 管理规则

**新增 import**（如果文件中不存在）：
```java
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
```

**删除 import**（如果文件中不再使用）：
```java
import org.springframework.web.bind.annotation.DeleteMapping;  // 当文件中无 @DeleteMapping 时删除
import org.springframework.web.bind.annotation.PutMapping;      // 当文件中无 @PutMapping 时删除
```

**import 删除判定**：修改完成后，使用 Grep 搜索整个文件，如果不再存在任何 `@DeleteMapping` 或 `@PutMapping` 使用，则删除对应 import。

**import 位置规则**：新增的 import 按字母顺序插入到现有 `org.springframework.web.bind.annotation` 包的 import 区域中。

### 约束

- **不修改** 任何接口 URL 路径
- **不修改** `@GetMapping`、`@PostMapping`、`@RequestMapping(method = POST/GET)` 的 HTTP 方法
- 仅对 `@DeleteMapping` 和 `@PutMapping` 做 POST 兼容增强
- 如果原注解已经是 `@RequestMapping(method = {DELETE, POST})` 形式 → **跳过**（幂等性）

### 边界场景

| 场景 | 处理方式 |
|------|---------|
| `@DeleteMapping({"/path1", "/path2"})` 多路径 | 保留多路径：`@RequestMapping(value = {"/path1", "/path2"}, method = {RequestMethod.DELETE, RequestMethod.POST})` |
| 注解跨多行书写 | 整体替换，新注解格式化为单行（属性少于 3 个时）或多行（属性 ≥ 3 个时） |
| 已存在 `@RequestMapping(method = {DELETE, POST})` | 跳过，不重复处理 |
| `@PatchMapping` | 不处理（约束限制项），仅在报告中标注 |

---

## 修复规范二：类命名修正

### 操作步骤（严格顺序）

对每个需要修正的类，按以下 6 步操作：

**Step 1：确认修正目标**
```
旧类名 → 新类名
确认新类名不会造成新的 Bean 冲突（在全局 Bean 名称列表中检查新名称首字母小写后的值）
```

**Step 2：修改类定义**
```java
// 旧：public class XxxCtrl {
// 新：public class XxxController {
```

**Step 3：修改文件名**
- 使用 Bash `mv` 命令重命名文件（保留原文件编码）
- `XxxCtrl.java` → `XxxController.java`
- **禁止** 使用 Read → Write 方式重建文件（会丢失编码）

**Step 4：更新所有 import 引用**
```
使用 Grep 搜索整个工程目录：
  pattern: "import.*\.旧类名;"
  或: "import.*\.旧类名$"（行尾匹配）
逐个文件修改 import 语句中的旧类名为新类名
```

**Step 5：更新所有使用引用**
```
使用 Grep 搜索整个工程目录：
  pattern: "旧类名"（作为独立标识符出现）
需要更新的引用类型：
  - 字段类型声明：private XxxCtrl xxx;
  - 方法参数类型：public void method(XxxCtrl param)
  - 方法返回类型：public XxxCtrl getXxx()
  - 局部变量类型：XxxCtrl xxx = new XxxCtrl();
  - 类型转换：(XxxCtrl) obj
  - 泛型参数：List<XxxCtrl>
  - 注解参数：@Qualifier("xxxCtrl")
```

**Step 6：更新注入点变量名**

注入点变量名跟随类名变化，规则如下：

| 注入方式 | 旧代码 | 新代码 |
|---------|--------|--------|
| `@Autowired` 字段注入 | `@Autowired private XxxCtrl xxxCtrl;` | `@Autowired private XxxController xxxController;` |
| `@Resource` 字段注入 | `@Resource private XxxCtrl xxxCtrl;` | `@Resource private XxxController xxxController;` |
| `@Resource(name="xxxCtrl")` | `@Resource(name="xxxCtrl") private XxxCtrl xxxCtrl;` | `@Resource(name="xxxController") private XxxController xxxController;` |
| 构造器注入 | `public MyService(XxxCtrl xxxCtrl)` | `public MyService(XxxController xxxController)` |
| `@Qualifier` | `@Qualifier("xxxCtrl")` | `@Qualifier("xxxController")` |
| setter 注入 | `public void setXxxCtrl(XxxCtrl xxxCtrl)` | `public void setXxxController(XxxController xxxController)` |

**变量名转换规则**（确定性）：
```
新变量名 = 新类名首字母小写
例：XxxController → xxxController
特殊：如果新类名前两个字母都大写（如 URLParser），则不转换首字母
```

**变量名更新范围**：
- 使用 Grep 搜索所有包含旧变量名的文件
- 只更新**与旧类名对应的变量名**（变量名 = 旧类名首字母小写）
- 如果变量名与旧类名不对应（如 `private XxxCtrl myCtrl;`），**只修改类型名，不修改变量名**

### import 管理规则

- 修改类包路径中的类名部分
- 如果类在同一包内引用，无 import 语句，需检查同包其他文件的直接引用
- Grep 搜索范围：整个工程的 `src/main/java/**/*.java`

### 后缀修正对照表

| 旧后缀/模式 | 新后缀 | 适用层 |
|------------|--------|--------|
| `Ctrl` | `Controller` | Controller 层 |
| `Action` | `Controller` | Controller 层 |
| `Svc` | `Service` | Service 层 |
| `Repo` | `Dao` | DAO 层 |
| `Mgr` | `Manager` 或 `Service` | 视上下文 |

### 大驼峰修正规则

| 旧命名 | 新命名 | 说明 |
|--------|--------|------|
| `elementController` | `ElementController` | 首字母大写 |
| `ELEMENT_CONTROLLER` | 不修改 | 全大写为常量命名，非类命名问题 |

### 验证步骤

修正完成后：
1. `Grep "旧类名" --include="*.java"` → 必须返回 0 结果
2. `Grep "import.*旧类名"` → 必须返回 0 结果
3. 检查新文件名与新类名一致

---

## 修复规范三：Bean 命名冲突处理

### 确定性处理决策树

```
IF 存在 XxxController 和 XxxController2 在同一模块:
  → 重命名 XxxController2 为有意义名称
  → 新名称规则：根据类的功能/业务领域命名，如 XxxExtController / XxxBatchController
  → 新名称选择：从类中 @RequestMapping 的路径提取二级路径作为前缀
  
IF 存在同名 Controller 在不同模块:
  → 不重命名类
  → 给其中一个添加 @Controller("模块名XxxController")
  → 模块名 = 当前模块的 artifactId 去掉 "grp-" 前缀的首段
  
IF 只有 XxxController2 无 XxxController:
  → 去掉 "2" 后缀
  → 重命名为 XxxController
  → 需先确认无冲突
  
IF 类上有明确注释说明命名原因:
  → 标记为约束限制
  → 不修改
```

### 处理策略表

| 场景 | 确定性处理方式 | 需要更新的内容 |
|------|-------------|-------------|
| 同模块同名 Bean | 重命名冲突方（"2"后缀的那个） | 类名、文件名、import、注入点 |
| 跨模块同名 Bean | 添加 `@Controller("前缀XxxController")` | 仅注解参数 |
| 孤立 "2" 后缀 | 去掉后缀 | 类名、文件名、import、注入点 |
| 自定义 Bean 名冲突 | 修改自定义名称为更具体名称 | 仅注解参数 |

### 前缀名称生成规则（确定性）

当需要为 Bean 指定前缀名时：
```
1. 获取当前模块的 pom.xml 中的 <artifactId>
2. 去掉 "grp-" 前缀（如 grp-pt-element → pt-element）
3. 转换为驼峰：pt-element → ptElement
4. 拼接：ptElementXxxController
```

### import 管理规则

- 添加 `@Controller("name")` 时，确认 `import org.springframework.stereotype.Controller` 已存在
- 如果类上原来使用 `@RestController`，改为 `@RestController` + `@Controller("name")` 是不合法的
  - 正确方式：`@RestController` 替换为 `@Controller("name")` + `@ResponseBody`
  - 或保持 `@RestController` 但在 Spring 容器层面处理冲突

---

## 执行操作规范

### 处理顺序（严格）

1. **修复规范一（接口路径兼容）**：先处理所有 `@DeleteMapping` → 再处理所有 `@PutMapping`
2. **修复规范二（类命名修正）**：先后缀修正 → 再大驼峰修正 → 每修改一个类立即更新所有引用
3. **修复规范三（Bean 冲突处理）**：按冲突严重程度排序（FAIL → WARN）

### 文件处理顺序（确定性）

在同一批次内多个文件需要处理时，按以下顺序：
1. 按模块名字母顺序
2. 模块内按文件路径字母顺序
3. 同一文件内按行号从上到下

### 每个文件的操作流程

```
1. Read 读取完整文件内容
2. 执行修改（Edit 工具）
3. Grep 验证旧内容已清除
4. Grep 搜索所有引用方
5. 逐一更新引用方
6. 再次 Grep 验证无残留引用
```

### 冲突预防

- 修改类名前，先 Grep 全工程检查新类名是否已存在
- 如果新类名已存在 → 报告冲突，由用户决定
- 修改文件名前，先检查目标文件名是否已存在

> 命名规范速查表见 → [templates/naming-convention.md](../templates/naming-convention.md)
