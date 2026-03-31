# 表格组件样例

> 规范来源：一体化系统界面规范 - 控件 - 表格

## 1. 普通表格（只读多选）

```vue
<template>
  <a-table
    :columns="columns"
    :data-source="dataSource"
    :row-selection="rowSelection"
    :pagination="paginationConfig"
    :scroll="{ x: 'max-content' }"
    bordered
    size="middle"
    row-key="id"
    :row-class-name="getRowClassName"
  >
    <!-- 行号列 -->
    <template #bodyCell="{ column, index }">
      <template v-if="column.key === 'rowIndex'">
        {{ (pagination.current - 1) * pagination.pageSize + index + 1 }}
      </template>
      <!-- 超链接列 -->
      <template v-if="column.key === 'voucherNo'">
        <a class="table-link" @click="handleViewVoucher(record)">
          {{ record.voucherNo }}
        </a>
      </template>
    </template>

    <!-- 合计行 -->
    <template #summary>
      <a-table-summary fixed>
        <!-- 选中合计行 -->
        <a-table-summary-row v-if="selectedRowKeys.length > 0" class="selected-summary-row">
          <a-table-summary-cell :index="0" :col-span="3">
            选中合计({{ selectedRowKeys.length }})
          </a-table-summary-cell>
          <a-table-summary-cell :index="3" align="right">
            {{ formatAmount(selectedTotal) }}
          </a-table-summary-cell>
        </a-table-summary-row>
        <!-- 全量合计行 -->
        <a-table-summary-row class="summary-row">
          <a-table-summary-cell :index="0" :col-span="3">
            合计
          </a-table-summary-cell>
          <a-table-summary-cell :index="3" align="right">
            {{ formatAmount(grandTotal) }}
          </a-table-summary-cell>
        </a-table-summary-row>
      </a-table-summary>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import type { ColumnsType } from 'ant-design-vue/es/table';

/**
 * 表格列定义规范：
 * - 第一列: 行号列（冻结左侧）
 * - 第二列: 复选框列（冻结左侧，可选）
 * - 最后列: 操作列（冻结右侧，可选）
 * - 标题行: 加粗居中
 * - 内容行: 文字列左对齐，数值列右对齐，状态列居中
 * - 冻结列每侧不超过 3 列
 */
const columns: ColumnsType = [
  {
    title: '序号',
    key: 'rowIndex',
    width: 60,
    fixed: 'left',
    align: 'center',
  },
  {
    title: '凭证号',
    dataIndex: 'voucherNo',
    key: 'voucherNo',
    width: 150,
    fixed: 'left',
    align: 'left',
  },
  {
    title: '预算单位',
    dataIndex: 'unitName',
    key: 'unitName',
    width: 200,
    align: 'left',
    ellipsis: true,
  },
  {
    title: '金额',
    dataIndex: 'amount',
    key: 'amount',
    width: 150,
    align: 'right',
    customRender: ({ text }) => formatAmount(text),
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
    align: 'center',
  },
  {
    title: '创建日期',
    dataIndex: 'createDate',
    key: 'createDate',
    width: 120,
    align: 'center',
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right',
    align: 'center',
  },
];

/** 斑马纹：奇数行白色，偶数行浅灰色 */
const getRowClassName = (_record: any, index: number) => {
  return index % 2 === 0 ? 'row-even' : 'row-odd';
};

/** 数值列加千分分隔符，金额默认保留两位小数 */
const formatAmount = (value: number) => {
  if (value == null) return '';
  return value.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};
</script>

<style scoped>
/* 标题行加粗居中 */
:deep(.ant-table-thead > tr > th) {
  font-weight: 600;
  text-align: center;
}

/* 斑马纹 */
:deep(.row-even) {
  background-color: #ffffff;
}
:deep(.row-odd) {
  background-color: #fafafa;
}

/* 超链接列：带下划线，悬浮蓝色 */
.table-link {
  color: #434343;
  text-decoration: underline;
}
.table-link:hover {
  color: #1890ff;
}

/* 合计行 */
:deep(.summary-row) {
  background-color: #fafafa;
  font-weight: 600;
}
:deep(.selected-summary-row) {
  background-color: #e6f7ff;
}
</style>
```

## 2. 可编辑表格

```vue
<template>
  <a-table
    :columns="editableColumns"
    :data-source="editableData"
    bordered
    :row-class-name="getRowClassName"
  >
    <template #bodyCell="{ column, record }">
      <!-- 可编辑单元格 -->
      <template v-if="column.editable">
        <div
          class="editable-cell"
          :class="{ 'editable-cell--editing': editingKey === record.id }"
          @click="startEdit(record.id)"
        >
          <template v-if="editingKey === record.id">
            <a-input
              v-if="column.editType === 'input'"
              v-model:value="record[column.dataIndex]"
              class="editable-cell__input"
              @pressEnter="saveEdit(record.id)"
              @blur="saveEdit(record.id)"
            />
            <a-select
              v-else-if="column.editType === 'select'"
              v-model:value="record[column.dataIndex]"
              class="editable-cell__input"
              :options="column.editOptions"
              @change="saveEdit(record.id)"
            />
          </template>
          <template v-else>
            {{ record[column.dataIndex] }}
          </template>
        </div>
      </template>
    </template>
  </a-table>
</template>

<style scoped>
/**
 * 可编辑表格规范：
 * - 可编辑区域背景色: #FEFFE6（淡黄色）
 * - 如果整个表格都可编辑，保持白色背景
 * - 必填列标题加红色 *
 * - 部分必填单元格左上角加红色角标
 * - 编辑框不设外边距，充满单元格
 */
.editable-cell {
  background-color: #feffe6;
  min-height: 32px;
  cursor: pointer;
  padding: 4px 8px;
}
.editable-cell__input {
  width: 100%;
  margin: 0;
}

/* 必填列标题红色星号 */
:deep(.required-column .ant-table-column-title::before) {
  content: '* ';
  color: #ff4d4f;
}

/* 部分必填单元格左上角红色角标 */
.editable-cell--required::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  border-style: solid;
  border-width: 6px;
  border-color: #ff4d4f transparent transparent #ff4d4f;
}
</style>
```

## 3. 列筛选与排序

```vue
<template>
  <!-- 列筛选和排序图标靠右排列 -->
  <!-- 两个图标同时存在时：筛选在右，排序在左 -->
  <a-table
    :columns="filterableColumns"
    :data-source="dataSource"
    @change="handleTableChange"
  />
</template>

<script setup lang="ts">
const filterableColumns = [
  {
    title: '预算单位',
    dataIndex: 'unitName',
    sorter: true, // 排序图标在左
    filters: [     // 筛选图标在右
      { text: '单位A', value: 'A' },
      { text: '单位B', value: 'B' },
    ],
  },
];
</script>
```

## 规范要点

- 行号和复选框冻结左侧，操作栏冻结右侧
- 冻结列每侧不超过 3 列
- 斑马纹隔行换色：奇数行白色，偶数行浅灰
- 标题行加粗居中
- 文字列左对齐，数值列右对齐，状态列居中
- 数值列加千分分隔符，金额保留两位小数
- 超链接列带下划线，悬浮变蓝色
- 合计行在表格下方，合计为全量数据合计（非当前页）
- 选中合计在合计行上方，格式：`选中合计(N)`
- 列宽出厂默认值适当，保证内容正常展示
- 可编辑区域淡黄色背景 `#FEFFE6`
- 编辑框充满单元格，无外边距
