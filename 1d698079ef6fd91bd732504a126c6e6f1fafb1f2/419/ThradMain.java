package com.hlj.threadpool;

import com.hlj.threadpool.thread.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/2  下午5:01.
 */
@Slf4j
public class ThradMain {

    public static void main(String[] args) {
        ThreadPoolUtils threadPoolUtils = new ThreadPoolUtils();

        for(int i = 1 ; i<100;i++) {
            threadPoolUtils.execute(() -> { //这里的参数必须是一个接口
                System.out.println("线程"+Thread.currentThread().getId());
                System.out.println(
                        "线程池完成的总线程"+threadPoolUtils.getThreadPoolExecutor().getTaskCount()+
                        "线程池中线程数目："+threadPoolUtils.getThreadPoolExecutor().getPoolSize()+
                                "，队列中核心线程的数目："+ threadPoolUtils.getThreadPoolExecutor().getCorePoolSize()+
                                "，队列中等待执行的任务数目："+ threadPoolUtils.getThreadPoolExecutor().getQueue().size()+
                                "，已执行玩别的任务数目："+threadPoolUtils.getThreadPoolExecutor().getCompletedTaskCount());

            });

        }

        System.out.println("线程池完成的总线程" + threadPoolUtils.getThreadPoolExecutor().getTaskCount());
        System.out.println("已执行玩别的任务数目:" + threadPoolUtils.getThreadPoolExecutor().getCompletedTaskCount());
        //当线程池完成的线程数等于线程池中的总线程数
        boolean allThreadsIsDone = threadPoolUtils.getThreadPoolExecutor().getTaskCount() == threadPoolUtils.getThreadPoolExecutor().getCompletedTaskCount();
        System.out.println(allThreadsIsDone);
        if (allThreadsIsDone) {
            System.out.println("--------");
            System.out.println("全部执行完成");
        }
        while (!allThreadsIsDone) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            allThreadsIsDone = threadPoolUtils.getThreadPoolExecutor().getTaskCount() == threadPoolUtils.getThreadPoolExecutor().getCompletedTaskCount();
            if (allThreadsIsDone) {
                System.out.println("全部执行完成");
            }

        }


    }
}
