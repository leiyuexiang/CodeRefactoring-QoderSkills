# 模块与业务划分样例

> 参照 framework-web2-server 分包规范，结合阿里前端开发规范，展示业务模块划分、组件提取、层级分离的标准做法。

## 1. 业务模块划分原则

### 1.1 按业务域划分模块

```
modules/
├── auth/                      # 认证域 —— 登录、注册、密码
├── dashboard/                 # 工作台域 —— 首页、门户
├── admin/                     # 系统管理域 —— 用户、角色、权限、组织
├── finance/                   # 财务域 —— 预算、用款、支付
├── workflow/                  # 流程域 —— 流程定义、流程审核、流转
├── message-center/            # 消息域 —— 通知、公告、站内信
├── scheduled-task/            # 调度域 —— 定时任务、批处理
├── system-maintenance/        # 运维域 —— 日志、监控、配置
├── data-quality/              # 数据质量域 —— 校验、清洗、比对
├── report/                    # 报表域 —— 统计、导出、打印
└── help/                      # 帮助域 —— 帮助中心、操作指南
```

### 1.2 模块划分标准

```typescript
/**
 * 模块划分决策矩阵
 *
 * 判断一组功能是否应独立为一个模块：
 * 1. 业务独立性：是否有独立的业务流程？
 * 2. 数据边界：是否有独立的数据模型？
 * 3. 功能聚合度：内部功能是否高内聚？
 * 4. 团队归属：是否可以独立分配给一个人/小组？
 * 5. 复用性：是否仅被一个入口引用？
 *
 * 满足 3 项及以上 → 独立模块
 * 满足 2 项 → 考虑作为子模块
 * 满足 1 项及以下 → 合并到相近模块
 */

// 示例：用款计划与预算管理
// ✅ 业务独立性：用款计划有独立的审批流程
// ✅ 数据边界：有独立的用款计划表
// ✅ 功能聚合度：录入、审核、查询、统计
// ✅ 团队归属：可独立分配
// ✅ 复用性：独立菜单入口
// 结论 → 独立为 finance/payment-plan 子模块
```

## 2. 大模块拆分示例

当一个模块（如原有的 `module`）功能过多时，按业务域拆分为子模块：

### 2.1 拆分前（不推荐）

```
modules/business/
├── views/
│   ├── PaymentPlanList.vue         # 用款计划列表
│   ├── PaymentPlanDetail.vue       # 用款计划详情
│   ├── BudgetList.vue              # 预算列表
│   ├── BudgetDetail.vue            # 预算详情
│   ├── ContractList.vue            # 合同列表
│   ├── ContractDetail.vue          # 合同详情
│   ├── SupplierList.vue            # 供应商列表
│   ├── InvoiceList.vue             # 发票列表
│   └── ... (30+ 个视图文件)
├── components/                     # 60+ 个组件混杂在一起
├── api/                            # 15+ 个接口文件
└── types/
```

### 2.2 拆分后（推荐）

```
modules/
├── finance/                        # 财务域
│   ├── payment-plan/              # 用款计划子模块
│   │   ├── views/
│   │   │   ├── PaymentPlanList.vue
│   │   │   └── PaymentPlanDetail.vue
│   │   ├── components/
│   │   │   ├── PaymentForm.vue
│   │   │   └── AmountSummary.vue
│   │   ├── api/
│   │   │   └── payment-plan.ts
│   │   ├── types/
│   │   │   └── payment-plan.ts
│   │   └── index.ts
│   ├── budget/                    # 预算子模块
│   │   ├── views/
│   │   ├── components/
│   │   ├── api/
│   │   ├── types/
│   │   └── index.ts
│   └── index.ts                   # 财务域导出入口
│
├── procurement/                    # 采购域
│   ├── contract/                  # 合同子模块
│   ├── supplier/                  # 供应商子模块
│   ├── invoice/                   # 发票子模块
│   └── index.ts
```

## 3. 组件提取层级判定

```typescript
/**
 * 组件放置位置决策流程
 *
 * Q1: 此组件是否被 2 个及以上模块使用？
 *   YES → Q2
 *   NO  → 放在当前模块的 components/ 中
 *
 * Q2: 此组件是否与特定业务逻辑绑定？
 *   YES → 放在 components/business/ 中
 *   NO  → Q3
 *
 * Q3: 此组件是否属于页面布局骨架？
 *   YES → 放在 components/layout/ 中
 *   NO  → 放在 components/common/ 中
 */
```

### 3.1 层级判定示例

```vue
<!-- ❌ 错误：模块内组件被其他模块引用 -->
<!-- modules/finance/components/AmountInput.vue 被 modules/procurement/ 使用 -->
<script setup lang="ts">
// 应提取到 components/common/ 或 components/business/
import AmountInput from '@/modules/finance/components/AmountInput.vue'
</script>

<!-- ✅ 正确：提取到共享组件层 -->
<script setup lang="ts">
import AmountInput from '@/components/business/AmountInput.vue'
</script>
```

### 3.2 组件分层映射

```
components/
├── common/                    # 通用组件：与业务无关，可跨项目复用
│   ├── DataTable/             # 数据表格
│   ├── FormCard/              # 录入卡片
│   ├── DetailCard/            # 详情卡片
│   ├── ConfirmDialog/         # 确认对话框
│   ├── EmptyState/            # 空状态占位
│   └── LoadingOverlay/        # 加载蒙层
│
├── layout/                    # 布局组件：页面骨架，全局唯一
│   ├── PageContainer.vue      # 页面容器
│   ├── OperationBar.vue       # 操作栏
│   ├── QueryPanel/            # 查询面板
│   ├── BreadcrumbNav.vue      # 面包屑
│   ├── SideNav.vue            # 侧边导航
│   └── TabBar.vue             # 页签栏
│
├── business/                  # 业务组件：与业务绑定但跨模块使用
│   ├── AuditLog/              # 审核日志
│   ├── AttachmentManager/     # 附件管理
│   ├── WorkflowPanel/         # 流程面板
│   ├── ProgressBar/           # 进度条
│   ├── AmountInput/           # 金额录入
│   └── AccountDisplay/        # 账号展示
```

## 4. 路由与模块注册

### 4.1 按模块拆分路由

```typescript
// framework/router/modules/finance.ts
import type { RouteRecordRaw } from 'vue-router'

const financeRoutes: RouteRecordRaw[] = [
  {
    path: '/finance',
    name: 'Finance',
    redirect: '/finance/payment-plan',
    meta: { title: '财务管理', icon: 'MoneyCollectOutlined' },
    children: [
      {
        path: 'payment-plan',
        name: 'PaymentPlanList',
        component: () => import('@/modules/finance/payment-plan/views/PaymentPlanList.vue'),
        meta: { title: '用款计划录入' },
      },
      {
        path: 'payment-plan/:id',
        name: 'PaymentPlanDetail',
        component: () => import('@/modules/finance/payment-plan/views/PaymentPlanDetail.vue'),
        meta: { title: '用款计划详情', hidden: true },
      },
      {
        path: 'budget',
        name: 'BudgetList',
        component: () => import('@/modules/finance/budget/views/BudgetList.vue'),
        meta: { title: '预算管理' },
      },
    ],
  },
]

export default financeRoutes
```

### 4.2 路由自动注册

```typescript
// framework/router/index.ts
import type { RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHistory } from 'vue-router'

// 自动导入模块路由
const routeModules = import.meta.glob('./modules/*.ts', { eager: true })
const moduleRoutes: RouteRecordRaw[] = []

Object.values(routeModules).forEach((module: any) => {
  const routes = module.default || module
  moduleRoutes.push(...(Array.isArray(routes) ? routes : [routes]))
})

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/modules/auth/views/Login.vue'),
      meta: { title: '登录', requiresAuth: false },
    },
    {
      path: '/',
      name: 'Layout',
      component: () => import('@/components/layout/PageContainer.vue'),
      children: moduleRoutes,
    },
  ],
})

export default router
```

## 5. 状态管理与模块隔离

### 5.1 按模块拆分 Store

```typescript
// framework/store/modules/finance.ts
import { defineStore } from 'pinia'
import type { PaymentPlan } from '@/modules/finance/payment-plan/types/payment-plan'

export const useFinanceStore = defineStore('finance', () => {
  // 仅存放跨页面共享的状态
  const currentPlan = ref<PaymentPlan | null>(null)
  const budgetYear = ref<number>(new Date().getFullYear())

  return { currentPlan, budgetYear }
})
```

### 5.2 页面级状态使用 Composable

```typescript
// modules/finance/payment-plan/composables/use-payment-plan.ts
import { ref, computed } from 'vue'
import { getPaymentPlanList } from '../api/payment-plan'
import type { PaymentPlan, QueryParams } from '../types/payment-plan'

export function usePaymentPlan() {
  const dataSource = ref<PaymentPlan[]>([])
  const loading = ref(false)
  const queryParams = ref<QueryParams>({})

  const fetchList = async () => {
    loading.value = true
    try {
      const res = await getPaymentPlanList(queryParams.value)
      dataSource.value = res.data.records
    } finally {
      loading.value = false
    }
  }

  return { dataSource, loading, queryParams, fetchList }
}
```

## 规范要点

- 模块按业务域划分，不按页面类型划分
- 单个模块的 views/ 文件数不超过 10 个，超过时拆分子模块
- 模块间不直接引用对方的 components/，需通过提升到共享层解耦
- 路由按模块文件拆分，通过自动导入注册
- Store 仅存放跨页面共享的状态，页面级状态使用 Composable
- 模块命名遵循 kebab-case，子模块使用目录嵌套而非名称拼接
- 每个模块/子模块必须有 `index.ts` 导出入口
- API 层与 Types 层文件一一对应，按业务实体划分
