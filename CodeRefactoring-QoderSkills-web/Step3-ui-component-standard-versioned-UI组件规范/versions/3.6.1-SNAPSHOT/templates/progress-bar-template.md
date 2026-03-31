# 进度条模板

> 普通进度条和可中止进度条两种模式。

## 模板代码

```vue
<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :width="480"
    centered
    :closable="false"
    :mask-closable="false"
    :keyboard="false"
    :footer="null"
  >
    <div class="progress-content">
      <div class="progress-info">
        <span class="progress-info__text">{{ statusText }}</span>
        <span class="progress-info__percent">{{ percent }}%</span>
      </div>

      <a-progress
        :percent="percent"
        :status="progressStatus"
        :stroke-color="strokeColor"
      />

      <div class="progress-detail">
        <span>已处理 {{ processed }}/{{ total }} 条</span>
        <span v-if="estimatedTime">预计剩余 {{ estimatedTime }}</span>
      </div>

      <!-- 可中止模式 -->
      <div class="progress-actions" v-if="cancellable && !finished">
        <a-dropdown v-if="cancelOptions.length > 1">
          <a-button danger>
            中止 <DownOutlined />
          </a-button>
          <template #overlay>
            <a-menu @click="handleCancel">
              <a-menu-item key="submitPartial">已完成部分提交，未完成部分取消</a-menu-item>
              <a-menu-item key="cancelAll">全部取消，数据回滚</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button v-else danger @click="handleCancel({ key: 'cancelAll' })">
          取消
        </a-button>
      </div>

      <!-- 完成后关闭 -->
      <div class="progress-actions" v-if="finished">
        <a-button type="primary" @click="handleClose">
          {{ finishedWithError ? '查看结果' : '确定' }}
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

interface Props {
  cancellable?: boolean;
  cancelOptions?: ('submitPartial' | 'cancelAll')[];
}

const props = withDefaults(defineProps<Props>(), {
  cancellable: false,
  cancelOptions: () => ['submitPartial', 'cancelAll'],
});

const emit = defineEmits<{
  cancel: [mode: 'submitPartial' | 'cancelAll'];
  close: [];
}>();

const visible = ref(false);
const title = ref('处理中');
const total = ref(0);
const processed = ref(0);
const statusText = ref('正在处理...');
const estimatedTime = ref('');
const finished = ref(false);
const finishedWithError = ref(false);
const cancelled = ref(false);

const percent = computed(() => {
  if (total.value === 0) return 0;
  return Math.round((processed.value / total.value) * 100);
});

const progressStatus = computed(() => {
  if (cancelled.value) return 'exception';
  if (finishedWithError.value) return 'exception';
  if (finished.value) return 'success';
  return 'active';
});

const strokeColor = computed(() => {
  if (cancelled.value || finishedWithError.value) return '#ff4d4f';
  if (finished.value) return '#52c41a';
  return '#1890ff';
});

// ============ 时间预估 ============
const startTime = ref(0);
watch(processed, (val) => {
  if (val === 0) return;
  const elapsed = Date.now() - startTime.value;
  const avgTime = elapsed / val;
  const remaining = (total.value - val) * avgTime;
  if (remaining > 60000) {
    estimatedTime.value = `${Math.ceil(remaining / 60000)} 分钟`;
  } else {
    estimatedTime.value = `${Math.ceil(remaining / 1000)} 秒`;
  }
});

// ============ 公共方法 ============
const open = (config: { title?: string; total: number }) => {
  title.value = config.title || '处理中';
  total.value = config.total;
  processed.value = 0;
  finished.value = false;
  finishedWithError.value = false;
  cancelled.value = false;
  statusText.value = '正在处理...';
  estimatedTime.value = '';
  startTime.value = Date.now();
  visible.value = true;
};

const updateProgress = (count: number, text?: string) => {
  processed.value = count;
  if (text) statusText.value = text;
};

const finish = (config?: { hasError?: boolean; message?: string }) => {
  finished.value = true;
  finishedWithError.value = config?.hasError || false;
  statusText.value = config?.message || '处理完成';
  estimatedTime.value = '';
};

const handleCancel = (e: { key: string }) => {
  cancelled.value = true;
  statusText.value = '正在取消...';
  emit('cancel', e.key as 'submitPartial' | 'cancelAll');
};

const handleClose = () => {
  visible.value = false;
  emit('close');
};

defineExpose({ open, updateProgress, finish });
</script>

<style scoped>
.progress-content {
  padding: 8px 0;
}
.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.progress-info__text {
  color: #434343;
  font-size: 14px;
}
.progress-info__percent {
  color: #1890ff;
  font-size: 14px;
  font-weight: 600;
}
.progress-detail {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #8c8c8c;
}
.progress-actions {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
```

## 使用示例

```vue
<script setup>
const progressRef = ref();

const handleBatchProcess = async () => {
  progressRef.value.open({ title: '批量送审', total: 100 });
  for (let i = 1; i <= 100; i++) {
    await processItem(i);
    progressRef.value.updateProgress(i, `正在处理第 ${i} 条...`);
  }
  progressRef.value.finish({ message: '全部处理完成' });
};
</script>

<template>
  <ProgressBar ref="progressRef" :cancellable="true" @cancel="handleCancel" />
</template>
```

## 规范要点

- 批量处理等待时间较长时使用进度条
- 显示处理进度和预计完成时间（动态更新）
- 可中止进度条支持两种中止方式：
  - 已完成部分提交，未完成部分取消
  - 全部取消，数据回滚
