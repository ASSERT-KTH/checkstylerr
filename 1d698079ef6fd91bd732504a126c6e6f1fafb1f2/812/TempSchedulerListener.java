package com.hlj.quartz.ddkj.monitor.quartz.listener;


import com.hlj.quartz.ddkj.monitor.AdmoreScheduler;
import com.hlj.quartz.ddkj.monitor.quartz.Job.QuartzCheckInterJob;
import com.hlj.quartz.ddkj.monitor.quartz.Job.QuartzCheckJob;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * 类名称：TempSchedulerListener
 * 类描述：集中用来启动进行监控job执行
 * 创建人：liqingxu
 * 修改人：
 * 修改时间：2017/7/21 上午10:26
 * 修改备注：
 *
 * @version 1.0.0
 */
public class TempSchedulerListener implements ServletContextListener {

    private Logger logger = LoggerFactory.getLogger(TempSchedulerListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            //每2分钟执行job监控
            AdmoreScheduler.getInstance().startJob(QuartzCheckJob.JOB_KEY,"0/3 * * * * ?", QuartzCheckJob.class);
        } catch (SchedulerException e) {
            e.printStackTrace();
            logger.error("启动job报错:"+e.getMessage(),e);
        }

        try {
            //每2分钟执行job监控
            AdmoreScheduler.getInstance().startJob(QuartzCheckInterJob.JOB_KEY,"0/3 * * * * ?", QuartzCheckInterJob.class);
        } catch (SchedulerException e) {
            e.printStackTrace();
            logger.error("启动job报错:"+e.getMessage(),e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}
