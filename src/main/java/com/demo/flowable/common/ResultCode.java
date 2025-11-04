package com.demo.flowable.common;

import lombok.Getter;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 响应状态码枚举
 */
@Getter
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未找到
     */
    NOT_FOUND(404, "资源未找到"),

    /**
     * 业务异常
     */
    BUSINESS_ERROR(600, "业务处理异常"),

    /**
     * 流程不存在
     */
    PROCESS_NOT_FOUND(601, "流程实例不存在"),

    /**
     * 任务不存在
     */
    TASK_NOT_FOUND(602, "任务不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
