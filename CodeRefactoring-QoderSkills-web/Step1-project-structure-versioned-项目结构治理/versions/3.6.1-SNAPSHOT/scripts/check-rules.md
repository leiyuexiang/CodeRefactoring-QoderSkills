# 项目结构规范检查规则

> 检查 Vue 3 项目的目录结构、文件组织是否符合标准化分包规范。

## 规则清单

### RULE-PS01: 顶层目录完整性检查

**级别**: ERROR  
**描述**: src/ 下必须包含标准的顶层目录结构。

```
必须存在的目录:
- src/assets/           # 静态资源层
- src/components/       # 共享组件层
- src/modules/          # 业务模块层
- src/framework/        # 框架层
- src/services/         # 服务层
- src/utils/            # 工具函数层
- src/types/            # 全局类型定义

必须存在的文件:
- src/App.vue           # 根组件
- src/main.ts           # 应用入口

检查方式:
1. 扫描 src/ 下的目录列表
2. 核对以上 7 个目录是否存在
3. 核对入口文件是否存在

合规示例: src/ 包含 assets, components, modules, framework, services, utils, types
违规示例: src/ 下直接放置 views/, api/ 等未归类目录
```

### RULE-PS02: assets 目录结构检查

**级别**: WARNING  
**描述**: 静态资源目录应按职责划分子目录。

```
标准结构:
src/assets/
├── styles/              # 全局样式
│   ├── variables.scss   # 变量定义（必须）
│   ├── index.scss       # 入口文件（必须）
│   └── ...
├── icons/               # 图标资源
└── images/              # 图片资源

检查方式:
1. 检查 assets/ 下是否有 styles/ 目录
2. 检查 styles/ 下是否有 variables.scss 和 index.scss
3. 检查是否有未归类的文件直接放在 assets/ 根目录

违规示例: assets/ 下直接放 .scss 文件而非放入 styles/
```

### RULE-PS03: components 分层检查

**级别**: ERROR  
**描述**: 共享组件目录必须分为 common/layout/business 三层。

```
标准结构:
src/components/
├── common/              # 通用组件（与业务无关）
├── layout/              # 布局组件（页面骨架）
└── business/            # 业务组件（跨模块共享）

检查方式:
1. 扫描 components/ 下的子目录
2. 检查是否存在 common/、layout/、business/ 三个分层目录
3. 检查是否有组件文件直接放在 components/ 根目录（应放入对应分层）
4. 检查 common/ 中的组件是否不含业务逻辑引用
5. 检查 layout/ 中的组件是否仅涉及页面结构

合规示例: components/common/DataTable/, components/layout/OperationBar.vue
违规示例: components/DataTable.vue（未分层）, components/common/AuditLog/（含业务逻辑应放 business/）
```

### RULE-PS04: 复合组件目录检查

**级别**: WARNING  
**描述**: 包含 2 个及以上子文件的组件应使用目录形式组织。

```
规范:
- 单文件组件: ComponentName.vue（仅一个文件）
- 复合组件: ComponentName/index.vue + 子组件 + types.ts

检查方式:
1. 扫描 components/ 下的 .vue 文件
2. 如果一个组件有配套的 types.ts、子组件，应提升为目录形式
3. 目录形式必须有 index.vue 作为入口

合规示例:
  QueryPanel/
  ├── index.vue
  ├── QueryPanelSummary.vue
  └── types.ts

违规示例:
  QueryPanel.vue
  QueryPanelSummary.vue
  QueryPanelTypes.ts  ← 散落在同级目录
```

### RULE-PS05: framework 目录结构检查

**级别**: WARNING  
**描述**: 框架层应按功能拆分为标准子目录。

```
标准结构:
src/framework/
├── router/              # 路由配置
│   ├── index.ts
│   ├── guards.ts
│   └── modules/         # 按模块拆分的路由文件
├── store/               # 状态管理
│   ├── index.ts
│   └── modules/         # 按模块拆分的 store
├── plugins/             # 插件注册
├── directives/          # 全局指令
└── config/              # 全局配置

检查方式:
1. 检查 framework/ 下是否有 router/ 和 store/ 目录
2. 检查 router/ 下是否有 modules/ 子目录
3. 路由文件数量 > 300 行时应拆分为 modules/ 中的独立文件

合规示例: framework/router/modules/finance.ts
违规示例: framework/router.ts（单文件包含全部路由）
```

### RULE-PS06: services 目录结构检查

**级别**: WARNING  
**描述**: 服务层应包含 HTTP 客户端和公共 API 定义。

```
标准结构:
src/services/
├── http/
│   ├── index.ts          # Axios 实例与拦截器
│   └── types.ts          # 请求/响应类型
└── api/
    ├── auth.ts           # 认证接口
    ├── dict.ts           # 字典接口
    └── file.ts           # 文件接口

检查方式:
1. 检查 services/ 下是否有 http/ 目录
2. 检查 http/ 下是否有统一的 Axios 实例
3. 检查是否有公共 API 定义（如 auth、dict）
4. 检查模块级 API 是否放在 modules/{name}/api/ 而非 services/api/

合规示例: services/http/index.ts 包含统一拦截器
违规示例: 直接在 utils/ 中创建 http.ts
```

### RULE-PS07: composables 命名与位置检查

**级别**: ERROR  
**描述**: 组合式函数必须以 use- 为前缀，放在正确的目录中。

```
规范:
- 全局 composables: src/composables/use-xxx.ts
- 模块 composables: modules/{name}/composables/use-xxx.ts
- 组件 composables: components/{name}/use-xxx.ts（与组件同目录）

检查方式:
1. 扫描所有 composables/ 目录中的文件
2. 检查文件名是否以 use- 开头
3. 检查函数导出名是否以 use 开头（camelCase）
4. 检查全局 composable 是否不含模块特定业务逻辑

合规示例: composables/use-table-data.ts → export function useTableData()
违规示例: composables/table-data.ts, composables/tableData.ts
```

### RULE-PS08: 禁止跨层引用检查

**级别**: ERROR  
**描述**: 各层之间的引用必须遵循依赖方向，禁止反向引用。

```
合法引用方向:
  modules/ → components/ ✅
  modules/ → composables/ ✅
  modules/ → services/ ✅
  modules/ → utils/ ✅
  modules/ → types/ ✅
  components/ → composables/ ✅
  components/ → utils/ ✅
  framework/ → modules/ ✅（仅路由注册）

非法引用:
  components/ → modules/ ❌ （共享组件不应引用模块内部文件）
  utils/ → modules/ ❌
  services/ → modules/ ❌
  modules/A → modules/B/components/ ❌ （应提升到共享组件层）

检查方式:
1. 扫描所有 import 语句
2. 解析引用路径
3. 检查是否存在非法的跨层引用
4. 特别关注 modules/ 之间的交叉引用

违规示例:
import PaymentForm from '@/modules/finance/components/PaymentForm.vue'
// 在 modules/procurement/ 中引用 finance 的内部组件
```

## 检查脚本伪代码

```javascript
function checkProjectStructure(projectRoot) {
  const results = [];
  const srcPath = `${projectRoot}/src`;

  // RULE-PS01: 顶层目录
  const requiredDirs = ['assets', 'components', 'modules', 'framework', 'services', 'utils', 'types'];
  for (const dir of requiredDirs) {
    if (!exists(`${srcPath}/${dir}`)) {
      results.push({
        rule: 'RULE-PS01',
        level: 'ERROR',
        message: `缺少必要的顶层目录: src/${dir}/`,
      });
    }
  }

  // RULE-PS03: components 分层
  const componentDirs = listDirs(`${srcPath}/components`);
  const requiredLayers = ['common', 'layout', 'business'];
  for (const layer of requiredLayers) {
    if (!componentDirs.includes(layer)) {
      results.push({
        rule: 'RULE-PS03',
        level: 'ERROR',
        message: `components/ 缺少分层目录: ${layer}/`,
      });
    }
  }
  const rootVueFiles = listFiles(`${srcPath}/components`, '*.vue');
  if (rootVueFiles.length > 0) {
    results.push({
      rule: 'RULE-PS03',
      level: 'ERROR',
      message: `components/ 根目录下不应直接放置组件文件，请移入 common/、layout/ 或 business/`,
    });
  }

  // RULE-PS07: composables 命名
  const composableFiles = glob(`${srcPath}/**/composables/*.ts`);
  for (const file of composableFiles) {
    const fileName = getFileName(file);
    if (!fileName.startsWith('use-')) {
      results.push({
        rule: 'RULE-PS07',
        level: 'ERROR',
        file,
        message: `Composable 文件名 "${fileName}" 必须以 use- 开头`,
      });
    }
  }

  // RULE-PS08: 跨层引用
  const moduleFiles = glob(`${srcPath}/modules/**/*.{ts,vue}`);
  for (const file of moduleFiles) {
    const imports = extractImports(file);
    const currentModule = getModuleName(file);
    for (const imp of imports) {
      if (imp.includes('/modules/') && !imp.includes(`/modules/${currentModule}/`)) {
        if (imp.includes('/components/') || imp.includes('/composables/')) {
          results.push({
            rule: 'RULE-PS08',
            level: 'ERROR',
            file,
            message: `非法跨模块引用: ${imp}，请将组件提升到 components/ 共享层`,
          });
        }
      }
    }
  }

  return results;
}
```
