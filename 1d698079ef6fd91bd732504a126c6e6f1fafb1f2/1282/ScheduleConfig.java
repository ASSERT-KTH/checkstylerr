package com.hlj.quartz.normalschedule.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

/**
 * @Description SpringBoot 默认是串行任务，也就是使用的是一个线程。这里配置使得SpringBoot可以使用多线程
 * （和Sinosoft中配置文件中添加的配置一样哦）
 * @Author HealerJean
 * @Date 2018/3/22  下午3:01.
 */
@Configuration
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer, AsyncConfigurer
{

    /*
     * 并行任务
     */
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar)
    {
        TaskScheduler taskScheduler = taskScheduler();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    /**
     * 并行任务使用策略：多线程处理（配置线程数等）
     *
     * @return ThreadPoolTaskScheduler 线程池
     */
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler()
    {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(20);
        scheduler.setThreadNamePrefix("task-");  //设置线程名开头
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }

    /*
     * 异步任务
     */
    public Executor getAsyncExecutor()
    {
        Executor executor = taskScheduler();
        return executor;
    }

    /*
     * 异步任务 异常处理
     */
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler()
    {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}

