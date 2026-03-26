# 微调后目标架构目录结构定义

## 与 engine-reconstruction 标准结构的差异

```diff
 {module}-module/
 ├── pom.xml
 ├── grp-common-{module}/                  # 公共层（不变）
 ├── grp-capability-{module}/              # 能力层容器
 │   ├── pom.xml
 │   ├── grp-{module}-api/                 # API 定义（不变）
-│   ├── {module}-server/                  # [engine-reconstruction 标准] Controller 层
+│   ├── {module}-controller/              # [微调后] Controller 层
-│   └── {module}-server-com/              # [engine-reconstruction 标准] Service 层
+│   └── {module}-service/                 # [微调后] Service 层
 ├── grp-aggregation-{module}/             # 聚合层容器（不变）
 │   ├── pom.xml
 │   └── {module}-server-springcloud/      # SC 适配（不变）
 └── grp-experience-{module}/              # 体验层容器（不变）
     ├── pom.xml
     └── {module}-feign-com/               # Feign SDK（不变）
```

## 微调后完整目标结构

```
{project-root}/
├── pom.xml                                    # 根POM (packaging=pom)
├── grp-common-boot/                           # 底座层容器（不变）
│   ├── pom.xml
│   ├── grp-logger-com/
│   ├── grp-exception-com/
│   ├── grp-util-com/
│   └── grp-database-com/
├── {module}-module/                           # 业务模块容器
│   ├── pom.xml                                # packaging=pom
│   ├── grp-common-{module}/                   # 公共层（可选预留）
│   ├── grp-capability-{module}/               # 能力层容器
│   │   ├── pom.xml                            # packaging=pom
│   │   ├── grp-{module}-api/                  # API 定义（不变）
│   │   │   └── pom.xml
│   │   ├── {module}-controller/               # ★ Controller 层（原 {module}-server）
│   │   │   └── pom.xml
│   │   └── {module}-service/                  # ★ Service 层（原 {module}-server-com）
│   │       └── pom.xml
│   ├── grp-aggregation-{module}/              # 聚合层容器
│   │   ├── pom.xml                            # packaging=pom
│   │   └── {module}-server-springcloud/       # SC 适配（不变）
│   │       └── pom.xml                        # packaging=jar
│   └── grp-experience-{module}/               # 体验层容器（可选）
│       ├── pom.xml                            # packaging=pom
│       └── {module}-feign-com/                # Feign SDK（不变）
│           └── pom.xml
```

## 依赖方向（微调后）

```
grp-experience-{module}/
  └── {module}-feign-com  ──依赖──→  grp-{module}-api

grp-aggregation-{module}/
  └── {module}-server-springcloud  ──依赖──→  {module}-controller (★微调后名称)

grp-capability-{module}/
  ├── {module}-controller (★)  ──依赖──→  {module}-service (★)
  ├── {module}-controller (★)  ──依赖──→  grp-{module}-api
  ├── {module}-service (★)     ──依赖──→  grp-{module}-api
  └── {module}-service (★)     ──依赖──→  底座层模块 (grp-util-com, grp-database-com 等)
```
