# 分页组件样例

> 规范来源：一体化系统界面规范 - 控件 - 分页

## 颜色规范

| 元素 | 色值 |
|------|------|
| 录入框文字 | #434343 |
| 标签文字 | #595959 |
| 当前页按钮背景 | #1890FF |
| 当前页按钮文字 | #FFFFFF |
| 非当前页悬停背景 | #F5F5F5 |

## 1. 极简模式

固定每页行数，仅保留页数导航，用于空间不足的场景。

```vue
<template>
  <a-pagination
    v-model:current="current"
    :total="total"
    :page-size="pageSize"
    simple
    size="small"
  />
</template>
```

## 2. 简单模式

在极简模式基础上增加每页行数切换。

```vue
<template>
  <a-pagination
    v-model:current="current"
    v-model:page-size="pageSize"
    :total="total"
    :show-size-changer="true"
    :page-size-options="['10', '20', '50', '100']"
    size="small"
  />
</template>
```

## 3. 中等模式

在简单模式基础上增加当前页跳转。

```vue
<template>
  <a-pagination
    v-model:current="current"
    v-model:page-size="pageSize"
    :total="total"
    :show-size-changer="true"
    :show-quick-jumper="true"
    size="small"
  />
</template>
```

## 4. 标准模式

在中等模式基础上增加数据汇总信息（完整形态）。

```vue
<template>
  <div class="standard-pagination">
    <a-pagination
      v-model:current="current"
      v-model:page-size="pageSize"
      :total="total"
      :show-size-changer="true"
      :show-quick-jumper="true"
      :show-total="(total: number, range: number[]) => `共 ${total} 条，当前 ${range[0]}-${range[1]} 条`"
    />
  </div>
</template>

<style scoped>
/* 分页栏位于表格下方，靠右对齐 */
.standard-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
```

## 5. 与表格配合使用

```vue
<template>
  <a-table
    :columns="columns"
    :data-source="dataSource"
    :pagination="{
      current: pagination.current,
      pageSize: pagination.pageSize,
      total: pagination.total,
      showSizeChanger: true,
      showQuickJumper: true,
      showTotal: (total) => `共 ${total} 条`,
      pageSizeOptions: ['10', '20', '50', '100'],
      position: ['bottomRight'],
    }"
    @change="handleTableChange"
  />
</template>
```

## 规范要点

- 分页栏位于表格下方，靠右与表格对齐
- 从左到右依次是：数据汇总信息、页数导航、每页行数切换、当前页跳转
- 空间不足时可裁剪，只保留必要导航
- 当前页按钮：背景 `#1890FF`，文字 `#FFFFFF`
- 非当前页悬停：背景 `#F5F5F5`
