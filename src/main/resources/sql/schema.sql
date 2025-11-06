-- =====================================================
-- Flowable 工作流系统 - 数据库初始化脚本
-- =====================================================

-- =====================================================
-- 1. 用户权限模块
-- =====================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    tenant_id VARCHAR(50) DEFAULT 'default' COMMENT '租户ID',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    INDEX idx_username (username),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    tenant_id VARCHAR(50) DEFAULT 'default' COMMENT '租户ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (role_code),
    INDEX idx_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_type VARCHAR(20) DEFAULT 'menu' COMMENT '权限类型：menu-菜单，button-按钮，api-接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父级权限ID',
    path VARCHAR(255) COMMENT '路由路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (permission_code),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user (user_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role (role_id),
    INDEX idx_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- =====================================================
-- 2. 表单设计器模块
-- =====================================================

-- 表单定义表
CREATE TABLE IF NOT EXISTS form_definition (
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
    tenant_id VARCHAR(50) DEFAULT 'default' COMMENT '租户ID',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_form_key (form_key),
    INDEX idx_tenant (tenant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单定义表';

-- 表单数据表
CREATE TABLE IF NOT EXISTS form_data (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单数据表';

-- =====================================================
-- 3. DMN 决策表模块
-- =====================================================

-- DMN 决策表定义
CREATE TABLE IF NOT EXISTS dmn_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dmn_key VARCHAR(100) NOT NULL UNIQUE COMMENT 'DMN唯一标识',
    dmn_name VARCHAR(100) NOT NULL COMMENT 'DMN名称',
    dmn_xml TEXT COMMENT 'DMN XML内容',
    dmn_config TEXT COMMENT 'DMN配置JSON',
    version INT DEFAULT 1 COMMENT '版本号',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft-草稿，published-已发布',
    deployment_id VARCHAR(100) COMMENT '部署ID',
    category VARCHAR(50) COMMENT '分类',
    description VARCHAR(255) COMMENT '描述',
    tenant_id VARCHAR(50) DEFAULT 'default' COMMENT '租户ID',
    created_by VARCHAR(50) COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_dmn_key (dmn_key),
    INDEX idx_tenant (tenant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DMN决策表定义';

-- =====================================================
-- 4. 审批记录增强模块
-- =====================================================

-- 审批意见表
CREATE TABLE IF NOT EXISTS approval_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(100) NOT NULL COMMENT '任务ID',
    process_instance_id VARCHAR(100) NOT NULL COMMENT '流程实例ID',
    comment_type VARCHAR(20) DEFAULT 'approve' COMMENT '意见类型：approve-同意，reject-驳回，transfer-转办',
    comment_text TEXT COMMENT '审批意见内容',
    attachments TEXT COMMENT '附件JSON数组',
    user_id VARCHAR(50) COMMENT '操作人ID',
    user_name VARCHAR(50) COMMENT '操作人姓名',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task (task_id),
    INDEX idx_process_instance (process_instance_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批意见表';

-- 任务转办记录表
CREATE TABLE IF NOT EXISTS task_transfer (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务转办记录表';

-- 任务加签记录表
CREATE TABLE IF NOT EXISTS task_delegate (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(100) NOT NULL COMMENT '原任务ID',
    delegate_task_id VARCHAR(100) COMMENT '加签任务ID',
    process_instance_id VARCHAR(100) NOT NULL COMMENT '流程实例ID',
    delegate_type VARCHAR(20) DEFAULT 'before' COMMENT '加签类型：before-前加签，after-后加签',
    from_user_id VARCHAR(50) COMMENT '发起人ID',
    from_user_name VARCHAR(50) COMMENT '发起人姓名',
    to_user_id VARCHAR(50) COMMENT '被加签人ID',
    to_user_name VARCHAR(50) COMMENT '被加签人姓名',
    delegate_reason VARCHAR(255) COMMENT '加签原因',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待处理，completed-已完成',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_time DATETIME COMMENT '完成时间',
    INDEX idx_task (task_id),
    INDEX idx_process_instance (process_instance_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务加签记录表';

-- =====================================================
-- 5. 系统监控模块
-- =====================================================

-- 流程统计表
CREATE TABLE IF NOT EXISTS process_statistics (
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
    INDEX idx_date (stat_date),
    INDEX idx_process (process_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程统计表';

-- 操作审计日志表
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(50) COMMENT '操作人ID',
    user_name VARCHAR(50) COMMENT '操作人姓名',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_name VARCHAR(100) COMMENT '操作名称',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    status VARCHAR(20) COMMENT '状态：success-成功，failure-失败',
    error_msg TEXT COMMENT '错误信息',
    execution_time BIGINT COMMENT '执行时长（毫秒）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_type (operation_type),
    INDEX idx_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志表';
