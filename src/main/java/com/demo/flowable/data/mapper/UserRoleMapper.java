package com.demo.flowable.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.flowable.data.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}
