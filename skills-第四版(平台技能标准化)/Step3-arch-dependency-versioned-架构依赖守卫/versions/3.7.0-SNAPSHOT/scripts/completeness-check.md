# 修复完整性校验清单 - 3.7.0-SNAPSHOT

> **本文件为摘要版本。完整规则必须从基线版本读取：`versions/3.6.0-SNAPSHOT/scripts/completeness-check.md`**

## 校验项清单

| 编号 | 校验项 | 执行时机 | 通过标准 |
|------|--------|---------|---------|
| V-01 | Controller 层 ServiceImpl 残留搜索 | 每批修复后 | `import.*service\.impl\.` 搜索结果为 0 |
| V-02 | Controller 层 DAO 残留搜索 | 每批修复后 | `import.*\.dao\.` 搜索结果为 0 |
| V-03 | 接口方法完整性校验 | **每完成 3~5 个 FAIL 项后必须执行** | Controller 调用的所有方法在接口中均有声明 |
| V-04 | 编译验证 | **每完成 3~5 个 FAIL 项后必须执行** | `mvn compile` 输出 BUILD SUCCESS |
| V-05 | 全工程打包验证 | 全部修复完毕后 | `mvn package -DskipTests` 全部 SUCCESS |
| V-06 | AI 标记完整性 | 全部修复完毕后 | `@AI-Begin` 数量 == `@AI-End` 数量 |

> **强制规定**：每完成 3~5 个 FAIL 项修复后，**必须立即执行 V-03 + V-04** 编译验证，不得等到全部修复完成后才编译。
