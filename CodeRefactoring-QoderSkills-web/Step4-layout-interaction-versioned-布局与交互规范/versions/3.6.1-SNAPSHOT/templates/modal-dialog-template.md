# 模式对话框模板

> 提示框、确认框、录入框三种模式对话框模板。

## 1. 提示框模板

```vue
<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :closable="true"
    centered
    :width="400"
    :footer="null"
  >
    <div class="modal-body">
      <component :is="iconComponent" :class="['modal-icon', `modal-icon--${type}`]" />
      <div class="modal-message">{{ message }}</div>
    </div>
    <div class="modal-actions">
      <a-button type="primary" @click="handleConfirm">确定</a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import {
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  CloseCircleOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons-vue';

type ModalType = 'success' | 'warning' | 'error' | 'info';

const visible = ref(false);
const title = ref('提示');
const message = ref('');
const type = ref<ModalType>('info');

const iconMap = {
  success: CheckCircleOutlined,
  warning: ExclamationCircleOutlined,
  error: CloseCircleOutlined,
  info: InfoCircleOutlined,
};
const iconComponent = computed(() => iconMap[type.value]);

const open = (config: { title?: string; message: string; type?: ModalType }) => {
  title.value = config.title || '提示';
  message.value = config.message;
  type.value = config.type || 'info';
  visible.value = true;
};

const handleConfirm = () => { visible.value = false; };
defineExpose({ open });
</script>

<style scoped>
.modal-body { display: flex; align-items: flex-start; gap: 12px; }
.modal-icon { font-size: 22px; flex-shrink: 0; margin-top: 2px; }
.modal-icon--success { color: #52c41a; }
.modal-icon--warning { color: #faad14; }
.modal-icon--error { color: #ff4d4f; }
.modal-icon--info { color: #1890ff; }
.modal-message { color: #434343; font-size: 14px; line-height: 1.6; }
.modal-actions { display: flex; justify-content: flex-end; margin-top: 24px; }
</style>
```

## 2. 确认框模板

```vue
<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    centered
    :width="420"
    @ok="handleOk"
    @cancel="handleCancel"
    :ok-text="okText"
    :cancel-text="cancelText"
    :ok-button-props="{ danger: isDanger }"
    :confirm-loading="loading"
  >
    <div class="modal-body">
      <ExclamationCircleOutlined :class="['modal-icon', isDanger ? 'modal-icon--error' : 'modal-icon--warning']" />
      <div class="modal-message">{{ message }}</div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 确认框规范：
 * - 确定按钮在右侧，蓝色背景（危险操作时红色）
 * - 取消按钮在左侧
 * - 按钮在页面右下角
 */
import { ref } from 'vue';

const visible = ref(false);
const title = ref('确认');
const message = ref('');
const okText = ref('确定');
const cancelText = ref('取消');
const isDanger = ref(false);
const loading = ref(false);

let resolvePromise: ((value: boolean) => void) | null = null;

const open = (config: {
  title?: string;
  message: string;
  okText?: string;
  cancelText?: string;
  danger?: boolean;
}): Promise<boolean> => {
  title.value = config.title || '确认';
  message.value = config.message;
  okText.value = config.okText || '确定';
  cancelText.value = config.cancelText || '取消';
  isDanger.value = config.danger || false;
  visible.value = true;
  return new Promise((resolve) => { resolvePromise = resolve; });
};

const handleOk = () => { visible.value = false; resolvePromise?.(true); };
const handleCancel = () => { visible.value = false; resolvePromise?.(false); };
defineExpose({ open });
</script>
```

## 3. 单栏录入框模板（440px）

```vue
<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :width="440"
    centered
    :confirm-loading="saving"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 18 }"
    >
      <slot name="form-items" :formData="formData">
        <!-- 默认表单项 -->
        <a-form-item label="审核意见" name="opinion">
          <a-textarea
            v-model:value="formData.opinion"
            placeholder="请输入审核意见"
            :rows="4"
          />
        </a-form-item>
      </slot>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 单栏录入框规范：
 * - 宽度固定 440px
 * - 高度自适应，280px ~ 720px
 * - 内容多时使用两栏或全屏页面
 */
</script>
```

## 4. 两栏录入框模板（856px）

```vue
<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :width="856"
    centered
    :confirm-loading="saving"
    @ok="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 20 }"
    >
      <a-row :gutter="16">
        <slot name="form-items" :formData="formData">
          <a-col :span="12">
            <a-form-item label="字段1" name="field1" required>
              <a-input v-model:value="formData.field1" placeholder="请输入" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="字段2" name="field2">
              <a-select v-model:value="formData.field2" placeholder="请选择" />
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label="备注" name="remark" :label-col="{ span: 2 }" :wrapper-col="{ span: 22 }">
              <a-textarea v-model:value="formData.remark" :rows="3" placeholder="请输入" />
            </a-form-item>
          </a-col>
        </slot>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 两栏录入框规范：
 * - 宽度固定 856px
 * - 高度自适应，520px ~ 720px
 * - 内容少时底部留白
 * - 内容多时使用全屏页面
 */
</script>
```

## 尺寸对照表

| 类型 | 宽度 | 高度范围 | 适用场景 |
|------|------|----------|----------|
| 提示框 | 400px | 自适应 | 系统提示、错误提示 |
| 确认框 | 420px | 自适应 | 删除确认、操作确认 |
| 单栏录入 | 440px | 280-720px | 审核意见、少量输入 |
| 两栏录入 | 856px | 520-720px | 中等信息输入 |
| 弹窗宽高比 | > 0.6 | - | 所有弹窗 |
