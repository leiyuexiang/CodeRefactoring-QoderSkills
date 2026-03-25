# 模块识别、归类与重命名规则

## 一、按名称模式自动识别目标层级

| 名称模式 | 目标层级 | 目标容器 |
|----------|----------|----------|
| `grp-*-com` (logger/exception/util/database/cache/gray 等通用) | 底座层 | `grp-common-boot/` |
| `grp-{module}-api` | 能力层 | `grp-capability-{module}/` |
| `{module}-server` 或 `{module}-server{N}` | 能力层 | `grp-capability-{module}/` |
| `{module}-server-com` 或 `{module}-server{N}-com` | 能力层 | `grp-capability-{module}/` |
| `{module}-server-springcloud` 或 `{module}-server{N}-springcloud` | 聚合层 | `grp-aggregation-{module}/` |
| `{module}-server-huawei` 或 `{module}-server{N}-huawei` | 聚合层 | `grp-aggregation-{module}/` |
| `{module}-server-tencent` 或 `{module}-server{N}-tencent*` | 聚合层 | `grp-aggregation-{module}/` |
| `{module}-server-pivotal` 或 `{module}-server{N}-pivotal` | 聚合层 | `grp-aggregation-{module}/` |
| `{module}-feign-com` 或 `{module}-feign-api` | 能力层 | `grp-capability-{module}/` |

## 二、模块名提取规则

从源模块名中提取业务模块名 `{module}`：

1. 去掉版本号后缀（如 `element-server2` → `element`，取 `-server` 前的部分）
2. 去掉框架后缀（如 `element-server2-springcloud` → `element`）
3. 去掉 `-com` 后缀（如 `element-server2-com` → `element`）

## 三、模块重命名映射（去掉版本号后缀）

- `element-server2` → `element-server`
- `element-server2-com` → `element-server-com`
- `element-server2-springcloud` → `element-server-springcloud`
- 以此类推，所有 `*2` 后缀统一去除
- `4A-*` → `grp-4a-*`（统一前缀+全小写）

## 四、移动操作规则

分析规则：
- 以 `grp-*-com/` 命名的通用模块 → 底座层 `grp-common-boot/`
- 以 `*-server/`、`*-server-com/`、`*-server2/`、`*-server2-com/` 命名 → 能力层 `grp-capability-{module}/`
- 以 `grp-*-api/` 命名 → 能力层 `grp-capability-{module}/`
- 以 `*-springcloud/`、`*-huawei/`、`*-tencent/`、`*-pivotal/` 命名 → 聚合层 `grp-aggregation-{module}/`
- 以 `*-feign-com/`、`*-feign-api/` 命名 → 能力层 `grp-capability-{module}/`

执行规则：
1. 使用 `mv` 或 `cp -r` 移动整个模块目录（含 src/、resources/、pom.xml）
2. 移动顺序：底座层 → 能力层 → 聚合层 → 体验层
3. 每移动一个模块后立即记录日志
4. **移动完成后必须删除旧源目录**（详见 Phase 3.5）

## 五、依赖吸收规则（单模块业务域合并）

当某个业务域（如 `4a`）在本项目中**仅有 1~2 个叶子模块**（通常只有一个 `*-api` 模块），
且该模块主要作为**主业务模块的依赖**而存在时，**不应**为其创建独立的 `{module}-module/` 容器。

### 判定条件

同时满足以下条件时，触发依赖吸收：
1. 该业务域在本项目中的叶子模块数量 ≤ 2
2. 该模块被主业务模块的叶子模块直接依赖（出现在其 `<dependencies>` 中）
3. 该模块自身不包含 SpringCloud 启动类（即不是独立可部署服务）

### 吸收策略

| 被吸收模块类型 | 放置位置 |
|---------------|---------|
| `*-api` | 主业务模块的 `grp-capability-{主module}/` |
| `*-com` | 主业务模块的 `grp-capability-{主module}/` |
| `*-feign-com` | 主业务模块的 `grp-capability-{主module}/` |

### 示例

```
原始模块：4A-server-api（仅此一个 4A 模块，被 framework-server2-com 依赖）
判定：4A 业务域仅 1 个模块，且作为 framework 的依赖 → 触发吸收
结果：重命名为 grp-4a-api，放入 framework-module/grp-capability-framework/grp-4a-api/
       （而非创建独立的 4a-module/grp-capability-4a/grp-4a-api/）
```

### 不吸收的情况

以下情况仍需创建独立的 `{module}-module/`：
- 该业务域有 3 个以上叶子模块（说明是完整的独立业务）
- 该业务域包含 `*-springcloud` 模块（说明是独立可部署服务）
- 该业务域的模块不被主业务模块依赖（说明是并列业务）

## 六、无法自动识别的模块

对于不符合上述任何模式的模块（如 `demo`、`hzero-demo-cpy`、地域定制模块 `guangdong`、`shenzhen`），输出警告并跳过，由用户手动决定归类。
