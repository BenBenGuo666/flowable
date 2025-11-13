package com.demo.flowable.constant;

/**
 * 权限常量
 * 定义系统中所有的权限点，用于方法级权限控制
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
public class PermissionConstant {

    // ==================== 用户管理权限 ====================

    /**
     * 用户管理 - 创建用户
     */
    public static final String USER_CREATE = "user:create";

    /**
     * 用户管理 - 更新用户
     */
    public static final String USER_UPDATE = "user:update";

    /**
     * 用户管理 - 删除用户
     */
    public static final String USER_DELETE = "user:delete";

    /**
     * 用户管理 - 查看用户
     */
    public static final String USER_VIEW = "user:view";

    /**
     * 用户管理 - 分配角色
     */
    public static final String USER_ASSIGN_ROLE = "user:assign_role";

    // ==================== 角色管理权限 ====================

    /**
     * 角色管理 - 创建角色
     */
    public static final String ROLE_CREATE = "role:create";

    /**
     * 角色管理 - 更新角色
     */
    public static final String ROLE_UPDATE = "role:update";

    /**
     * 角色管理 - 删除角色
     */
    public static final String ROLE_DELETE = "role:delete";

    /**
     * 角色管理 - 查看角色
     */
    public static final String ROLE_VIEW = "role:view";

    /**
     * 角色管理 - 分配权限
     */
    public static final String ROLE_ASSIGN_PERMISSION = "role:assign_permission";

    // ==================== 权限管理 ====================

    /**
     * 权限管理 - 创建权限
     */
    public static final String PERMISSION_CREATE = "permission:create";

    /**
     * 权限管理 - 更新权限
     */
    public static final String PERMISSION_UPDATE = "permission:update";

    /**
     * 权限管理 - 删除权限
     */
    public static final String PERMISSION_DELETE = "permission:delete";

    /**
     * 权限管理 - 查看权限
     */
    public static final String PERMISSION_VIEW = "permission:view";

    // ==================== 流程管理权限 ====================

    /**
     * 流程管理 - 部署流程
     */
    public static final String PROCESS_DEPLOY = "process:deploy";

    /**
     * 流程管理 - 删除流程
     */
    public static final String PROCESS_DELETE = "process:delete";

    /**
     * 流程管理 - 激活/挂起流程
     */
    public static final String PROCESS_MANAGE = "process:manage";

    /**
     * 流程管理 - 查看流程
     */
    public static final String PROCESS_VIEW = "process:view";

    // ==================== 表单管理权限 ====================

    /**
     * 表单管理 - 创建表单
     */
    public static final String FORM_CREATE = "form:create";

    /**
     * 表单管理 - 更新表单
     */
    public static final String FORM_UPDATE = "form:update";

    /**
     * 表单管理 - 删除表单
     */
    public static final String FORM_DELETE = "form:delete";

    /**
     * 表单管理 - 发布表单
     */
    public static final String FORM_PUBLISH = "form:publish";

    /**
     * 表单管理 - 查看表单
     */
    public static final String FORM_VIEW = "form:view";

    // ==================== 任务管理权限 ====================

    /**
     * 任务管理 - 审批任务
     */
    public static final String TASK_APPROVE = "task:approve";

    /**
     * 任务管理 - 驳回任务
     */
    public static final String TASK_REJECT = "task:reject";

    /**
     * 任务管理 - 转交任务
     */
    public static final String TASK_TRANSFER = "task:transfer";

    /**
     * 任务管理 - 委派任务
     */
    public static final String TASK_DELEGATE = "task:delegate";

    /**
     * 任务管理 - 认领任务
     */
    public static final String TASK_CLAIM = "task:claim";

    /**
     * 任务管理 - 查看任务
     */
    public static final String TASK_VIEW = "task:view";

    // ==================== 系统管理权限 ====================

    /**
     * 系统管理 - 查看日志
     */
    public static final String SYSTEM_LOG_VIEW = "system:log:view";

    /**
     * 系统管理 - 查看监控
     */
    public static final String SYSTEM_MONITOR_VIEW = "system:monitor:view";

    // ==================== 角色预定义 ====================

    /**
     * 超级管理员角色（拥有所有权限）
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * 普通用户角色
     */
    public static final String ROLE_USER = "ROLE_USER";

    /**
     * 流程管理员角色
     */
    public static final String ROLE_PROCESS_ADMIN = "ROLE_PROCESS_ADMIN";

    private PermissionConstant() {
        // 工具类，禁止实例化
    }
}
