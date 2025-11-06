package com.demo.flowable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.flowable.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
