# 项目脚手架模板

> 完整的 Vue 3 + Ant Design Vue 项目初始化脚手架，包含全局配置、公共资源、框架层的标准模板代码。

## 使用方式

初始化新项目或重构现有项目时，按照以下模板创建全局层的基础文件。

## 1. 全局样式变量

```scss
// src/assets/styles/variables.scss

// ===== 品牌色 =====
$color-primary: #1890ff;              // 品牌主色（Ant Design Blue-6）
$color-primary-hover: #40a9ff;        // 主色悬浮
$color-primary-active: #096dd9;       // 主色点击

// ===== 功能色 =====
$color-success: #52c41a;              // 成功
$color-warning: #faad14;              // 警告
$color-error: #ff4d4f;                // 错误/危险
$color-info: #1890ff;                 // 信息

// ===== 中性色 =====
$color-text-primary: #434343;         // 内容文字
$color-text-label: #595959;           // 标签文字
$color-text-placeholder: #bfbfbf;     // 占位提示
$color-text-disabled: #bfbfbf;        // 禁用文字
$color-text-breadcrumb: #8c8c8c;      // 面包屑非当前页

// ===== 背景色 =====
$bg-disabled: #fafafa;                // 禁用背景
$bg-hover: #f5f5f5;                   // 悬浮背景
$bg-selected: #e6f7ff;               // 选中背景
$bg-editable: #feffe6;               // 可编辑区域背景
$bg-table-stripe: #fafafa;           // 表格斑马纹

// ===== 边框色 =====
$border-color-base: #d9d9d9;          // 默认边框
$border-color-split: #f0f0f0;         // 分割线

// ===== 字号 =====
$font-size-card-title: 16px;          // 卡片标题
$font-size-base: 14px;                // 控件标签/按钮/正文
$font-size-small: 12px;               // 提示/辅助文字

// ===== 间距 =====
$spacing-xs: 4px;
$spacing-sm: 8px;
$spacing-md: 16px;
$spacing-lg: 24px;

// ===== 滚动条 =====
$scrollbar-width: 6px;
$scrollbar-thumb-color: #c1c1c1;
$scrollbar-thumb-radius: 3px;

// ===== 布局 =====
$page-border-color: #f0f0f0;
$operation-bar-height: 40px;
$query-panel-field-count: 3;          // 查询面板每行字段数
$nav-default-width: 200px;
$breadcrumb-max-width: 60%;
$breadcrumb-max-levels: 10;

// ===== 对话框宽度 =====
$modal-width-alert: 400px;
$modal-width-confirm: 420px;
$modal-width-single-col: 440px;
$modal-width-double-col: 856px;

// ===== 更多面板 =====
$more-panel-max-width: 200px;
```

## 2. 全局滚动条样式

```scss
// src/assets/styles/scrollbar.scss

// 全局滚动条样式 - 默认隐藏，鼠标移入显示
::-webkit-scrollbar {
  width: $scrollbar-width;
  height: $scrollbar-width;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: $scrollbar-thumb-radius;
}

// 鼠标移入时显示滚动条
*:hover::-webkit-scrollbar-thumb {
  background: $scrollbar-thumb-color;
}

// Firefox 滚动条
* {
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;

  &:hover {
    scrollbar-color: $scrollbar-thumb-color transparent;
  }
}
```

## 3. 样式入口文件

```scss
// src/assets/styles/index.scss
@import './variables';
@import './reset';
@import './scrollbar';
@import './mixins';
```

## 4. HTTP 客户端

```typescript
// src/services/http/index.ts
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { message } from 'ant-design-vue'

const http: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截器
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器
http.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, msg, data } = response.data
    if (code === 0) {
      return response.data
    }
    message.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          message.error('登录已过期，请重新登录')
          // 跳转登录页
          break
        case 403:
          message.error('没有操作权限')
          break
        case 500:
          message.error('服务器内部错误')
          break
        default:
          message.error(`请求失败 (${status})`)
      }
    } else {
      message.error('网络连接异常')
    }
    return Promise.reject(error)
  },
)

export default http
```

## 5. 格式化工具

```typescript
// src/utils/format.ts

/** 金额格式化：千分分隔 + 两位小数 */
export function formatAmount(value: number | string, decimals = 2): string {
  const num = typeof value === 'string' ? parseFloat(value) : value
  if (isNaN(num)) return '--'
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  })
}

/** 账号格式化：每四位空格分隔 */
export function formatAccount(account: string): string {
  if (!account) return '--'
  return account.replace(/(.{4})/g, '$1 ').trim()
}

/** 身份证号脱敏 */
export function formatIdCard(idCard: string): string {
  if (!idCard || idCard.length < 8) return idCard || '--'
  return `${idCard.slice(0, 4)}${'*'.repeat(idCard.length - 8)}${idCard.slice(-4)}`
}

/** 日期格式化 */
export function formatDate(date: string | Date, format = 'YYYY-MM-DD'): string {
  if (!date) return '--'
  const d = typeof date === 'string' ? new Date(date) : date
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
}
```

## 6. 全局常量

```typescript
// src/utils/constants.ts

/** 分页默认配置 */
export const DEFAULT_PAGINATION = {
  pageNum: 1,
  pageSize: 20,
  pageSizeOptions: ['10', '20', '50', '100'],
} as const

/** 表格配置 */
export const TABLE_CONFIG = {
  ROW_INDEX_WIDTH: 60,        // 行号列宽度
  CHECKBOX_WIDTH: 50,         // 复选框列宽度
  ACTION_MIN_WIDTH: 100,      // 操作列最小宽度
  MAX_FROZEN_COLS: 3,         // 每侧最大冻结列数
} as const

/** 按钮配置 */
export const BUTTON_CONFIG = {
  MAX_VISIBLE_COUNT: 4,       // 操作按钮最大可见数量
  MORE_PANEL_WIDTH: 200,      // 更多面板最大宽度
} as const

/** 查询面板配置 */
export const QUERY_PANEL_CONFIG = {
  FIELDS_PER_ROW: 3,          // 每行字段数
  MAX_QUICK_FIELDS: 3,        // 快捷查询最大条件数
} as const

/** 页面层级配置 */
export const PAGE_HIERARCHY = {
  FULLSCREEN: 1,              // 全屏页面
  DRAWER: 2,                  // 抽屉页面
  MODAL: 3,                   // 模式对话框
} as const

/** 面包屑配置 */
export const BREADCRUMB_CONFIG = {
  MAX_LEVELS: 10,             // 最大层数
  MAX_WIDTH_PERCENT: 60,      // 最大宽度百分比
} as const
```

## 7. 全局类型定义

```typescript
// src/types/api.d.ts

/** 通用分页请求参数 */
export interface PaginationParams {
  pageNum: number
  pageSize: number
}

/** 通用分页响应 */
export interface PaginatedResponse<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

/** 通用 API 响应 */
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

/** 通用查询参数 */
export interface BaseQueryParams extends PaginationParams {
  keyword?: string
  startDate?: string
  endDate?: string
  orderBy?: string
  orderDirection?: 'asc' | 'desc'
}
```

```typescript
// src/types/component.d.ts
import type { VNode } from 'vue'

/** 表格列定义 */
export interface TableColumn {
  title: string
  dataIndex: string
  key?: string
  width?: number
  fixed?: 'left' | 'right'
  align?: 'left' | 'center' | 'right'
  sorter?: boolean
  customRender?: (opt: { text: any; record: any; index: number }) => VNode | string
}

/** 查询字段定义 */
export interface QueryField {
  field: string
  label: string
  type: 'input' | 'select' | 'date' | 'dateRange' | 'number'
  placeholder?: string
  options?: { label: string; value: string | number }[]
  defaultValue?: any
}

/** 页签定义 */
export interface TabItem {
  key: string
  label: string
  count?: number
}

/** 操作按钮定义 */
export interface ActionButton {
  key: string
  label: string
  type?: 'primary' | 'default' | 'danger'
  icon?: string
  disabled?: boolean
  confirm?: string
}
```

## 8. 应用入口

```typescript
// src/main.ts
import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import { createPinia } from 'pinia'
import router from './framework/router'
import App from './App.vue'
import 'ant-design-vue/dist/reset.css'
import './assets/styles/index.scss'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Antd)
app.mount('#app')
```

## 规范要点

- 所有颜色值通过 SCSS 变量统一管理，禁止在组件内硬编码色值
- HTTP 客户端统一处理 token 注入、错误提示、登录过期
- 格式化函数统一放在 utils/format.ts，确保全项目金额、日期、账号展示一致
- 全局常量集中定义，避免 magic number
- 类型定义与 API 层分离，放在 types/ 目录统一管理
- 样式入口文件按顺序导入：变量 → 重置 → 滚动条 → Mixin
