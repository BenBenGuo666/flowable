package com.demo.flowable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 创建表单请求DTO
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Builder
public record FormCreateRequest(
        @NotBlank(message = "表单Key不能为空")
        @Size(max = 100, message = "表单Key长度不能超过100")
        @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "表单Key必须以字母开头，只能包含字母、数字和下划线")
        String formKey,

        @NotBlank(message = "表单名称不能为空")
        @Size(max = 100, message = "表单名称长度不能超过100")
        String formName,

        @NotBlank(message = "表单Schema不能为空")
        String formSchema,

        String formConfig,

        @Size(max = 50, message = "分类长度不能超过50")
        String category,

        @Size(max = 255, message = "描述长度不能超过255")
        String description,

        String tenantId
) {
}
