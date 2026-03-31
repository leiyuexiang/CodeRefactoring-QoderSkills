# 树形控件样例

> 规范来源：一体化系统界面规范 - 控件 - 树

## 1. 单选树

```vue
<template>
  <a-tree
    :tree-data="treeData"
    :selected-keys="selectedKeys"
    :expanded-keys="expandedKeys"
    :auto-expand-parent="true"
    @select="handleSelect"
    @expand="handleExpand"
  >
    <template #title="{ title }">
      <span>{{ title }}</span>
    </template>
  </a-tree>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const selectedKeys = ref<string[]>([]);
const expandedKeys = ref<string[]>([]);

/**
 * 单选规范：
 * - 点击文本区域或展开/折叠图标都可以展开/折叠非末级节点
 * - 当前节点背景色: #E6F7FF
 * - 鼠标悬停背景色: #F5F5F5
 */
const handleSelect = (keys: string[]) => {
  selectedKeys.value = keys;
};

const handleExpand = (keys: string[]) => {
  expandedKeys.value = keys;
};
</script>

<style scoped>
:deep(.ant-tree-node-selected) {
  background-color: #e6f7ff !important;
}
:deep(.ant-tree-node-content-wrapper:hover) {
  background-color: #f5f5f5;
}
</style>
```

## 2. 多选树（级联选择）

```vue
<template>
  <a-tree
    v-model:checked-keys="checkedKeys"
    :tree-data="treeData"
    checkable
    :check-strictly="false"
    :expanded-keys="expandedKeys"
    @check="handleCheck"
    @expand="handleExpand"
  >
  </a-tree>
</template>

<script setup lang="ts">
/**
 * 级联选择规范（默认模式）：
 * - 选择父级节点自动级联选择下级和上级节点
 * - 节点选择状态：选择、部分选择、未选择
 * - 点击文本区域或复选框都可以选择/取消
 * - :check-strictly="false" 启用级联
 */
</script>
```

## 3. 多选树（自由选择）

```vue
<template>
  <!-- 自由选择：不进行上下级联动，可只选择非末级节点 -->
  <a-tree
    v-model:checked-keys="checkedKeys"
    :tree-data="treeData"
    checkable
    :check-strictly="true"
    :expanded-keys="expandedKeys"
    @check="handleCheck"
  >
  </a-tree>
</template>
```

## 4. 带搜索框的树

```vue
<template>
  <div class="searchable-tree">
    <a-input-search
      v-model:value="searchValue"
      placeholder="请输入关键字搜索"
      allow-clear
      style="margin-bottom: 8px"
    />
    <a-tree
      :tree-data="filteredTreeData"
      :expanded-keys="autoExpandedKeys"
      :auto-expand-parent="true"
      :selected-keys="selectedKeys"
      @select="handleSelect"
    >
      <template #title="{ title }">
        <span v-if="title.indexOf(searchValue) > -1">
          {{ title.substring(0, title.indexOf(searchValue)) }}
          <span style="color: #f50">{{ searchValue }}</span>
          {{ title.substring(title.indexOf(searchValue) + searchValue.length) }}
        </span>
        <span v-else>{{ title }}</span>
      </template>
    </a-tree>
  </div>
</template>

<script setup lang="ts">
/**
 * 搜索规范：
 * - 模糊搜索，快速定位
 * - 搜索结果高亮显示
 * - 自动展开匹配节点的父节点
 */
</script>
```

## 规范要点

- 树形控件分为单选和多选
- 多选分为级联选择（默认）和自由选择
- 点击文本区域或展开/折叠图标都可以展开/折叠
- 点击文本区域或复选框都可以选择/取消
- 当前节点背景色：`#E6F7FF`
- 鼠标悬停背景色：`#F5F5F5`
- 自由选择用于可以只选择非末级节点的场景
- 级联选择时选择父级自动选中下级和上级
