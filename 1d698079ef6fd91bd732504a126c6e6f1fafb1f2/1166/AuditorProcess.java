package com.hlj.proj.service.flow.base.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hlj.proj.data.dao.mybatis.manager.flow.*;
import com.hlj.proj.data.pojo.flow.*;
import com.hlj.proj.dto.user.IdentityInfoDTO;
import com.hlj.proj.enums.StatusEnum;
import com.hlj.proj.exception.BusinessException;
import com.hlj.proj.service.flow.service.dto.AuditorResultDTO;
import com.hlj.proj.service.flow.service.enums.FlowAuditType;
import com.hlj.proj.service.spring.SpringContextHolder;
import com.hlj.proj.utils.EmptyUtil;
import com.hlj.proj.utils.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName AuditorProcess
 * @Description 审批流程
 */
@Data
@Slf4j
public class AuditorProcess {

    /**
     * 审批链条
     */
    private String instantsNo;
    /**
     * 审批人
     */
    private List<Auditor> auditors;

    /**
     * 审批执行步骤
     */
    private AtomicInteger auditSept;

    /**
     * 初始化审批节点
     * 1、有审批记录，将它的审批步揍以及审批状态赋值给Auditors
     * 2、无审批记录，只保存Auditors ，不需要设置审批步揍，审批步揍为NULL
     */
    public static AuditorProcess initAuditProcess(String nodeCode, String nodeDetail, String instantsNo, int index) {
        AuditorProcess auditorProcess = new AuditorProcess();

        //初始化审批流程
        ScfFlowAuditRecordManager scfFlowAuditRecordManager = SpringContextHolder.getBean(ScfFlowAuditRecordManager.class);
        //查询该节点在该步奏中所有的记录（有可能有多个审批步奏）
        ScfFlowAuditRecordQuery query = new ScfFlowAuditRecordQuery();
        query.setInstantsNo(instantsNo);
        query.setNodeCode(nodeCode);
        query.setSept(index + 1);
        List<ScfFlowAuditRecord> auditRecords = scfFlowAuditRecordManager.queryList(query);

        //初始化审批人数据
        ArrayList<Auditor> auditors = JsonUtils.toObject(nodeDetail, new TypeReference<ArrayList<Auditor>>() { });
        for (Auditor auditor : auditors) {
            auditor.setStatus(Result.AuditStatusEnum.UNKNOWN.getCode());
        }
        auditorProcess.setAuditSept(new AtomicInteger(0));
        if (!EmptyUtil.isEmpty(auditRecords)) {
            int auditSeptMax = 0 ;
            for (int i = 0; i < auditRecords.size(); i++) {
                ScfFlowAuditRecord scfFlowAuditRecord = auditRecords.get(i);
                Integer auditSept = scfFlowAuditRecord.getAuditSept();
                Auditor auditor = auditors.get(auditSept - 1);
                auditor.setStatus(scfFlowAuditRecord.getStatus());
                if (auditSept > auditSeptMax ) {
                    auditSeptMax = auditSept;
                }
            }
            //设置当前审批到了第几步奏
            auditorProcess.setAuditSept(new AtomicInteger(auditSeptMax));
        }
        auditorProcess.setInstantsNo(instantsNo);
        auditorProcess.setAuditors(auditors);
        return auditorProcess ;
    }


    /**
     * 获取一个正在暂停中等待审批的审批进程
     * @param auditRecordId
     * @return
     */
    public static AuditorProcess ofSuspendAuditorProcess(Long auditRecordId) {
        AuditorProcess auditorProcess = new AuditorProcess();
        //查询审批记录
        ScfFlowAuditRecordManager scfFlowAuditRecordManager = SpringContextHolder.getBean(ScfFlowAuditRecordManager.class);
        ScfFlowAuditRecord auditRecord = scfFlowAuditRecordManager.findById(auditRecordId);

        //初始化审批流程
        //查询该节点在该步奏中所有的记录（有可能有多个审批步奏）
        ScfFlowAuditRecordQuery query = new ScfFlowAuditRecordQuery();
        query.setInstantsNo(auditRecord.getInstantsNo());
        query.setNodeCode(auditRecord.getNodeCode());
        query.setSept(auditRecord.getSept());
        List<ScfFlowAuditRecord> auditRecords = scfFlowAuditRecordManager.queryList(query);

        //初始化审批人数据
        //查询流程节点
        ScfFlowNodeManager scfFlowNodeManager = SpringContextHolder.getBean(ScfFlowNodeManager.class);
        ScfFlowNodeQuery scfFlowNodeQuery = new ScfFlowNodeQuery();
        scfFlowNodeQuery.setNodeCode(auditRecord.getNodeCode());
        ScfFlowNode scfFlowNode = scfFlowNodeManager.findByQueryContion(scfFlowNodeQuery);
        ArrayList<Auditor> auditors = JsonUtils.toObject(scfFlowNode.getAuditors(), new TypeReference<ArrayList<Auditor>>() { });
        for (Auditor auditor : auditors) {
            auditor.setStatus(Result.AuditStatusEnum.UNKNOWN.getCode());
        }
        if (!EmptyUtil.isEmpty(auditRecords)) {
            for (ScfFlowAuditRecord scfFlowAuditRecord :  auditRecords) {
                Integer auditSept = scfFlowAuditRecord.getAuditSept();
                Auditor auditor = auditors.get(auditSept - 1);
                auditor.setStatus(scfFlowAuditRecord.getStatus());
            }
        }
        auditorProcess.setInstantsNo(auditRecord.getInstantsNo());
        auditorProcess.setAuditors(auditors);
        auditorProcess.setAuditSept(new AtomicInteger(auditRecord.getAuditSept()));
        return auditorProcess;
    }


    /**
     * 同意(true)/(false)拒绝当前审批
     */
    public void audit(AuditorResultDTO auditorResultDTO, IdentityInfoDTO identityInfo) {
        JpaTransactionManager transactionManager = SpringContextHolder.getBean(JpaTransactionManager.class);
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            //根据审批步揍获取当前审批人
            int i = auditSept.intValue();
            Auditor auditor = auditors.get(i - 1);
            //先判断是否成功
            Boolean auditSuccess = auditorResultDTO.getAuditResult();
            if (auditSuccess) {
                auditor.setStatus(Result.AuditStatusEnum.Success.getCode());
            } else {
                auditor.setStatus(Result.AuditStatusEnum.Reject.getCode());
            }

            ScfFlowAuditRecordManager scfFlowAuditRecordManager = SpringContextHolder.getBean(ScfFlowAuditRecordManager.class);
            ScfFlowAuditRecord auditRecord = scfFlowAuditRecordManager.findById(auditorResultDTO.getAuditRecordId());
            //保存 审核记录 数据
            auditRecord.setStatus(auditor.getStatus());
            auditRecord.setOptUser(identityInfo.getUserId());
            auditRecord.setOptUserName(identityInfo.getRealName());
            auditRecord.setOptTime(new Date());
            auditRecord.setAuditMessage(auditorResultDTO.getAuditMessage());
            auditRecord.setRefFileIds(EmptyUtil.isEmpty( auditorResultDTO.getFileIds()) == true ? null : JsonUtils.toJsonString(auditorResultDTO.getFileIds()));
            scfFlowAuditRecordManager.updateSelective(auditRecord);

            //保存审批日志
            saveAuditLog(auditRecord,identityInfo);

            String auditData = auditRecord.getAuditData();
            //当审批流程全部走完时，则触发流程的下一步
            if (isAuditsComplete()) {
                //审批完成，给抄送人创建信息
                sendToCopyUser(auditRecord);
                Process process = ProcessDefinition.ofSuspendProcess(instantsNo);
                process.nextFlow(instantsNo, auditData, identityInfo);
            }else {
                //创建下一个审批记录
                //审批步揍 加 1
                createAuditRecord(auditRecord.getInstantsNo(), auditRecord.getNodeCode(), auditSept.intValue()+1, auditRecord.getAuditData(),identityInfo);
                log.info("执行流程名 {}, 执行第{}步：{}，实例编号：{}执行下一个审批：执行第{}步", auditRecord.getFlowName(), auditRecord.getSept(), auditRecord.getNodeName(), instantsNo,auditSept.intValue());
            }
            transactionManager.commit(status);
        } catch (BusinessException e) {
            log.error("审批错误", e);
            transactionManager.rollback(status);
            throw e;
        } catch (Exception e) {
            log.error("审批错误", e);
            transactionManager.rollback(status);
            throw new BusinessException("审批错误");
        }
    }



    /**
     * 判断该审批节点是否全部完成
     * @return
     */
    public Boolean isAuditsComplete() {
        int i = auditSept.intValue();
        Auditor auditor = auditors.get(i - 1);
        return (auditSept.intValue()) == auditors.size() &&
                (Result.AuditStatusEnum.Success.getCode().equals(auditor.getStatus())
                        || Result.AuditStatusEnum.Reject.getCode().equals(auditor.getStatus())   ) ;
    }

    /**
     * 审批拒绝
     */
    public boolean isReject() {
        int i = auditSept.intValue();
        Auditor auditor = auditors.get(i - 1);
        return Result.AuditStatusEnum.Reject.getCode().equals(auditor.getStatus());
    }


    /**
     * 创建审批记录
     */
    public void createAuditRecord(String instantsNo, String nodeCode, Integer auditSept, String data,IdentityInfoDTO identityInfo) {
        this.auditSept = new AtomicInteger(auditSept);
        int i = auditSept.intValue() ;
        auditors.get(i-1).setStatus(Result.AuditStatusEnum.Suspend.getCode());

        ScfFlowRecordManager scfFlowRecordManager = SpringContextHolder.getBean(ScfFlowRecordManager.class);
        ScfFlowRecordQuery scfFlowRecordQuery = new ScfFlowRecordQuery();
        scfFlowRecordQuery.setInstantsNo(instantsNo);
        scfFlowRecordQuery.setStatus(Result.StatusEnum.Suspend.getCode());
        scfFlowRecordQuery.setNodeCode(nodeCode);
        ScfFlowRecord scfFlowRecord = scfFlowRecordManager.findByQueryContion(scfFlowRecordQuery);
        if (scfFlowRecord == null) {
            log.error("执行时找不到对应的流程记录，instantsNo：{}", instantsNo);
            throw new BusinessException("执行时找不到对应的流程记录");
        }

        //创建第一个审核记录
        ScfFlowAuditRecord scfFlowAuditRecord = new ScfFlowAuditRecord();
        scfFlowAuditRecord.setInstantsNo(scfFlowRecord.getInstantsNo());
        scfFlowAuditRecord.setSept(scfFlowRecord.getSept());
        scfFlowAuditRecord.setFlowCode(scfFlowRecord.getFlowCode());
        scfFlowAuditRecord.setFlowName(scfFlowRecord.getFlowName());
        scfFlowAuditRecord.setNodeCode(scfFlowRecord.getNodeCode());
        scfFlowAuditRecord.setNodeName(scfFlowRecord.getNodeName());
        scfFlowAuditRecord.setAuditSept(auditSept);
        scfFlowAuditRecord.setStatus(Result.StatusEnum.Suspend.getCode());
        scfFlowAuditRecord.setAuditData(data);
        scfFlowAuditRecord.setCreateUser(identityInfo == null ? null : identityInfo.getUserId());
        scfFlowAuditRecord.setCreateName(identityInfo == null ? null : identityInfo.getRealName());
        ScfFlowAuditRecordManager scfFlowAuditRecordManager = SpringContextHolder.getBean(ScfFlowAuditRecordManager.class);
        scfFlowAuditRecordManager.save(scfFlowAuditRecord);

        //保存日志
        saveAuditLog(scfFlowAuditRecord,identityInfo);

        // 保存审批人关系
        saveAuditEvent(auditSept, scfFlowAuditRecord);
    }


    /**
     * 审批完成交给抄送人
     */
   void sendToCopyUser(ScfFlowAuditRecord scfFlowAuditRecord){
        ScfFlowNodeManager flowNodeManager = SpringContextHolder.getBean(ScfFlowNodeManager.class);
       ScfFlowNodeQuery flowNodeQuery = new ScfFlowNodeQuery();
       flowNodeQuery.setNodeCode(scfFlowAuditRecord.getNodeCode());
       flowNodeQuery.setStatus(StatusEnum.生效.code);
       ScfFlowNode auditFlowNode = flowNodeManager.findByQueryContion(flowNodeQuery);
       if(StringUtils.isNotBlank(auditFlowNode.getCopyTo())){
           Auditor auditor = JsonUtils.toObject(auditFlowNode.getCopyTo(),Auditor.class);
           saveAuditEvent(scfFlowAuditRecord,auditor,true);
       }
    }

    /**
     * 保存审批人关系
     */
    private void saveAuditEvent(Integer auditSept, ScfFlowAuditRecord scfFlowAuditRecord) {
        Auditor auditor = auditors.get(auditSept - 1);
        saveAuditEvent(scfFlowAuditRecord, auditor,false);
    }

    private void saveAuditEvent(ScfFlowAuditRecord scfFlowAuditRecord, Auditor auditor, Boolean copy) {
        List<FlowRefAuditorEvent> list = new ArrayList<>();
        List<Long> ids = auditor.getIds();
        if (!EmptyUtil.isEmpty(ids)) {
            ids.stream().forEach(id -> {
                FlowRefAuditorEvent auditorEvent = new FlowRefAuditorEvent();
                auditorEvent.setRefFlowAuditRecordId(scfFlowAuditRecord.getId());
                auditorEvent.setAuditType(FlowAuditType.ID.getType());
                auditorEvent.setAuditObject(id);
                auditorEvent.setCopy(copy);
                list.add(auditorEvent);
            });
        }

        List<Long> roles = auditor.getRoles();
        if (!EmptyUtil.isEmpty(roles)) {
            roles.stream().forEach(role -> {
                FlowRefAuditorEvent auditorEvent = new FlowRefAuditorEvent();
                auditorEvent.setRefFlowAuditRecordId(scfFlowAuditRecord.getId());
                auditorEvent.setAuditType(FlowAuditType.ROLE.getType());
                auditorEvent.setAuditObject(role);
                auditorEvent.setCopy(copy);
                list.add(auditorEvent);
            });
        }
        FlowRefAuditorEventManager flowRefAuditorEventManager = SpringContextHolder.getBean(FlowRefAuditorEventManager.class);
        flowRefAuditorEventManager.batchInsert(list);
    }


    /**
     * 保存审批日志
     *
     * @param auditRecord
     */
    private void saveAuditLog(ScfFlowAuditRecord auditRecord,IdentityInfoDTO identityInfoDTO) {
        FlowAuditRecordLog flowAuditRecordLog = new FlowAuditRecordLog();
        flowAuditRecordLog.setRefFlowAuditRecordId(auditRecord.getId());
        flowAuditRecordLog.setRefFileIds(auditRecord.getRefFileIds());
        flowAuditRecordLog.setInstantsNo(auditRecord.getInstantsNo());
        flowAuditRecordLog.setSept(auditRecord.getSept());
        flowAuditRecordLog.setFlowCode(auditRecord.getFlowCode());
        flowAuditRecordLog.setFlowName(auditRecord.getFlowName());
        flowAuditRecordLog.setNodeCode(auditRecord.getNodeCode());
        flowAuditRecordLog.setNodeName(auditRecord.getNodeName());
        flowAuditRecordLog.setAuditSept(auditRecord.getAuditSept());
        flowAuditRecordLog.setAuditData(auditRecord.getAuditData());
        flowAuditRecordLog.setStatus(auditRecord.getStatus());
        flowAuditRecordLog.setCreateUser(identityInfoDTO == null ? null :identityInfoDTO.getUserId());
        flowAuditRecordLog.setCreateName(identityInfoDTO == null ? null :identityInfoDTO.getRealName());
        flowAuditRecordLog.setAuditMessage(auditRecord.getAuditMessage());
        FlowAuditRecordLogManager flowAuditRecordLogManager = SpringContextHolder.getBean(FlowAuditRecordLogManager.class);
        flowAuditRecordLogManager.save(flowAuditRecordLog);
    }

}
