# 页面布局样例

> 规范来源：一体化系统界面规范 - 业务系统部分 - 一级页面

## 1. 一级页面完整布局

```vue
<template>
  <div class="page-container">
    <!-- 页签栏（可选） -->
    <div class="page-tabs" v-if="showTabs">
      <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
        <a-tab-pane key="pending">
          <template #tab>待送审<span class="tab-count" v-if="counts.pending">({{ counts.pending }})</span></template>
        </a-tab-pane>
        <a-tab-pane key="submitted" tab="已送审" />
        <a-tab-pane key="auditing" tab="待审核" />
        <a-tab-pane key="audited" tab="已审核" />
        <a-tab-pane key="all" tab="全部" />
      </a-tabs>
    </div>

    <div class="page-body" :class="{ 'page-body--with-nav': showNav }">
      <!-- 导航栏（可选） -->
      <div class="page-nav" v-if="showNav" :style="{ width: navWidth + 'px' }">
        <div class="page-nav__header">
          <!-- 导航方式切换按钮 -->
          <a-button
            v-if="navModes.length > 1"
            type="text"
            size="small"
            @click="switchNavMode"
          >
            <AppstoreOutlined />
          </a-button>
          <!-- 搜索框 -->
          <a-input-search
            v-model:value="navSearchValue"
            placeholder="搜索"
            size="small"
            allow-clear
          />
        </div>
        <!-- 树形导航 -->
        <a-tree
          :tree-data="navTreeData"
          :selected-keys="navSelectedKeys"
          :checkable="navMultiSelect"
          @select="handleNavSelect"
        />
        <!-- 多选模式下的刷新按钮 -->
        <a-button
          v-if="navMultiSelect"
          type="primary"
          size="small"
          @click="handleRefreshByNav"
        >
          刷新
        </a-button>
      </div>

      <div class="page-main">
        <!-- 操作栏 -->
        <div class="operation-bar">
          <div class="operation-bar__left">
            <a-button type="primary" @click="handleAdd">新增</a-button>
            <a-button @click="handleEdit">修改</a-button>
            <a-button @click="handleSubmit">送审</a-button>
            <a-dropdown>
              <a-button>更多 <DownOutlined /></a-button>
              <template #overlay>
                <a-menu><a-menu-item key="delete">删除</a-menu-item></a-menu>
              </template>
            </a-dropdown>
          </div>
          <div class="operation-bar__right">
            <a-tooltip title="刷新"><a-button type="text"><ReloadOutlined /></a-button></a-tooltip>
            <a-tooltip title="查询"><a-button type="text" @click="toggleQuery"><SearchOutlined /></a-button></a-tooltip>
            <a-tooltip title="全屏"><a-button type="text"><FullscreenOutlined /></a-button></a-tooltip>
            <a-tooltip title="设置"><a-button type="text"><SettingOutlined /></a-button></a-tooltip>
          </div>
        </div>

        <!-- 查询条件汇总栏（查询面板关闭时显示） -->
        <div class="query-summary" v-if="!queryPanelOpen && hasQueryConditions">
          <a-tag
            v-for="condition in queryConditions"
            :key="condition.field"
            closable
            @close="removeCondition(condition.field)"
          >
            {{ condition.label }}：{{ condition.displayValue }}
          </a-tag>
          <a @click="clearAllConditions">清空条件</a>
        </div>

        <!-- 查询面板（浮动） -->
        <div class="query-panel" v-if="queryPanelOpen">
          <!-- 详见 query-panel-examples.md -->
        </div>

        <!-- 数据表格 -->
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :pagination="paginationConfig"
          :row-selection="rowSelection"
          bordered
          :row-class-name="zebraRowClass"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 业务功能设置整体外边框，与系统框架区分 */
.page-container {
  border: 1px solid #f0f0f0;
  background: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 页签栏 */
.page-tabs {
  padding: 0 16px;
  border-bottom: 1px solid #f0f0f0;
}
.page-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}
.tab-count {
  color: #ff4d4f;
}

/* 导航栏 */
.page-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}
.page-nav {
  border-right: 1px solid #f0f0f0;
  overflow-y: auto;
  padding: 8px;
  flex-shrink: 0;
}

/* 操作栏 */
.operation-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
}
.operation-bar__left,
.operation-bar__right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 主内容区 */
.page-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 查询条件汇总栏 */
.query-summary {
  padding: 4px 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

/* 滚动条统一规范 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 3px;
}
*:hover::-webkit-scrollbar-thumb {
  background: #c1c1c1;
}
</style>
```

## 2. 导航栏布局方式

### 正T字布局（页签在上，导航栏和数据区横向并列）

适用于每个页签数据不同的场景。

```
┌─────────────────────────────────┐
│ [页签1] [页签2] [页签3]          │
├────────┬────────────────────────┤
│ 导航栏 │ 操作栏                  │
│        │ 数据表格                │
│        │                        │
└────────┴────────────────────────┘
```

### 倒T字布局（导航栏在左，页签栏和数据区纵向并列）

适用于每个页签数据和导航内容相同的场景。

```
┌────────┬────────────────────────┐
│ 导航栏 │ [页签1] [页签2] [页签3] │
│        │ 操作栏                  │
│        │ 数据表格                │
│        │                        │
└────────┴────────────────────────┘
```

## 规范要点

- 业务功能区设置整体外边框，与系统框架区分
- 功能区大小计算准确，不出现多余滚动条
- 滚动条默认隐藏，鼠标移入显示，移出隐藏
- 页签栏、导航栏为可选项
- 第一个页签左边距与操作区第一个按钮左边距相同
- 导航栏支持模糊搜索、单选/多选、收缩/展开、宽度记忆
- 操作栏左侧为业务按钮，右侧为辅助按钮
- 业务按钮不超过 4 个，多余放到更多
