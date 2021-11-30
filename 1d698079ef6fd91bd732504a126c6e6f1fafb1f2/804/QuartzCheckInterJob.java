package com.hlj.quartz.ddkj.monitor.quartz.Job;


import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * 类描述：多点宝job检查
 * 创建人：liqingxu
 * 创建时间：2017/7/21
 * 修改人：
 * 修改时间：
 * 修改备注：
   将该注解加到job类上，告诉Quartz不要并发地执行同一个job定义（这里指特定的job类）的多个实例。
 * 请注意这里的用词。拿前一小节的例子来说，如果“SalesReportJob”类上有该注解，则同一时刻仅允许执行一个“SalesReportForJoe”实例，
 * 但可以并发地执行“SalesReportForMike”类的一个实例。所以该限制是针对JobDetail的，而不是job类的。
 * 但是我们认为（在设计Quartz的时候）应该将该注解放在job类上，因为job类的改变经常会导致其行为发生变化。
 * @version 1.0.0
 */
@DisallowConcurrentExecution // 保证多个任务间不会同时执行.所以在多任务执行时最好加上

public class QuartzCheckInterJob implements Job {
    public static final String JOB_KEY = "quartz.inter.job.check";

    private Logger logger = LoggerFactory.getLogger(QuartzCheckInterJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
//            QuartzCheckService quartzCheckService = SpringHelper.getBean(QuartzCheckService.class);
//            Calendar calendar = Calendar.getInstance();
//            //查询之前5分钟的job本应该开始的job
//            calendar.add(Calendar.MINUTE,-5);
//            quartzCheckService.checkQuartzInterJob(calendar);
            logger.info("inter job check process time:"+ Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("[quartz.inter.job.check]" + e.getMessage(),e);
        }
    }
}
