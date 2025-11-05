package com.demo.flowable.dto;

import java.time.LocalDateTime;

/**
 * 任务 DTO - 通用任务对象
 */
public record TaskDTO(
        String id,
        String name,
        String description,
        String processInstanceId,
        String processDefinitionId,
        String processDefinitionName,
        String businessKey,
        String assignee,
        String owner,
        LocalDateTime createTime,
        LocalDateTime dueDate,
        Integer priority,
        String formKey,
        String category
) {
}
