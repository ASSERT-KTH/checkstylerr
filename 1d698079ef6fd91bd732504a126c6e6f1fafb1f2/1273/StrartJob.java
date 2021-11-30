package com.hlj;

import com.hlj.quartz.core.schedule.HealerJeanScheduler;
import com.hlj.quartz.job.QuartzCheckJob;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * 类名称：StrartJob
 * 类描述：集中用来启动进行监控job执行
 * 修改备注：
 */
@Service
public class StrartJob implements CommandLineRunner {

    private Logger logger = LoggerFactory.getLogger(StrartJob.class);

    @Override
    public void run(String... args) throws Exception {
        try {
            //每5分钟执行job监控
            HealerJeanScheduler.getInstance().startJob(QuartzCheckJob.JOB_KEY,"0 0/5 * * * ?", QuartzCheckJob.class);
        } catch (SchedulerException e) {
            e.printStackTrace();
            logger.error("启动job报错:"+e.getMessage(),e);
        }
    }

}
