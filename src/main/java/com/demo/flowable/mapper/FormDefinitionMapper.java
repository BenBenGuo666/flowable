package com.demo.flowable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.flowable.entity.FormDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 表单定义Mapper接口
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Mapper
public interface FormDefinitionMapper extends BaseMapper<FormDefinition> {

    /**
     * 根据formKey查询表单定义
     *
     * @param formKey 表单Key
     * @return 表单定义
     */
    @Select("SELECT * FROM form_definition WHERE form_key = #{formKey} AND deleted = 0 ORDER BY version DESC LIMIT 1")
    FormDefinition findByFormKey(@Param("formKey") String formKey);

    /**
     * 根据formKey查询所有版本
     *
     * @param formKey 表单Key
     * @return 表单定义列表
     */
    @Select("SELECT * FROM form_definition WHERE form_key = #{formKey} AND deleted = 0 ORDER BY version DESC")
    List<FormDefinition> findAllVersionsByFormKey(@Param("formKey") String formKey);

    /**
     * 查询已发布的表单
     *
     * @param page 分页参数
     * @return 表单定义分页
     */
    @Select("SELECT * FROM form_definition WHERE status = 'published' AND deleted = 0 ORDER BY created_time DESC")
    IPage<FormDefinition> findPublishedForms(Page<FormDefinition> page);

    /**
     * 根据分类查询表单
     *
     * @param page     分页参数
     * @param category 分类
     * @return 表单定义分页
     */
    @Select("SELECT * FROM form_definition WHERE category = #{category} AND deleted = 0 ORDER BY created_time DESC")
    IPage<FormDefinition> findByCategory(Page<FormDefinition> page, @Param("category") String category);

    /**
     * 模糊查询表单名称
     *
     * @param page     分页参数
     * @param formName 表单名称
     * @return 表单定义分页
     */
    @Select("SELECT * FROM form_definition WHERE form_name LIKE CONCAT('%', #{formName}, '%') AND deleted = 0 ORDER BY created_time DESC")
    IPage<FormDefinition> searchByName(Page<FormDefinition> page, @Param("formName") String formName);
}
