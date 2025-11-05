package com.demo.flowable.service;

import com.demo.flowable.dto.ProcessTemplateDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 流程模板服务
 */
@Service
public class ProcessTemplateService {

    /**
     * 获取所有流程模板
     */
    public List<ProcessTemplateDTO> getAllTemplates() {
        return List.of(
                getLeaveTemplate(),
                getReimbursementTemplate(),
                getPurchaseTemplate(),
                getApprovalTemplate()
        );
    }

    /**
     * 根据ID获取模板
     */
    public ProcessTemplateDTO getTemplateById(String id) {
        return getAllTemplates().stream()
                .filter(template -> template.id().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 请假流程模板
     */
    private ProcessTemplateDTO getLeaveTemplate() {
        String bpmnXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xmlns:flowable="http://flowable.org/bpmn"
                             targetNamespace="http://flowable.org/test">
                  <process id="leave_process" name="请假流程" isExecutable="true">
                    <startEvent id="startEvent" name="开始"/>
                    <userTask id="applyTask" name="提交申请" flowable:assignee="${applicant}"/>
                    <userTask id="managerApprove" name="经理审批" flowable:candidateGroups="manager"/>
                    <exclusiveGateway id="decision" name="审批决策"/>
                    <endEvent id="endEvent" name="结束"/>
                    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="applyTask"/>
                    <sequenceFlow id="flow2" sourceRef="applyTask" targetRef="managerApprove"/>
                    <sequenceFlow id="flow3" sourceRef="managerApprove" targetRef="decision"/>
                    <sequenceFlow id="flow4" sourceRef="decision" targetRef="endEvent">
                      <conditionExpression xsi:type="tFormalExpression">${approved}</conditionExpression>
                    </sequenceFlow>
                    <sequenceFlow id="flow5" sourceRef="decision" targetRef="endEvent">
                      <conditionExpression xsi:type="tFormalExpression">${!approved}</conditionExpression>
                    </sequenceFlow>
                  </process>
                </definitions>
                """;

        return new ProcessTemplateDTO(
                "leave",
                "请假流程",
                "标准的请假申请和审批流程，包含员工申请、经理审批等环节",
                "人事管理",
                "📝",
                bpmnXml
        );
    }

    /**
     * 报销流程模板
     */
    private ProcessTemplateDTO getReimbursementTemplate() {
        String bpmnXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:flowable="http://flowable.org/bpmn"
                             targetNamespace="http://flowable.org/test">
                  <process id="reimbursement_process" name="报销流程" isExecutable="true">
                    <startEvent id="start" name="开始"/>
                    <userTask id="submit" name="提交报销" flowable:assignee="${applicant}"/>
                    <userTask id="managerApprove" name="部门经理审批" flowable:candidateGroups="manager"/>
                    <userTask id="financeApprove" name="财务审批" flowable:candidateGroups="finance"/>
                    <endEvent id="end" name="结束"/>
                    <sequenceFlow id="f1" sourceRef="start" targetRef="submit"/>
                    <sequenceFlow id="f2" sourceRef="submit" targetRef="managerApprove"/>
                    <sequenceFlow id="f3" sourceRef="managerApprove" targetRef="financeApprove"/>
                    <sequenceFlow id="f4" sourceRef="financeApprove" targetRef="end"/>
                  </process>
                </definitions>
                """;

        return new ProcessTemplateDTO(
                "reimbursement",
                "报销流程",
                "费用报销审批流程，包含部门经理审批和财务审批",
                "财务管理",
                "💰",
                bpmnXml
        );
    }

    /**
     * 采购流程模板
     */
    private ProcessTemplateDTO getPurchaseTemplate() {
        String bpmnXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:flowable="http://flowable.org/bpmn"
                             targetNamespace="http://flowable.org/test">
                  <process id="purchase_process" name="采购流程" isExecutable="true">
                    <startEvent id="start" name="开始"/>
                    <userTask id="apply" name="提交采购申请" flowable:assignee="${applicant}"/>
                    <userTask id="managerApprove" name="部门经理审批" flowable:candidateGroups="manager"/>
                    <userTask id="purchaseExecute" name="采购执行" flowable:candidateGroups="purchase"/>
                    <userTask id="acceptance" name="验收" flowable:assignee="${applicant}"/>
                    <endEvent id="end" name="结束"/>
                    <sequenceFlow sourceRef="start" targetRef="apply"/>
                    <sequenceFlow sourceRef="apply" targetRef="managerApprove"/>
                    <sequenceFlow sourceRef="managerApprove" targetRef="purchaseExecute"/>
                    <sequenceFlow sourceRef="purchaseExecute" targetRef="acceptance"/>
                    <sequenceFlow sourceRef="acceptance" targetRef="end"/>
                  </process>
                </definitions>
                """;

        return new ProcessTemplateDTO(
                "purchase",
                "采购流程",
                "采购申请审批流程，包含申请、审批、执行、验收等环节",
                "采购管理",
                "🛒",
                bpmnXml
        );
    }

    /**
     * 通用审批流程模板
     */
    private ProcessTemplateDTO getApprovalTemplate() {
        String bpmnXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:flowable="http://flowable.org/bpmn"
                             targetNamespace="http://flowable.org/test">
                  <process id="general_approval" name="通用审批流程" isExecutable="true">
                    <startEvent id="start" name="开始"/>
                    <userTask id="submit" name="提交申请" flowable:assignee="${applicant}"/>
                    <userTask id="approve" name="审批" flowable:candidateGroups="approver"/>
                    <endEvent id="end" name="结束"/>
                    <sequenceFlow sourceRef="start" targetRef="submit"/>
                    <sequenceFlow sourceRef="submit" targetRef="approve"/>
                    <sequenceFlow sourceRef="approve" targetRef="end"/>
                  </process>
                </definitions>
                """;

        return new ProcessTemplateDTO(
                "approval",
                "通用审批流程",
                "简单的两级审批流程，可用于各种通用审批场景",
                "通用",
                "✅",
                bpmnXml
        );
    }
}
