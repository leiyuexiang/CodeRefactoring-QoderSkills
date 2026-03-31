# 查询面板样例

> 规范来源：一体化系统界面规范 - 业务系统部分 - 查询面板

## 1. 关闭状态 - 查询条件汇总栏

```vue
<template>
  <div class="query-summary-bar" v-if="hasConditions && !panelOpen">
    <!-- 查询条件标签 -->
    <div class="query-summary-tags">
      <a-tag
        v-for="cond in visibleConditions"
        :key="cond.field"
        closable
        class="query-tag"
        @close="removeCondition(cond.field)"
        @contextmenu.prevent="showContextMenu($event, cond)"
      >
        <a-tooltip :title="cond.fullText" placement="top">
          <span class="query-tag__text">
            {{ cond.label }}：{{ cond.displayValue }}
          </span>
        </a-tooltip>
      </a-tag>
      <!-- 超过5个条件时显示更多 -->
      <a-popover v-if="hiddenConditions.length > 0" trigger="hover">
        <template #content>
          <div class="hidden-conditions">
            <a-tag
              v-for="cond in hiddenConditions"
              :key="cond.field"
              closable
              @close="removeCondition(cond.field)"
            >
              {{ cond.label }}：{{ cond.displayValue }}
            </a-tag>
          </div>
        </template>
        <a-button type="link" size="small">更多({{ hiddenConditions.length }})</a-button>
      </a-popover>
    </div>
    <!-- 全部清除 -->
    <a class="query-summary-clear" @click="clearAllConditions">清空条件</a>
  </div>
</template>

<script setup lang="ts">
/**
 * 查询条件汇总栏规范：
 * - 按单个条件分开展示
 * - 每个条件可点击右侧关闭按钮清除
 * - 显示内容：条件名称 + 中文冒号 + 条件值
 * - 要素查询条件同时显示编码和名称，多值用逗号分隔
 *   例：30101 基本工资，30103 奖金
 * - 单个条件宽度不超过 200px，超长省略，悬停显示全部
 * - 超过 5 个条件，多余放到更多按钮中
 * - 右键菜单：清除当前条件、清空所有条件、清空其他条件
 */
</script>

<style scoped>
.query-summary-bar {
  display: flex;
  align-items: center;
  padding: 4px 16px;
  gap: 4px;
}
.query-summary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  flex: 1;
}
.query-tag {
  max-width: 200px;
}
.query-tag__text {
  display: inline-block;
  max-width: 170px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}
</style>
```

## 2. 打开状态 - 查询面板

```vue
<template>
  <div class="query-panel" v-show="panelOpen">
    <div class="query-panel__header">
      <!-- 左上角：常用查询（可选） -->
      <div class="query-panel__saved">
        <a-select
          v-model:value="selectedSavedQuery"
          placeholder="常用查询"
          allow-clear
          style="width: 200px"
          @change="applySavedQuery"
        >
          <a-select-option
            v-for="sq in savedQueries"
            :key="sq.id"
            :value="sq.id"
          >
            <span>{{ sq.name }}</span>
            <StarFilled v-if="sq.isDefault" style="color: #faad14; margin-left: 4px" />
          </a-select-option>
        </a-select>
      </div>
      <!-- 右上角：操作按钮 -->
      <div class="query-panel__actions">
        <a-button type="primary" @click="handleQuery">查询</a-button>
        <a-button @click="handleReset">重置</a-button>
        <a-button @click="handleSaveQuery">保存</a-button>
        <a-button @click="openQuerySettings">设置</a-button>
      </div>
    </div>

    <div class="query-panel__body">
      <a-form layout="inline" :model="queryForm">
        <a-row :gutter="16" style="width: 100%">
          <!-- 一行放三个控件 -->
          <a-col :span="8">
            <a-form-item label="预算单位">
              <a-select v-model:value="queryForm.unit" placeholder="请选择" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="支出功能分类">
              <a-tree-select v-model:value="queryForm.funcCategory" placeholder="请选择" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <!-- 日期范围封装为一个控件 -->
            <a-form-item label="申请日期">
              <a-range-picker v-model:value="queryForm.dateRange" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <!-- 金额范围封装为一个控件 -->
            <a-form-item label="金额范围">
              <a-input-group compact>
                <a-input-number v-model:value="queryForm.amountMin" placeholder="最小" style="width: 45%" />
                <a-input style="width: 10%; text-align: center; pointer-events: none" placeholder="~" disabled />
                <a-input-number v-model:value="queryForm.amountMax" placeholder="最大" style="width: 45%" />
              </a-input-group>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </div>
  </div>
</template>

<style scoped>
/**
 * 查询面板规范：
 * - 默认关闭，通过查询按钮切换
 * - 浮动显示（特殊情况可固定）
 * - 左上角：常用查询（可选）
 * - 右上角：操作按钮（文字按钮），查询按钮蓝色背景
 * - 标签在控件左侧，一行三个控件
 * - 日期范围、金额范围封装为一个控件
 */
.query-panel {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  padding: 16px;
  z-index: 10;
}
.query-panel__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.query-panel__actions {
  display: flex;
  gap: 8px;
}
</style>
```

## 3. 条件设置（抽屉页面）

```vue
<template>
  <a-drawer
    title="查询条件设置"
    :open="settingsOpen"
    :width="600"
    :mask="false"
    @close="settingsOpen = false"
  >
    <div class="query-settings">
      <!-- 左侧：启用字段 -->
      <div class="query-settings__enabled">
        <h4>启用字段</h4>
        <!-- 快捷查询区（最多3个） -->
        <div class="field-section">
          <div class="field-section__title">快捷查询区</div>
          <draggable v-model="quickFields" group="fields" item-key="id">
            <template #item="{ element }">
              <div class="field-item">{{ element.label }}</div>
            </template>
          </draggable>
        </div>
        <!-- 常规查询区 -->
        <div class="field-section">
          <div class="field-section__title">常规查询区</div>
          <draggable v-model="normalFields" group="fields" item-key="id">
            <template #item="{ element }">
              <div class="field-item">{{ element.label }}</div>
            </template>
          </draggable>
        </div>
      </div>
      <!-- 右侧：全部字段 -->
      <div class="query-settings__all">
        <h4>全部字段</h4>
        <draggable v-model="allFields" group="fields" item-key="id">
          <template #item="{ element }">
            <div class="field-item">{{ element.label }}</div>
          </template>
        </draggable>
      </div>
    </div>
    <!-- 恢复默认 -->
    <template #footer>
      <a-button @click="restoreDefaults">恢复默认</a-button>
    </template>
  </a-drawer>
</template>
```

## 4. 常用查询

```vue
<template>
  <!--
    常用查询规范：
    - 按用户、功能页签存储
    - 每用户每功能页签不超过 20 个
    - 名称可修改，不再使用的可删除
    - 可设为首选，首选显示在列表最上方
    - 页面加载时自动按首选条件过滤
    - 可设为首选/取消首选
  -->
</template>
```

## 规范要点

- 查询面板默认关闭，通过查询按钮切换
- 浮动显示，特殊情况可固定
- 一行放置三个控件，标签在控件左侧
- 日期范围/金额范围封装为一个控件
- 条件值格式：`条件名称：条件值`（中文冒号）
- 单个条件栏宽度不超过 200px
- 超过 5 个条件放到更多中
- 快捷查询最多 3 个条件
- 支持指标模糊查询和常规查询两种模式
