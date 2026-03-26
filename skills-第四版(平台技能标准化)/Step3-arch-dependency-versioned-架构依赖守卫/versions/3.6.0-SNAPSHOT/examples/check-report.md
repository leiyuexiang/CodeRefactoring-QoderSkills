# S1 依赖检查报告输出示例

以下为检查完成后的标准输出格式：

```
# S1 架构依赖违规检查报告

## 检查概览
- 检查路径：{path}
- 检查 Controller 数：{count}
- 严重违规（FAIL）：{fail_count}
- 警告（WARN）：{warn_count}
- 通过（PASS）：{pass_count}

## 详细结果

### S1-01 Controller→Controller 依赖
| Controller 类 | 被依赖 Controller | 调用方法 | 状态 |
|--------------|-------------------|---------|------|
| AController  | BController       | doSomething() | FAIL |

### S1-02 Controller→DAO/Mapper 直接依赖
| Controller 类 | 被依赖 DAO/Mapper | 状态 |
|--------------|-------------------|------|
| XxxController | XxxDao           | FAIL |

### S1-03 Controller→ServiceImpl 注入
| Controller 类 | 注入的 ServiceImpl | 应改为 | 状态 |
|--------------|-------------------|--------|------|
| XxxController | XxxServiceImpl   | IXxxService | FAIL |

### S1-04 Entity 泄露
| Controller 类 | 方法名 | Entity 类 | 使用方式（参数/返回值） | 状态 |
|--------------|--------|----------|---------------------|------|
| XxxController | getDetail() | XxxEntity | 返回值 | WARN |

### S1-05 跨模块直接类引用
| Controller 类 | 引用的外部类 | 所属模块 | 状态 |
|--------------|-------------|---------|------|
| AController  | BModuleService | b-module | WARN |

## 修复建议
1. [FAIL] AController 注入了 BController，应改为通过 Service 层调用
2. [FAIL] XxxController 直接注入 XxxDao，应补建 IXxxService 中间层
3. [FAIL] XxxController 注入 XxxServiceImpl，应改为注入 IXxxService 接口
4. [WARN] XxxController.getDetail() 返回 XxxEntity，建议使用 DTO/VO 替代
```
