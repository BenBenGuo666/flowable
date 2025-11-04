package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import com.demo.flowable.dto.ApprovalDTO;
import com.demo.flowable.dto.LeaveRequestDTO;
import com.demo.flowable.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 请假流程控制器
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    /**
     * 提交请假申请
     *
     * @param leaveRequest 请假申请信息
     * @return 响应结果
     */
    @PostMapping("/submit")
    public Result<Map<String, String>> submitLeaveRequest(@Valid @RequestBody LeaveRequestDTO leaveRequest) {
        log.info("接收请假申请: {}", leaveRequest);

        String processInstanceId = leaveService.submitLeaveRequest(leaveRequest);

        Map<String, String> data = new HashMap<>(1);
        data.put("processInstanceId", processInstanceId);

        return Result.success("请假申请提交成功", data);
    }

    /**
     * 获取待办任务列表
     *
     * @param assignee 任务处理人
     * @return 待办任务列表
     */
    @GetMapping("/tasks")
    public Result<Map<String, Object>> getTaskList(
            @NotBlank(message = "任务处理人不能为空") @RequestParam String assignee) {
        log.info("查询待办任务列表, 处理人: {}", assignee);

        List<Map<String, Object>> tasks = leaveService.getTaskList(assignee);

        Map<String, Object> data = new HashMap<>(2);
        data.put("tasks", tasks);
        data.put("count", tasks.size());

        return Result.success(data);
    }

    /**
     * 审批任务
     *
     * @param taskId      任务ID
     * @param approvalDTO 审批数据
     * @return 响应结果
     */
    @PostMapping("/approve/{taskId}")
    public Result<Void> approveTask(
            @NotBlank(message = "任务ID不能为空") @PathVariable String taskId,
            @Valid @RequestBody ApprovalDTO approvalDTO) {
        log.info("审批任务, 任务ID: {}, 审批数据: {}", taskId, approvalDTO);

        leaveService.approveTask(taskId, approvalDTO.getApproved(), approvalDTO.getComment());

        return Result.success("审批完成", null);
    }

    /**
     * 查询流程实例状态
     *
     * @param processInstanceId 流程实例ID
     * @return 流程实例信息
     */
    @GetMapping("/process/{processInstanceId}")
    public Result<Map<String, Object>> getProcessInstanceInfo(
            @NotBlank(message = "流程实例ID不能为空") @PathVariable String processInstanceId) {
        log.info("查询流程实例状态, 流程实例ID: {}", processInstanceId);

        Map<String, Object> info = leaveService.getProcessInstanceInfo(processInstanceId);

        return Result.success(info);
    }
}
