package com.hlj.quartz.core.event;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

/**
 * 类描述：
 * 创建人： HealerJean
 * job任务监听，在 HealerJeanScheduler 里面配置
 */
@Slf4j
public class HealerJeanJobListener implements JobListener {

    private static final String LISTENER_NAME = "healerjean.job.listener";

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    /**
     * 任务执行之前执行
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        log.info(jobExecutionContext.getJobDetail().getKey() + "  to be execute");
    }

    /**
     *
     * 任务取消
     * 这个方法正常情况下不执行,但是如果当 TriggerListener中的 vetoJobExecution 方法返回true时, 那么执行这个方法.
     * 需要注意的是 如果方法(2)执行 那么(1),(3)这个俩个方法不会执行,因为任务被终止了嘛.
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        log.info(jobExecutionContext.getJobDetail().getKey() + " vetoed");
    }

    /**
     任务执行完成后执行,jobException如果它不为空则说明任务在执行过程中出现了异常
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        if (e != null){
            String jobName = jobExecutionContext.getJobDetail().getKey().getName();
            String errMsg = e.getMessage();
            log.error(jobExecutionContext.getJobDetail().getKey() + "  execute failure");
            log.error(e.getMessage(),e);
        } else {
            log.info(jobExecutionContext.getJobDetail().getKey() + "  execute success");
        }
    }
}
