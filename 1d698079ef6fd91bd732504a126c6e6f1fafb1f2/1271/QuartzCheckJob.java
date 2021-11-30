package com.hlj.quartz.job;

import com.hlj.service.QuartzCheckService;
import com.hlj.utils.SpringHelper;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * @DisallowConcurrentExecution 禁止并发执行多个相同定义的JobDetail,
 * 这个注解是加在Job类上的, 但意思并不是不能同时执行多个Job, 而是不能并发执行同一个Job Definition(由JobDetail定义),
 * 但是可以同时执行多个不同的JobDetail,
 * 举例说明,我们有一个Job类,叫做SayHelloJob, 并在这个Job上加了这个注解, 然后在这个Job上定义了很多个JobDetail,
 * 如sayHelloToJoeJobDetail, sayHelloToMikeJobDetail,
 * 那么当scheduler启动时, 不会并发执行多个sayHelloToJoeJobDetail或者sayHelloToMikeJobDetail,
 */
@DisallowConcurrentExecution //一般情况下建议加上
public class QuartzCheckJob implements Job {
    public static final String JOB_KEY = "quartz.job.check";

    private Logger logger = LoggerFactory.getLogger(QuartzCheckJob.class);


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            QuartzCheckService quartzCheckService = SpringHelper.getBean(QuartzCheckService.class);

            Calendar calendar = Calendar.getInstance();
            //查询之前5分钟的job本应该开始的job
            calendar.add(Calendar.MINUTE,-5);
            quartzCheckService.checkQuartzJob(calendar);

            logger.info("job check process time:"+ Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("[quartz.job.check]" + e.getMessage(),e);
        }
    }



}
