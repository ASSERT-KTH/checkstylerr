package com.hlj.proj.service.flow.service.impl;

import com.hlj.proj.data.dao.mybatis.manager.flow.FlowRefAuditorEventManager;
import com.hlj.proj.data.dao.mybatis.manager.flow.ScfFlowAuditRecordManager;
import com.hlj.proj.data.pojo.flow.*;
import com.hlj.proj.dto.PageDTO;
import com.hlj.proj.dto.system.RoleDTO;
import com.hlj.proj.dto.user.IdentityInfoDTO;
import com.hlj.proj.exception.BusinessException;
import com.hlj.proj.service.flow.base.entity.AuditorProcess;
import com.hlj.proj.service.flow.service.dto.AuditJobCollectDTO;
import com.hlj.proj.service.flow.service.dto.AuditRecordDTO;
import com.hlj.proj.service.flow.service.dto.AuditorResultDTO;
import com.hlj.proj.service.flow.base.entity.Result;
import com.hlj.proj.service.flow.service.ScfFlowService;
import com.hlj.proj.service.flow.service.enums.FlowAuditType;
import com.hlj.proj.service.flow.service.enums.FlowNodeTypeEnum;
import com.hlj.proj.utils.BeanUtils;
import com.hlj.proj.utils.EmptyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName ScfFlowServiceImpl
 * @Author TD
 * @Date 2019/6/19 16:11
 * @Description
 */
@Service
@Slf4j
public class ScfFlowServiceImpl implements ScfFlowService {

    @Autowired
    private ScfFlowAuditRecordManager scfFlowAuditRecordManager;
    @Autowired
    private FlowRefAuditorEventManager flowRefAuditorEventManager;

    /**
     * 查询当前所有待审批节点
     */
    @Override
    public List<AuditJobCollectDTO> jobCollect(IdentityInfoDTO authUser) {
        List<AuditJobCollectDTO> collect = new ArrayList<>();
        List<RoleDTO> roleDtos = authUser.getRoles();
        List<Long> roles = null;
        if (roleDtos != null && !roleDtos.isEmpty()) {
            roles = roleDtos.stream().map(RoleDTO::getId).collect(Collectors.toList());
        }
        Long userId = authUser.getUserId();
        ScfFlowAuditRecordQuery auditRecordQuery = new ScfFlowAuditRecordQuery();
        auditRecordQuery.setId(userId);
        auditRecordQuery.setRoleIds(roles);
        auditRecordQuery.setStatus(Result.AuditStatusEnum.Suspend.getCode());
        List<ScfFlowAuditRecord> scfFlowNodes = scfFlowAuditRecordManager.jobCollect(auditRecordQuery);
        if (!EmptyUtil.isEmpty(scfFlowNodes)) {
            collect = scfFlowNodes.stream().map(BeanUtils::toAuditJobCollectDTO).collect(Collectors.toList());
        }
        return collect;
    }


    /**
     * 查询任务列表
     */
    @Override
    public PageDTO<AuditRecordDTO> readyAudits(AuditJobCollectDTO auditRecordDTO, IdentityInfoDTO authUser) {
        List<AuditRecordDTO> collect = new ArrayList<>();
        List<RoleDTO> roleDtos = authUser.getRoles();
        List<Long> roles = null;
        if (roleDtos != null && !roleDtos.isEmpty()) {
            roles = roleDtos.stream().map(RoleDTO::getId).collect(Collectors.toList());
        }
        Long userId = authUser.getUserId();
        ScfFlowAuditRecordQuery auditRecordQuery = new ScfFlowAuditRecordQuery();
        auditRecordQuery.setId(userId);
        auditRecordQuery.setRoleIds(roles);
        auditRecordQuery.setNodeCode(auditRecordDTO.getNodeCode());
        auditRecordQuery.setStatus(Result.AuditStatusEnum.Suspend.getCode());
        ScfFlowAuditRecordPage page = scfFlowAuditRecordManager.readyAuditsPage(auditRecordQuery);
        List<ScfFlowAuditRecord> auditRecords = page.getValues();
        if (!EmptyUtil.isEmpty(auditRecords)) {
            collect = auditRecords.stream().map(BeanUtils::toAuditRecordDTO).collect(Collectors.toList());
        }
        return BeanUtils.toPageDTO(page, collect);
    }

    /**
     * 对指定审批节点进行审批
     * 1、判断该用户是否有审核权限
     * 2、启动审批流
     * 3、开始审批
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(AuditorResultDTO auditorResultDTO, IdentityInfoDTO identityInfo) {
        // 1、判断该用户是否有审核权限
        ScfFlowAuditRecord auditRecord = scfFlowAuditRecordManager.findById(auditorResultDTO.getAuditRecordId());
        if (auditRecord == null) {
            log.error("执行时找不到对应的审批记录，auditorResultDTO：{}", auditorResultDTO);
            throw new BusinessException("执行时找不到对应的审批记录");
        }
        if (StringUtils.equals(auditRecord.getStatus(), Result.StatusEnum.Success.getCode())) {
            log.error("已经审核了，无需重复审核");
            throw new BusinessException("已经审核了，无需重复审核");
        }
        if (!auditPermission(auditorResultDTO, identityInfo)) {
            throw new BusinessException("该用户没有审批该记录权限");
        }
        // 2、启动审批流
        AuditorProcess auditorProcess = AuditorProcess.ofSuspendAuditorProcess(auditorResultDTO.getAuditRecordId());
        // 3、开始审批
        auditorProcess.audit(auditorResultDTO, identityInfo);
    }


    /**
     * 1、判断角色是否可以审核
     * 2、判断用户是否可以审核
     */
    private Boolean auditPermission(AuditorResultDTO auditorResultDTO, IdentityInfoDTO identityInfo) {

        FlowRefAuditorEventQuery auditorEventQuery = new FlowRefAuditorEventQuery();
        auditorEventQuery.setRefFlowAuditRecordId(auditorResultDTO.getAuditRecordId());

        // 1、判断角色是否可以审核
        List<RoleDTO> roleDtos = identityInfo.getRoles();
        List<Long> roles = null;
        if (roleDtos != null && !roleDtos.isEmpty()) {
            roles = roleDtos.stream().map(RoleDTO::getId).collect(Collectors.toList());
        }
        auditorEventQuery.setAuditType(FlowAuditType.ROLE.getType());
        auditorEventQuery.setRoleIds(roles);
        auditorEventQuery.setCopy(false);
        List<FlowRefAuditorEvent> flowRefAuditorEvents = flowRefAuditorEventManager.queryList(auditorEventQuery);
        if (!EmptyUtil.isEmpty(flowRefAuditorEvents)) {
            return true;
        }

        // 2、判断用户是否可以审核
        Long userId = identityInfo.getUserId();
        auditorEventQuery.setRoleIds(null);
        auditorEventQuery.setAuditType(FlowAuditType.ID.getType());
        auditorEventQuery.setId(userId);
        auditorEventQuery.setCopy(false);
        flowRefAuditorEvents = flowRefAuditorEventManager.queryList(auditorEventQuery);
        if (!EmptyUtil.isEmpty(flowRefAuditorEvents)) {
            return true;
        }
        return false;
    }


}
