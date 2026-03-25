# 目标架构目录结构定义

```
{project-root}/
├── pom.xml                              # 根POM (packaging=pom, parent=spring-boot-starter-parent)
│                                        # 包含 dependencyManagement + repositories
├── grp-common-boot/                     # 底座层容器（如有）
│   ├── pom.xml                          # packaging=pom
│   ├── grp-logger-com/                  # 日志模块
│   ├── grp-exception-com/               # 异常模块
│   ├── grp-util-com/                    # 工具模块
│   └── grp-database-com/               # 数据库模块
├── {module}-module/                     # 业务模块容器 (每个业务一个)
│   ├── pom.xml                          # packaging=pom
│   ├── grp-common-{module}/             # 底座层容器（模块级）
│   │   ├── pom.xml                      # packaging=pom
│   │   └── ...                          # 模块级通用子模块（按需）
│   ├── grp-capability-{module}/         # 能力层容器
│   │   ├── pom.xml                      # packaging=pom
│   │   ├── grp-{module}-api/            # API定义 (可选)
│   │   ├── {module}-server/             # Controller层
│   │   ├── {module}-server-com/         # 业务实现层
│   │   └── {module}-feign-com/          # Feign SDK
│   ├── grp-aggregation-{module}/        # 聚合层容器
│   │   ├── pom.xml                      # packaging=pom
│   │   ├── {module}-server-springcloud/ # SC适配
│   │   ├── {module}-server-huawei/      # 华为适配
│   │   ├── {module}-server-pivotal/     # Pivotal适配
│   │   └── {module}-server-tencent/     # 腾讯适配
│   └── grp-experience-{module}/         # 体验层容器 (可选)
│       └── pom.xml                      # packaging=pom
```
