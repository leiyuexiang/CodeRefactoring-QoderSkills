# Step8 公共模块提取 - 迁移流程示例

## 示例一：迁移 constant/ElementConstant.java（最简单场景）

### 背景

`ElementConstant.java` 是纯常量类，无任何依赖，被 3 个模块引用。

### Before（迁移前）

**文件位置**: `grp-capability-element/element-service/src/main/java/grp/pt/constant/ElementConstant.java`

```java
package grp.pt.constant;

/**
 * 要素常量定义
 */
public class ElementConstant {

    /** 要素类型：基础 */
    public static final String ELEMENT_TYPE_BASIC = "BASIC";

    /** 要素类型：高级 */
    public static final String ELEMENT_TYPE_ADVANCED = "ADVANCED";

    /** 默认页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;
}
```

### Step 1: Read

```
Read 文件: grp-capability-element/element-service/src/main/java/grp/pt/constant/ElementConstant.java
结果: 读取成功，package 声明为 "package grp.pt.constant;"
```

### Step 2: Analyze

```
来源 package: grp.pt.constant
目标 package: grp.pt.constant（grp-common-element/src/main/java/grp/pt/constant/）
判定: 相同 → 无需修改 package 声明
```

### Step 3: Copy

```bash
# Windows
mkdir "grp-common-element\src\main\java\grp\pt\constant"
copy "grp-capability-element\element-service\src\main\java\grp\pt\constant\ElementConstant.java" "grp-common-element\src\main\java\grp\pt\constant\ElementConstant.java"

# Linux/Mac
mkdir -p grp-common-element/src/main/java/grp/pt/constant/
cp grp-capability-element/element-service/src/main/java/grp/pt/constant/ElementConstant.java grp-common-element/src/main/java/grp/pt/constant/ElementConstant.java
```

### Step 4: Grep

```
Grep 模式: import grp\.pt\.constant\.ElementConstant
搜索范围: 所有 .java 文件
结果:
  - element-service/src/main/java/grp/pt/service/impl/ElementServiceImpl.java (第5行)
  - element-controller/src/main/java/grp/pt/controller/custom/ElementController.java (第8行)
  - bff-element/src/main/java/grp/pt/bff/ElementBffService.java (第3行)
```

### Step 5: UpdateImport

```
package 路径未变 (grp.pt.constant → grp.pt.constant)
→ 无需修改任何 import 语句
→ 跳过此步骤
```

### Step 6: Delete

```
DeleteFile: grp-capability-element/element-service/src/main/java/grp/pt/constant/ElementConstant.java
```

### Step 7: Verify

```
V-01: Read 目标文件 → package grp.pt.constant; → 与目录路径一致 → OK
V-02: Grep import grp.pt.constant.ElementConstant → 3处引用，路径有效 → OK
V-03: Read 来源路径 → 文件不存在 → OK（已删除）
```

### After（迁移后）

**新文件位置**: `grp-common-element/src/main/java/grp/pt/constant/ElementConstant.java`

```java
package grp.pt.constant;

/**
 * 要素常量定义
 */
public class ElementConstant {

    /** 要素类型：基础 */
    public static final String ELEMENT_TYPE_BASIC = "BASIC";

    /** 要素类型：高级 */
    public static final String ELEMENT_TYPE_ADVANCED = "ADVANCED";

    /** 默认页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;
}
```

**注意**: 文件内容与迁移前完全相同，一个字节都不变。

---

## 示例二：迁移 util/DateUtil.java（纯工具类）

### 背景

`DateUtil.java` 是纯工具类，依赖第三方库 `commons-lang3`，无 Service/DAO 依赖。

### Before（迁移前）

**文件位置**: `grp-capability-element/element-service/src/main/java/grp/pt/util/DateUtil.java`

```java
package grp.pt.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.commons.lang3.StringUtils;

/**
 * 日期工具类
 */
public class DateUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return StringUtils.EMPTY;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    public static LocalDateTime parse(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            return null;
        }
        return LocalDateTime.parse(dateStr, DEFAULT_FORMATTER);
    }
}
```

### 检查阶段判定

```
Grep#1: "import grp\.pt\..*\.service\." in DateUtil.java → 无匹配
Grep#2: "import grp\.pt\..*\.(dao|mapper)\." in DateUtil.java → 无匹配
Grep#3: "@Autowired|@Resource" in DateUtil.java → 无匹配
Grep#4: "import.*grp\.pt\.util\.DateUtil" in 整个工程 → 11 处（3个模块）

判定: 无 Service/DAO 依赖，无注入 → EXTRACT（推荐提取）
```

### 迁移执行

**Step 1-7**: 与示例一相同流程

### After（迁移后）

**新文件位置**: `grp-common-element/src/main/java/grp/pt/util/DateUtil.java`
- 文件内容完全不变
- 引用方的 import 语句不变：`import grp.pt.util.DateUtil;`
- common 模块 pom.xml 需添加 `commons-lang3` 依赖（迁移全部完成后统一处理）

---

## 示例三：RETAIN 判定 - cache/ElementDataCache.java（缓存加载器）

### 背景

`ElementDataCache.java` 是缓存加载器，通过 @Autowired 注入了 DAO 层 Mapper。

### 检查阶段

**文件位置**: `grp-capability-element/element-service/src/main/java/grp/pt/cache/ElementDataCache.java`

```java
package grp.pt.cache;

import grp.pt.dao.ElementMapper;
import grp.pt.entity.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ElementDataCache {

    @Autowired
    private ElementMapper elementMapper;

    private final Map<String, Element> cache = new ConcurrentHashMap<>();

    public void refresh() {
        List<Element> elements = elementMapper.selectAll();
        cache.clear();
        elements.forEach(e -> cache.put(e.getCode(), e));
    }

    public Element getByCode(String code) {
        return cache.get(code);
    }
}
```

### 判定过程

```
Grep#1: "import grp\.pt\..*\.service\." → 无匹配
Grep#2: "import grp\.pt\..*\.(dao|mapper)\." → 匹配: import grp.pt.dao.ElementMapper;
Grep#3: "@Autowired|@Resource" → 匹配: @Autowired private ElementMapper elementMapper;

判定流程:
  Grep#2 = 有匹配（依赖 DAO/Mapper，属于缓存加载器）
  → RETAIN（建议保留，缓存加载器与 DAO 强耦合）

结果: RETAIN — 不迁移此文件
原因: @Autowired 注入了 ElementMapper（DAO 层），迁移到 common 后会造成循环依赖
```

---

## 示例四：EVALUATE 判定 - cache/CacheManager.java（通用缓存工具）

### 背景

`CacheManager.java` 是通用缓存管理工具，注入了 RedisTemplate（通用组件），被 2 个模块引用。

### 检查阶段

**文件位置**: `grp-capability-element/element-service/src/main/java/grp/pt/cache/CacheManager.java`

```java
package grp.pt.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class CacheManager {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
```

### 判定过程

```
Grep#1: "import grp\.pt\..*\.service\." → 无匹配
Grep#2: "import grp\.pt\..*\.(dao|mapper)\." → 无匹配
Grep#3: "@Autowired|@Resource" → 匹配: @Autowired private RedisTemplate
Grep#4: "import.*grp\.pt\.cache\.CacheManager" → 2 个不同模块引用

判定流程:
  Grep#2 = 无匹配（不依赖 DAO）
  Grep#1 = 无匹配（不依赖 Service）
  Grep#3 = 有匹配，但注入的是 RedisTemplate（属于通用组件白名单）
  → EXTRACT 或 EVALUATE

  进一步: Grep#4 跨模块引用数 = 2 >= 2
  → EVALUATE（需人工判断）

结果: EVALUATE — 列入报告，等待用户确认
原因: 注入了通用组件 RedisTemplate，被多模块引用，可以迁移但需用户确认
      （因为是 @Component 注解的 Spring Bean，迁移后需确认 @ComponentScan 覆盖）
```

---

## 示例五：config/ 配置类的特殊处理

### 5a: SwaggerConfig.java — EXTRACT 判定

```java
package grp.pt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("Element API")
                        .version("1.0")
                        .build())
                .select()
                .build();
    }
}
```

**判定**:
```
Grep#1 (@MapperScan): 无匹配
Grep#2 (@ComponentScan): 无匹配
Grep#4 (@Autowired): 无匹配
Grep#5 (Service import): 无匹配
Grep#6 (DAO import): 无匹配
→ EXTRACT（推荐提取）— 纯通用配置类
```

### 5b: MybatisConfig.java — RETAIN 判定

```java
package grp.pt.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("grp.pt.dao")
public class MybatisConfig {
}
```

**判定**:
```
Grep#1 (@MapperScan): 匹配!
→ RETAIN（安全红线 S-05，绝对不移动）
```

---

## 示例六：POM 依赖调整

### 所有文件迁移完成后，统一处理 POM

#### element-service/pom.xml 添加 common 依赖

**Before**:
```xml
<dependencies>
    <dependency>
        <groupId>grp</groupId>
        <artifactId>element-dao</artifactId>
        <version>${revision}</version>
    </dependency>
    <!-- 其他依赖 -->
</dependencies>
```

**After**:
```xml
<dependencies>
    <dependency>
        <groupId>grp</groupId>
        <artifactId>grp-common-element</artifactId>
        <version>${revision}</version>
    </dependency>
    <dependency>
        <groupId>grp</groupId>
        <artifactId>element-dao</artifactId>
        <version>${revision}</version>
    </dependency>
    <!-- 其他依赖 -->
</dependencies>
```

#### grp-common-element/pom.xml 添加第三方依赖

**Before**:
```xml
<dependencies>
    <!-- 空或仅有基础依赖 -->
</dependencies>
```

**After**（根据迁移文件的实际依赖添加）:
```xml
<dependencies>
    <!-- DateUtil 依赖 -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>

    <!-- JsonUtil 依赖 -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>

    <!-- CacheManager 依赖（如用户确认迁移） -->
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-redis</artifactId>
    </dependency>

    <!-- Lombok（如迁移的类使用了 @Slf4j、@Data 等） -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 迁移完成报告示例

```
## Step8 公共模块提取 - 迁移完成报告

### 工程信息
- 工程版本: 3.6.0-SNAPSHOT
- 来源模块: grp-capability-element/element-service
- 目标模块: grp-common-element
- 执行时间: 2026-03-30

### 迁移统计

| 包名 | 迁移文件数 | 保留文件数 | 冲突文件数 |
|------|-----------|-----------|-----------|
| constant/ | 1 | 0 | 0 |
| enums/ | 1 | 0 | 0 |
| exception/ | 2 | 0 | 0 |
| util/ | 15 | 4 | 0 |
| cache/ | 2 | 3 | 0 |
| config/ | 1 | 2 | 0 |
| **合计** | **22** | **9** | **0** |

### 保留文件清单

| 文件 | 原因 | 判定 |
|------|------|------|
| util/ServiceHelper.java | @Autowired ElementService | RETAIN |
| util/DaoQueryUtil.java | import grp.pt.dao.ElementMapper | RETAIN |
| util/BizCalculator.java | @Autowired ElementService | RETAIN |
| util/ElementProcessor.java | @Resource ElementDao | RETAIN |
| cache/ElementDataCache.java | @Autowired ElementMapper | RETAIN |
| cache/BizCacheLoader.java | @Autowired ElementService | RETAIN |
| cache/ElementTreeCache.java | @Autowired ElementMapper | RETAIN |
| config/MybatisConfig.java | @MapperScan (S-05) | RETAIN |
| config/ScheduleConfig.java | @Autowired TaskService | RETAIN |

### POM 变更

| 模块 | 变更内容 |
|------|---------|
| element-service/pom.xml | 添加 grp-common-element 依赖 |
| element-controller/pom.xml | 添加 grp-common-element 依赖 |
| grp-common-element/pom.xml | 添加 commons-lang3、jackson-databind、lombok 依赖 |
| element-module/pom.xml | 确认 modules 包含 grp-common-element (已存在) |

### 验证结果

| 验证项 | 状态 |
|--------|------|
| 所有目标文件 package 声明正确 | OK |
| 所有 import 引用路径正确 | OK |
| 所有来源文件已删除 | OK |
| 空目录已清理 | OK |
```
