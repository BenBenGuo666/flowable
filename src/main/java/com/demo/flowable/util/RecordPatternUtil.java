package com.demo.flowable.util;

import com.demo.flowable.dto.record.ProcessInstanceRecord;
import com.demo.flowable.dto.record.TaskInfoRecord;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: Record Pattern 工具类 (JDK 21+)
 * <p>
 * 演示 JDK 21 Record Patterns 特性：
 * 1. 在 instanceof 中使用 Record Patterns
 * 2. 在 switch 表达式中使用模式匹配
 * 3. 简化数据提取
 * </p>
 */
public class RecordPatternUtil {

    /**
     * 使用 Record Patterns 解构任务信息
     * 演示模式匹配在 instanceof 中的使用
     */
    public static String describeTask(Object obj) {
        // JDK 21 Record Pattern: 直接解构 Record 的组件
        if (obj instanceof TaskInfoRecord(String taskId, String taskName, String processInstanceId, var createTime, var variables)) {
            return String.format("任务[%s] - %s (流程: %s, 创建时间: %s)",
                    taskId, taskName, processInstanceId, createTime);
        }
        return "未知对象";
    }

    /**
     * 使用 Switch 表达式 + Record Patterns
     * 根据流程实例状态返回不同的处理建议
     */
    public static String getProcessAdvice(ProcessInstanceRecord record) {
        // JDK 21 Switch Expressions with Pattern Matching
        if (record.isEnded() && !record.isActive()) {
            return "流程已结束 (ID: " + record.processInstanceId() + ")，可查看历史记录";
        } else if (!record.isEnded() && record.isActive()) {
            return "流程运行中 (ID: " + record.processInstanceId() +
                   "，定义: " + record.processDefinitionId() + ")，请耐心等待审批";
        } else {
            return "流程状态异常 (ID: " + record.processInstanceId() + ")，请联系管理员";
        }
    }

    /**
     * 任务优先级判断
     * 使用 Switch 表达式判断优先级
     */
    public static int getTaskPriority(TaskInfoRecord task) {
        String taskName = task.taskName();
        var createTime = task.createTime();

        // 经理审批任务，且创建时间超过1小时 -> 高优先级
        if (taskName.contains("经理") && isOverdue(createTime)) {
            return 1;
        }
        // HR备案任务 -> 中优先级
        else if (taskName.contains("HR")) {
            return 2;
        }
        // 其他任务 -> 普通优先级
        else {
            return 3;
        }
    }

    /**
     * 批量处理任务信息
     * 演示 Record Pattern 在流式操作中的应用
     */
    public static String batchProcessTasks(java.util.List<TaskInfoRecord> tasks) {
        return tasks.stream()
                .map(task -> {
                    String taskId = task.taskId();
                    String taskName = task.taskName();
                    String processInstanceId = task.processInstanceId();

                    if ("经理审批".equals(taskName)) {
                        return "🔴 待审批: " + taskId + " (流程: " + processInstanceId + ")";
                    } else if ("HR备案".equals(taskName)) {
                        return "🟡 待备案: " + taskId + " (流程: " + processInstanceId + ")";
                    } else {
                        return "⚪ " + taskName + ": " + taskId + " (流程: " + processInstanceId + ")";
                    }
                })
                .reduce((a, b) -> a + "\n" + b)
                .orElse("无待办任务");
    }

    /**
     * 辅助方法：判断任务是否超时
     */
    private static boolean isOverdue(java.time.LocalDateTime createTime) {
        if (createTime == null) {
            return false;
        }
        return createTime.plusHours(1).isBefore(java.time.LocalDateTime.now());
    }

    /**
     * 提取任务关键信息
     * 演示 Record 组件访问
     */
    public static String extractKeyInfo(TaskInfoRecord task) {
        // 使用 Record 的组件访问方法
        String taskId = task.taskId();
        String taskName = task.taskName();
        var variables = task.variables();

        String applicant = variables != null ? (String) variables.get("applicant") : "未知";
        return String.format("任务 %s: %s (申请人: %s)", taskId, taskName, applicant);
    }

    /**
     * 使用 instanceof 和 Record Pattern 进行类型检查和解构
     * 这是 JDK 21 正式支持的特性
     */
    public static String analyzeProcessStatus(Object obj) {
        return switch (obj) {
            case ProcessInstanceRecord record when record.isRunning() ->
                "流程运行中: " + record.processInstanceId();
            case ProcessInstanceRecord record when record.isEnded() ->
                "流程已结束: " + record.processInstanceId();
            case ProcessInstanceRecord record ->
                "流程状态未知: " + record.processInstanceId();
            case null -> "空对象";
            default -> "不是流程实例对象";
        };
    }

    /**
     * 使用 instanceof Record Pattern 进行解构和提取
     */
    public static String getTaskSummary(Object obj) {
        if (obj instanceof TaskInfoRecord(
                String taskId,
                String taskName,
                String processInstanceId,
                var createTime,
                var variables)) {

            String applicant = variables != null ? (String) variables.get("applicant") : "未知";
            return String.format("【%s】%s - 申请人: %s, 流程: %s",
                    taskId, taskName, applicant, processInstanceId);
        }
        return "非任务对象";
    }
}
