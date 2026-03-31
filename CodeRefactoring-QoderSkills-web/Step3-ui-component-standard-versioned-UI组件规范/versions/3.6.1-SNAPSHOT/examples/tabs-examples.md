# 选项卡组件样例

> 规范来源：一体化系统界面规范 - 控件 - 选项卡

## 1. 横向排列

```vue
<template>
  <a-tabs v-model:activeKey="activeKey" @change="handleTabChange">
    <a-tab-pane key="pending" tab="待送审" />
    <a-tab-pane key="submitted" tab="已送审" />
    <a-tab-pane key="auditing" tab="待审核" />
    <a-tab-pane key="audited" tab="已审核" />
  </a-tabs>
</template>

<style scoped>
/**
 * 横向选项卡颜色规范：
 * - 选中：文字 #1890FF，加粗
 * - 悬停：文字 #1890FF
 * - 其他：文字 #434343
 * - 状态条：#1890FF，和文字等宽，2px
 * - 分割线：#F0F0F0，1px
 */
:deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #1890ff;
  font-weight: 600;
}
:deep(.ant-tabs-tab:hover .ant-tabs-tab-btn) {
  color: #1890ff;
}
:deep(.ant-tabs-tab-btn) {
  color: #434343;
}
:deep(.ant-tabs-ink-bar) {
  background: #1890ff;
  height: 2px;
}
:deep(.ant-tabs-nav::before) {
  border-bottom-color: #f0f0f0;
}
</style>
```

## 2. 纵向排列

```vue
<template>
  <a-tabs v-model:activeKey="activeKey" tab-position="left">
    <a-tab-pane key="basic" tab="基本信息" />
    <a-tab-pane key="detail" tab="明细信息" />
    <a-tab-pane key="attachment" tab="附件管理" />
    <a-tab-pane key="log" tab="审核日志" />
  </a-tabs>
</template>

<style scoped>
/**
 * 纵向选项卡颜色规范：
 * - 选中：文字 #1890FF，加粗
 * - 悬停：文字 #1890FF
 * - 其他：文字 #434343
 * - 状态条：#1890FF，2px
 * - 分割线：#F0F0F0，1px
 */
:deep(.ant-tabs-left > .ant-tabs-nav .ant-tabs-ink-bar) {
  width: 2px;
  background: #1890ff;
}
</style>
```

## 3. 带数量标记的页签栏

推荐在页签后显示单据数量。

```vue
<template>
  <a-tabs v-model:activeKey="activeKey">
    <a-tab-pane key="all" tab="全部" />
    <a-tab-pane key="pending">
      <template #tab>
        <span>待送审</span>
        <span class="tab-count" v-if="pendingCount > 0">({{ pendingCount }})</span>
      </template>
    </a-tab-pane>
    <a-tab-pane key="submitted" tab="已送审" />
    <a-tab-pane key="returned" tab="被退回" />
  </a-tabs>
</template>

<style scoped>
/**
 * 页签命名规范：
 * - 统一为：待XX、已XX、被XX
 * - 如：待送审、已送审、待审核、已审核、待发送、已发送、被退回
 * - 全部数据页签叫：全部
 * - 数据驱动录入功能第一个页签叫：录入
 *
 * 数量显示规范：
 * - 样式：待送审(10)
 * - 括号为半角
 * - 括号内数字颜色为红色
 */
.tab-count {
  color: #ff4d4f;
  margin-left: 2px;
}
</style>
```

## 规范要点

- 选项卡用于分组展示相关但相对独立的数据
- 横向和纵向两种布局方式
- 选中文字蓝色加粗 `#1890FF`
- 状态条蓝色 `#1890FF`，2px
- 分割线 `#F0F0F0`，1px
- 页签命名统一：待XX、已XX、被XX
- 数量括号为半角，内部数字红色
