# S5 接口与命名规范检查报告输出示例

以下为检查完成后的标准输出格式：

```
# S5 接口与命名规范检查报告

## 检查概览
- 检查路径：{path}
- 可修复项（FAIL/WARN）：{fixable_count}
- 约束限制项：{constrained_count}

## 命名与规范检查

### S5-01 接口路径规范
| Controller | 路径 | 问题 | 状态 |
|-----------|------|------|------|
| ElementController | /api/element | 不符合四级结构 | WARN（约束限制） |
| BooksetController | /config/bookset/query/list | 符合四级结构 | PASS |
| OrgController | @DeleteMapping | 不推荐 HTTP 方法 | WARN |

### S5-02 类命名规范
| 类名 | 问题 | 建议 | 状态 |
|------|------|------|------|
| xxxCtrl | 后缀不规范 | XxxController | WARN |
| elementService | 首字母小写 | ElementService | WARN |

### S5-03 属性命名规范
| 类名 | 属性 | 问题 | 状态 |
|------|------|------|------|
| UserDTO | user_name | 下划线命名 | WARN（约束限制） |
| OrderVO | isActive | 符合布尔前缀 | PASS |

### S5-04 接口参数规范
| Controller 方法 | 参数 | 问题 | 状态 |
|---------------|------|------|------|
| queryList | page/size | 分页参数不统一 | WARN |
| addElement | Long id | 缺少 @NotNull | WARN |

### S5-05 接口响应规范
| Controller 方法 | 返回类型 | 问题 | 状态 |
|---------------|---------|------|------|
| getElement | ElementEntity | 直接返回 Entity | WARN（约束限制） |
| queryList | ReturnPage<ElementVO> | 正确包装 | PASS |

### S5-06 Bean 命名冲突
| Bean 名称 | 冲突类 | 状态 |
|-----------|--------|------|
| elementController | ElementController, ElementController2 | WARN |

## 约束限制项（不建议修改）
| 问题类型 | 具体问题 | 约束原因 |
|---------|---------|---------|
| URL 路径结构 | /api/element 不符合四级结构 | 修改影响前端调用 |
| DTO 属性命名 | user_name 使用下划线 | 修改影响 JSON 序列化 |
| 返回类型 | 直接返回 Entity | 修改影响前端解析 |

## 修复建议
1. [建议] xxxCtrl 重命名为 XxxController
2. [建议] @DeleteMapping 添加 POST 兼容
3. [建议] 确认 ElementController2 是否为有意命名
```
