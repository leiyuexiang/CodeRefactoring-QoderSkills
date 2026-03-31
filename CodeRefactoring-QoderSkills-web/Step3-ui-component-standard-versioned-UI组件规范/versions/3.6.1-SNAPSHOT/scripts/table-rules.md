# 表格规范检查规则

> 检查列定义、对齐方式、冻结列、合计行等是否符合规范。

## 规则清单

### RULE-TB01: 行号列检查

**级别**: ERROR  
**描述**: 表格第一列必须为行号列，冻结在左侧。

```
检查项:
- 第一列 key 为 'rowIndex' 或类似标识
- 设置 fixed: 'left'
- 设置 align: 'center'
- 行号连续计算（切换页数后继续计数）
```

### RULE-TB02: 复选框列检查

**级别**: WARNING  
**描述**: 多选表格的复选框列必须冻结在左侧第二列。

```
检查方式:
1. 扫描 row-selection 配置
2. 检查 fixed: true 设置
3. 复选框列应在行号列之后
```

### RULE-TB03: 操作列检查

**级别**: WARNING  
**描述**: 操作列应冻结在表格右侧最后一列。

```
检查方式:
1. 扫描 key='action' 的列
2. 检查 fixed: 'right'
3. 确认在列定义的最后位置
```

### RULE-TB04: 冻结列数量检查

**级别**: WARNING  
**描述**: 冻结列每侧不超过 3 列。

```
检查方式:
1. 统计 fixed: 'left' 的列数（不含行号和复选框）
2. 统计 fixed: 'right' 的列数（不含操作列）
3. 每侧不超过 3 列

建议: 冻结区不超过表格宽度的 1/3
```

### RULE-TB05: 斑马纹检查

**级别**: ERROR  
**描述**: 表格行必须使用斑马纹隔行换色。

```
规范:
- 奇数行: 白色背景 (#FFFFFF)
- 偶数行: 浅灰背景 (#FAFAFA)

检查方式:
1. 扫描 row-class-name 属性
2. 检查是否实现了斑马纹逻辑
3. 检查对应的 CSS 样式
```

### RULE-TB06: 标题行样式检查

**级别**: ERROR  
**描述**: 标题行必须加粗居中。

```
检查方式:
1. 扫描 .ant-table-thead 的样式
2. 检查 font-weight 是否为 600/bold
3. 检查 text-align 是否为 center
```

### RULE-TB07: 内容对齐检查

**级别**: ERROR  
**描述**: 不同类型的列内容对齐方式必须符合规范。

```
规范:
- 文字列: align: 'left'
- 数值/金额列: align: 'right'
- 状态/日期列: align: 'center'
- 行号列: align: 'center'

检查方式:
1. 遍历所有列定义
2. 根据 dataType 或列名判断类型
3. 检查 align 属性是否正确
```

### RULE-TB08: 合计行检查

**级别**: WARNING  
**描述**: 表格合计行位置和内容必须符合规范。

```
规范:
- 合计行在表格下方
- 选中合计行在合计行上方
- 选中合计格式: "选中合计(N)"
- 合计为全量数据合计，非当前页合计

检查方式:
1. 扫描 #summary 模板
2. 检查合计行位置（summary-row 在 selected-summary 下方）
3. 检查合计数据源（是否为全量）
```

### RULE-TB09: 超链接列样式检查

**级别**: WARNING  
**描述**: 超链接列文字带下划线，悬浮变蓝色。

```
规范:
- 默认: text-decoration: underline; color: #434343
- 悬浮: color: #1890FF

检查方式:
1. 扫描表格中的 <a> 标签
2. 检查是否设置了下划线和悬浮色
```

### RULE-TB10: 列宽合理性检查

**级别**: SUGGESTION  
**描述**: 表格列宽出厂默认值应适当。

```
建议:
- 行号列: 60px
- 复选框列: 50px
- 日期列: 120px
- 状态列: 80-100px
- 金额列: 130-150px
- 操作列: 根据按钮数量 80-200px
- 名称/描述: 150-250px
- 避免过窄（<60px）或过宽（>400px）

检查方式:
1. 扫描列定义的 width 属性
2. 检查是否在合理范围内
```

### RULE-TB11: 列筛选排序图标检查

**级别**: SUGGESTION  
**描述**: 筛选和排序图标的位置应符合规范。

```
规范:
- 图标靠右排列
- 两个图标同时存在时：筛选在右，排序在左

检查方式:
1. 扫描同时设置 sorter 和 filters 的列
2. Ant Design 默认布局已符合此规范
```

## 检查脚本伪代码

```javascript
function checkTableRules(files) {
  const results = [];

  for (const file of files) {
    const content = readFile(file);
    const columns = extractColumnDefinitions(content);

    if (columns.length === 0) continue;

    // RULE-TB01: 行号列
    if (columns[0]?.key !== 'rowIndex') {
      results.push({ rule: 'RULE-TB01', level: 'ERROR', file, message: '表格第一列应为行号列' });
    } else if (columns[0]?.fixed !== 'left') {
      results.push({ rule: 'RULE-TB01', level: 'ERROR', file, message: '行号列应冻结在左侧(fixed: "left")' });
    }

    // RULE-TB04: 冻结列数量
    const leftFrozen = columns.filter(c => c.fixed === 'left' && c.key !== 'rowIndex').length;
    const rightFrozen = columns.filter(c => c.fixed === 'right' && c.key !== 'action').length;
    if (leftFrozen > 3) {
      results.push({ rule: 'RULE-TB04', level: 'WARNING', file, message: `左侧冻结列 ${leftFrozen} 个，建议不超过3个` });
    }
    if (rightFrozen > 3) {
      results.push({ rule: 'RULE-TB04', level: 'WARNING', file, message: `右侧冻结列 ${rightFrozen} 个，建议不超过3个` });
    }

    // RULE-TB05: 斑马纹
    if (!content.includes('row-class-name') && !content.includes('rowClassName')) {
      results.push({ rule: 'RULE-TB05', level: 'ERROR', file, message: '表格未设置斑马纹(row-class-name)' });
    }

    // RULE-TB07: 对齐检查
    for (const col of columns) {
      if (isAmountColumn(col) && col.align !== 'right') {
        results.push({ rule: 'RULE-TB07', level: 'ERROR', file, message: `金额列 "${col.title}" 应右对齐` });
      }
    }
  }
  return results;
}
```
