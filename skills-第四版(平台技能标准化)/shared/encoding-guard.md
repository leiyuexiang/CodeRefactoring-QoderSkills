# 全局编码防护规范（Windows 环境）

## 背景

在中文 Windows 系统上，PowerShell 默认使用系统代码页（GBK/CP936），而非 UTF-8。当 Qoder 的 Bash 工具通过 PowerShell 执行命令时，存在以下编码损坏链路：

```
Java 源文件 (UTF-8)
  → PowerShell 按 GBK 解码输出流
  → Qoder 按 UTF-8 读取该输出
  → 中文字符被双重编码，产生乱码
```

**本规范定义事前防护措施，消除编码损坏发生的条件，而非事后修复。**

---

## 规则一：工具优先原则（强制）

**核心思想**：凡是 Qoder 内置工具能做的事，禁止通过 Bash 命令执行。内置工具直接操作文件字节流，不经过 PowerShell 的编码管线。

### 工具替代清单

| 需求 | 禁止使用 | 必须使用 | 原因 |
|------|---------|---------|------|
| 搜索文件内容 | `grep`、`findstr` | **Grep 工具** | Grep 工具直接读取文件字节，不经过 PowerShell 编码转换 |
| 读取文件内容 | `cat`、`type`、`Get-Content` | **Read 工具** | Read 工具直接读取原始字节 |
| 修改文件内容 | `sed`、`awk`、PowerShell 文本替换 | **Edit 工具** | Edit 工具进行二进制精确替换，自动保留原文件编码（含 BOM） |
| 创建新文件 | `echo >`、`Out-File`、`Set-Content` | **Write 工具** | Write 工具直接写入 UTF-8 字节 |
| 查找文件路径 | `find`、`dir /s`、`Get-ChildItem -Recurse` | **Glob 工具** | Glob 工具在文件系统层面匹配，不涉及文本编码 |
| 删除文件 | `del`、`Remove-Item` | **DeleteFile 工具** | DeleteFile 工具直接操作文件系统 |

### 允许通过 Bash 执行的操作（白名单）

以下操作为**二进制安全**操作，不涉及文本内容的编码转换，可以安全通过 Bash 执行：

| 操作 | 命令 | 安全原因 |
|------|------|---------|
| 文件复制 | `copy`(Windows) / `cp`(Linux) | 二进制拷贝，不解析内容 |
| 文件/目录移动 | `move`(Windows) / `mv`(Linux) | 仅修改文件系统元数据，不接触文件内容 |
| 目录重命名 | `ren`(Windows) / `mv`(Linux) | 同上 |
| 创建目录 | `mkdir` | 不涉及文件内容 |
| 删除空目录 | `rmdir`(Windows) / `rmdir`(Linux) | 不涉及文件内容 |
| Maven 编译 | `mvn compile` | 编译结果仅作为成功/失败判断，不写入源文件 |

---

## 规则二：Bash 命令编码隔离（强制）

当确实需要通过 Bash 执行**文本敏感**命令（如 Maven 编译输出分析、日志检查等），**必须**在命令前添加编码初始化前缀：

### Windows 编码初始化命令

```bash
chcp 65001 >nul 2>&1 && [Console]::OutputEncoding = [System.Text.Encoding]::UTF8; 实际命令
```

**分段解释**：
- `chcp 65001 >nul 2>&1` — 将控制台代码页切换为 UTF-8（65001）
- `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8` — 设置 PowerShell 输出流编码为 UTF-8

### 典型场景：Maven 编译输出分析

```bash
# 错误示例（可能产生乱码）：
mvn compile 2>&1 | Out-File -FilePath ".\compile_result.txt" -Encoding utf8

# 正确示例：
chcp 65001 >nul 2>&1 && mvn compile -pl framework-controller -am
```

**说明**：
- Maven 编译命令的输出直接由 Qoder 的 Bash 工具捕获即可
- 不需要重定向到文件再读取（避免中间文件的编码问题）
- 如果编译输出中的中文出现乱码，**不影响判断**（只需匹配 `BUILD SUCCESS` 或 `ERROR` 即可）

---

## 规则三：文件操作编码保全矩阵（强制）

### 操作分类

根据操作对文件内容的影响，分为三类：

| 类别 | 定义 | 编码风险 | 应对策略 |
|------|------|---------|---------|
| A 类：不接触内容 | copy、mv、mkdir、rmdir、rename | 零风险 | 直接使用 Bash |
| B 类：只读内容 | 搜索、查看、分析 | 零风险（不写回） | 使用 Grep/Read 工具 |
| C 类：修改内容 | 替换文本、更新路径、修改声明 | **高风险** | 使用 Edit 工具 |

### 各 Step 典型操作的分类

| 操作 | 类别 | 工具选择 |
|------|------|---------|
| 移动模块目录（Step1/Step2） | A | Bash `move`/`mv` |
| 复制文件到 common 模块（Step8） | A | Bash `copy`/`cp` |
| 搜索旧 import 残留（Step2 验证） | B | Grep 工具 |
| 搜索架构违规（Step3 检查） | B | Grep 工具 |
| 搜索文件归属判定（Step4-8 检查） | B | Grep 工具 |
| 修改 pom.xml artifactId（Step2） | C | Edit 工具 |
| 修改 package 声明（Step2/4/5/6/8） | C | Edit 工具 |
| 修改 import 语句（全部 Step） | C | Edit 工具 |
| 修改类名/文件名（Step7） | A+C | Bash `mv` 重命名文件 + Edit 修改类名声明 |

---

## 规则四：禁止 PowerShell 特有的文件操作命令（强制）

以下 PowerShell cmdlet 会隐式进行编码转换，**严禁在技能执行中使用**：

| 禁止命令 | 风险说明 | 替代方案 |
|----------|---------|---------|
| `Get-Content` | 默认按系统代码页解码，中文可能乱码 | 使用 Read 工具 |
| `Set-Content` | 默认以系统代码页写入，破坏 UTF-8 | 使用 Write 或 Edit 工具 |
| `Out-File` | 即使指定 `-Encoding utf8`，也会添加 BOM（PowerShell 5.x） | 使用 Write 工具 |
| `Add-Content` | 追加内容时可能混合编码 | 使用 Edit 工具 |
| `Select-String` | 输出格式化文本时可能乱码 | 使用 Grep 工具 |
| `[IO.File]::ReadAllText()` | 需显式指定编码，默认 UTF-8 但 PowerShell 5.x 行为不一致 | 使用 Read 工具 |

### 唯一例外

`Get-Content -Encoding Byte -TotalCount 3` — 用于检测 BOM 标记（读取原始字节，不涉及文本编码转换）。此命令仅在需要确认文件 BOM 状态时使用。

---

## 规则五：编码验证后置检查（建议）

在每个 Step 完成文件修改操作后，对修改过的文件进行编码完整性抽检：

### 检查方法

```
对每个被修改的 Java 文件，使用 Read 工具读取文件内容，确认：
1. 中文注释未出现乱码（如 "é¢å" 等多字节碎片）
2. package 声明行格式正确
3. import 语句行格式正确
```

### 乱码特征识别

如果 Read 工具读取的文件内容出现以下特征，说明编码已被破坏：

| 特征 | 原因 | 示例 |
|------|------|------|
| 中文变成问号 `???` | 编码完全丢失 | `// ???` |
| 中文变成方块 `□□□` | Unicode 替换字符 | `// □□□` |
| 中文变成拉丁乱码 `é¢å` | UTF-8 被 GBK 误读 | `// é¢åç´ ` |
| 中文变成双倍长度乱码 | GBK 被 UTF-8 误读再写回 | `// 鍏冪礌` |

---

## 规则六：Step2 验证命令替代（强制）

Step2 的验证阶段使用了 Bash `grep` 命令检查残留引用，**必须替换为 Grep 工具**：

```
# 旧方式（禁止）：
grep -r "{module}-server</artifactId>" --include="pom.xml"
grep -r "import.*{module}.server.com." --include="*.java"

# 新方式（强制）：
Grep 工具: pattern="{module}-server</artifactId>", glob="**/pom.xml"
Grep 工具: pattern="import.*{module}\.server\.com\.", glob="**/*.java"
```

---

## 规则七：Step3 编译验证替代（强制）

Step3 使用了 PowerShell 命令进行编译验证，**必须简化为直接 Bash 调用**：

```
# 旧方式（禁止）：
mvn compile -pl framework-controller -am 2>&1 | Out-File -FilePath ".\compile_result.txt" -Encoding utf8
Get-Content ".\compile_result.txt" | Select-String "ERROR|BUILD SUCCESS"

# 新方式（强制）：
# 1. 直接通过 Bash 工具执行 Maven 编译
mvn compile -pl framework-controller -am

# 2. Qoder 的 Bash 工具会自动捕获输出
# 3. 直接在输出中搜索 BUILD SUCCESS 或 ERROR 关键词判断结果
# 4. 不需要重定向到中间文件
```

---

## 执行检查清单

在每个 Step 开始执行前，确认以下条件：

```
□ 确认当前操作系统（Windows/Linux/Mac）
□ 所有文件搜索操作使用 Grep/Glob 工具（非 Bash grep/find）
□ 所有文件读取操作使用 Read 工具（非 Bash cat/type/Get-Content）
□ 所有文件修改操作使用 Edit 工具（非 Bash sed/awk/PowerShell替换）
□ 仅 A 类操作（copy/mv/mkdir/rmdir）通过 Bash 执行
□ 无 PowerShell 文件操作 cmdlet（Get-Content/Set-Content/Out-File）
```

---

## 引用方式

各 Step 技能在 REFERENCE.md 或 SKILL.md 中添加以下引用：

```
## 编码防护规范（强制前置）

在执行任何检查或修复操作之前，必须读取并遵守全局编码防护规范：
→ [shared/encoding-guard.md](../../shared/encoding-guard.md)

该规范定义了 Windows 环境下防止中文编码被 PowerShell 破坏的事前防护措施。
```
