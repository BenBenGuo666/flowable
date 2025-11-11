# 设计文档

## 概述

Flowable UI 系统是一个基于 Vue.js 3 和 Spring Boot 构建的现代化、模块化 Web 应用程序，为 Flowable 7.1.0 流程引擎提供全面的管理功能。系统采用微服务风格的架构，前端模块与后端服务之间清晰分离，确保可扩展性、可维护性以及与 Flowable REST API 的无缝集成。

设计强调苹果风格的用户体验原则，包括清晰性、一致性和直观交互，同时保持企业级的流程管理、任务处理和系统管理功能。

## 架构设计

### 高层架构

```
┌─────────────────────────────────────────────────────────────┐
│                    前端 (Vue.js 3)                          │
├─────────────────────────────────────────────────────────────┤
│  流程设计器 │ 任务中心 │ 表单设计器 │ 身份管理 │ 系统设置      │
│  流程监控   │ 仪表盘   │ 决策表     │ 权限管理 │ 国际化       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                后端 API 层 (Spring Boot)                    │
├─────────────────────────────────────────────────────────────┤
│  流程API │ 任务API │ 表单API │ 身份API │ 认证API │ 国际化API  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Flowable 引擎 7.1.0                        │
├─────────────────────────────────────────────────────────────┤
│     流程引擎 │ 任务服务 │ 身份服务 │ DMN 引擎                │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

**前端技术栈:**
- Vue.js 3 with Composition API
- TypeScript 类型安全
- Pinia 状态管理
- Vue Router 路由导航
- Tailwind CSS 样式框架
- Vite 构建工具
- Axios HTTP 客户端
- Vue I18n 国际化

**后端技术栈:**
- Spring Boot 3.x
- Spring Security 认证授权
- MyBatis Plus 数据库操作
- H2/MySQL 数据持久化
- Flowable 7.1.0 集成
- JWT 会话管理
- Spring Boot I18n 国际化

## 组件和接口设计

### 前端模块结构

```
src/
├── modules/
│   ├── dashboard/              # 仪表盘和概览
│   ├── process-modeler/        # BPMN 可视化设计器
│   ├── process-monitor/        # 实例监控
│   ├── task-center/           # 任务管理
│   ├── form-designer/         # 动态表单构建器
│   ├── decision-tables/       # DMN 表管理
│   ├── identity-management/   # 用户/角色管理
│   └── settings/              # 系统配置
├── shared/
│   ├── components/            # 可复用 UI 组件
│   ├── composables/           # Vue 组合函数
│   ├── services/              # API 服务层
│   ├── stores/                # Pinia 状态存储
│   ├── i18n/                  # 国际化资源
│   └── utils/                 # 工具函数
└── assets/                    # 静态资源
```

### 核心组件设计

#### 1. 流程设计器组件

**功能目标:** 具有拖拽功能的可视化 BPMN 建模界面

**核心特性:**
- 支持缩放和平移的画布区域
- 包含 BPMN 元素的调色板（开始/结束事件、任务、网关）
- 节点配置属性面板
- 保存、验证和导出功能的工具栏
- 版本历史和回滚功能

**技术实现:**
- 使用 bpmn-js 库进行 BPMN 渲染
- 自定义 Vue 包装组件进行集成
- 使用 Flowable 模型验证 API 进行实时验证
- 基于 JSON 的模型存储，支持 BPMN XML 导出

#### 2. 任务中心组件

**功能目标:** 任务管理和审批工作流的统一界面

**核心特性:**
- 支持过滤和排序的任务列表
- 嵌入表单的任务详情视图
- 审批操作（同意、驳回、委派、认领）
- 任务历史和审计跟踪
- 通过 WebSocket 实现实时通知

**技术实现:**
- 使用 Vue 响应式系统的响应式任务列表
- 基于表单定义的动态表单渲染
- WebSocket 集成实现实时更新
- 失败时回滚的乐观 UI 更新

#### 3. 表单设计器组件

**功能目标:** 流程任务表单的可视化表单构建器

**核心特性:**
- 拖拽式表单控件
- 属性配置面板
- 表单预览和测试
- JSON 模式生成
- 表单版本控制和发布

**技术实现:**
- 使用 Vue 动态组件的自定义表单构建器
- 使用 Ajv 进行 JSON 模式验证
- 运行时显示的表单渲染引擎
- 与流程设计器集成进行表单绑定

### API 接口设计

#### REST API 端点

```typescript
// 流程管理
GET    /api/processes                    // 列出流程定义
POST   /api/processes                    // 创建流程模型
PUT    /api/processes/{id}               // 更新流程模型
DELETE /api/processes/{id}               // 删除流程模型
POST   /api/processes/{id}/deploy        // 部署流程定义

// 流程实例
GET    /api/instances                    // 列出流程实例
GET    /api/instances/{id}               // 获取实例详情
POST   /api/instances/{id}/suspend       // 挂起实例
POST   /api/instances/{id}/activate      // 激活实例
DELETE /api/instances/{id}               // 删除实例

// 任务管理
GET    /api/tasks                        // 列出任务
GET    /api/tasks/{id}                   // 获取任务详情
POST   /api/tasks/{id}/complete          // 完成任务
POST   /api/tasks/{id}/claim             // 认领任务
POST   /api/tasks/{id}/delegate          // 委派任务

// 表单管理
GET    /api/forms                        // 列出表单定义
POST   /api/forms                        // 创建表单
PUT    /api/forms/{id}                   // 更新表单
GET    /api/forms/{id}/render            // 为任务渲染表单

// 身份管理
GET    /api/users                        // 列出用户
POST   /api/users                        // 创建用户
PUT    /api/users/{id}                   // 更新用户
GET    /api/roles                        // 列出角色
POST   /api/roles                        // 创建角色

// 国际化
GET    /api/i18n/messages/{locale}       // 获取语言资源
PUT    /api/i18n/messages/{locale}       // 更新语言资源
```

#### WebSocket 事件

```typescript
// 实时通知
interface TaskNotification {
  type: 'TASK_ASSIGNED' | 'TASK_COMPLETED' | 'TASK_DELEGATED';
  taskId: string;
  userId: string;
  processInstanceId: string;
  timestamp: Date;
  message: string;
}

interface ProcessNotification {
  type: 'PROCESS_STARTED' | 'PROCESS_COMPLETED' | 'PROCESS_ERROR';
  processInstanceId: string;
  processDefinitionKey: string;
  timestamp: Date;
  message: string;
}
```

## 数据模型

### 核心实体

#### 流程模型
```typescript
interface ProcessModel {
  id: string;
  name: string;
  key: string;
  category: string;
  description?: string;
  version: number;
  modelJson: string;          // JSON 格式的 BPMN 模型
  bpmnXml?: string;          // 生成的 BPMN XML
  tenantId?: string;
  createdBy: string;
  createdTime: Date;
  lastModified: Date;
  isPublished: boolean;
  deploymentId?: string;
}
```

#### 任务定义
```typescript
interface Task {
  id: string;
  name: string;
  assignee?: string;
  candidateGroups?: string[];
  processInstanceId: string;
  processDefinitionId: string;
  taskDefinitionKey: string;
  formKey?: string;
  priority: number;
  dueDate?: Date;
  createTime: Date;
  variables: Record<string, any>;
  status: 'CREATED' | 'ASSIGNED' | 'COMPLETED' | 'SUSPENDED';
}
```

#### 表单定义
```typescript
interface FormDefinition {
  id: string;
  name: string;
  key: string;
  version: number;
  schema: FormSchema;         // 表单结构的 JSON 模式
  tenantId?: string;
  createdBy: string;
  createdTime: Date;
  isPublished: boolean;
}

interface FormSchema {
  fields: FormField[];
  layout: LayoutConfig;
  validation: ValidationRules;
}

interface FormField {
  id: string;
  type: 'text' | 'number' | 'date' | 'select' | 'checkbox' | 'file';
  label: string;
  required: boolean;
  defaultValue?: any;
  options?: SelectOption[];
  validation?: FieldValidation;
}
```

#### 用户和角色模型
```typescript
interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  tenantId?: string;
  roles: Role[];
  createdTime: Date;
  lastLoginTime?: Date;
  preferredLanguage: 'zh-CN' | 'en-US';
}

interface Role {
  id: string;
  name: string;
  description?: string;
  permissions: Permission[];
  tenantId?: string;
}

interface Permission {
  id: string;
  resource: string;          // 例如: 'process', 'task', 'user'
  action: string;            // 例如: 'read', 'write', 'delete'
  scope?: string;            // 例如: 'own', 'group', 'all'
}
```

## 错误处理

### 前端错误处理策略

1. **API 错误拦截器:** 全局 Axios 拦截器处理常见 HTTP 错误
2. **用户友好消息:** 将技术错误转换为用户可读消息
3. **重试机制:** 对瞬时故障自动重试
4. **离线支持:** 后端不可用时的优雅降级
5. **错误边界:** Vue 错误边界防止应用崩溃

### 后端错误处理

1. **全局异常处理器:** 集中错误处理，响应格式一致
2. **验证错误:** 详细的字段级验证错误消息
3. **业务逻辑错误:** 业务规则违反的自定义异常
4. **集成错误:** 正确处理 Flowable 引擎错误
5. **审计日志:** 全面的错误日志记录用于调试

### 错误响应格式

```typescript
interface ErrorResponse {
  error: {
    code: string;
    message: string;
    details?: Record<string, any>;
    timestamp: Date;
    path: string;
  };
}
```

## 测试策略

### 前端测试

1. **单元测试:** 使用 Vue Test Utils 进行组件测试
2. **集成测试:** 使用模拟后端进行 API 集成测试
3. **端到端测试:** 使用 Playwright 进行端到端用户工作流测试
4. **视觉回归测试:** 截图比较确保 UI 一致性
5. **无障碍测试:** 使用 axe-core 进行自动化 a11y 测试

### 后端测试

1. **单元测试:** 使用 JUnit 5 进行服务层测试
2. **集成测试:** 使用 Spring Boot Test 进行 API 端点测试
3. **数据库测试:** 使用 Testcontainers 进行数据库集成
4. **Flowable 集成:** 模拟 Flowable 引擎进行隔离测试
5. **性能测试:** 使用 JMeter 对关键端点进行负载测试

### 测试覆盖率目标

- 单元测试覆盖率: >80%
- 集成测试覆盖率: >70%
- 端到端测试覆盖率: 关键用户路径
- 性能: <2秒页面加载，<500毫秒 API 响应

## 安全考虑

### 认证和授权

1. **基于 JWT 的认证:** 无状态令牌认证
2. **基于角色的访问控制:** 细粒度权限系统
3. **多租户安全:** 通过租户 ID 进行数据隔离
4. **会话管理:** 安全的令牌刷新和登出
5. **密码安全:** 使用盐的 BCrypt 哈希

### 数据保护

1. **输入验证:** 所有输入的服务器端验证
2. **SQL 注入防护:** 通过 MyBatis 使用参数化查询
3. **XSS 保护:** 内容安全策略和输入清理
4. **CSRF 保护:** 状态变更操作的 CSRF 令牌
5. **审计日志:** 安全事件的全面审计跟踪

### API 安全

1. **速率限制:** 防止 API 滥用和 DoS 攻击
2. **CORS 配置:** 正确的跨域资源共享设置
3. **HTTPS 强制:** 所有通信的 TLS 加密
4. **API 版本控制:** 向后兼容和弃用策略
5. **错误信息:** 最小错误详情防止信息泄露

## 性能优化

### 前端性能

1. **代码分割:** 基于路由和组件的懒加载
2. **包优化:** Tree shaking 和压缩
3. **缓存策略:** HTTP 缓存和浏览器存储
4. **虚拟滚动:** 大数据列表
5. **图片优化:** WebP 格式和响应式图片

### 后端性能

1. **数据库优化:** 适当的索引和查询优化
2. **缓存层:** Redis 缓存频繁访问的数据
3. **连接池:** 高效的数据库连接管理
4. **异步处理:** 长时间运行任务的非阻塞操作
5. **API 分页:** 限制大数据集的数据传输

### 国际化设计

1. **多语言支持:** 中文和英文界面切换
2. **资源文件管理:** 集中的语言资源管理
3. **动态语言切换:** 运行时语言切换不需重启
4. **日期时间本地化:** 根据用户区域设置格式化
5. **数字货币格式化:** 本地化的数字和货币显示

### 监控和指标

1. **应用指标:** 响应时间、错误率、吞吐量
2. **用户体验指标:** 核心 Web 指标、用户交互时间
3. **基础设施指标:** CPU、内存、磁盘使用情况
4. **业务指标:** 流程完成率、任务处理时间
5. **告警:** 基于阈值的主动监控告警