# Step8 公共模块提取 - 检查报告示例

## Step8 公共模块提取检查报告

### 检查概要

| 项目 | 值 |
|------|------|
| 检查时间 | 2026-03-25 |
| 工程版本 | 3.6.0-SNAPSHOT |
| 来源模块 | grp-capability-element/element-service |
| 目标模块 | grp-common-element |

### 检查结果汇总

| 包名 | 文件数 | 推荐提取 | 需人工判断 | 建议保留 |
|------|--------|---------|-----------|---------|
| util/ | 21 | 15 | 4 | 2 |
| cache/ | 7 | 3 | 2 | 2 |
| constant/ | 1 | 1 | 0 | 0 |
| enums/ | 1 | 1 | 0 | 0 |
| exception/ | 1 | 1 | 0 | 0 |
| config/ | 3 | 1 | 1 | 1 |
| **合计** | **34** | **22** | **7** | **5** |

---

### S8-01: util/ 包检查详情

#### 推荐提取

| 文件 | 引用数 | 依赖分析 | 建议 |
|------|--------|---------|------|
| DateUtil.java | 12 | 无外部依赖 | 推荐提取 |
| StringUtil.java | 8 | 依赖 commons-lang3 | 推荐提取 |
| JsonUtil.java | 15 | 依赖 jackson | 推荐提取 |

#### 需人工判断

| 文件 | 引用数 | 依赖分析 | 建议 |
|------|--------|---------|------|
| ElementCacheUtil.java | 3 | @Autowired RedisTemplate | 需判断 |

#### 建议保留

| 文件 | 引用数 | 依赖分析 | 建议 |
|------|--------|---------|------|
| ServiceHelper.java | 2 | @Autowired ElementService | 保留 |

---

### S8-06: config/ 包检查详情

#### 推荐提取

| 文件 | 引用数 | 依赖分析 | 建议 |
|------|--------|---------|------|
| SwaggerConfig.java | 1 | 仅依赖 springfox | 推荐提取 |

#### 建议保留

| 文件 | 引用数 | 依赖分析 | 建议 |
|------|--------|---------|------|
| MqAutoConfiguration.java | 1 | @Autowired RocketMQ相关 | 保留 |

---

### 目标模块状态

| 检查项 | 状态 |
|--------|------|
| grp-common-element 目录存在 | OK |
| pom.xml 存在 | 需创建 |
| src/main/java 目录 | 需创建 |
| 父 POM modules 声明 | 需检查 |
