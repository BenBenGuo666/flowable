package com.demo.flowable.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.flowable.dto.UserDTO;
import com.demo.flowable.data.entity.User;
import com.demo.flowable.data.entity.UserRole;
import com.demo.flowable.data.mapper.RoleMapper;
import com.demo.flowable.data.mapper.UserMapper;
import com.demo.flowable.data.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建用户
     *
     * @param userDTO 用户DTO
     * @return 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserDTO userDTO) {
        log.info("创建用户: {}", userDTO.getUsername());

        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(userDTO.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在: " + userDTO.getUsername());
        }

        // 转换DTO为实体
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 加密密码
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // 默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 保存用户
        userMapper.insert(user);
        log.info("用户创建成功, ID: {}", user.getId());

        // 分配角色
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), userDTO.getRoleIds());
        }

        return user.getId();
    }

    /**
     * 更新用户
     *
     * @param id      用户ID
     * @param userDTO 用户DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserDTO userDTO) {
        log.info("更新用户: ID={}", id);

        // 检查用户是否存在
        User existUser = userMapper.selectById(id);
        if (existUser == null) {
            throw new RuntimeException("用户不存在: " + id);
        }

        // 如果修改了用户名，检查用户名是否已被占用
        if (StringUtils.hasText(userDTO.getUsername()) && !userDTO.getUsername().equals(existUser.getUsername())) {
            User duplicateUser = userMapper.selectByUsername(userDTO.getUsername());
            if (duplicateUser != null) {
                throw new RuntimeException("用户名已存在: " + userDTO.getUsername());
            }
        }

        // 转换DTO为实体
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setId(id);

        // 如果提供了新密码，进行加密
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }

        // 更新用户
        userMapper.updateById(user);
        log.info("用户更新成功, ID: {}", id);

        // 更新角色关联
        if (userDTO.getRoleIds() != null) {
            // 删除旧的角色关联
            LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserRole::getUserId, id);
            userRoleMapper.delete(wrapper);

            // 添加新的角色关联
            if (!userDTO.getRoleIds().isEmpty()) {
                assignRoles(id, userDTO.getRoleIds());
            }
        }
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        log.info("删除用户: ID={}", id);

        // 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + id);
        }

        // 删除用户（逻辑删除）
        userMapper.deleteById(id);

        // 删除用户角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, id);
        userRoleMapper.delete(wrapper);

        log.info("用户删除成功, ID: {}", id);
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户DTO
     */
    public UserDTO getUserById(Long id) {
        log.info("查询用户: ID={}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + id);
        }

        // 转换为DTO
        UserDTO userDTO = convertToDTO(user);

        // 查询用户角色
        userDTO.setRoleIds(userMapper.selectRoleIdsByUserId(id));
        userDTO.setRoles(roleMapper.selectRolesByUserId(id).stream()
                .map(role -> {
                    var roleDTO = new com.demo.flowable.dto.RoleDTO();
                    BeanUtils.copyProperties(role, roleDTO);
                    return roleDTO;
                })
                .collect(Collectors.toList()));

        return userDTO;
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户DTO
     */
    public UserDTO getUserByUsername(String username) {
        log.info("根据用户名查询用户: {}", username);

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }

        // 转换为DTO
        UserDTO userDTO = convertToDTO(user);

        // 查询用户角色
        userDTO.setRoleIds(userMapper.selectRoleIdsByUserId(user.getId()));

        return userDTO;
    }

    /**
     * 获取用户列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词（用户名、真实姓名）
     * @return 用户列表
     */
    public Page<UserDTO> getUserList(int pageNum, int pageSize, String keyword) {
        log.info("查询用户列表: pageNum={}, pageSize={}, keyword={}", pageNum, pageSize, keyword);

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }
        wrapper.orderByDesc(User::getCreatedTime);

        // 分页查询
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> userPage = userMapper.selectPage(page, wrapper);

        // 转换为DTO
        Page<UserDTO> dtoPage = new Page<>(pageNum, pageSize);
        dtoPage.setTotal(userPage.getTotal());
        dtoPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return dtoPage;
    }

    /**
     * 为用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        log.info("为用户分配角色: userId={}, roleIds={}", userId, roleIds);

        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }

        // 删除旧的角色关联
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId);
        userRoleMapper.delete(wrapper);

        // 添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }

        log.info("角色分配成功");
    }

    /**
     * 将User实体转换为UserDTO
     *
     * @param user 用户实体
     * @return 用户DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        // 不返回密码
        userDTO.setPassword(null);
        return userDTO;
    }
}
