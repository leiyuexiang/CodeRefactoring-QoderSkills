# 抽屉二级页面模板

> 从屏幕右侧弹出的二级页面，支持详情和录入两种模式。

## 模板代码

```vue
<template>
  <a-drawer
    v-model:open="visible"
    :title="null"
    :width="drawerWidth"
    :mask="mode === 'input'"
    :mask-closable="mode === 'detail'"
    :closable="false"
    :body-style="{ padding: 0, display: 'flex', flexDirection: 'column', height: '100%' }"
    @close="handleClose"
  >
    <!-- ========== 标题栏 ========== -->
    <div class="drawer-titlebar">
      <!-- 返回按钮（最左侧，多级页面时显示） -->
      <a-button
        v-if="pageStack.length > 1"
        type="text"
        size="small"
        class="drawer-titlebar__back"
        @click="goBack"
      >
        <LeftOutlined />
      </a-button>

      <!-- 面包屑或标题 -->
      <div class="drawer-titlebar__nav">
        <a-breadcrumb v-if="pageStack.length > 1">
          <a-breadcrumb-item v-for="(page, index) in pageStack" :key="index">
            <a v-if="index < pageStack.length - 1" @click="navigateTo(index)">{{ page.title }}</a>
            <span v-else>{{ page.title }}</span>
          </a-breadcrumb-item>
        </a-breadcrumb>
        <span v-else class="drawer-titlebar__title">{{ title }}</span>
      </div>

      <!-- 快捷操作按钮（返回和关闭之间） -->
      <div class="drawer-titlebar__actions">
        <a-tooltip title="刷新" v-if="mode === 'detail'">
          <a-button type="text" size="small" @click="handleRefresh"><ReloadOutlined /></a-button>
        </a-tooltip>
        <a-tooltip title="保存" v-if="mode === 'input'">
          <a-button type="text" size="small" @click="handleSave"><SaveOutlined /></a-button>
        </a-tooltip>
      </div>

      <!-- 关闭按钮（最右侧） -->
      <a-button type="text" size="small" class="drawer-titlebar__close" @click="handleClose">
        <CloseOutlined />
      </a-button>
    </div>

    <!-- ========== 内容区（可滚动） ========== -->
    <div class="drawer-body">
      <!-- 详情模式 -->
      <template v-if="mode === 'detail'">
        <a-descriptions
          :column="descriptionsColumn"
          bordered
          :label-style="{ color: '#595959', width: '120px' }"
          :content-style="{ color: '#434343' }"
        >
          <a-descriptions-item
            v-for="field in detailFields"
            :key="field.key"
            :label="field.label"
          >
            <!-- 超链接字段 -->
            <a v-if="field.link" class="detail-link" @click="field.linkHandler">
              {{ field.value }}
            </a>
            <!-- 格式化金额 -->
            <span v-else-if="field.type === 'amount'">
              {{ formatAmount(field.value) }}
            </span>
            <!-- 普通文本 -->
            <span v-else>{{ field.value }}</span>
          </a-descriptions-item>
        </a-descriptions>
      </template>

      <!-- 录入模式 -->
      <template v-if="mode === 'input'">
        <a-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          :label-col="{ style: { width: '112px' } }"
          label-align="right"
        >
          <a-row :gutter="16">
            <a-col :span="colSpan" v-for="field in formFields" :key="field.name">
              <a-form-item
                :label="field.label"
                :name="field.name"
                :required="field.required"
              >
                <component
                  :is="field.component"
                  v-model:value="formData[field.name]"
                  v-bind="field.props"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </div>

    <!-- ========== 底部操作栏（录入模式） ========== -->
    <div class="drawer-footer" v-if="mode === 'input'">
      <a-button @click="handleClose">取消</a-button>
      <a-button type="primary" :loading="saving" @click="handleSubmit">
        {{ submitButtonLabel }}
      </a-button>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

interface Props {
  mode?: 'detail' | 'input';
  title?: string;
  width?: 'single' | 'double';
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'detail',
  title: '详情',
  width: 'single',
});

const visible = ref(false);
const saving = ref(false);
const submitButtonLabel = ref('保存');

/**
 * 抽屉宽度规范：
 * - 一列抽屉：480px
 * - 两列抽屉：720px
 * - 总宽度不超过屏幕一半
 */
const drawerWidth = computed(() => {
  return props.width === 'double' ? 720 : 480;
});

/** 描述列表列数根据宽度适配 */
const descriptionsColumn = computed(() => {
  return props.width === 'double' ? 2 : 1;
});

/** 表单列数根据宽度适配 */
const colSpan = computed(() => {
  return props.width === 'double' ? 12 : 24;
});

// ============ 页面导航栈 ============
interface PageInfo { title: string; key: string; }
const pageStack = ref<PageInfo[]>([]);

const goBack = () => {
  if (pageStack.value.length > 1) {
    pageStack.value.pop();
  }
};

const navigateTo = (index: number) => {
  pageStack.value = pageStack.value.slice(0, index + 1);
};

// ============ 公共方法 ============
const open = (config: { title: string; data?: any }) => {
  pageStack.value = [{ title: config.title, key: 'root' }];
  visible.value = true;
};

const handleClose = () => {
  visible.value = false;
};

const formatAmount = (value: number) => {
  if (value == null) return '';
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

defineExpose({ open });
</script>

<style scoped>
/* 标题栏 - 固定不滚动 */
.drawer-titlebar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.drawer-titlebar__nav {
  flex: 1;
  margin: 0 8px;
}
.drawer-titlebar__title {
  font-size: 16px;
  font-weight: 600;
  color: #434343;
}
.drawer-titlebar__actions {
  display: flex;
  gap: 4px;
}

/* 内容区 - 可滚动 */
.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

/* 底部操作栏 - 固定不滚动 */
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.detail-link {
  color: #1890ff;
  text-decoration: underline;
  cursor: pointer;
}

/* 面包屑 */
:deep(.ant-breadcrumb-link a) { color: #8c8c8c; }
:deep(.ant-breadcrumb-link a:hover) { color: #1890ff; }
:deep(.ant-breadcrumb > span:last-child .ant-breadcrumb-link) { color: #434343; }

/* 滚动条 */
.drawer-body::-webkit-scrollbar { width: 6px; }
.drawer-body::-webkit-scrollbar-thumb { background: transparent; border-radius: 3px; }
.drawer-body:hover::-webkit-scrollbar-thumb { background: #c1c1c1; }
</style>
```

## 关键规范

| 规范项 | 详情模式 | 录入模式 |
|--------|----------|----------|
| 遮罩 | 无 | 全屏遮罩 |
| 点击外部 | 自动关闭 | 不可关闭 |
| 宽度 | 480px / 720px | 480px / 720px |
| 横向滚动条 | 不允许 | 不允许 |
| 纵向滚动 | 仅内容区 | 仅内容区 |
| 多级页面 | 面包屑导航 | 面包屑导航 |
