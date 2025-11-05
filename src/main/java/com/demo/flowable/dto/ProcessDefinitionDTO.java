package com.demo.flowable.dto;

import java.time.LocalDateTime;

/**
 * 流程定义 DTO
 */
public record ProcessDefinitionDTO(
        String id,
        String key,
        String name,
        String description,
        Integer version,
        String category,
        String deploymentId,
        String resourceName,
        String tenantId,
        Boolean suspended,
        LocalDateTime deploymentTime
) {
}
