package com.demo.flowable.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.demo.flowable.common.Result;
import com.demo.flowable.dto.*;
import com.demo.flowable.service.FormDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表单定义Controller
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Slf4j
@RestController
@RequestMapping("/api/form-definition")
@RequiredArgsConstructor
public class FormDefinitionController {

    private final FormDefinitionService formDefinitionService;

    /**
     * 创建表单定义
     *
     * @param request 创建请求
     * @return 表单定义DTO
     */
    @PostMapping
    public Result<FormDefinitionDTO> createFormDefinition(@Valid @RequestBody FormCreateRequest request) {
        log.info("API - 创建表单定义: formKey={}", request.formKey());
        FormDefinitionDTO result = formDefinitionService.createFormDefinition(request);
        return Result.success("表单创建成功", result);
    }

    /**
     * 更新表单定义
     *
     * @param id      表单ID
     * @param request 更新请求
     * @return 表单定义DTO
     */
    @PutMapping("/{id}")
    public Result<FormDefinitionDTO> updateFormDefinition(
            @PathVariable Long id,
            @Valid @RequestBody FormUpdateRequest request) {
        log.info("API - 更新表单定义: id={}", id);
        FormDefinitionDTO result = formDefinitionService.updateFormDefinition(id, request);
        return Result.success("表单更新成功", result);
    }

    /**
     * 发布表单定义
     *
     * @param id 表单ID
     * @return 表单定义DTO
     */
    @PostMapping("/{id}/publish")
    public Result<FormDefinitionDTO> publishFormDefinition(@PathVariable Long id) {
        log.info("API - 发布表单定义: id={}", id);
        FormDefinitionDTO result = formDefinitionService.publishFormDefinition(id);
        return Result.success("表单发布成功", result);
    }

    /**
     * 创建新版本
     *
     * @param formKey 表单Key
     * @return 新版本表单定义DTO
     */
    @PostMapping("/{formKey}/new-version")
    public Result<FormDefinitionDTO> createNewVersion(@PathVariable String formKey) {
        log.info("API - 创建表单新版本: formKey={}", formKey);
        FormDefinitionDTO result = formDefinitionService.createNewVersion(formKey);
        return Result.success("新版本创建成功", result);
    }

    /**
     * 删除表单定义
     *
     * @param id 表单ID
     * @return 成功消息
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFormDefinition(@PathVariable Long id) {
        log.info("API - 删除表单定义: id={}", id);
        formDefinitionService.deleteFormDefinition(id);
        return Result.success("表单删除成功");
    }

    /**
     * 根据ID查询表单定义
     *
     * @param id 表单ID
     * @return 表单定义DTO
     */
    @GetMapping("/{id}")
    public Result<FormDefinitionDTO> getFormDefinitionById(@PathVariable Long id) {
        log.debug("API - 查询表单定义: id={}", id);
        FormDefinitionDTO result = formDefinitionService.getFormDefinitionById(id);
        return Result.success(result);
    }

    /**
     * 根据formKey查询最新版本
     *
     * @param formKey 表单Key
     * @return 表单定义DTO
     */
    @GetMapping("/by-key/{formKey}")
    public Result<FormDefinitionDTO> getLatestFormByKey(@PathVariable String formKey) {
        log.debug("API - 查询表单最新版本: formKey={}", formKey);
        FormDefinitionDTO result = formDefinitionService.getLatestFormByKey(formKey);
        return Result.success(result);
    }

    /**
     * 查询表单所有版本
     *
     * @param formKey 表单Key
     * @return 表单定义DTO列表
     */
    @GetMapping("/versions/{formKey}")
    public Result<List<FormDefinitionDTO>> getAllVersions(@PathVariable String formKey) {
        log.debug("API - 查询表单所有版本: formKey={}", formKey);
        List<FormDefinitionDTO> result = formDefinitionService.getAllVersions(formKey);
        return Result.success(result);
    }

    /**
     * 分页查询表单定义列表
     *
     * @param page     页码
     * @param size     页大小
     * @param status   状态
     * @param category 分类
     * @param formName 表单名称（模糊查询）
     * @return 表单定义分页结果
     */
    @GetMapping("/list")
    public Result<IPage<FormDefinitionDTO>> getFormDefinitionList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String formName) {
        log.debug("API - 分页查询表单定义: page={}, size={}", page, size);
        IPage<FormDefinitionDTO> result = formDefinitionService.getFormDefinitionList(
                page, size, status, category, formName);
        return Result.success(result);
    }

    /**
     * 查询已发布的表单列表
     *
     * @param page 页码
     * @param size 页大小
     * @return 表单定义分页结果
     */
    @GetMapping("/published")
    public Result<IPage<FormDefinitionDTO>> getPublishedForms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.debug("API - 查询已发布表单: page={}, size={}", page, size);
        IPage<FormDefinitionDTO> result = formDefinitionService.getPublishedForms(page, size);
        return Result.success(result);
    }
}
