package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import com.demo.flowable.dto.ProcessTemplateDTO;
import com.demo.flowable.service.ProcessTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程模板控制器
 */
@RestController
@RequestMapping("/process-template")
public class ProcessTemplateController {

    private final ProcessTemplateService processTemplateService;

    public ProcessTemplateController(ProcessTemplateService processTemplateService) {
        this.processTemplateService = processTemplateService;
    }

    /**
     * 获取所有流程模板
     */
    @GetMapping("/list")
    public Result<List<ProcessTemplateDTO>> getAllTemplates() {
        return Result.success(processTemplateService.getAllTemplates());
    }

    /**
     * 根据ID获取流程模板
     */
    @GetMapping("/{id}")
    public Result<ProcessTemplateDTO> getTemplateById(@PathVariable String id) {
        ProcessTemplateDTO template = processTemplateService.getTemplateById(id);
        return template != null
                ? Result.success(template)
                : Result.error("模板不存在");
    }
}
