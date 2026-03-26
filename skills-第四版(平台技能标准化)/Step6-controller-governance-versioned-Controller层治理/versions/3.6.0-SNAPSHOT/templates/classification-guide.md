# 外部/内部接口分类判断指南

## 分类判断流程

```
Controller 文件
    │
    ├── 检查 @RequestMapping 路径前缀
    │   ├── 一级路径为 run/  → custom/（外部接口）
    │   ├── 一级路径为 config/ → common/（内部接口）
    │   └── 无明确前缀 → 进入业务职责判断
    │
    └── 业务职责判断
        ├── 面向前端 UI 操作 → custom/
        ├── 内部 API / 工具 / 调试 → common/
        └── 不确定 → 默认归入 custom/
```

## 详细分类标准

### 归入 custom/（外部接口）的特征

| 特征 | 示例 |
|------|------|
| 路径以 `run/` 开头 | `@RequestMapping("/run/element/query")` |
| 提供前端页面操作的 CRUD 接口 | 增删改查要素、账套、单位 |
| 返回 VO 对象供前端展示 | `ReturnData<ElementVO>` |
| 接收前端表单提交 | `@RequestBody ElementDTO dto` |
| 包含分页查询 | `ReturnPage<ElementVO>` |
| 面向第三方系统的开放接口 | 对外API |

### 归入 common/（内部接口）的特征

| 特征 | 示例 |
|------|------|
| 路径以 `config/` 开头 | `@RequestMapping("/config/element/init")` |
| 内部微服务间调用的接口 | `/api/v1/element/getById` |
| 工具/调试类接口 | 缓存清理、数据刷新 |
| 通知/回调类接口 | 消息通知、事件回调 |
| 数据同步类接口 | 定时同步、增量同步 |
| 健康检查/监控类接口 | 健康检查、状态查询 |

## 常见二级分组映射

### 原始目录 → 目标目录

| 原始位置 | 目标位置 | 分类原因 |
|---------|---------|---------|
| `controller/basedata/` | `controller/custom/basedata/` | 面向前端的基础数据管理 |
| `controller/bookset/` | `controller/custom/bookset/` | 面向前端的账套管理 |
| `controller/agencyManager/` | `controller/custom/agencyManager/` | 面向前端的单位管理 |
| `controller/api/` | `controller/common/api/` | 内部API接口 |
| `controller/util/` | `controller/common/util/` | 工具/调试类 |
| `controller/notify/` | `controller/common/notify/` | 通知类接口 |
| `controller/imp/` | 根据业务判断 | 需逐个分析 |

## 边界情况处理

| 场景 | 处理方式 |
|------|---------|
| 同一 Controller 既有外部接口又有内部接口 | 建议拆分为两个 Controller |
| Controller 不属于任何业务分组 | 新建合适的二级分组目录 |
| 非 Controller 类（工具类、常量类）在 controller 包下 | 迁移到 `common/` 下或移出 controller 包 |
| 无法判断归属 | 默认归入 custom/，在报告中标注待确认 |
