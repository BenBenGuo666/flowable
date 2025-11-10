package com.demo.flowable.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 表单数据DTO
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Builder
public record FormDataDTO(
        Long id,
        String formKey,
        String formData,
        String businessKey,
        String processInstanceId,
        String taskId,
        String createdBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedTime
) {
}
