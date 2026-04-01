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
- 信息项（INFO）：{info_count}

## 独立业务域模块豁免
| 模块路径 | 包含结构 | 状态 |
|---------|---------|------|
| view/ | entity/ + mapper/ + service/ | INFO（已豁免） |

## 详细结果

### S2-01 目录命名规范
| 目录路径 | 当前名称 | 标准名称 | 状态 |
|---------|---------|---------|------|
| service/imp/ | imp | impl | FAIL |
| dao/imp/ | imp | impl | FAIL |
| service/serviceImp/ | serviceImp（合成词变体） | impl | FAIL |

### S2-02 DAO 层接口/实现分离
| 文件名 | 当前位置 | 标准位置 | 分类规则 | 状态 |
|--------|---------|---------|---------|------|
| XxxDaoImpl.java | dao/ | dao/impl/ | 优先级4：Impl后缀 | FAIL |
| BpmDao.java | dao/ | dao/impl/ | 优先级8：兜底推定 | FAIL |
| YearDao.java | dao/ | dao/impl/ | 优先级8：兜底推定 | FAIL |
| IXxxDao.java | dao/ | dao/ | 优先级7：I+大写接口 | PASS |

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
| YyyMapper.java | grp.pt.mapper（dao/mapper/ 已存在同名文件） | 冗余副本 | FAIL |

### S2-07 Model 层 dto/vo/query/po 分类
| 文件名 | 当前位置 | 标准位置 | 匹配规则 | 状态 |
|--------|---------|---------|---------|------|
| XxxEntity.java | model/ | dao/entity/ | 优先级4：Entity后缀 | FAIL |
| UserDTO.java | model/ | model/dto/ | 优先级1：DTO后缀 | FAIL |
| SysLogInfoDto.java | model/ | model/dto/ | 优先级1：Dto后缀 | FAIL |
| UserVO.java | model/ | model/vo/ | 优先级2：VO后缀 | FAIL |
| ElementBo.java | model/ | model/vo/ | 优先级2：Bo后缀 | FAIL |
| UiViewBO.java | model/ | model/vo/ | 优先级2：BO后缀 | FAIL |
| UserQuery.java | model/ | model/query/ | 优先级3：Query后缀 | FAIL |
| Module.java | model/ | model/po/ | 优先级6：无后缀兜底 | FAIL |
| Tenant.java | model/ | model/po/ | 优先级6：无后缀兜底 | FAIL |
| SysConfig.java | model/ | model/po/ | 优先级6：无后缀兜底 | FAIL |
| ErrorLoggerInfo.java | model/ | model/po/ | 优先级6：无后缀兜底 | FAIL |

### S2-08 公共模块结构
| 目录 | 状态 | 说明 |
|------|------|------|
| config/ | 存在 | PASS |
| util/ | 缺失 | WARN |

## 修复建议
1. [FAIL] service/imp/ 目录应重命名为 service/impl/，涉及 {N} 个文件的 package/import 更新
2. [FAIL] service/serviceImp/ 合成词变体应重命名为 impl/，涉及 {N} 个文件的 package/import 更新
3. [FAIL] XxxDaoImpl.java 应从 dao/ 移入 dao/impl/（Impl后缀匹配）
4. [FAIL] BpmDao.java 应从 dao/ 移入 dao/impl/（兜底推定为实现类）
5. [FAIL] UserDTO.java 应从 model/ 移入 model/dto/
6. [FAIL] ElementBo.java 应从 model/ 移入 model/vo/（Bo后缀匹配优先级2）
7. [FAIL] XxxMapper.java 应从独立包移入 dao/mapper/
8. [FAIL] YyyMapper.java 为冗余副本（dao/mapper/ 已存在），应删除独立包中的副本
9. [FAIL] Module.java、Tenant.java、SysConfig.java 无标准后缀，按兜底规则移入 model/po/
10. [WARN] element-server 模块缺少 model/ 目录，建议创建
```
