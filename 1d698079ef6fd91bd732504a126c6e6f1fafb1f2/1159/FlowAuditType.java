package com.hlj.proj.service.flow.service.enums;

/**
 * @ClassName FlowAuditType
 * @Author TD
 * @Date 2019/6/19 10:46
 * @Description 审批类型
 */
public enum  FlowAuditType {

    ID("ID","根据ID审批"),
    ROLE("ROLE","根据角色审批");


    private String type;
    private String desc;

    FlowAuditType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static FlowAuditType toEnum(String type) {
        for (FlowAuditType item : FlowAuditType.values()) {
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
