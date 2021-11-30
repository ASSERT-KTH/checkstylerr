package com.hlj.proj.service.flow.base.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.hlj.proj.dto.user.IdentityInfoDTO;
import com.hlj.proj.utils.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName AuditorFlowNode
 * @Description 审核节点
 */
@Data
@Slf4j
public class AuditorFlowNode extends FlowNode {

    /**
     * 审批流程控制器
     */
    private AuditorProcess auditorProcess;

    /**
     * 返回流程暂停
     * 1、最开始创建的审批的饿时候 auditorProcess.getAuditSept()== null
     * @param data
     * @return
     */
    @Override
    public Result deal(String instantsNo, String data, IdentityInfoDTO identityInfo) {
        if (auditorProcess.getAuditSept().intValue() == 0) {
            //创建审批记录，并将审批步揍加1
            auditorProcess.createAuditRecord(instantsNo, this.getNodeCode(), 1, data, identityInfo);
        }
        if (auditorProcess.isReject()) {
            log.info("执行审核流程 ：{}，拒绝，失败 ", getNodeName());
            return Result.fail(data);
        } else if (auditorProcess.isAuditsComplete()) {
            log.info("执行审核流程 ：{}，成功 ", getNodeName());
            return Result.success(data);
        }
        log.info("执行审核流程 ：{}，待审批 ", getNodeName());
        return Result.suspend(data);
    }


    @Override
    protected void fail(String instantsNo, String data, IdentityInfoDTO identityInfo) {

    }
}
