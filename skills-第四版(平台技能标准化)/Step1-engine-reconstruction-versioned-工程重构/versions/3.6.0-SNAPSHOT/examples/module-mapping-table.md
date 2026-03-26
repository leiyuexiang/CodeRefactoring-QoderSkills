# 模块映射表示例

重构 Phase 1 分析完成后，输出的模块映射表格式：

```
| 源路径 | 目标层级 | 目标路径 | 操作类型 |
|--------|----------|----------|----------|
| grp-framework-api/ | 能力层 (API) | framework-module/grp-capability-framework/grp-framework-api/ | 移动 |
| framework-server2/ | 能力层 (Controller) | framework-module/grp-capability-framework/framework-server/ | 移动+重命名 |
| framework-server2-com/ | 能力层 (Service) | framework-module/grp-capability-framework/framework-server-com/ | 移动+重命名 |
| framework-server2-springcloud/ | 聚合层 | framework-module/grp-aggregation-framework/framework-server-springcloud/ | 移动+重命名 |
| framework-feign-com/ | 能力层 | framework-module/grp-capability-framework/framework-feign-com/ | 移动 |
| 4A-server-api/ | 能力层 (API) | 4a-module/grp-capability-4a/grp-4a-api/ | 移动+重命名 |
```
