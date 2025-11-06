package com.demo.flowable.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户DTO
 */
@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private String tenantId;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 角色列表
     */
    private List<RoleDTO> roles;
}
