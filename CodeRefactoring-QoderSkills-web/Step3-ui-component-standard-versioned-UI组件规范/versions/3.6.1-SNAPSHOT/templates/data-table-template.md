# 数据表格模板

> 包含排序、筛选、分页、合计、导出功能的完整数据表格模板。

## 模板代码

```vue
<template>
  <div class="data-table-wrapper">
    <a-table
      :columns="mergedColumns"
      :data-source="dataSource"
      :loading="loading"
      :pagination="false"
      :row-selection="enableSelection ? rowSelection : undefined"
      :row-class-name="zebraRowClass"
      :scroll="{ x: scrollX, y: scrollY }"
      bordered
      size="middle"
      :row-key="rowKey"
      @change="handleChange"
      @resize-column="handleResizeColumn"
    >
      <!-- 行号列 -->
      <template #bodyCell="{ column, text, record, index }">
        <template v-if="column.key === 'rowIndex'">
          {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
        </template>

        <!-- 超链接列 -->
        <template v-else-if="column.linkable">
          <a class="table-link" @click="handleCellClick(column, record)">{{ text }}</a>
        </template>

        <!-- 金额列：千分符 + 两位小数 -->
        <template v-else-if="column.dataType === 'amount'">
          {{ formatAmount(text) }}
        </template>

        <!-- 状态列：居中 + 标签 -->
        <template v-else-if="column.dataType === 'status'">
          <a-tag :color="getStatusColor(text)">{{ getStatusText(text) }}</a-tag>
        </template>

        <!-- 操作列 -->
        <template v-else-if="column.key === 'action'">
          <slot name="action" :record="record" />
        </template>
      </template>

      <!-- 合计行 -->
      <template #summary v-if="showSummary">
        <a-table-summary fixed>
          <!-- 选中合计行 -->
          <a-table-summary-row v-if="selectedRowKeys.length > 0" class="selected-summary-row">
            <a-table-summary-cell :index="0" :col-span="summaryLabelColSpan">
              选中合计({{ selectedRowKeys.length }})
            </a-table-summary-cell>
            <a-table-summary-cell
              v-for="col in summaryAmountColumns"
              :key="col.dataIndex"
              :index="col.summaryIndex"
              align="right"
            >
              {{ formatAmount(selectedSummary[col.dataIndex]) }}
            </a-table-summary-cell>
          </a-table-summary-row>
          <!-- 全量合计行 -->
          <a-table-summary-row class="total-summary-row">
            <a-table-summary-cell :index="0" :col-span="summaryLabelColSpan">
              合计
            </a-table-summary-cell>
            <a-table-summary-cell
              v-for="col in summaryAmountColumns"
              :key="col.dataIndex"
              :index="col.summaryIndex"
              align="right"
            >
              {{ formatAmount(totalSummary[col.dataIndex]) }}
            </a-table-summary-cell>
          </a-table-summary-row>
        </a-table-summary>
      </template>
    </a-table>

    <!-- 分页栏 -->
    <div class="pagination-bar" v-if="showPagination">
      <a-pagination
        v-model:current="pagination.current"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :show-size-changer="paginationMode !== 'minimal'"
        :show-quick-jumper="paginationMode === 'standard' || paginationMode === 'medium'"
        :show-total="paginationMode === 'standard' ? showTotal : undefined"
        :page-size-options="['10', '20', '50', '100']"
        @change="handlePageChange"
        @show-size-change="handleSizeChange"
      />
    </div>

    <!-- ========== 表格设置抽屉 ========== -->
    <a-drawer
      v-model:open="settingsVisible"
      title="表格设置"
      :width="600"
      :mask="false"
    >
      <div class="table-settings">
        <div class="settings-enabled">
          <h4>启用字段</h4>
          <div class="settings-zone">
            <div class="settings-zone__title">左冻结区</div>
            <div class="field-list" />
          </div>
          <div class="settings-zone">
            <div class="settings-zone__title">常规区</div>
            <div class="field-list" />
          </div>
          <div class="settings-zone">
            <div class="settings-zone__title">右冻结区</div>
            <div class="field-list" />
          </div>
        </div>
        <div class="settings-all">
          <h4>全部字段</h4>
          <div class="field-list" />
        </div>
      </div>
      <template #footer>
        <a-button @click="restoreTableDefaults">恢复默认</a-button>
        <a-button type="primary" @click="saveTableSettings" style="margin-left: 8px">保存</a-button>
      </template>
    </a-drawer>

    <!-- ========== 数据导出抽屉 ========== -->
    <a-drawer
      v-model:open="exportVisible"
      title="数据导出"
      :width="500"
      :mask="false"
    >
      <a-form :model="exportConfig" layout="vertical">
        <a-form-item label="导出范围">
          <a-radio-group v-model:value="exportConfig.scope">
            <a-radio value="selected" :disabled="selectedRowKeys.length === 0">导出选择行</a-radio>
            <a-radio value="currentPage">导出当前页</a-radio>
            <a-radio value="all">导出全部数据</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="导出列">
          <a-checkbox-group v-model:value="exportConfig.columns" :options="exportColumnOptions" />
        </a-form-item>
        <a-form-item label="文件名">
          <a-input v-model:value="exportConfig.fileName" placeholder="不填则自动生成" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button type="primary" :loading="exporting" @click="handleExport">导出</a-button>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

// ============ Props ============
interface Props {
  rowKey?: string;
  enableSelection?: boolean;
  selectionType?: 'checkbox' | 'radio';
  showSummary?: boolean;
  showPagination?: boolean;
  paginationMode?: 'minimal' | 'simple' | 'medium' | 'standard';
}

const props = withDefaults(defineProps<Props>(), {
  rowKey: 'id',
  enableSelection: true,
  selectionType: 'checkbox',
  showSummary: true,
  showPagination: true,
  paginationMode: 'standard',
});

// ============ 状态 ============
const loading = ref(false);
const dataSource = ref<any[]>([]);
const selectedRowKeys = ref<(string | number)[]>([]);
const settingsVisible = ref(false);
const exportVisible = ref(false);
const exporting = ref(false);

const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
});

// ============ 行选择 ============
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  type: props.selectionType,
  fixed: true,
  onChange: (keys: (string | number)[]) => { selectedRowKeys.value = keys; },
}));

// ============ 斑马纹 ============
const zebraRowClass = (_record: any, index: number) => {
  return index % 2 === 0 ? 'row-even' : 'row-odd';
};

// ============ 格式化 ============
const formatAmount = (value: number | null | undefined) => {
  if (value == null) return '';
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

const showTotal = (total: number, range: number[]) => `共 ${total} 条，当前 ${range[0]}-${range[1]} 条`;

// ============ 导出 ============
const exportConfig = ref({
  scope: 'currentPage' as 'selected' | 'currentPage' | 'all',
  columns: [] as string[],
  fileName: '',
});
</script>

<style scoped>
.data-table-wrapper { display: flex; flex-direction: column; height: 100%; }

/* 斑马纹 */
:deep(.row-even) { background: #fff; }
:deep(.row-odd) { background: #fafafa; }

/* 标题行加粗居中 */
:deep(.ant-table-thead > tr > th) { font-weight: 600; text-align: center; }

/* 超链接列 */
.table-link { color: #434343; text-decoration: underline; }
.table-link:hover { color: #1890ff; }

/* 合计行 */
:deep(.total-summary-row) { background: #fafafa; font-weight: 600; }
:deep(.selected-summary-row) { background: #e6f7ff; }

/* 分页栏 */
.pagination-bar { display: flex; justify-content: flex-end; padding: 12px 0; }

/* 表格设置 */
.table-settings { display: flex; gap: 16px; }
.settings-enabled, .settings-all { flex: 1; }
.settings-zone { margin-bottom: 12px; }
.settings-zone__title { font-size: 12px; color: #8c8c8c; margin-bottom: 4px; }

/* 滚动条 */
:deep(.ant-table-body)::-webkit-scrollbar { width: 6px; height: 6px; }
:deep(.ant-table-body)::-webkit-scrollbar-thumb { background: transparent; border-radius: 3px; }
:deep(.ant-table-body):hover::-webkit-scrollbar-thumb { background: #c1c1c1; }
</style>
```

## 列定义规范

```typescript
/** 列类型定义示例 */
const columns = [
  // 行号列（冻结左侧）
  { title: '序号', key: 'rowIndex', width: 60, fixed: 'left', align: 'center' },
  // 文字列（左对齐）
  { title: '名称', dataIndex: 'name', width: 200, align: 'left', ellipsis: true },
  // 金额列（右对齐，千分符，两位小数）
  { title: '金额', dataIndex: 'amount', width: 150, align: 'right', dataType: 'amount' },
  // 状态列（居中）
  { title: '状态', dataIndex: 'status', width: 100, align: 'center', dataType: 'status' },
  // 超链接列
  { title: '凭证号', dataIndex: 'voucherNo', width: 150, linkable: true },
  // 操作列（冻结右侧）
  { title: '操作', key: 'action', width: 120, fixed: 'right', align: 'center' },
];
```
