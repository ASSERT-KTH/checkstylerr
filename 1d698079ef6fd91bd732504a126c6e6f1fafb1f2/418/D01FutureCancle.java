package com.hlj.threadpool.d01Submit.D02SubmitFuture;

import java.util.concurrent.*;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/13  下午2:00.
 * 类描述：
 */
public class D01FutureCancle {

    /** 信号量 */
    private Semaphore semaphore = new Semaphore(0); // 1

    /** 线程池 */
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 5, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3));

    /** Future */
    private Future<String> future ;

    public void test(){

        future = pool.submit(new Callable<String>() {

            @Override
            public String call() {
                String result = null;
                try {
                    // 同步阻塞获取信号量
                    semaphore.acquire();
                    result = "ok";
                } catch (InterruptedException e) {
                    result = "interrupted";
                }
                return result;
            }
        });

        String result = "timeout";
        try {
            // 等待3s
            result = future.get(3, TimeUnit.SECONDS);
        }catch (Exception e) {
            System.out.println("超时异常");
        }

        // 删除线程池中任务
        boolean cancelResult = future.cancel(true);

        System.out.println("result is " + result);
        System.out.println("删除结果："  +cancelResult);
        System.out.println("当前active线程数：" +pool.getActiveCount());

    }

    public static void main(String[] args) {
        D01FutureCancle o = new D01FutureCancle();
        o.test();
    }

}
