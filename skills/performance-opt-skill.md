---
name: performance-opt-skill
description: 针对 Spring Boot 工程进行全面的性能分析与优化。当用户要求进行性能优化、性能分析、慢接口排查、JVM 调优、数据库优化、缓存优化时使用此技能。
---

本技能指导对 Spring Boot 工程进行系统性的性能分析与优化。基于生产环境实战经验，覆盖**启动优化**、**JVM 调优**、**数据库与 SQL 优化**、**缓存策略**、**线程池与连接池调优**、**接口性能优化**、**序列化与网络优化**七大核心领域。

用户提供项目路径或指定优化方向，技能分析源代码与配置后输出可落地的优化方案。

## 工作流程

### 第一阶段：项目诊断

在输出优化方案前，必须深入分析目标项目：

1. **项目结构扫描**
   - 识别项目类型：单体 / 微服务（Spring Cloud / Dubbo）
   - 读取 `pom.xml` 或 `build.gradle`，分析依赖树，识别不必要的 starter 依赖
   - 统计模块数量、代码规模

2. **启动配置分析**
   - 读取 `application.yml` / `application.properties` 及 profile 配置
   - 检查主启动类上的注解（`@SpringBootApplication`、`@EnableXxx` 系列）
   - 识别自动配置排除项（`spring.autoconfigure.exclude`）

3. **数据访问层分析**
   - 识别 ORM 框架（MyBatis / MyBatis-Plus / JPA / Hibernate）
   - 扫描 Mapper XML 文件，检查 SQL 写法
   - 检查数据源配置（连接池类型、参数）
   - 识别分页查询实现方式

4. **缓存层分析**
   - 检查缓存依赖（Redis / Caffeine / EhCache）
   - 扫描 `@Cacheable`、`@CacheEvict`、`@CachePut` 使用情况
   - 检查 Redis 配置（序列化方式、连接池、超时时间）

5. **线程池与异步分析**
   - 搜索 `@Async`、`ThreadPoolTaskExecutor`、`ExecutorService` 配置
   - 检查是否存在未配置线程池的 `@Async` 调用（使用 SimpleAsyncTaskExecutor 的隐患）
   - 检查 `@Scheduled` 定时任务配置

6. **接口层分析**
   - 扫描 Controller 层，识别接口数量和复杂度
   - 检查是否存在同步阻塞的外部调用（HTTP / RPC）
   - 检查文件上传下载的实现方式
   - 检查统一返回体和全局异常处理实现

7. **日志与监控分析**
   - 检查日志框架配置（Logback / Log4j2）和日志级别
   - 检查是否集成 Actuator / Prometheus / Micrometer
   - 检查日志中是否存在性能敏感操作（循环内打印日志、大对象 toString）

**关键要求**：所有诊断结论必须基于实际读取的源代码和配置文件，禁止假设或臆断。

### 第二阶段：优化方案输出

根据诊断结果，生成一份结构化的优化方案文档，输出到目标项目根目录，命名为 `{project-name}-performance-optimization.md`。

---

## 输出文档结构

生成的优化方案必须严格遵循以下结构：

```markdown
# {项目名称} 性能优化方案

## 文档信息
| 项目 | 内容 |
|------|------|
| 项目名称 | {name} |
| Spring Boot 版本 | {从 pom.xml 获取} |
| Java 版本 | {从 pom.xml 获取} |
| 分析日期 | {当前日期} |
| 优化优先级 | P0-紧急 / P1-高 / P2-中 / P3-低 |

---

## 一、性能诊断概览

### 1.1 项目概况
{项目架构概述、模块组成、技术栈总结}

### 1.2 发现问题汇总
| 序号 | 问题分类 | 问题描述 | 严重程度 | 影响范围 |
|------|----------|----------|----------|----------|
| 1 | {分类} | {描述} | P0/P1/P2/P3 | {影响的模块或功能} |

---

## 二、启动优化

### 2.1 自动配置精简
{分析当前引入但未实际使用的 starter 依赖，给出排除建议}

**当前问题**：
{列出发现的具体问题}

**优化方案**：
```yaml
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.xxx.XxxAutoConfiguration
```

**预期效果**：{启动时间减少预估}

### 2.2 Bean 延迟加载
{分析是否适合开启延迟加载，哪些 Bean 可以延迟初始化}

**优化方案**：
```yaml
spring:
  main:
    lazy-initialization: true
```

**注意事项**：{延迟加载对首次请求的影响、不适用场景}

### 2.3 组件扫描优化
{检查 @ComponentScan 范围是否过大}

---

## 三、JVM 调优

### 3.1 堆内存配置
{根据项目规模和特点给出 JVM 参数建议}

**当前配置**：{从启动脚本或 Dockerfile 中读取当前 JVM 参数}

**优化方案**：
```bash
# 堆内存设置（根据容器/服务器实际内存调整）
-Xms{推荐值} -Xmx{推荐值}

# 元空间设置
-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m
```

### 3.2 GC 策略选择
{根据 Java 版本和应用特点推荐 GC 策略}

**推荐方案**：
```bash
# Java 8 推荐 G1GC
-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:G1HeapRegionSize=16m

# Java 11+ 推荐 ZGC（低延迟场景）
-XX:+UseZGC -XX:ZCollectionInterval=120
```

**选型依据**：{为什么选择该 GC 策略}

### 3.3 GC 日志配置
```bash
# Java 8
-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/var/log/gc.log

# Java 11+
-Xlog:gc*:file=/var/log/gc.log:time,uptime,level,tags:filecount=5,filesize=50m
```

---

## 四、数据库与 SQL 优化

### 4.1 连接池调优
{分析当前连接池配置，给出优化建议}

**当前配置**：{列出当前连接池参数}

**优化方案**：
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: {推荐值，通常为 max 的一半}
      maximum-pool-size: {推荐值，通常 CPU核数*2 + 磁盘数}
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

**参数说明**：{每个参数的调优依据}

### 4.2 SQL 优化
{逐条分析发现的问题 SQL}

#### 问题 SQL #{序号}
**位置**：`{Mapper类名}.{方法名}` — `{Mapper XML 文件路径}:{行号}`

**原始 SQL**：
```sql
{原始 SQL}
```

**问题分析**：{具体问题：全表扫描 / 隐式类型转换 / 未命中索引 / N+1查询 等}

**优化后 SQL**：
```sql
{优化后的 SQL}
```

**建议索引**：
```sql
CREATE INDEX idx_xxx ON table_name(column1, column2);
```

### 4.3 MyBatis 优化
{针对 MyBatis 框架的特定优化}

- **批量操作优化**：将循环单条插入改为批量插入
- **ResultMap 优化**：避免使用 `resultType="map"`，使用明确的 ResultMap
- **分页优化**：检查分页插件配置，深分页问题处理
- **二级缓存评估**：是否适合开启，开启后的一致性问题

### 4.4 N+1 查询问题
{扫描代码中循环查库的模式}

**位置**：`{Service类名}.{方法名}` — `{文件路径}:{行号}`
**问题**：{描述 N+1 问题的具体场景}
**优化方案**：{改为批量查询 + 内存组装，或使用 JOIN 查询}

---

## 五、缓存优化

### 5.1 缓存架构评估
{评估当前缓存使用是否合理}

### 5.2 本地缓存引入
{对于高频读取、变化不频繁的数据，建议使用本地缓存}

**优化方案**：
```java
@Bean
public CacheManager caffeineCacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager();
    manager.setCaffeine(Caffeine.newBuilder()
        .initialCapacity(100)
        .maximumSize(1000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .recordStats());
    return manager;
}
```

**适用场景**：{字典数据 / 配置数据 / 组织机构树 等}

### 5.3 Redis 优化
{分析 Redis 使用中的问题}

- **序列化优化**：将 JdkSerializationRedisSerializer 替换为 Jackson2JsonRedisSerializer 或 GenericFastJsonRedisSerializer
- **Pipeline 批量操作**：将循环单次操作改为 Pipeline
- **Key 设计优化**：检查 Key 命名规范、TTL 设置
- **大 Key 排查**：检查是否存在超大 Value 的 Key（如将整个列表存入单个 Key）
- **连接池优化**：Lettuce 连接池参数调整

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 32
        max-idle: 16
        min-idle: 8
        max-wait: 5000ms
```

### 5.4 缓存穿透/击穿/雪崩防护
{检查是否有相关防护措施，给出补充建议}

---

## 六、线程池与异步优化

### 6.1 线程池配置
{分析当前线程池配置是否合理}

**当前问题**：{未配置线程池 / 参数不合理 / 缺少拒绝策略}

**优化方案**：
```java
@Bean("taskExecutor")
public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize({CPU核数 + 1，IO密集型为 CPU核数 * 2});
    executor.setMaxPoolSize({推荐值});
    executor.setQueueCapacity({推荐值});
    executor.setKeepAliveSeconds(60);
    executor.setThreadNamePrefix("{业务名}-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    return executor;
}
```

**参数说明**：
- CPU 密集型：corePoolSize = CPU核数 + 1
- IO 密集型：corePoolSize = CPU核数 * 2
- 混合型：需根据 IO 等待比例计算

### 6.2 异步调用优化
{分析 @Async 使用情况}

- 确保所有 `@Async` 方法指定线程池名称
- 检查 `@Async` 方法的返回值处理（Future / CompletableFuture）
- 检查 `@Async` 方法的异常处理（AsyncUncaughtExceptionHandler）

### 6.3 Tomcat 线程池调优
```yaml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 50
    max-connections: 10000
    accept-count: 100
    connection-timeout: 20000
```

**调优依据**：{根据项目 QPS 和接口平均响应时间计算}

---

## 七、接口性能优化

### 7.1 慢接口识别
{如果能从代码中识别出潜在慢接口，列出分析}

| 序号 | 接口路径 | 潜在问题 | 优化建议 |
|------|----------|----------|----------|
| 1 | {path} | {问题描述} | {建议} |

### 7.2 接口通用优化

#### 7.2.1 响应压缩
```yaml
server:
  compression:
    enabled: true
    min-response-size: 1024
    mime-types: application/json,application/xml,text/html,text/plain
```

#### 7.2.2 JSON 序列化优化
{检查当前 JSON 框架配置，给出优化建议}

```yaml
spring:
  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false
```

#### 7.2.3 接口幂等与防重复提交
{检查是否有相关机制，给出建议}

### 7.3 文件上传下载优化
{如果项目包含文件操作，分析其实现}

- 大文件使用流式处理，避免全量加载到内存
- 使用 `StreamingResponseBody` 替代 byte[] 返回
- 文件上传限制合理性检查

### 7.4 外部调用优化
{分析 Feign / RestTemplate / WebClient 调用}

- 超时时间设置是否合理
- 是否配置了重试机制
- 是否有熔断降级（Hystrix / Resilience4j / Sentinel）
- HTTP 连接池是否配置（Apache HttpClient / OkHttp）

```yaml
feign:
  client:
    config:
      default:
        connect-timeout: 5000
        read-timeout: 10000
  httpclient:
    enabled: true
    max-connections: 200
    max-connections-per-route: 50
```

---

## 八、日志优化

### 8.1 日志框架优化
{分析日志配置问题}

- **异步日志**：将同步 Appender 改为 AsyncAppender
- **日志级别**：生产环境确保非关键包的日志级别为 WARN 或 ERROR
- **日志格式**：避免使用字符串拼接，使用 `{}` 占位符

```xml
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <queueSize>512</queueSize>
    <discardingThreshold>0</discardingThreshold>
    <includeCallerData>false</includeCallerData>
    <appender-ref ref="FILE"/>
</appender>
```

### 8.2 生产环境日志级别建议
```yaml
logging:
  level:
    root: WARN
    com.{公司包名}: INFO
    org.springframework: WARN
    org.apache: WARN
    com.zaxxer.hikari: WARN
    io.lettuce: WARN
```

---

## 九、其他优化

### 9.1 Spring Boot Actuator 监控端点
{检查是否启用了 Actuator，建议启用的端点}

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    tags:
      application: ${spring.application.name}
```

### 9.2 优雅停机
```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

### 9.3 Spring Boot DevTools 生产排查
{确保生产环境未引入 spring-boot-devtools}

---

## 十、优化实施计划

### 10.1 优化优先级矩阵
| 优先级 | 优化项 | 预期收益 | 实施风险 | 是否需要重启 |
|--------|--------|----------|----------|--------------|
| P0 | {项} | {收益描述} | 高/中/低 | 是/否 |
| P1 | {项} | {收益描述} | 高/中/低 | 是/否 |

### 10.2 验证方法
{对每项优化给出验证方式}

| 优化项 | 验证方式 | 关注指标 |
|--------|----------|----------|
| {项} | {压测/监控/日志对比} | {具体指标：响应时间/吞吐量/GC次数/CPU使用率} |

### 10.3 回滚方案
```
{对高风险优化项给出回滚步骤}
```

## 优化方案质量标准

### 问题定位规范
- 每个问题必须指明源代码位置（文件路径 + 行号或方法名）
- 必须说明问题的实际影响（性能损耗的量级或表现）
- 禁止列出"可能存在"的问题，所有问题必须有代码依据

### 优化方案规范
- 每个优化方案必须包含：当前状况、问题分析、优化方案（含代码/配置）、预期效果
- 配置类优化必须给出完整的 YAML/Properties 配置
- 代码类优化必须给出优化前后的代码对比
- 标注优化的风险等级和回滚方案

### 严重程度定义
- **P0-紧急**：直接导致生产事故的问题（内存泄漏、连接池耗尽、死锁、线程池溢出）
- **P1-高**：显著影响性能的问题（慢 SQL、N+1 查询、缺少索引、序列化效率低）
- **P2-中**：存在优化空间的问题（配置参数未调优、缺少缓存、日志级别不当）
- **P3-低**：锦上添花的优化（启动速度、代码风格优化）

### 禁止事项
- 禁止推荐未经生产验证的技术方案
- 禁止给出脱离项目实际情况的通用建议
- 禁止建议引入项目当前技术栈之外的重量级框架（除非问题无法在当前技术栈内解决）
- 禁止仅列出问题而不给出具体的优化代码或配置
- 禁止忽略优化的副作用和风险

## 常见生产问题检查清单

以下为生产环境中高频出现的性能问题，诊断时重点排查：

### 内存相关
- [ ] 是否存在大对象频繁创建（如在循环内 new 大数组/集合）
- [ ] 是否存在集合只增不减的情况（内存泄漏）
- [ ] 是否存在 ThreadLocal 未清理（在线程池环境下尤其危险）
- [ ] 是否存在大量临时字符串拼接（应使用 StringBuilder）

### 数据库相关
- [ ] 是否存在循环内单条查库（N+1 问题）
- [ ] 是否存在 `SELECT *` 查询未限定字段
- [ ] 是否存在未分页的全表查询
- [ ] 是否存在慢 SQL 未加索引
- [ ] 是否存在事务范围过大（`@Transactional` 标注在整个方法上，但方法内有非事务操作如远程调用）
- [ ] 是否存在连接池配置过小或连接泄漏

### 线程与并发相关
- [ ] 是否存在使用默认线程池的 `@Async` 方法
- [ ] 是否存在未设置超时的外部调用（HTTP / RPC / 数据库）
- [ ] 是否存在锁粒度过大（如 synchronized 整个方法而非关键代码段）
- [ ] 是否存在 `@Transactional` + `synchronized` 的并发问题

### 缓存相关
- [ ] 是否存在高频读取但未缓存的数据（如字典表、配置表）
- [ ] 是否存在缓存未设置过期时间
- [ ] 是否存在 Redis 大 Key（单个 Value 超过 10KB）
- [ ] 是否存在缓存与数据库一致性问题未处理

### 序列化相关
- [ ] Redis 是否使用 JDK 默认序列化（性能差、不可读）
- [ ] JSON 序列化是否配置了忽略空值
- [ ] 接口返回是否包含大量无用字段

### 配置相关
- [ ] Tomcat 线程池是否使用默认配置
- [ ] HikariCP 连接池是否使用默认配置
- [ ] Redis 连接池是否配置
- [ ] 日志级别在生产环境是否合理

## 输出语言

- 文档正文全部使用**中文**输出
- 配置项、类名、方法名、SQL 等代码标识符保持原文
- 技术术语首次出现时可附英文原文（如：垃圾回收（Garbage Collection, GC））

