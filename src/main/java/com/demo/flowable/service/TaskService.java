package com.demo.flowable.service;

import com.demo.flowable.dto.ApprovalDTO;
import com.demo.flowable.dto.TaskDTO;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用任务服务
 */
@Service
public class TaskService {

    private final org.flowable.engine.TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public TaskService(org.flowable.engine.TaskService taskService,
                      RuntimeService runtimeService,
                      HistoryService historyService) {
        this.taskService = taskService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    /**
     * 获取用户的待办任务
     */
    public List<TaskDTO> getMyPendingTasks(String userId) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime()
                .desc()
                .list();

        return tasks.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 获取用户的已办任务
     */
    public List<TaskDTO> getMyCompletedTasks(String userId) {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();

        return tasks.stream()
                .map(this::convertHistoricToDTO)
                .toList();
    }

    /**
     * 根据ID获取任务详情
     */
    public TaskDTO getTaskById(String taskId) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();

        return task != null ? convertToDTO(task) : null;
    }

    /**
     * 完成任务（审批通过）
     */
    @Transactional
    public void completeTask(String taskId, ApprovalDTO approvalDTO) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);
        variables.put("comment", approvalDTO.getComment());

        taskService.complete(taskId, variables);
    }

    /**
     * 驳回任务
     */
    @Transactional
    public void rejectTask(String taskId, ApprovalDTO approvalDTO) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);
        variables.put("comment", approvalDTO.getComment());

        taskService.complete(taskId, variables);
    }

    /**
     * 转办任务
     */
    @Transactional
    public void transferTask(String taskId, String targetUserId) {
        taskService.setAssignee(taskId, targetUserId);
    }

    /**
     * 委派任务
     */
    @Transactional
    public void delegateTask(String taskId, String delegateUserId) {
        taskService.delegateTask(taskId, delegateUserId);
    }

    /**
     * 认领任务（从候选人列表中）
     */
    @Transactional
    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    /**
     * 获取候选任务（可认领的任务）
     */
    public List<TaskDTO> getCandidateTasks(String userId) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .orderByTaskCreateTime()
                .desc()
                .list();

        return tasks.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 获取任务的流程变量
     */
    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    /**
     * 设置任务变量
     */
    @Transactional
    public void setTaskVariables(String taskId, Map<String, Object> variables) {
        taskService.setVariables(taskId, variables);
    }

    /**
     * 转换为 DTO
     */
    private TaskDTO convertToDTO(Task task) {
        LocalDateTime createTime = task.getCreateTime() != null
                ? LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault())
                : null;

        LocalDateTime dueDate = task.getDueDate() != null
                ? LocalDateTime.ofInstant(task.getDueDate().toInstant(), ZoneId.systemDefault())
                : null;

        return new TaskDTO(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getProcessInstanceId(),
                task.getProcessDefinitionId(),
                task.getProcessDefinitionId(), // 简化处理，实际应查询流程定义名称
                task.getProcessInstanceId(), // 简化处理，实际应查询业务键
                task.getAssignee(),
                task.getOwner(),
                createTime,
                dueDate,
                task.getPriority(),
                task.getFormKey(),
                task.getCategory()
        );
    }

    /**
     * 转换历史任务为 DTO
     */
    private TaskDTO convertHistoricToDTO(HistoricTaskInstance task) {
        LocalDateTime createTime = task.getCreateTime() != null
                ? LocalDateTime.ofInstant(task.getCreateTime().toInstant(), ZoneId.systemDefault())
                : null;

        LocalDateTime dueDate = task.getDueDate() != null
                ? LocalDateTime.ofInstant(task.getDueDate().toInstant(), ZoneId.systemDefault())
                : null;

        return new TaskDTO(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getProcessInstanceId(),
                task.getProcessDefinitionId(),
                task.getProcessDefinitionId(),
                task.getProcessInstanceId(),
                task.getAssignee(),
                task.getOwner(),
                createTime,
                dueDate,
                task.getPriority(),
                task.getFormKey(),
                task.getCategory()
        );
    }
}
