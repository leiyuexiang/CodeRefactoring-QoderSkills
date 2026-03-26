# 日志增强规则

## 类级别日志声明

| 现有状态 | 处理方式 |
|----------|----------|
| 无日志声明 | 添加 `@Slf4j` 注解 + `import lombok.extern.slf4j.Slf4j;` |
| 已有 `@Slf4j` | 不动，使用 `log` 变量 |
| 已有 `LoggerFactory.getLogger()` | 不改声明，使用已有的 `logger` 或 `log` 变量名 |
| 已有 `LogFactory.getLog()` | 不改声明，使用已有的 `log` 变量名 |

---

## 方法级日志策略

### Service 层方法

```java
// 方法入口
log.info("方法名 - 开始, 关键参数={}, 参数2={}", param1, param2);

// 异常捕获块
log.error("方法名 - 执行异常: {}", e.getMessage(), e);

// 重要业务分支
log.warn("方法名 - 异常分支描述, 参数={}", param);
```

### DAO 层方法

```java
// 方法入口（用 debug 避免生产环境日志过多）
log.debug("方法名 - 执行, 关键参数={}", param1);

// 异常捕获块
log.error("方法名 - 数据库操作异常: {}", e.getMessage(), e);
```

---

## 不添加日志的场景

- 简单 getter/setter 方法
- 纯委托方法（方法体仅 `return dao.xxx()` 一行）
- 循环体内部不添加 `log.info`（如需要可用 `log.debug`）
- 方法中**已有**任何 `log.info/debug/warn/error` 调用 → **整个方法不动**

---

## 日志格式规范

| 规则 | 说明 |
|------|------|
| 使用占位符 | `log.info("msg={}", var)` 而非 `log.info("msg=" + var)` |
| 异常对象放最后 | `log.error("msg: {}", e.getMessage(), e)` |
| 方法名前缀 | 日志消息以 `"方法名 - "` 开头 |
| 关键参数记录 | 只记录关键业务参数，不记录大对象 |
