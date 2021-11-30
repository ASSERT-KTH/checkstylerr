package com.hlj.quartz.quartz.service;

import com.hlj.quartz.quartz.Job.HelloJobOne;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  下午4:38.
 */
/**
 SimpleScheduleBuilder是简单调用触发器，它只能指定触发的间隔时间和执行次数；
 CronScheduleBuilder是类似于Linux Cron的触发器，它通过一个称为CronExpression的规则来指定触发规则，通常是每次触发的具体时间；（关于CronExpression，详见：官方，中文网文）
 CalendarIntervalScheduleBuilder是对CronScheduleBuilder的补充，它能指定每隔一段时间触发一次。
 */

@Service
public class QuartzService {

    @Resource
    private Scheduler scheduler;

    public void startJob(String time,String jobName,String group,Class job){
        try {
            // 创建jobDetail实例，绑定Job实现类
            // 指明job的名称，所在组的名称，以及绑定job类
            JobDetail jobDetail = JobBuilder.newJob(job).withIdentity(jobName, group).build();//设置Job的名字和组
            //corn表达式  每2秒执行一次
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(time/*"0/2 * * * * ?"*/);
            //设置定时任务的时间触发规则
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(jobName,group) .withSchedule(scheduleBuilder).build();
            System.out.println(scheduler.getSchedulerName());
            // 把作业和触发器注册到任务调度中, 启动调度
            scheduler.scheduleJob(jobDetail,cronTrigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startJob2(String time,String jobName,String group,Class job){
        try {
            JobDetail jobDetail = JobBuilder.newJob(job).withIdentity(jobName, group).build();//设置Job的名字和组
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(time/*"0/2 * * * * ?"*/);
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(jobName,group) .withSchedule(scheduleBuilder).build();
            System.out.println(scheduler.getSchedulerName());
            scheduler.scheduleJob(jobDetail,cronTrigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****
     * 暂停一个任务
     * @param triggerName
     * @param triggerGroupName
     */
    public void pauseJob(String triggerName,String triggerGroupName){
        try {
            JobKey jobKey = new JobKey(triggerName, triggerGroupName);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail==null){
                return;
            }
            System.out.println("开始暂停一个定时器");
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /****
     * 暂停重启一个定时器任务
     * @param triggerName
     * @param triggerGroupName
     */
    public void resumeJob(String triggerName,String triggerGroupName){
        try {
            JobKey jobKey = new JobKey(triggerName, triggerGroupName);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail==null){
                return;
            }
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }



    /****
     * 删除一个定时器任务，删除了，重启就没什么用了
     * @param triggerName
     * @param triggerGroupName
     */
    public void deleteJob(String triggerName,String triggerGroupName){
        try {
            JobKey jobKey = new JobKey(triggerName, triggerGroupName);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            if (jobDetail==null){
                return;
            }
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }




    /***
     * 开启定时器，这时才可以开始所有的任务，默认是开启的
     */
    public void startAllJob(){
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭定时器，则所有任务不能执行和创建
     */
    public void shutdown(){
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


    public void haveProperties() throws SchedulerException, InterruptedException{
        /*
         *在 Quartz 中， scheduler 由 scheduler 工厂创建：
         * DirectSchedulerFactory 或者 StdSchedulerFactory。
         *第二种工厂 StdSchedulerFactory 使用较多，
         *因为 DirectSchedulerFactory 使用起来不够方便，需要作许多详细的手工编码设置。
         */
        // 获取Scheduler实例
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        System.out.println("scheduler.start");
        //具体任务.
        JobDetail jobDetail = JobBuilder.newJob(HelloJobOne.class).withIdentity("job1","group1").build();
        //触发时间点. (每5秒执行1次.)
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5).repeatForever();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1","group1").startNow().withSchedule(simpleScheduleBuilder).build();
        // 交由Scheduler安排触发
        scheduler.scheduleJob(jobDetail,trigger);
        //睡眠20秒.
        TimeUnit.SECONDS.sleep(20);
        scheduler.shutdown();//关闭定时任务调度器.
        System.out.println("scheduler.shutdown");

    }
}
