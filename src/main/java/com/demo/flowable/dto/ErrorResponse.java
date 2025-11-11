package com.demo.flowable.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth 2.0 错误响应 DTO
 * 符合 OAuth 2.0 规范的错误响应格式
 *
 * @author e-Benben.Guo
 * @date 2025/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * 错误代码
     * 标准错误代码：
     * - invalid_request: 请求参数无效
     * - invalid_grant: 授权无效（如用户名密码错误）
     * - invalid_token: Token 无效
     * - unauthorized_client: 客户端未授权
     * - unsupported_grant_type: 不支持的授权类型
     */
    @JsonProperty("error")
    private String error;

    /**
     * 错误描述
     */
    @JsonProperty("error_description")
    private String errorDescription;

    /**
     * 错误详细信息 URI（可选）
     */
    @JsonProperty("error_uri")
    private String errorUri;

    /**
     * 创建标准错误响应
     *
     * @param error 错误代码
     * @param description 错误描述
     * @return ErrorResponse
     */
    public static ErrorResponse of(String error, String description) {
        return ErrorResponse.builder()
                .error(error)
                .errorDescription(description)
                .build();
    }

    /**
     * 创建 invalid_grant 错误（用户名或密码错误）
     */
    public static ErrorResponse invalidGrant(String description) {
        return of("invalid_grant", description);
    }

    /**
     * 创建 invalid_token 错误（Token 无效）
     */
    public static ErrorResponse invalidToken(String description) {
        return of("invalid_token", description);
    }

    /**
     * 创建 invalid_request 错误（请求参数无效）
     */
    public static ErrorResponse invalidRequest(String description) {
        return of("invalid_request", description);
    }
}
