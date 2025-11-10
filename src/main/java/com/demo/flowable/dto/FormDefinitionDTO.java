package com.demo.flowable.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 表单定义DTO
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Builder
public record FormDefinitionDTO(
        Long id,
        String formKey,
        String formName,
        String formSchema,
        String formConfig,
        Integer version,
        String status,
        String category,
        String description,
        String tenantId,
        String createdBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdTime,
        String updatedBy,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedTime
) {
}
