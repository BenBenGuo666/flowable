-- =====================================================
-- Flowable 工作流系统 - 初始数据
-- =====================================================

-- =====================================================
-- 1. 初始化用户（密码统一为：123456）
-- BCrypt 加密后的密码：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi
-- =====================================================

INSERT INTO sys_user (id, username, password, real_name, email, phone, status, tenant_id, created_by) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@flowable.com', '13800138000', 1, 'default', 'system'),
(2, 'manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '部门经理', 'manager@flowable.com', '13800138001', 1, 'default', 'system'),
(3, 'employee', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '普通员工', 'employee@flowable.com', '13800138002', 1, 'default', 'system');

-- =====================================================
-- 2. 初始化角色
-- =====================================================

INSERT INTO sys_role (id, role_code, role_name, description, tenant_id) VALUES
(1, 'ROLE_ADMIN', '系统管理员', '系统管理员，拥有所有权限', 'default'),
(2, 'ROLE_MANAGER', '部门经理', '部门经理，可审批流程', 'default'),
(3, 'ROLE_EMPLOYEE', '普通员工', '普通员工，可发起流程', 'default'),
(4, 'ROLE_DESIGNER', '流程设计师', '流程设计师，可设计流程和表单', 'default');

-- =====================================================
-- 3. 初始化权限
-- =====================================================

-- 一级菜单
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
(1, 'dashboard', '工作台', 'menu', 0, '/dashboard', 'dashboard', 1),
(2, 'process', '流程管理', 'menu', 0, '/process', 'workflow', 2),
(3, 'task', '任务中心', 'menu', 0, '/task', 'task', 3),
(4, 'form', '表单管理', 'menu', 0, '/form', 'form', 4),
(5, 'dmn', '决策表管理', 'menu', 0, '/dmn', 'rule', 5),
(6, 'identity', '身份权限', 'menu', 0, '/identity', 'user', 6),
(7, 'monitor', '系统监控', 'menu', 0, '/monitor', 'monitor', 7);

-- 流程管理子菜单
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
(10, 'process:designer', '流程设计器', 'menu', 2, '/process/designer', NULL, 1),
(11, 'process:definitions', '流程定义', 'menu', 2, '/process/definitions', NULL, 2),
(12, 'process:instances', '流程实例', 'menu', 2, '/process/instances', NULL, 3),
(13, 'process:templates', '流程模板', 'menu', 2, '/process/templates', NULL, 4);

-- 任务中心子菜单
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
(20, 'task:my-tasks', '我的任务', 'menu', 3, '/task/my-tasks', NULL, 1),
(21, 'task:approve', '审批任务', 'button', 3, NULL, NULL, 2),
(22, 'task:transfer', '转办任务', 'button', 3, NULL, NULL, 3),
(23, 'task:delegate', '加签任务', 'button', 3, NULL, NULL, 4);

-- 表单管理子菜单
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
(30, 'form:designer', '表单设计器', 'menu', 4, '/form/designer', NULL, 1),
(31, 'form:list', '表单列表', 'menu', 4, '/form/list', NULL, 2),
(32, 'form:data', '表单数据', 'menu', 4, '/form/data', NULL, 3);

-- DMN 决策表子菜单
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
(40, 'dmn:designer', 'DMN设计器', 'menu', 5, '/dmn/designer', NULL, 1),
(41, 'dmn:list', '决策表列表', 'menu', 5, '/dmn/list', NULL, 2),
(42, 'dmn:test', '决策表测试', 'menu', 5, '/dmn/test', NULL, 3);

-- 身份权限子菜单
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
(50, 'identity:users', '用户管理', 'menu', 6, '/identity/users', NULL, 1),
(51, 'identity:roles', '角色管理', 'menu', 6, '/identity/roles', NULL, 2),
(52, 'identity:permissions', '权限管理', 'menu', 6, '/identity/permissions', NULL, 3);

-- 系统监控子菜单
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
(60, 'monitor:statistics', '统计报表', 'menu', 7, '/monitor/statistics', NULL, 1),
(61, 'monitor:health', '健康检查', 'menu', 7, '/monitor/health', NULL, 2),
(62, 'monitor:audit', '审计日志', 'menu', 7, '/monitor/audit', NULL, 3);

-- API 权限
INSERT INTO sys_permission (id, permission_code, permission_name, permission_type, parent_id, path, icon, sort_order) VALUES
(100, 'api:process:deploy', '流程部署', 'api', 2, '/process-definition/deploy', NULL, 0),
(101, 'api:process:delete', '流程删除', 'api', 2, '/process-definition/delete', NULL, 0),
(102, 'api:user:create', '创建用户', 'api', 6, '/user/create', NULL, 0),
(103, 'api:user:delete', '删除用户', 'api', 6, '/user/delete', NULL, 0),
(104, 'api:role:create', '创建角色', 'api', 6, '/role/create', NULL, 0),
(105, 'api:role:delete', '删除角色', 'api', 6, '/role/delete', NULL, 0);

-- =====================================================
-- 4. 初始化用户角色关联
-- =====================================================

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> ROLE_ADMIN
(1, 4),  -- admin -> ROLE_DESIGNER
(2, 2),  -- manager -> ROLE_MANAGER
(3, 3);  -- employee -> ROLE_EMPLOYEE

-- =====================================================
-- 5. 初始化角色权限关联
-- =====================================================

-- 管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = 0;

-- 部门经理权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1),  -- 工作台
(2, 3),  -- 任务中心
(2, 20), -- 我的任务
(2, 21), -- 审批任务
(2, 22), -- 转办任务
(2, 2),  -- 流程管理
(2, 12), -- 流程实例
(2, 7),  -- 系统监控
(2, 60); -- 统计报表

-- 普通员工权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1),  -- 工作台
(3, 3),  -- 任务中心
(3, 20), -- 我的任务
(3, 21); -- 审批任务

-- 流程设计师权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(4, 1),  -- 工作台
(4, 2),  -- 流程管理
(4, 10), -- 流程设计器
(4, 11), -- 流程定义
(4, 12), -- 流程实例
(4, 13), -- 流程模板
(4, 4),  -- 表单管理
(4, 30), -- 表单设计器
(4, 31), -- 表单列表
(4, 32), -- 表单数据
(4, 5),  -- 决策表管理
(4, 40), -- DMN设计器
(4, 41), -- 决策表列表
(4, 42), -- 决策表测试
(4, 100),-- 流程部署
(4, 101);-- 流程删除

-- =====================================================
-- 6. 初始化示例表单
-- =====================================================

INSERT INTO form_definition (form_key, form_name, form_type, category, description, status, tenant_id, created_by) VALUES
('leave_form', '请假申请表', 'process', '人事管理', '员工请假申请表单', 'published', 'default', 'admin'),
('expense_form', '报销申请表', 'process', '财务管理', '员工报销申请表单', 'published', 'default', 'admin');

-- =====================================================
-- 7. 初始化流程统计数据（示例）
-- =====================================================

INSERT INTO process_statistics (stat_date, process_key, start_count, complete_count, running_count, avg_duration) VALUES
(CURDATE(), 'leave_process', 0, 0, 0, 0),
(CURDATE(), 'expense_process', 0, 0, 0, 0);
