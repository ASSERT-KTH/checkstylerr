package com.hlj.quartz.normalschedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 没有config 那就是串行，也就是一个线程，如果有了config中的配置，那么就是并行了
 * SpringBoot定时任务默认单线程，多线程需要自行实现或配置文件(sinosoft中一样)
 * @Author HealerJean
 * @Date 2018/3/22  下午2:52.
 */
@Component
public class MyTaskAnnotation {
    private static final Logger logger = LoggerFactory.getLogger(MyTaskAnnotation.class);

    /**
     * 定时计算。每天凌晨 01:00 执行一次
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void show(){
        System.out.println("Annotation：is show run");
    }



    /**
     * 心跳更新。启动时执行一次，之后每隔1秒执行一次
     */
    @Scheduled(fixedRate = 1000*1)
    public void printOne(){
        Thread current = Thread.currentThread();
        System.out.println("定时任务1:"+current.getId());
        logger.info("心跳更新。启动时执行一次，之后每隔3秒执行一次 :"+current.getId()+ ",name:"+current.getName());
    }

    /**
     * 心跳更新。启动时执行一次，之后每隔3秒执行一次
     */
    @Scheduled(fixedRate = 1000*3)
    public void printTow(){
        Thread current = Thread.currentThread();
        System.out.println("定时任务2:"+current.getId());
        logger.info("心跳更新。启动时执行一次，之后每隔1秒执行一次 :"+current.getId()+ ",name:"+current.getName());
    }


}