// Service 接口模板 - 3.7.0-SNAPSHOT
// 使用说明：补建 Service 中间层时，按此模板创建 Service 接口
// 将 {ServiceName}、{ReturnType}、{MethodName}、{ParamType}、{paramName} 替换为实际值
//
// ===== 设计原则：直接代理 =====
// 新建的 Service 接口仅作为 ServiceImpl 的调用转发层（Delegation Layer）。
// 接口方法必须与 ServiceImpl 中已有的 public 方法签名完全一致。
// 禁止：新增方法、合并方法、拆分方法、移动 SQL 逻辑。
// 详细规则 → scripts/interface-design-rules.md
//
// ===== 方法提取流程 =====
// 1. 读取对应 ServiceImpl 类的全部 public 方法
// 2. 逐个复制方法签名（返回类型 + 方法名 + 参数列表 + throws）
// 3. 为每个方法添加 JavaDoc（从 Impl 复制或根据方法名生成）
// 4. 确保 import 列表仅包含接口方法签名中引用的类型

package {basePackage}.service;

import {需要的类型};

/**
 * {ServiceName} 服务接口
 * 提供 {功能简述}
 */
public interface I{ServiceName}Service {

    /**
     * {D-05-C注释模板对应的中文功能说明}
     */
    {ReturnType} {methodName}({ParamType} {paramName}) throws {ExceptionType};
}
