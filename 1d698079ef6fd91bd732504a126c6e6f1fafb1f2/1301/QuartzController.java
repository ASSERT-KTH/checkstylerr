package com.hlj.web.quartz;

import com.hlj.data.general.ResponseBean;
import com.hlj.quartz.core.schedule.HealerJeanScheduler;
import com.hlj.web.quartz.vo.JobDetailBean;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 类描述：
 * 创建人： HealerJean
 */
@Controller
@RequestMapping("quartz")
public class QuartzController {

    private Logger logger = LoggerFactory.getLogger(QuartzController.class);

    @Resource
    private HealerJeanScheduler scheduler;

    @RequestMapping("jobList")
    @ResponseBody
    public List<JobDetailBean> login(){

        Set<JobKey> jobKeySet = scheduler.currentJobs();
        List<JobDetailBean> jobDetailList = new ArrayList<>();

        for (JobKey jobKey : jobKeySet){
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            Trigger trigger = scheduler.getJobTrigger(jobKey);

            JobDetailBean detailBean = new JobDetailBean();
            detailBean.setJobId(jobKey.getName());
            detailBean.setJobDesc(jobDetail.getDescription());
            detailBean.setCron(((CronTrigger)trigger).getCronExpression());
            detailBean.setJobClass(jobDetail.getJobClass().toString());
            detailBean.setPreviousFireTime(trigger.getPreviousFireTime());
            detailBean.setNextFireTime(trigger.getNextFireTime());

            Trigger.TriggerState triggerState = scheduler.getTriggerState(jobKey);
            detailBean.setJobStatus(triggerState.name());

            jobDetailList.add(detailBean);
        }
        return jobDetailList;
    }


    @RequestMapping("create")
    @ResponseBody
    public ResponseBean create(String jobId, String jobCron, String jobClass, String jobDesc){
        try {
            scheduler.startJob(jobId,jobCron,Class.forName(jobClass).asSubclass(Job.class),jobDesc);
            return ResponseBean.buildSuccess();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseBean.buildFailure(e.getMessage());
        }
    }

    @RequestMapping("update")
    @ResponseBody
    public ResponseBean update(String jobId,String jobCron,String jobClass,String jobDesc){
        try {
            scheduler.resetJob(jobId,jobCron);
            return ResponseBean.buildSuccess();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseBean.buildFailure(e.getMessage());
        }
    }

    @RequestMapping("delete")
    @ResponseBody
    public ResponseBean delete(String jobId){
        try {
            scheduler.deleteJob(jobId);
            return ResponseBean.buildSuccess();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseBean.buildFailure(e.getMessage());
        }
    }

    @RequestMapping("pause")
    @ResponseBody
    public ResponseBean pause(String jobId){
        try {
            scheduler.pauseJob(jobId);
            return ResponseBean.buildSuccess();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseBean.buildFailure(e.getMessage());
        }
    }

    @RequestMapping("resume")
    @ResponseBody
    public ResponseBean start(String jobId){
        try {
            scheduler.resumeJob(jobId);
            return ResponseBean.buildSuccess();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseBean.buildFailure(e.getMessage());
        }
    }

}
