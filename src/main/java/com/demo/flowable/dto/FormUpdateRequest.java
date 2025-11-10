package com.demo.flowable.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * 更新表单请求DTO
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Builder
public record FormUpdateRequest(
        @Size(max = 100, message = "表单名称长度不能超过100")
        String formName,

        String formSchema,

        String formConfig,

        @Size(max = 50, message = "分类长度不能超过50")
        String category,

        @Size(max = 255, message = "描述长度不能超过255")
        String description
) {
}
