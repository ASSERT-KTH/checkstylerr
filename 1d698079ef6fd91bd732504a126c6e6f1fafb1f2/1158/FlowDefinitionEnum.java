package com.hlj.proj.service.flow.service.enums;

/**
 * @ClassName ServiceEnum
 * @Author TD
 * @Date 2019/6/13 10:08
 * @Description 流程节点枚举
 */
public enum FlowDefinitionEnum {

    demoJob("demoJob","Demo任务)"),

    auditJob("auditJob","审核任务)"),

    ;



    private String flowCode;
    private String flowName;

    FlowDefinitionEnum(String flowCode, String flowName) {
        this.flowCode = flowCode;
        this.flowName = flowName;
    }

    public static FlowDefinitionEnum toEnum(String flowCode) {
        for (FlowDefinitionEnum item : FlowDefinitionEnum.values()) {
            if (item.getFlowCode().equals(flowCode)) {
                return item;
            }
        }
        return null;
    }


    public String getFlowCode() {
        return flowCode;
    }

    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }
}
