# S4 检查规则清单

## S4-01：custom/common 一级目录存在性

**检查目标**：`controller/` 下是否存在 `custom/` 和 `common/` 两个一级子目录。

**检查方法**：
- 使用 Glob 扫描 `controller/` 下的一级子目录
- 检查是否存在 `custom/` 目录
- 检查是否存在 `common/` 目录

**判定标准**：
- `controller/` 下不存在 `custom/` 子目录 → **FAIL**
- `controller/` 下不存在 `common/` 子目录 → **FAIL**
- 两个目录都存在 → **PASS**

---

## S4-02：Controller 归属正确性

**检查目标**：每个 Controller 文件是否放在正确的 `custom/` 或 `common/` 子目录下。

**检查方法**：
1. 使用 Glob 扫描 `controller/` 下所有 Java 文件
2. 对 `controller/` 根目录下的 Controller 文件 → 标记为 FAIL
3. 对非 `custom/` 非 `common/` 子目录下的 Controller 文件 → 标记为 FAIL
4. 使用 Grep 搜索 `@RequestMapping` 路径前缀判断接口类型
5. 对照分类原则验证归属是否正确

**判定标准**：
- Controller 文件直接放在 `controller/` 根目录 → **FAIL**
- Controller 文件放在非 `custom/` 非 `common/` 的子目录（如 `controller/basedata/`） → **FAIL**
- 外部接口 Controller 不在 `custom/` 下 → **WARN**
- 内部接口 Controller 不在 `common/` 下 → **WARN**

### 分类判断依据

| 判断条件 | 归属 |
|---------|------|
| `@RequestMapping` 路径以 `run/` 开头 | custom/（外部接口） |
| `@RequestMapping` 路径以 `config/` 开头 | common/（内部接口） |
| 面向前端 UI 的 CRUD 接口 | custom/ |
| 内部 API / 工具 / 调试 / 同步 | common/ |

> 详细分类指南 → [templates/classification-guide.md](../templates/classification-guide.md)

---

## S4-03：二级业务分组合理性

**检查目标**：`custom/` 和 `common/` 下的二级分组是否合理。

**检查方法**：
- 统计 `custom/` 和 `common/` 下每个子目录的文件数
- 检查功能相关的 Controller 是否在同一子目录下
- 检查是否有未分组的文件直接在 `custom/` 或 `common/` 根目录

**判定标准**：
- `custom/` 或 `common/` 下文件超过 10 个未进一步分组 → **WARN**
- 功能相关的 Controller 分散在不同子目录 → **WARN**
- `custom/` 或 `common/` 根目录下直接存在 Controller 文件 → **WARN**

### 二级分组规范

- custom/ 内部按**业务域**分组（basedata、bookset、agencyManager 等）
- common/ 内部按**功能类型**分组（api、util、notify、sync 等）
- 单个分组文件超过 10 个时建议进一步细分

---

## S4-04：非 controller 包下的 Controller

**检查目标**：是否有 Controller 类放在 `controller` 包以外的位置。

**检查方法**：
- 使用 Grep 搜索 `@Controller` 和 `@RestController` 注解
- 检查这些类的 package 声明是否在 `controller` 包下
- 排除合法的非 controller 包（如 feign client）

**判定标准**：
- Controller 类放在非 `controller` 包下（如根包 `grp.pt`） → **FAIL**
- Feign 客户端的 `@FeignClient` 接口不算 Controller → 排除
