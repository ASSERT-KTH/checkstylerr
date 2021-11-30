package com.hlj.proj.service.flow.service.enums;

/**
 * @ClassName ServiceEnum
 * @Author TD
 * @Date 2019/6/13 10:08
 * @Description 流程节点枚举
 */
public enum FlowNodeTypeEnum {

    AuditNode("AuditNode","审批节点"),
    ServiceNode("ServiceNode","业务节点");

    private String type;
    private String desc;

    FlowNodeTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static FlowNodeTypeEnum toEnum(String type) {
        for (FlowNodeTypeEnum item : FlowNodeTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
