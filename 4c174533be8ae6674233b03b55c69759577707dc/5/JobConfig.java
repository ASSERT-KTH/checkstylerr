package me.jcala.quartz.schedule.config;

import me.jcala.quartz.schedule.job.SampleJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfig {

    @Bean
    public JobDetail sampleJobDetail() {
        return JobBuilder.newJob(SampleJob.class).withIdentity("sampleJob")
                .usingJobData("name", "World").storeDurably().build();
    }

    @Bean
    public Trigger sampleJobTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(2).repeatForever();

        return TriggerBuilder.newTrigger().forJob(sampleJobDetail())
                .withIdentity("sampleTrigger").withSchedule(scheduleBuilder).build();
    }

}
