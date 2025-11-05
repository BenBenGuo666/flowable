package com.demo.flowable.service;

import com.demo.flowable.dto.ProcessInstanceDTO;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 流程实例服务
 */
@Service
public class ProcessInstanceService {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public ProcessInstanceService(RuntimeService runtimeService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    /**
     * 获取运行中的流程实例列表
     */
    public List<ProcessInstanceDTO> getRunningProcessInstances() {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .orderByStartTime()
                .desc()
                .list();

        return instances.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 获取所有流程实例（包括已结束）
     */
    public List<ProcessInstanceDTO> getAllProcessInstances() {
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime()
                .desc()
                .list();

        return instances.stream()
                .map(this::convertHistoricToDTO)
                .toList();
    }

    /**
     * 根据流程定义Key查询流程实例
     */
    public List<ProcessInstanceDTO> getProcessInstancesByDefinitionKey(String processDefinitionKey) {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .orderByStartTime()
                .desc()
                .list();

        return instances.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 根据ID获取流程实例详情
     */
    public ProcessInstanceDTO getProcessInstanceById(String processInstanceId) {
        // 先查运行中的
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (instance != null) {
            return convertToDTO(instance);
        }

        // 再查历史的
        HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        return historicInstance != null ? convertHistoricToDTO(historicInstance) : null;
    }

    /**
     * 挂起流程实例
     */
    @Transactional
    public void suspendProcessInstance(String processInstanceId) {
        runtimeService.suspendProcessInstanceById(processInstanceId);
    }

    /**
     * 激活流程实例
     */
    @Transactional
    public void activateProcessInstance(String processInstanceId) {
        runtimeService.activateProcessInstanceById(processInstanceId);
    }

    /**
     * 删除流程实例
     */
    @Transactional
    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
        // 同时删除历史记录
        historyService.deleteHistoricProcessInstance(processInstanceId);
    }

    /**
     * 获取流程实例的当前活动节点
     */
    public String getCurrentActivity(String processInstanceId) {
        var executions = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .list();

        if (!executions.isEmpty()) {
            return executions.get(0).getActivityId();
        }
        return null;
    }

    /**
     * 转换运行中的流程实例为DTO
     */
    private ProcessInstanceDTO convertToDTO(ProcessInstance instance) {
        LocalDateTime startTime = instance.getStartTime() != null
                ? LocalDateTime.ofInstant(instance.getStartTime().toInstant(), ZoneId.systemDefault())
                : null;

        String currentActivityId = getCurrentActivity(instance.getId());

        return new ProcessInstanceDTO(
                instance.getId(),
                instance.getProcessDefinitionId(),
                instance.getProcessDefinitionName(),
                instance.getProcessDefinitionKey(),
                instance.getBusinessKey(),
                instance.isSuspended(),
                false,
                startTime,
                null,
                instance.getStartUserId(),
                currentActivityId
        );
    }

    /**
     * 转换历史流程实例为DTO
     */
    private ProcessInstanceDTO convertHistoricToDTO(HistoricProcessInstance instance) {
        LocalDateTime startTime = instance.getStartTime() != null
                ? LocalDateTime.ofInstant(instance.getStartTime().toInstant(), ZoneId.systemDefault())
                : null;

        LocalDateTime endTime = instance.getEndTime() != null
                ? LocalDateTime.ofInstant(instance.getEndTime().toInstant(), ZoneId.systemDefault())
                : null;

        return new ProcessInstanceDTO(
                instance.getId(),
                instance.getProcessDefinitionId(),
                instance.getProcessDefinitionName(),
                instance.getProcessDefinitionKey(),
                instance.getBusinessKey(),
                false,
                endTime != null,
                startTime,
                endTime,
                instance.getStartUserId(),
                null
        );
    }
}
