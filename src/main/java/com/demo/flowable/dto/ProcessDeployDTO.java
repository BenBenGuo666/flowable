package com.demo.flowable.dto;

/**
 * 流程部署 DTO
 */
public record ProcessDeployDTO(
        String name,
        String key,
        String category,
        String bpmnXml
) {
}