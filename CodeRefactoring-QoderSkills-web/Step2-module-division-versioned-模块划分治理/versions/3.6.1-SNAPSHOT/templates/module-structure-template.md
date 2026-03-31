# 业务模块脚手架模板

> 标准化的业务模块目录结构模板，用于快速创建新的业务模块。

## 使用方式

创建新业务模块时，按照以下模板生成目录和文件。将模板中的 `{ModuleName}` 替换为实际模块名（PascalCase），`{module-name}` 替换为实际模块名（kebab-case）。

## 1. 标准业务模块模板

### 1.1 目录结构

```
modules/{module-name}/
├── views/
│   ├── {ModuleName}List.vue       # 列表页（一级页面）
│   └── {ModuleName}Detail.vue     # 详情/编辑页（二级页面）
├── components/
│   ├── {ModuleName}Form.vue       # 录入表单组件
│   ├── {ModuleName}QueryPanel.vue # 查询条件组件（可选）
│   └── {ModuleName}Card.vue       # 详情卡片组件（可选）
├── composables/
│   └── use-{module-name}.ts       # 模块核心业务逻辑
├── api/
│   └── {module-name}.ts           # API 接口定义
├── types/
│   └── {module-name}.ts           # 类型定义
└── index.ts                       # 模块导出入口
```

### 1.2 列表页模板

```vue
<!-- modules/{module-name}/views/{ModuleName}List.vue -->
<template>
  <div class="page-container">
    <!-- 页签栏（可选） -->
    <div class="page-tabs" v-if="tabs.length > 0">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane v-for="tab in tabs" :key="tab.key">
          <template #tab>
            {{ tab.label }}
            <span class="tab-count" v-if="tab.count">({{ tab.count }})</span>
          </template>
        </a-tab-pane>
      </a-tabs>
    </div>

    <div class="page-body">
      <!-- 操作栏 -->
      <div class="operation-bar">
        <div class="operation-bar__left">
          <a-button type="primary" @click="handleAdd">新增</a-button>
          <a-button @click="handleEdit" :disabled="!hasSelected">修改</a-button>
          <a-button @click="handleSubmit" :disabled="!hasSelected">送审</a-button>
          <a-dropdown v-if="moreActions.length > 0">
            <a-button>更多 <DownOutlined /></a-button>
            <template #overlay>
              <a-menu @click="handleMoreAction">
                <a-menu-item v-for="action in moreActions" :key="action.key">
                  {{ action.label }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div class="operation-bar__right">
          <a-tooltip title="刷新">
            <a-button type="text" @click="handleRefresh"><ReloadOutlined /></a-button>
          </a-tooltip>
          <a-tooltip title="查询">
            <a-button type="text" @click="toggleQueryPanel"><SearchOutlined /></a-button>
          </a-tooltip>
        </div>
      </div>

      <!-- 查询条件汇总栏 -->
      <div class="query-summary" v-if="!queryPanelVisible && activeConditions.length > 0">
        <a-tag
          v-for="cond in activeConditions"
          :key="cond.field"
          closable
          @close="handleRemoveCondition(cond.field)"
        >
          {{ cond.label }}：{{ cond.displayValue }}
        </a-tag>
        <a @click="handleClearConditions">清空条件</a>
      </div>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="paginationConfig"
        :row-selection="rowSelection"
        :row-class-name="(_record, index) => index % 2 === 1 ? 'table-row--striped' : ''"
        bordered
        @change="handleTableChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { DownOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue'
import { use{ModuleName} } from '../composables/use-{module-name}'
import type { {ModuleName}QueryParams } from '../types/{module-name}'

defineOptions({ name: '{ModuleName}List' })

// ========== 页签 ==========
const activeTab = ref('pending')
const tabs = ref([
  { key: 'pending', label: '待送审', count: 0 },
  { key: 'submitted', label: '已送审', count: 0 },
  { key: 'all', label: '全部', count: 0 },
])

// ========== 业务逻辑 ==========
const {
  dataSource,
  loading,
  queryParams,
  paginationConfig,
  selectedRowKeys,
  fetchList,
} = use{ModuleName}()

// ========== 查询 ==========
const queryPanelVisible = ref(false)
const activeConditions = ref<{ field: string; label: string; displayValue: string }[]>([])

const toggleQueryPanel = () => {
  queryPanelVisible.value = !queryPanelVisible.value
}

// ========== 操作 ==========
const hasSelected = computed(() => selectedRowKeys.value.length > 0)
const moreActions = ref([
  { key: 'delete', label: '删除' },
  { key: 'export', label: '导出' },
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: string[]) => { selectedRowKeys.value = keys },
}))

const handleAdd = () => { /* 新增逻辑 */ }
const handleEdit = () => { /* 编辑逻辑 */ }
const handleSubmit = () => { /* 送审逻辑 */ }
const handleRefresh = () => { fetchList() }
const handleTabChange = (key: string) => { fetchList() }
const handleMoreAction = ({ key }: { key: string }) => { /* 更多操作 */ }
const handleRemoveCondition = (field: string) => { /* 移除条件 */ }
const handleClearConditions = () => { /* 清空条件 */ }
const handleTableChange = (pagination: any) => { /* 分页变更 */ }

// ========== 初始化 ==========
fetchList()
</script>

<style scoped>
.page-container { border: 1px solid #f0f0f0; background: #fff; height: 100%; display: flex; flex-direction: column; }
.page-tabs { padding: 0 16px; border-bottom: 1px solid #f0f0f0; }
.tab-count { color: #ff4d4f; }
.page-body { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.operation-bar { display: flex; justify-content: space-between; align-items: center; padding: 8px 16px; }
.operation-bar__left, .operation-bar__right { display: flex; align-items: center; gap: 8px; }
.query-summary { padding: 4px 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 4px; }
:deep(.table-row--striped) { background: #fafafa; }
</style>
```

### 1.3 业务逻辑 Composable 模板

```typescript
// modules/{module-name}/composables/use-{module-name}.ts
import { ref, reactive, computed } from 'vue'
import { get{ModuleName}List, delete{ModuleName} } from '../api/{module-name}'
import type { {ModuleName}, {ModuleName}QueryParams } from '../types/{module-name}'

export function use{ModuleName}() {
  const dataSource = ref<{ModuleName}[]>([])
  const loading = ref(false)
  const selectedRowKeys = ref<string[]>([])
  const queryParams = reactive<{ModuleName}QueryParams>({
    pageNum: 1,
    pageSize: 20,
  })
  const total = ref(0)

  const paginationConfig = computed(() => ({
    current: queryParams.pageNum,
    pageSize: queryParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (t: number) => `共 ${t} 条`,
  }))

  const fetchList = async () => {
    loading.value = true
    try {
      const res = await get{ModuleName}List(queryParams)
      dataSource.value = res.data.records
      total.value = res.data.total
    } finally {
      loading.value = false
    }
  }

  const handleDelete = async (ids: string[]) => {
    await delete{ModuleName}(ids)
    selectedRowKeys.value = []
    await fetchList()
  }

  return {
    dataSource,
    loading,
    selectedRowKeys,
    queryParams,
    paginationConfig,
    total,
    fetchList,
    handleDelete,
  }
}
```

### 1.4 API 接口模板

```typescript
// modules/{module-name}/api/{module-name}.ts
import http from '@/services/http'
import type { {ModuleName}, {ModuleName}QueryParams } from '../types/{module-name}'

const BASE_URL = '/api/{module-name}'

/** 查询列表 */
export function get{ModuleName}List(params: {ModuleName}QueryParams) {
  return http.get<{ records: {ModuleName}[]; total: number }>(BASE_URL, { params })
}

/** 查询详情 */
export function get{ModuleName}Detail(id: string) {
  return http.get<{ModuleName}>(`${BASE_URL}/${id}`)
}

/** 新增 */
export function create{ModuleName}(data: Partial<{ModuleName}>) {
  return http.post(BASE_URL, data)
}

/** 修改 */
export function update{ModuleName}(id: string, data: Partial<{ModuleName}>) {
  return http.put(`${BASE_URL}/${id}`, data)
}

/** 删除 */
export function delete{ModuleName}(ids: string[]) {
  return http.delete(BASE_URL, { data: { ids } })
}

/** 送审 */
export function submit{ModuleName}(ids: string[]) {
  return http.post(`${BASE_URL}/submit`, { ids })
}
```

### 1.5 类型定义模板

```typescript
// modules/{module-name}/types/{module-name}.ts

/** {ModuleName} 实体类型 */
export interface {ModuleName} {
  id: string
  // TODO: 根据业务补充字段
  createTime: string
  updateTime: string
  createBy: string
  status: {ModuleName}Status
}

/** 状态枚举 */
export enum {ModuleName}Status {
  DRAFT = 'DRAFT',
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

/** 查询参数 */
export interface {ModuleName}QueryParams {
  pageNum: number
  pageSize: number
  status?: {ModuleName}Status
  keyword?: string
  startDate?: string
  endDate?: string
}
```

### 1.6 模块导出入口

```typescript
// modules/{module-name}/index.ts
export { default as {ModuleName}List } from './views/{ModuleName}List.vue'
export { default as {ModuleName}Detail } from './views/{ModuleName}Detail.vue'
export { use{ModuleName} } from './composables/use-{module-name}'
export type { {ModuleName}, {ModuleName}QueryParams, {ModuleName}Status } from './types/{module-name}'
```

## 2. 含子模块的业务域模板

当业务域包含多个子模块时使用：

```
modules/{domain-name}/
├── {sub-module-a}/
│   ├── views/
│   ├── components/
│   ├── composables/
│   ├── api/
│   ├── types/
│   └── index.ts
├── {sub-module-b}/
│   ├── views/
│   ├── components/
│   ├── composables/
│   ├── api/
│   ├── types/
│   └── index.ts
├── shared/                    # 域内共享资源
│   ├── components/            # 域内共享组件
│   ├── composables/           # 域内共享逻辑
│   ├── types/                 # 域内共享类型
│   └── constants.ts           # 域内常量
└── index.ts                   # 域导出入口
```

## 规范要点

- 所有模板占位符 `{ModuleName}` 使用 PascalCase，`{module-name}` 使用 kebab-case
- 列表页必须包含：操作栏、查询条件汇总、数据表格、分页
- Composable 负责业务逻辑，组件只做渲染
- API 层统一使用 RESTful 风格
- 每个模块的 `index.ts` 导出所有公开的视图、Composable 和类型
- 域内共享资源放在 `shared/` 目录，避免跨域引用模块内部文件
