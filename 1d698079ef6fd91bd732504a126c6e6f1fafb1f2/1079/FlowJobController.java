package com.hlj.proj.controller.flow;

import com.hlj.proj.api.flowjob.JobService;
import com.hlj.proj.dto.ResponseBean;
import com.hlj.proj.dto.flowJob.DemoJobDTO;
import com.hlj.proj.dto.user.IdentityInfoDTO;
import com.hlj.proj.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HealerJean
 * @ClassName FlowJobController
 * @date 2019/8/10  13:55.
 * @Description 添加流程测试任务
 */
@RestController
@Slf4j
@RequestMapping("api/flow")
public class FlowJobController {

    @Autowired
    private JobService jobService ;


    @GetMapping("startJob")
    public ResponseBean test(){
        DemoJobDTO demoJobDTO = new DemoJobDTO();
        demoJobDTO.setId(1L);
        demoJobDTO.setName("2");
        IdentityInfoDTO identityInfoDTO = UserUtils.getAuthUser();
        jobService.startJob(demoJobDTO,identityInfoDTO);
        return ResponseBean.buildSuccess();
    }


    @GetMapping("continueJob")
    public ResponseBean continueJob(DemoJobDTO demoJobDTO){
        IdentityInfoDTO identityInfoDTO = UserUtils.getAuthUser();
        jobService.continueJob(demoJobDTO,identityInfoDTO);
        return ResponseBean.buildSuccess();
    }













    @GetMapping("auditFlowStartJob")
    public ResponseBean auditFlowStartJob(){
        DemoJobDTO demoJobDTO = new DemoJobDTO();
        demoJobDTO.setId(1L);
        demoJobDTO.setName("2");
        IdentityInfoDTO identityInfoDTO = UserUtils.getAuthUser();
        jobService.auditFlowStartJob(demoJobDTO,identityInfoDTO);
        return ResponseBean.buildSuccess();
    }

    @GetMapping("continueAuditFlowJob")
    public ResponseBean auditFlowStartJob(DemoJobDTO demoJobDTO){
        IdentityInfoDTO identityInfoDTO = UserUtils.getAuthUser();
        jobService.continueAuditFlowJob(demoJobDTO,identityInfoDTO);
        return ResponseBean.buildSuccess();
    }

}
