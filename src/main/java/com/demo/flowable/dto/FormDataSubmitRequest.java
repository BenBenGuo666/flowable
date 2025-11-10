package com.demo.flowable.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * 表单数据提交请求DTO
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Builder
public record FormDataSubmitRequest(
        @NotBlank(message = "表单Key不能为空")
        String formKey,

        @NotBlank(message = "表单数据不能为空")
        String formData,

        String businessKey,

        String processInstanceId,

        String taskId
) {
}
