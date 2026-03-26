# Step8 公共模块提取 - 迁移流程示例

## 示例一：迁移 constant/ElementConstant.java

### Step 1: Read

```
读取文件: grp-capability-element/element-service/src/main/java/grp/pt/constant/ElementConstant.java
```

### Step 2: Modify

```java
// package 声明无需修改（路径相同）
package grp.pt.constant;
```

### Step 3: Write

```
写入文件: grp-common-element/src/main/java/grp/pt/constant/ElementConstant.java
```

### Step 4: Grep

```
搜索: import grp.pt.constant.ElementConstant
结果: 
  - element-service/.../XxxService.java (3处)
  - element-controller/.../XxxController.java (1处)
```

### Step 5: Edit

```
import 路径未变，无需修改引用文件
```

### Step 6: Delete

```
删除: grp-capability-element/element-service/src/main/java/grp/pt/constant/ElementConstant.java
```

### Step 7: Verify

```
检查 import 引用正确 → OK
检查 package 声明一致 → OK
```

---

## 示例二：迁移 util/DateUtil.java

### Step 1: Read

```
读取文件: grp-capability-element/element-service/src/main/java/grp/pt/util/DateUtil.java
```

### Step 2: Modify

```java
// package 声明无需修改
package grp.pt.util;
```

### Step 3: Write

```
写入文件: grp-common-element/src/main/java/grp/pt/util/DateUtil.java
```

### Step 4: Grep

```
搜索: import grp.pt.util.DateUtil
结果:
  - element-service/.../XxxService.java (5处)
  - element-service/.../YyyService.java (3处)
  - element-controller/.../ZzzController.java (2处)
  - element-controller/.../AaaFeignController.java (1处)
```

### Step 5: Edit

```
import 路径未变，无需修改引用文件
```

### Step 6: Delete

```
删除: grp-capability-element/element-service/src/main/java/grp/pt/util/DateUtil.java
```

### Step 7: Verify

```
检查 import 引用正确 → OK
检查 package 声明一致 → OK
```

---

## 示例三：POM 依赖调整

### element-service pom.xml 添加依赖

```xml
<dependency>
    <groupId>grp</groupId>
    <artifactId>grp-common-element</artifactId>
    <version>${revision}</version>
</dependency>
```

### element-controller pom.xml 添加依赖

```xml
<dependency>
    <groupId>grp</groupId>
    <artifactId>grp-common-element</artifactId>
    <version>${revision}</version>
</dependency>
```

### grp-common-element pom.xml 添加第三方依赖

```xml
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
```

---

## 迁移完成报告示例

### 迁移统计

| 包名 | 迁移文件数 | 保留文件数 |
|------|-----------|-----------|
| constant/ | 1 | 0 |
| enums/ | 1 | 0 |
| exception/ | 1 | 0 |
| util/ | 15 | 6 |
| cache/ | 3 | 4 |
| config/ | 1 | 2 |
| **合计** | **22** | **12** |

### POM 变更

| 模块 | 变更 |
|------|------|
| element-service/pom.xml | 添加 grp-common-element 依赖 |
| element-controller/pom.xml | 添加 grp-common-element 依赖 |
| grp-common-element/pom.xml | 创建并添加 commons-lang3、jackson 依赖 |
| element-module/pom.xml | modules 中确认包含 grp-common-element |

