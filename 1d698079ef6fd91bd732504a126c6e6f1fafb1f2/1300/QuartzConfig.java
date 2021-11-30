package com.hlj;

import com.alibaba.druid.pool.DruidDataSource;
import com.hlj.quartz.core.listener.HealerJeanJobListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;

/**
 * 类描述：
 * 创建人： HealerJean
 */
@Configuration
public class QuartzConfig {

    @Value("${healerjean.datasource.url}")
    private String healerjeanUrl;
    @Value("${healerjean.datasource.username}")
    private String healerjeanname;
    @Value("${healerjean.datasource.password}")
    private String healerjeanPassword;

    @Bean
    public SpringBeanJobFactory jobFactory (){
        return new SpringBeanJobFactory();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean (SpringBeanJobFactory jobFactory) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setDataSource(createAdmore());
        schedulerFactoryBean.setConfigLocation(new ClassPathResource("quartz.properties"));
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactoryBean.setGlobalJobListeners(new HealerJeanJobListener());
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        return schedulerFactoryBean;
    }

    /**
     * 单独为定时任务创建一个单独的datasource
     */
    private DataSource createAdmore(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(healerjeanUrl);
        druidDataSource.setUsername(healerjeanname);
        druidDataSource.setPassword(healerjeanPassword);
        druidDataSource.setMaxActive(10);
        druidDataSource.setInitialSize(5);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setMaxWait(2500);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);

        return druidDataSource;
    }

}
