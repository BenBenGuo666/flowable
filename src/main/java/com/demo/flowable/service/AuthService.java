package com.demo.flowable.service;

import com.demo.flowable.dto.LoginRequest;
import com.demo.flowable.dto.LoginResponse;
import com.demo.flowable.dto.UserDTO;
import com.demo.flowable.entity.User;
import com.demo.flowable.mapper.UserMapper;
import com.demo.flowable.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 认证服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含token和用户信息）
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());

        // 根据用户名查询用户
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        // 查询用户权限
        List<String> permissions = userMapper.selectPermissionCodesByUserId(user.getId());

        // 构造用户DTO
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setPassword(null); // 不返回密码

        log.info("用户登录成功: {}", loginRequest.getUsername());

        return new LoginResponse(token, userDTO, permissions);
    }

    /**
     * 用户注册
     *
     * @param userDTO 用户信息
     * @return 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserDTO userDTO) {
        log.info("用户注册: {}", userDTO.getUsername());

        // 验证必填字段
        if (!StringUtils.hasText(userDTO.getUsername())) {
            throw new RuntimeException("用户名不能为空");
        }
        if (!StringUtils.hasText(userDTO.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }

        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(userDTO.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 转换DTO为实体
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 加密密码
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        // 默认状态为启用
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        // 保存用户
        userMapper.insert(user);
        log.info("用户注册成功, ID: {}", user.getId());

        return user.getId();
    }

    /**
     * 根据Token获取当前用户信息
     *
     * @param token JWT Token
     * @return 用户信息
     */
    public UserDTO getCurrentUser(String token) {
        log.info("获取当前用户信息");

        // 从Token中获取用户名
        String username = jwtUtil.getUsernameFromToken(token);
        if (!StringUtils.hasText(username)) {
            throw new RuntimeException("无效的Token");
        }

        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 构造用户DTO
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        userDTO.setPassword(null); // 不返回密码

        // 查询用户角色
        userDTO.setRoleIds(userMapper.selectRoleIdsByUserId(user.getId()));

        return userDTO;
    }
}
