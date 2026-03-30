# 代码违规模式与修复前后对比示例 - 3.6.1-SNAPSHOT

> 与 3.6.0-SNAPSHOT 基线版本完全一致。完整示例请参见 `versions/3.6.0-SNAPSHOT/examples/violation-patterns.md`。
>
> **执行修复时，必须读取基线版本的完整示例**，了解每种违规模式的修复前后对比。

## 一、Controller→Controller 依赖

违规代码 → 修复为 Service 接口注入（策略A）或提取 Private Helper（策略B）。
完整示例 → 基线版 violation-patterns.md。

## 二、Controller 直接依赖 DAO/Mapper

违规代码 → 补建 DelegateService 接口+实现类，Controller 改为注入 DelegateService。
完整示例 → 基线版 violation-patterns.md。

## 三、Controller 注入 ServiceImpl 而非接口

违规代码 → 字段类型改为 Service 接口，import 改为接口路径。
完整示例 → 基线版 violation-patterns.md。

## 四、Entity 泄露到 Controller 层

违规代码 → 返回值改为 VO，参数改为 DTO/Query。
完整示例 → 基线版 violation-patterns.md。
