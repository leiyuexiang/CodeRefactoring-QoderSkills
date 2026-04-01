# Step8 公共模块提取 - 检查报告示例

## Step8 公共模块提取检查报告

### 检查概要

| 项目 | 值 |
|------|------|
| 检查时间 | 2026-03-30 |
| 工程版本 | 3.6.0-SNAPSHOT |
| 来源模块 | grp-capability-element/element-service |
| 目标模块 | grp-common-element |

### 检查结果汇总

| 包名 | 文件数 | EXTRACT | EVALUATE | RETAIN | RETAIN 原因 |
|------|--------|---------|----------|--------|------------|
| constant/ | 1 | 1 | 0 | 0 | — |
| enums/ | 1 | 1 | 0 | 0 | — |
| exception/ | 3 | 2 | 0 | 1 | @ControllerAdvice + Service 依赖 |
| util/ | 21 | 15 | 2 | 4 | @Autowired Service/DAO |
| cache/ | 7 | 2 | 1 | 4 | @Autowired DAO/Mapper |
| config/ | 3 | 1 | 0 | 2 | @MapperScan / Service 依赖 |
| **合计** | **36** | **22** | **3** | **11** | — |

---

### S8-01: util/ 包检查详情

#### EXTRACT（推荐提取）— 15 个文件

| 文件 | 跨模块引用数 | Service依赖 | DAO依赖 | 注入依赖 | 判定路径 |
|------|------------|------------|---------|---------|---------|
| DateUtil.java | 3 | 无 | 无 | 无 | Gate2-Q3(NO)→Q4(NO)→Gate3-Q5(NO)→Gate4-Q6(NO)→EXTRACT |
| StringUtil.java | 2 | 无 | 无 | 无 | 同上 |
| JsonUtil.java | 4 | 无 | 无 | 无 | 同上 |
| NumberUtil.java | 2 | 无 | 无 | 无 | 同上 |
| FileUtil.java | 1 | 无 | 无 | 无 | 同上 |
| EncryptUtil.java | 2 | 无 | 无 | 无 | 同上 |
| BeanCopyUtil.java | 3 | 无 | 无 | 无 | 同上 |
| ExcelUtil.java | 1 | 无 | 无 | 无 | 同上 |
| TreeUtil.java | 2 | 无 | 无 | 无 | 同上 |
| PageUtil.java | 3 | 无 | 无 | 无 | 同上 |
| ValidateUtil.java | 1 | 无 | 无 | 无 | 同上 |
| IdGenerator.java | 2 | 无 | 无 | 无 | 同上 |
| HttpUtil.java | 1 | 无 | 无 | 无 | 同上 |
| Base64Util.java | 1 | 无 | 无 | 无 | 同上 |
| CollectionUtil.java | 2 | 无 | 无 | 无 | 同上 |

#### EVALUATE（需人工判断）— 2 个文件

| 文件 | 跨模块引用数 | Service依赖 | DAO依赖 | 注入依赖 | 判定路径 |
|------|------------|------------|---------|---------|---------|
| RedisUtil.java | 2 | 无 | 无 | @Autowired RedisTemplate（通用组件） | Gate3-Q5(YES)→Q5a(白名单)→Q5b(>=2)→EVALUATE |
| SpringContextUtil.java | 3 | 无 | 无 | @Autowired ApplicationContext（通用组件） | Gate3-Q5(YES)→Q5a(白名单)→Q5b(>=2)→EVALUATE |

#### RETAIN（建议保留）— 4 个文件

| 文件 | 跨模块引用数 | Service依赖 | DAO依赖 | 注入依赖 | 判定路径 |
|------|------------|------------|---------|---------|---------|
| ServiceHelper.java | 1 | 有(import grp.pt.service.IElementService) | 无 | @Autowired IElementService | Gate2-Q4(YES)→Q4a(<2)→RETAIN |
| DaoQueryUtil.java | 1 | 无 | 有(import grp.pt.dao.ElementMapper) | @Resource ElementMapper | Gate2-Q3(YES)→RETAIN |
| BizCalculator.java | 1 | 有(import grp.pt.service.ICalcService) | 无 | @Autowired ICalcService | Gate2-Q4(YES)→Q4a(<2)→RETAIN |
| ElementProcessor.java | 1 | 无 | 有(import grp.pt.dao.ElementDao) | @Resource ElementDao | Gate2-Q3(YES)→RETAIN |

---

### S8-02: cache/ 包检查详情

#### EXTRACT（推荐提取）— 2 个文件

| 文件 | 跨模块引用数 | 判定路径 |
|------|------------|---------|
| CacheKeyBuilder.java | 2 | 无依赖、无注入→EXTRACT |
| CacheConstants.java | 1 | 无依赖、无注入→EXTRACT |

#### EVALUATE（需人工判断）— 1 个文件

| 文件 | 跨模块引用数 | 注入依赖 | 判定路径 |
|------|------------|---------|---------|
| CacheManager.java | 2 | @Autowired RedisTemplate（通用组件） | Gate3-Q5(YES)→Q5a(白名单)→Q5b(>=2)→EVALUATE |

#### RETAIN（建议保留）— 4 个文件

| 文件 | 注入依赖 | 判定路径 |
|------|---------|---------|
| ElementDataCache.java | @Autowired ElementMapper | Gate2-Q3(YES)→RETAIN |
| BizCacheLoader.java | @Autowired IElementService | Gate2-Q4(YES)→Q4a(<2)→RETAIN |
| ElementTreeCache.java | @Autowired ElementMapper | Gate2-Q3(YES)→RETAIN |
| ScheduleCacheRefresher.java | @Autowired IElementService + ElementMapper | Gate2-Q3(YES)→RETAIN |

---

### S8-03: constant/ 包检查详情

#### EXTRACT（推荐提取）— 1 个文件

| 文件 | 跨模块引用数 | 判定路径 |
|------|------------|---------|
| ElementConstant.java | 3 | 无依赖、无注入→EXTRACT |

---

### S8-04: enums/ 包检查详情

#### EXTRACT（推荐提取）— 1 个文件

| 文件 | 跨模块引用数 | 判定路径 |
|------|------------|---------|
| ElementTypeEnum.java | 2 | 无依赖、无注入→EXTRACT |

---

### S8-05: exception/ 包检查详情

#### EXTRACT（推荐提取）— 2 个文件

| 文件 | 类型 | 判定路径 |
|------|------|---------|
| BusinessException.java | 异常定义类(extends RuntimeException) | 无依赖→EXTRACT |
| ElementNotFoundException.java | 异常定义类(extends RuntimeException) | 无依赖→EXTRACT |

#### RETAIN（建议保留）— 1 个文件

| 文件 | 类型 | 注入依赖 | 判定路径 |
|------|------|---------|---------|
| GlobalExceptionHandler.java | 全局异常处理器(@RestControllerAdvice) | @Autowired IErrorLogService | Gate2-Q4(YES)→Q4a(<2)→RETAIN |

---

### S8-06: config/ 包检查详情

#### EXTRACT（推荐提取）— 1 个文件

| 文件 | 判定路径 |
|------|---------|
| SwaggerConfig.java | 无 @MapperScan、无 @ComponentScan、无注入→Gate4-Q6(YES)→Q6a(NO)→EXTRACT |

#### RETAIN（建议保留）— 2 个文件

| 文件 | 原因 | 判定路径 |
|------|------|---------|
| MybatisConfig.java | @MapperScan("grp.pt.dao") | Gate1-Q1(YES)→RETAIN (S-05) |
| ScheduleConfig.java | @Autowired ITaskService | Gate2-Q4(YES)→Q4a(<2)→RETAIN |

---

### 目标模块状态

| 检查项 | 状态 | 说明 |
|--------|------|------|
| grp-common-element 目录存在 | OK | 路径已确认 |
| pom.xml 存在 | OK | 已存在 |
| src/main/java 目录 | 需创建 | 迁移时自动创建 |
| 父 POM modules 声明 | OK | 已包含 grp-common-element |

### 第三方依赖分析

迁移 EXTRACT 文件后，common 模块需添加的第三方依赖：

| 依赖 | 来源文件 | 检查 common pom.xml |
|------|---------|-------------------|
| commons-lang3 | DateUtil.java, StringUtil.java | 需添加 |
| jackson-databind | JsonUtil.java | 需添加 |
| lombok | 多个文件 @Slf4j | 需添加 |
| hutool-all | ExcelUtil.java | 需添加 |
