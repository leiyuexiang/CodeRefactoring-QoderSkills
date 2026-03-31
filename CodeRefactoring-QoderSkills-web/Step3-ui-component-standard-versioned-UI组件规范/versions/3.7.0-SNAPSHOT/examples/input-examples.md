# 录入框组件样例

> 规范来源：一体化系统界面规范 - 控件 - 录入框

## 1. 文本框

```vue
<template>
  <a-form-item label="支出功能分类" :required="true">
    <a-input
      v-model:value="formData.functionCategory"
      placeholder="请输入支出功能分类"
      allow-clear
    />
  </a-form-item>
</template>

<style scoped>
/* 必填项标签前红色星号由 a-form-item :required 自动处理 */
/* 星号颜色: #FF4D4F */
/* 标签文字: #595959, 14px */
/* 内容文字: #434343, 14px */
/* 占位提示: #BFBFBF */
/* 错误信息: #FF4D4F */
</style>
```

## 2. 下拉框

```vue
<template>
  <a-form-item label="预算级次" :required="true">
    <a-select
      v-model:value="formData.budgetLevel"
      placeholder="请选择预算级次"
      allow-clear
      :options="budgetLevelOptions"
      :dropdown-match-select-width="false"
    >
    </a-select>
  </a-form-item>
</template>

<script setup lang="ts">
/**
 * 下拉框规范：
 * - 下拉列表默认和控件等宽，可超过控件宽度（:dropdown-match-select-width="false"）
 * - 选项高度 32px，左右边距 8px，文字边距 8px
 * - 悬浮背景: #F5F5F5
 * - 选中背景: #E6F7FF
 * - 非激活/未输入时显示下拉图标
 * - 鼠标悬停且有内容时显示删除图标（allow-clear）
 * - 删除图标大小: 16px
 */
</script>

<style scoped>
:deep(.ant-select-dropdown) {
  .ant-select-item {
    height: 32px;
    line-height: 32px;
    padding: 0 8px;
  }
  .ant-select-item-option-content {
    padding: 0 8px;
  }
  .ant-select-item-option-active:not(.ant-select-item-option-disabled) {
    background-color: #f5f5f5;
  }
  .ant-select-item-option-selected:not(.ant-select-item-option-disabled) {
    background-color: #e6f7ff;
  }
}
</style>
```

## 3. 日期框

```vue
<template>
  <!-- 单日期选择 -->
  <a-form-item label="编制日期">
    <a-date-picker
      v-model:value="formData.createDate"
      placeholder="请选择日期"
      style="width: 100%"
    />
  </a-form-item>

  <!-- 日期范围（封装为一个控件使用） -->
  <a-form-item label="申请日期">
    <a-range-picker
      v-model:value="formData.dateRange"
      style="width: 100%"
    />
  </a-form-item>
</template>
```

**注意：日期框的下拉面板是固定高度和宽度，删除图标大小 16px。**

## 4. 复选框

```vue
<template>
  <a-form-item label="资金性质">
    <a-checkbox-group v-model:value="formData.fundTypes" :options="fundTypeOptions" />
  </a-form-item>
</template>
```

## 5. 单选框

```vue
<template>
  <a-form-item label="支付方式">
    <a-radio-group v-model:value="formData.payMethod" :options="payMethodOptions" />
  </a-form-item>
</template>
```

## 6. 禁用态

```vue
<template>
  <!-- 禁用控件背景色 #FAFAFA，文字颜色 #BFBFBF -->
  <a-form-item label="创建人">
    <a-input v-model:value="formData.creator" disabled />
  </a-form-item>
</template>

<style scoped>
:deep(.ant-input-disabled) {
  background-color: #fafafa;
  color: #bfbfbf;
}
:deep(.ant-select-disabled .ant-select-selector) {
  background-color: #fafafa !important;
  color: #bfbfbf !important;
}
</style>
```

## 7. 录入卡片完整示例

标签和录入框推荐上下结构，标签统一计算宽度，最大不超过 8 个汉字。

```vue
<template>
  <div class="form-card">
    <div class="form-card__header">
      <span class="form-card__title">基本信息</span>
    </div>
    <div class="form-card__body">
      <a-form
        :model="formData"
        :label-col="{ style: { width: '112px' } }"
        label-align="right"
      >
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="支出功能分类" :required="true">
              <a-input v-model:value="formData.functionCategory" placeholder="请输入" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="预算单位" :required="true">
              <a-select v-model:value="formData.unit" placeholder="请选择" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="编制日期">
              <a-date-picker v-model:value="formData.createDate" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
.form-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #434343;
}
/* 标签: 14px, #595959 */
:deep(.ant-form-item-label > label) {
  font-size: 14px;
  color: #595959;
}
/* 控件内容: 14px, #434343 */
:deep(.ant-input),
:deep(.ant-select-selection-item) {
  font-size: 14px;
  color: #434343;
}
/* 提示文字: 12px */
:deep(.ant-form-item-explain) {
  font-size: 12px;
}
/* 必填星号: #FF4D4F */
:deep(.ant-form-item-required::before) {
  color: #ff4d4f !important;
}
</style>
```

## 规范要点

- 标签和录入框左右结构，标签在左，录入框在右
- 控件标签和内容文字 14px，提示内容 12px
- 框内提示文字颜色 `#BFBFBF`
- 标签文字颜色 `#595959`，内容文字颜色 `#434343`
- 错误信息颜色 `#FF4D4F`
- 必填标签前加红色星号 `*`（颜色 `#FF4D4F`），不改变录入框背景色
- 禁用控件背景色 `#FAFAFA`，文字颜色 `#BFBFBF`
- 下拉图标/删除图标大小 16px
