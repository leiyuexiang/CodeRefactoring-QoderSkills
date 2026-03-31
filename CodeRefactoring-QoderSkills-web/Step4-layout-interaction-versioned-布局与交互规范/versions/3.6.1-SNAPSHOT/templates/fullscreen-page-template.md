# 全屏二级页面模板

> 覆盖全部功能区域的二级页面，适用于大量信息的录入和展示。

## 模板代码

```vue
<template>
  <div class="fullscreen-page">
    <!-- ========== 操作区 ========== -->
    <div class="fullscreen-page__header">
      <!-- 左侧：面包屑导航 -->
      <div class="fullscreen-page__nav">
        <a-breadcrumb>
          <a-breadcrumb-item v-for="(page, index) in pageStack" :key="index">
            <a v-if="index < pageStack.length - 1" @click="navigateTo(index)">
              <template v-if="index === 0">
                <HomeOutlined style="margin-right: 4px" />
              </template>
              {{ page.title }}
            </a>
            <span v-else>{{ page.title }}</span>
          </a-breadcrumb-item>
        </a-breadcrumb>
      </div>

      <!-- 右侧：操作按钮（重要性从右到左） -->
      <div class="fullscreen-page__actions">
        <!-- 返回按钮（最左侧） -->
        <a-button @click="goBack">返回</a-button>
        <!-- 全局开关（返回按钮左侧） -->
        <a-checkbox v-if="showContinuousInput" v-model:checked="continuousInput">
          连续录入
        </a-checkbox>
        <!-- 普通按钮 -->
        <a-button v-for="btn in normalButtons" :key="btn.key" @click="btn.handler">
          {{ btn.label }}
        </a-button>
        <!-- 更多按钮（超过4个时） -->
        <a-dropdown v-if="moreButtons.length > 0">
          <a-button>更多 <DownOutlined /></a-button>
          <template #overlay>
            <a-menu @click="handleMoreAction">
              <a-menu-item v-for="btn in moreButtons" :key="btn.key">{{ btn.label }}</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
        <!-- 主按钮（最右侧） -->
        <a-button type="primary" :loading="submitting" @click="handlePrimaryAction">
          {{ primaryButtonLabel }}
        </a-button>
      </div>
    </div>

    <a-divider style="margin: 0" />

    <!-- ========== 数据区 ========== -->
    <div class="fullscreen-page__body">
      <!-- 详情卡片示例 -->
      <div class="data-card" v-for="card in cardConfigs" :key="card.key">
        <div class="data-card__header">
          <div class="data-card__header-left">
            <span class="data-card__title">{{ card.title }}</span>
            <a-tag v-for="tag in card.tags" :key="tag.text" :color="tag.color">{{ tag.text }}</a-tag>
          </div>
          <div class="data-card__header-right">
            <!-- 卡片操作按钮（图标按钮） -->
            <template v-for="action in card.actions" :key="action.key">
              <a-tooltip :title="action.tooltip">
                <a-button type="text" size="small" @click="action.handler">
                  <component :is="action.icon" />
                </a-button>
              </a-tooltip>
            </template>
            <!-- 分组分隔线 -->
            <a-divider type="vertical" v-if="card.actions?.length" />
            <!-- 展开/折叠按钮（最右侧） -->
            <a-tooltip :title="card.collapsed ? '展开' : '折叠'">
              <a-button type="text" size="small" @click="toggleCard(card.key)">
                <UpOutlined v-if="!card.collapsed" />
                <DownOutlined v-if="card.collapsed" />
              </a-button>
            </a-tooltip>
          </div>
        </div>
        <div class="data-card__body" v-show="!card.collapsed">
          <!-- 根据 card.type 渲染不同内容 -->
          <slot :name="card.key" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';

// ============ 页面导航 ============
interface PageInfo {
  title: string;
  key: string;
}

const pageStack = ref<PageInfo[]>([
  { title: '用款计划', key: 'list' },
  { title: '录入', key: 'input' },
]);

const goBack = () => {
  // 返回上一级页面
};

const navigateTo = (index: number) => {
  // 导航到指定层级
  pageStack.value = pageStack.value.slice(0, index + 1);
};

// ============ 操作按钮 ============
/**
 * 按钮布局规范（从左到右）：
 * 返回 | 全局开关 | 普通按钮... | 更多 | 主按钮
 * 注意：与一级页面方向相反，重要性从右到左
 */
const primaryButtonLabel = ref('送审');
const submitting = ref(false);
const continuousInput = ref(false);
const showContinuousInput = ref(true);

// ============ 卡片管理 ============
interface CardConfig {
  key: string;
  title: string;
  type: 'detail' | 'form' | 'list';
  collapsed: boolean;
  tags?: { text: string; color: string }[];
  actions?: { key: string; tooltip: string; icon: any; handler: () => void }[];
}

const cardConfigs = reactive<CardConfig[]>([
  {
    key: 'basic',
    title: '基本信息',
    type: 'detail',
    collapsed: false,
    tags: [{ text: '三保', color: 'red' }],
  },
  {
    key: 'input',
    title: '录入信息',
    type: 'form',
    collapsed: false,
  },
  {
    key: 'detail',
    title: '明细列表',
    type: 'list',
    collapsed: false,
  },
]);

const toggleCard = (key: string) => {
  const card = cardConfigs.find((c) => c.key === key);
  if (card) card.collapsed = !card.collapsed;
};
</script>

<style scoped>
.fullscreen-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #f0f0f0;
  background: #fff;
}

/* 操作区 - 始终在顶部 */
.fullscreen-page__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  flex-shrink: 0;
}
.fullscreen-page__nav {
  max-width: 60%;
}
.fullscreen-page__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 面包屑样式 */
:deep(.ant-breadcrumb-link) { color: #8c8c8c; }
:deep(.ant-breadcrumb-link a) { color: #8c8c8c; }
:deep(.ant-breadcrumb-link a:hover) { color: #1890ff; }
:deep(.ant-breadcrumb > span:last-child .ant-breadcrumb-link) { color: #434343; }

/* 数据区 - 可滚动，操作区不受影响 */
.fullscreen-page__body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

/* 数据卡片 */
.data-card {
  border: 1px solid #f0f0f0;
  border-radius: 2px;
  margin-bottom: 16px;
}
.data-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.data-card__header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.data-card__header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}
.data-card__title {
  font-size: 16px;
  font-weight: 600;
  color: #434343;
}
.data-card__body {
  padding: 16px;
}

/* 滚动条 */
::-webkit-scrollbar { width: 6px; }
::-webkit-scrollbar-thumb { background: transparent; border-radius: 3px; }
.fullscreen-page__body:hover::-webkit-scrollbar-thumb { background: #c1c1c1; }
</style>
```

## 关键规范

| 规范项 | 要求 |
|--------|------|
| 页面覆盖 | 覆盖全部功能区域（包括一级页面页签栏） |
| 外边框 | 与平台框架保持相同 |
| 操作区 | 始终在顶部，不随内容滚动 |
| 按钮方向 | 重要性从右到左（与一级页面相反） |
| 返回按钮 | 每页默认有，排在最左侧 |
| 面包屑 | 不超过页面宽度 60%，层数不超过十层 |
| 数据区滚动 | 纵向可滚动，操作区不受影响 |
| 卡片折叠 | 单卡片可不显示折叠按钮 |
