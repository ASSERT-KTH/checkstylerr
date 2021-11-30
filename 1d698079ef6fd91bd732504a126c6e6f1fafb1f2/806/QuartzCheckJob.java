package com.hlj.quartz.ddkj.monitor.quartz.Job;


import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * 类描述：
 * 创建人：liqingxu
 * 创建时间：2017/7/21
 * 修改人：
 * 修改时间：
 * 修改备注：
 *
 * @version 1.0.0
 */
@DisallowConcurrentExecution
public class QuartzCheckJob implements Job {
    public static final String JOB_KEY = "quartz.job.check";

    private Logger logger = LoggerFactory.getLogger(QuartzCheckJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
//            QuartzCheckService quartzCheckService = SpringHelper.getBean(QuartzCheckService.class);
//            Calendar calendar = Calendar.getInstance();
//            //查询之前5分钟的job本应该开始的job
//            calendar.add(Calendar.MINUTE,-5);
//            quartzCheckService.checkQuartzJob(calendar);
            logger.info("job check process time:"+ Calendar.getInstance().getTime());
        } catch (Exception e) {
            logger.error("[quartz.job.check]" + e.getMessage(),e);
        }
    }
}
