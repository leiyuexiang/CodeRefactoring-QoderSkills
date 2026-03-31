# S6-S7 布局与交互规范规则（3.6.0-SNAPSHOT）

## 概述

本文档定义了前端页面布局与交互规范检查与代码生成的完整规则集。基于《一体化系统界面规范》文档，涵盖页面布局规范（S06，9 条规则）和交互行为规范（S07，8 条规则），以及配套的页面布局样例和页面级代码模板。

**本技能提供三大功能：**
1. **布局与交互规范检查**：扫描前端页面代码，检查布局和交互实现是否符合规范
2. **页面布局样例展示**：提供各类页面和容器的标准化示例代码
3. **页面模板生成**：基于页面级可复用模板快速生成符合规范的页面代码

## 使用场景

| 用户意图 | 触发关键词 | 执行功能 |
|---------|-----------|----------|
| 检查页面布局是否合规 | "页面布局规范"、"页面层级"、"弹窗规范"、"抽屉规范" | 功能一：布局检查 |
| 检查交互行为是否合规 | "交互规范"、"滚动条规范"、"悬浮反馈"、"提示消息" | 功能一：交互检查 |
| 查看页面布局样例 | "页面样例"、"布局样例"、"弹窗样例"、"卡片样例" | 功能二：样例展示 |
| 生成页面代码 | "生成页面"、"一级页面"、"全屏页面"、"抽屉页面" | 功能三：模板生成 |

---

## 检查规则总览

### S06 页面布局规范（9 条）

| 编号 | 检查项 | 级别 |
|------|--------|------|
| S06-01 | 页面层级规范 | ERROR |
| S06-02 | 面包屑规范 | WARNING |
| S06-03 | 外边框一致性 | WARNING |
| S06-04 | 弹窗尺寸规范 | WARNING |
| S06-05 | 抽屉尺寸规范 | WARNING |
| S06-06 | 操作区固定 | WARNING |
| S06-07 | 页签位置 | WARNING |
| S06-08 | 全屏覆盖规范 | ERROR |
| S06-09 | 卡片间距 | SUGGESTION |

**核心要点**：
```
页面层级规范:
  全屏页面 > 抽屉页面 > 模式对话框
  下一级页面类型 ≤ 上一级页面类型

面包屑规范:
  不超过十层
  长度不超过页面宽度 60%

弹窗尺寸:
  提示框/确认框：小尺寸
  单栏录入：480px 宽
  两栏录入：720px 宽

抽屉尺寸:
  详情抽屉：480px 宽
  录入抽屉：720px 宽
```

### S07 交互行为规范（8 条）

| 编号 | 检查项 | 级别 |
|------|--------|------|
| S07-01 | 滚动条统一 | WARNING |
| S07-02 | 菜单栏交互 | WARNING |
| S07-03 | 提示消息规范 | ERROR |
| S07-04 | 悬浮反馈 | SUGGESTION |
| S07-05 | 省略号处理 | WARNING |
| S07-06 | 自动锁定 | SUGGESTION |
| S07-07 | 金额单位 | WARNING |
| S07-08 | 导航栏记忆 | SUGGESTION |

**核心要点**：
```
滚动条:
  全局统一风格
  默认隐藏，鼠标移入显示，移出隐藏

提示消息:
  成功 → 绿色，自动消失（3秒）
  警告 → 黄色，需手动关闭或自动消失
  错误 → 红色，需手动关闭
  超长文本自动截断

悬浮反馈:
  按钮悬浮变色
  表格行悬浮高亮
  链接悬浮下划线
```

---

## 功能一：布局与交互规范检查流程

### Phase 1: 确定检查范围
1. 确定要检查的页面文件或目录
2. 确定要检查的规则类别（布局/交互/全部）

### Phase 2: 逐项检查
按选定的规则类别执行检查，详见 `scripts/` 目录中的规则文件：
- `scripts/layout-rules.md` → 页面布局规范检查
- `scripts/interaction-rules.md` → 交互行为规范检查

### Phase 3: 生成检查报告
输出合规项、违规项、修复建议

---

## 功能二：页面布局样例展示

### 可用样例列表

| 序号 | 样例文件 | 说明 |
|------|---------|------|
| E08 | [examples/page-layout-examples.md](examples/page-layout-examples.md) | 页面布局样例（一级页面、页签栏、导航栏、操作栏） |
| E09 | [examples/query-panel-examples.md](examples/query-panel-examples.md) | 查询面板样例（关闭态、展开态、条件设置、常用查询） |
| E10 | [examples/card-examples.md](examples/card-examples.md) | 卡片样例（详情卡片、录入卡片、列表卡片） |
| E11 | [examples/dialog-examples.md](examples/dialog-examples.md) | 对话框样例（提示框、确认框、录入框） |
| E12 | [examples/notification-examples.md](examples/notification-examples.md) | 提示消息样例（成功、警告、错误、超长自隐提示） |
| E13 | [examples/drawer-examples.md](examples/drawer-examples.md) | 抽屉页面样例（详情抽屉、录入抽屉、审核日志） |
| E14 | [examples/attachment-examples.md](examples/attachment-examples.md) | 附件管理样例（上传、查看、分类/无分类） |

---

## 功能三：页面模板生成

### 可用模板列表

| 序号 | 模板文件 | 说明 |
|------|---------|------|
| T01 | [templates/primary-page-template.md](templates/primary-page-template.md) | 一级页面模板（含页签栏、导航栏、操作栏、表格区） |
| T02 | [templates/fullscreen-page-template.md](templates/fullscreen-page-template.md) | 全屏二级页面模板（含面包屑、操作区、数据区） |
| T03 | [templates/drawer-page-template.md](templates/drawer-page-template.md) | 抽屉二级页面模板（含详情/录入两种模式） |
| T04 | [templates/modal-dialog-template.md](templates/modal-dialog-template.md) | 模式对话框模板（提示框、确认框、录入框） |

### 生成流程
1. 用户指定要生成的页面类型（一级页面/全屏二级/抽屉/对话框）
2. 从对应模板读取代码骨架
3. 参考对应样例调整细节
4. 使用 S06、S07 规则自检生成的代码

---

## 安全约束

1. **页面层级不可违反**：生成的页面必须遵循层级递减规则
2. **尺寸标准化**：弹窗和抽屉的宽度必须使用标准尺寸
3. **交互一致性**：所有交互行为必须与全局规范保持一致

---

## 文件索引

### 规则文件
| 文件 | 说明 |
|------|------|
| [scripts/layout-rules.md](scripts/layout-rules.md) | 页面布局规范检查规则（9 条） |
| [scripts/interaction-rules.md](scripts/interaction-rules.md) | 交互行为规范检查规则（8 条） |
