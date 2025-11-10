package com.demo.flowable.controller;

import com.demo.flowable.common.Result;
import com.demo.flowable.dto.FormDataDTO;
import com.demo.flowable.dto.FormDataSubmitRequest;
import com.demo.flowable.service.FormDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表单数据Controller
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Slf4j
@RestController
@RequestMapping("/api/form-data")
@RequiredArgsConstructor
public class FormDataController {

    private final FormDataService formDataService;

    /**
     * 提交表单数据
     *
     * @param request 提交请求
     * @return 表单数据DTO
     */
    @PostMapping("/submit")
    public Result<FormDataDTO> submitFormData(@Valid @RequestBody FormDataSubmitRequest request) {
        log.info("API - 提交表单数据: formKey={}", request.formKey());
        FormDataDTO result = formDataService.submitFormData(request);
        return Result.success("表单提交成功", result);
    }

    /**
     * 更新表单数据
     *
     * @param id       数据ID
     * @param formData 表单数据JSON
     * @return 表单数据DTO
     */
    @PutMapping("/{id}")
    public Result<FormDataDTO> updateFormData(
            @PathVariable Long id,
            @RequestBody String formData) {
        log.info("API - 更新表单数据: id={}", id);
        FormDataDTO result = formDataService.updateFormData(id, formData);
        return Result.success("表单更新成功", result);
    }

    /**
     * 根据ID查询表单数据
     *
     * @param id 数据ID
     * @return 表单数据DTO
     */
    @GetMapping("/{id}")
    public Result<FormDataDTO> getFormDataById(@PathVariable Long id) {
        log.debug("API - 查询表单数据: id={}", id);
        FormDataDTO result = formDataService.getFormDataById(id);
        return Result.success(result);
    }

    /**
     * 根据流程实例ID查询表单数据
     *
     * @param processInstanceId 流程实例ID
     * @return 表单数据DTO列表
     */
    @GetMapping("/by-process/{processInstanceId}")
    public Result<List<FormDataDTO>> getFormDataByProcessInstance(@PathVariable String processInstanceId) {
        log.debug("API - 查询流程实例表单数据: processInstanceId={}", processInstanceId);
        List<FormDataDTO> result = formDataService.getFormDataByProcessInstance(processInstanceId);
        return Result.success(result);
    }

    /**
     * 根据任务ID查询表单数据
     *
     * @param taskId 任务ID
     * @return 表单数据DTO
     */
    @GetMapping("/by-task/{taskId}")
    public Result<FormDataDTO> getFormDataByTaskId(@PathVariable String taskId) {
        log.debug("API - 查询任务表单数据: taskId={}", taskId);
        FormDataDTO result = formDataService.getFormDataByTaskId(taskId);
        return Result.success(result);
    }

    /**
     * 根据业务Key查询表单数据
     *
     * @param businessKey 业务Key
     * @return 表单数据DTO列表
     */
    @GetMapping("/by-business/{businessKey}")
    public Result<List<FormDataDTO>> getFormDataByBusinessKey(@PathVariable String businessKey) {
        log.debug("API - 查询业务表单数据: businessKey={}", businessKey);
        List<FormDataDTO> result = formDataService.getFormDataByBusinessKey(businessKey);
        return Result.success(result);
    }

    /**
     * 根据表单Key查询数据
     *
     * @param formKey 表单Key
     * @return 表单数据DTO列表
     */
    @GetMapping("/by-form-key/{formKey}")
    public Result<List<FormDataDTO>> getFormDataByFormKey(@PathVariable String formKey) {
        log.debug("API - 查询表单数据列表: formKey={}", formKey);
        List<FormDataDTO> result = formDataService.getFormDataByFormKey(formKey);
        return Result.success(result);
    }

    /**
     * 删除表单数据
     *
     * @param id 数据ID
     * @return 成功消息
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFormData(@PathVariable Long id) {
        log.info("API - 删除表单数据: id={}", id);
        formDataService.deleteFormData(id);
        return Result.success("表单数据删除成功");
    }
}
