package com.hlj.quartz.quartz.Job;
import java.util.Date;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * @Description 任务类.
 * @Author HealerJean
 * @Date 2018/3/22  下午4:17.
 */

/**
 * @DisallowConcurrentExecution 禁止并发执行多个相同定义的JobDetail,
 * 这个注解是加在Job类上的, 但意思并不是不能同时执行多个Job, 而是不能并发执行同一个Job Definition(由JobDetail定义),
 * 但是可以同时执行多个不同的JobDetail,
 * 举例说明,我们有一个Job类,叫做SayHelloJob, 并在这个Job上加了这个注解, 然后在这个Job上定义了很多个JobDetail,
 * 如sayHelloToJoeJobDetail, sayHelloToMikeJobDetail,
 * 那么当scheduler启动时, 不会并发执行多个sayHelloToJoeJobDetail或者sayHelloToMikeJobDetail,
 */
@DisallowConcurrentExecution //一般情况下建议加上
public class HelloJobOne implements Job{

        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("一号任务"+new Date());

        }

}