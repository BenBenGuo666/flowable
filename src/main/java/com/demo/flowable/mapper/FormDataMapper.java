package com.demo.flowable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.flowable.entity.FormData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 表单数据Mapper接口
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Mapper
public interface FormDataMapper extends BaseMapper<FormData> {

    /**
     * 根据流程实例ID查询表单数据
     *
     * @param processInstanceId 流程实例ID
     * @return 表单数据列表
     */
    @Select("SELECT * FROM form_data WHERE process_instance_id = #{processInstanceId} ORDER BY created_time DESC")
    List<FormData> findByProcessInstanceId(@Param("processInstanceId") String processInstanceId);

    /**
     * 根据任务ID查询表单数据
     *
     * @param taskId 任务ID
     * @return 表单数据
     */
    @Select("SELECT * FROM form_data WHERE task_id = #{taskId} ORDER BY created_time DESC LIMIT 1")
    FormData findByTaskId(@Param("taskId") String taskId);

    /**
     * 根据业务Key查询表单数据
     *
     * @param businessKey 业务Key
     * @return 表单数据列表
     */
    @Select("SELECT * FROM form_data WHERE business_key = #{businessKey} ORDER BY created_time DESC")
    List<FormData> findByBusinessKey(@Param("businessKey") String businessKey);

    /**
     * 根据表单Key查询数据
     *
     * @param formKey 表单Key
     * @return 表单数据列表
     */
    @Select("SELECT * FROM form_data WHERE form_key = #{formKey} ORDER BY created_time DESC LIMIT 100")
    List<FormData> findByFormKey(@Param("formKey") String formKey);
}
