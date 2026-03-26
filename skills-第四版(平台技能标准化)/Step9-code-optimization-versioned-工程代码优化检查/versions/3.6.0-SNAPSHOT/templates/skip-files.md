# 已知超大文件清单（预期跳过）

以下文件超过 1000 行，应自动标记为 SKIP。实际处理时以文件真实行数为准。

| 文件 | 预估行数 | 所属模块 |
|------|----------|---------|
| ElementValueServiceImpl.java | ~4914 | service/basedata/impl/ |
| ElementValueAgencyDaoImpl.java | ~4401 | dao/basedata/impl/ |
| ElementValueAgencyServiceImpl.java | ~2661 | service/basedata/impl/ |
| ElementValueDaoImpl.java | ~2962 | dao/basedata/impl/ |
| ElementValueSpecialServiceImpl.java | ~2410 | service/basedata/impl/ |
| ElementServiceImpl.java | ~1304 | service/basedata/impl/ |
| BasMofDivServiceImpl.java | ~1171 | service/basedata/impl/ |
| FieldServiceImpl.java | ~1045 | service/basedata/impl/ |

## 处理策略

- Step 1 扫描时，统计真实行数
- 超过 1000 行的文件记入跳过清单
- 跳过清单在最终变更报告中完整列出
- 未来可考虑按方法粒度单独处理超大文件（当前版本不支持）
