# 按钮规范检查规则

> 检查按钮类型、数量、布局和交互是否符合规范。

## 规则清单

### RULE-B01: 主按钮数量检查

**级别**: ERROR  
**描述**: 一个页面/区域最多一个主按钮。

```
检查方式:
1. 在同一个 Vue 组件中扫描 a-button[type="primary"]
2. 排除嵌套在 a-dropdown、a-modal 等子组件中的
3. 同一操作栏中 type="primary" 的按钮不超过 1 个

违规示例:
<a-button type="primary">新增</a-button>
<a-button type="primary">保存</a-button>  <!-- 违规：两个主按钮 -->
```

### RULE-B02: 危险按钮数量检查

**级别**: WARNING  
**描述**: 危险按钮应慎用，一个页面不超过一个。

```
检查方式:
1. 扫描 a-button[danger] 的数量
2. 同一页面中不超过 1 个
```

### RULE-B03: 操作按钮数量检查

**级别**: WARNING  
**描述**: 可见操作按钮一般不超过 4 个，多余的放到更多按钮中。

```
检查方式:
1. 在 .operation-bar__left 或类似操作区内
2. 统计直接子级的 a-button 数量（排除 a-dropdown 内的）
3. 超过 4 个时检查是否有 a-dropdown 更多按钮

违规示例:
- 操作栏有 6 个直接可见按钮，没有更多按钮
```

### RULE-B04: 更多按钮面板宽度检查

**级别**: WARNING  
**描述**: 更多按钮浮动面板宽度不超过 200px。

```
检查方式:
1. 扫描 a-dropdown 下的 a-menu 组件
2. 检查是否设置了 max-width: 200px 或同等限制
3. 检查菜单项是否设置了超长省略
```

### RULE-B05: 按钮文本字号检查

**级别**: ERROR  
**描述**: 按钮文本使用 14px 字体。

```
检查方式:
1. 扫描 .ant-btn 的 font-size 覆盖
2. 如有覆盖，应为 14px
```

### RULE-B06: 按钮 loading 状态检查

**级别**: WARNING  
**描述**: 长时间操作的按钮应设置 loading 状态，防止二次触发。

```
检查方式:
1. 扫描主按钮和提交类按钮
2. 检查是否绑定了 :loading 属性
3. 检查点击事件处理中是否有 loading 状态管理

违规示例:
<a-button type="primary" @click="handleSubmit">提交</a-button>
<!-- 缺少 :loading 属性 -->
```

### RULE-B07: 二级页面按钮方向检查

**级别**: WARNING  
**描述**: 二级页面操作按钮重要性从右到左排列（与一级页面相反）。

```
检查方式:
1. 识别二级页面（含面包屑导航的页面）
2. 检查操作按钮区域
3. 主按钮应在最右侧
4. 返回按钮应在最左侧

规范布局（从左到右）:
一级页面: [主按钮] [普通按钮...] [更多]
二级页面: [返回] [开关] [普通按钮...] [更多] [主按钮]
```

### RULE-B08: 返回按钮检查

**级别**: ERROR  
**描述**: 二级页面默认都有返回按钮，排在操作按钮最左侧。

```
检查方式:
1. 识别二级页面组件
2. 检查操作区是否包含返回按钮
3. 返回按钮应在最左侧
```

## 检查脚本伪代码

```javascript
function checkButtonRules(files) {
  const results = [];

  for (const file of files) {
    const content = readFile(file);
    const template = extractTemplate(content);

    // RULE-B01: 主按钮数量
    const primaryButtons = findElements(template, 'a-button[type="primary"]');
    const operationBars = findElements(template, '.operation-bar, .operation-bar__left');
    for (const bar of operationBars) {
      const primaryInBar = primaryButtons.filter(b => isChildOf(b, bar));
      if (primaryInBar.length > 1) {
        results.push({
          rule: 'RULE-B01',
          level: 'ERROR',
          file,
          message: `操作栏中有 ${primaryInBar.length} 个主按钮，最多允许 1 个`,
        });
      }
    }

    // RULE-B03: 按钮数量
    for (const bar of operationBars) {
      const directButtons = getDirectChildButtons(bar);
      const hasDropdown = hasElement(bar, 'a-dropdown');
      if (directButtons.length > 4 && !hasDropdown) {
        results.push({
          rule: 'RULE-B03',
          level: 'WARNING',
          file,
          message: `操作栏有 ${directButtons.length} 个可见按钮，超过4个应使用更多按钮`,
        });
      }
    }

    // RULE-B06: loading 状态
    const submitButtons = findElements(template, 'a-button[type="primary"]');
    for (const btn of submitButtons) {
      if (!hasAttribute(btn, ':loading') && !hasAttribute(btn, 'v-bind:loading')) {
        results.push({
          rule: 'RULE-B06',
          level: 'WARNING',
          file,
          message: '主按钮缺少 :loading 属性，长时间操作可能被重复触发',
        });
      }
    }
  }
  return results;
}
```
