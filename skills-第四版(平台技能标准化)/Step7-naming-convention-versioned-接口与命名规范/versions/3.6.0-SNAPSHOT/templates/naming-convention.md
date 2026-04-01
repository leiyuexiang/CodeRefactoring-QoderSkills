# 类命名 / 属性命名 / 路径规范速查表

## 类命名规范

### 后缀规范

| 分层 | 标准后缀 | 最大类名长度 | 示例 | 接口前缀 |
|------|---------|------------|------|---------|
| 控制层 | Controller | 40 字符 | ElementController | 无 |
| 逻辑层接口 | Service | 37 字符 | IElementService | I（可选） |
| 逻辑层实现 | ServiceImpl | 41 字符 | ElementServiceImpl | 无 |
| 数据层接口 | Dao | 33 字符 | IElementDao | I（可选） |
| 数据层实现 | DaoImpl | 37 字符 | ElementDaoImpl | 无 |
| 映射层 | Mapper | 36 字符 | ElementMapper | 无 |
| 实体层 | Entity | 36 字符 | ElementEntity | 无（Entity 后缀可选） |
| 数据传输 | DTO | 无硬性限制 | ElementDTO | 无 |
| 视图对象 | VO | 无硬性限制 | ElementVO | 无 |
| 查询条件 | Query / Param | 无硬性限制 | ElementQuery | 无 |

### 命名风格

| 规则 | 正确示例 | 错误示例 |
|------|---------|---------|
| 大驼峰命名 | `ElementController` | `elementController` |
| 标准后缀 | `ElementController` | `ElementCtrl` |
| 无不规范缩写 | `ElementService` | `ElementSvc` |
| 接口前缀（可选） | `IElementService` | — |

### 不规范缩写黑名单

| 缩写 | 检测方式 | 应替换为 | 转换示例 |
|------|---------|---------|---------|
| Ctrl | 类名以 `Ctrl` 结尾 | Controller | `ElementCtrl` → `ElementController` |
| Svc | 类名包含 `Svc` | Service | `ElementSvc` → `ElementService` |
| Repo | 类名以 `Repo` 结尾 | Repository 或 Dao | `ElementRepo` → `ElementDao` |
| Mgr | 类名以 `Mgr` 结尾 | Manager | `ElementMgr` → `ElementManager` |
| Util（单数） | 类名以 `Util` 结尾 | Utils | `StringUtil` → `StringUtils` |
| Info 替代 DTO/VO | 类名以 `Info` 结尾且在 dto/vo 目录 | DTO 或 VO | `ElementInfo` → `ElementDTO` |
| Action | 类名以 `Action` 结尾且在 controller 目录 | Controller | `ElementAction` → `ElementController` |

### 后缀转换确定性规则

当需要修正后缀时，按以下精确规则转换：

```
function convertSuffix(className, layer):
    // 1. 先尝试精确匹配黑名单后缀
    for (oldSuffix, newSuffix) in SUFFIX_MAP:
        if className.endsWith(oldSuffix):
            return className.removeSuffix(oldSuffix) + newSuffix
    
    // 2. 如果无黑名单匹配，直接追加标准后缀
    return className + STANDARD_SUFFIX[layer]
```

**SUFFIX_MAP（按优先级排序）**：

| 旧后缀 | 新后缀 | 适用层 |
|--------|--------|--------|
| `Ctrl` | `Controller` | Controller |
| `Action` | `Controller` | Controller |
| `Svc` | `Service` | Service |
| `SvcImpl` | `ServiceImpl` | Service 实现 |
| `Repo` | `Dao` | DAO |
| `RepoImpl` | `DaoImpl` | DAO 实现 |
| `Mgr` | `Manager` | 任意 |

### 跳过后缀检查的类类型

| 类所在目录 | 说明 |
|-----------|------|
| `**/util/**` 或 `**/utils/**` | 工具类，无标准后缀要求 |
| `**/config/**` 或 `**/configuration/**` | 配置类，无标准后缀要求 |
| `**/constant/**` 或 `**/constants/**` | 常量类，无标准后缀要求 |
| `**/exception/**` | 异常类，期望 `Exception` 后缀 |
| `**/enums/**` 或 `**/enum/**` | 枚举类，无标准后缀要求 |
| `**/handler/**` | 处理器类，无标准后缀要求 |
| `**/interceptor/**` | 拦截器类，无标准后缀要求 |
| `**/filter/**` | 过滤器类，无标准后缀要求 |
| `**/listener/**` | 监听器类，无标准后缀要求 |
| `**/aspect/**` | 切面类，无标准后缀要求 |
| `**/converter/**` | 转换器类，无标准后缀要求 |

---

## 属性命名规范

| 规则 | 正确示例 | 错误示例 | 检测正则 |
|------|---------|---------|---------|
| 小驼峰命名 | `userId` | `user_id`（约束限制） | `\w+_\w+` |
| ID 后缀格式 | `userId`、`orderId` | `userID`、`user_ID` | `\w+ID$` 或 `\w+_[iI][dD]$` |
| 布尔前缀 | `isActive`、`isDeleted` | `active`、`deleted` | `boolean/Boolean` 类型且不匹配 `^is[A-Z]` |
| 禁止单字符 | `index`、`count` | `i`、`n`（循环变量除外） | 字段名长度 = 1 |
| 枚举值全大写 | `ORDER_STATUS` | `orderStatus` | 枚举常量不匹配 `^[A-Z][A-Z0-9_]*$` |
| 常量全大写 | `MAX_SIZE` | `maxSize` | `static final` 字段不匹配 `^[A-Z][A-Z0-9_]*$` |

### 约束说明

- DTO/VO 属性如果使用下划线命名（如 `user_name`），**通常不建议修改**（影响 JSON 序列化兼容性）
- 如需修改下划线属性，应同步添加 `@JsonProperty("user_name")` 注解保持兼容
- 布尔属性前缀 `is` 是约束限制项，因为修改会影响 getter/setter 方法名（Lombok 生成或手写），可能导致序列化/反序列化异常

### 特殊字段跳过规则

| 字段 | 跳过原因 |
|------|---------|
| `serialVersionUID` | Java 序列化保留字段 |
| `logger` / `log` / `LOG` | 日志字段，非业务属性 |
| `static final` 字段 | 按常量规范检查（全大写下划线），不检查小驼峰 |

---

## 接口路径规范

### 四级路径结构

```
/{一级路径}/{二级路径}/{三级路径}/{四级路径}
```

| 层级 | 说明 | 合规示例 | 不合规示例 |
|------|------|---------|-----------|
| 一级路径 | config/run 内部/外部标识 | `/config`、`/run` | `/api`（不符合内外部分类） |
| 二级路径 | 模块名 | `/element`、`/bookset`、`/org` | `/elem`（缩写） |
| 三级路径 | 操作类型 | `/query`、`/add`、`/modify`、`/delete` | `/get`、`/create`（非标准动词） |
| 四级路径 | 接口明细名 | `/list`、`/detail`、`/byId` | `/getAllList`（冗余） |

### 标准三级路径动词

| 标准动词 | 含义 | 不推荐的替代词 |
|---------|------|-------------|
| `query` | 查询 | `get`、`find`、`search`、`select`、`fetch` |
| `add` | 新增 | `create`、`insert`、`save`、`new` |
| `modify` | 修改 | `update`、`edit`、`change`、`put` |
| `delete` | 删除 | `remove`、`del`、`destroy`、`drop` |
| `import` | 导入 | `upload`（当导入数据时） |
| `export` | 导出 | `download`（当导出数据时） |

### HTTP 方法规范

| 推荐 | 不推荐 | 兼容方案 | 说明 |
|------|--------|---------|------|
| `@PostMapping` | `@DeleteMapping` | `@RequestMapping(method = {DELETE, POST})` | 可修复 |
| `@GetMapping` | `@PutMapping` | `@RequestMapping(method = {PUT, POST})` | 可修复 |
| `@RequestMapping` | `@PatchMapping` | — | 约束限制，仅报告 |

### 路径约束

| 约束 | 标准 | 检查方法 |
|------|------|---------|
| 单层路径最大长度 | 40 字符 | 每个 "/" 之间的字符数 |
| 单个 Controller 最大接口数 | 15 个 | 统计 `@*Mapping` 注解数 |
| 路径使用小写 + 短横线 | `/element-config/query/list` | 路径段不含大写字母 |
| 路径不以 "/" 结尾 | `/config/element/query/list` | 路径末尾无 "/" |
| 路径以 "/" 开头 | `/config/element/query/list` | 路径开头有 "/" |

---

## 接口参数规范

| 规则 | 正确示例 | 错误示例 | 分类 |
|------|---------|---------|------|
| 小驼峰命名 | `pageNum` | `page_num`（约束限制） | 约束限制 |
| 分页参数统一 | `pageNum` + `pageSize` | `page`/`size`/`limit`/`pageNo` | 约束限制 |
| 必填参数校验 | `@NotNull Long id` | `Long id` | 约束限制 |

### 分页参数标准化对照表

| 非标准参数名 | 标准参数名 | 说明 |
|------------|-----------|------|
| `page` | `pageNum` | 页码 |
| `pageNo` | `pageNum` | 页码 |
| `currentPage` | `pageNum` | 页码 |
| `size` | `pageSize` | 每页大小 |
| `limit` | `pageSize` | 每页大小 |
| `rows` | `pageSize` | 每页大小 |
| `offset` | — | offset 分页模式不做强制转换 |

### 跳过检查的参数类型

| 参数类型 | 跳过原因 |
|---------|---------|
| `HttpServletRequest` | 框架参数 |
| `HttpServletResponse` | 框架参数 |
| `Model` / `ModelMap` / `ModelAndView` | Spring MVC 参数 |
| `BindingResult` / `Errors` | 校验结果参数 |
| `MultipartFile` | 文件上传参数 |
| `Authentication` / `Principal` | 安全框架参数 |
| `Pageable` | Spring Data 分页参数 |

## 接口响应规范

| 响应类型 | 包装类 | 说明 |
|---------|--------|------|
| 普通响应 | `ReturnData<T>` | 统一返回格式 |
| 分页响应 | `ReturnPage<T>` | 统一分页返回格式 |
| 禁止 | 直接返回 Entity | Entity 不应暴露到 Controller 层 |

### 返回类型合规判定

| 返回类型模式 | 判定 | 说明 |
|------------|------|------|
| `ReturnData<XxxVO>` | PASS | 标准包装 |
| `ReturnData<List<XxxVO>>` | PASS | 标准列表包装 |
| `ReturnData<Map<String, Object>>` | PASS | Map 包装（不推荐但合规） |
| `ReturnData<Boolean>` | PASS | 布尔包装 |
| `ReturnData<String>` | PASS | 字符串包装 |
| `ReturnData<?>` | WARN | 建议指定具体类型 |
| `ReturnData<Object>` | WARN | 建议指定具体类型 |
| `ReturnPage<XxxVO>` | PASS | 标准分页包装 |
| `ReturnData<XxxEntity>` | WARN | Entity 不应暴露 |
| `XxxEntity` | WARN | 未包装且暴露 Entity |
| `List<XxxVO>` | WARN | 未使用 ReturnData 包装 |
| `ResponseEntity<?>` | WARN | 非标准返回类型 |
| `void` | PASS | 无返回值（文件下载等场景合规） |
| `String`（返回视图名） | PASS | MVC 场景合规 |

---

## 注解转换速查表

### @DeleteMapping 转换

| 原形式 | 目标形式 |
|--------|---------|
| `@DeleteMapping("/path")` | `@RequestMapping(value = "/path", method = {RequestMethod.DELETE, RequestMethod.POST})` |
| `@DeleteMapping(value = "/path")` | `@RequestMapping(value = "/path", method = {RequestMethod.DELETE, RequestMethod.POST})` |
| `@DeleteMapping(value = "/path", produces = "application/json")` | `@RequestMapping(value = "/path", method = {RequestMethod.DELETE, RequestMethod.POST}, produces = "application/json")` |
| `@DeleteMapping({"/path1", "/path2"})` | `@RequestMapping(value = {"/path1", "/path2"}, method = {RequestMethod.DELETE, RequestMethod.POST})` |
| `@DeleteMapping` （无参数） | `@RequestMapping(method = {RequestMethod.DELETE, RequestMethod.POST})` |

### @PutMapping 转换

| 原形式 | 目标形式 |
|--------|---------|
| `@PutMapping("/path")` | `@RequestMapping(value = "/path", method = {RequestMethod.PUT, RequestMethod.POST})` |
| `@PutMapping(value = "/path")` | `@RequestMapping(value = "/path", method = {RequestMethod.PUT, RequestMethod.POST})` |
| `@PutMapping(value = "/path", consumes = "application/json")` | `@RequestMapping(value = "/path", method = {RequestMethod.PUT, RequestMethod.POST}, consumes = "application/json")` |
| `@PutMapping({"/path1", "/path2"})` | `@RequestMapping(value = {"/path1", "/path2"}, method = {RequestMethod.PUT, RequestMethod.POST})` |
| `@PutMapping` （无参数） | `@RequestMapping(method = {RequestMethod.PUT, RequestMethod.POST})` |

### import 联动更新表

| 操作 | 新增 import | 删除 import（条件） |
|------|------------|-------------------|
| `@DeleteMapping` → `@RequestMapping` | `RequestMapping`、`RequestMethod` | `DeleteMapping`（当文件中不再使用时） |
| `@PutMapping` → `@RequestMapping` | `RequestMapping`、`RequestMethod` | `PutMapping`（当文件中不再使用时） |
| 类名修改 | 新 import 路径 | 旧 import 路径 |
