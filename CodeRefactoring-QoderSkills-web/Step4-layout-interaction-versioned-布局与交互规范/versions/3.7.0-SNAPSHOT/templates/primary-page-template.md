# 一级页面模板

> 包含页签栏、导航栏、操作栏、查询面板、数据表格的完整一级页面模板。

## 使用方式

复制模板代码，根据业务需求修改以下内容：
1. 页签配置（`tabsConfig`）
2. 导航栏配置（`navConfig`）
3. 表格列定义（`columns`）
4. 操作按钮定义（`actionButtons`）
5. 查询条件定义（`queryFields`）

## 模板代码

```vue
<template>
  <div class="primary-page">
    <!-- ========== 页签栏（可选） ========== -->
    <div class="primary-page__tabs" v-if="tabsConfig.enabled">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane v-for="tab in tabsConfig.items" :key="tab.key">
          <template #tab>
            <span>{{ tab.label }}</span>
            <span class="tab-badge" v-if="tab.count != null">({{ tab.count }})</span>
          </template>
        </a-tab-pane>
      </a-tabs>
    </div>

    <div class="primary-page__body">
      <!-- ========== 导航栏（可选） ========== -->
      <div
        class="primary-page__nav"
        v-if="navConfig.enabled"
        :style="{ width: navConfig.width + 'px' }"
      >
        <div class="nav-header">
          <a-button
            v-if="navConfig.modes.length > 1"
            type="text"
            size="small"
            @click="switchNavMode"
          >
            <SwapOutlined />
          </a-button>
          <a-input-search
            v-model:value="navSearch"
            placeholder="搜索"
            size="small"
            allow-clear
          />
        </div>
        <div class="nav-body">
          <a-tree
            :tree-data="navTreeData"
            :selected-keys="navSelectedKeys"
            :checked-keys="navCheckedKeys"
            :checkable="navConfig.multiSelect"
            :expanded-keys="navExpandedKeys"
            :auto-expand-parent="true"
            @select="handleNavSelect"
            @check="handleNavCheck"
            @expand="handleNavExpand"
          />
        </div>
        <!-- 多选模式下显示操作按钮 -->
        <div class="nav-footer" v-if="navConfig.multiSelect">
          <a-button type="primary" size="small" @click="handleNavRefresh">查询</a-button>
        </div>
      </div>

      <!-- ========== 主内容区 ========== -->
      <div class="primary-page__main">
        <!-- 操作栏 -->
        <div class="operation-bar">
          <!-- 左侧：业务操作按钮 -->
          <div class="operation-bar__left">
            <template v-for="btn in visibleActionButtons" :key="btn.key">
              <a-button
                :type="btn.primary ? 'primary' : 'default'"
                :danger="btn.danger"
                @click="btn.handler"
              >
                {{ btn.label }}
              </a-button>
            </template>
            <!-- 更多按钮（超过4个时） -->
            <a-dropdown v-if="moreActionButtons.length > 0">
              <a-button>更多 <DownOutlined /></a-button>
              <template #overlay>
                <a-menu @click="handleMoreAction">
                  <a-menu-item v-for="btn in moreActionButtons" :key="btn.key">
                    {{ btn.label }}
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>

          <!-- 右侧：辅助操作 -->
          <div class="operation-bar__right">
            <!-- 快捷查询（可选，最多3个条件） -->
            <a-input
              v-if="quickSearchConfig.enabled"
              v-model:value="quickSearchValue"
              :placeholder="quickSearchConfig.placeholder"
              allow-clear
              style="width: 180px"
              @press-enter="handleQuickSearch"
            >
              <template #prefix><SearchOutlined /></template>
            </a-input>
            <!-- 金额单位切换（可选） -->
            <a-radio-group
              v-if="amountUnitConfig.enabled"
              v-model:value="amountUnit"
              size="small"
              @change="handleUnitChange"
            >
              <a-radio-button value="yuan">元</a-radio-button>
              <a-radio-button value="wan">万元</a-radio-button>
              <a-radio-button value="yi">亿元</a-radio-button>
            </a-radio-group>
            <!-- 辅助图标按钮 -->
            <a-tooltip title="刷新"><a-button type="text" @click="handleRefresh"><ReloadOutlined /></a-button></a-tooltip>
            <a-tooltip title="查询"><a-button type="text" @click="toggleQueryPanel"><SearchOutlined /></a-button></a-tooltip>
            <a-tooltip title="全屏"><a-button type="text" @click="toggleFullscreen"><FullscreenOutlined /></a-button></a-tooltip>
            <a-tooltip title="设置"><a-button type="text" @click="openTableSettings"><SettingOutlined /></a-button></a-tooltip>
          </div>
        </div>

        <!-- 查询条件汇总栏（面板关闭且有条件时显示） -->
        <div class="query-summary" v-if="!queryPanelOpen && activeConditions.length > 0">
          <template v-for="(cond, index) in activeConditions.slice(0, 5)" :key="cond.field">
            <a-tag closable @close="removeCondition(cond.field)">
              <a-tooltip :title="cond.fullDisplayText">
                <span class="query-tag-text">{{ cond.label }}：{{ cond.displayValue }}</span>
              </a-tooltip>
            </a-tag>
          </template>
          <a-popover v-if="activeConditions.length > 5" trigger="hover">
            <template #content>
              <a-tag
                v-for="cond in activeConditions.slice(5)"
                :key="cond.field"
                closable
                @close="removeCondition(cond.field)"
              >
                {{ cond.label }}：{{ cond.displayValue }}
              </a-tag>
            </template>
            <a-button type="link" size="small">更多({{ activeConditions.length - 5 }})</a-button>
          </a-popover>
          <a type="link" @click="clearAllConditions">清空条件</a>
        </div>

        <!-- 查询面板（浮动） -->
        <div class="query-panel" v-if="queryPanelOpen">
          <div class="query-panel__header">
            <a-select v-model:value="savedQueryId" placeholder="常用查询" allow-clear style="width: 180px" />
            <div class="query-panel__actions">
              <a-button type="primary" @click="handleQuery">查询</a-button>
              <a-button @click="handleResetQuery">重置</a-button>
              <a-button @click="handleSaveQuery">保存</a-button>
              <a-button @click="openQuerySettings">设置</a-button>
            </div>
          </div>
          <a-form :model="queryForm" layout="inline">
            <a-row :gutter="16" style="width: 100%">
              <a-col :span="8" v-for="field in queryFields" :key="field.name">
                <a-form-item :label="field.label">
                  <component :is="field.component" v-model:value="queryForm[field.name]" v-bind="field.props" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- 数据表格 -->
        <div class="table-area">
          <a-table
            :columns="tableColumns"
            :data-source="dataSource"
            :loading="loading"
            :pagination="paginationConfig"
            :row-selection="rowSelectionConfig"
            :row-class-name="zebraRowClass"
            :scroll="{ x: 'max-content' }"
            bordered
            size="middle"
            row-key="id"
            @change="handleTableChange"
          >
            <template #bodyCell="{ column, text, record, index }">
              <!-- 行号 -->
              <template v-if="column.key === 'rowIndex'">
                {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
              </template>
            </template>
            <!-- 合计行 -->
            <template #summary v-if="showSummary">
              <a-table-summary fixed>
                <a-table-summary-row v-if="selectedRowKeys.length > 0" class="selected-summary">
                  <a-table-summary-cell :col-span="summaryColSpan">
                    选中合计({{ selectedRowKeys.length }})
                  </a-table-summary-cell>
                  <!-- 金额合计列 -->
                </a-table-summary-row>
                <a-table-summary-row class="total-summary">
                  <a-table-summary-cell :col-span="summaryColSpan">合计</a-table-summary-cell>
                  <!-- 金额合计列 -->
                </a-table-summary-row>
              </a-table-summary>
            </template>
          </a-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

// ============ 配置区 - 根据业务修改 ============

/** 页签配置 */
const tabsConfig = {
  enabled: true,
  items: [
    { key: 'pending', label: '待送审', count: 10 },
    { key: 'submitted', label: '已送审', count: null },
    { key: 'auditing', label: '待审核', count: 5 },
    { key: 'all', label: '全部', count: null },
  ],
};

/** 导航栏配置 */
const navConfig = {
  enabled: true,
  width: 240,
  multiSelect: false,
  modes: ['tree'],
};

/** 金额单位配置 */
const amountUnitConfig = { enabled: true };

/** 快捷查询配置 */
const quickSearchConfig = { enabled: true, placeholder: '请输入关键字' };

// ============ 状态管理 ============

const activeTab = ref('pending');
const loading = ref(false);
const queryPanelOpen = ref(false);
const amountUnit = ref('wan');
const selectedRowKeys = ref<string[]>([]);

/** 斑马纹 */
const zebraRowClass = (_record: any, index: number) => {
  return index % 2 === 0 ? 'row-even' : 'row-odd';
};

/** 操作按钮拆分（前4个可见，其余放更多） */
const actionButtons = [
  { key: 'add', label: '新增', primary: true, handler: () => {} },
  { key: 'edit', label: '修改', handler: () => {} },
  { key: 'submit', label: '送审', handler: () => {} },
  { key: 'delete', label: '删除', handler: () => {} },
  { key: 'copy', label: '复制', handler: () => {} },
];
const visibleActionButtons = computed(() => actionButtons.slice(0, 4));
const moreActionButtons = computed(() => actionButtons.slice(4));
</script>

<style scoped>
.primary-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #f0f0f0;
  background: #fff;
}
.primary-page__tabs { padding: 0 16px; border-bottom: 1px solid #f0f0f0; }
.primary-page__tabs :deep(.ant-tabs-nav) { margin-bottom: 0; }
.tab-badge { color: #ff4d4f; }
.primary-page__body { flex: 1; display: flex; overflow: hidden; }
.primary-page__nav { border-right: 1px solid #f0f0f0; display: flex; flex-direction: column; flex-shrink: 0; }
.nav-header { padding: 8px; }
.nav-body { flex: 1; overflow-y: auto; padding: 0 8px; }
.nav-footer { padding: 8px; border-top: 1px solid #f0f0f0; text-align: center; }
.primary-page__main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.operation-bar { display: flex; justify-content: space-between; align-items: center; padding: 8px 16px; }
.operation-bar__left, .operation-bar__right { display: flex; align-items: center; gap: 8px; }
.query-summary { padding: 4px 16px; display: flex; align-items: center; flex-wrap: wrap; gap: 4px; }
.query-tag-text { max-width: 170px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; vertical-align: middle; }
.query-panel { padding: 16px; border: 1px solid #f0f0f0; margin: 0 16px; border-radius: 2px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.query-panel__header { display: flex; justify-content: space-between; margin-bottom: 16px; }
.query-panel__actions { display: flex; gap: 8px; }
.table-area { flex: 1; padding: 0 16px 16px; overflow: auto; }
:deep(.row-even) { background: #fff; }
:deep(.row-odd) { background: #fafafa; }
:deep(.ant-table-thead > tr > th) { font-weight: 600; text-align: center; }

/* 滚动条统一规范 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background: transparent; border-radius: 3px; }
*:hover::-webkit-scrollbar-thumb { background: #c1c1c1; }
</style>
```

## 配置清单

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| tabsConfig.enabled | 是否启用页签栏 | true |
| navConfig.enabled | 是否启用导航栏 | true |
| navConfig.width | 导航栏宽度(px) | 240 |
| navConfig.multiSelect | 导航栏多选模式 | false |
| amountUnitConfig.enabled | 是否启用金额单位切换 | true |
| quickSearchConfig.enabled | 是否启用快捷查询 | true |
