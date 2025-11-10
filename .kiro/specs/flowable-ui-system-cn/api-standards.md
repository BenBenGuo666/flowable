# API 设计标准

## 概述

本文档定义了 Flowable UI 系统的 API 设计标准和规范，确保 API 的一致性、可维护性和易用性。

## RESTful API 设计原则

### 1. 资源命名规范

#### 1.1 URL 路径规范
- 使用名词而非动词：`/api/users` 而不是 `/api/getUsers`
- 使用复数形式：`/api/processes` 而不是 `/api/process`
- 使用小写字母和连字符：`/api/process-instances` 而不是 `/api/processInstances`
- 避免深层嵌套，最多 3 层：`/api/processes/{id}/tasks`

#### 1.2 资源层次结构
```
/api/processes                    # 流程定义
/api/processes/{id}               # 特定流程定义
/api/processes/{id}/deploy        # 流程部署
/api/processes/{id}/versions      # 流程版本

/api/instances                    # 流程实例
/api/instances/{id}               # 特定流程实例
/api/instances/{id}/tasks         # 实例任务
/api/instances/{id}/variables     # 实例变量

/api/tasks                        # 任务
/api/tasks/{id}                   # 特定任务
/api/tasks/{id}/complete          # 完成任务
/api/tasks/{id}/delegate          # 委派任务

/api/forms                        # 表单定义
/api/forms/{id}/render            # 渲染表单

/api/users                        # 用户管理
/api/roles                        # 角色管理
/api/permissions                  # 权限管理
```

### 2. HTTP 方法使用规范

| 方法 | 用途 | 示例 |
|------|------|------|
| GET | 获取资源 | `GET /api/processes` |
| POST | 创建资源 | `POST /api/processes` |
| PUT | 完整更新资源 | `PUT /api/processes/{id}` |
| PATCH | 部分更新资源 | `PATCH /api/processes/{id}` |
| DELETE | 删除资源 | `DELETE /api/processes/{id}` |

### 3. HTTP 状态码规范

#### 3.1 成功响应
- `200 OK` - 成功获取或更新资源
- `201 Created` - 成功创建资源
- `204 No Content` - 成功删除资源或无返回内容

#### 3.2 客户端错误
- `400 Bad Request` - 请求参数错误
- `401 Unauthorized` - 未认证
- `403 Forbidden` - 无权限
- `404 Not Found` - 资源不存在
- `409 Conflict` - 资源冲突
- `422 Unprocessable Entity` - 验证失败

#### 3.3 服务器错误
- `500 Internal Server Error` - 服务器内部错误
- `502 Bad Gateway` - 网关错误
- `503 Service Unavailable` - 服务不可用

## 请求和响应格式

### 1. 请求格式

#### 1.1 Content-Type
- JSON 请求：`Content-Type: application/json`
- 文件上传：`Content-Type: multipart/form-data`

#### 1.2 请求头标准
```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer {jwt_token}
Accept-Language: zh-CN,en-US
X-Tenant-ID: {tenant_id}
```

#### 1.3 请求体示例
```json
{
  "name": "请假流程",
  "key": "leave_process",
  "category": "HR",
  "description": "员工请假审批流程",
  "tenantId": "default"
}
```

### 2. 响应格式

#### 2.1 成功响应格式
```json
{
  "success": true,
  "data": {
    "id": "process_001",
    "name": "请假流程",
    "version": 1,
    "createdTime": "2024-01-01T10:00:00Z"
  },
  "message": "操作成功",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### 2.2 列表响应格式
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "process_001",
        "name": "请假流程"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "total": 100,
      "totalPages": 5
    }
  },
  "message": "查询成功",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

#### 2.3 错误响应格式
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "输入验证失败",
    "details": {
      "name": ["名称不能为空"],
      "key": ["流程标识已存在"]
    }
  },
  "timestamp": "2024-01-01T10:00:00Z",
  "path": "/api/processes"
}
```

## 分页和过滤

### 1. 分页参数
```
GET /api/processes?page=1&size=20&sort=createdTime,desc
```

#### 1.1 分页参数说明
- `page`: 页码，从 1 开始
- `size`: 每页大小，默认 20，最大 100
- `sort`: 排序字段和方向，格式：`field,direction`

### 2. 过滤参数
```
GET /api/processes?category=HR&status=ACTIVE&name=请假
```

#### 2.1 过滤操作符
- 等于：`field=value`
- 模糊匹配：`field=*value*`
- 大于：`field=>value`
- 小于：`field=<value`
- 范围：`field=value1,value2`
- 包含：`field=in:value1,value2`

### 3. 搜索参数
```
GET /api/processes?search=请假流程
```

## 版本控制

### 1. API 版本策略
- 使用 URL 路径版本：`/api/v1/processes`
- 主版本号变更：不兼容的更改
- 次版本号变更：向后兼容的新功能
- 修订版本号：向后兼容的错误修复

### 2. 版本兼容性
- 保持至少 2 个主版本的兼容性
- 提前 6 个月通知版本弃用
- 在响应头中包含版本信息：`API-Version: 1.0`

## 认证和授权

### 1. JWT 认证
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. 权限检查
- 每个 API 端点都需要明确的权限要求
- 使用基于角色的访问控制 (RBAC)
- 支持细粒度权限控制

### 3. 多租户支持
```http
X-Tenant-ID: tenant_001
```

## 国际化支持

### 1. 语言协商
```http
Accept-Language: zh-CN,en-US;q=0.9
```

### 2. 响应消息国际化
- 所有用户可见的消息都需要支持国际化
- 错误消息使用错误码 + 本地化消息
- 支持中文和英文

## 缓存策略

### 1. HTTP 缓存头
```http
Cache-Control: public, max-age=3600
ETag: "33a64df551425fcc55e4d42a148795d9f25f89d4"
Last-Modified: Wed, 21 Oct 2015 07:28:00 GMT
```

### 2. 缓存策略
- 静态资源：长期缓存 (1年)
- API 响应：短期缓存 (5-60分钟)
- 用户相关数据：不缓存或私有缓存

## 限流和安全

### 1. 速率限制
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1609459200
```

### 2. 安全头
```http
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000
```

## API 文档标准

### 1. OpenAPI 规范
- 使用 OpenAPI 3.0 规范
- 包含完整的请求/响应示例
- 详细的错误码说明

### 2. 文档内容要求
- API 端点描述
- 参数说明和验证规则
- 响应格式和状态码
- 使用示例和最佳实践

## 监控和日志

### 1. 请求追踪
```http
X-Request-ID: 550e8400-e29b-41d4-a716-446655440000
```

### 2. 性能监控
- 响应时间监控
- 错误率统计
- API 使用量统计

### 3. 日志格式
```json
{
  "timestamp": "2024-01-01T10:00:00Z",
  "level": "INFO",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "method": "POST",
  "path": "/api/processes",
  "statusCode": 201,
  "responseTime": 150,
  "userId": "user_001",
  "tenantId": "tenant_001"
}
```

## 最佳实践

### 1. 性能优化
- 使用分页避免大量数据传输
- 实现字段选择：`?fields=id,name,status`
- 使用 HTTP/2 和压缩

### 2. 错误处理
- 提供有意义的错误消息
- 包含错误恢复建议
- 避免暴露敏感信息

### 3. 向后兼容
- 新增字段而不是修改现有字段
- 使用可选参数
- 保持响应格式稳定

### 4. 测试要求
- 每个 API 端点都需要单元测试
- 集成测试覆盖主要业务流程
- 性能测试验证响应时间要求