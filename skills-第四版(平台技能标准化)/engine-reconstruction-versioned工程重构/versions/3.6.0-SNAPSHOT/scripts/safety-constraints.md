# 重构安全约束（红线）

执行重构前必须遵守以下约束，**任何一条违反都必须立即停止并报告**：

| 编号 | 约束 | 说明 |
|------|------|------|
| S-01 | **禁止修改 Java 方法体** | 不得修改任何方法的实现逻辑、变量、算法 |
| S-02 | **禁止修改 Java 类结构** | 不得增删字段、方法、注解（import/package 除外） |
| S-03 | **禁止修改 SQL/XML 映射** | MyBatis XML、SQL 文件内容不得改动 |
| S-04 | **禁止修改前端代码** | JS/CSS/HTML/Vue 等前端文件不得改动 |
| S-05 | **禁止修改 resources 资源** | 配置模板、i18n、静态资源等不得改动（yaml/properties 除外） |
| S-06 | **禁止丢失任何文件** | 移动操作必须确保文件完整性：先复制到目标位置并验证完整后，再删除旧源目录。不得跳过验证直接删除 |
| S-07 | **必须备份** | 重构前必须确认用户已备份，或提醒用户备份 |
| S-08 | **移动后必须清理旧目录** | 模块成功移动到目标位置后，必须删除根目录下残留的旧源目录，避免新旧目录并存造成混淆 |

## 允许修改的范围

- `pom.xml`: parent、modules、artifactId、groupId、version、dependencies、relativePath、packaging、dependencyManagement、repositories、properties
- `*.yml` / `*.yaml` / `*.properties`: spring.application.name、server.port、路径引用
- `*.java`: 仅 `package` 声明和 `import` 语句
