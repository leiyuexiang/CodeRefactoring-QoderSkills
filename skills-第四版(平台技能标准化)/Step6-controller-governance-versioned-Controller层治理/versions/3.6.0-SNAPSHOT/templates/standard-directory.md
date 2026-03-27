# custom/common 标准目录结构模板（集中重构模式）

## 重构策略

本技能采用**集中重构**策略：将所有 Controller 从原有的分散业务包中抽取到统一的 `controller/` 包下，按 `custom/common` 一级分类，再按业务或功能域二级分组。

**禁止**采用就地重构策略（即禁止在原业务包内部添加 controller/custom/ 子目录）。

---

## 标准目录结构

```
{modulePrefix}/controller/
├── custom/                           # 业务接口（Level 3 默认归属）
│   ├── {businessGroup1}/            # 从原包路径自动提取，全小写
│   │   ├── XxxController.java
│   │   └── YyyController.java
│   ├── {businessGroup2}/
│   │   └── ZzzController.java
│   └── ...                          # 每个原始业务模块对应一个子目录
└── common/                          # 框架级功能接口（仅 Level 1/2 命中）
    ├── api/                         # I*接口定义（Level 2 正则命中）
    │   ├── IMenuController.java
    │   ├── IModuleController.java
    │   └── ...
    ├── sso/                         # 单点登录（Level 2 关键词 Sso/ssoplatform）
    │   ├── SsoConfigController.java
    │   └── ...
    └── monitor/                     # 监控/服务器信息（Level 2 关键词 ServerInfo/monitoringcenter）
        ├── ServerInfoController.java
        └── ...
```

---

## common/ 子目录规范（封闭列表）

common/ 下**仅允许**以下 3 个子目录，不允许自行创建新的子目录：

| 子目录 | 用途 | Level 2 命中条件 |
|--------|------|----------------|
| `api/` | I*接口定义、内部 RPC 端点 | 类名匹配正则 `^I[A-Z].*Controller\d*$` |
| `sso/` | 单点登录配置、SSO 标准字段 | 类名包含 `Sso` 或包路径包含 `ssoplatform` |
| `monitor/` | 服务器信息监控、系统健康 | 类名包含 `ServerInfo`/`OracleServer` 或包路径包含 `monitoringcenter` |

**封闭性规则**：
- 如果 Level 2 需要新增关键词，其目标子目录必须映射到上述 3 个之一
- 如果确实需要新的 common 子目录类别，必须先修改本模板文件增加条目，不允许执行时临时创建

---

## custom/ 子目录规范（开放，确定性命名）

custom/ 下的子目录由 Level 3 业务分组提取公式自动确定，**不允许**手工重命名或合并。

### 禁止语义归并（强制约束）

> **此约束为安全红线 S-11，违反等同于违反 C-06 确定性保证。**

- AI 执行时**不得**基于语义理解对 businessGroup 进行任何形式的归并、简化、同义替换
- businessGroup **完全**由 `extractBusinessGroup` 公式确定性产出，详见 [classification-guide.md](classification-guide.md) Level 3 的 L3-PROHIBIT-01~03
- 即使工程中已存在名称相似的目录（如已有 `view/`），也不得将 `gapuiview` 归并到 `view/`
- 即使原始包名存在明显拼写错误（如 `eleprotalststemcontroller`），也不得做任何语义修正

### 命名规则

1. 子目录名 = 原始 package 中 `config`/`config2` 之后的第一个包段
2. 一律转为**全小写**
3. 不同原始包产生相同 businessGroup 的 Controller 合并到同一目录

### 示例

| 原始包中的业务段 | custom/ 子目录名 |
|----------------|-----------------|
| `config.user` | `custom/user/` |
| `config.menuplan` | `custom/menuplan/` |
| `config.messageRemind` | `custom/messageremind/` |
| `config.portalSystem` | `custom/portalsystem/` |
| `config.whiteList` | `custom/whitelist/` |
| `config2.admin` | `custom/admin/` |
| `config2.userrole` | `custom/userrole/` |
| `config2.monitoringcenter` | 不会出现（Level 2 已命中归入 common/monitor） |

---

## 分组容量规则

| 条件 | 级别 | 处理方式 |
|------|------|---------|
| 单个子目录下 Controller 文件数 ≤ 10 | PASS | 无需操作 |
| 单个子目录下 Controller 文件数 > 10 | WARN | **仅报告**，不自动拆分，由人工决定是否进一步细分 |

**明确规定**：本工具不执行任何子目录的自动拆分操作。超过 10 个文件的情况仅在检查报告中标注 WARN。
