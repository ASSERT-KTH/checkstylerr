package com.hlj.quartz.quartz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;


/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/22  下午3:45.
 */
@Configuration
public class QuartzConfig {

    @Bean
    public SpringBeanJobFactory jobFactory (){
        return new SpringBeanJobFactory();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(SpringBeanJobFactory jobFactory) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();

        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setConfigLocation(new ClassPathResource("quartz.properties"));
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactoryBean.setOverwriteExistingJobs(true);

        return schedulerFactoryBean;
    }


}