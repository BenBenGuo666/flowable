package com.demo.flowable.dto;

import java.time.LocalDateTime;

/**
 * 流程实例 DTO
 */
public record ProcessInstanceDTO(
        String id,
        String processDefinitionId,
        String processDefinitionName,
        String processDefinitionKey,
        String businessKey,
        Boolean suspended,
        Boolean ended,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String startUserId,
        String currentActivityId
) {
}
