# 命名修正标准流程与操作示例

## 标准修正步骤

每个类命名修正遵循以下 5 步流程：

```
1. 确认修正 → 检查当前类名和目标类名
2. 修改类定义 → 更新类名声明
3. 修改文件名 → 文件名与类名保持一致
4. 更新引用 → Grep 搜索所有 import 和使用位置，逐一修改
5. 验证 → Grep 确认无残留旧类名引用
```

## 操作示例：类名后缀修正

### 修正目标

```
旧类名：ElementCtrl
新类名：ElementController
旧文件名：ElementCtrl.java
新文件名：ElementController.java
```

### 变更内容

```java
// 修改类定义
// 旧：public class ElementCtrl {
// 新：public class ElementController {

// 所有引用方的 import 语句：
// 旧：import grp.pt.controller.ElementCtrl;
// 新：import grp.pt.controller.ElementController;
```

### 额外同步更新

注入点也需更新：

```java
// 其他类中的注入
// 旧：@Autowired private ElementCtrl elementCtrl;
// 新：@Autowired private ElementController elementController;
```

## 操作示例：DELETE/PUT 兼容增强

### 修正目标

```
旧注解：@DeleteMapping("/element/delete")
新注解：@RequestMapping(value = "/element/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
```

### 变更内容

```java
// 旧
@DeleteMapping("/element/delete")
public ReturnData<Boolean> deleteElement(@RequestParam Long id) { ... }

// 新
@RequestMapping(value = "/element/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
public ReturnData<Boolean> deleteElement(@RequestParam Long id) { ... }
```

### 注意事项

- 原有的 URL 路径 **不变**
- 原有的 HTTP 方法 **保留**，仅增加 POST 兼容
- 需确认 Controller 类上是否有 `@RequestMapping` 前缀

## 操作示例：Bean 命名冲突处理

### 场景

```
冲突 Bean：ElementController 和 ElementController2
原因：不同模块的同名 Controller
```

### 处理方式

```java
// 方式一：显式指定 Bean 名
@Controller("ptElementController")
public class ElementController { ... }

// 方式二：重命名类（如确认 "2" 后缀非有意设计）
// 旧：public class ElementController2
// 新：public class ElementExtController
```

## 执行优先级

1. **接口路径兼容**：DELETE/PUT 添加 POST 兼容
2. **类命名修正**：后缀修正 → 大驼峰修正
3. **Bean 冲突处理**：确认后处理
4. **标注约束**：在报告中列出不修改的约束限制项
