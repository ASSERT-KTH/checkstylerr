package com.hlj.quartz.ddkj.monitor.quartz.listener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/9/13
 * version：1.0.0
 */
@Configuration
public class ListenerConfig {

    @Bean
    public QuartzListener quartzListener(){
        return new QuartzListener();
    }

    @Bean
    public TempSchedulerListener tempSchedulerListener(){
        return new TempSchedulerListener();
    }

}
