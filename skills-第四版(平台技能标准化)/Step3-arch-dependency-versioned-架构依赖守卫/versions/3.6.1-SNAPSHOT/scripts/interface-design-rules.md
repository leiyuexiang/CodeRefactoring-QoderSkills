# 接口设计确定性规范 - 3.6.1-SNAPSHOT

> **本文件为摘要版本。完整规则必须从基线版本读取：`versions/3.6.0-SNAPSHOT/scripts/interface-design-rules.md`**
>
> 基线版本包含 D-01 ~ D-11 共 11 条确定性规则，本版本与基线完全一致。

## 核心原则：直接代理

新建 Service 接口的唯一目的是**解除 Controller 对 DAO/Impl 的直接依赖**，不是重新设计业务接口。

## 规则清单（完整定义在基线版本中）

| 规则编号 | 规则名称 | 核心要点 |
|---------|---------|---------|
| D-01 | 方法签名一致 | 方法名、参数、返回类型、throws 完全一致 + 多DAO同名方法冲突时强制加前缀 |
| D-02 | 禁止 SQL 逻辑移动 | SQL 留在 Controller，Service 纯转发 |
| D-03 | 禁止方法合并/拆分/迁移 | 一个 DAO 方法对应一个 Service 接口方法 |
| D-04 | 接口命名规范 | XxxDao→IXxxDelegateService, XxxServiceImpl→IXxxService |
| D-04-A | 多 DAO 合并策略 | 一个 Controller 对应一个 DelegateService（强制合并） |
| D-04-B | 跨 Controller 复用策略 | 每个 Controller 独立创建（禁止复用） |
| D-05 | 方法注释要求 | 中文 JavaDoc，按 D-05-C 标准化模板生成 |
| D-06 | import 最小化 | 仅导入方法签名中引用的类型 |
| D-07 | 已有接口复用 | 仅适用于 S1-03，S1-02 必须新建 |
| D-08 | 新建文件 Maven 模块位置 | 必须放在 Service 层模块（{module}-server-com / {module}-service） |
| D-09 | 替换后字段命名确定性 | S1-01/S1-02: 接口名去I后首字母小写; S1-03: 字段名不变 |
| D-10 | Controller 业务名提取算法 | 去掉末尾 Controller 后缀，保持原始大小写 |
| D-11 | S1-03 已有接口查找路径 | 先查 implements 声明 → 再查同包 service 目录 → 全局搜索 → 判定不存在 |
