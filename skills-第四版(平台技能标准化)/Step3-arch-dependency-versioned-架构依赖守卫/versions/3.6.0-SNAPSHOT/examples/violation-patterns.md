# 代码违规模式与修复前后对比示例

## 一、Controller→Controller 依赖

### 违规代码

```java
@RestController
public class AController {
    @Autowired
    private BController bController;  // Controller 注入另一个 Controller

    public ReturnData someMethod() {
        return bController.doSomething();  // 直接调用另一个 Controller
    }
}
```

### 修复后（策略 A：替换为 Service 接口注入，最小改动）

```java
@RestController
public class AController {
    @Autowired
    private IXxxService xxxService;  // 改为注入 Service 接口

    public ReturnData someMethod() {
        return xxxService.doSomething();  // 通过 Service 层完成业务逻辑
    }
}
```

### 修复后（策略 B：提取 Private Helper 方法）

适用于被调用 Controller 方法包含复合逻辑（Session 处理、多 Service 编排等）：

1. 在当前 Controller 中创建 private 方法
2. 将被调用 Controller 方法的核心逻辑复制到 private 方法中
3. 将 Controller 调用替换为 Service 调用
4. 移除对被调用 Controller 的 `@Autowired` 注入

---

## 二、Controller 直接依赖 DAO/Mapper

### 违规代码

```java
@RestController
public class XxxController {
    @Autowired
    private XxxDao xxxDao;  // 直接注入 DAO

    public ReturnData method() {
        return xxxDao.query(param);  // 跳过 Service 层直接操作 DAO
    }
}
```

### 修复后

```java
// 1. 新建 DelegateService 接口（方法签名与 DAO 原方法一致）
public interface IXxxDelegateService {
    /**
     * 查询数据
     */
    ReturnType query(ParamType param);
}

// 2. 新建 DelegateServiceImpl（纯转发，不含业务逻辑）
@Service
public class XxxDelegateServiceImpl implements IXxxDelegateService {
    @Autowired
    private XxxDao xxxDao;

    @Override
    public ReturnType query(ParamType param) {
        return xxxDao.query(param);  // 纯转发，方法名和参数不变
    }
}

// 3. 修改 Controller（注入 DelegateService 接口，调用方法名不变）
@RestController
public class XxxController {
    @Autowired
    private IXxxDelegateService xxxDelegateService;  // 改为注入 DelegateService 接口

    public ReturnData method() {
        return xxxDelegateService.query(param);  // 方法名与 DAO 原方法一致
    }
}
```

---

## 三、Controller 注入 ServiceImpl 而非接口

### 违规代码

```java
@RestController
public class XxxController {
    @Autowired
    private XxxServiceImpl xxxService;  // 直接注入实现类
}
```

### 修复后

```java
@RestController
public class XxxController {
    @Autowired
    private IXxxService xxxService;  // 改为注入 Service 接口
}
```

修改要点：
- 字段类型从 `XxxServiceImpl` 改为 `IXxxService`（或 `XxxService`）
- import 从 `import xxx.service.impl.XxxServiceImpl` 改为 `import xxx.service.IXxxService`
- 如果不存在 Service 接口 → 先提取接口，再修改注入

---

## 四、Entity 泄露到 Controller 层

### 违规代码

```java
@RestController
public class XxxController {
    @GetMapping("/detail")
    public XxxEntity getDetail(Long id) {  // 直接返回 Entity
        return xxxService.getById(id);
    }
}
```

### 修复后

```java
@RestController
public class XxxController {
    @GetMapping("/detail")
    public XxxVO getDetail(Long id) {  // 改为返回 VO
        return xxxService.getDetailVO(id);  // Service 层负责 Entity→VO 转换
    }
}
```

修复要点：
- **返回值泄露**：在 Service 层添加 Entity→DTO/VO 转换
- **参数泄露**：创建对应的 DTO/Query 对象替换
- 如果项目约定 Entity 直接作为 DTO 使用（如继承自 HashMap），标记为 WARN 但不强制修改
