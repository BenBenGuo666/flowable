package com.demo.flowable.dto.record;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 流程实例信息 Record (JDK 21+)
 */
public record ProcessInstanceRecord(
        String processInstanceId,
        String processDefinitionId,
        boolean isEnded,
        boolean isActive
) {
    /**
     * 紧凑构造器：参数校验
     */
    public ProcessInstanceRecord {
        if (processInstanceId == null || processInstanceId.isBlank()) {
            throw new IllegalArgumentException("流程实例ID不能为空");
        }
    }

    /**
     * 判断流程是否运行中
     */
    public boolean isRunning() {
        return isActive && !isEnded;
    }

    /**
     * 获取流程状态描述
     */
    public String getStatusDescription() {
        if (isEnded) {
            return "已结束";
        } else if (isActive) {
            return "运行中";
        } else {
            return "未知状态";
        }
    }

    /**
     * 静态工厂方法：创建运行中的流程实例
     */
    public static ProcessInstanceRecord running(String processInstanceId, String processDefinitionId) {
        return new ProcessInstanceRecord(processInstanceId, processDefinitionId, false, true);
    }

    /**
     * 静态工厂方法：创建已结束的流程实例
     */
    public static ProcessInstanceRecord ended(String processInstanceId) {
        return new ProcessInstanceRecord(processInstanceId, null, true, false);
    }
}
