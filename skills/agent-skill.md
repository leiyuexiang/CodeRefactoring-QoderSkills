---
name: agent-skills
description: 通过执行其他skills自动完成流程上的系统开发
---

# Maven模块快速创建

## 四层架构说明

| 层级 | 后缀示例 | 职责 |
|------|----------|------|
| 体验层 | `-feign-com` | Feign接口封装，供其他服务调用 |
| 聚合层 | `-server-springcloud/huawei/tencent` | 服务启动编排，纯启动类 |
| 能力层 | `-server`, `-server-com`, `grp-*-api` | 业务实现(Controller/Service/DAO) |
| 底座层 | `grp-common`, `grp-util-com` | 通用基础设施 |