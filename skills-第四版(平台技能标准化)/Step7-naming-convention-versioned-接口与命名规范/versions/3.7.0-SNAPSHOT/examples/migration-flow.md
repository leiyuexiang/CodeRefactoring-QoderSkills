# 命名修正标准流程与操作示例

## 标准修正步骤

每个类命名修正遵循以下 6 步流程（严格顺序）：

```
1. 幂等检查 → 目标状态是否已达成？已达成则跳过
2. 冲突预检 → 新名称是否已存在？存在则报告等待用户决策
3. 修改类定义 → 更新类名声明
4. 修改文件名 → 使用 mv 命令（保留编码），文件名与类名保持一致
5. 更新引用 → Grep 搜索所有 import 和使用位置，逐一修改（含注入点变量名）
6. 验证 → Grep 确认无残留旧类名引用（必须返回 0 结果）
```

---

## 操作示例一：类名后缀修正

### 修正目标

```
旧类名：ElementCtrl
新类名：ElementController
旧文件名：ElementCtrl.java
新文件名：ElementController.java
```

### Step 1：幂等检查

```
检查类名是否已以 Controller 结尾：
  "ElementCtrl" 以 "Ctrl" 结尾 → 需要修正
```

### Step 2：冲突预检

```
Grep 搜索 "class ElementController" --include="*.java"
  → 0 结果 → 无冲突，可继续

Grep 搜索 "elementController" 在注入点中作为 Bean 名
  → 检查是否存在其他 Bean 使用此名称
```

### Step 3：修改类定义

```java
// 旧：public class ElementCtrl {
// 新：public class ElementController {
```

### Step 4：修改文件名

```bash
# 使用 mv 保留编码（不使用 Read → Write）
mv ElementCtrl.java ElementController.java
```

### Step 5：更新引用

**5a. import 语句**：
```java
// 旧：import grp.pt.controller.ElementCtrl;
// 新：import grp.pt.controller.ElementController;
```

**5b. @Autowired 注入点**：
```java
// 旧：@Autowired private ElementCtrl elementCtrl;
// 新：@Autowired private ElementController elementController;
```
变量名转换规则：旧类名首字母小写 = 旧变量名（`elementCtrl`），新类名首字母小写 = 新变量名（`elementController`）

**5c. @Resource 注入点**：
```java
// 旧：@Resource private ElementCtrl elementCtrl;
// 新：@Resource private ElementController elementController;

// 如有 name 属性：
// 旧：@Resource(name = "elementCtrl") private ElementCtrl elementCtrl;
// 新：@Resource(name = "elementController") private ElementController elementController;
```

**5d. 构造器注入**：
```java
// 旧：
public MyService(ElementCtrl elementCtrl) {
    this.elementCtrl = elementCtrl;
}

// 新：
public MyService(ElementController elementController) {
    this.elementController = elementController;
}
```

**5e. @Qualifier 引用**：
```java
// 旧：@Qualifier("elementCtrl")
// 新：@Qualifier("elementController")
```

**5f. 类型引用（方法参数、返回值、局部变量）**：
```java
// 旧：public ElementCtrl getCtrl() { return new ElementCtrl(); }
// 新：public ElementController getCtrl() { return new ElementController(); }
// 注意：方法名 getCtrl() 不修改（属于业务逻辑）
```

**5g. 变量名不对应时的处理**：
```java
// 如果变量名不是旧类名首字母小写（如自定义变量名）：
// 旧：private ElementCtrl myCustomCtrl;
// 新：private ElementController myCustomCtrl;  // 只改类型名，不改变量名
```

### Step 6：验证

```bash
# 搜索所有 Java 文件中的旧类名
Grep "ElementCtrl" --include="*.java"
# 预期结果：0 匹配

# 搜索旧 import
Grep "import.*ElementCtrl" --include="*.java"
# 预期结果：0 匹配
```

---

## 操作示例二：DELETE/PUT 兼容增强

### 修正目标

```
旧注解：@DeleteMapping("/element/delete")
新注解：@RequestMapping(value = "/element/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
```

### Step 1：幂等检查

```
检查注解是否已是 @RequestMapping(method = {DELETE, POST}) 形式：
  当前为 @DeleteMapping → 需要修正
```

### Step 2：变更内容

```java
// 旧
@DeleteMapping("/element/delete")
public ReturnData<Boolean> deleteElement(@RequestParam Long id) { ... }

// 新
@RequestMapping(value = "/element/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
public ReturnData<Boolean> deleteElement(@RequestParam Long id) { ... }
```

### Step 3：import 管理

```java
// 新增（如不存在）：
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

// 删除（如文件中不再使用）：
import org.springframework.web.bind.annotation.DeleteMapping;
```

### Step 4：带额外属性的 @DeleteMapping

```java
// 旧（带 produces）
@DeleteMapping(value = "/element/delete", produces = "application/json")
public ReturnData<Boolean> deleteElement(@RequestParam Long id) { ... }

// 新（属性顺序：value → method → produces）
@RequestMapping(value = "/element/delete", method = {RequestMethod.DELETE, RequestMethod.POST}, produces = "application/json")
public ReturnData<Boolean> deleteElement(@RequestParam Long id) { ... }
```

### Step 5：多路径的 @DeleteMapping

```java
// 旧
@DeleteMapping({"/element/delete", "/element/remove"})

// 新
@RequestMapping(value = {"/element/delete", "/element/remove"}, method = {RequestMethod.DELETE, RequestMethod.POST})
```

### 注意事项

- 原有的 URL 路径 **不变**
- 原有的 HTTP 方法 **保留**，仅增加 POST 兼容
- 需确认 Controller 类上是否有 `@RequestMapping` 前缀
- **不修改** `@PatchMapping`（约束限制项）

---

## 操作示例三：Bean 命名冲突处理

### 场景A：同模块同名（"2"后缀）

```
冲突 Bean：ElementController 和 ElementController2
所在模块：element-controller
```

**决策树判定**：
```
存在 XxxController 和 XxxController2 在同一模块
→ 实际冲突
→ 可修复：重命名 ElementController2
→ 从 ElementController2 的 @RequestMapping 路径提取二级路径
  假设路径为 /config/element-ext/... → 新名称 ElementExtController
```

**处理步骤**：
```java
// 旧：public class ElementController2
// 新：public class ElementExtController

// 同步更新文件名、import、注入点
```

### 场景B：跨模块同名

```
冲突 Bean：elementController
  - ElementController 在 grp-pt-element/element-controller
  - ElementController 在 grp-pt-bookset/bookset-controller
```

**决策树判定**：
```
存在同名 Controller 在不同模块
→ 不重命名类
→ 给一方添加 @Controller("前缀名XxxController")
→ 前缀名 = artifactId 去掉 grp- 转驼峰
  grp-pt-element → ptElement
→ @Controller("ptElementElementController")
```

**处理步骤**：
```java
// 修改冲突方（按模块名字母序，选后者）
@RestController  // 保持不变
@Controller("ptElementElementController")  // 新增，但这与 @RestController 冲突！

// 正确方式：使用 @RestController 的自定义名称
// 旧：@RestController
// 新：@RestController("ptElementElementController")
// 注意：@RestController 支持 value 属性指定 Bean 名
```

### 场景C：孤立 "2" 后缀

```
只存在 ElementController2，不存在 ElementController
```

**决策树判定**：
```
只有 XxxController2 无 XxxController
→ 去掉 "2" 后缀
→ 重命名为 ElementController
→ 需先确认无冲突
```

---

## 执行优先级

1. **修复规范一（注解兼容）**：先 @DeleteMapping → 再 @PutMapping
2. **修复规范二（类命名修正）**：后缀修正 → 大驼峰修正
3. **修复规范三（Bean 冲突处理）**：FAIL → WARN
4. **标注约束**：在报告中列出不修改的约束限制项

## 文件处理顺序

同一批次内多个文件：
1. 按模块名字母顺序
2. 模块内按文件路径字母顺序
3. 同一文件内按行号从上到下
