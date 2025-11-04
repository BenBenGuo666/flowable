package com.demo.flowable.service;

import com.demo.flowable.constant.FlowableConstants;
import com.demo.flowable.dto.LeaveRequestDTO;
import com.demo.flowable.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 请假流程服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;

    /**
     * 提交请假申请
     *
     * @param leaveRequest 请假申请信息
     * @return 流程实例ID
     */
    public String submitLeaveRequest(LeaveRequestDTO leaveRequest) {
        log.info("提交请假申请, 申请人: {}, 类型: {}, 天数: {}",
                leaveRequest.getApplicant(),
                leaveRequest.getLeaveType(),
                leaveRequest.getDays());

        try {
            // 准备流程变量
            Map<String, Object> variables = buildProcessVariables(leaveRequest);

            // 启动流程实例
            ProcessInstance processInstance = runtimeService
                    .startProcessInstanceByKey(FlowableConstants.LEAVE_PROCESS_KEY, variables);

            String processInstanceId = processInstance.getId();
            log.info("请假流程启动成功, 流程实例ID: {}", processInstanceId);

            return processInstanceId;
        } catch (Exception e) {
            log.error("提交请假申请失败: {}", e.getMessage(), e);
            throw new BusinessException("提交请假申请失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取待办任务列表
     *
     * @param assignee 任务处理人
     * @return 待办任务列表
     */
    public List<Map<String, Object>> getTaskList(String assignee) {
        if (!StringUtils.hasText(assignee)) {
            throw new BusinessException("任务处理人不能为空");
        }

        log.info("查询待办任务, 处理人: {}", assignee);

        try {
            List<Task> tasks = taskService.createTaskQuery()
                    .taskAssignee(assignee)
                    .orderByTaskCreateTime()
                    .desc()
                    .list();

            log.info("查询到 {} 条待办任务", tasks.size());

            return tasks.stream()
                    .map(this::buildTaskInfo)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询待办任务失败: {}", e.getMessage(), e);
            throw new BusinessException("查询待办任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 审批任务
     *
     * @param taskId   任务ID
     * @param approved 是否批准
     * @param comment  审批意见
     */
    public void approveTask(String taskId, boolean approved, String comment) {
        if (!StringUtils.hasText(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }

        log.info("审批任务, 任务ID: {}, 审批结果: {}, 意见: {}", taskId, approved, comment);

        try {
            // 验证任务是否存在
            Task task = taskService.createTaskQuery()
                    .taskId(taskId)
                    .singleResult();

            if (task == null) {
                throw new BusinessException("任务不存在，任务ID: " + taskId);
            }

            // 设置审批结果
            Map<String, Object> variables = new HashMap<>(2);
            variables.put(FlowableConstants.VAR_APPROVED, approved);
            variables.put(FlowableConstants.VAR_COMMENT, comment);

            // 完成任务
            taskService.complete(taskId, variables);

            log.info("任务审批完成, 任务ID: {}", taskId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("审批任务失败: {}", e.getMessage(), e);
            throw new BusinessException("审批任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询流程实例状态
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例信息
     */
    public Map<String, Object> getProcessInstanceInfo(String processInstanceId) {
        if (!StringUtils.hasText(processInstanceId)) {
            throw new BusinessException("流程实例ID不能为空");
        }

        log.info("查询流程实例信息, 流程实例ID: {}", processInstanceId);

        try {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();

            Map<String, Object> info = new HashMap<>(4);
            info.put("processInstanceId", processInstanceId);

            if (processInstance != null) {
                info.put("processDefinitionId", processInstance.getProcessDefinitionId());
                info.put("isEnded", false);
                info.put("isActive", true);
                log.info("流程实例运行中: {}", processInstanceId);
            } else {
                // 流程已结束
                info.put("isEnded", true);
                info.put("isActive", false);
                log.info("流程实例已结束: {}", processInstanceId);
            }

            return info;
        } catch (Exception e) {
            log.error("查询流程实例信息失败: {}", e.getMessage(), e);
            throw new BusinessException("查询流程实例信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建流程变量
     */
    private Map<String, Object> buildProcessVariables(LeaveRequestDTO leaveRequest) {
        Map<String, Object> variables = new HashMap<>(6);
        variables.put(FlowableConstants.VAR_APPLICANT, leaveRequest.getApplicant());
        variables.put(FlowableConstants.VAR_LEAVE_TYPE, leaveRequest.getLeaveType());
        variables.put(FlowableConstants.VAR_START_DATE, leaveRequest.getStartDate().toString());
        variables.put(FlowableConstants.VAR_END_DATE, leaveRequest.getEndDate().toString());
        variables.put(FlowableConstants.VAR_DAYS, leaveRequest.getDays());
        variables.put(FlowableConstants.VAR_REASON, leaveRequest.getReason());
        return variables;
    }

    /**
     * 构建任务信息
     */
    private Map<String, Object> buildTaskInfo(Task task) {
        Map<String, Object> taskMap = new HashMap<>(5);
        taskMap.put("taskId", task.getId());
        taskMap.put("taskName", task.getName());
        taskMap.put("processInstanceId", task.getProcessInstanceId());
        taskMap.put("createTime", task.getCreateTime());

        // 获取流程变量
        Map<String, Object> variables = taskService.getVariables(task.getId());
        taskMap.put("variables", variables);

        return taskMap;
    }
}
