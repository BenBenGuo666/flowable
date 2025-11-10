package com.demo.flowable.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表单定义实体类
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("form_definition")
public class FormDefinition {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 表单唯一标识
     */
    private String formKey;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 表单JSON模式
     */
    private String formSchema;

    /**
     * 表单配置JSON
     */
    private String formConfig;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态：draft-草稿，published-已发布
     */
    private String status;

    /**
     * 分类
     */
    private String category;

    /**
     * 描述
     */
    private String description;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableLogic
    private Integer deleted;
}
