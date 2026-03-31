# 查询面板模板

> 包含查询条件、常用查询、条件设置的完整查询面板模板。

## 模板代码

```vue
<template>
  <div class="query-module">
    <!-- ========== 查询条件汇总栏（面板关闭时） ========== -->
    <div class="query-summary" v-if="!panelOpen && conditions.length > 0">
      <div class="query-summary__tags">
        <a-tag
          v-for="cond in conditions.slice(0, 5)"
          :key="cond.field"
          closable
          class="condition-tag"
          @close="removeCondition(cond.field)"
          @contextmenu.prevent="(e) => showContextMenu(e, cond)"
        >
          <a-tooltip :title="`${cond.label}：${cond.fullValue}`">
            <span class="condition-tag__text">{{ cond.label }}：{{ cond.displayValue }}</span>
          </a-tooltip>
        </a-tag>
        <a-popover v-if="conditions.length > 5" trigger="hover">
          <template #content>
            <div class="overflow-conditions">
              <a-tag v-for="c in conditions.slice(5)" :key="c.field" closable @close="removeCondition(c.field)">
                {{ c.label }}：{{ c.displayValue }}
              </a-tag>
            </div>
          </template>
          <a-button type="link" size="small">更多({{ conditions.length - 5 }})</a-button>
        </a-popover>
      </div>
      <a class="query-summary__clear" @click="clearAll">清空条件</a>
    </div>

    <!-- 右键菜单 -->
    <div
      class="context-menu"
      v-if="contextMenuVisible"
      :style="{ top: contextMenuY + 'px', left: contextMenuX + 'px' }"
    >
      <div class="context-menu__item" @click="removeCondition(contextMenuField)">清除当前条件</div>
      <div class="context-menu__item" @click="clearAll">清空所有条件</div>
      <div class="context-menu__item" @click="clearOthers(contextMenuField)">清空其他条件</div>
    </div>

    <!-- ========== 查询面板（展开时） ========== -->
    <div class="query-panel" v-if="panelOpen">
      <div class="query-panel__header">
        <!-- 常用查询 -->
        <div class="query-panel__saved" v-if="savedQueryEnabled">
          <a-select
            v-model:value="selectedSavedId"
            placeholder="常用查询"
            allow-clear
            style="width: 200px"
            @change="applySavedQuery"
          >
            <a-select-option v-for="sq in savedQueries" :key="sq.id" :value="sq.id">
              {{ sq.name }}
              <StarFilled v-if="sq.isDefault" style="color: #faad14; margin-left: 4px" />
            </a-select-option>
          </a-select>
        </div>
        <!-- 操作按钮 -->
        <div class="query-panel__actions">
          <a-button type="primary" @click="handleQuery">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
          <a-button @click="handleSaveQuery" v-if="savedQueryEnabled">保存</a-button>
          <a-button @click="openSettings">设置</a-button>
        </div>
      </div>
      <a-form :model="queryForm" class="query-panel__form">
        <a-row :gutter="16">
          <a-col :span="8" v-for="field in activeQueryFields" :key="field.name">
            <a-form-item :label="field.label" :label-col="{ style: { width: '100px' } }">
              <!-- 文本输入 -->
              <a-input
                v-if="field.type === 'input'"
                v-model:value="queryForm[field.name]"
                :placeholder="field.placeholder || '请输入'"
                allow-clear
              />
              <!-- 下拉选择 -->
              <a-select
                v-else-if="field.type === 'select'"
                v-model:value="queryForm[field.name]"
                :placeholder="field.placeholder || '请选择'"
                :options="field.options"
                allow-clear
                :mode="field.multiple ? 'multiple' : undefined"
              />
              <!-- 树选择 -->
              <a-tree-select
                v-else-if="field.type === 'treeSelect'"
                v-model:value="queryForm[field.name]"
                :placeholder="field.placeholder || '请选择'"
                :tree-data="field.treeData"
                allow-clear
              />
              <!-- 日期范围（封装为一个控件） -->
              <a-range-picker
                v-else-if="field.type === 'dateRange'"
                v-model:value="queryForm[field.name]"
                style="width: 100%"
              />
              <!-- 金额范围（封装为一个控件） -->
              <a-input-group compact v-else-if="field.type === 'amountRange'">
                <a-input-number
                  v-model:value="queryForm[field.name + 'Min']"
                  placeholder="最小"
                  style="width: 45%"
                />
                <a-input
                  style="width: 10%; text-align: center; pointer-events: none"
                  placeholder="~"
                  disabled
                />
                <a-input-number
                  v-model:value="queryForm[field.name + 'Max']"
                  placeholder="最大"
                  style="width: 45%"
                />
              </a-input-group>
              <!-- 日期选择 -->
              <a-date-picker
                v-else-if="field.type === 'date'"
                v-model:value="queryForm[field.name]"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </div>

    <!-- ========== 条件设置抽屉 ========== -->
    <a-drawer
      v-model:open="settingsVisible"
      title="查询条件设置"
      :width="600"
      :mask="false"
    >
      <div class="settings-layout">
        <div class="settings-left">
          <h4>启用字段</h4>
          <div class="settings-section">
            <div class="settings-section__title">快捷查询区（最多3个）</div>
            <div class="field-list field-list--quick">
              <div
                v-for="field in enabledQuickFields"
                :key="field.name"
                class="field-item"
                draggable="true"
              >
                {{ field.label }}
              </div>
            </div>
          </div>
          <div class="settings-section">
            <div class="settings-section__title">常规查询区</div>
            <div class="field-list">
              <div
                v-for="field in enabledNormalFields"
                :key="field.name"
                class="field-item"
                draggable="true"
              >
                {{ field.label }}
              </div>
            </div>
          </div>
        </div>
        <div class="settings-right">
          <h4>全部字段</h4>
          <div class="field-list">
            <div v-for="field in allAvailableFields" :key="field.name" class="field-item">
              {{ field.label }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <a-button @click="restoreQueryDefaults">恢复默认</a-button>
        <a-button type="primary" @click="saveQuerySettings" style="margin-left: 8px">保存</a-button>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

// ============ 配置区 ============

interface QueryField {
  name: string;
  label: string;
  type: 'input' | 'select' | 'treeSelect' | 'dateRange' | 'amountRange' | 'date';
  placeholder?: string;
  options?: { label: string; value: string }[];
  treeData?: any[];
  multiple?: boolean;
  zone: 'quick' | 'normal'; // 快捷查询区 or 常规查询区
}

/** 查询字段定义 - 根据业务修改 */
const queryFieldDefs = ref<QueryField[]>([
  { name: 'unit', label: '预算单位', type: 'treeSelect', zone: 'normal' },
  { name: 'funcCategory', label: '功能分类', type: 'treeSelect', zone: 'normal' },
  { name: 'dateRange', label: '申请日期', type: 'dateRange', zone: 'normal' },
  { name: 'amount', label: '金额范围', type: 'amountRange', zone: 'normal' },
  { name: 'status', label: '状态', type: 'select', options: [], zone: 'normal' },
]);

/** 常用查询是否启用 */
const savedQueryEnabled = ref(true);

// ============ 状态 ============
const panelOpen = ref(false);
const settingsVisible = ref(false);
const queryForm = ref<Record<string, any>>({});
const selectedSavedId = ref<string>();

const toggle = () => { panelOpen.value = !panelOpen.value; };

defineExpose({ toggle, panelOpen });
</script>

<style scoped>
.query-summary { display: flex; align-items: center; padding: 4px 0; gap: 4px; }
.query-summary__tags { display: flex; flex-wrap: wrap; gap: 4px; flex: 1; }
.condition-tag { max-width: 200px; }
.condition-tag__text { max-width: 170px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; display: inline-block; vertical-align: middle; }
.query-panel { padding: 16px; border: 1px solid #f0f0f0; border-radius: 2px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); background: #fff; }
.query-panel__header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.query-panel__actions { display: flex; gap: 8px; }
.context-menu { position: fixed; background: #fff; border: 1px solid #f0f0f0; border-radius: 2px; box-shadow: 0 2px 8px rgba(0,0,0,0.15); z-index: 1000; }
.context-menu__item { padding: 6px 16px; cursor: pointer; font-size: 14px; }
.context-menu__item:hover { background: #f5f5f5; }
.settings-layout { display: flex; gap: 16px; height: 100%; }
.settings-left, .settings-right { flex: 1; }
.settings-section { margin-bottom: 16px; }
.settings-section__title { font-size: 12px; color: #8c8c8c; margin-bottom: 8px; }
.field-item { padding: 6px 12px; border: 1px solid #f0f0f0; border-radius: 2px; margin-bottom: 4px; cursor: move; background: #fff; }
.field-item:hover { border-color: #1890ff; }
</style>
```

## 配置清单

| 配置项 | 说明 |
|--------|------|
| queryFieldDefs | 查询字段定义，含类型、区域 |
| savedQueryEnabled | 是否启用常用查询 |
| 一行控件数 | 固定 3 个（:span="8"） |
| 快捷查询数 | 最多 3 个字段 |
| 汇总栏显示 | 最多 5 个，超出放更多 |
| 单条件宽度 | 不超过 200px |
