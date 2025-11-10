package com.demo.flowable.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.flowable.data.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 查询角色的所有权限ID
     */
    @Select("SELECT rp.permission_id FROM sys_role_permission rp WHERE rp.role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(Long roleId);

    /**
     * 根据用户ID查询角色列表
     */
    @Select("""
        SELECT r.* FROM sys_role r
        INNER JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId} AND r.deleted = 0
        """)
    List<Role> selectRolesByUserId(Long userId);
}
