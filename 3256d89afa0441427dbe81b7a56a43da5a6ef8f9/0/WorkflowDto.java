package com.java110.dto.workflow;

import com.java110.dto.PageDto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @ClassName FloorDto
 * @Description 工作流数据层封装
 * @Author wuxw
 * @Date 2019/4/24 8:52
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
public class WorkflowDto extends PageDto implements Serializable {

    //10001 投诉建议 20002 报修 30003 采购
    public static final String FLOW_TYPE_COMPLAINT = "10001";
    //报修
    public static final String FLOW_TYPE_REPAIR= "20002";
    //采购
    public static final String FLOW_TYPE_PURCHASE= "30003";

    public static final String FLOW_TYPE_COLLECTION = "40004";//物品领用
    public static final String DEFAULT_SKIP_LEVEL = "1";

    public static final String DEFAULT_PROCESS = "java110_" ;

    private String skipLevel;
    private String describle;
    private String communityId;
    private String storeId;
    private String flowId;
    private String flowName;
    private String flowType;
    private String flowTypeName;
    private String processDefinitionKey;


    private Date createTime;

    private String statusCd = "0";

    private List<WorkflowStepDto> workflowSteps;



    public String getSkipLevel() {
        return skipLevel;
    }

    public void setSkipLevel(String skipLevel) {
        this.skipLevel = skipLevel;
    }

    public String getDescrible() {
        return describle;
    }

    public void setDescrible(String describle) {
        this.describle = describle;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getFlowTypeName() {
        return flowTypeName;
    }

    public void setFlowTypeName(String flowTypeName) {
        this.flowTypeName = flowTypeName;
    }

    public List<WorkflowStepDto> getWorkflowSteps() {
        return workflowSteps;
    }

    public void setWorkflowSteps(List<WorkflowStepDto> workflowSteps) {
        this.workflowSteps = workflowSteps;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }
}
