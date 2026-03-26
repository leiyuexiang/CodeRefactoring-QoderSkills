# S3 检查规则清单

## S3-01：Service 层接口/实现分离规范

**检查目标**：Service 层是否按接口/实现分离。

**标准结构**：
```
service/
├── IXxxService.java       (接口，位于 service/ 或 service/facade/)
└── impl/
    └── XxxServiceImpl.java (实现类)
```

**检查方法**：
- 扫描 `service/` 目录下的文件
- 检查接口是否在 `service/` 根目录或 `service/facade/` 下
- 检查实现类是否在 `service/impl/` 下
- 检查是否有实现类（`@Service` 注解）直接放在 `service/` 根目录

**判定标准**：
- Service 实现类不在 `impl/` 目录 → **FAIL**
- Service 接口和实现混放在同一目录 → **WARN**

---

## S3-02：facade/ 目录存在性

**检查目标**：`service/` 下是否存在 `facade/` 子目录，且包含所有 Service 接口。

**检查方法**：
- 使用 Glob 扫描 `service/` 下的一级子目录
- 检查是否存在 `facade/` 目录
- 使用 Grep 搜索所有 `interface` + `Service` 后缀的文件
- 对比 `facade/` 中的接口数与项目中 Service 接口总数

**判定标准**：
- `service/` 下不存在 `facade/` 子目录 → **FAIL**
- `facade/` 存在但不包含所有 Service 接口 → **FAIL**
- `facade/` 存在且包含所有 Service 接口 → **PASS**

---

## S3-03：Service 接口归属正确性

**检查目标**：每个 Service 接口文件是否正确放在 `facade/` 下。

**检查方法**：
1. 使用 Grep 搜索所有 `interface` 定义文件
2. 筛选类名含 `Service` 后缀的接口（排除 `@FeignClient`）
3. 检查每个 Service 接口的 package 路径

**判定标准**：
- Service 接口直接放在 `service/` 根目录 → **FAIL**
- Service 接口放在业务子目录（如 `service/basedata/`）而非 `facade/` → **FAIL**
- 非 Service 文件混入 `facade/` → **WARN**
- Service 接口正确在 `service/facade/` 下 → **PASS**

### 排除项

以下不算 Service 接口：
- `@FeignClient` 注解的接口（Feign 客户端）
- 非 `Service` 后缀的通用接口

> 详细分类指南 → [templates/classification-guide.md](../templates/classification-guide.md)

---

## S3-04：Service 实现归属正确性

**检查目标**：所有 Service 实现类是否统一放在 `service/impl/` 下。

**检查方法**：
1. 使用 Grep 搜索 `@Service` 注解和 `ServiceImpl` 后缀的类
2. 检查每个实现类的 package 路径
3. 检查是否存在分散的 `impl/` 子目录（如 `basedata/impl/`、`bookset/impl/`）

**判定标准**：
- Service 实现分散在多个 `impl/` 子目录（如 `basedata/impl/`、`bookset/impl/`） → **FAIL**
- 实现类不在 `service/impl/` 下 → **FAIL**
- `service/imp/` 残留目录（命名不规范） → **FAIL**
- 所有实现统一在 `service/impl/` 下 → **PASS**

---

## S3-05：非 Service 文件处理

**检查目标**：`service/` 下的非 Service 文件是否合理放置。

**检查方法**：
- 扫描 `service/` 下所有文件
- 排除 Service 接口和实现后，识别非 Service 文件
- 检查非 Service 文件是否混入 `facade/` 或 `impl/`

**判定标准**：
- 非 Service 文件混入 `facade/` → **WARN**
- 非 Service 文件混入 `impl/` → **WARN**
- Feign 客户端接口混入 `service/facade/` → **WARN**
- 非 Service 业务子包（constant、enums、model、util 等）存在于 `service/` → **WARN**（建议后续迁出，但不在 S3 范围内修复）
