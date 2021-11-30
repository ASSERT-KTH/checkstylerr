package com.hlj.proj.service.flow.service.enums;

/**
 * @author HealerJean
 * @ClassName FlowNodeNameEnum
 * @date 2019-08-11  22:14.
 * @Description
 */
public interface FlowNodeNameEnum {




     enum FlowServiceNodeEnum {


        demoJobSubmit("demoJobSubmit","任务提交","ServiceNode"),
        demoAJobDeal("demoAJobDeal","任务A处理","ServiceNode"),
        demoBJobDeal("demoBJobDeal","任务B处理","ServiceNode"),
        demoCJobDeal("demoCJobDeal","任务C处理","ServiceNode"),
        demoJobsuccess("demoJobsuccess","任务完成","ServiceNode"),



        auditJobSubmit("auditJobSubmit","审核任务提交","ServiceNode"),
        auditServiceAJobDeal("auditServiceAJobDeal","审核任务ServiceA处理","ServiceNode"),
        auditServiceBJobDeal("auditServiceBJobDeal","审核任务ServiceB处理","ServiceNode"),
        auditJobsuccess("auditJobsuccess","审核任务完成","ServiceNode"),


        ;



        private String nodeCode;
        private String nodeName;
        private String nodeType;

        FlowServiceNodeEnum(String nodeCode, String nodeName, String nodeType) {
            this.nodeCode = nodeCode;
            this.nodeName = nodeName;
            this.nodeType = nodeType;
        }

        public static FlowServiceNodeEnum toEnum(String nodeCode) {
            for (FlowServiceNodeEnum item : FlowServiceNodeEnum.values()) {
                if (item.getNodeCode().equals(nodeCode)) {
                    return item;
                }
            }
            return null;
        }


        public String getNodeCode() {
            return nodeCode;
        }

        public void setNodeCode(String nodeCode) {
            this.nodeCode = nodeCode;
        }

        public String getNodeName() {
            return nodeName;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }
    }


     enum FlowAuditNodeEnum {

        auditAJobDeal("auditAJobDeal","审核任务A处理"),
        auditBJobDeal("auditBJobDeal","审核任务B处理"),

        ;

        private String type;
        private String desc;

        FlowAuditNodeEnum(String type, String desc) {
            this.type = type;
            this.desc = desc;
        }

        public static FlowAuditNodeEnum toEnum(String type) {
            for (FlowAuditNodeEnum item : FlowAuditNodeEnum.values()) {
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


}
