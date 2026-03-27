# S4 Controller 接口分离检查报告输出示例

以下为检查完成后的标准输出格式：

```
# S4 Controller custom/common 接口分离检查报告

## 检查概览
- 检查路径：{path}
- FAIL 项：{fail_count}
- WARN 项：{warn_count}
- INFO 项：{info_count}
- PASS 项：{pass_count}

## 详细结果

### S4-01 custom/common 目录存在性
| 检查项 | 状态 | 说明 |
|--------|------|------|
| controller/custom/ 目录 | FAIL | 不存在，需创建 |
| controller/common/ 目录 | FAIL | 不存在，需创建 |

### S4-02 Controller 归属检查
| Controller 类 | 当前位置 | 分类级别 | 命中规则 | 目标位置 | 状态 |
|--------------|---------|---------|---------|---------|------|
| ElementController | config.basedata.controller | L3 | 默认 custom, businessGroup=basedata | controller.custom.basedata | FAIL |
| BooksetController | config.bookset.controller | L3 | 默认 custom, businessGroup=bookset | controller.custom.bookset | FAIL |
| SsoController | config.sso.controller | L2 | L2-P1: className 包含 Sso → sso | controller.common.sso | FAIL |
| ServerInfoController | config.monitoringcenter.controller | L2 | L2-P3: className 包含 ServerInfo → monitor | controller.common.monitor | FAIL |
| CacheController | controller.custom.cache | L3 | 默认 custom, businessGroup=cache | controller.custom.cache | PASS |

### S4-03 二级业务分组容量
| 目录 | 文件数 | 状态 | 说明 |
|------|--------|------|------|
| custom/basedata/ | 5 | PASS | 文件数合理 |
| common/sso/ | 3 | PASS | 文件数合理 |

### S4-04 非 controller 包下的 Controller
| Controller 类 | 当前位置 | 状态 |
|--------------|---------|------|
| (无) | — | PASS |

### S4-05 controller 包下的非 Controller 类
| 类名 | 当前位置 | 状态 | 说明 |
|------|---------|------|------|
| GapModuleOperLog | controller.custom.gap | INFO | 非 Controller 类，不迁移，保留原位 |

## 修复建议
1. [FAIL] 创建 controller/custom/ 和 controller/common/ 目录
2. [FAIL] 将 ElementController 从 config.basedata.controller 迁入 controller.custom.basedata（L3: 默认 custom）
3. [FAIL] 将 SsoController 从 config.sso.controller 迁入 controller.common.sso（L2: className 包含 Sso）
4. [INFO] GapModuleOperLog 不是 Controller 类，不迁移
```

---

## 分区扫描计数输出示例

以下为修复流程 Phase 1 步骤 1.1.1~1.1.4 的分区扫描标准输出格式：

```
【步骤 1.1.1: config/ 扫描结果】
| config/ 子目录 | Controller 数量 | 文件列表 |
|---------------|----------------|---------|
| config/user/controller/ | 17 | ACFTUserController, AgencyUserController, ApiUserConfigController, ... |
| config/servermodule/controller/ | 6 | ModuleController, ModuleFileController, ... |
| config/dataright/controller/ | 4 | RightModelController, RoleButtonRightController, ... |
| config/ssoplatform/ | 3 | SsoConfigController, SsoConfigLogController, SsoStandardFieldsController |
| config/icontroller/ | 0 | （无 @RestController/@Controller 注解，门控过滤，不计入） |
| config/portal/controller/ | 3 | PortalController, PortletController, UserPortletController |
| config/role/controller/ | 3 | RoleController, RoleGroupController, RoleMoveController |
| ... | ... | ... |

config/ 扫描完成：共发现 68 个 Controller，分布在 31 个子目录中

【步骤 1.1.2: config2/ 扫描结果】
| config2/ 子目录 | Controller 数量 | 文件列表 |
|----------------|----------------|---------|
| config2/admin/controller/ | 3 | AdminMenuController, AdminRoleController, AdminUserController |
| config2/agent/controller/ | 1 | AgentRightController |
| config2/userrole/controller/ | 3 | RoleManageController, RoleModuleManageController, UserRoleManageController |
| ... | ... | ... |

config2/ 扫描完成：共发现 20 个 Controller，分布在 16 个子目录中

【步骤 1.1.3: controller/ 及其他目录扫描结果】
controller/ 及其他目录扫描完成：共发现 0 个 Controller

【步骤 1.1.4: 汇总与交叉验证】
| 源目录              | Controller 数量 |
|--------------------|----------------|
| config/            | 68             |
| config2/           | 20             |
| controller/及其他   | 0              |
| 全量验证            | 88             |

分区总计: 68 + 20 + 0 = 88
全量校验: 88 = 88 ✅
```
