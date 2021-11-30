package com.hlj.threadpool.d01Submit.D02SubmitFuture;

import java.util.concurrent.*;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/13  下午1:34.
 * 类描述：
 */
public class D01FutureisDone {

    private static final String SUCCESS = "success";


    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        System.out.println("线程开始执行");

        Future<String> future = executorService.submit(new D01FutureisDone().new TaskCallable());

        try {
            System.out.println("future.isDone()="+future.isDone());
            String s = future.get();
            if (SUCCESS.equals(s)) {
                System.out.println("future.isDone()"+future.isDone());
                System.out.println("线程执行结束");
            }
        } catch (InterruptedException e) {
            System.out.println("InterruptedException future.isDone()="+future.isDone());
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.out.println("ExecutionException future.isDone()="+future.isDone());
            e.printStackTrace();
        }

    }


class TaskCallable implements Callable<String> {


    @Override
    public String call() throws Exception {
        Thread.sleep(5000);
        int i = 1/0 ;
        System.out.println("submit方法执行任务完成" + "   thread name: " + Thread.currentThread().getName());
        return SUCCESS;
    }
}

}
