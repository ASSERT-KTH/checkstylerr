package com.hlj.proj.service.flowJob;

import com.hlj.proj.api.flowjob.JobService;
import com.hlj.proj.dto.flowJob.DemoJobDTO;
import com.hlj.proj.dto.user.IdentityInfoDTO;
import com.hlj.proj.service.flow.base.entity.Process;
import com.hlj.proj.service.flow.base.entity.ProcessDefinition;
import com.hlj.proj.service.flow.service.enums.FlowDefinitionEnum;
import com.hlj.proj.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author HealerJean
 * @ClassName JobServiceImpl
 * @date 2019/8/10  14:27.
 * @Description
 */
@Service
@Slf4j
public class JobServiceImpl implements JobService {

    /**
     * 启动任务
     */
    @Override
    public void startJob(DemoJobDTO demoJobDTO, IdentityInfoDTO identityInfoDTO) {
        Process process = ProcessDefinition.initProcess(FlowDefinitionEnum.demoJob.getFlowCode());
        process.startFlow(JsonUtils.toJsonString(demoJobDTO), identityInfoDTO);
    }

    /**
     * 继续任务
     */
    @Override
    public void continueJob(DemoJobDTO demoJobDTO, IdentityInfoDTO identityInfoDTO) {
        Process process = ProcessDefinition.ofSuspendProcess(demoJobDTO.getInstanceNo());
        process.nextFlow(demoJobDTO.getInstanceNo(), JsonUtils.toJsonString(demoJobDTO), identityInfoDTO);
    }



    //------------------------------------------------------------------------------------------------

    /**
     * 审核任务的测试启动
     */
    @Override
    public void auditFlowStartJob(DemoJobDTO demoJobDTO, IdentityInfoDTO identityInfoDTO) {
        Process process = ProcessDefinition.initProcess(FlowDefinitionEnum.auditJob.getFlowCode());
        process.startFlow(JsonUtils.toJsonString(demoJobDTO), identityInfoDTO);
    }

    /**
     * 审核任务的继续
     */
    @Override
    public void continueAuditFlowJob(DemoJobDTO demoJobDTO, IdentityInfoDTO identityInfoDTO) {
        Process process = ProcessDefinition.ofSuspendProcess(demoJobDTO.getInstanceNo());
        process.nextFlow(demoJobDTO.getInstanceNo(), JsonUtils.toJsonString(demoJobDTO), identityInfoDTO);
    }


}
