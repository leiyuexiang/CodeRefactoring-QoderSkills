# S4 Controller 接口分离检查报告输出示例

以下为检查完成后的标准输出格式：

```
# S4 Controller custom/common 接口分离检查报告

## 检查概览
- 检查路径：{path}
- FAIL 项：{fail_count}
- WARN 项：{warn_count}
- PASS 项：{pass_count}

## 详细结果

### S4-01 custom/common 目录存在性
| 检查项 | 状态 | 说明 |
|--------|------|------|
| controller/custom/ 目录 | FAIL | 不存在，需创建 |
| controller/common/ 目录 | FAIL | 不存在，需创建 |

### S4-02 Controller 归属检查
| Controller 类 | 当前位置 | 接口类型(外部/内部) | 正确位置 | 状态 |
|--------------|---------|-------------------|---------|------|
| ElementController | controller/basedata/ | 外部(run/) | controller/custom/basedata/ | FAIL |
| BooksetController | controller/bookset/ | 外部(run/) | controller/custom/bookset/ | FAIL |
| CacheUtilController | controller/util/ | 内部(config/) | controller/common/util/ | FAIL |
| NotifyController | controller/ | 内部 | controller/common/notify/ | FAIL |

### S4-03 二级业务分组
| 目录 | 文件数 | 状态 | 说明 |
|------|--------|------|------|
| custom/basedata/ | 5 | PASS | 文件数合理 |
| common/api/ | 12 | WARN | 文件超过10个，建议进一步细分 |

### S4-04 非 controller 包下的 Controller
| Controller 类 | 当前位置 | 状态 |
|--------------|---------|------|
| (无) | — | PASS |

## 修复建议
1. [FAIL] 创建 controller/custom/ 和 controller/common/ 目录
2. [FAIL] 将 ElementController 从 controller/basedata/ 迁入 controller/custom/basedata/
3. [FAIL] 将 CacheUtilController 从 controller/util/ 迁入 controller/common/util/
4. [WARN] common/api/ 下文件超过 10 个，建议按功能进一步细分
```
