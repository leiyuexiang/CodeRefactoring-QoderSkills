# 修复完整性校验清单 - 3.6.1-SNAPSHOT

> 完整性校验在所有版本中保持一致。

与 3.6.0-SNAPSHOT 基线版本一致，详见 [3.6.0-SNAPSHOT/scripts/completeness-check.md](../../3.6.0-SNAPSHOT/scripts/completeness-check.md)。

核心校验项：
- **V-01** Controller 层 ServiceImpl 残留搜索
- **V-02** Controller 层 DAO 残留搜索
- **V-03** 接口方法完整性校验
- **V-04** 编译验证（mvn compile）
- **V-05** 全工程打包验证（mvn package -DskipTests）
- **V-06** AI 标记完整性（@AI-Begin 与 @AI-End 成对）
