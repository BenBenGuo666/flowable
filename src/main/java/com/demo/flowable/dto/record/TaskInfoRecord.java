package com.demo.flowable.dto.record;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 任务信息 Record (JDK 21+)
 * <p>
 * 使用 Record 的优势：
 * 1. 不可变数据结构，线程安全
 * 2. 自动生成 equals、hashCode、toString
 * 3. 支持模式匹配和解构
 * 4. 代码更简洁
 * </p>
 */
public record TaskInfoRecord(
        String taskId,
        String taskName,
        String processInstanceId,
        LocalDateTime createTime,
        Map<String, Object> variables
) {
    /**
     * 紧凑构造器：参数校验
     */
    public TaskInfoRecord {
        if (taskId == null || taskId.isBlank()) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        if (taskName == null || taskName.isBlank()) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        if (processInstanceId == null || processInstanceId.isBlank()) {
            throw new IllegalArgumentException("流程实例ID不能为空");
        }
    }

    /**
     * 自定义方法：获取申请人
     */
    public String getApplicant() {
        if (variables == null) {
            return null;
        }
        return (String) variables.get("applicant");
    }

    /**
     * 自定义方法：获取请假类型
     */
    public String getLeaveType() {
        if (variables == null) {
            return null;
        }
        return (String) variables.get("leaveType");
    }

    /**
     * 自定义方法：判断是否为经理审批任务
     */
    public boolean isManagerApproval() {
        return "经理审批".equals(taskName);
    }

    /**
     * 自定义方法：判断是否为HR备案任务
     */
    public boolean isHRRecord() {
        return "HR备案".equals(taskName);
    }
}
