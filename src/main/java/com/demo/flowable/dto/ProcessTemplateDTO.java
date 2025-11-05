package com.demo.flowable.dto;

/**
 * 流程模板 DTO
 */
public record ProcessTemplateDTO(
        String id,
        String name,
        String description,
        String category,
        String icon,
        String bpmnXml
) {
}
