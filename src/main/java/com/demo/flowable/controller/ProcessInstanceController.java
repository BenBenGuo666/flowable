package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import com.demo.flowable.dto.ProcessInstanceDTO;
import com.demo.flowable.service.ProcessInstanceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程实例监控控制器
 */
@RestController
@RequestMapping("/process-instance")
public class ProcessInstanceController {

    private final ProcessInstanceService processInstanceService;

    public ProcessInstanceController(ProcessInstanceService processInstanceService) {
        this.processInstanceService = processInstanceService;
    }

    /**
     * 获取运行中的流程实例列表
     */
    @GetMapping("/running")
    public Result<List<ProcessInstanceDTO>> getRunningProcessInstances() {
        return Result.success(processInstanceService.getRunningProcessInstances());
    }

    /**
     * 获取所有流程实例（包括已结束）
     */
    @GetMapping("/all")
    public Result<List<ProcessInstanceDTO>> getAllProcessInstances() {
        return Result.success(processInstanceService.getAllProcessInstances());
    }

    /**
     * 根据流程定义Key查询流程实例
     */
    @GetMapping("/by-key/{key}")
    public Result<List<ProcessInstanceDTO>> getProcessInstancesByDefinitionKey(@PathVariable String key) {
        return Result.success(processInstanceService.getProcessInstancesByDefinitionKey(key));
    }

    /**
     * 根据ID获取流程实例详情
     */
    @GetMapping("/{id}")
    public Result<ProcessInstanceDTO> getProcessInstanceById(@PathVariable String id) {
        ProcessInstanceDTO instance = processInstanceService.getProcessInstanceById(id);
        return instance != null
                ? Result.success(instance)
                : Result.error("流程实例不存在");
    }

    /**
     * 挂起流程实例
     */
    @PutMapping("/{id}/suspend")
    public Result<Void> suspendProcessInstance(@PathVariable String id) {
        try {
            processInstanceService.suspendProcessInstance(id);
            return Result.success("流程实例已挂起");
        } catch (Exception e) {
            return Result.error("挂起失败: " + e.getMessage());
        }
    }

    /**
     * 激活流程实例
     */
    @PutMapping("/{id}/activate")
    public Result<Void> activateProcessInstance(@PathVariable String id) {
        try {
            processInstanceService.activateProcessInstance(id);
            return Result.success("流程实例已激活");
        } catch (Exception e) {
            return Result.error("激活失败: " + e.getMessage());
        }
    }

    /**
     * 删除流程实例
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProcessInstance(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "手动删除") String reason) {
        try {
            processInstanceService.deleteProcessInstance(id, reason);
            return Result.success("流程实例已删除");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取流程实例的当前活动节点
     */
    @GetMapping("/{id}/current-activity")
    public Result<String> getCurrentActivity(@PathVariable String id) {
        String activityId = processInstanceService.getCurrentActivity(id);
        return activityId != null
                ? Result.success(activityId)
                : Result.error("未找到当前活动节点");
    }
}
