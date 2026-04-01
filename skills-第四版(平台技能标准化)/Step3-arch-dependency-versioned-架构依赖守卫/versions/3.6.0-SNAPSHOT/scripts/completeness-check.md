# 修复完整性校验清单

修复全部 FAIL 项后，**必须按以下顺序执行完整性校验**。校验不通过则修复后重新校验，直到全部通过。

> ⚠️ **强制规定**：每完成一批（3~5个）FAIL 项修复后，**必须立即执行 V-03 + V-04** 编译验证，不得等到全部修复完成后才编译。历史教训：接口方法缺失若积累过多，编译错误数量会爆炸式增长，排查成本极高。

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
2. **Grep 搜索所有调用该接口字段的 Controller 文件**（不仅是当前修改的文件）：
   ```
   Grep pattern: {fieldName}\.
   Grep path: {controller-module-src-path}
   ```
3. 逐一比对方法调用是否在接口方法列表中
4. 缺失的方法 → **读取 Impl 类中该方法的完整签名**（含返回类型、参数列表、throws 声明）→ 在接口中追加声明
5. **特别注意重载方法**：同名但参数不同的方法（如 `checkUniqueLoginSn(String)` 和 `checkUniqueLoginSn(String, Integer)`）必须在接口中分别声明

**通过标准**：Controller 中调用的所有方法在接口中均有声明（包括所有重载变体）。

**常见遗漏场景**：
- 带额外参数的重载方法（如带 `auditStatus`、`tenantId` 的重载版本）
- 多个 Controller 引用同一接口，只检查了部分 Controller
- ServiceImpl 实现了多个接口，Controller 调用的方法分布在不同接口中

---

## 校验 V-04：编译验证（每批修复后必须执行）

**执行时机**：
- **每完成 3~5 个 FAIL 项修复后立即执行**（不得延后到全部修复完再执行）
- 全部 FAIL 项修复完毕后执行最终编译验证

**操作**：

```bash
# 标准编译命令（直接通过 Bash 工具执行，不使用 PowerShell 管线）
cd {capability-framework-path}
mvn compile -pl framework-controller -am
```

**说明**：Qoder 的 Bash 工具会自动捕获命令输出。直接在输出中查找 `BUILD SUCCESS` 或 `ERROR` 关键词判断结果。禁止使用 PowerShell 的 `Out-File`/`Get-Content`/`Select-String`（可能导致中文编码损坏）。详见 [shared/encoding-guard.md](../../../../shared/encoding-guard.md) 规则二和规则四。

**通过标准**：输出中包含 `BUILD SUCCESS`，且不含 `ERROR` 行

**若不通过，按以下步骤排查**：
1. 查看完整错误文件，找出所有 `找不到符号` 的方法名和所在类
2. 对每个缺失方法：在 Impl 类中搜索该方法的完整签名（`Grep pattern: public.*{方法名}`）
3. 将方法签名追加到对应接口中（注意保持返回类型、参数类型、throws 完全一致）
4. 修复后**必须重新执行 V-01 和 V-02**，确认修复过程未引入新的违规
5. 再次编译验证直到 BUILD SUCCESS

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

## 校验 V-07：文件结构完整性（花括号配对）

**操作**：对每个在本次修复中修改过的 Java 文件，检查花括号 `{` 和 `}` 的配对完整性。

执行步骤：
1. 对每个修改过的文件，统计文件中所有 `{` 和 `}` 的数量（排除字符串字面量和注释中的花括号）
2. 确认 `{` 数量 == `}` 数量
3. 确认文件的最后一个非空行为类定义的闭合 `}`（对于顶级类文件）
4. 特别检查文件末尾是否出现多余的 `}` 花括号

**通过标准**：
- 每个文件的 `{` 与 `}` 数量相等
- 文件末尾不存在多余的闭合花括号
- 文件末尾结构为：`最后一个方法的 }` → `类定义的 }`（中间可有空行）

**常见问题**：
- 修复过程中 Edit 操作时意外在文件末尾追加了多余的 `}`
- 删除方法或字段时遗漏了对应的 `}`
- 多次 Edit 操作导致花括号层级错乱

**若不通过**：立即修复多余或缺失的花括号，确保文件结构完整后重新校验。

---

## 校验执行时机

| 时机 | 执行校验项 | 说明 |
|------|-----------|------|
| 每完成 3~5 个 FAIL 项修复 | **V-03 + V-04**（强制） | 接口方法完整性 + 编译验证，不得跳过 |
| 每完成一个 S1-03 批次 | V-01、V-02（快速检查） | 确认无 ServiceImpl 和 DAO 残留 |
| 全部 FAIL 项修复完毕 | V-01 ~ V-07（完整校验） | 最终完整验证 |
| 编译错误修复后 | V-01、V-02（回归检查） | 确认修复未引入新违规 |
| 每个文件修改完成后 | V-07（即时检查） | 确认文件结构未被破坏 |
