package com.hlj.quartz.ddkj.monitor.quartz.listener;


import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/6/4
 * version：1.0.0
 */
public class AdmoreJobListener implements JobListener {

    private Logger logger = LoggerFactory.getLogger(JobListener.class);

    private static final String LISTENER_NAME = "admore.job.listener";

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    /**
     * 任务执行之前执行
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        logger.info("任务即将开始"+jobExecutionContext.getJobDetail().getKey() + "  to be execute");
    }

    /**
     *
     * 任务取消
     * 这个方法正常情况下不执行,但是如果当TriggerListener中的vetoJobExecution方法返回true时,那么执行这个方法.
     * 需要注意的是 如果方法(2)执行 那么(1),(3)这个俩个方法不会执行,因为任务被终止了嘛.
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        logger.info(jobExecutionContext.getJobDetail().getKey() + " vetoed");
    }


    /**
     任务执行完成后执行,jobException如果它不为空则说明任务在执行过程中出现了异常
     */

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        if (e != null){
            String jobName = jobExecutionContext.getJobDetail().getKey().getName();
            String errMsg = e.getMessage();

            logger.error(jobExecutionContext.getJobDetail().getKey() + "  execute failure");
            logger.error(e.getMessage(),e);

        } else {
            logger.info("任务执行完成"+jobExecutionContext.getJobDetail().getKey() + "  execute success");
        }
    }
}
