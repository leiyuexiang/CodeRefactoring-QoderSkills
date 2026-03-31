# 模块划分规范检查规则

> 检查业务模块的划分、子模块组织、模块内部结构是否符合分包规范。

## 规则清单

### RULE-MD01: 模块内部目录完整性检查

**级别**: ERROR  
**描述**: 每个业务模块必须包含标准的内部目录结构。

```
标准结构:
modules/{module-name}/
├── views/             # 必须：页面视图
├── components/        # 可选：模块内组件
├── composables/       # 可选：模块内组合函数
├── api/               # 必须：API 接口定义
├── types/             # 必须：类型定义
└── index.ts           # 必须：模块导出入口

检查方式:
1. 扫描 modules/ 下的每个子目录
2. 检查是否存在 views/、api/、types/ 三个必须目录
3. 检查是否存在 index.ts 导出入口
4. 如果有 .vue 文件不在 views/ 或 components/ 中则报错

合规示例: modules/finance/views/PaymentPlanList.vue
违规示例: modules/finance/PaymentPlanList.vue（视图文件未放入 views/）
```

### RULE-MD02: 模块大小检查

**级别**: WARNING  
**描述**: 单个模块的 views/ 文件数不应超过 10 个，超过时应拆分子模块。

```
规范:
- views/ 下的 .vue 文件数量 ≤ 10 → 合规
- views/ 下的 .vue 文件数量 > 10 → 建议拆分为子模块
- views/ 下的 .vue 文件数量 > 20 → 必须拆分

检查方式:
1. 统计每个模块 views/ 下的 .vue 文件数量
2. 超过 10 个发出 WARNING
3. 超过 20 个发出 ERROR

合规示例:
modules/finance/
├── payment-plan/      # 子模块1（5个视图）
├── budget/            # 子模块2（4个视图）
└── index.ts

违规示例:
modules/business/views/  # 30+ 个视图文件混杂在一起
```

### RULE-MD03: 模块命名规范检查

**级别**: ERROR  
**描述**: 模块目录名必须使用 kebab-case，且具有业务语义。

```
规范:
- 使用 kebab-case: payment-plan, message-center, data-quality
- 禁止使用 camelCase: paymentPlan ❌
- 禁止使用 PascalCase: PaymentPlan ❌
- 禁止使用 snake_case: payment_plan ❌
- 禁止使用无语义命名: module, module1, page, page2 ❌

检查方式:
1. 扫描 modules/ 下的目录名
2. 检查是否为 kebab-case 格式
3. 检查是否含有数字后缀（module1, page2）
4. 检查是否有通用无语义命名

合规示例: scheduled-task, system-maintenance
违规示例: module, scheduledTask, ScheduledTask, scheduled_task
```

### RULE-MD04: 模块导出入口检查

**级别**: ERROR  
**描述**: 每个模块/子模块必须有 index.ts 导出入口，且导出内容完整。

```
规范:
index.ts 应导出:
- 所有页面视图组件（通过 default export 的 re-export）
- 核心 Composable 函数
- 类型定义（type export）

检查方式:
1. 检查模块根目录是否存在 index.ts
2. 检查 index.ts 是否导出了 views/ 中的组件
3. 检查 index.ts 是否导出了 types/

合规示例:
// modules/finance/index.ts
export { default as PaymentPlanList } from './views/PaymentPlanList.vue'
export { usePaymentPlan } from './composables/use-payment-plan'
export type { PaymentPlan } from './types/payment-plan'

违规示例:
// 空的 index.ts 或缺少 index.ts
```

### RULE-MD05: API 与 Types 对应检查

**级别**: WARNING  
**描述**: api/ 和 types/ 目录中的文件应一一对应。

```
规范:
- api/payment-plan.ts ↔ types/payment-plan.ts
- api/ 中的接口函数必须引用 types/ 中定义的类型
- 类型定义不应直接写在 api/ 文件中

检查方式:
1. 列出 api/ 和 types/ 中的文件名
2. 检查是否存在对应关系
3. 检查 api/ 文件中的 import 是否引用了对应 types/ 文件

合规示例:
api/payment-plan.ts → import type { PaymentPlan } from '../types/payment-plan'
types/payment-plan.ts → export interface PaymentPlan { ... }

违规示例:
api/payment-plan.ts 中直接定义 interface PaymentPlan（未放入 types/）
```

### RULE-MD06: 子模块结构检查

**级别**: WARNING  
**描述**: 业务域中的子模块应遵循统一的结构规范。

```
规范:
modules/{domain}/
├── {sub-module-a}/        # 子模块 A
│   ├── views/
│   ├── api/
│   ├── types/
│   └── index.ts
├── {sub-module-b}/        # 子模块 B
│   └── ...
├── shared/                # 域内共享资源（可选）
│   ├── components/
│   ├── composables/
│   └── types/
└── index.ts               # 域导出入口

检查方式:
1. 如果 modules/{domain}/ 下同时存在子目录和 views/ 目录，则为混合模式 → WARNING
2. 域导出入口 index.ts 应汇总导出所有子模块
3. 域内共享资源应放在 shared/ 而非域根目录

合规示例:
modules/finance/payment-plan/views/PaymentPlanList.vue
modules/finance/shared/components/AmountDisplay.vue

违规示例:
modules/finance/views/PaymentPlanList.vue     ← 域目录下直接放 views
modules/finance/payment-plan/views/...        ← 同时又有子模块（混合模式）
```

### RULE-MD07: 模块间依赖方向检查

**级别**: ERROR  
**描述**: 模块之间的引用必须通过共享层中转，禁止直接引用其他模块内部文件。

```
合法引用:
modules/A → components/ ✅
modules/A → composables/ ✅
modules/A → services/ ✅
modules/A → utils/ ✅
modules/A → modules/A/子路径 ✅
modules/A → modules/B (仅 index.ts 导出的类型) ✅

非法引用:
modules/A → modules/B/components/ ❌
modules/A → modules/B/composables/ ❌
modules/A → modules/B/api/ ❌
modules/A → modules/B/views/ ❌

检查方式:
1. 扫描模块内所有文件的 import 语句
2. 解析引用路径的所属模块
3. 如果引用了其他模块的非 index.ts 导出路径，报错
4. 建议将被跨模块引用的资源提升到 components/ 或 composables/ 共享层

修复建议:
- 将被多个模块使用的组件提取到 components/business/
- 将被多个模块使用的 Composable 提取到 src/composables/
- 将被多个模块使用的类型提取到 src/types/
```

### RULE-MD08: 路由文件与模块对应检查

**级别**: WARNING  
**描述**: 每个业务模块应有对应的路由配置文件。

```
规范:
framework/router/modules/{module-name}.ts ↔ modules/{module-name}/

检查方式:
1. 列出 modules/ 下的所有模块名
2. 检查 framework/router/modules/ 中是否有对应的路由文件
3. 检查路由文件中的 component 引用是否指向正确的模块视图

合规示例:
modules/finance/ → framework/router/modules/finance.ts
finance.ts 中: component: () => import('@/modules/finance/views/PaymentPlanList.vue')

违规示例:
modules/finance/ 存在但无对应的 router/modules/finance.ts
```

### RULE-MD09: Store 与模块对应检查

**级别**: SUGGESTION  
**描述**: 需要跨页面共享状态的模块应有对应的 Store 文件。

```
规范:
- Store 仅存放跨页面共享的状态
- 页面级状态使用模块内 Composable
- Store 文件与模块名对应: framework/store/modules/{module-name}.ts

检查方式:
1. 扫描 modules/ 中使用了 defineStore 的文件
2. 检查 Store 定义是否在 framework/store/modules/ 中
3. 不在标准位置的 Store 定义发出建议

合规示例: framework/store/modules/finance.ts → useFinanceStore
违规示例: modules/finance/store.ts（Store 定义在模块内部）
```

### RULE-MD10: 视图文件命名规范检查

**级别**: ERROR  
**描述**: views/ 中的页面组件文件必须使用 PascalCase，并遵循命名规则。

```
命名规则:
- 列表页: {ModuleName}List.vue
- 详情页: {ModuleName}Detail.vue
- 编辑页: {ModuleName}Edit.vue
- 新增页: {ModuleName}Create.vue（或复用 Edit）
- 概览页: {ModuleName}Overview.vue

检查方式:
1. 扫描 views/ 下的 .vue 文件名
2. 检查是否为 PascalCase
3. 检查是否以标准后缀结尾（List, Detail, Edit, Create, Overview）

合规示例: PaymentPlanList.vue, BudgetDetail.vue
违规示例: paymentPlanList.vue, payment-plan-list.vue, list.vue
```

## 检查脚本伪代码

```javascript
function checkModuleDivision(projectRoot) {
  const results = [];
  const modulesPath = `${projectRoot}/src/modules`;
  const modules = listDirs(modulesPath);

  for (const moduleName of modules) {
    const modulePath = `${modulesPath}/${moduleName}`;

    // RULE-MD01: 内部目录完整性
    const requiredDirs = ['views', 'api', 'types'];
    for (const dir of requiredDirs) {
      if (!exists(`${modulePath}/${dir}`)) {
        // 检查是否为域模块（含子模块）
        const subDirs = listDirs(modulePath);
        const hasSubModules = subDirs.some(d => exists(`${modulePath}/${d}/views`));
        if (!hasSubModules) {
          results.push({
            rule: 'RULE-MD01',
            level: 'ERROR',
            message: `模块 ${moduleName} 缺少必要目录: ${dir}/`,
          });
        }
      }
    }
    if (!exists(`${modulePath}/index.ts`)) {
      results.push({
        rule: 'RULE-MD04',
        level: 'ERROR',
        message: `模块 ${moduleName} 缺少导出入口 index.ts`,
      });
    }

    // RULE-MD02: 模块大小
    const viewFiles = glob(`${modulePath}/views/*.vue`);
    if (viewFiles.length > 20) {
      results.push({
        rule: 'RULE-MD02',
        level: 'ERROR',
        message: `模块 ${moduleName} 视图文件数 (${viewFiles.length}) 超过 20，必须拆分子模块`,
      });
    } else if (viewFiles.length > 10) {
      results.push({
        rule: 'RULE-MD02',
        level: 'WARNING',
        message: `模块 ${moduleName} 视图文件数 (${viewFiles.length}) 超过 10，建议拆分子模块`,
      });
    }

    // RULE-MD03: 模块命名
    if (!isKebabCase(moduleName)) {
      results.push({
        rule: 'RULE-MD03',
        level: 'ERROR',
        message: `模块名 "${moduleName}" 不符合 kebab-case 规范`,
      });
    }
    if (/^(module|page|view)\d*$/.test(moduleName)) {
      results.push({
        rule: 'RULE-MD03',
        level: 'ERROR',
        message: `模块名 "${moduleName}" 缺乏业务语义，请使用业务域名称`,
      });
    }

    // RULE-MD10: 视图文件命名
    for (const viewFile of viewFiles) {
      const fileName = getFileName(viewFile).replace('.vue', '');
      if (!isPascalCase(fileName)) {
        results.push({
          rule: 'RULE-MD10',
          level: 'ERROR',
          file: viewFile,
          message: `视图文件名 "${fileName}.vue" 不符合 PascalCase 规范`,
        });
      }
    }
  }

  // RULE-MD07: 模块间依赖
  for (const moduleName of modules) {
    const moduleFiles = glob(`${modulesPath}/${moduleName}/**/*.{ts,vue}`);
    for (const file of moduleFiles) {
      const imports = extractImports(file);
      for (const imp of imports) {
        const importedModule = parseModuleName(imp);
        if (importedModule && importedModule !== moduleName) {
          // 检查是否引用了其他模块的内部路径
          if (!imp.endsWith('/index') && !imp.match(/\/modules\/[^/]+$/)) {
            results.push({
              rule: 'RULE-MD07',
              level: 'ERROR',
              file,
              message: `非法跨模块引用: ${imp}，请通过共享层或 index.ts 导出中转`,
            });
          }
        }
      }
    }
  }

  return results;
}
```
