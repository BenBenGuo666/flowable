package com.demo.flowable.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限DTO
 */
@Data
public class PermissionDTO {

    private Long id;

    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;

    @NotBlank(message = "权限名称不能为空")
    private String permissionName;

    private String permissionType;

    private Long parentId;

    private String path;

    private String icon;

    private Integer sortOrder;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    /**
     * 子权限列表（树形结构）
     */
    private List<PermissionDTO> children;
}
