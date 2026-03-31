# 命名规范检查规则

> 检查组件、文件、变量命名是否符合阿里前端开发规范。

## 规则清单

### RULE-N01: 组件命名检查

**级别**: ERROR  
**描述**: Vue 组件必须使用 PascalCase 命名。

```
规范:
- 组件名: PascalCase (如 QueryPanel、DataTable)
- 组件注册: PascalCase
- 模板使用: PascalCase 或 kebab-case 均可

检查方式:
1. 扫描 defineComponent 或 <script setup> 中的组件名
2. 扫描组件文件名（应为 PascalCase.vue 或 kebab-case.vue）
3. 检查是否有小写开头或含下划线的组件名

合规示例: QueryPanel.vue, DataTableCard.vue
违规示例: queryPanel.vue, data_table.vue
```

### RULE-N02: 文件命名检查

**级别**: ERROR  
**描述**: 文件名必须使用 kebab-case。

```
规范:
- Vue 组件文件: PascalCase.vue 或 kebab-case.vue
- TS/JS 工具文件: kebab-case.ts
- 样式文件: kebab-case.scss/less
- 目录名: kebab-case

检查方式:
1. 扫描项目文件名
2. 检查是否含有大写字母（Vue组件除外）或下划线
3. 检查目录名是否为 kebab-case

合规示例: use-table-data.ts, format-amount.ts
违规示例: useTableData.ts, format_amount.ts
```

### RULE-N03: 变量/函数命名检查

**级别**: ERROR  
**描述**: 变量和函数必须使用 camelCase 命名。

```
规范:
- 变量: camelCase (如 pageSize, selectedKeys)
- 函数: camelCase (如 handleSubmit, formatAmount)
- 常量: UPPER_SNAKE_CASE (如 MAX_PAGE_SIZE)
- 接口/类型: PascalCase (如 QueryField, TableColumn)
- 枚举: PascalCase，枚举值 UPPER_SNAKE_CASE

检查方式:
1. 扫描 const/let/var 声明
2. 扫描 function 声明
3. 检查命名风格

合规示例: const selectedRowKeys = ref([])
违规示例: const selected_row_keys = ref([])
```

### RULE-N04: CSS 类名检查

**级别**: WARNING  
**描述**: CSS 类名应使用 BEM 命名规范或 CSS Modules。

```
BEM 规范:
- Block: .operation-bar
- Element: .operation-bar__left
- Modifier: .operation-bar--active

检查方式:
1. 扫描 <style> 中的类名
2. 检查是否遵循 BEM 格式
3. 或使用 scoped + CSS Modules

合规示例: .query-panel__header, .data-card--collapsed
违规示例: .queryPanelHeader, .dataCardCollapsed
```

### RULE-N05: 事件处理函数命名检查

**级别**: SUGGESTION  
**描述**: 事件处理函数应使用 handle 前缀。

```
规范:
- 组件内部处理: handleXxx (如 handleSubmit, handleTabChange)
- Emit 事件: 动词形式 (如 update:value, change)
- 工具函数: 描述性动词 (如 formatAmount, parseDate)

检查方式:
1. 扫描 @click、@change 等事件绑定的函数名
2. 检查是否以 handle 开头

合规示例: @click="handleDelete"
违规示例: @click="deleteRow"（不够明确是事件处理）
```

### RULE-N06: 页签命名检查

**级别**: WARNING  
**描述**: 页签命名应统一遵循规范格式。

```
规范:
- 统一格式: 待XX、已XX、被XX
- 示例: 待送审、已送审、待审核、已审核、待发送、已发送、被退回
- 全部数据: "全部"
- 数据驱动录入第一个: "录入"

检查方式:
1. 扫描 a-tabs / a-tab-pane 的 tab 属性
2. 检查页签文本是否遵循 待/已/被 格式
3. 数量显示格式: 半角括号，红色数字
```

### RULE-N07: 流程节点命名检查

**级别**: SUGGESTION  
**描述**: 菜单和流程节点命名应符合规范。

```
规范:
- 菜单名称: 业务单据 + 操作
  合规: 用款计划录入、用款计划审核
  违规: 科室录入用款计划

- 流程节点名称: 岗位名称（不带业务数据名称）
  合规: 单位录入、单位审核、业务处室初审
  违规: 用款计划单位录入

检查方式:
1. 扫描菜单配置和流程节点配置
2. 检查命名格式是否合规
```

### RULE-N08: Props 类型定义检查

**级别**: ERROR  
**描述**: 组件 Props 必须定义 TypeScript 类型。

```
检查方式:
1. 扫描 defineProps 调用
2. 检查是否使用了泛型参数或 PropType
3. 不允许使用 any 类型的 Props

合规示例:
defineProps<{ title: string; count?: number }>()

违规示例:
defineProps(['title', 'count'])  // 数组形式，缺少类型
```

## 检查脚本伪代码

```javascript
function checkNamingRules(files) {
  const results = [];

  for (const file of files) {
    const content = readFile(file);
    const fileName = getFileName(file);

    // RULE-N02: 文件名
    if (file.endsWith('.ts') || file.endsWith('.js')) {
      if (!isKebabCase(fileName.replace(/\.\w+$/, ''))) {
        results.push({
          rule: 'RULE-N02',
          level: 'ERROR',
          file,
          message: `文件名 "${fileName}" 不符合 kebab-case 规范`,
        });
      }
    }

    // RULE-N03: 变量命名
    const declarations = extractDeclarations(content);
    for (const decl of declarations) {
      if (decl.kind === 'const' && isUpperSnakeCase(decl.name)) continue; // 常量允许
      if (decl.kind === 'interface' && isPascalCase(decl.name)) continue; // 接口允许
      if (!isCamelCase(decl.name) && !isPascalCase(decl.name)) {
        results.push({
          rule: 'RULE-N03',
          level: 'ERROR',
          file,
          message: `变量 "${decl.name}" 不符合 camelCase 命名规范`,
        });
      }
    }

    // RULE-N08: Props 类型
    if (content.includes('defineProps') && !content.includes('defineProps<')) {
      if (content.includes("defineProps([") || content.includes("defineProps({")) {
        results.push({
          rule: 'RULE-N08',
          level: 'ERROR',
          file,
          message: 'defineProps 应使用 TypeScript 泛型定义类型',
        });
      }
    }
  }
  return results;
}
```
