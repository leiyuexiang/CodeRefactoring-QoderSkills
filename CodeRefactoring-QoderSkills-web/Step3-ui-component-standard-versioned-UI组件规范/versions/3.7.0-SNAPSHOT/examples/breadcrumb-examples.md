# 面包屑组件样例

> 规范来源：一体化系统界面规范 - 控件 - 面包屑

## 1. 标准面包屑

```vue
<template>
  <a-breadcrumb>
    <a-breadcrumb-item>
      <a @click="navigateTo('home')">用款计划</a>
    </a-breadcrumb-item>
    <a-breadcrumb-item>
      <a @click="navigateTo('list')">录入</a>
    </a-breadcrumb-item>
    <a-breadcrumb-item>详情</a-breadcrumb-item>
  </a-breadcrumb>
</template>

<style scoped>
/**
 * 标准面包屑颜色规范：
 * - 当前页面: #434343
 * - 非当前页面: #8C8C8C
 * - 分隔符: #8C8C8C
 * - 非当前页面悬停: #1890FF
 */
:deep(.ant-breadcrumb-link) {
  color: #8c8c8c;
}
:deep(.ant-breadcrumb-link a) {
  color: #8c8c8c;
}
:deep(.ant-breadcrumb-link a:hover) {
  color: #1890ff;
}
:deep(.ant-breadcrumb > span:last-child .ant-breadcrumb-link) {
  color: #434343;
}
:deep(.ant-breadcrumb-separator) {
  color: #8c8c8c;
}
</style>
```

## 2. 带图标面包屑

用于二级全屏页面的操作栏。

```vue
<template>
  <a-breadcrumb>
    <a-breadcrumb-item>
      <a @click="navigateTo('home')">
        <HomeOutlined /> 首页
      </a>
    </a-breadcrumb-item>
    <a-breadcrumb-item>
      <a @click="navigateTo('budget')">
        <AccountBookOutlined /> 预算管理
      </a>
    </a-breadcrumb-item>
    <a-breadcrumb-item>用款计划录入</a-breadcrumb-item>
  </a-breadcrumb>
</template>
```

## 3. 折叠面包屑

打开页面多、空间不足时进行中间页面折叠。

```vue
<template>
  <div class="collapsible-breadcrumb">
    <a-breadcrumb>
      <!-- 一级页面 -->
      <a-breadcrumb-item>
        <a @click="navigateTo(0)">用款计划</a>
      </a-breadcrumb-item>
      <!-- 二级页面 -->
      <a-breadcrumb-item>
        <a @click="navigateTo(1)">录入</a>
      </a-breadcrumb-item>
      <!-- 折叠中间页面 -->
      <a-breadcrumb-item v-if="pages.length > 4">
        <a-popover trigger="hover" placement="bottom">
          <template #content>
            <div class="collapsed-pages">
              <a
                v-for="(page, index) in collapsedPages"
                :key="index"
                class="collapsed-page-link"
                @click="navigateTo(index + 2)"
              >
                {{ page.title }}
              </a>
            </div>
          </template>
          <span class="breadcrumb-ellipsis">...</span>
        </a-popover>
      </a-breadcrumb-item>
      <!-- 上级页面 -->
      <a-breadcrumb-item>
        <a @click="navigateTo(pages.length - 2)">
          {{ pages[pages.length - 2]?.title }}
        </a>
      </a-breadcrumb-item>
      <!-- 当前页面 -->
      <a-breadcrumb-item>
        {{ pages[pages.length - 1]?.title }}
      </a-breadcrumb-item>
    </a-breadcrumb>
  </div>
</template>

<script setup lang="ts">
/**
 * 折叠面包屑规范：
 * - 保留：一级页面、二级页面、当前页面上级、当前页面
 * - 中间页面通过 ... 折叠
 * - 点击 ... 浮动显示折叠的页面
 * - 面包屑长度不超过页面宽度 60%
 * - 层数建议不超过十层
 */
</script>

<style scoped>
.collapsible-breadcrumb {
  max-width: 60%;
  overflow: hidden;
}
.breadcrumb-ellipsis {
  cursor: pointer;
  color: #8c8c8c;
}
.breadcrumb-ellipsis:hover {
  color: #1890ff;
}
.collapsed-page-link {
  display: block;
  padding: 4px 0;
  color: #8c8c8c;
}
.collapsed-page-link:hover {
  color: #1890ff;
}
</style>
```

## 规范要点

- 面包屑用于页面导航，表述页面调用层次
- 除当前页面外都可点击返回
- 面包屑长度不超过页面宽度 60%
- 层数不超过十层
- 长度过长时中间页面通过 `...` 展示
- 鼠标悬停时浮动显示隐藏的页面链接
- 当前页面 `#434343`
- 非当前页面/分隔符 `#8C8C8C`
- 非当前页面悬停 `#1890FF`
