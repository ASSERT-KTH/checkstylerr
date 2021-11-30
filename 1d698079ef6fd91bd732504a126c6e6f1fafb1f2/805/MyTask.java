package com.hlj.quartz.ddkj.monitor.quartz.Job;

import com.hlj.quartz.ddkj.monitor.quartz.Repository.CScheduleTriggerRepository;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/23  下午4:19.
 */

@Component
public class MyTask implements Job {

    //这里就可以通过spring注入bean了
    @Autowired
    private CScheduleTriggerRepository jobRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        boolean isExecute = false;  //是否已执行业务逻辑
        boolean flag = false;  //业务逻辑执行后返回结果
        try {
            //可以通过context拿到执行当前任务的quartz中的很多信息，如当前是哪个trigger在执行该任务
            CronTrigger trigger = (CronTrigger) context.getTrigger();
            String corn = trigger.getCronExpression();
            String jobName = trigger.getKey().getName();
            String jobGroup = trigger.getKey().getGroup();
            System.out.println("corn:"+corn);
            System.out.println("jobName:"+jobName);
            System.out.println("jobGroup:"+jobGroup);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("执行任务中");
    }
}