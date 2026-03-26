# 接口设计确定性规范 - 3.7.0-SNAPSHOT

> 接口设计规范在所有版本中保持一致。

与 3.6.0-SNAPSHOT 基线版本一致，详见 [3.6.0-SNAPSHOT/scripts/interface-design-rules.md](../../3.6.0-SNAPSHOT/scripts/interface-design-rules.md)。

核心要点：
- **直接代理原则**：Service 接口仅作为调用中转层，不重新设计业务接口
- **D-01** 方法签名一致（方法名、参数、返回类型、throws 完全一致）
- **D-02** 禁止 SQL 逻辑移动
- **D-03** 禁止方法合并/拆分
- **D-04** 接口命名规范（IXxxService）
- **D-05** 方法注释要求（JavaDoc 必填）
- **D-06** import 最小化
- **D-07** 已有接口复用
