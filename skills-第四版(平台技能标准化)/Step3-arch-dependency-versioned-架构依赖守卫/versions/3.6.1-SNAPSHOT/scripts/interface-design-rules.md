# 接口设计确定性规范 - 3.6.1-SNAPSHOT

> 接口设计规范在所有版本中保持一致。

与 3.6.0-SNAPSHOT 基线版本一致，详见 [3.6.0-SNAPSHOT/scripts/interface-design-rules.md](../../3.6.0-SNAPSHOT/scripts/interface-design-rules.md)。

核心要点：
- **直接代理原则**：Service 接口仅作为调用中转层，不重新设计业务接口
- **D-01** 方法签名一致（方法名、参数、返回类型、throws 完全一致）+ 多 DAO 同名方法冲突时强制加前缀
- **D-02** 禁止 SQL 逻辑移动
- **D-03** 禁止方法合并/拆分
- **D-04** 接口命名规范（IXxxService）
- **D-04-A** 多 DAO 合并策略（强制合并到同一 DelegateService）
- **D-04-B** 跨 Controller 独立创建（禁止跨 Controller 复用 DelegateService）
- **D-05** 方法注释要求（JavaDoc 必填，含带 DAO 前缀的冲突方法注释模板）
- **D-06** import 最小化
- **D-07** 已有接口复用
