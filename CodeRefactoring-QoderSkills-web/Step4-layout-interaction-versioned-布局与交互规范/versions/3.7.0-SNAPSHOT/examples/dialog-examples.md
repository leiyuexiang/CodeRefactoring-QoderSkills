# 对话框样例

> 规范来源：一体化系统界面规范 - 业务系统部分 - 模式对话框

## 1. 提示框

只带一个确定按钮，放置在页面右下角。

```vue
<template>
  <a-modal
    v-model:open="visible"
    title="提示"
    :closable="false"
    :mask-closable="false"
    :footer="null"
    centered
  >
    <div class="modal-content">
      <ExclamationCircleOutlined class="modal-icon modal-icon--warning" />
      <p class="modal-message">{{ message }}</p>
    </div>
    <div class="modal-footer">
      <a-button type="primary" @click="handleConfirm">确定</a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 提示框规范：
 * - 提示信息清晰简洁，避免口语化
 * - 用最少的字数表达准确含义
 * - 前端预警/提示适用非阻断的自隐浮动提示框
 * - 后台错误应枚举错误类型，定义标准错误码
 */
</script>

<style scoped>
.modal-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.modal-icon {
  font-size: 22px;
  flex-shrink: 0;
  margin-top: 2px;
}
.modal-icon--warning {
  color: #faad14;
}
.modal-message {
  color: #434343;
  font-size: 14px;
  margin: 0;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}
</style>
```

## 2. 确认框

确定和取消两个按钮，确定在右侧蓝色背景，取消在左侧。

```vue
<template>
  <a-modal
    v-model:open="visible"
    title="确认"
    centered
    @ok="handleOk"
    @cancel="handleCancel"
    :ok-text="'确定'"
    :cancel-text="'取消'"
  >
    <div class="modal-content">
      <ExclamationCircleOutlined class="modal-icon modal-icon--confirm" />
      <p class="modal-message">确定要删除选中的 {{ selectedCount }} 条数据吗？</p>
    </div>
  </a-modal>
</template>

<style scoped>
/**
 * 确认框规范：
 * - 确定按钮在右侧，蓝色背景
 * - 取消按钮在左侧
 * - 按钮放置在页面右下角
 */
.modal-icon--confirm {
  color: #1890ff;
}
</style>
```

## 3. 单栏录入框

宽度固定 440px，高度 280px ~ 720px 自适应。

```vue
<template>
  <a-modal
    v-model:open="visible"
    title="审核通过"
    :width="440"
    centered
    @ok="handleSubmit"
  >
    <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="审核意见">
        <a-textarea
          v-model:value="formData.opinion"
          placeholder="请输入审核意见"
          :rows="4"
        />
      </a-form-item>
    </a-form>
    <!-- 常用意见（可选） -->
    <div class="common-opinions">
      <a-button size="small" @click="toggleOpinions">
        常用意见 <DownOutlined />
      </a-button>
      <div class="opinions-panel" v-if="opinionsVisible">
        <div
          v-for="opinion in commonOpinions"
          :key="opinion.id"
          class="opinion-item"
          @click="selectOpinion(opinion)"
          @mouseenter="hoveredOpinion = opinion.id"
          @mouseleave="hoveredOpinion = null"
        >
          <span>{{ opinion.text }}</span>
          <div class="opinion-actions" v-if="hoveredOpinion === opinion.id">
            <a-tooltip :title="opinion.pinned ? '取消置顶' : '置顶'">
              <PushpinOutlined @click.stop="togglePin(opinion)" />
            </a-tooltip>
            <a-tooltip title="修改">
              <EditOutlined @click.stop="editOpinion(opinion)" />
            </a-tooltip>
            <a-tooltip title="删除">
              <DeleteOutlined @click.stop="deleteOpinion(opinion)" />
            </a-tooltip>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
/**
 * 常用意见规范：
 * - 按使用频率排序，频繁的在前面
 * - 支持置顶/取消置顶
 * - 有置顶数据时按 是否置顶 + 使用次数 排序
 * - 浏览状态：选择意见，悬停显示操作图标
 * - 新增状态：列表末尾编辑框，其他数据不可选
 * - 修改状态：当前项编辑框，其他数据不可选
 * - 编辑未提交时关闭面板则放弃操作
 */
</script>
```

## 4. 两栏录入框

宽度固定 856px，高度 520px ~ 720px 自适应。

```vue
<template>
  <a-modal
    v-model:open="visible"
    title="指定退回"
    :width="856"
    centered
    @ok="handleSubmit"
  >
    <a-form :model="formData" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="退回类型" required>
            <a-radio-group v-model:value="formData.returnType">
              <a-radio value="prev">退回上一岗</a-radio>
              <a-radio value="input">退回录入岗</a-radio>
              <a-radio value="specified">退回指定岗</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col :span="12" v-if="formData.returnType === 'specified'">
          <a-form-item label="退回岗位" required>
            <a-select v-model:value="formData.targetPosition" placeholder="请选择退回岗位" />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label="退回意见" :label-col="{ span: 2 }" :wrapper-col="{ span: 22 }">
            <a-textarea v-model:value="formData.opinion" :rows="4" placeholder="请输入退回意见" />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>
```

## 规范要点

- 模式对话框设置全屏页面遮罩
- 打开后不能操作其他区域
- 提示框：一个确定按钮，右下角
- 确认框：确定（右侧蓝色）+ 取消（左侧），右下角
- 单栏录入框：宽 440px，高 280-720px
- 两栏录入框：宽 856px，高 520-720px
- 弹窗宽高比 > 0.6
- 弹窗不应包含表格，包含时建议使用全屏页面
- 详情查看不建议使用弹窗
