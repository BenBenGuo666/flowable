package com.demo.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 请假申请数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {

    /**
     * 申请人
     */
    @NotBlank(message = "申请人不能为空")
    @Size(min = 2, max = 50, message = "申请人姓名长度必须在2-50个字符之间")
    private String applicant;

    /**
     * 请假类型
     */
    @NotBlank(message = "请假类型不能为空")
    private String leaveType;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @Future(message = "开始日期必须是未来时间")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    @Future(message = "结束日期必须是未来时间")
    private LocalDate endDate;

    /**
     * 请假天数
     */
    @NotNull(message = "请假天数不能为空")
    @Min(value = 1, message = "请假天数至少为1天")
    @Max(value = 365, message = "请假天数不能超过365天")
    private Integer days;

    /**
     * 请假原因
     */
    @NotBlank(message = "请假原因不能为空")
    @Size(min = 5, max = 500, message = "请假原因长度必须在5-500个字符之间")
    private String reason;
}
