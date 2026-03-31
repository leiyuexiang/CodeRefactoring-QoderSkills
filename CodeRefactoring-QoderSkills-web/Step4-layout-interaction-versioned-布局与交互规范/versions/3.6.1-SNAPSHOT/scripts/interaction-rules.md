# 交互规范检查规则

> 检查滚动条、悬浮反馈、提示消息等交互行为是否符合规范。

## 规则清单

### RULE-X01: 滚动条样式检查

**级别**: ERROR  
**描述**: 全局滚动条样式必须统一，默认隐藏，鼠标移入显示。

```
规范:
- 默认隐藏滚动条
- 鼠标移入容器时显示滚动条
- 鼠标移出时隐藏
- 宽度/高度: 6px
- 圆角: 3px

检查方式:
1. 检查全局 CSS 中是否有 ::-webkit-scrollbar 定义
2. 检查是否设置 background: transparent 默认隐藏
3. 检查 :hover 时是否显示

参考代码:
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: transparent; border-radius: 3px; }
*:hover > ::-webkit-scrollbar-thumb { background: #c1c1c1; }
```

### RULE-X02: 菜单栏滚动检查

**级别**: ERROR  
**描述**: 菜单栏不能出现水平滚动条。

```
检查方式:
1. 扫描菜单栏容器
2. 检查 overflow-x: hidden
3. 超长菜单文字显示省略号，鼠标悬浮浮动显示全部
4. 菜单栏宽度默认 240px，不可拖动改变
```

### RULE-X03: 提示消息规范检查

**级别**: ERROR  
**描述**: 提示消息应使用全局自隐提示，不打断用户操作。

```
规范:
- 优先使用 message.success/warning/error
- 显示在上部中央
- 默认停留 3 秒
- 信息最多两行，超过用省略号
- 鼠标悬停时不自动隐藏

检查方式:
1. 扫描提示调用（message、notification、Modal.info 等）
2. 检查是否优先使用 message（非阻断）
3. 检查 duration 配置
4. 阻断式提示（Modal.confirm）仅用于必要场景
```

### RULE-X04: 悬浮反馈检查

**级别**: WARNING  
**描述**: 可交互元素应有悬浮反馈。

```
检查项:
- 按钮悬浮应有视觉反馈
- 超链接悬浮变蓝（#1890FF）
- 表格行悬浮有背景变化
- 树节点悬浮背景 #F5F5F5
- 非当前面包屑悬浮变蓝
- 菜单末级悬浮显示收藏按钮
- 下拉选项悬浮背景 #F5F5F5

检查方式:
1. 扫描可交互元素
2. 检查 :hover 伪类样式
3. 检查 Tooltip 是否覆盖需要的元素
```

### RULE-X05: 省略号与悬停展示检查

**级别**: WARNING  
**描述**: 超长文本应显示省略号，鼠标悬停显示完整内容。

```
适用场景:
- 菜单文字超过菜单栏宽度
- 标签文字超过 8 个汉字
- 查询条件值超过 200px
- 面包屑过长
- 更多按钮面板文字超长
- 表格单元格文字超长

检查方式:
1. 扫描设置了 max-width 或 width 限制的元素
2. 检查是否设置 overflow: hidden; text-overflow: ellipsis
3. 检查是否有 a-tooltip 或 title 属性显示完整内容
```

### RULE-X06: 自动锁定功能检查

**级别**: SUGGESTION  
**描述**: 系统应支持自动锁定和手工锁定功能。

```
规范:
- 用户可设置闲置锁定时间
- 系统默认时间（如 30 分钟）
- 可以改小，不可以改大
- 启用锁定后去掉前端登录超时

检查方式:
1. 扫描锁定相关配置
2. 检查超时设置是否可配置
```

### RULE-X07: 金额单位切换检查

**级别**: WARNING  
**描述**: 金额单位切换应符合规范。

```
规范:
- 支持：元、万元、亿元
- 可设置页面默认单位
- 对用户选择进行记忆
- 切换针对整个页面（包括二级页面）
- 二级页面跟随一级页面，不能变更
- 特殊设置的字段不受影响

检查方式:
1. 扫描金额单位切换组件
2. 检查是否有记忆逻辑
3. 检查二级页面是否继承一级页面设置
```

### RULE-X08: 导航栏记忆检查

**级别**: SUGGESTION  
**描述**: 导航栏的收缩/展开、宽度、默认导航项应支持记忆。

```
检查方式:
1. 扫描导航栏组件
2. 检查是否有 localStorage/sessionStorage 保存状态
3. 页面再次打开时是否恢复上次设置
```

## 检查脚本伪代码

```javascript
function checkInteractionRules(files) {
  const results = [];
  let hasGlobalScrollbarStyle = false;

  for (const file of files) {
    const content = readFile(file);

    // RULE-X01: 全局滚动条
    if (content.includes('::-webkit-scrollbar')) {
      hasGlobalScrollbarStyle = true;
      if (!content.includes('background: transparent') && !content.includes('background:transparent')) {
        results.push({
          rule: 'RULE-X01',
          level: 'ERROR',
          file,
          message: '滚动条未设置默认隐藏(background: transparent)',
        });
      }
    }

    // RULE-X03: 提示消息
    if (content.includes('Modal.info') || content.includes('Modal.success')) {
      results.push({
        rule: 'RULE-X03',
        level: 'WARNING',
        file,
        message: '建议使用 message 全局自隐提示代替 Modal 阻断式提示',
      });
    }

    // RULE-X05: 省略号
    const maxWidthElements = findElementsWithStyle(content, 'max-width');
    for (const el of maxWidthElements) {
      if (!hasStyle(el, 'text-overflow: ellipsis') && !hasStyle(el, 'overflow: hidden')) {
        results.push({
          rule: 'RULE-X05',
          level: 'WARNING',
          file,
          message: '设置了 max-width 的元素建议添加省略号处理',
        });
      }
    }
  }

  // 全局检查
  if (!hasGlobalScrollbarStyle) {
    results.push({
      rule: 'RULE-X01',
      level: 'ERROR',
      file: 'global',
      message: '未找到全局滚动条样式定义',
    });
  }

  return results;
}
```
