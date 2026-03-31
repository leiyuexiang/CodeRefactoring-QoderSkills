# 详情卡片模板

> 含字段格式化、超链接、快捷操作图标的详情卡片模板。

## 模板代码

```vue
<template>
  <div class="detail-card">
    <div class="detail-card__header">
      <div class="detail-card__header-left">
        <span class="detail-card__title">{{ title }}</span>
        <a-tag v-for="tag in tags" :key="tag.text" :color="tag.color">{{ tag.text }}</a-tag>
      </div>
      <div class="detail-card__header-right">
        <slot name="actions" />
        <a-divider type="vertical" v-if="$slots.actions" />
        <a-button v-if="collapsible" type="text" size="small" @click="toggleState">
          <UpOutlined v-if="currentState === 'expanded'" />
          <DownOutlined v-if="currentState !== 'expanded'" />
        </a-button>
      </div>
    </div>

    <!-- 半折叠态：显示摘要信息 -->
    <div class="detail-card__summary" v-if="currentState === 'half' && summaryFields.length > 0">
      <a-descriptions :column="summaryColumns" size="small" :label-style="summaryLabelStyle" :content-style="summaryContentStyle">
        <a-descriptions-item v-for="field in summaryFields" :key="field.key" :label="field.label">
          {{ formatFieldValue(field) }}
        </a-descriptions-item>
      </a-descriptions>
    </div>

    <!-- 展开态：显示全部信息 -->
    <div class="detail-card__body" v-if="currentState === 'expanded'">
      <a-descriptions :column="columns" :label-style="labelStyle" :content-style="contentStyle">
        <a-descriptions-item v-for="field in fields" :key="field.key" :label="field.label">
          <!-- 超链接 -->
          <template v-if="field.link">
            <a class="detail-link" @click="field.linkHandler?.()">{{ formatFieldValue(field) }}</a>
            <a-tooltip v-if="field.quickAction" :title="field.quickAction.tooltip">
              <component
                :is="field.quickAction.icon"
                class="quick-icon"
                @click="field.quickAction.handler?.()"
              />
            </a-tooltip>
          </template>
          <!-- 带快捷操作图标 -->
          <template v-else-if="field.quickAction">
            <span>{{ formatFieldValue(field) }}</span>
            <a-tooltip :title="field.quickAction.tooltip">
              <component :is="field.quickAction.icon" class="quick-icon" @click="field.quickAction.handler?.()" />
            </a-tooltip>
          </template>
          <!-- 普通字段 -->
          <template v-else>
            {{ formatFieldValue(field) }}
          </template>
        </a-descriptions-item>
      </a-descriptions>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

type CardState = 'expanded' | 'half' | 'collapsed';

interface QuickAction {
  tooltip: string;
  icon: any;
  handler?: () => void;
}

interface DetailField {
  key: string;
  label: string;
  value: any;
  type?: 'text' | 'amount' | 'date' | 'account' | 'idCard';
  link?: boolean;
  linkHandler?: () => void;
  quickAction?: QuickAction;
}

interface Props {
  title: string;
  fields: DetailField[];
  summaryFields?: DetailField[];
  tags?: { text: string; color: string }[];
  columns?: number;
  summaryColumns?: number;
  collapsible?: boolean;
  halfCollapsible?: boolean;
  defaultState?: CardState;
}

const props = withDefaults(defineProps<Props>(), {
  columns: 3,
  summaryColumns: 4,
  collapsible: true,
  halfCollapsible: false,
  defaultState: 'expanded',
  tags: () => [],
  summaryFields: () => [],
});

const currentState = ref<CardState>(props.defaultState);

const toggleState = () => {
  if (props.halfCollapsible) {
    currentState.value = currentState.value === 'expanded' ? 'half' : 'expanded';
  } else {
    currentState.value = currentState.value === 'expanded' ? 'collapsed' : 'expanded';
  }
};

/**
 * 字段格式化规范：
 * - 金额：千分符 + 两位小数
 * - 账号：每4位空格分隔 (1234 5678 9012 3456)
 * - 身份证号：中间空格分隔 (2101 0119 9001 0112 34)
 */
const formatFieldValue = (field: DetailField) => {
  const val = field.value;
  if (val == null || val === '') return '-';
  switch (field.type) {
    case 'amount':
      return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    case 'account':
      return String(val).replace(/(\d{4})(?=\d)/g, '$1 ');
    case 'idCard':
      return String(val).replace(/(\d{4})(?=\d)/g, '$1 ');
    default:
      return String(val);
  }
};

const labelStyle = { color: '#595959', fontSize: '14px' };
const contentStyle = { color: '#434343', fontSize: '14px' };
const summaryLabelStyle = { color: '#595959', fontSize: '12px' };
const summaryContentStyle = { color: '#434343', fontSize: '12px' };
</script>

<style scoped>
.detail-card { border: 1px solid #f0f0f0; border-radius: 2px; margin-bottom: 16px; }
.detail-card__header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #f0f0f0; }
.detail-card__header-left { display: flex; align-items: center; gap: 8px; }
.detail-card__header-right { display: flex; align-items: center; gap: 4px; }
.detail-card__title { font-size: 16px; font-weight: 600; color: #434343; }
.detail-card__summary { padding: 12px 16px; background: #fafafa; }
.detail-card__body { padding: 16px; }

/* 标签和内容通过中文冒号分隔（Descriptions 自带） */
:deep(.ant-descriptions-item-label) { color: #595959; }
:deep(.ant-descriptions-item-content) { color: #434343; }

/* 超链接 */
.detail-link { color: #1890ff; text-decoration: underline; cursor: pointer; }
.detail-link:hover { color: #40a9ff; }

/* 快捷操作图标 */
.quick-icon { margin-left: 8px; color: #8c8c8c; cursor: pointer; font-size: 14px; }
.quick-icon:hover { color: #1890ff; }
</style>
```

## 使用示例

```vue
<DetailCard
  title="指标信息"
  :fields="detailFields"
  :summary-fields="summaryFields"
  :tags="[{ text: '三保', color: 'red' }]"
  :columns="3"
  :half-collapsible="true"
  default-state="half"
/>
```
