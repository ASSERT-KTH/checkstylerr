package com.hlj.quartz.core.schedule;

import com.hlj.quartz.core.event.HealerJeanJobListener;
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
public class HealerJeanScheduler {

    private static Logger logger = LoggerFactory.getLogger(HealerJeanScheduler.class);

    private final static String TRIGGER_PERFIX = "t_";

    private Scheduler scheduler = null;
    private static HealerJeanScheduler instance = null;


    private HealerJeanScheduler(){
    }

    private HealerJeanScheduler(Scheduler scheduler){
        this.scheduler = scheduler;
    }

    public static HealerJeanScheduler getInstance(){
        if (instance == null){
            logger.error("AdmoreScheduler not initialized");
            throw new RuntimeException("AdmoreScheduler not initialized");
        }
        return instance;
    }

    /**
     * 初始化一个定时器调度器
     */
// public static void initialise(StdSchedulerFactory stdSchedulerFactory){
   public static void initialise(){
        if (instance == null){
            synchronized (HealerJeanScheduler.class){
                if (instance == null){
                    try {
                        //下面这个会自动读取配置文件中的数据 名字为 HealerJeanquartzScheduler
//                        Scheduler scheduler = stdSchedulerFactory.getScheduler();
                        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler() ;
                        //添加任务监听
                        scheduler.getListenerManager().addJobListener(new HealerJeanJobListener());
                        instance = new HealerJeanScheduler(scheduler);

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


}
