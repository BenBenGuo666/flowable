package com.demo.flowable.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色DTO
 */
@Data
public class RoleDTO {

    private Long id;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    private String description;

    private String tenantId;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    /**
     * 权限列表
     */
    private List<PermissionDTO> permissions;
}
