# 修复完整性校验清单

修复全部 FAIL 项后，**必须按以下顺序执行完整性校验**。校验不通过则修复后重新校验，直到全部通过。

---

## 校验 V-01：Controller 层 ServiceImpl 残留搜索

**操作**：在 Controller 模块目录下执行 Grep 搜索

```
Grep pattern: import.*service\.impl\.
Grep path: {controller-module-path}
```

**通过标准**：搜索结果为 0 条（或仅剩已标记为 SKIP 的项）

**若不通过**：找到遗漏的 ServiceImpl 引用，按 S1-03 修复规范处理后重新校验。

---

## 校验 V-02：Controller 层 DAO 残留搜索

**操作**：在 Controller 模块目录下执行 Grep 搜索

```
Grep pattern: import.*\.dao\.
Grep path: {controller-module-path}
```

**通过标准**：搜索结果为 0 条（或仅剩 Controller 模块自身的 DAO，如 config2/agent 下的内部 DAO）

**若不通过**：找到遗漏的 DAO 引用，按 S1-02 修复规范处理后重新校验。

---

## 校验 V-03：接口方法完整性校验

**操作**：对每个被替换为接口注入的字段，检查 Controller 中所有对该字段的方法调用是否在接口中有声明。

执行步骤：
1. 对每个新建/修改的接口文件，列出所有已声明的方法名
2. 在引用该接口的 Controller 中，Grep 搜索 `{fieldName}\.` 获取所有方法调用
3. 逐一比对方法调用是否在接口方法列表中
4. 缺失的方法 → 在 Impl 类中查找方法签名 → 在接口中追加声明

**通过标准**：Controller 中调用的所有方法在接口中均有声明。

---

## 校验 V-04：编译验证

**操作**：

```bash
mvn compile -pl {service-module},{controller-module} -am -T 4
```

**通过标准**：BUILD SUCCESS

**若不通过**：
1. 分析编译错误类型（找不到符号、语法错误等）
2. 修复编译错误
3. 修复后**必须重新执行 V-01 和 V-02**，确认修复过程未引入新的违规

---

## 校验 V-05：全工程打包验证

**操作**：

```bash
mvn package -DskipTests -T 4
```

**通过标准**：全部模块 BUILD SUCCESS

---

## 校验 V-06：AI 标记完整性

**操作**：搜索所有 `@AI-Begin` 标记，确认每个有对应的 `@AI-End`

```
Grep pattern: @AI-Begin
Grep path: {module-root-path}
```

```
Grep pattern: @AI-End
Grep path: {module-root-path}
```

**通过标准**：`@AI-Begin` 数量 == `@AI-End` 数量

---

## 校验执行时机

| 时机 | 执行校验项 |
|------|-----------|
| 每完成一个 FAIL 项修复 | V-01、V-02（快速检查） |
| 全部 FAIL 项修复完毕 | V-01 ~ V-06（完整校验） |
| 编译错误修复后 | V-01、V-02（回归检查） |
