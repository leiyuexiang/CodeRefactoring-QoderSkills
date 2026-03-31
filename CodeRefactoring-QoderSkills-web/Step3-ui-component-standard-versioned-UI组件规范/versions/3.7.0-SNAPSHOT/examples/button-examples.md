# 按钮组件样例

> 规范来源：一体化系统界面规范 - 控件 - 按钮

## 1. 主按钮

一个页面最多一个主按钮，蓝色背景，白色文字，14px 字体。

```vue
<template>
  <a-button type="primary" @click="handleSubmit" :loading="submitting">
    提交
  </a-button>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const submitting = ref(false);

const handleSubmit = async () => {
  submitting.value = true;
  try {
    await submitData();
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.ant-btn-primary {
  font-size: 14px;
}
</style>
```

## 2. 次按钮（普通按钮）

灰色边框，一个页面可有若干个。

```vue
<template>
  <a-button @click="handleCancel">取消</a-button>
  <a-button @click="handleReset">重置</a-button>
</template>
```

## 3. 危险按钮

用于特殊场景提醒用户慎重操作，一个页面不超过一个。

```vue
<template>
  <a-button danger @click="handleDelete">删除</a-button>
</template>
```

**注意：危险按钮应该慎用。**

## 4. 更多按钮

当操作按钮超过 4 个时，多余操作放到更多按钮下。

```vue
<template>
  <div class="action-bar">
    <a-button type="primary" @click="handleAdd">新增</a-button>
    <a-button @click="handleEdit">修改</a-button>
    <a-button @click="handleSubmitAudit">送审</a-button>
    <a-dropdown>
      <a-button>
        更多 <DownOutlined />
      </a-button>
      <template #overlay>
        <a-menu @click="handleMoreAction">
          <a-menu-item key="copy">复制</a-menu-item>
          <a-menu-item key="import">导入</a-menu-item>
          <a-menu-item key="export">导出</a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>
  </div>
</template>

<script setup lang="ts">
import { DownOutlined } from '@ant-design/icons-vue';
</script>

<style scoped>
/* 更多面板宽度不超过200px，超长内容显示省略号 */
.ant-dropdown-menu {
  max-width: 200px;
}
.ant-dropdown-menu-item {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
```

## 5. 一级页面操作栏按钮布局

左侧为业务操作按钮（文字按钮），右侧为辅助操作按钮（图标按钮）。

```vue
<template>
  <div class="operation-bar">
    <!-- 左侧：业务操作按钮 -->
    <div class="operation-bar__left">
      <a-button type="primary" @click="handleAdd">新增</a-button>
      <a-button @click="handleEdit">修改</a-button>
      <a-button @click="handleSubmit">送审</a-button>
      <a-dropdown>
        <a-button>更多 <DownOutlined /></a-button>
        <template #overlay>
          <a-menu>
            <a-menu-item key="delete">删除</a-menu-item>
            <a-menu-item key="copy">复制</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </div>

    <!-- 右侧：辅助操作按钮（图标按钮） -->
    <div class="operation-bar__right">
      <!-- 快捷查询区 -->
      <a-input
        v-model:value="quickSearch"
        placeholder="请输入关键字"
        allow-clear
        style="width: 200px"
      />
      <!-- 金额单位切换 -->
      <a-radio-group v-model:value="amountUnit" size="small">
        <a-radio-button value="yuan">元</a-radio-button>
        <a-radio-button value="wan">万元</a-radio-button>
        <a-radio-button value="yi">亿元</a-radio-button>
      </a-radio-group>
      <!-- 辅助图标按钮 -->
      <a-tooltip title="刷新">
        <a-button type="text" @click="handleRefresh">
          <ReloadOutlined />
        </a-button>
      </a-tooltip>
      <a-tooltip title="查询">
        <a-button type="text" @click="toggleQueryPanel">
          <SearchOutlined />
        </a-button>
      </a-tooltip>
      <a-tooltip title="全屏">
        <a-button type="text" @click="toggleFullscreen">
          <FullscreenOutlined />
        </a-button>
      </a-tooltip>
      <a-tooltip title="设置">
        <a-button type="text" @click="openSettings">
          <SettingOutlined />
        </a-button>
      </a-tooltip>
    </div>
  </div>
</template>
```

## 6. 二级页面操作按钮

操作按钮按重要性从右到左排列（与一级页面相反），每页默认一个返回按钮。

```vue
<template>
  <div class="secondary-operation-bar">
    <!-- 左侧：面包屑导航 -->
    <div class="secondary-operation-bar__left">
      <a-breadcrumb>
        <a-breadcrumb-item>
          <a @click="goBack">用款计划</a>
        </a-breadcrumb-item>
        <a-breadcrumb-item>录入</a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <!-- 右侧：操作按钮（重要性从右到左） -->
    <div class="secondary-operation-bar__right">
      <a-button @click="goBack">返回</a-button>
      <a-checkbox v-model:checked="continuousInput">连续录入</a-checkbox>
      <a-button @click="handleSave">保存</a-button>
      <a-button type="primary" @click="handleSubmitAudit">送审</a-button>
    </div>
  </div>
</template>
```

## 规范要点

- 按钮应该及时响应用户操作
- 鼠标悬停、点击时给用户反馈
- 长时间等待时显示 loading 状态
- 按钮文本使用 14px 字体
- 主按钮蓝色背景，普通按钮灰色边框
- 辅助按钮使用图标按钮，大小风格一致
