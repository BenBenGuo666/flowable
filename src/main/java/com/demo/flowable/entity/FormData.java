package com.demo.flowable.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 表单数据实体类
 *
 * @author flowable-ui-team
 * @since 2025-01-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("form_data")
public class FormData {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 表单Key
     */
    private String formKey;

    /**
     * 表单数据JSON
     */
    private String formData;

    /**
     * 业务Key（关联流程实例）
     */
    private String businessKey;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 任务ID
     */
    private String taskId;

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
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}
