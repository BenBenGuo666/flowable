package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import com.demo.flowable.dto.ApprovalDTO;
import com.demo.flowable.dto.TaskDTO;
import com.demo.flowable.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通用任务控制器
 */
@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 获取我的待办任务
     */
    @GetMapping("/pending")
    public Result<List<TaskDTO>> getMyPendingTasks(@RequestParam(defaultValue = "user1") String userId) {
        return Result.success(taskService.getMyPendingTasks(userId));
    }

    /**
     * 获取我的已办任务
     */
    @GetMapping("/completed")
    public Result<List<TaskDTO>> getMyCompletedTasks(@RequestParam(defaultValue = "user1") String userId) {
        return Result.success(taskService.getMyCompletedTasks(userId));
    }

    /**
     * 获取候选任务（可认领）
     */
    @GetMapping("/candidate")
    public Result<List<TaskDTO>> getCandidateTasks(@RequestParam(defaultValue = "user1") String userId) {
        return Result.success(taskService.getCandidateTasks(userId));
    }

    /**
     * 根据ID获取任务详情
     */
    @GetMapping("/{taskId}")
    public Result<TaskDTO> getTaskById(@PathVariable String taskId) {
        TaskDTO task = taskService.getTaskById(taskId);
        return task != null
                ? Result.success(task)
                : Result.error("任务不存在");
    }

    /**
     * 完成任务（审批通过）
     */
    @PostMapping("/{taskId}/complete")
    public Result<Void> completeTask(@PathVariable String taskId, @RequestBody ApprovalDTO approvalDTO) {
        try {
            taskService.completeTask(taskId, approvalDTO);
            return Result.success("任务已完成");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 驳回任务
     */
    @PostMapping("/{taskId}/reject")
    public Result<Void> rejectTask(@PathVariable String taskId, @RequestBody ApprovalDTO approvalDTO) {
        try {
            taskService.rejectTask(taskId, approvalDTO);
            return Result.success("任务已驳回");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 转办任务
     */
    @PostMapping("/{taskId}/transfer")
    public Result<Void> transferTask(@PathVariable String taskId, @RequestParam String targetUserId) {
        try {
            taskService.transferTask(taskId, targetUserId);
            return Result.success("任务已转办");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 委派任务
     */
    @PostMapping("/{taskId}/delegate")
    public Result<Void> delegateTask(@PathVariable String taskId, @RequestParam String delegateUserId) {
        try {
            taskService.delegateTask(taskId, delegateUserId);
            return Result.success("任务已委派");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 认领任务
     */
    @PostMapping("/{taskId}/claim")
    public Result<Void> claimTask(@PathVariable String taskId, @RequestParam String userId) {
        try {
            taskService.claimTask(taskId, userId);
            return Result.success("任务已认领");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务变量
     */
    @GetMapping("/{taskId}/variables")
    public Result<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        return Result.success(taskService.getTaskVariables(taskId));
    }

    /**
     * 设置任务变量
     */
    @PutMapping("/{taskId}/variables")
    public Result<Void> setTaskVariables(@PathVariable String taskId, @RequestBody Map<String, Object> variables) {
        try {
            taskService.setTaskVariables(taskId, variables);
            return Result.success("变量已更新");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }
}
