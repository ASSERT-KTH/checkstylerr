package com.hlj.quartz.core.event;

import com.hlj.quartz.core.schedule.HealerJeanScheduler;
import org.quartz.SchedulerException;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

/**
 * web应用启动的时候执行，初始化quartz
 * QuartzInitializerListener implements ServletContextListener,所以可以用下面的，或者我们自己也可以设置其他方式，只要让它启动的时候执行
 */
@WebListener
public class QuartzListener extends QuartzInitializerListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
//     可以使用下面的传入，但是我认为还是没有必要了
//     schedulerFactory会自动装入ServletContext 可以使用schedulerFactory.get
//        ServletContext ctx = servletContextEvent.getServletContext();
//        StdSchedulerFactory schedulerFactory = (StdSchedulerFactory) ctx.getAttribute(QUARTZ_FACTORY_KEY);
//        HealerJeanScheduler.initialise(schedulerFactory);

        HealerJeanScheduler.initialise();
    }



}
