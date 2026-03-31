# 录入控件规范检查规则

> 检查必填标记、占位提示、禁用态、下拉选项等是否符合规范。

## 规则清单

### RULE-I01: 必填项标记检查

**级别**: ERROR  
**描述**: 必填项的标签前必须加红色星号。

```
检查方式:
1. 扫描表单验证规则中 required: true 的字段
2. 检查对应 a-form-item 是否设置 :required="true" 或 required
3. 必填星号颜色必须为 #FF4D4F
4. 星号只出现在标签前，不改变录入框背景色

违规示例:
- 验证规则中有 required 但 form-item 未标记
- 星号颜色不是 #FF4D4F
```

### RULE-I02: 占位提示检查

**级别**: WARNING  
**描述**: 所有录入控件必须设置输入提示。

```
检查方式:
1. 扫描所有 a-input、a-select、a-date-picker 等组件
2. 检查是否设置 placeholder 属性
3. 提示文字颜色应为 #BFBFBF

推荐提示格式:
- 输入框: "请输入XXX"
- 选择框: "请选择XXX"
- 日期框: "请选择日期"
```

### RULE-I03: 禁用态样式检查

**级别**: ERROR  
**描述**: 禁用控件的背景色和文字颜色必须符合规范。

```
规范:
- 禁用背景色: #FAFAFA
- 禁用文字颜色: #BFBFBF

检查方式:
1. 扫描 disabled 属性的控件
2. 检查是否有对应的禁用态样式覆盖
3. 或确认使用 Ant Design 默认禁用样式
```

### RULE-I04: 下拉框规范检查

**级别**: WARNING  
**描述**: 下拉框的选项高度、边距、悬浮/选中背景必须符合规范。

```
规范:
- 选项高度: 32px
- 左右边距: 8px
- 文字边距: 8px
- 悬浮背景: #F5F5F5
- 选中背景: #E6F7FF
- 图标大小: 16px（下拉/删除图标）

检查方式:
1. 扫描 a-select 组件的样式覆盖
2. 检查 dropdown 相关样式配置
3. 检查 allow-clear 属性是否设置
```

### RULE-I05: 录入框清除功能检查

**级别**: SUGGESTION  
**描述**: 下拉框、日期框等应支持清除功能。

```
检查方式:
1. 扫描 a-select、a-date-picker、a-tree-select 组件
2. 检查是否设置 allow-clear 属性

违规示例:
<a-select v-model:value="data">  <!-- 缺少 allow-clear -->
```

### RULE-I06: 标签布局检查

**级别**: WARNING  
**描述**: 标签和录入框的布局关系必须符合规范。

```
规范:
- 标签在左，录入框在右（左右结构）
- 或标签在上，录入框在下（上下结构，推荐）
- 标签最大宽度 8 个汉字
- 标签文字颜色: #595959
- 控件内容颜色: #434343

检查方式:
1. 扫描 a-form 的 layout 属性
2. 检查 label-col 的 width 不超过 128px
3. 检查标签和内容的颜色设置
```

### RULE-I07: 表格可编辑区域检查

**级别**: ERROR  
**描述**: 可编辑表格区域必须按规范标识。

```
规范:
- 部分可编辑: 可编辑单元格背景色 #FEFFE6
- 全部可编辑: 保持白色背景
- 必填列标题加红色 *
- 部分必填单元格左上角红色角标
- 编辑框不设外边距，充满单元格

检查方式:
1. 扫描可编辑表格组件
2. 检查是否正确设置了编辑区域背景色
3. 检查必填列标识
```

### RULE-I08: 查询面板控件布局检查

**级别**: ERROR  
**描述**: 查询面板中控件布局必须符合规范。

```
规范:
- 标签在控件左侧
- 一行放置三个控件（:span="8"）
- 日期范围/金额范围封装为一个控件
- 快捷查询条件不超过 3 个

检查方式:
1. 扫描查询面板组件
2. 检查 a-col 的 span 属性是否为 8
3. 检查日期范围是否使用 a-range-picker
4. 检查快捷查询区字段数量
```

## 检查脚本伪代码

```javascript
function checkInputRules(files) {
  const results = [];

  for (const file of files) {
    const content = readFile(file);

    // RULE-I01: 必填项标记
    const formRules = extractFormRules(content);
    const formItems = extractFormItems(content);
    for (const [field, rules] of Object.entries(formRules)) {
      if (rules.some(r => r.required)) {
        const item = formItems.find(i => i.name === field);
        if (item && !item.required) {
          results.push({
            rule: 'RULE-I01',
            level: 'ERROR',
            file,
            message: `字段 "${field}" 在验证规则中为必填，但 form-item 未设置 required`,
          });
        }
      }
    }

    // RULE-I02: 占位提示
    const inputs = findElements(content, 'a-input, a-select, a-date-picker, a-tree-select');
    for (const input of inputs) {
      if (!hasAttribute(input, 'placeholder')) {
        results.push({
          rule: 'RULE-I02',
          level: 'WARNING',
          file,
          message: `录入控件缺少 placeholder 属性`,
        });
      }
    }

    // RULE-I05: 清除功能
    const clearableComponents = findElements(content, 'a-select, a-date-picker, a-tree-select');
    for (const comp of clearableComponents) {
      if (!hasAttribute(comp, 'allow-clear')) {
        results.push({
          rule: 'RULE-I05',
          level: 'SUGGESTION',
          file,
          message: `建议设置 allow-clear 属性以支持清除功能`,
        });
      }
    }
  }
  return results;
}
```
