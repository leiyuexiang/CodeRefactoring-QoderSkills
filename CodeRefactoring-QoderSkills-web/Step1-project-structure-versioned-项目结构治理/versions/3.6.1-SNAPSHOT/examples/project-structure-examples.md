# 项目结构样例

> 参照 framework-web2-server 分包规范，结合阿里前端开发规范，定义 Vue 3 + Ant Design Vue 项目的标准目录结构。

## 1. 整体项目结构

基于现有系统三层架构（common 全局资源 / components 共享组件 / templates 业务模块），映射为 Vue 3 标准化目录：

```
src/
├── assets/                    # 静态资源层（对应 common/）
│   ├── styles/                # 全局样式
│   │   ├── variables.scss     # CSS 变量定义（品牌色、中性色、功能色）
│   │   ├── mixins.scss        # SCSS Mixin 集合
│   │   ├── reset.scss         # 样式重置
│   │   ├── scrollbar.scss     # 全局滚动条样式
│   │   └── index.scss         # 样式入口文件
│   ├── icons/                 # 图标资源
│   │   ├── svg/               # SVG 图标文件
│   │   └── index.ts           # 图标注册入口
│   └── images/                # 图片资源
│       ├── logo.png
│       └── empty-data.png
│
├── components/                # 共享组件层（对应 components/）
│   ├── business/              # 业务通用组件
│   │   ├── AuditLog/          # 审核日志组件
│   │   │   ├── index.vue
│   │   │   ├── AuditLogTimeline.vue
│   │   │   └── types.ts
│   │   ├── AttachmentManager/ # 附件管理组件
│   │   ├── ProgressBar/       # 进度条组件
│   │   └── WorkflowPanel/     # 流程面板组件
│   ├── layout/                # 布局组件
│   │   ├── PageContainer.vue  # 页面容器
│   │   ├── OperationBar.vue   # 操作栏
│   │   ├── QueryPanel/        # 查询面板
│   │   └── BreadcrumbNav.vue  # 面包屑导航
│   └── common/                # 基础通用组件
│       ├── DataTable/         # 数据表格（封装 a-table）
│       ├── FormCard/          # 录入卡片
│       ├── DetailCard/        # 详情卡片
│       └── ConfirmDialog/     # 确认对话框
│
├── composables/               # 组合式函数层
│   ├── use-table-data.ts      # 表格数据管理
│   ├── use-query-panel.ts     # 查询面板逻辑
│   ├── use-pagination.ts      # 分页逻辑
│   ├── use-drawer.ts          # 抽屉控制
│   ├── use-modal.ts           # 对话框控制
│   └── use-permission.ts      # 权限控制
│
├── modules/                   # 业务模块层（对应 templates/）
│   ├── admin/                 # 系统管理模块
│   │   ├── views/             # 页面视图
│   │   ├── components/        # 模块内组件
│   │   ├── composables/       # 模块内组合函数
│   │   ├── api/               # 模块接口定义
│   │   ├── types/             # 模块类型定义
│   │   └── index.ts           # 模块导出入口
│   ├── finance/               # 财务管理模块
│   ├── workflow/              # 流程管理模块
│   └── ...                    # 其他业务模块
│
├── framework/                 # 框架层（对应 framework/）
│   ├── router/                # 路由配置
│   │   ├── index.ts
│   │   ├── guards.ts          # 路由守卫
│   │   └── modules/           # 模块路由（按模块拆分）
│   ├── store/                 # 状态管理
│   │   ├── index.ts
│   │   └── modules/           # 模块状态（按模块拆分）
│   ├── plugins/               # 插件注册
│   ├── directives/            # 全局指令
│   └── config/                # 全局配置
│
├── services/                  # 服务层
│   ├── http/                  # HTTP 客户端
│   │   ├── index.ts           # Axios 实例与拦截器
│   │   └── types.ts           # 请求/响应类型
│   └── api/                   # 公共 API 定义
│       ├── auth.ts            # 认证接口
│       ├── dict.ts            # 字典接口
│       └── file.ts            # 文件接口
│
├── utils/                     # 工具函数层
│   ├── format.ts              # 格式化工具（金额、日期、账号）
│   ├── validate.ts            # 校验工具
│   ├── storage.ts             # 存储工具
│   └── constants.ts           # 全局常量
│
├── types/                     # 全局类型定义
│   ├── global.d.ts            # 全局类型声明
│   ├── api.d.ts               # API 通用类型
│   └── component.d.ts         # 组件通用类型
│
├── App.vue                    # 根组件
└── main.ts                    # 应用入口
```

## 2. 业务模块内部结构

每个业务模块（modules/下的子目录）遵循统一的内部结构：

```
modules/finance/                # 财务管理模块
├── views/                     # 页面视图
│   ├── PaymentPlanList.vue    # 用款计划列表页（一级页面）
│   ├── PaymentPlanDetail.vue  # 用款计划详情页（全屏二级页面）
│   ├── PaymentPlanEdit.vue    # 用款计划编辑页（全屏二级页面）
│   └── BudgetOverview.vue     # 预算概览页
│
├── components/                # 模块内组件（仅本模块使用）
│   ├── PaymentForm.vue        # 用款录入表单
│   ├── BudgetTree.vue         # 预算科目树
│   └── AmountSummary.vue      # 金额汇总卡片
│
├── composables/               # 模块内组合函数
│   ├── use-payment-plan.ts    # 用款计划业务逻辑
│   └── use-budget-tree.ts     # 预算科目树逻辑
│
├── api/                       # 模块接口定义
│   ├── payment-plan.ts        # 用款计划接口
│   └── budget.ts              # 预算接口
│
├── types/                     # 模块类型定义
│   ├── payment-plan.ts        # 用款计划类型
│   └── budget.ts              # 预算类型
│
└── index.ts                   # 模块导出入口
```

## 3. 共享组件内部结构

复合组件（如查询面板、数据表格）采用目录形式组织：

```
components/layout/QueryPanel/
├── index.vue                  # 组件主入口（对外暴露）
├── QueryPanelSummary.vue      # 查询条件汇总子组件
├── QueryPanelDrawer.vue       # 条件设置抽屉子组件
├── QueryPanelSaved.vue        # 常用查询子组件
├── types.ts                   # 组件类型定义
└── constants.ts               # 组件常量

components/common/DataTable/
├── index.vue                  # 表格主入口
├── DataTableHeader.vue        # 表头设置子组件
├── DataTableSummary.vue       # 合计行子组件
├── DataTableExport.vue        # 导出功能子组件
├── types.ts                   # 类型定义
├── use-table-scroll.ts        # 表格滚动逻辑
└── constants.ts               # 默认配置常量
```

## 4. 框架与业务模块的映射关系

现有 framework-web2-server 业务模块到 Vue 3 标准模块的映射：

| 原有模块 | Vue 3 模块 | 分类 | 说明 |
|----------|-----------|------|------|
| framework/frame | framework/ | 框架层 | 系统框架核心，拆分为 router/store/plugins/directives |
| framework/autojob | modules/scheduled-task/ | 业务模块 | 自动任务管理 |
| framework/maintenance | modules/system-maintenance/ | 业务模块 | 系统维护 |
| framework/message | modules/message-center/ | 业务模块 | 消息中心 |
| framework/uiview | framework/config/ | 框架层 | UI 视图配置 |
| admin | modules/admin/ | 业务模块 | 系统管理 |
| module | modules/business/ | 业务模块 | 核心业务模块（需拆分） |
| home / businessHome | modules/dashboard/ | 业务模块 | 首页/工作台 |
| flow_business | modules/workflow/ | 业务模块 | 流程业务 |
| login / register | modules/auth/ | 业务模块 | 认证模块 |
| helpCenter / guide | modules/help/ | 业务模块 | 帮助中心 |
| element | components/business/ | 共享组件 | 业务元素组件 |
| workflow | components/business/WorkflowPanel/ | 共享组件 | 流程组件 |
| portalet | components/layout/ | 共享组件 | 门户组件 |
| common/ | assets/ + utils/ + services/ | 公共层 | 全局资源，按职责拆分 |

## 规范要点

- 目录名统一使用 kebab-case
- 组件文件名使用 PascalCase.vue
- 工具文件名使用 kebab-case.ts
- 每个模块必须有 `index.ts` 作为导出入口
- 模块内组件不应被其他模块直接引用，需提升到 `components/` 层
- `composables/` 函数文件统一以 `use-` 为前缀
- `api/` 文件按业务实体划分，一个实体一个文件
- `types/` 文件与 `api/` 文件一一对应
- 公共资源（common/）拆分为 assets（静态资源）、utils（工具函数）、services（服务层）三个职责明确的目录
