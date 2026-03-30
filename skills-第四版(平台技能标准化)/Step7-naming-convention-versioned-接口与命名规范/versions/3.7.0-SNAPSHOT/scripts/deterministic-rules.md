# 确定性决策规则与幂等性保障

## 概述

本文件定义所有 S5 检查与修复操作的**确定性决策规则**和**幂等性保障机制**，确保相同代码在多次执行时产生一致的检查结果和修复输出。

---

## 一、确定性决策树

### 1.1 S5-01 接口路径 — 决策树

```
INPUT: 一个 Controller 方法上的 HTTP 注解

├─ 注解类型 = @DeleteMapping
│   ├─ 已有 POST 兼容 (即已是 @RequestMapping(method={DELETE,POST}))
│   │   └─ 结论：PASS（跳过）
│   └─ 未有 POST 兼容
│       └─ 结论：WARN（可修复），执行修复规范一
│
├─ 注解类型 = @PutMapping
│   ├─ 已有 POST 兼容
│   │   └─ 结论：PASS（跳过）
│   └─ 未有 POST 兼容
│       └─ 结论：WARN（可修复），执行修复规范一
│
├─ 注解类型 = @PatchMapping
│   └─ 结论：WARN（约束限制），仅报告不修复
│
├─ 注解类型 = @GetMapping / @PostMapping
│   └─ 结论：PASS
│
└─ 注解类型 = @RequestMapping
    ├─ method 属性包含 DELETE 或 PUT（无 POST 兼容）
    │   └─ 结论：WARN（可修复），添加 POST 兼容
    ├─ method 属性包含 DELETE/PUT 且已有 POST
    │   └─ 结论：PASS（跳过）
    └─ method 属性为 GET/POST/未指定
        └─ 结论：PASS（method 未指定时标记 WARN：HTTP 方法不明确）
```

### 1.2 S5-02 类命名 — 决策树

```
INPUT: 一个 Java 类文件

├─ 文件路径在排除目录中（test/target/generated）
│   └─ 结论：跳过
│
├─ 类为枚举类 / 工具类 / 配置类 / 常量类 / 异常类
│   └─ 仅检查大驼峰命名，跳过层级后缀检查
│
├─ 类为 Controller 层
│   ├─ 类名以 Controller 结尾
│   │   ├─ 大驼峰命名 → PASS
│   │   └─ 非大驼峰 → WARN（可修复：大驼峰修正）
│   └─ 类名不以 Controller 结尾
│       ├─ 类名以 Ctrl/Action 等黑名单缩写结尾
│       │   └─ WARN（可修复：后缀修正）
│       └─ 类名以其他后缀结尾
│           └─ WARN（可修复：后缀修正为 Controller）
│
├─ 类为 Service 层（接口）
│   ├─ 类名以 Service 结尾（含 IXxxService 形式）→ PASS
│   └─ 其他 → WARN（可修复）
│
├─ 类为 Service 层（实现）
│   ├─ 类名以 ServiceImpl 结尾 → PASS
│   └─ 其他 → WARN（可修复）
│
├─ 类为 DAO 层
│   ├─ 接口：以 Dao 结尾 → PASS
│   ├─ 实现：以 DaoImpl 结尾 → PASS
│   └─ 其他 → WARN（可修复）
│
├─ 类为 Mapper 层
│   ├─ 以 Mapper 结尾 → PASS
│   └─ 其他 → WARN（可修复）
│
└─ 类为 Model 层（Entity/DTO/VO/Query）
    ├─ 后缀匹配对应目录规范 → PASS
    └─ 后缀不匹配 → WARN（约束限制，影响序列化）
```

### 1.3 S5-03 属性命名 — 决策树

```
INPUT: 一个 Java 字段声明

├─ static final 常量
│   ├─ 全大写下划线格式 → PASS
│   └─ 非全大写 → WARN（可修复）
│
├─ serialVersionUID
│   └─ 跳过
│
├─ 字段所在类为 DTO/VO
│   ├─ 字段使用下划线命名
│   │   ├─ 有 @JsonProperty 注解 → PASS（已兼容）
│   │   └─ 无 @JsonProperty → WARN（约束限制：影响序列化）
│   └─ 字段使用驼峰命名
│       ├─ 小驼峰 → PASS
│       └─ 非小驼峰 → WARN（可修复）
│
├─ 字段所在类为非 DTO/VO
│   ├─ 字段使用下划线命名 → WARN（可修复）
│   ├─ 字段首字母大写 → WARN（可修复）
│   ├─ 字段为单字符 → WARN（可修复）
│   └─ 字段为小驼峰 → PASS
│
├─ 布尔类型字段
│   ├─ boolean/Boolean 类型
│   │   ├─ 以 is 开头 → PASS
│   │   └─ 不以 is 开头 → WARN（约束限制：影响 getter/setter 命名）
│   └─ 非布尔类型 → 跳过此项检查
│
└─ ID 后缀检查
    ├─ 字段名以 ID 结尾（全大写）→ WARN（应为 xxxId）
    ├─ 字段名以 _id 结尾 → WARN（应为 xxxId）
    ├─ 字段名以 Id 结尾 → PASS
    └─ 不涉及 ID → 跳过
```

### 1.4 S5-06 Bean 命名冲突 — 决策树

```
INPUT: 全局 Bean 名称列表

├─ 无重复 Bean 名
│   └─ 结论：PASS
│
├─ 存在重复 Bean 名
│   ├─ 冲突双方在同一模块
│   │   ├─ 一方类名有 "2" 后缀
│   │   │   ├─ 有注释说明 "2" 的用途 → 约束限制
│   │   │   └─ 无注释说明 → FAIL（可修复：重命名 "2" 后缀的类）
│   │   └─ 双方无 "2" 后缀
│   │       └─ FAIL（可修复：给一方添加模块前缀 Bean 名）
│   │
│   └─ 冲突双方在不同模块
│       └─ FAIL（可修复：给冲突方添加 @Controller("模块前缀名")）
│
├─ 存在 "2" 后缀但无冲突
│   ├─ 同名无 "2" 的类不存在
│   │   └─ WARN（可修复：去掉 "2" 后缀）
│   └─ 同名无 "2" 的类存在（在其他模块）
│       └─ WARN（可修复：重命名为有意义名称）
│
└─ 存在自定义 Bean 名 @Service("xxx")
    ├─ 自定义名与其他默认 Bean 名不冲突 → PASS
    └─ 自定义名与其他 Bean 名冲突 → FAIL（可修复：修改自定义名）
```

---

## 二、幂等性保障机制

### 2.1 检查阶段幂等性

**原则**：同一代码，无论执行多少次检查，产生的检查报告内容和格式完全一致。

**实现规则**：
1. 检查结果仅基于代码静态分析，不依赖外部状态（如时间、随机数）
2. 报告中的问题项按固定顺序排列：
   - 第一排序键：检查项编号（S5-01 → S5-06）
   - 第二排序键：文件路径字母序
   - 第三排序键：行号升序
3. 同一问题不重复报告（使用文件路径+行号+问题类型作为去重键）

### 2.2 修复阶段幂等性

**原则**：如果代码已处于合规状态，重复执行修复操作不产生任何变更。

**实现规则**：

| 修复项 | 幂等性检查方法 | 已合规判定条件 |
|--------|-------------|-------------|
| DELETE/PUT 兼容 | 检查注解是否已包含 `method = {原Method, RequestMethod.POST}` | 已包含 → 跳过 |
| 类名后缀修正 | 检查类名是否已使用标准后缀 | 后缀合规 → 跳过 |
| 类名大驼峰修正 | 检查首字母是否已大写 | 已大写 → 跳过 |
| Bean 冲突处理 | 检查是否存在重复 Bean 名 | 无重复 → 跳过 |
| import 清理 | 检查是否存在无用 import | 无无用 import → 跳过 |

### 2.3 变更检测

修复前后对比：
```
1. 修复前：记录待修改文件列表
2. 执行修复
3. 修复后：Grep 验证旧内容不存在
4. 如果修复前后文件无变化 → 报告"已合规，无需修改"
```

---

## 三、一致性保障规则

### 3.1 报告格式一致性

所有检查报告**必须**使用以下严格模板（非示例），任何偏离都视为不合规输出。

**报告模板见** → [examples/check-report.md](../examples/check-report.md)

### 3.2 命名转换一致性

所有命名转换**必须**使用以下确定性算法：

**首字母小写算法**：
```
function toLowerFirst(className):
    if len(className) >= 2 and className[0].isUpper() and className[1].isUpper():
        return className  // 如 URLParser → URLParser（连续大写不转换）
    return className[0].lower() + className[1:]  // 如 ElementController → elementController
```

**首字母大写算法**：
```
function toUpperFirst(name):
    return name[0].upper() + name[1:]  // 如 elementController → ElementController
```

**后缀替换算法**：
```
function replaceSuffix(className, oldSuffix, newSuffix):
    if className.endsWith(oldSuffix):
        return className[:-len(oldSuffix)] + newSuffix
    return className + newSuffix  // 如果无旧后缀，直接追加新后缀
```

### 3.3 文件处理顺序一致性

所有文件处理**必须**按以下确定性顺序：

```
1. 模块排序：按 pom.xml 中 <modules> 声明顺序
2. 层级排序：Controller → Service → DAO → Model
3. 目录排序：按目录路径字母序
4. 文件排序：按文件名字母序
5. 文件内排序：按行号升序
```

### 3.4 修复计划格式一致性

修复计划**必须**使用以下格式：

```
## 修复计划

### 修复项 1：[修复类型]
- 文件：[完整文件路径]
- 当前：[当前代码]
- 目标：[修改后代码]
- 影响范围：[受影响的引用文件列表]
- 风险等级：[低/中/高]

### 修复项 2：[修复类型]
...
```

---

## 四、异常处理规则

### 4.1 编译错误场景

| 场景 | 处理方式 |
|------|---------|
| 修改类名后发现新名称已存在 | 回滚此次修改，报告冲突，等待用户决策 |
| import 路径找不到 | 标记为需要手动处理的项 |
| 同一类在多处使用不同缩写引用 | 逐一替换，不批量 |

### 4.2 无法自动决策的场景

以下场景**必须**暂停并请求用户确认：

1. 类名有 "2" 后缀且有注释说明用途
2. 新类名已存在于工程中
3. 同一个 Bean 名称冲突涉及 3 个以上类
4. 类名修改可能影响外部系统调用（如 Feign 接口）

### 4.3 回滚策略

如果修复过程中出现异常：
1. 停止当前修复项
2. 不回滚已成功的修复项
3. 报告异常原因和当前进度
4. 等待用户确认后继续或终止
