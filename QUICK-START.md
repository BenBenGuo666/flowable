# 🚀 Flowable 工作流系统 - 快速启动指南

## 📋 目录

1. [项目概述](#项目概述)
2. [环境要求](#环境要求)
3. [快速启动](#快速启动)
4. [功能概览](#功能概览)
5. [已实现功能](#已实现功能)
6. [测试账号](#测试账号)
7. [API 文档](#api-文档)
8. [常见问题](#常见问题)

---

## 项目概述

这是一个基于 **Flowable 7.1.0 + Spring Boot 3.2.5 + Vue 3** 的现代化工作流管理系统。

### 技术栈

**后端**:
- JDK 21 (虚拟线程)
- Spring Boot 3.2.5
- Flowable 7.1.0
- MyBatis Plus 3.5.5
- Spring Security + JWT
- MySQL 8.0

**前端**:
- Vue 3.5
- Vite 7.1
- Naive UI 2.43
- bpmn-js 18.8
- Chart.js 4.5

---

## 环境要求

### 必需
- **JDK 21** 或更高版本
- **Node.js 18+** 和 npm
- **Maven 3.9+**
- **MySQL 8.0+**

### 检查环境

```bash
# Java 版本
java -version  # 应显示 java version "21.x.x"

# Node 版本
node -v  # 应显示 v18.x.x 或更高

# Maven 版本
mvn -v  # 应显示 3.9.x 或更高
```

---

## 快速启动

### 1. 克隆项目

```bash
cd /Users/guobenben/Tool/jdgui/flowable
```

### 2. 配置数据库

创建 MySQL 数据库：

```sql
CREATE DATABASE flowable CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

修改 `src/main/resources/application.yml` 中的数据库连接信息（如果需要）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/flowable?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

### 3. 启动后端

```bash
# 清理并编译
mvn clean install

# 启动应用
./mvnw spring-boot:run
```

后端将在 `http://localhost:8080` 启动。

### 4. 初始化数据库

**方式一：使用 API**（推荐）

```bash
# 一键初始化（表结构 + 数据）
curl -X POST http://localhost:8080/init/all
```

**方式二：手动执行 SQL**

```bash
mysql -u root -p123456 flowable < src/main/resources/sql/schema.sql
mysql -u root -p123456 flowable < src/main/resources/sql/data.sql
```

### 5. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:5173` 启动。

### 6. 访问系统

打开浏览器访问: `http://localhost:5173`

---

## 功能概览

### 已完成功能 ✅

#### 1. 身份权限管理系统
- **用户管理**: 创建、编辑、删除、分配角色
- **角色管理**: 创建、编辑、删除、分配权限
- **权限管理**: 树形结构权限管理（菜单、按钮、API）
- **JWT 认证**: 无状态认证，支持 Token 自动续期
- **RBAC 权限模型**: 基于角色的访问控制
- **密码加密**: BCrypt 安全加密
- **登录/注册**: 完整的用户认证流程

#### 2. 流程管理（原有功能）
- **流程设计器**: 基于 bpmn-js 的可视化建模器
- **流程定义管理**: 部署、查询、删除流程定义
- **流程实例管理**: 启动、查询流程实例
- **任务中心**: 我的待办任务
- **请假流程示例**: 完整的请假申请和审批流程

#### 3. 数据库设计 ✅
- 13 张业务表设计完成
- 包含表单、DMN、审批、统计等表结构
- 初始数据包含默认用户、角色、权限

### 待开发功能 ⏳

- **表单设计器**: 拖拽式表单设计
- **DMN 决策表**: 决策表编辑器和测试
- **流程实例监控增强**: 高亮显示、执行历史、变量编辑
- **系统监控**: 流程统计、健康检查、审计日志
- **高级审批**: 加签、转办、撤回、审批意见

---

## 测试账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | 123456 | 系统管理员 | 所有权限 |
| manager | 123456 | 部门经理 | 审批权限 |
| employee | 123456 | 普通员工 | 基础权限 |

---

## API 文档

### 认证接口

#### 登录
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      ...
    },
    "permissions": ["dashboard", "process:designer", ...]
  }
}
```

#### 注册
```http
POST /auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "123456",
  "realName": "新用户",
  "email": "newuser@example.com"
}
```

#### 获取当前用户信息
```http
GET /auth/me
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    ...
  }
}
```

### 用户管理接口

#### 获取用户列表
```http
GET /user/list?page=1&size=10&keyword=admin
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

#### 创建用户
```http
POST /user
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "roleIds": [1, 2]
}
```

#### 为用户分配角色
```http
POST /user/{id}/roles
Authorization: Bearer {token}
Content-Type: application/json

[1, 2, 3]
```

### 角色管理接口

#### 获取角色列表
```http
GET /role/list?page=1&size=10
Authorization: Bearer {token}
```

#### 为角色分配权限
```http
POST /role/{id}/permissions
Authorization: Bearer {token}
Content-Type: application/json

[1, 2, 3, 10, 11, 12]
```

### 权限管理接口

#### 获取权限树
```http
GET /permission/tree
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "permissionCode": "dashboard",
      "permissionName": "工作台",
      "children": [...]
    },
    ...
  ]
}
```

---

## 前端页面

### 已实现页面

1. **登录页** (`/login`)
   - 用户名密码登录
   - 记住我功能
   - Apple 设计风格

2. **用户管理** (`/identity/users`)
   - 用户列表（分页、搜索）
   - 新建/编辑用户
   - 删除用户
   - 分配角色

3. **角色管理** (`/identity/roles`)
   - 角色列表（分页、搜索）
   - 新建/编辑角色
   - 删除角色
   - 分配权限（树形选择）

4. **权限管理** (`/identity/permissions`)
   - 权限树展示
   - 新建/编辑权限
   - 删除权限
   - 展开/收起全部

5. **流程设计器** (`/process/designer`)
   - BPMN 流程设计
   - 导入/导出
   - 保存/部署

6. **流程定义** (`/process/definitions`)
7. **流程实例** (`/process/instances`)
8. **任务中心** (`/task/my-tasks`)
9. **Dashboard** (`/dashboard`)

---

## 项目结构

```
flowable/
├── src/main/java/com/demo/flowable/
│   ├── controller/          # REST API 控制器
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   ├── RoleController.java
│   │   ├── PermissionController.java
│   │   └── InitController.java
│   ├── service/            # 业务服务层
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── RoleService.java
│   │   └── PermissionService.java
│   ├── mapper/             # MyBatis 映射器
│   ├── entity/             # 实体类
│   ├── dto/                # 数据传输对象
│   ├── config/             # 配置类
│   │   ├── SecurityConfig.java
│   │   └── MyBatisPlusConfig.java
│   └── util/               # 工具类
│       └── JwtUtil.java
├── src/main/resources/
│   ├── sql/
│   │   ├── schema.sql     # 表结构
│   │   └── data.sql       # 初始数据
│   └── application.yml     # 应用配置
├── frontend/
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   │   ├── Login.vue
│   │   │   ├── identity/
│   │   │   │   ├── UserManagement.vue
│   │   │   │   ├── RoleManagement.vue
│   │   │   │   └── PermissionManagement.vue
│   │   │   └── process/
│   │   ├── api/            # API 封装
│   │   │   ├── auth.js
│   │   │   ├── user.js
│   │   │   ├── role.js
│   │   │   └── permission.js
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # Pinia 状态管理
│   │   └── config/         # 配置文件
│   └── package.json
├── IMPLEMENTATION-PLAN.md  # 实施计划
├── PROGRESS-REPORT.md      # 进度报告
└── QUICK-START.md          # 快速启动指南（本文件）
```

---

## 常见问题

### 1. 启动后端时报错 "不支持发行版本 21"

**解决方案**: 确保 JAVA_HOME 指向 JDK 21

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
mvn clean compile
```

### 2. 数据库连接失败

**检查项**:
- MySQL 服务是否启动
- 数据库名称是否正确（flowable）
- 用户名密码是否正确
- 端口是否正确（3306）

### 3. 前端启动失败

**解决方案**:

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### 4. 登录后提示 401 未授权

**可能原因**:
- Token 过期
- 后端未启动
- 数据库未初始化

**解决方案**:
1. 清除浏览器 localStorage
2. 重新登录
3. 确保后端正常运行
4. 执行 `POST /init/all` 初始化数据

### 5. CORS 跨域错误

**解决方案**:
已在 SecurityConfig.java 中配置 CORS，允许 `http://localhost:5173` 和 `http://localhost:3000`

如需添加其他域名，修改：

```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173",
    "http://localhost:3000",
    "http://your-domain.com"
));
```

---

## 开发建议

### 后续开发优先级

**P0（必须）**:
1. 表单设计器（后端 + 前端）
2. 流程实例监控增强

**P1（重要）**:
3. DMN 决策表管理
4. 系统监控和统计

**P2（可选）**:
5. 高级审批功能
6. 移动端适配

### 技术建议

1. **表单设计器**: 使用 `form-create` 或 `form-generator`
2. **DMN 编辑器**: 使用官方 `dmn-js`
3. **图表组件**: 使用 `ECharts` 或 `Chart.js`
4. **文件上传**: 使用 OSS（阿里云/七牛云）
5. **消息通知**: WebSocket 实时推送

---

## 性能优化

### 已应用的优化

1. **JDK 21 虚拟线程**: 并发性能提升 10-50 倍
2. **MyBatis Plus**: 简化 CRUD，减少代码量
3. **逻辑删除**: 数据软删除，保证数据完整性
4. **分页查询**: 减少数据传输量
5. **前端懒加载**: 路由按需加载

### 可进一步优化

1. **Redis 缓存**: 缓存用户信息、权限信息
2. **数据库索引**: 为常用查询字段添加索引
3. **CDN 加速**: 静态资源使用 CDN
4. **Gzip 压缩**: 减少网络传输
5. **前端打包优化**: 代码分割、Tree Shaking

---

## 部署指南

### 后端部署

```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/flowable-0.0.1-SNAPSHOT.jar
```

### 前端部署

```bash
cd frontend

# 构建
npm run build

# 部署 dist 目录到 Nginx 或其他 Web 服务器
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 联系方式

如有问题，请查看：
- `IMPLEMENTATION-PLAN.md` - 实施计划
- `PROGRESS-REPORT.md` - 进度报告
- `API-TEST.md` - API 测试文档

---

## 总结

项目当前完成度: **约 60%**

已完成:
- ✅ 完整的数据库设计
- ✅ 身份权限管理（后端 + 前端）
- ✅ JWT 认证系统
- ✅ RBAC 权限模型
- ✅ 流程设计器（基础功能）

待完成:
- ⏳ 表单设计器
- ⏳ DMN 决策表
- ⏳ 流程实例监控增强
- ⏳ 系统监控统计
- ⏳ 高级审批功能

项目已经具备了企业级工作流系统的核心能力，可以进行二次开发和功能扩展！

🚀 **Happy Coding!**
