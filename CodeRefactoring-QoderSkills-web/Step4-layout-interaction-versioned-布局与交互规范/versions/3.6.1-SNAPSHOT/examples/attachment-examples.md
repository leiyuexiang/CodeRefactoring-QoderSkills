# 附件管理样例

> 规范来源：一体化系统界面规范 - 业务系统部分 - 附件管理

## 1. 附件上传 - 不设置分类

```vue
<template>
  <div class="attachment-upload">
    <div class="attachment-upload__header">
      <span class="attachment-upload__title">附件</span>
      <span class="attachment-upload__count">({{ fileList.length }})</span>
    </div>

    <!-- 未上传状态 -->
    <div v-if="fileList.length === 0" class="attachment-upload__empty">
      <a-upload
        :file-list="fileList"
        :before-upload="beforeUpload"
        :custom-request="handleUpload"
        multiple
      >
        <a-button>
          <UploadOutlined /> 上传附件
        </a-button>
      </a-upload>
    </div>

    <!-- 已上传状态 -->
    <div v-else class="attachment-upload__list">
      <a-upload
        :file-list="fileList"
        :before-upload="beforeUpload"
        :custom-request="handleUpload"
        @remove="handleRemove"
        list-type="text"
      >
        <a-button size="small"><UploadOutlined /> 继续上传</a-button>
      </a-upload>
    </div>
  </div>
</template>
```

## 2. 附件上传 - 设置分类

```vue
<template>
  <div class="attachment-upload--categorized">
    <div class="attachment-category" v-for="category in categories" :key="category.id">
      <div class="attachment-category__header">
        <span class="attachment-category__name">{{ category.name }}</span>
        <span class="attachment-category__count">({{ category.files.length }})</span>
        <a-tag v-if="category.required" color="red">必传</a-tag>
      </div>
      <div class="attachment-category__body">
        <a-upload
          :file-list="category.files"
          :custom-request="(info) => handleCategoryUpload(info, category.id)"
          @remove="(file) => handleCategoryRemove(file, category.id)"
          list-type="text"
        >
          <a-button size="small"><UploadOutlined /> 上传</a-button>
        </a-upload>
      </div>
    </div>
  </div>
</template>
```

## 3. 附件上传 - 列表显示模式

```vue
<template>
  <a-table :columns="attachColumns" :data-source="fileList" size="small" :pagination="false">
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'name'">
        <a @click="previewFile(record)">{{ record.name }}</a>
      </template>
      <template v-if="column.key === 'action'">
        <a-button type="link" size="small" @click="downloadFile(record)">下载</a-button>
        <a-button type="link" size="small" danger @click="removeFile(record)">删除</a-button>
      </template>
    </template>
  </a-table>
</template>

<script setup lang="ts">
const attachColumns = [
  { title: '序号', key: 'index', width: 60, align: 'center' },
  { title: '文件名', key: 'name', ellipsis: true },
  { title: '分类', dataIndex: 'category', width: 120 },
  { title: '大小', dataIndex: 'size', width: 100, align: 'right' },
  { title: '上传时间', dataIndex: 'uploadTime', width: 160, align: 'center' },
  { title: '操作', key: 'action', width: 120, align: 'center' },
];
</script>
```

## 4. 附件查看 - 无分类

```vue
<template>
  <div class="attachment-view">
    <a-list :data-source="fileList" size="small">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <template #avatar>
              <PaperClipOutlined />
            </template>
            <template #title>
              <a @click="previewFile(item)">{{ item.name }}</a>
            </template>
            <template #description>
              {{ item.size }} | {{ item.uploadTime }}
            </template>
          </a-list-item-meta>
          <template #actions>
            <a @click="downloadFile(item)">下载</a>
          </template>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>
```

## 5. 附件查看 - 有分类

```vue
<template>
  <div class="attachment-view--categorized">
    <a-collapse v-model:activeKey="activeCategoryKeys">
      <a-collapse-panel
        v-for="category in categories"
        :key="category.id"
        :header="`${category.name} (${category.files.length})`"
      >
        <a-list :data-source="category.files" size="small">
          <template #renderItem="{ item }">
            <a-list-item>
              <a @click="previewFile(item)">{{ item.name }}</a>
              <template #actions>
                <a @click="downloadFile(item)">下载</a>
              </template>
            </a-list-item>
          </template>
        </a-list>
      </a-collapse-panel>
    </a-collapse>
  </div>
</template>
```

## 规范要点

- 附件上传支持分类/无分类两种模式
- 列表显示模式适合大量附件管理
- 附件查看暂无操作功能（后续可添加删除、上传）
- 查看模式分为有分类和无分类
