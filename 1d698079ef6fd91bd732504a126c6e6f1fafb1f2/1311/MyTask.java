package com.hlj.quartz.quartz.Job;

import com.hlj.quartz.quartz.Repository.CScheduleTriggerRepository;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/23  下午4:19.
 */
/**
 * @DisallowConcurrentExecution 禁止并发执行多个相同定义的JobDetail,
 * 这个注解是加在Job类上的, 但意思并不是不能同时执行多个Job, 而是不能并发执行同一个Job Definition(由JobDetail定义),
 * 但是可以同时执行多个不同的JobDetail,
 * 举例说明,我们有一个Job类,叫做SayHelloJob, 并在这个Job上加了这个注解, 然后在这个Job上定义了很多个JobDetail,
 * 如sayHelloToJoeJobDetail, sayHelloToMikeJobDetail,
 * 那么当scheduler启动时, 不会并发执行多个sayHelloToJoeJobDetail或者sayHelloToMikeJobDetail,
 */
@DisallowConcurrentExecution //一般情况下建议加上
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