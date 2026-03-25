# 微调计划输出示例

以 `element` 模块为例：

---

## 工程架构微调计划

### 工程信息

- **工程根目录**: `/path/to/project`
- **groupId**: `grp.pt`
- **version**: `3.6.0-SNAPSHOT`

### 检测到的待微调模块

| 业务模块 | 原模块名 | 新模块名 | 当前状态 |
|----------|---------|---------|---------|
| element | `element-server` | `element-controller` | 待重命名 |
| element | `element-server-com` | `element-service` | 待重命名 |

### 影响范围

#### 1. 目录重命名（2 个）

```
element-module/grp-capability-element/element-server-com/  →  element-module/grp-capability-element/element-service/
element-module/grp-capability-element/element-server/      →  element-module/grp-capability-element/element-controller/
```

#### 2. POM 文件更新（预计 5 个文件）

| POM 文件 | 更新内容 |
|----------|---------|
| `element-controller/pom.xml` | artifactId: `element-server` → `element-controller` |
| `element-service/pom.xml` | artifactId: `element-server-com` → `element-service` |
| `grp-capability-element/pom.xml` | modules 声明更新 |
| 根 `pom.xml` | dependencyManagement 中的 artifactId 更新 |
| `element-server-springcloud/pom.xml` | dependency: `element-server` → `element-controller` |
| `element-controller/pom.xml` | dependency: `element-server-com` → `element-service` |

#### 3. Java 文件更新

| 目录范围 | 替换规则 |
|----------|---------|
| `element-service/src/` | `package *.element.server.com.*` → `package *.element.service.*` |
| `element-controller/src/` | `package *.element.server.*` → `package *.element.controller.*` |
| 全工程 `*.java` | `import *.element.server.com.*` → `import *.element.service.*` |
| 全工程 `*.java` | `import *.element.server.*` → `import *.element.controller.*`（排除 springcloud 包） |

---

**是否确认执行以上微调操作？（Y/N）**
