package com.hlj.quartz.job;

import org.quartz.*;
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

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        try {

            //可以通过context拿到执行当前任务的quartz中的很多信息，如当前是哪个trigger在执行该任务
            CronTrigger trigger = (CronTrigger) context.getTrigger();

            TriggerKey triggerKey =trigger.getKey() ;
            String corn = trigger.getCronExpression();
            System.out.println("任务规则 :"+corn);

            System.out.println("triggerKey  Name:"+triggerKey.getName()); //t_jobId
            System.out.println("triggerKey group:"+triggerKey.getGroup()); //t_jobId

            System.out.println("----------------------");
            JobKey jobKey =   context.getJobDetail().getKey() ;
            System.out.println("jobKey getName"+jobKey.getName()); //jobId
            System.out.println("jobKey getGroup"+jobKey.getGroup()); //DEFAULT

            Scheduler scheduler =  context.getScheduler();
            System.out.println("scheduler.getCalendarNames()"+scheduler.getCalendarNames());

            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            System.out.println(jobDetail.getJobClass().toString());

        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("执行任务中");
    }
}