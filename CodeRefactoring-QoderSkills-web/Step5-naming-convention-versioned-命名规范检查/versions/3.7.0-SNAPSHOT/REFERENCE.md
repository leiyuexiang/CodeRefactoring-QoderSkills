# S8 命名规范检查规则（3.6.0-SNAPSHOT）

## 概述

本文档定义了前端命名规范检查与修复的完整规则集。基于阿里前端开发规范和《一体化系统界面规范》，涵盖组件命名、文件命名、变量命名、CSS 类名、事件函数命名、页签命名、流程节点命名、Props 类型命名共 8 大类检查规则。

**本技能提供两大功能：**
1. **命名规范检查**：扫描前端代码，检查各类命名是否符合规范
2. **命名规范修复**：根据检查结果，辅助修复不合规的命名

## 使用场景

| 用户意图 | 触发关键词 | 执行功能 |
|---------|-----------|----------|
| 检查命名是否合规 | "命名规范"、"命名检查"、"命名审查" | 功能一：命名检查 |
| 修复命名违规 | "命名修复"、"重命名"、"命名整改" | 功能一 + 功能二：检查 + 修复 |
| 检查特定类别 | "组件命名"、"文件命名"、"变量命名"、"CSS类名" | 功能一：分类检查 |

---

## 检查项总览

| 编号 | 检查项 | 严重级别 | 说明 |
|------|--------|---------|------|
| S8-01 | 组件命名规范 | ERROR | 组件使用 PascalCase，单文件组件文件名与组件名一致 |
| S8-02 | 文件命名规范 | ERROR | 普通文件使用 kebab-case，组件文件使用 PascalCase |
| S8-03 | 变量命名规范 | WARNING | 变量/函数使用 camelCase，常量使用 UPPER_SNAKE_CASE |
| S8-04 | CSS 类名规范 | WARNING | 使用 BEM 命名或 CSS Modules |
| S8-05 | 事件函数命名 | WARNING | 事件处理函数以 handle/on 前缀开头 |
| S8-06 | 页签命名 | WARNING | 页签名称简洁，不超过 6 个汉字 |
| S8-07 | 流程节点命名 | WARNING | 流程节点使用动宾结构 |
| S8-08 | Props 类型命名 | ERROR | Props 接口以组件名 + Props 后缀命名 |

---

## 详细规则定义

### S8-01: 组件命名规范

**级别**: ERROR

```
规范要求:
- 组件名使用 PascalCase（大驼峰）
- 单文件组件 .vue 文件名与内部 name 一致
- 多单词组件名（避免与 HTML 元素冲突）
- 基础组件以 Base/App/V 开头

合规示例:
  DataTable.vue → name: 'DataTable'
  BaseButton.vue → name: 'BaseButton'
  QueryPanel.vue → name: 'QueryPanel'

违规示例:
  dataTable.vue ❌（应使用 PascalCase）
  data-table.vue ❌（组件文件应使用 PascalCase）
  Table.vue ❌（单单词，与 HTML <table> 冲突）

检查方式:
1. 扫描所有 .vue 文件名
2. 检查是否为 PascalCase
3. 检查组件 name 属性是否与文件名一致
4. 检查是否为多单词（≥2 个单词）
```

### S8-02: 文件命名规范

**级别**: ERROR

```
规范要求:
- TypeScript 文件: kebab-case（如 use-table-data.ts）
- Vue 组件文件: PascalCase（如 DataTable.vue）
- 目录名: kebab-case（如 query-panel/）
- 测试文件: *.spec.ts 或 *.test.ts

合规示例:
  api/index.ts
  composables/use-table-data.ts
  types/base-data.ts
  components/DataTable.vue
  utils/format-number.ts

违规示例:
  api/Index.ts ❌
  composables/tableData.ts ❌
  types/BaseData.ts ❌（非组件 TS 文件应 kebab-case）
  utils/FormatNumber.ts ❌

检查方式:
1. 扫描所有 .ts 文件名，检查是否为 kebab-case
2. 扫描所有 .vue 文件名，检查是否为 PascalCase
3. 扫描所有目录名，检查是否为 kebab-case
4. 排除 node_modules/、dist/ 等构建目录
```

### S8-03: 变量命名规范

**级别**: WARNING

```
规范要求:
- 变量/函数: camelCase
- 常量: UPPER_SNAKE_CASE
- 接口/类型: PascalCase（以 I 前缀可选）
- 枚举: PascalCase，枚举值 UPPER_SNAKE_CASE
- 布尔变量: is/has/can/should 前缀

合规示例:
  const userName = 'admin'
  const MAX_RETRY_COUNT = 3
  interface UserInfo { ... }
  enum StatusType { ACTIVE = 'active' }
  const isVisible = ref(true)

违规示例:
  const user_name = 'admin' ❌（应 camelCase）
  const maxRetryCount = 3 ❌（常量应 UPPER_SNAKE_CASE）
  const visible = ref(true) ❌（布尔应加前缀）
```

### S8-04: CSS 类名规范

**级别**: WARNING

```
规范要求:
- 使用 BEM 命名法或 CSS Modules
- 块: kebab-case（如 query-panel）
- 元素: 双下划线分隔（如 query-panel__item）
- 修饰符: 双连字符分隔（如 query-panel--expanded）
- 避免使用内联样式
- 颜色值使用 CSS 变量

合规示例:
  .data-table { }
  .data-table__header { }
  .data-table__row--selected { }

违规示例:
  .dataTable { } ❌（应 kebab-case）
  .data_table { } ❌（应用连字符）
  style="color: #1890FF" ❌（应使用 CSS 变量）
```

### S8-05: 事件函数命名

**级别**: WARNING

```
规范要求:
- 事件处理函数: handle + 动作（如 handleSubmit、handleClick）
- 自定义事件: on + 动作（如 onUpdate、onChange）
- emit 事件名: kebab-case（如 'update:modelValue'）
- 计算属性: 名词或形容词（如 fullName、isValid）

合规示例:
  const handleSubmit = () => { ... }
  const handleRowClick = (row) => { ... }
  emit('update:model-value', newValue)

违规示例:
  const submit = () => { ... } ❌（缺少 handle 前缀）
  const clickRow = (row) => { ... } ❌
  emit('updateModelValue', newValue) ❌（应 kebab-case）
```

### S8-06: 页签命名

**级别**: WARNING

```
规范要求:
- 页签名称简洁，不超过 6 个汉字
- 使用业务术语而非技术术语
- 避免重复前缀

合规示例: "基础数据"、"账套管理"、"凭证处理"
违规示例: "基础数据管理页面" ❌（超过 6 个汉字）、"Tab1" ❌（使用技术术语）
```

### S8-07: 流程节点命名

**级别**: WARNING

```
规范要求:
- 流程节点使用动宾结构
- 状态值使用英文常量
- 节点名称不超过 8 个汉字

合规示例: "提交审核"、"部门审批"、"财务复核"
违规示例: "审核" ❌（缺少动词）、"提交申请并进行部门审核" ❌（过长）
```

### S8-08: Props 类型命名

**级别**: ERROR

```
规范要求:
- Props 接口以组件名 + Props 后缀命名
- Emits 类型以组件名 + Emits 后缀命名
- 导出类型放在 types.ts 中

合规示例:
  // DataTable/types.ts
  export interface DataTableProps { ... }
  export interface DataTableEmits { ... }

违规示例:
  export interface Props { ... } ❌（缺少组件名前缀）
  export interface TableProps { ... } ❌（应与组件名完全一致）
```

---

## 检查流程

### Phase 1: 确定检查范围
1. 确定要检查的文件或目录
2. 确定要检查的规则类别（组件/文件/变量/CSS/全部）

### Phase 2: 逐项检查
按 S8-01 至 S8-08 的规则逐项检查

### Phase 3: 生成检查报告
输出合规项、违规项、修复建议

### Phase 4: 辅助修复
对于可自动修复的项（如文件重命名），提供批量修复方案

---

## 安全约束

1. **文件重命名需谨慎**：重命名文件后必须同步更新所有 import 路径
2. **组件重命名需全局更新**：修改组件名需同步更新所有使用该组件的模板引用
3. **用户确认机制**：批量重命名前必须生成计划，等待用户确认

---

## 文件索引

### 规则文件
| 文件 | 说明 |
|------|------|
| [scripts/check-rules.md](scripts/check-rules.md) | 命名规范检查规则（8 条规则的详细定义） |
