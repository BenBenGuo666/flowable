package com.demo.flowable.service;

import com.demo.flowable.exception.BusinessException;
import com.demo.flowable.common.ResultCode;
import com.demo.flowable.dto.FormDataDTO;
import com.demo.flowable.dto.FormDataSubmitRequest;
import com.demo.flowable.entity.FormData;
import com.demo.flowable.mapper.FormDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 表单数据服务类
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormDataService {

    private final FormDataMapper formDataMapper;

    /**
     * 提交表单数据
     *
     * @param request 提交请求
     * @return 表单数据DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FormDataDTO submitFormData(FormDataSubmitRequest request) {
        log.info("提交表单数据: formKey={}, taskId={}, processInstanceId={}",
                request.formKey(), request.taskId(), request.processInstanceId());

        FormData formData = FormData.builder()
                .formKey(request.formKey())
                .formData(request.formData())
                .businessKey(request.businessKey())
                .processInstanceId(request.processInstanceId())
                .taskId(request.taskId())
                .build();

        formDataMapper.insert(formData);

        log.info("表单数据提交成功: id={}, formKey={}", formData.getId(), formData.getFormKey());
        return convertToDTO(formData);
    }

    /**
     * 更新表单数据
     *
     * @param id      数据ID
     * @param formDataJson 表单数据JSON
     * @return 表单数据DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public FormDataDTO updateFormData(Long id, String formDataJson) {
        log.info("更新表单数据: id={}", id);

        FormData formData = formDataMapper.selectById(id);
        if (formData == null) {
            throw new BusinessException("表单数据不存在: " + id);
        }

        formData.setFormData(formDataJson);
        formDataMapper.updateById(formData);

        log.info("表单数据更新成功: id={}", id);
        return convertToDTO(formData);
    }

    /**
     * 根据ID查询表单数据
     *
     * @param id 数据ID
     * @return 表单数据DTO
     */
    public FormDataDTO getFormDataById(Long id) {
        log.debug("查询表单数据: id={}", id);

        FormData formData = formDataMapper.selectById(id);
        if (formData == null) {
            throw new BusinessException("表单数据不存在: " + id);
        }

        return convertToDTO(formData);
    }

    /**
     * 根据流程实例ID查询表单数据
     *
     * @param processInstanceId 流程实例ID
     * @return 表单数据DTO列表
     */
    public List<FormDataDTO> getFormDataByProcessInstance(String processInstanceId) {
        log.debug("查询流程实例表单数据: processInstanceId={}", processInstanceId);

        List<FormData> formDataList = formDataMapper.findByProcessInstanceId(processInstanceId);
        return formDataList.stream().map(this::convertToDTO).toList();
    }

    /**
     * 根据任务ID查询表单数据
     *
     * @param taskId 任务ID
     * @return 表单数据DTO
     */
    public FormDataDTO getFormDataByTaskId(String taskId) {
        log.debug("查询任务表单数据: taskId={}", taskId);

        FormData formData = formDataMapper.findByTaskId(taskId);
        if (formData == null) {
            throw new BusinessException("任务表单数据不存在: " + taskId);
        }

        return convertToDTO(formData);
    }

    /**
     * 根据业务Key查询表单数据
     *
     * @param businessKey 业务Key
     * @return 表单数据DTO列表
     */
    public List<FormDataDTO> getFormDataByBusinessKey(String businessKey) {
        log.debug("查询业务表单数据: businessKey={}", businessKey);

        List<FormData> formDataList = formDataMapper.findByBusinessKey(businessKey);
        return formDataList.stream().map(this::convertToDTO).toList();
    }

    /**
     * 根据表单Key查询数据
     *
     * @param formKey 表单Key
     * @return 表单数据DTO列表
     */
    public List<FormDataDTO> getFormDataByFormKey(String formKey) {
        log.debug("查询表单数据列表: formKey={}", formKey);

        List<FormData> formDataList = formDataMapper.findByFormKey(formKey);
        return formDataList.stream().map(this::convertToDTO).toList();
    }

    /**
     * 删除表单数据
     *
     * @param id 数据ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFormData(Long id) {
        log.info("删除表单数据: id={}", id);

        FormData formData = formDataMapper.selectById(id);
        if (formData == null) {
            throw new BusinessException("表单数据不存在: " + id);
        }

        formDataMapper.deleteById(id);
        log.info("表单数据删除成功: id={}", id);
    }

    /**
     * 转换为DTO
     *
     * @param formData 表单数据实体
     * @return 表单数据DTO
     */
    private FormDataDTO convertToDTO(FormData formData) {
        return FormDataDTO.builder()
                .id(formData.getId())
                .formKey(formData.getFormKey())
                .formData(formData.getFormData())
                .businessKey(formData.getBusinessKey())
                .processInstanceId(formData.getProcessInstanceId())
                .taskId(formData.getTaskId())
                .createdBy(formData.getCreatedBy())
                .createdTime(formData.getCreatedTime())
                .updatedTime(formData.getUpdatedTime())
                .build();
    }
}
