// Service 接口模板 - 3.6.1-SNAPSHOT
// 注意：本模板与 3.6.0-SNAPSHOT 基线版本完全一致
//
// ===== 模板变量表（必须严格按此填充） =====
// {DaoClassName}    = 被替换的 DAO 类名（如 UserDao）
// {DaoPrefix}       = DAO 类名去掉 Dao/DAO 后缀（如 User）
// {DaoPackageBase}  = DAO 所在的业务域包（如 grp.pt.frame.config.user）
// 接口名           = I{DaoPrefix}DelegateService（如 IUserDelegateService）
// 实现类名         = {DaoPrefix}DelegateServiceImpl（如 UserDelegateServiceImpl）
// 接口包路径       = {DaoPackageBase}.service（如 grp.pt.frame.config.user.service）
//
// ===== 设计原则：直接代理 =====
// 新建的 Service 接口仅作为 DAO 的调用转发层（Delegation Layer）。
// 接口方法必须与 DAO 中被 Controller 实际调用的 public 方法签名完全一致（FCC 指令）。
// 禁止：改名、改返回类型、改参数类型、合并方法、拆分方法、移动 SQL 逻辑。
// 详细规则 → 基线版 versions/3.6.0-SNAPSHOT/scripts/interface-design-rules.md
//
// ===== 方法提取流程（不可跳过） =====
// 1. 先读取 DAO 源码，提取所有 public 方法签名
// 2. 在 Controller 中 Grep 搜索 `{fieldName}.` 获取所有实际调用的方法名
// 3. 取交集：仅将 Controller 实际调用的方法的签名原样复制到接口（遵循 S-10 最小化原则）
// 4. 为每个方法添加中文 JavaDoc 注释（按 D-05-C 标准化模板）
// 5. 确保 import 列表仅包含接口方法签名中引用的类型（D-06）

package {DaoPackageBase}.service;

import {仅导入方法签名中引用的类型};

/**
 * {DaoPrefix} 代理服务接口
 * 为 Controller 层提供 {DaoClassName} 的转发访问，修复 S1-02 违规
 */
public interface I{DaoPrefix}DelegateService {

    /**
     * {D-05-C注释模板对应的中文功能说明}
     */
    {ReturnType} {methodName}({ParamType} {paramName});
}
