# S3 Service 层治理检查报告输出示例

以下为检查完成后的标准输出格式：

```
# S3 Service 层治理检查报告

## 检查概览
- 检查路径：{path}
- FAIL 项：{fail_count}
- WARN 项：{warn_count}
- PASS 项：{pass_count}

## 详细结果

### S3-02 facade/ 目录存在性
| 检查项 | 状态 | 说明 |
|--------|------|------|
| service/facade/ 目录 | FAIL | 不存在，需创建 |
| facade/ 包含所有接口 | FAIL | 接口散落在业务子目录中 |

### S3-03 Service 接口归属
| Service 接口 | 当前位置 | 正确位置 | 状态 |
|-------------|---------|---------|------|
| IElementService | service/basedata/ | service/facade/ | FAIL |
| IBooksetService | service/bookset/ | service/facade/ | FAIL |
| IUserService | service/ | service/facade/ | FAIL |

### S3-04 Service 实现归属
| Service 实现 | 当前位置 | 正确位置 | 状态 |
|-------------|---------|---------|------|
| ElementServiceImpl | service/basedata/impl/ | service/impl/ | FAIL |
| BooksetServiceImpl | service/bookset/impl/ | service/impl/ | FAIL |
| UserServiceImpl | service/impl/ | service/impl/ | PASS |

### S3-05 非 Service 文件
| 文件 | 当前位置 | 建议处理 | 状态 |
|------|---------|---------|------|
| ElementConstant | service/basedata/constant/ | 保留原位 | WARN |
| ElementEnum | service/basedata/enums/ | 保留原位 | WARN |

## 修复建议
1. [FAIL] 创建 service/facade/ 目录，将所有 Service 接口迁入
2. [FAIL] 将 ElementServiceImpl 从 service/basedata/impl/ 迁入 service/impl/
3. [FAIL] 将 BooksetServiceImpl 从 service/bookset/impl/ 迁入 service/impl/
4. [WARN] 非 Service 文件保留原位，后续可评估迁入公共模块
```
