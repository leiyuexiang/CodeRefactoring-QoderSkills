# S2 DAO-Model 层检查报告输出示例

以下为检查完成后的标准输出格式：

```
# S2 DAO-Model 层治理检查报告

## 检查概览
- 检查路径：{path}
- 检查模块数：{count}
- 不通过项（FAIL）：{fail_count}
- 警告项（WARN）：{warn_count}
- 通过项（PASS）：{pass_count}

## 详细结果

### S2-01 目录命名规范
| 目录路径 | 当前名称 | 标准名称 | 状态 |
|---------|---------|---------|------|
| service/imp/ | imp | impl | FAIL |
| dao/imp/ | imp | impl | FAIL |

### S2-02 DAO 层接口/实现分离
| 文件名 | 当前位置 | 标准位置 | 状态 |
|--------|---------|---------|------|
| XxxDaoImpl.java | dao/ | dao/impl/ | FAIL |

### S2-03 DTO/VO/Query 分类归档
| 文件名 | 当前位置 | 标准位置 | 状态 |
|--------|---------|---------|------|
| UserDTO.java | model/ | model/dto/ | FAIL |
| UserVO.java | model/ | model/vo/ | FAIL |

### S2-04 核心四层目录完整性
| 模块 | controller | service | dao | model | 状态 |
|------|-----------|---------|-----|-------|------|
| element-server | 存在 | 存在 | 存在 | 缺失 | WARN |

### S2-05 resources/mapper 目录对应
| 文件名 | 当前位置 | 建议位置 | 状态 |
|--------|---------|---------|------|
| XxxMapper.xml | mapper/ | mapper/element/ | WARN |

### S2-06 DAO 层 mapper/entity 分离
| 文件名 | 当前位置 | 标准位置 | 状态 |
|--------|---------|---------|------|
| XxxMapper.java | grp.pt.mapper | dao/mapper/ | FAIL |

### S2-07 Model 层 dto/vo/query 分类
| 文件名 | 当前位置 | 标准位置 | 状态 |
|--------|---------|---------|------|
| XxxEntity.java | model/ | dao/entity/ | WARN |

### S2-08 公共模块结构
| 目录 | 状态 | 说明 |
|------|------|------|
| config/ | 存在 | PASS |
| util/ | 缺失 | WARN |

## 修复建议
1. [FAIL] service/imp/ 目录应重命名为 service/impl/，涉及 {N} 个文件的 package/import 更新
2. [FAIL] XxxDaoImpl.java 应从 dao/ 移入 dao/impl/
3. [FAIL] UserDTO.java 应从 model/ 移入 model/dto/
4. [FAIL] XxxMapper.java 应从独立包移入 dao/mapper/
5. [WARN] element-server 模块缺少 model/ 目录，建议创建
```
