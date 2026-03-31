# 提示消息样例

> 规范来源：一体化系统界面规范 - 提示

## 1. 成功提示

```vue
<template>
  <a-button @click="showSuccess">保存</a-button>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue';

const showSuccess = () => {
  message.success('保存成功');
};
</script>
```

## 2. 警告提示

```vue
<script setup lang="ts">
import { message } from 'ant-design-vue';

const showWarning = () => {
  message.warning('金额超出预算控制数，请确认');
};
</script>
```

## 3. 错误提示

```vue
<script setup lang="ts">
import { message } from 'ant-design-vue';

const showError = () => {
  message.error('网络异常，请稍后重试');
};
</script>
```

## 4. 超长提示

```vue
<script setup lang="ts">
import { message } from 'ant-design-vue';

/**
 * 超长提示规范：
 * - 信息不宜过长，应简洁明了
 * - 较多时适当延长停留时间
 * - 最多折行显示两行，超过用省略号
 * - 鼠标悬停时不自动隐藏，离开后再隐藏
 */
const showLongMessage = () => {
  message.info({
    content: '操作完成，共处理了 100 条数据，其中成功 98 条，失败 2 条...',
    duration: 5, // 信息较多时延长停留时间
  });
};
</script>

<style>
/* 全局样式：限制提示信息最多两行 */
.ant-message-notice-content {
  max-width: 500px;
}
.ant-message-custom-content {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 悬停时显示全部 */
.ant-message-custom-content:hover {
  -webkit-line-clamp: unset;
  overflow: visible;
}
</style>
```

## 5. 全局自隐提示配置

```typescript
/**
 * 统一提示配置
 * - 显示位置：操作界面上部中央（推荐顶栏）
 * - 默认停留 3 秒后自动消失
 * - 所有提示优先使用全局自隐提示，不打断用户操作
 */
import { message } from 'ant-design-vue';

message.config({
  top: '60px',     // 显示在顶栏下方
  duration: 3,     // 默认停留3秒
  maxCount: 3,     // 最多同时显示3条
});
```

## 6. 错误码管理

```typescript
/**
 * 后台错误信息规范：
 * - 枚举错误类型，定义标准错误码
 * - 后台返回标准错误码和错误提示信息
 * - 前端根据错误码显示对应提示
 * - 支持运维人员关联知识库，提供解决方案
 * - 支持运维人员定制提示信息
 */
interface ErrorResponse {
  code: string;       // 标准错误码
  message: string;    // 错误提示信息
  detail?: string;    // 详细信息（可选）
  solution?: string;  // 解决方案（可选，来自运维知识库）
}

const handleApiError = (error: ErrorResponse) => {
  if (error.solution) {
    message.error(`${error.message}（${error.solution}）`);
  } else {
    message.error(error.message);
  }
};
```

## 规范要点

- 所有提示优先使用全局自隐提示，不打断用户操作
- 显示在操作界面上部中央，尽量不遮挡功能区
- 默认停留 3 秒后自动消失
- 信息简洁明了，最多两行，超过用省略号
- 鼠标悬停时不自动隐藏，离开后再隐藏
- 后台错误使用标准错误码体系
