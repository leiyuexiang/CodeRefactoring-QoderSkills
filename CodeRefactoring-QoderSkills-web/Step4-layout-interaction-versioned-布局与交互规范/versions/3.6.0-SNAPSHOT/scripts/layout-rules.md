# 页面布局规范检查规则

> 检查页面层级、尺寸、间距、容器嵌套是否符合规范。

## 规则清单

### RULE-L01: 页面层级关系检查

**级别**: ERROR  
**描述**: 下一级页面类型必须小于等于上一级页面类型。

```
类型权重: 全屏页面(3) > 抽屉页面(2) > 模式对话框(1)

检查规则:
- 全屏页面可打开：全屏、抽屉、对话框
- 抽屉页面可打开：抽屉、对话框
- 模式对话框不建议再打开其他页面

检查方式:
1. 分析组件调用链
2. 检查页面组件中是否有违规的子页面引用
```

### RULE-L02: 面包屑层级检查

**级别**: WARNING  
**描述**: 面包屑导航层数不超过十层，长度不超过页面宽度 60%。

```
检查方式:
1. 扫描面包屑组件的数据源
2. 检查最大层级限制
3. 检查是否有折叠处理逻辑
4. 检查容器的 max-width: 60%
```

### RULE-L03: 功能区外边框检查

**级别**: WARNING  
**描述**: 业务功能区应设置整体外边框，与系统框架区分。

```
检查方式:
1. 扫描一级页面根容器
2. 检查是否设置 border 样式
3. 功能区大小应计算准确，不出现多余滚动条
```

### RULE-L04: 弹窗尺寸检查

**级别**: ERROR  
**描述**: 弹窗的尺寸必须符合规范。

```
规范:
- 提示框: ~400px
- 确认框: ~420px
- 单栏录入框: 440px，高度 280-720px
- 两栏录入框: 856px，高度 520-720px
- 弹窗宽高比 > 0.6

检查方式:
1. 扫描 a-modal 的 :width 属性
2. 根据用途判断尺寸是否合规
3. 检查是否设置了 centered 属性
```

### RULE-L05: 抽屉尺寸检查

**级别**: WARNING  
**描述**: 抽屉页面尺寸和属性必须符合规范。

```
规范:
- 一列抽屉: ~480px
- 两列抽屉: ~720px
- 总宽度不超过屏幕一半
- 详情抽屉: mask=false
- 录入抽屉: mask=true, mask-closable=false

检查方式:
1. 扫描 a-drawer 组件
2. 检查 :width 属性
3. 检查 :mask 和 :mask-closable 属性
4. 不应出现横向滚动条
```

### RULE-L06: 操作区固定检查

**级别**: ERROR  
**描述**: 数据区滚动时操作区必须保持固定。

```
检查方式:
1. 扫描页面布局结构
2. 检查操作区是否有 flex-shrink: 0
3. 检查数据区是否有 overflow-y: auto
4. 确认滚动不影响操作区
```

### RULE-L07: 页签栏位置检查

**级别**: WARNING  
**描述**: 页签栏首个页签的左边距应与操作区首个按钮对齐。

```
检查方式:
1. 扫描页签栏和操作栏的 padding-left 值
2. 两者应相等
3. 无导航栏时页签和按钮左对齐
```

### RULE-L08: 全屏页面覆盖检查

**级别**: ERROR  
**描述**: 全屏二级页面应覆盖全部功能区域。

```
检查方式:
1. 二级全屏页面应覆盖一级页面的页签栏
2. 与平台框架保持相同外边框
3. 检查是否设置 height: 100% 或同等撑满逻辑
```

### RULE-L09: 卡片间距检查

**级别**: SUGGESTION  
**描述**: 数据卡片之间应有合理间距。

```
检查方式:
1. 扫描 .data-card 或类似卡片的 margin-bottom
2. 建议使用 16px 间距
3. 操作区和数据区之间用分割线分隔
```

## 检查脚本伪代码

```javascript
function checkLayoutRules(files) {
  const results = [];

  for (const file of files) {
    const content = readFile(file);

    // RULE-L04: 弹窗尺寸
    const modals = findElements(content, 'a-modal');
    for (const modal of modals) {
      const width = getAttributeValue(modal, 'width') || getAttributeValue(modal, ':width');
      if (width) {
        const w = parseInt(width);
        // 检查是否为规范尺寸
        if (![400, 420, 440, 856].includes(w) && w < 400) {
          results.push({
            rule: 'RULE-L04',
            level: 'ERROR',
            file,
            message: `弹窗宽度 ${w}px 不符合规范（400/420/440/856）`,
          });
        }
      }
      if (!hasAttribute(modal, 'centered')) {
        results.push({
          rule: 'RULE-L04',
          level: 'WARNING',
          file,
          message: '弹窗建议设置 centered 属性居中显示',
        });
      }
    }

    // RULE-L05: 抽屉
    const drawers = findElements(content, 'a-drawer');
    for (const drawer of drawers) {
      const width = parseInt(getAttributeValue(drawer, ':width') || '0');
      if (width > 960) {
        results.push({
          rule: 'RULE-L05',
          level: 'WARNING',
          file,
          message: `抽屉宽度 ${width}px 过大，不应超过屏幕一半`,
        });
      }
    }

    // RULE-L06: 操作区固定
    if (isSecondaryPage(content)) {
      const hasFixedHeader = content.includes('flex-shrink: 0') || content.includes('flex-shrink:0');
      const hasScrollBody = content.includes('overflow-y: auto') || content.includes('overflow-y:auto');
      if (!hasFixedHeader || !hasScrollBody) {
        results.push({
          rule: 'RULE-L06',
          level: 'ERROR',
          file,
          message: '二级页面操作区未固定或数据区未设置滚动',
        });
      }
    }
  }
  return results;
}
```
