# 卡片组件样例

> 规范来源：一体化系统界面规范 - 业务系统部分 - 数据区

## 1. 详情卡片

```vue
<template>
  <div class="detail-card">
    <div class="detail-card__header">
      <div class="detail-card__header-left">
        <span class="detail-card__title">基本信息</span>
        <!-- 标签/热点分类 -->
        <a-tag color="red">三保</a-tag>
        <a-tag color="orange">监控预警</a-tag>
      </div>
      <div class="detail-card__header-right">
        <!-- 操作按钮（图标按钮） -->
        <a-tooltip title="打印">
          <a-button type="text" size="small"><PrinterOutlined /></a-button>
        </a-tooltip>
        <a-divider type="vertical" />
        <a-tooltip title="刷新">
          <a-button type="text" size="small"><ReloadOutlined /></a-button>
        </a-tooltip>
        <!-- 折叠/展开按钮（最右侧） -->
        <a-tooltip :title="collapsed ? '展开' : '折叠'">
          <a-button type="text" size="small" @click="toggleCollapse">
            <UpOutlined v-if="!collapsed" />
            <DownOutlined v-if="collapsed" />
          </a-button>
        </a-tooltip>
      </div>
    </div>

    <div class="detail-card__body" v-show="!collapsed">
      <a-descriptions :column="3" :label-style="labelStyle" :content-style="contentStyle">
        <a-descriptions-item label="凭证号">
          <a class="detail-link" @click="viewVoucher">ZF2024001234</a>
        </a-descriptions-item>
        <a-descriptions-item label="预算单位">
          <span>辽宁省公安厅</span>
          <!-- 快捷操作图标 -->
          <a-tooltip title="查看单位预算执行分析">
            <BarChartOutlined
              class="quick-action-icon"
              @click="viewUnitAnalysis"
            />
          </a-tooltip>
        </a-descriptions-item>
        <a-descriptions-item label="申请金额">
          {{ formatAmount(1234567.89) }}
        </a-descriptions-item>
        <a-descriptions-item label="账号">
          1234 5678 9012 3456
        </a-descriptions-item>
        <a-descriptions-item label="身份证号">
          2101 0119 9001 0112 34
        </a-descriptions-item>
        <a-descriptions-item label="创建日期">
          2024-01-15
        </a-descriptions-item>
      </a-descriptions>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const collapsed = ref(false);

const toggleCollapse = () => {
  collapsed.value = !collapsed.value;
};

/** 金额格式化：保留两位小数 + 千分符 */
const formatAmount = (value: number) => {
  return value.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};

const labelStyle = { color: '#595959', fontSize: '14px', width: '112px' };
const contentStyle = { color: '#434343', fontSize: '14px' };
</script>

<style scoped>
.detail-card {
  border: 1px solid #f0f0f0;
  border-radius: 2px;
  margin-bottom: 16px;
}
.detail-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.detail-card__header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.detail-card__header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}
.detail-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #434343;
}
.detail-card__body {
  padding: 16px;
}
/* 标签和内容之间用中文冒号分隔，颜色不同 */
:deep(.ant-descriptions-item-label) {
  color: #595959;
}
:deep(.ant-descriptions-item-content) {
  color: #434343;
}
/* 超链接 */
.detail-link {
  color: #1890ff;
  text-decoration: underline;
  cursor: pointer;
}
/* 快捷操作图标 */
.quick-action-icon {
  margin-left: 8px;
  color: #8c8c8c;
  cursor: pointer;
}
.quick-action-icon:hover {
  color: #1890ff;
}
</style>
```

## 2. 半折叠状态卡片

```vue
<template>
  <div class="detail-card">
    <div class="detail-card__header">
      <span class="detail-card__title">指标信息</span>
      <a-button type="text" size="small" @click="toggleHalfCollapse">
        <UpOutlined v-if="expanded" />
        <DownOutlined v-if="!expanded" />
      </a-button>
    </div>
    <!-- 半折叠态：显示部分重要信息 -->
    <div class="detail-card__body detail-card__body--summary" v-if="!expanded">
      <a-descriptions :column="4" size="small">
        <a-descriptions-item label="功能分类">2050101 一般行政管理事务</a-descriptions-item>
        <a-descriptions-item label="经济分类">30101 基本工资</a-descriptions-item>
        <a-descriptions-item label="预算金额">{{ formatAmount(500000) }}</a-descriptions-item>
        <a-descriptions-item label="已执行">{{ formatAmount(320000) }}</a-descriptions-item>
      </a-descriptions>
    </div>
    <!-- 展开态：显示全部信息 -->
    <div class="detail-card__body" v-if="expanded">
      <a-descriptions :column="3">
        <!-- 全部字段 -->
      </a-descriptions>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 半折叠卡片规范：
 * - 半折叠状态不隐藏所有信息，显示部分重要信息
 * - 有半折叠状态的卡片不再保留全折叠功能
 * - 仅保留半折叠、展开两个状态
 * - 默认可设为半折叠状态
 */
</script>
```

## 3. 录入卡片

```vue
<template>
  <div class="form-card">
    <div class="form-card__header">
      <span class="form-card__title">录入信息</span>
      <a-button type="text" size="small" @click="toggleCollapse">
        <UpOutlined v-if="!collapsed" />
        <DownOutlined v-if="collapsed" />
      </a-button>
    </div>
    <div class="form-card__body" v-show="!collapsed">
      <a-form
        :model="formData"
        :rules="formRules"
        :label-col="{ style: { width: '112px' } }"
        label-align="right"
      >
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="支出功能分类" name="funcCategory" required>
              <a-tree-select
                v-model:value="formData.funcCategory"
                placeholder="请选择支出功能分类"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="预算单位" name="unit" required>
              <a-select v-model:value="formData.unit" placeholder="请选择预算单位" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="金额" name="amount" required>
              <a-input-number
                v-model:value="formData.amount"
                placeholder="请输入金额"
                :precision="2"
                :formatter="(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')"
                :parser="(value) => value.replace(/,/g, '')"
                style="width: 100%; text-align: right"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="编制日期" name="createDate">
              <a-date-picker v-model:value="formData.createDate" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="备注" name="remark">
              <a-input v-model:value="formData.remark" placeholder="请输入备注" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
/**
 * 录入卡片规范：
 * - 必填字段标签加红色星号 *
 * - 标签和控件字体颜色不同
 * - 控件内容默认左对齐，数值控件右对齐
 * - 必须设置输入提示
 * - 提示信息颜色与实际输入不同
 * - 不可编辑的下拉框点击也可展开/收起
 * - 标签最大宽度 8 个汉字（约 112px）
 */
.form-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #434343;
}
</style>
```

## 4. 列表卡片

```vue
<template>
  <div class="list-card">
    <div class="list-card__header">
      <span class="list-card__title">明细列表</span>
      <div class="list-card__actions">
        <a-tooltip title="新增行"><a-button type="text" size="small"><PlusOutlined /></a-button></a-tooltip>
        <a-tooltip title="删除行"><a-button type="text" size="small"><DeleteOutlined /></a-button></a-tooltip>
        <a-divider type="vertical" />
        <a-tooltip title="刷新"><a-button type="text" size="small"><ReloadOutlined /></a-button></a-tooltip>
      </div>
    </div>
    <div class="list-card__body">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        bordered
        size="small"
        :row-class-name="zebraRowClass"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 列表卡片规范：
 * - 少量数据（上级页面带入）：无查询条件、无分页、无复选框
 * - 大量数据：有查询条件、有分页、可有选择框
 * - 可编辑表格：可编辑列背景浅黄 #FEFFE6，必填列标题加红 *
 * - 少数可编辑列建议冻结在左侧或右侧
 */
</script>
```

## 规范要点

- 卡片标题 16px，位于左上角，字体比正文大一号
- 卡片右上角为操作区，操作按钮都是图标按钮
- 状态按钮在最右侧，其他按钮从右到左按重要性排列
- 按钮分组间用竖线分隔
- 卡片有展开和折叠两种状态
- 只有一个卡片时可不显示折叠按钮
- 详情字段居左对齐，标签和内容用中文冒号分隔
- 金额保留两位小数并加千分符
- 账号、身份证号等可插入空格增加可读性
- 支持超链接查看关联信息
