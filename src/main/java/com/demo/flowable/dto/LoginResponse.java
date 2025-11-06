package com.demo.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 登录响应DTO
 */
@Data
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * 用户信息
     */
    private UserDTO userInfo;

    /**
     * 权限列表
     */
    private List<String> permissions;
}
