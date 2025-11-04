package com.demo.flowable.constant;

/**
 * @author: e-Benben.Guo
 * @date: 2025/11
 * @desc: 流程相关常量
 */
public class FlowableConstants {

    /**
     * 流程定义Key
     */
    public static final String LEAVE_PROCESS_KEY = "leaveProcess";

    /**
     * 流程变量Key
     */
    public static final String VAR_APPLICANT = "applicant";
    public static final String VAR_LEAVE_TYPE = "leaveType";
    public static final String VAR_START_DATE = "startDate";
    public static final String VAR_END_DATE = "endDate";
    public static final String VAR_DAYS = "days";
    public static final String VAR_REASON = "reason";
    public static final String VAR_APPROVED = "approved";
    public static final String VAR_COMMENT = "comment";

    /**
     * 任务处理人
     */
    public static final String ASSIGNEE_MANAGER = "manager";
    public static final String ASSIGNEE_HR = "hr";

    /**
     * 请假类型
     */
    public static final String LEAVE_TYPE_ANNUAL = "年假";
    public static final String LEAVE_TYPE_SICK = "病假";
    public static final String LEAVE_TYPE_PERSONAL = "事假";
    public static final String LEAVE_TYPE_MATERNITY = "产假";
    public static final String LEAVE_TYPE_PATERNITY = "陪产假";
    public static final String LEAVE_TYPE_MARRIAGE = "婚假";
    public static final String LEAVE_TYPE_BEREAVEMENT = "丧假";

    private FlowableConstants() {
        throw new IllegalStateException("Constant class");
    }
}
