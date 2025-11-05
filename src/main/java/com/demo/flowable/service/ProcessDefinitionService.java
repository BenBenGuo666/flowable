package com.demo.flowable.service;

import com.demo.flowable.dto.ProcessDefinitionDTO;
import com.demo.flowable.dto.ProcessDeployDTO;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 流程定义服务
 */
@Service
public class ProcessDefinitionService {

    private final RepositoryService repositoryService;

    public ProcessDefinitionService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /**
     * 获取流程定义列表
     */
    public List<ProcessDefinitionDTO> getProcessDefinitions() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey()
                .asc()
                .list();

        return definitions.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 根据ID获取流程定义
     */
    public ProcessDefinitionDTO getProcessDefinitionById(String id) {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(id)
                .singleResult();

        return definition != null ? convertToDTO(definition) : null;
    }

    /**
     * 根据Key获取流程定义的所有版本
     */
    public List<ProcessDefinitionDTO> getProcessDefinitionsByKey(String key) {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .orderByProcessDefinitionVersion()
                .desc()
                .list();

        return definitions.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 部署流程定义
     */
    @Transactional
    public String deployProcessDefinition(ProcessDeployDTO deployDTO) {
        String resourceName = deployDTO.key() + ".bpmn20.xml";

        Deployment deployment = repositoryService.createDeployment()
                .name(deployDTO.name())
                .category(deployDTO.category())
                .addString(resourceName, deployDTO.bpmnXml())
                .deploy();

        return deployment.getId();
    }

    /**
     * 上传并部署BPMN文件
     */
    @Transactional
    public String deployBpmnFile(String name, String category, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        Deployment deployment = repositoryService.createDeployment()
                .name(name)
                .category(category)
                .addInputStream(fileName, file.getInputStream())
                .deploy();

        return deployment.getId();
    }

    /**
     * 删除流程定义
     */
    @Transactional
    public void deleteProcessDefinition(String deploymentId, boolean cascade) {
        repositoryService.deleteDeployment(deploymentId, cascade);
    }

    /**
     * 获取流程定义的BPMN XML
     */
    public String getProcessDefinitionXml(String processDefinitionId) throws IOException {
        ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
        var inputStream = repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(),
                processDefinition.getResourceName()
        );

        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 激活流程定义
     */
    @Transactional
    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
    }

    /**
     * 挂起流程定义
     */
    @Transactional
    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
    }

    /**
     * 转换为DTO
     */
    private ProcessDefinitionDTO convertToDTO(ProcessDefinition definition) {
        var deployment = repositoryService.createDeploymentQuery()
                .deploymentId(definition.getDeploymentId())
                .singleResult();

        LocalDateTime deploymentTime = deployment.getDeploymentTime() != null
                ? LocalDateTime.ofInstant(deployment.getDeploymentTime().toInstant(), ZoneId.systemDefault())
                : null;

        return new ProcessDefinitionDTO(
                definition.getId(),
                definition.getKey(),
                definition.getName(),
                definition.getDescription(),
                definition.getVersion(),
                definition.getCategory(),
                definition.getDeploymentId(),
                definition.getResourceName(),
                definition.getTenantId(),
                definition.isSuspended(),
                deploymentTime
        );
    }
}
