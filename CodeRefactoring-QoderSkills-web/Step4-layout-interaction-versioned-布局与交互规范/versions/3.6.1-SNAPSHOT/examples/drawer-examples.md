# 抽屉页面样例

> 规范来源：一体化系统界面规范 - 业务系统部分 - 抽屉

## 1. 详情抽屉

不需要遮罩，点击外部自动关闭。

```vue
<template>
  <a-drawer
    v-model:open="visible"
    title="单据详情"
    :width="drawerWidth"
    :mask="false"
    :closable="true"
    @close="handleClose"
  >
    <!-- 面包屑导航（多级页面时） -->
    <template #title>
      <div class="drawer-header">
        <div class="drawer-header__nav">
          <a-button type="text" size="small" @click="goBack" v-if="pageStack.length > 1">
            <LeftOutlined /> 返回
          </a-button>
          <a-breadcrumb v-if="pageStack.length > 1">
            <a-breadcrumb-item
              v-for="(page, index) in pageStack"
              :key="index"
            >
              <a v-if="index < pageStack.length - 1" @click="navigateTo(index)">
                {{ page.title }}
              </a>
              <span v-else>{{ page.title }}</span>
            </a-breadcrumb-item>
          </a-breadcrumb>
          <span v-else class="drawer-title">{{ pageStack[0]?.title || '详情' }}</span>
        </div>
        <div class="drawer-header__actions">
          <!-- 快捷操作按钮 -->
          <a-button type="text" size="small" @click="handleRefresh">
            <ReloadOutlined />
          </a-button>
          <!-- 关闭按钮（最右侧） -->
        </div>
      </div>
    </template>

    <!-- 内容区域 -->
    <div class="drawer-content">
      <a-descriptions :column="1" bordered>
        <a-descriptions-item label="凭证号">ZF2024001234</a-descriptions-item>
        <a-descriptions-item label="预算单位">辽宁省公安厅</a-descriptions-item>
        <a-descriptions-item label="金额">1,234,567.89</a-descriptions-item>
        <a-descriptions-item label="状态">已审核</a-descriptions-item>
      </a-descriptions>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
/**
 * 详情抽屉规范：
 * - 从屏幕右侧弹出
 * - 右边与一级页面右边界对齐
 * - 不需要遮罩（:mask="false"）
 * - 点击外部区域自动关闭
 * - 高度与功能区等高
 * - 不应出现横向滚动条
 * - 总宽度不超过屏幕一半
 */
const drawerWidth = 480; // 一列抽屉
// const drawerWidth = 720; // 两列抽屉
</script>

<style scoped>
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.drawer-header__nav {
  display: flex;
  align-items: center;
  gap: 8px;
}
.drawer-header__actions {
  display: flex;
  align-items: center;
  gap: 4px;
}
.drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #434343;
}
</style>
```

## 2. 录入抽屉

需要全屏遮罩，不能操作上级页面。

```vue
<template>
  <a-drawer
    v-model:open="visible"
    title="编辑单据"
    :width="600"
    :mask="true"
    :mask-closable="false"
    @close="handleClose"
  >
    <div class="drawer-content">
      <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="支出功能分类" required>
          <a-tree-select v-model:value="formData.funcCategory" placeholder="请选择" />
        </a-form-item>
        <a-form-item label="金额" required>
          <a-input-number
            v-model:value="formData.amount"
            :precision="2"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="formData.remark" :rows="3" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </div>

    <template #footer>
      <div class="drawer-footer">
        <a-button @click="handleClose">取消</a-button>
        <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
      </div>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
/**
 * 录入抽屉规范：
 * - 需要全屏遮罩（:mask="true"）
 * - 打开后不能操作上级页面（:mask-closable="false"）
 * - 录入控件遵从录入卡片规范
 * - 不应出现横向滚动条
 * - 纵向滚动时只滚动内容区，不滚动标题和操作区
 */
</script>

<style scoped>
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
```

## 3. 审核日志抽屉

```vue
<template>
  <a-drawer
    v-model:open="visible"
    title="审核日志"
    :width="500"
    :mask="false"
    @close="handleClose"
  >
    <a-timeline mode="left">
      <!-- 未来节点（上方） -->
      <a-timeline-item
        v-for="node in futureNodes"
        :key="node.id"
        color="gray"
      >
        <div class="audit-node audit-node--future">
          <div class="audit-node__position">{{ node.positionName }}</div>
        </div>
      </a-timeline-item>

      <!-- 当前节点 -->
      <a-timeline-item color="blue">
        <div class="audit-node audit-node--current">
          <div class="audit-node__position">{{ currentNode.positionName }}</div>
          <div class="audit-node__info">
            <span>审核人：{{ currentNode.auditorName }}</span>
            <span>联系电话：{{ currentNode.phone }}</span>
          </div>
        </div>
      </a-timeline-item>

      <!-- 已审核节点（下方，按时间倒序） -->
      <a-timeline-item
        v-for="node in completedNodes"
        :key="node.id"
        :color="node.isReverse ? 'red' : 'green'"
      >
        <div class="audit-node audit-node--completed">
          <div class="audit-node__position">
            {{ node.positionName }}
            <a-tag :color="node.isReverse ? 'red' : 'green'" size="small">
              {{ node.operationType }}
            </a-tag>
          </div>
          <div class="audit-node__info">
            <span>{{ node.operatorName }}</span>
            <span>{{ node.operateTime }}</span>
          </div>
          <div class="audit-node__opinion" v-if="node.opinion">
            {{ node.opinion }}
          </div>
        </div>
      </a-timeline-item>
    </a-timeline>
  </a-drawer>
</template>

<script setup lang="ts">
/**
 * 审核日志规范：
 * - 使用抽屉页面，不需要遮罩
 * - 显示：已完成节点、当前节点、未来节点
 * - 三类节点不同显示风格
 * - 已审核节点：操作人、时间、意见、操作类型
 *   正向操作（审核通过等）绿色，逆向操作（退回等）红色
 * - 当前节点：审核人、联系电话
 * - 未来节点：仅显示岗位名称
 * - 时间顺序倒排：已审核在下，未审核在上
 * - 当前节点在中央可见位置
 *
 * 菜单命名规范：业务单据 + 操作
 *   如：用款计划录入、用款计划审核
 * 流程节点命名规范：岗位名称
 *   如：单位录入、单位审核、业务处室初审
 */
</script>

<style scoped>
.audit-node__position {
  font-size: 14px;
  font-weight: 600;
  color: #434343;
  margin-bottom: 4px;
}
.audit-node__info {
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  gap: 16px;
}
.audit-node__opinion {
  font-size: 12px;
  color: #595959;
  margin-top: 4px;
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 2px;
}
.audit-node--future .audit-node__position {
  color: #bfbfbf;
}
.audit-node--current .audit-node__position {
  color: #1890ff;
}
</style>
```

## 规范要点

- 抽屉从屏幕右侧弹出，右边与一级页面对齐
- 详情抽屉无遮罩，点击外部关闭
- 录入抽屉有遮罩，不可操作其他区域
- 高度与功能区等高
- 总宽度不超过屏幕一半
- 不出现横向滚动条
- 纵向滚动只滚动内容区
- 多级页面通过面包屑导航
- 关闭按钮关闭页面，返回按钮返回上一级
- 快捷操作按钮在返回和关闭之间
