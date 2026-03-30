// ServiceImpl 实现类模板 - 3.7.0-SNAPSHOT
// 注意：本模板与 3.6.0-SNAPSHOT 基线版本完全一致
//
// ===== 模板变量表（必须严格按此填充） =====
// {DaoClassName}    = 被替换的 DAO 类名（如 UserDao）
// {DaoPrefix}       = DAO 类名去掉 Dao/DAO 后缀（如 User）
// {DaoPackageBase}  = DAO 所在的业务域包（如 grp.pt.frame.config.user）
// {daoFieldName}    = DAO 字段名（如 userDao）
// 实现类名         = {DaoPrefix}DelegateServiceImpl（如 UserDelegateServiceImpl）
// 实现类包路径     = {DaoPackageBase}.service.impl
//
// ===== 关键规则 =====
// 1. 类必须声明 implements I{DaoPrefix}DelegateService
// 2. 每个接口方法实现前必须标注 @Override（强制，不可省略）
// 3. 方法体必须为纯转发：直接调用 DAO 的同名方法（FCC 指令）
// 4. 禁止在方法体中添加任何业务逻辑、类型转换、异常处理等额外代码
// 5. 已有 ServiceImpl 补加 implements 时：
//    a. 仅在类声明上追加 implements I{DaoPrefix}DelegateService
//    b. 已有的 public 方法前补加 @Override 注解
//    c. 不得修改方法内部逻辑

package {DaoPackageBase}.service.impl;

import {DaoPackageBase}.service.I{DaoPrefix}DelegateService;
import {DaoPackageBase}.dao.{DaoClassName};
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {DaoPrefix} 代理服务实现类
 * 合并 {DaoClassName} 的调用，为 Controller 层提供数据访问，修复 S1-02 违规
 */
@Service
public class {DaoPrefix}DelegateServiceImpl implements I{DaoPrefix}DelegateService {

    @Autowired
    private {DaoClassName} {daoFieldName};

    @Override
    public {ReturnType} {methodName}({ParamType} {paramName}) {
        return {daoFieldName}.{methodName}({paramName});
    }
}
