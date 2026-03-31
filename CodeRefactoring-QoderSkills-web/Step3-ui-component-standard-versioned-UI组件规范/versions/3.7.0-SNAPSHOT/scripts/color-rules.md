# 颜色规范检查规则

> 检查前端代码中颜色使用是否符合一体化系统界面规范。

## 规则清单

### RULE-C01: 品牌色使用检查

**级别**: ERROR  
**描述**: 主题色必须使用蓝色色系（Ant Design Blue），不允许自定义主题色。

```
检查项:
- 主按钮背景色应为品牌蓝（Ant Design primary）
- 不应出现自定义的非规范品牌色
```

### RULE-C02: 中性色合规检查

**级别**: ERROR  
**描述**: 文字、背景等中性色必须使用规范中定义的色值。

```
色值白名单:
- 标签文字: #595959
- 内容文字: #434343
- 占位提示/禁用文字: #BFBFBF
- 面包屑非当前页/分隔符: #8C8C8C
- 分割线: #F0F0F0
- 悬浮背景: #F5F5F5
- 禁用背景/斑马纹偶数行: #FAFAFA
- 选中背景: #E6F7FF

检查方式:
1. 扫描 CSS/SCSS/Less 文件中的 color、background-color、border-color 属性
2. 扫描 style 标签中的颜色值
3. 比对是否在白名单中
4. 允许 #FFFFFF（白色）和 #000000（黑色）
```

### RULE-C03: 功能色使用检查

**级别**: WARNING  
**描述**: 功能色（错误、警告、成功）使用应遵循规范。

```
检查项:
- 错误/必填星号: #FF4D4F
- 链接/选中/主题色: #1890FF
- 可编辑区域背景: #FEFFE6
- 当前页按钮背景: #1890FF
- 当前页按钮文字: #FFFFFF

违规示例:
- 使用 red/green/blue 等非精确色值
- 大面积使用非主题色的对比色
```

### RULE-C04: 颜色种类控制

**级别**: WARNING  
**描述**: 界面中颜色种类不能过多，对比色使用应节制。

```
检查方式:
1. 统计单个组件中使用的不同颜色值数量
2. 排除中性色后，功能色种类不应超过 5 种
3. 检查是否有大面积的非主题色使用
```

### RULE-C05: CSS 变量使用检查

**级别**: SUGGESTION  
**描述**: 建议通过 CSS 变量或主题 token 使用颜色，避免硬编码色值。

```
推荐:
- 使用 var(--ant-primary-color) 代替 #1890FF
- 使用 var(--ant-text-color) 代替 #434343
- 使用 CSS 变量便于后续主题切换

检查方式:
1. 扫描 CSS 中直接使用的十六进制颜色值
2. 建议替换为 CSS 变量引用
```

## 检查脚本伪代码

```javascript
/**
 * 颜色规范检查器
 * @param {string[]} files - 待检查的文件路径列表
 * @returns {CheckResult[]} 检查结果
 */
function checkColorRules(files) {
  const results = [];
  const allowedColors = [
    '#595959', '#434343', '#BFBFBF', '#8C8C8C',
    '#F0F0F0', '#F5F5F5', '#FAFAFA', '#E6F7FF',
    '#FF4D4F', '#1890FF', '#FEFFE6',
    '#FFFFFF', '#000000',
  ];

  for (const file of files) {
    const content = readFile(file);

    // 提取所有颜色值
    const colorRegex = /#[0-9A-Fa-f]{3,8}\b/g;
    const matches = content.matchAll(colorRegex);

    for (const match of matches) {
      const color = normalizeColor(match[0]);
      const line = getLineNumber(content, match.index);

      // RULE-C02: 中性色合规检查
      if (!allowedColors.includes(color)) {
        results.push({
          rule: 'RULE-C02',
          level: 'ERROR',
          file,
          line,
          message: `颜色值 ${color} 不在规范白名单中`,
          suggestion: `请使用规范中定义的颜色值，参考 reference.md`,
        });
      }

      // RULE-C05: CSS 变量检查
      if (!isInCssVariable(content, match.index)) {
        results.push({
          rule: 'RULE-C05',
          level: 'SUGGESTION',
          file,
          line,
          message: `建议使用 CSS 变量代替硬编码色值 ${color}`,
        });
      }
    }
  }

  return results;
}
```

## 快速参考

| 色值 | 用途 | 规则 |
|------|------|------|
| #595959 | 标签文字 | RULE-C02 |
| #434343 | 内容文字、当前页面包屑 | RULE-C02 |
| #BFBFBF | 占位提示、禁用文字 | RULE-C02 |
| #8C8C8C | 非当前页面包屑 | RULE-C02 |
| #F0F0F0 | 分割线、选项卡分割 | RULE-C02 |
| #F5F5F5 | 悬浮背景 | RULE-C02 |
| #FAFAFA | 禁用背景 | RULE-C02 |
| #E6F7FF | 选中背景、当前节点 | RULE-C02 |
| #FF4D4F | 错误信息、必填星号 | RULE-C03 |
| #1890FF | 链接、选中、主色 | RULE-C03 |
| #FEFFE6 | 可编辑区域背景 | RULE-C03 |
