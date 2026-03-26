# 重构计划确认示例

重构计划生成后，需向用户展示以下信息并获取确认：

```
## 重构计划摘要

### 工程信息
- 工程版本: 3.6.0-SNAPSHOT
- groupId: grp.pt
- 根POM模式: standalone (spring-boot-starter-parent)

### 模块映射
[展示模块映射表]

### 目标目录结构
[展示目标目录树]

### POM 更新计划
- 新建根POM: spring-boot-starter-parent + dependencyManagement + repositories
- 新建容器POM: X 个
- 更新叶子POM: X 个

### Java 文件影响
- 需要更新 package/import 的文件: X 个（或0个）

### 确认选项
1. 确认执行
2. 仅重构指定模块
3. 暂不执行
```
