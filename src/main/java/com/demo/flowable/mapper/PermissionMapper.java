package com.demo.flowable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.flowable.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限 Mapper
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限列表
     */
    @Select("""
        SELECT p.* FROM sys_permission p
        INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
        WHERE rp.role_id = #{roleId} AND p.deleted = 0
        """)
    List<Permission> selectPermissionsByRoleId(Long roleId);

    /**
     * 查询所有菜单权限（树形结构）
     */
    @Select("SELECT * FROM sys_permission WHERE permission_type = 'menu' AND deleted = 0 ORDER BY sort_order")
    List<Permission> selectMenuPermissions();
}
