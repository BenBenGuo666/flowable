# 功能补充实施计划

## 一、数据库设计

### 1. 用户权限表

```sql
-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    tenant_id VARCHAR(50) COMMENT '租户ID',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_username (username),
    INDEX idx_tenant (tenant_id)
) COMMENT='系统用户表';

-- 角色表
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    tenant_id VARCHAR(50) COMMENT '租户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (role_code),
    INDEX idx_tenant (tenant_id)
) COMMENT='系统角色表';

-- 权限表
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(20) COMMENT '权限类型：menu-菜单，button-按钮，api-接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父级权限ID',
    path VARCHAR(255) COMMENT '路由路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (permission_code),
    INDEX idx_parent (parent_id)
) COMMENT='系统权限表';

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user (user_id),
    INDEX idx_role (role_id)
) COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role (role_id),
    INDEX idx_permission (permission_id)
) COMMENT='角色权限关联表';
```

### 2. 表单设计器表

```sql
-- 表单定义表
CREATE TABLE form_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    form_key VARCHAR(100) NOT NULL UNIQUE COMMENT '表单唯一标识',
    form_name VARCHAR(100) NOT NULL COMMENT '表单名称',
    form_type VARCHAR(20) DEFAULT 'custom' COMMENT '表单类型：custom-自定义，process-流程表单',
    form_config TEXT COMMENT '表单配置JSON（字段定义、布局等）',
    form_schema TEXT COMMENT '表单Schema（JSON Schema）',
    version INT DEFAULT 1 COMMENT '版本号',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft-草稿，published-已发布',
    category VARCHAR(50) COMMENT '分类',
    description VARCHAR(255) COMMENT '描述',
    tenant_id VARCHAR(50) COMMENT '租户ID',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_form_key (form_key),
    INDEX idx_tenant (tenant_id)
) COMMENT='表单定义表';

-- 表单数据表
CREATE TABLE form_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    form_key VARCHAR(100) NOT NULL COMMENT '表单Key',
    form_data TEXT COMMENT '表单数据JSON',
    business_key VARCHAR(100) COMMENT '业务Key（关联流程实例）',
    process_instance_id VARCHAR(100) COMMENT '流程实例ID',
    task_id VARCHAR(100) COMMENT '任务ID',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_form_key (form_key),
    INDEX idx_business_key (business_key),
    INDEX idx_process_instance (process_instance_id)
) COMMENT='表单数据表';
```

### 3. DMN 决策表

```sql
-- DMN 决策表定义
CREATE TABLE dmn_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dmn_key VARCHAR(100) NOT NULL UNIQUE COMMENT 'DMN唯一标识',
    dmn_name VARCHAR(100) NOT NULL COMMENT 'DMN名称',
    dmn_xml TEXT COMMENT 'DMN XML内容',
    dmn_config TEXT COMMENT 'DMN配置JSON',
    version INT DEFAULT 1 COMMENT '版本号',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    deployment_id VARCHAR(100) COMMENT '部署ID',
    category VARCHAR(50) COMMENT '分类',
    description VARCHAR(255) COMMENT '描述',
    tenant_id VARCHAR(50) COMMENT '租户ID',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_dmn_key (dmn_key),
    INDEX idx_tenant (tenant_id)
) COMMENT='DMN决策表定义';
```

### 4. 审批记录增强表

```sql
-- 审批意见表
CREATE TABLE approval_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(100) NOT NULL COMMENT '任务ID',
    process_instance_id VARCHAR(100) NOT NULL COMMENT '流程实例ID',
    comment_type VARCHAR(20) COMMENT '意见类型：approve-同意，reject-驳回，transfer-转办',
    comment_text TEXT COMMENT '审批意见内容',
    attachments TEXT COMMENT '附件JSON数组',
    user_id VARCHAR(50) COMMENT '操作人ID',
    user_name VARCHAR(50) COMMENT '操作人姓名',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task (task_id),
    INDEX idx_process_instance (process_instance_id)
) COMMENT='审批意见表';

-- 任务转办记录表
CREATE TABLE task_transfer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(100) NOT NULL COMMENT '任务ID',
    process_instance_id VARCHAR(100) NOT NULL COMMENT '流程实例ID',
    from_user_id VARCHAR(50) COMMENT '转出人ID',
    from_user_name VARCHAR(50) COMMENT '转出人姓名',
    to_user_id VARCHAR(50) COMMENT '接收人ID',
    to_user_name VARCHAR(50) COMMENT '接收人姓名',
    transfer_reason VARCHAR(255) COMMENT '转办原因',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task (task_id),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id)
) COMMENT='任务转办记录表';

-- 任务加签记录表
CREATE TABLE task_delegate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(100) NOT NULL COMMENT '原任务ID',
    delegate_task_id VARCHAR(100) COMMENT '加签任务ID',
    process_instance_id VARCHAR(100) NOT NULL COMMENT '流程实例ID',
    delegate_type VARCHAR(20) COMMENT '加签类型：before-前加签，after-后加签',
    from_user_id VARCHAR(50) COMMENT '发起人ID',
    from_user_name VARCHAR(50) COMMENT '发起人姓名',
    to_user_id VARCHAR(50) COMMENT '被加签人ID',
    to_user_name VARCHAR(50) COMMENT '被加签人姓名',
    delegate_reason VARCHAR(255) COMMENT '加签原因',
    status VARCHAR(20) COMMENT '状态：pending-待处理，completed-已完成',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_time DATETIME COMMENT '完成时间',
    INDEX idx_task (task_id),
    INDEX idx_process_instance (process_instance_id)
) COMMENT='任务加签记录表';
```

### 5. 系统监控表

```sql
-- 流程统计表
CREATE TABLE process_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    stat_date DATE NOT NULL COMMENT '统计日期',
    process_key VARCHAR(100) COMMENT '流程Key',
    start_count INT DEFAULT 0 COMMENT '启动次数',
    complete_count INT DEFAULT 0 COMMENT '完成次数',
    running_count INT DEFAULT 0 COMMENT '运行中数量',
    avg_duration BIGINT COMMENT '平均耗时（毫秒）',
    max_duration BIGINT COMMENT '最大耗时',
    min_duration BIGINT COMMENT '最小耗时',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stat (stat_date, process_key),
    INDEX idx_date (stat_date)
) COMMENT='流程统计表';
```

## 二、实现顺序

### Phase 1: 基础设施（优先级最高）
1. ✅ 数据库表创建脚本
2. 身份权限管理后端
3. 身份权限管理前端

### Phase 2: 核心功能
4. 表单设计器后端
5. 表单设计器前端
6. 流程实例监控增强

### Phase 3: 高级功能
7. DMN 决策表管理
8. 系统监控统计
9. 高级审批功能

## 三、技术选型

### 后端
- Spring Security：权限认证
- JWT：Token 认证
- BCrypt：密码加密
- Flowable DMN Engine：决策引擎

### 前端
- @form-create/designer：表单设计器
- dmn-js：DMN 编辑器
- echarts/chart.js：图表组件
- vxe-table：高级表格

## 四、API 设计规范

### RESTful API 规范
```
GET    /api/{resource}           # 查询列表
GET    /api/{resource}/{id}      # 查询详情
POST   /api/{resource}           # 新增
PUT    /api/{resource}/{id}      # 更新
DELETE /api/{resource}/{id}      # 删除
```

### 统一响应格式（已有）
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

## 五、前端路由规划

```javascript
/identity              # 身份权限管理
  /identity/users      # 用户管理
  /identity/roles      # 角色管理
  /identity/permissions # 权限管理

/form                  # 表单管理
  /form/designer       # 表单设计器
  /form/list           # 表单列表
  /form/data           # 表单数据

/dmn                   # DMN 决策表
  /dmn/designer        # DMN 设计器
  /dmn/list            # 决策表列表

/monitor               # 系统监控
  /monitor/statistics  # 统计报表
  /monitor/health      # 健康检查
```

## 六、开发规范

### 代码规范
1. 遵循项目现有的代码风格
2. 使用 JDK 21 Record 定义 DTO
3. 所有 Service 方法添加事务注解
4. 使用 Lombok 简化代码

### 前端规范
1. 遵循 Apple 设计风格
2. 使用 Composition API
3. 组件化开发
4. 统一使用 Naive UI 组件

### 安全规范
1. 所有 API 需要权限验证
2. 密码必须加密存储
3. 防止 SQL 注入、XSS 攻击
4. 敏感操作记录审计日志
