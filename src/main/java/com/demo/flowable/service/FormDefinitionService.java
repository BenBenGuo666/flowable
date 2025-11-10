package com.demo.flowable.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.flowable.exception.BusinessException;
import com.demo.flowable.common.ResultCode;
import com.demo.flowable.dto.*;
import com.demo.flowable.entity.FormDefinition;
import com.demo.flowable.mapper.FormDefinitionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 表单定义服务类
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormDefinitionService {

    private final FormDefinitionMapper formDefinitionMapper;

    /**
     * 创建表单定义
     *
     * @param request 创建请求
     * @return 表单定义DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FormDefinitionDTO createFormDefinition(FormCreateRequest request) {
        log.info("创建表单定义: formKey={}, formName={}", request.formKey(), request.formName());

        // 检查formKey是否已存在
        FormDefinition existing = formDefinitionMapper.findByFormKey(request.formKey());
        if (existing != null) {
            throw new BusinessException("表单Key已存在: " + request.formKey());
        }

        // 构建表单定义实体
        FormDefinition formDefinition = FormDefinition.builder()
                .formKey(request.formKey())
                .formName(request.formName())
                .formSchema(request.formSchema())
                .formConfig(request.formConfig())
                .version(1)
                .status("draft")
                .category(request.category())
                .description(request.description())
                .tenantId(StringUtils.hasText(request.tenantId()) ? request.tenantId() : "default")
                .build();

        formDefinitionMapper.insert(formDefinition);

        log.info("表单定义创建成功: id={}, formKey={}", formDefinition.getId(), formDefinition.getFormKey());
        return convertToDTO(formDefinition);
    }

    /**
     * 更新表单定义
     *
     * @param id      表单ID
     * @param request 更新请求
     * @return 表单定义DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FormDefinitionDTO updateFormDefinition(Long id, FormUpdateRequest request) {
        log.info("更新表单定义: id={}", id);

        FormDefinition formDefinition = formDefinitionMapper.selectById(id);
        if (formDefinition == null) {
            throw new BusinessException("表单不存在: " + id);
        }

        // 只能更新草稿状态的表单
        if ("published".equals(formDefinition.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "已发布的表单不能直接修改，请创建新版本");
        }

        // 更新字段
        if (StringUtils.hasText(request.formName())) {
            formDefinition.setFormName(request.formName());
        }
        if (StringUtils.hasText(request.formSchema())) {
            formDefinition.setFormSchema(request.formSchema());
        }
        if (request.formConfig() != null) {
            formDefinition.setFormConfig(request.formConfig());
        }
        if (StringUtils.hasText(request.category())) {
            formDefinition.setCategory(request.category());
        }
        if (request.description() != null) {
            formDefinition.setDescription(request.description());
        }

        formDefinitionMapper.updateById(formDefinition);

        log.info("表单定义更新成功: id={}", id);
        return convertToDTO(formDefinition);
    }

    /**
     * 发布表单定义
     *
     * @param id 表单ID
     * @return 表单定义DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FormDefinitionDTO publishFormDefinition(Long id) {
        log.info("发布表单定义: id={}", id);

        FormDefinition formDefinition = formDefinitionMapper.selectById(id);
        if (formDefinition == null) {
            throw new BusinessException("表单不存在: " + id);
        }

        if ("published".equals(formDefinition.getStatus())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "表单已经是发布状态");
        }

        formDefinition.setStatus("published");
        formDefinitionMapper.updateById(formDefinition);

        log.info("表单定义发布成功: id={}, formKey={}", id, formDefinition.getFormKey());
        return convertToDTO(formDefinition);
    }

    /**
     * 创建新版本
     *
     * @param formKey 表单Key
     * @return 新版本表单定义DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FormDefinitionDTO createNewVersion(String formKey) {
        log.info("创建表单新版本: formKey={}", formKey);

        FormDefinition latestVersion = formDefinitionMapper.findByFormKey(formKey);
        if (latestVersion == null) {
            throw new BusinessException("表单不存在: " + formKey);
        }

        // 基于最新版本创建新版本
        FormDefinition newVersion = FormDefinition.builder()
                .formKey(latestVersion.getFormKey())
                .formName(latestVersion.getFormName())
                .formSchema(latestVersion.getFormSchema())
                .formConfig(latestVersion.getFormConfig())
                .version(latestVersion.getVersion() + 1)
                .status("draft")
                .category(latestVersion.getCategory())
                .description(latestVersion.getDescription())
                .tenantId(latestVersion.getTenantId())
                .build();

        formDefinitionMapper.insert(newVersion);

        log.info("表单新版本创建成功: id={}, formKey={}, version={}",
                newVersion.getId(), newVersion.getFormKey(), newVersion.getVersion());
        return convertToDTO(newVersion);
    }

    /**
     * 删除表单定义（逻辑删除）
     *
     * @param id 表单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFormDefinition(Long id) {
        log.info("删除表单定义: id={}", id);

        FormDefinition formDefinition = formDefinitionMapper.selectById(id);
        if (formDefinition == null) {
            throw new BusinessException("表单不存在: " + id);
        }

        formDefinitionMapper.deleteById(id);
        log.info("表单定义删除成功: id={}", id);
    }

    /**
     * 根据ID查询表单定义
     *
     * @param id 表单ID
     * @return 表单定义DTO
     */
    public FormDefinitionDTO getFormDefinitionById(Long id) {
        log.debug("查询表单定义: id={}", id);

        FormDefinition formDefinition = formDefinitionMapper.selectById(id);
        if (formDefinition == null) {
            throw new BusinessException("表单不存在: " + id);
        }

        return convertToDTO(formDefinition);
    }

    /**
     * 根据formKey查询最新版本
     *
     * @param formKey 表单Key
     * @return 表单定义DTO
     */
    public FormDefinitionDTO getLatestFormByKey(String formKey) {
        log.debug("查询表单最新版本: formKey={}", formKey);

        FormDefinition formDefinition = formDefinitionMapper.findByFormKey(formKey);
        if (formDefinition == null) {
            throw new BusinessException("表单不存在: " + formKey);
        }

        return convertToDTO(formDefinition);
    }

    /**
     * 查询表单所有版本
     *
     * @param formKey 表单Key
     * @return 表单定义DTO列表
     */
    public List<FormDefinitionDTO> getAllVersions(String formKey) {
        log.debug("查询表单所有版本: formKey={}", formKey);

        List<FormDefinition> versions = formDefinitionMapper.findAllVersionsByFormKey(formKey);
        return versions.stream().map(this::convertToDTO).toList();
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
    public IPage<FormDefinitionDTO> getFormDefinitionList(int page, int size, String status,
                                                          String category, String formName) {
        log.debug("分页查询表单定义: page={}, size={}, status={}, category={}, formName={}",
                page, size, status, category, formName);

        Page<FormDefinition> pageParam = new Page<>(page, size);
        IPage<FormDefinition> result;

        if (StringUtils.hasText(formName)) {
            // 按名称模糊查询
            result = formDefinitionMapper.searchByName(pageParam, formName);
        } else if (StringUtils.hasText(category)) {
            // 按分类查询
            result = formDefinitionMapper.findByCategory(pageParam, category);
        } else if (StringUtils.hasText(status)) {
            // 按状态查询
            LambdaQueryWrapper<FormDefinition> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FormDefinition::getStatus, status)
                    .orderByDesc(FormDefinition::getCreatedTime);
            result = formDefinitionMapper.selectPage(pageParam, wrapper);
        } else {
            // 查询所有
            LambdaQueryWrapper<FormDefinition> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(FormDefinition::getCreatedTime);
            result = formDefinitionMapper.selectPage(pageParam, wrapper);
        }

        // 转换为DTO
        return result.convert(this::convertToDTO);
    }

    /**
     * 查询已发布的表单列表
     *
     * @param page 页码
     * @param size 页大小
     * @return 表单定义分页结果
     */
    public IPage<FormDefinitionDTO> getPublishedForms(int page, int size) {
        log.debug("查询已发布表单: page={}, size={}", page, size);

        Page<FormDefinition> pageParam = new Page<>(page, size);
        IPage<FormDefinition> result = formDefinitionMapper.findPublishedForms(pageParam);

        return result.convert(this::convertToDTO);
    }

    /**
     * 转换为DTO
     *
     * @param formDefinition 表单定义实体
     * @return 表单定义DTO
     */
    private FormDefinitionDTO convertToDTO(FormDefinition formDefinition) {
        return FormDefinitionDTO.builder()
                .id(formDefinition.getId())
                .formKey(formDefinition.getFormKey())
                .formName(formDefinition.getFormName())
                .formSchema(formDefinition.getFormSchema())
                .formConfig(formDefinition.getFormConfig())
                .version(formDefinition.getVersion())
                .status(formDefinition.getStatus())
                .category(formDefinition.getCategory())
                .description(formDefinition.getDescription())
                .tenantId(formDefinition.getTenantId())
                .createdBy(formDefinition.getCreatedBy())
                .createdTime(formDefinition.getCreatedTime())
                .updatedBy(formDefinition.getUpdatedBy())
                .updatedTime(formDefinition.getUpdatedTime())
                .build();
    }
}
