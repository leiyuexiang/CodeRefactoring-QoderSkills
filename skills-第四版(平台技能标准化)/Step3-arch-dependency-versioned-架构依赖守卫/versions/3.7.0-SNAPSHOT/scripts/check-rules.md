# S1 检查规则清单 - 3.7.0-SNAPSHOT

> 基于 3.6.0-SNAPSHOT 基线版本。**TODO**: 请在此补充 3.7.0 版本的规则差异。

## S1-01：Controller→Controller 直接依赖

**判定标准**：
- Controller 类中存在 `import xxx.controller.XxxController` → **FAIL**
- Controller 类中存在 `@Autowired XxxController` → **FAIL**
- Controller 方法中调用 `xxxController.someMethod()` → **FAIL**

---

## S1-02：Controller 直接依赖 DAO/Mapper

**判定标准**：
- Controller 类中存在 `@Autowired XxxDao xxxDao` → **FAIL**
- Controller 类中存在 `@Autowired XxxMapper xxxMapper` → **FAIL**

---

## S1-03：Controller 注入 ServiceImpl 而非 Service 接口

**判定标准**：
- Controller 类中存在 `@Autowired XxxServiceImpl` → **FAIL**
- Controller 类中存在 `import xxx.service.impl.XxxServiceImpl` → **FAIL**

---

## S1-04：Entity 泄露到 Controller 层

**判定标准**：
- Controller 方法返回类型为 Entity 类 → **WARN**
- Controller 方法参数类型为 Entity 类 → **WARN**

---

## S1-05：跨模块直接类引用

**判定标准**：
- 跨模块引用非 Feign/API 接口的类 → **WARN**
