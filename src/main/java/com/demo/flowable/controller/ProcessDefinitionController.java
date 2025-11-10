package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import com.demo.flowable.dto.ProcessDefinitionDTO;
import com.demo.flowable.dto.ProcessDeployDTO;
import com.demo.flowable.service.ProcessDefinitionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 流程定义管理控制器
 */
@RestController
@RequestMapping("/api/process-definition")
public class ProcessDefinitionController {

    private final ProcessDefinitionService processDefinitionService;

    public ProcessDefinitionController(ProcessDefinitionService processDefinitionService) {
        this.processDefinitionService = processDefinitionService;
    }

    /**
     * 获取流程定义列表
     */
    @GetMapping("/list")
    public Result<List<ProcessDefinitionDTO>> getProcessDefinitions() {
        return Result.success(processDefinitionService.getProcessDefinitions());
    }

    /**
     * 根据ID获取流程定义
     */
    @GetMapping("/{id}")
    public Result<ProcessDefinitionDTO> getProcessDefinitionById(@PathVariable String id) {
        ProcessDefinitionDTO definition = processDefinitionService.getProcessDefinitionById(id);
        return definition != null
                ? Result.success(definition)
                : Result.error("流程定义不存在");
    }

    /**
     * 根据Key获取流程定义的所有版本
     */
    @GetMapping("/versions/{key}")
    public Result<List<ProcessDefinitionDTO>> getProcessDefinitionsByKey(@PathVariable String key) {
        return Result.success(processDefinitionService.getProcessDefinitionsByKey(key));
    }

    /**
     * 部署流程定义
     */
    @PostMapping("/deploy")
    public Result<String> deployProcessDefinition(@RequestBody ProcessDeployDTO deployDTO) {
        try {
            String deploymentId = processDefinitionService.deployProcessDefinition(deployDTO);
            return Result.success(deploymentId, "流程部署成功");
        } catch (Exception e) {
            return Result.error("流程部署失败: " + e.getMessage());
        }
    }

    /**
     * 上传并部署BPMN文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadBpmnFile(
            @RequestParam("name") String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam("file") MultipartFile file) {
        try {
            String deploymentId = processDefinitionService.deployBpmnFile(name, category, file);
            return Result.success(deploymentId, "BPMN文件上传成功");
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除流程定义
     */
    @DeleteMapping("/{deploymentId}")
    public Result<Void> deleteProcessDefinition(
            @PathVariable String deploymentId,
            @RequestParam(defaultValue = "false") boolean cascade) {
        try {
            processDefinitionService.deleteProcessDefinition(deploymentId, cascade);
            return Result.success("流程定义删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取流程定义的BPMN XML
     */
    @GetMapping("/{id}/xml")
    public Result<String> getProcessDefinitionXml(@PathVariable String id) {
        try {
            String xml = processDefinitionService.getProcessDefinitionXml(id);
            return Result.success(xml);
        } catch (IOException e) {
            return Result.error("获取流程定义XML失败: " + e.getMessage());
        }
    }

    /**
     * 激活流程定义
     */
    @PutMapping("/{id}/activate")
    public Result<Void> activateProcessDefinition(@PathVariable String id) {
        try {
            processDefinitionService.activateProcessDefinition(id);
            return Result.success("流程定义已激活");
        } catch (Exception e) {
            return Result.error("激活失败: " + e.getMessage());
        }
    }

    /**
     * 挂起流程定义
     */
    @PutMapping("/{id}/suspend")
    public Result<Void> suspendProcessDefinition(@PathVariable String id) {
        try {
            processDefinitionService.suspendProcessDefinition(id);
            return Result.success("流程定义已挂起");
        } catch (Exception e) {
            return Result.error("挂起失败: " + e.getMessage());
        }
    }
}
