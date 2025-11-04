package com.demo.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 审批数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalDTO {

    /**
     * 是否批准
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;

    /**
     * 审批意见
     */
    @NotBlank(message = "审批意见不能为空")
    @Size(min = 2, max = 500, message = "审批意见长度必须在2-500个字符之间")
    private String comment;
}
