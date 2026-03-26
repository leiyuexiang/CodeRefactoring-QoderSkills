// ServiceImpl 实现类模板 - 3.7.0-SNAPSHOT
// 使用说明：补建 Service 中间层时，按此模板创建 Service 实现类
// 将 {ServiceName}、{ReturnType}、{MethodName}、{DaoName} 等替换为实际值
//
// ===== 关键规则 =====
// 1. 类必须声明 implements I{ServiceName}Service
// 2. 每个接口方法实现前必须标注 @Override（强制，不可省略）
// 3. 已有 ServiceImpl 补加 implements 时：
//    a. 仅在类声明上追加 implements I{ServiceName}Service
//    b. 已有的 public 方法前补加 @Override 注解
//    c. 不得修改方法内部逻辑

package {basePackage}.service.impl;

import {basePackage}.service.I{ServiceName}Service;
import {basePackage}.dao.{DaoName};
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * {ServiceName} 服务实现类。
 *
 * @see I{ServiceName}Service
 */
@Service
public class {ServiceName}ServiceImpl implements I{ServiceName}Service {

    @Autowired
    private {DaoName} {daoFieldName};

    /**
     * {@inheritDoc}
     */
    @Override
    public {ReturnType} {methodName}({ParamType} {paramName}) {
        // 从 Controller 中迁移过来的 DAO 调用逻辑
        return {daoFieldName}.{daoMethod}({paramName});
    }
}
