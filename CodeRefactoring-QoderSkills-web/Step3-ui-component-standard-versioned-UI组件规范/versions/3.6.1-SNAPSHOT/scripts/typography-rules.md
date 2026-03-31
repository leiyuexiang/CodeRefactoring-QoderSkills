# 字体排版规范检查规则

> 检查前端代码中字号、字重、行高是否符合规范。

## 规则清单

### RULE-T01: 字号规范检查

**级别**: ERROR  
**描述**: 界面元素字号必须使用规范定义的三级字号。

```
规范字号:
- 16px: 卡片标题
- 14px: 控件标签、按钮文本、正文内容、录入框内容
- 12px: 提示文字、辅助内容、录入框下方提示

检查方式:
1. 扫描 CSS 中的 font-size 属性
2. 检查值是否为 12px、14px、16px 之一
3. 允许 Ant Design 组件库默认字号

违规示例:
- font-size: 13px  // 非规范字号
- font-size: 18px  // 卡片标题应为16px
```

### RULE-T02: 字重规范检查

**级别**: WARNING  
**描述**: 字重使用应符合规范。

```
规范字重:
- 标题行/卡片标题/选中选项卡: font-weight: 600 (bold)
- 正文/标签/其他: font-weight: 400 (normal)

检查项:
- 表格标题行应加粗（font-weight: 600）
- 卡片标题应加粗
- 选中选项卡文字应加粗
- 其他文字不应使用非 400/600 的字重
```

### RULE-T03: 标签宽度检查

**级别**: WARNING  
**描述**: 表单标签最大宽度不超过 8 个汉字。

```
检查项:
- label-col 的 width 不应超过 128px（8个汉字 x 16px）
- 推荐使用 112px（8个汉字 x 14px）
- 标签超长时应使用省略号，鼠标悬停显示全部

检查方式:
1. 扫描 a-form 组件的 label-col 配置
2. 扫描 .ant-form-item-label 的 width 样式
3. 检查是否超过 128px
```

### RULE-T04: 文字对齐规范检查

**级别**: ERROR  
**描述**: 不同类型内容的对齐方式必须符合规范。

```
规范:
- 表格标题行: 居中 (text-align: center)
- 表格文字列: 左对齐 (text-align: left)
- 表格数值列: 右对齐 (text-align: right)
- 表格状态列: 居中 (text-align: center)
- 表单标签: 右对齐 (label-align: right)
- 按钮文字: 居中

检查方式:
1. 扫描表格列定义中的 align 属性
2. 根据列的 dataType 判断是否使用正确的对齐方式
3. 标题行是否设置了居中
```

### RULE-T05: 数值格式化检查

**级别**: ERROR  
**描述**: 金额和数值列必须按规范格式化显示。

```
规范:
- 金额列: 保留两位小数 + 千分分隔符 (如: 1,234,567.89)
- 账号: 每4位空格分隔 (如: 1234 5678 9012)
- 身份证号: 适当空格分隔

检查方式:
1. 扫描标记为金额类型的列/字段
2. 检查是否使用了 toLocaleString 或千分符格式化
3. 检查是否设置了正确的小数位数
```

## 检查脚本伪代码

```javascript
function checkTypographyRules(files) {
  const results = [];
  const allowedFontSizes = ['12px', '14px', '16px'];

  for (const file of files) {
    const content = readFile(file);

    // RULE-T01: 字号检查
    const fontSizeRegex = /font-size:\s*(\d+)px/g;
    for (const match of content.matchAll(fontSizeRegex)) {
      if (!allowedFontSizes.includes(`${match[1]}px`)) {
        results.push({
          rule: 'RULE-T01',
          level: 'ERROR',
          file,
          line: getLineNumber(content, match.index),
          message: `字号 ${match[1]}px 不符合规范，应为 12px/14px/16px`,
        });
      }
    }

    // RULE-T04: 表格对齐检查
    const columnDefs = extractColumnDefinitions(content);
    for (const col of columnDefs) {
      if (col.dataType === 'amount' && col.align !== 'right') {
        results.push({
          rule: 'RULE-T04',
          level: 'ERROR',
          file,
          message: `金额列 "${col.title}" 应设置右对齐(align: 'right')`,
        });
      }
      if (col.dataType === 'status' && col.align !== 'center') {
        results.push({
          rule: 'RULE-T04',
          level: 'ERROR',
          file,
          message: `状态列 "${col.title}" 应设置居中(align: 'center')`,
        });
      }
    }

    // RULE-T05: 金额格式化检查
    if (hasAmountColumns(content) && !hasAmountFormatter(content)) {
      results.push({
        rule: 'RULE-T05',
        level: 'ERROR',
        file,
        message: '金额列缺少千分符格式化和两位小数设置',
      });
    }
  }
  return results;
}
```

## 快速参考

| 元素 | 字号 | 字重 | 对齐 |
|------|------|------|------|
| 卡片标题 | 16px | 600 | - |
| 表格标题 | 14px | 600 | 居中 |
| 控件标签 | 14px | 400 | 右对齐 |
| 按钮文本 | 14px | 400 | 居中 |
| 正文内容 | 14px | 400 | 左对齐 |
| 金额列 | 14px | 400 | 右对齐 |
| 状态列 | 14px | 400 | 居中 |
| 提示文字 | 12px | 400 | - |
| 选中选项卡 | 14px | 600 | - |
