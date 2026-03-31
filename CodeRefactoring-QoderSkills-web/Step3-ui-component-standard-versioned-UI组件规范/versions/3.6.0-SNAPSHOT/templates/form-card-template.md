# 录入卡片模板

> 含表单校验、必填项标记、上下/左右布局的录入卡片模板。

## 模板代码

```vue
<template>
  <div class="form-card">
    <div class="form-card__header">
      <div class="form-card__header-left">
        <span class="form-card__title">{{ title }}</span>
        <slot name="tags" />
      </div>
      <div class="form-card__header-right">
        <slot name="actions" />
        <a-button type="text" size="small" @click="toggleCollapse" v-if="collapsible">
          <UpOutlined v-if="!collapsed" />
          <DownOutlined v-if="collapsed" />
        </a-button>
      </div>
    </div>

    <div class="form-card__body" v-show="!collapsed">
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="labelCol"
        :wrapper-col="wrapperCol"
        :label-align="labelAlign"
        :layout="layout"
      >
        <a-row :gutter="gutter">
          <a-col :span="colSpan" v-for="field in fields" :key="field.name">
            <a-form-item
              :label="field.label"
              :name="field.name"
              :required="field.required"
            >
              <!-- 文本输入 -->
              <a-input
                v-if="field.type === 'input'"
                v-model:value="formData[field.name]"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :disabled="field.disabled"
                :maxlength="field.maxLength"
                allow-clear
              />

              <!-- 多行文本 -->
              <a-textarea
                v-else-if="field.type === 'textarea'"
                v-model:value="formData[field.name]"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :rows="field.rows || 3"
                :disabled="field.disabled"
              />

              <!-- 数值（右对齐，千分符） -->
              <a-input-number
                v-else-if="field.type === 'number' || field.type === 'amount'"
                v-model:value="formData[field.name]"
                :placeholder="field.placeholder || `请输入${field.label}`"
                :precision="field.type === 'amount' ? 2 : field.precision"
                :formatter="field.type === 'amount' ? amountFormatter : undefined"
                :parser="field.type === 'amount' ? amountParser : undefined"
                :disabled="field.disabled"
                style="width: 100%; text-align: right"
              />

              <!-- 下拉选择 -->
              <a-select
                v-else-if="field.type === 'select'"
                v-model:value="formData[field.name]"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :options="field.options"
                :disabled="field.disabled"
                :mode="field.multiple ? 'multiple' : undefined"
                allow-clear
                :dropdown-match-select-width="false"
              />

              <!-- 树选择 -->
              <a-tree-select
                v-else-if="field.type === 'treeSelect'"
                v-model:value="formData[field.name]"
                :placeholder="field.placeholder || `请选择${field.label}`"
                :tree-data="field.treeData"
                :disabled="field.disabled"
                allow-clear
              />

              <!-- 日期选择 -->
              <a-date-picker
                v-else-if="field.type === 'date'"
                v-model:value="formData[field.name]"
                :disabled="field.disabled"
                style="width: 100%"
              />

              <!-- 日期范围 -->
              <a-range-picker
                v-else-if="field.type === 'dateRange'"
                v-model:value="formData[field.name]"
                :disabled="field.disabled"
                style="width: 100%"
              />

              <!-- 复选框 -->
              <a-checkbox-group
                v-else-if="field.type === 'checkbox'"
                v-model:value="formData[field.name]"
                :options="field.options"
                :disabled="field.disabled"
              />

              <!-- 单选框 -->
              <a-radio-group
                v-else-if="field.type === 'radio'"
                v-model:value="formData[field.name]"
                :options="field.options"
                :disabled="field.disabled"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import type { FormInstance } from 'ant-design-vue';

// ============ Props ============
interface FormField {
  name: string;
  label: string;
  type: 'input' | 'textarea' | 'number' | 'amount' | 'select' | 'treeSelect' | 'date' | 'dateRange' | 'checkbox' | 'radio';
  required?: boolean;
  disabled?: boolean;
  placeholder?: string;
  options?: { label: string; value: any }[];
  treeData?: any[];
  multiple?: boolean;
  maxLength?: number;
  rows?: number;
  precision?: number;
}

interface Props {
  title: string;
  fields: FormField[];
  formData: Record<string, any>;
  formRules?: Record<string, any>;
  columns?: 1 | 2 | 3;
  labelPosition?: 'left' | 'top';
  collapsible?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  columns: 3,
  labelPosition: 'left',
  collapsible: true,
});

// ============ 布局计算 ============
const colSpan = computed(() => Math.floor(24 / props.columns));
const gutter = 16;

/**
 * 布局规范：
 * - 推荐上下结构（labelPosition='top'）
 * - 左右结构时标签最大宽度 8 个汉字（约 112px）
 */
const layout = computed(() => props.labelPosition === 'top' ? 'vertical' : 'horizontal');
const labelCol = computed(() => props.labelPosition === 'left' ? { style: { width: '112px' } } : undefined);
const wrapperCol = computed(() => props.labelPosition === 'left' ? { flex: 1 } : undefined);
const labelAlign = 'right';

// ============ 折叠 ============
const collapsed = ref(false);
const toggleCollapse = () => { collapsed.value = !collapsed.value; };

// ============ 金额格式化 ============
const amountFormatter = (value: string) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
const amountParser = (value: string) => value.replace(/,/g, '');

// ============ 表单引用 ============
const formRef = ref<FormInstance>();
const validate = () => formRef.value?.validate();
const resetFields = () => formRef.value?.resetFields();

defineExpose({ validate, resetFields, formRef });
</script>

<style scoped>
.form-card { border: 1px solid #f0f0f0; border-radius: 2px; margin-bottom: 16px; }
.form-card__header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-bottom: 1px solid #f0f0f0; }
.form-card__header-left { display: flex; align-items: center; gap: 8px; }
.form-card__header-right { display: flex; align-items: center; gap: 4px; }
.form-card__title { font-size: 16px; font-weight: 600; color: #434343; }
.form-card__body { padding: 16px; }

/* 标签样式 */
:deep(.ant-form-item-label > label) { font-size: 14px; color: #595959; }
/* 内容样式 */
:deep(.ant-input), :deep(.ant-select-selection-item), :deep(.ant-input-number-input) { font-size: 14px; color: #434343; }
/* 提示文字 */
:deep(.ant-form-item-explain) { font-size: 12px; }
/* 必填星号 */
:deep(.ant-form-item-required::before) { color: #ff4d4f !important; }
/* 禁用态 */
:deep(.ant-input-disabled), :deep(.ant-select-disabled .ant-select-selector) { background-color: #fafafa !important; color: #bfbfbf !important; }
</style>
```

## 使用示例

```vue
<FormCard
  title="基本信息"
  :fields="[
    { name: 'unit', label: '预算单位', type: 'treeSelect', required: true },
    { name: 'funcCategory', label: '支出功能分类', type: 'treeSelect', required: true },
    { name: 'amount', label: '申请金额', type: 'amount', required: true },
    { name: 'createDate', label: '编制日期', type: 'date' },
    { name: 'remark', label: '备注', type: 'textarea' },
  ]"
  :form-data="formData"
  :columns="3"
  label-position="left"
/>
```
