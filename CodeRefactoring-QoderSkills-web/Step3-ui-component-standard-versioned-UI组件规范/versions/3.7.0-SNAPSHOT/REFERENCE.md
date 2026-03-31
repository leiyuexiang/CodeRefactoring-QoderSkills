# S1-S5 UI 组件规范规则（3.6.0-SNAPSHOT）

## 概述

本文档定义了前端 UI 组件规范检查与代码生成的完整规则集。基于《一体化系统界面规范》文档，涵盖颜色体系（S01）、字体排版（S02）、按钮规范（S03）、录入控件（S04）、表格规范（S05）共五大类检查规则，以及配套的组件样例和代码模板。

**本技能提供三大功能：**
1. **组件规范检查**：扫描前端代码，检查组件实现是否符合 UI 规范
2. **标准样例展示**：提供各类组件的标准化示例代码
3. **组件模板生成**：基于可复用模板快速生成符合规范的组件代码

## 使用场景

| 用户意图 | 触发关键词 | 执行功能 |
|---------|-----------|----------|
| 检查组件是否符合 UI 规范 | "颜色检查"、"字体规范"、"按钮规范"、"表格规范"、"录入规范" | 功能一：规范检查 |
| 查看组件标准样例 | "按钮样例"、"表格样例"、"录入框样例"、"组件样例" | 功能二：样例展示 |
| 生成符合规范的组件代码 | "生成表格"、"生成表单"、"生成查询面板" | 功能三：模板生成 |

---

## 检查规则总览

### S01 颜色规范（5 条）

| 编号 | 检查项 | 级别 |
|------|--------|------|
| S01-01 | 品牌色使用正确性 | ERROR |
| S01-02 | 中性色体系一致性 | WARNING |
| S01-03 | 功能色使用正确性 | ERROR |
| S01-04 | 颜色种类数量控制 | WARNING |
| S01-05 | CSS 变量使用 | SUGGESTION |

**核心色值表**：
```
品牌色：#1890FF（Ant Design Blue-6）
标签文字：#595959    内容文字：#434343    占位提示：#BFBFBF
错误信息/必填星号：#FF4D4F    成功：#52C41A    警告：#FAAD14
禁用背景：#FAFAFA    禁用文字：#BFBFBF
悬浮背景：#F5F5F5    选中背景：#E6F7FF
可编辑区域背景：#FEFFE6
分割线：#F0F0F0    面包屑非当前页：#8C8C8C
链接/选中文字：#1890FF
```

### S02 字体排版规范（5 条）

| 编号 | 检查项 | 级别 |
|------|--------|------|
| S02-01 | 字号使用正确性 | ERROR |
| S02-02 | 字重使用规范 | WARNING |
| S02-03 | 标签宽度限制 | WARNING |
| S02-04 | 文字对齐规则 | WARNING |
| S02-05 | 数值格式化 | WARNING |

**核心字号表**：
```
16px → 卡片标题
14px → 控件标签/按钮/正文
12px → 提示文字/辅助内容
```

### S03 按钮规范（8 条）

| 编号 | 检查项 | 级别 |
|------|--------|------|
| S03-01 | 主按钮数量（≤1） | ERROR |
| S03-02 | 危险按钮数量（≤1） | WARNING |
| S03-03 | 按钮总数控制 | WARNING |
| S03-04 | 更多面板宽度（≤200px） | WARNING |
| S03-05 | 按钮文本字号（14px） | ERROR |
| S03-06 | loading 状态 | WARNING |
| S03-07 | 二级页面按钮方向 | WARNING |
| S03-08 | 返回按钮位置 | WARNING |

**核心要点**：
- 一个页面最多一个主按钮（蓝色背景）
- 若干普通按钮（灰色边框）
- 危险按钮慎用，不超过 1 个
- 操作按钮超过 4 个时使用"更多"按钮
- 更多面板宽度不超过 200px

### S04 录入控件规范（8 条）

| 编号 | 检查项 | 级别 |
|------|--------|------|
| S04-01 | 必填标记（红色 *） | ERROR |
| S04-02 | 占位提示文案 | WARNING |
| S04-03 | 禁用态样式 | WARNING |
| S04-04 | 下拉框规范 | WARNING |
| S04-05 | 清除功能 | SUGGESTION |
| S04-06 | 标签布局方式 | WARNING |
| S04-07 | 可编辑区域 | WARNING |
| S04-08 | 查询面板规范 | WARNING |

**核心要点**：
- 必填项标签前加红色星号 `*`
- 标签和控件推荐上下结构
- 标签最大宽度 8 个汉字，超过省略
- 查询面板一行放置三个控件
- 快捷查询条件不超过 3 个

### S05 表格规范（11 条）

| 编号 | 检查项 | 级别 |
|------|--------|------|
| S05-01 | 行号列 | ERROR |
| S05-02 | 复选框列 | WARNING |
| S05-03 | 操作列 | WARNING |
| S05-04 | 冻结列数（≤3） | WARNING |
| S05-05 | 斑马纹 | WARNING |
| S05-06 | 标题行样式 | WARNING |
| S05-07 | 内容对齐规则 | WARNING |
| S05-08 | 合计行 | WARNING |
| S05-09 | 超链接样式 | WARNING |
| S05-10 | 列宽设置 | SUGGESTION |
| S05-11 | 筛选排序 | SUGGESTION |

**核心要点**：
- 第一列：行号列（冻结左侧）
- 最后一列：操作列（冻结右侧）
- 斑马纹：奇数行白色，偶数行浅灰色
- 标题行加粗居中
- 内容行：文字左对齐，数值右对齐，状态居中
- 数值列加千分分隔符，金额默认保留两位小数
- 合计行在表格下方，选中合计在合计行上方
- 冻结列每侧不超过 3 列

---

## 功能一：组件规范检查流程

### Phase 1: 确定检查范围
1. 确定要检查的组件文件或目录
2. 确定要检查的规则类别（颜色/字体/按钮/录入/表格/全部）

### Phase 2: 逐项检查
按选定的规则类别执行检查，详见 `scripts/` 目录中的规则文件：
- `scripts/color-rules.md` → 颜色规范检查
- `scripts/typography-rules.md` → 字体排版检查
- `scripts/button-rules.md` → 按钮规范检查
- `scripts/input-rules.md` → 录入控件检查
- `scripts/table-rules.md` → 表格规范检查

### Phase 3: 生成检查报告
输出合规项、违规项、修复建议

---

## 功能二：标准样例展示

### 可用样例列表

| 序号 | 样例文件 | 说明 |
|------|---------|------|
| E01 | [examples/button-examples.md](examples/button-examples.md) | 按钮组件样例（主按钮、次按钮、危险按钮、更多按钮） |
| E02 | [examples/input-examples.md](examples/input-examples.md) | 录入框样例（文本框、下拉框、日期框、复选框、单选框） |
| E03 | [examples/table-examples.md](examples/table-examples.md) | 表格样例（普通表格、可编辑表格、表头设置） |
| E04 | [examples/pagination-examples.md](examples/pagination-examples.md) | 分页样例（极简、简单、中等、标准四种模式） |
| E05 | [examples/tree-examples.md](examples/tree-examples.md) | 树形控件样例（单选、多选、带搜索） |
| E06 | [examples/tabs-examples.md](examples/tabs-examples.md) | 选项卡样例（横向排列、纵向排列） |
| E07 | [examples/breadcrumb-examples.md](examples/breadcrumb-examples.md) | 面包屑样例（标准、带图标、折叠） |

---

## 功能三：组件模板生成

### 可用模板列表

| 序号 | 模板文件 | 说明 |
|------|---------|------|
| T05 | [templates/query-panel-template.md](templates/query-panel-template.md) | 查询面板模板（含查询条件、常用查询、条件设置） |
| T06 | [templates/data-table-template.md](templates/data-table-template.md) | 数据表格模板（含排序、筛选、分页、合计、导出） |
| T07 | [templates/form-card-template.md](templates/form-card-template.md) | 录入卡片模板（含表单校验、必填项标记） |
| T08 | [templates/detail-card-template.md](templates/detail-card-template.md) | 详情卡片模板（含字段格式化、超链接） |
| T09 | [templates/audit-log-template.md](templates/audit-log-template.md) | 审核日志模板（含流程节点、三种状态展示） |
| T10 | [templates/progress-bar-template.md](templates/progress-bar-template.md) | 进度条模板（普通/可中止两种模式） |

### 生成流程
1. 用户指定要生成的组件类型
2. 从对应模板读取代码骨架
3. 参考对应样例调整细节
4. 使用对应规则自检生成的代码

---

## 安全约束

1. **样式一致性**：所有生成的组件必须使用 CSS 变量而非硬编码色值
2. **无障碍要求**：按钮、表单控件必须有 aria 属性或可访问标签
3. **代码质量**：生成的代码遵循阿里前端开发规范

---

## 文件索引

### 规则文件
| 文件 | 说明 |
|------|------|
| [scripts/color-rules.md](scripts/color-rules.md) | 颜色规范检查规则（5 条） |
| [scripts/typography-rules.md](scripts/typography-rules.md) | 字体排版规范检查规则（5 条） |
| [scripts/button-rules.md](scripts/button-rules.md) | 按钮规范检查规则（8 条） |
| [scripts/input-rules.md](scripts/input-rules.md) | 录入控件规范检查规则（8 条） |
| [scripts/table-rules.md](scripts/table-rules.md) | 表格规范检查规则（11 条） |
