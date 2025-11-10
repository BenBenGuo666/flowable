package com.demo.flowable.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.flowable.data.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（包含角色和权限）
     */
    @Select("SELECT u.* FROM sys_user u WHERE u.username = #{username} AND u.deleted = 0")
    User selectByUsername(String username);

    /**
     * 查询用户的所有角色ID
     */
    @Select("SELECT ur.role_id FROM sys_user_role ur WHERE ur.user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);

    /**
     * 查询用户的所有权限编码
     */
    @Select("""
        SELECT DISTINCT p.permission_code
        FROM sys_permission p
        INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
        INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND p.deleted = 0
        """)
    List<String> selectPermissionCodesByUserId(Long userId);
}
