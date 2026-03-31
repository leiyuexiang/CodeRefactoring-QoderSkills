# 审核日志模板

> 含流程节点、三种状态展示的审核日志模板。

## 模板代码

```vue
<template>
  <a-drawer
    v-model:open="visible"
    title="审核日志"
    :width="520"
    :mask="false"
    @close="close"
  >
    <a-timeline mode="left" class="audit-timeline">
      <!-- 未来节点（上方，仅显示岗位名称） -->
      <a-timeline-item
        v-for="node in futureNodes"
        :key="node.id"
        color="gray"
      >
        <div class="audit-node audit-node--future">
          <div class="audit-node__position">{{ node.positionName }}</div>
        </div>
      </a-timeline-item>

      <!-- 当前节点（中央位置） -->
      <a-timeline-item color="blue" v-if="currentNode">
        <div class="audit-node audit-node--current">
          <div class="audit-node__position">
            {{ currentNode.positionName }}
            <a-tag color="blue" size="small">当前节点</a-tag>
          </div>
          <div class="audit-node__detail">
            <div class="audit-node__info-item" v-if="currentNode.auditors.length > 0">
              <span class="info-label">审核人：</span>
              <span>{{ currentNode.auditors.slice(0, 3).map(a => a.name).join('、') }}</span>
              <span v-if="currentNode.auditors.length > 3">等{{ currentNode.auditors.length }}人</span>
            </div>
            <div class="audit-node__info-item" v-if="currentNode.phone">
              <span class="info-label">联系电话：</span>
              <span>{{ currentNode.phone }}</span>
            </div>
          </div>
        </div>
      </a-timeline-item>

      <!-- 已审核节点（下方，按时间倒序） -->
      <a-timeline-item
        v-for="node in completedNodes"
        :key="node.id"
        :color="isReverseOp(node.operationType) ? 'red' : 'green'"
      >
        <div class="audit-node audit-node--completed" @click="toggleNodeDetail(node.id)">
          <div class="audit-node__position">
            {{ node.positionName }}
            <a-tag
              :color="isReverseOp(node.operationType) ? 'red' : 'green'"
              size="small"
            >
              {{ node.operationType }}
            </a-tag>
          </div>
          <div class="audit-node__detail" v-show="expandedNodeIds.includes(node.id)">
            <div class="audit-node__info-item">
              <span class="info-label">操作人：</span>
              <span>{{ node.operatorName }}</span>
            </div>
            <div class="audit-node__info-item">
              <span class="info-label">操作时间：</span>
              <span>{{ node.operateTime }}</span>
            </div>
            <div class="audit-node__opinion" v-if="node.opinion">
              <span class="info-label">审核意见：</span>
              <span>{{ node.opinion }}</span>
            </div>
          </div>
        </div>
      </a-timeline-item>
    </a-timeline>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue';

// ============ 数据类型 ============
interface Auditor {
  name: string;
  phone?: string;
}

interface AuditNode {
  id: string;
  positionName: string;
  status: 'completed' | 'current' | 'future';
  operationType?: string;    // 新增、修改、送审、撤销送审、审核通过、审核退回、撤销审核
  operatorName?: string;
  operateTime?: string;
  opinion?: string;
  auditors?: Auditor[];
  phone?: string;
}

// ============ 状态 ============
const visible = ref(false);
const nodes = ref<AuditNode[]>([]);
const expandedNodeIds = ref<string[]>([]);

// ============ 计算属性 ============

/** 未来节点（上方） */
const futureNodes = computed(() =>
  nodes.value.filter((n) => n.status === 'future')
);

/** 当前节点 */
const currentNode = computed(() =>
  nodes.value.find((n) => n.status === 'current')
);

/** 已审核节点（下方，时间倒序） */
const completedNodes = computed(() =>
  nodes.value
    .filter((n) => n.status === 'completed')
    .sort((a, b) => {
      if (!a.operateTime || !b.operateTime) return 0;
      return new Date(b.operateTime).getTime() - new Date(a.operateTime).getTime();
    })
);

/**
 * 操作类型分类：
 * - 正向操作（绿色）：新增、修改、送审、审核通过
 * - 逆向操作（红色）：撤销送审、审核退回、撤销审核
 */
const reverseOps = ['撤销送审', '审核退回', '撤销审核'];
const isReverseOp = (type: string | undefined) => reverseOps.includes(type || '');

/** 切换节点详情展开/折叠 */
const toggleNodeDetail = (id: string) => {
  const idx = expandedNodeIds.value.indexOf(id);
  if (idx >= 0) {
    expandedNodeIds.value.splice(idx, 1);
  } else {
    expandedNodeIds.value.push(id);
  }
};

// ============ 公共方法 ============
const open = async (auditNodes: AuditNode[]) => {
  nodes.value = auditNodes;
  expandedNodeIds.value = auditNodes.filter((n) => n.status === 'completed').map((n) => n.id);
  visible.value = true;
  await nextTick();
  // 滚动到当前节点位置
  scrollToCurrentNode();
};

const close = () => { visible.value = false; };

const scrollToCurrentNode = () => {
  const currentEl = document.querySelector('.audit-node--current');
  currentEl?.scrollIntoView({ behavior: 'smooth', block: 'center' });
};

defineExpose({ open, close });
</script>

<style scoped>
.audit-timeline {
  padding: 16px 0;
}

/* 节点通用样式 */
.audit-node {
  cursor: pointer;
}
.audit-node__position {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.audit-node__detail {
  padding: 8px 0;
}
.audit-node__info-item {
  font-size: 12px;
  color: #595959;
  margin-bottom: 4px;
}
.info-label {
  color: #8c8c8c;
}
.audit-node__opinion {
  font-size: 12px;
  color: #595959;
  margin-top: 4px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 2px;
}

/* 未来节点 */
.audit-node--future .audit-node__position {
  color: #bfbfbf;
  font-weight: normal;
}

/* 当前节点 */
.audit-node--current .audit-node__position {
  color: #1890ff;
}

/* 已审核节点 */
.audit-node--completed .audit-node__position {
  color: #434343;
}
</style>
```

## 节点命名规范

| 维度 | 规范 | 示例 |
|------|------|------|
| 菜单名称 | 业务单据 + 操作 | 用款计划录入、用款计划审核 |
| 流程节点名称 | 岗位名称 | 单位录入、单位审核、业务处室初审 |
| 正向操作标签 | 绿色显示 | 新增、修改、送审、审核通过 |
| 逆向操作标签 | 红色显示 | 撤销送审、审核退回、撤销审核 |
