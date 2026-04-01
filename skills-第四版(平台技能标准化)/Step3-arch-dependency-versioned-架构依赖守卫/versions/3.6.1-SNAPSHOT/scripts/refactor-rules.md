# S1 修复规范 - 3.6.1-SNAPSHOT

> **本文件为摘要版本。完整规则必须从基线版本读取：`versions/3.6.0-SNAPSHOT/scripts/refactor-rules.md`**
>
> **强制要求**：执行修复前，必须先读取 `versions/3.6.0-SNAPSHOT/scripts/refactor-rules.md` 获取以下完整内容：
> - 修复规范一~四的完整决策树和修复步骤
> - 修复顺序确定性规则（S1-01 → S1-02 → S1-03 固定顺序）
> - FCC（强制原样复制）指令
> - 确定性检查清单
> - import 语句排序确定性规则
> - 方法调用对象替换全场景规则
> - AI 标记一致性规范（Tag 粒度规则、标记范围规则）
> - 接口设计规范引用

## 修复规范摘要（完整版在基线文件中）

### 修复规范一：消除 Controller→Controller 依赖

与 3.6.0-SNAPSHOT 一致（含修复决策树、策略A/B、移除代码注释规范）。

### 修复规范二：Controller→DAO/Mapper 补建 Service 中间层

与 3.6.0-SNAPSHOT 一致（含 DAO 调用逻辑迁移边界、包路径确定性规则、跨Controller独立创建规则、确定性检查清单、FCC指令）。

### 修复规范三：Controller 注入 ServiceImpl → Service 接口

与 3.6.0-SNAPSHOT 一致（含 S1-03 修复范围确定性规则、接口方法提取范围规则）。

### 修复规范四：Entity 泄露修复

与 3.6.0-SNAPSHOT 一致。

### 修复顺序确定性规则

与 3.6.0-SNAPSHOT 一致。固定顺序：S1-01 → S1-02 → S1-03。跨Controller按类名字母表升序处理。

### import 语句排序确定性规则

与 3.6.0-SNAPSHOT 一致。固定分组顺序：java.* → javax.* → org.springframework.* → 项目内部包 → 其他第三方包。

### 方法调用对象替换全场景规则

与 3.6.0-SNAPSHOT 一致。替换完成后必须 Grep 验证旧字段名无残留。

### AI 标记一致性规范

与 3.6.0-SNAPSHOT 一致（Tag 粒度规则、标记范围规则、标记位置示例）。

### 接口设计规范引用

与 3.6.0-SNAPSHOT 一致，详见基线版 `versions/3.6.0-SNAPSHOT/scripts/interface-design-rules.md`（D-01~D-11）。
