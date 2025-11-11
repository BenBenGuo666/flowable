package com.demo.flowable.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 2.0 Token 响应 DTO
 * 符合 OAuth 2.0 规范的 Token 响应格式
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /**
     * Access Token
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Refresh Token
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * Token 类型（固定为 Bearer）
     */
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Access Token 有效期（秒）
     */
    @JsonProperty("expires_in")
    private Long expiresIn;

    /**
     * 用户信息（可选）
     */
    @JsonProperty("user")
    private UserDTO user;
}
