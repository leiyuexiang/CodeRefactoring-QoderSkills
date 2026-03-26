# 类命名 / 属性命名 / 路径规范速查表

## 类命名规范

### 后缀规范

| 分层 | 标准后缀 | 名称限制 | 示例 |
|------|---------|---------|------|
| 控制层 | Controller | 不超过 40 字符 | ElementController |
| 逻辑层接口 | Service | 不超过 37 字符 | IElementService |
| 逻辑层实现 | ServiceImpl | 不超过 37 字符 | ElementServiceImpl |
| 数据层接口 | Dao | 不超过 33 字符 | IElementDao |
| 数据层实现 | DaoImpl | 不超过 33 字符 | ElementDaoImpl |
| 映射层 | Mapper | 不超过 36 字符 | ElementMapper |
| 实体层 | Entity | 不超过 36 字符 | ElementEntity |
| 数据传输 | DTO | 无硬性限制 | ElementDTO |
| 视图对象 | VO | 无硬性限制 | ElementVO |
| 查询条件 | Query / Param | 无硬性限制 | ElementQuery |

### 命名风格

| 规则 | 正确示例 | 错误示例 |
|------|---------|---------|
| 大驼峰命名 | `ElementController` | `elementController` |
| 标准后缀 | `ElementController` | `ElementCtrl` |
| 无不规范缩写 | `ElementService` | `ElementSvc` |
| 接口前缀（可选） | `IElementService` | — |

### 不规范缩写黑名单

| 缩写 | 应替换为 |
|------|---------|
| Ctrl | Controller |
| Svc | Service |
| Repo | Repository / Dao |
| Impl（独立使用） | XxxServiceImpl |

---

## 属性命名规范

| 规则 | 正确示例 | 错误示例 |
|------|---------|---------|
| 小驼峰命名 | `userId` | `user_id`（约束限制） |
| ID 后缀格式 | `userId`、`orderId` | `userID`、`user_ID` |
| 布尔前缀 | `isActive`、`isDeleted` | `active`、`deleted` |
| 禁止单字符 | `index`、`count` | `i`、`n`（循环变量除外） |
| 枚举值全大写 | `ORDER_STATUS` | `orderStatus` |

### 约束说明

- DTO/VO 属性如果使用下划线命名（如 `user_name`），**通常不建议修改**（影响 JSON 序列化兼容性）
- 如需修改下划线属性，应同步添加 `@JsonProperty("user_name")` 注解保持兼容

---

## 接口路径规范

### 四级路径结构

```
/{一级路径}/{二级路径}/{三级路径}/{四级路径}
```

| 层级 | 说明 | 示例 |
|------|------|------|
| 一级路径 | config/run 内部/外部标识 | `/config`、`/run` |
| 二级路径 | 模块名 | `/element`、`/bookset`、`/org` |
| 三级路径 | 操作类型 | `/query`、`/add`、`/modify`、`/delete` |
| 四级路径 | 接口明细名 | `/list`、`/detail`、`/byId` |

### HTTP 方法规范

| 推荐 | 不推荐 | 兼容方案 |
|------|--------|---------|
| `@PostMapping` | `@DeleteMapping` | `@RequestMapping(method = {DELETE, POST})` |
| `@GetMapping` | `@PutMapping` | `@RequestMapping(method = {PUT, POST})` |
| `@RequestMapping` | — | — |

### 路径约束

| 约束 | 标准 |
|------|------|
| 单层路径最大长度 | 40 字符 |
| 单个 Controller 最大接口数 | 15 个 |
| 路径使用小写 + 短横线 | `/element-config/query/list` |

---

## 接口参数规范

| 规则 | 正确示例 | 错误示例 |
|------|---------|---------|
| 小驼峰命名 | `pageNum` | `page_num`（约束限制） |
| 分页参数统一 | `pageNum` + `pageSize` | `page`/`size`/`limit` |
| 必填参数校验 | `@NotNull Long id` | `Long id` |

## 接口响应规范

| 响应类型 | 包装类 | 说明 |
|---------|--------|------|
| 普通响应 | `ReturnData<T>` | 统一返回格式 |
| 分页响应 | `ReturnPage<T>` | 统一分页返回格式 |
| 禁止 | 直接返回 Entity | Entity 不应暴露到 Controller 层 |
