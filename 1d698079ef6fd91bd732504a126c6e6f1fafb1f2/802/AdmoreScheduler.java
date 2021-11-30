package com.hlj.quartz.ddkj.monitor;

import com.hlj.quartz.ddkj.monitor.quartz.listener.AdmoreJobListener;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by j.sh on 2015/4/9.
 * quartz job调度核心类
 */
@Component
public class AdmoreScheduler {

    private static Logger logger = LoggerFactory.getLogger(AdmoreScheduler.class);

    private final static String TRIGGER_PERFIX = "t_";

    private Scheduler scheduler = null;
    private static AdmoreScheduler instance = null;


    private AdmoreScheduler(){
    }

    private AdmoreScheduler(Scheduler scheduler){
        this.scheduler = scheduler;
    }

    public static AdmoreScheduler getInstance(){
        if (instance == null){
            logger.error("AdmoreScheduler not initialized");
            throw new RuntimeException("AdmoreScheduler not initialized");
        }
        return instance;
    }

    public static void initialise(StdSchedulerFactory schedulerFactory){
        if (instance == null){
            synchronized (AdmoreScheduler.class){
                if (instance == null){
                    try {
                        Scheduler scheduler = schedulerFactory.getScheduler();
                        //添加监听
                        scheduler.getListenerManager().addJobListener(new AdmoreJobListener());
                        instance = new AdmoreScheduler(scheduler);
                        logger.info("Scheduler init complete");
                    } catch (SchedulerException e) {
                        logger.error("Scheduler init failed" , e );
                        throw new RuntimeException("Scheduler init failed"  + e.getCause(),e);
                    }
                }
            }
        }
    }

    public <T extends Job> void startJob(String jobId, String cron, Class<T> t) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(t).withIdentity(jobId).build();
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_PERFIX + jobId).withSchedule(CronScheduleBuilder.cronSchedule(cron)).build();

        scheduler.scheduleJob(job, trigger);
        logger.info(jobId + " start at " + new Date());
    }

    public void addJobListener(String jobKey,JobListener listener) throws SchedulerException {
        Matcher<JobKey> matcher = KeyMatcher.keyEquals(new JobKey(jobKey));
        scheduler.getListenerManager().addJobListener(listener,matcher);
    }

    public void deleteJob(String jobId) throws SchedulerException {
        scheduler.deleteJob(new JobKey(jobId));
        logger.info(jobId + " stop at " + new Date());
    }

}
