package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import com.demo.flowable.dto.LoginRequest;
import com.demo.flowable.dto.LoginResponse;
import com.demo.flowable.dto.UserDTO;
import com.demo.flowable.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含token和用户信息）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return Result.success("登录成功", loginResponse);
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     *
     * @param userDTO 用户信息
     * @return 用户ID
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody UserDTO userDTO) {
        log.info("用户注册: {}", userDTO.getUsername());
        try {
            Long userId = authService.register(userDTO);
            return Result.success("注册成功", userId);
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息（从JWT token）
     *
     * @param authorization Authorization Header（格式：Bearer token）
     * @return 当前用户信息
     */
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        log.info("获取当前用户信息");
        try {
            // 从Authorization header中提取token（格式：Bearer token）
            String token = authorization;
            if (authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            }

            UserDTO userDTO = authService.getCurrentUser(token);
            return Result.success(userDTO);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }
}
