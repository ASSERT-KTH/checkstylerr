package com.hlj.threadpool.d01Submit.D01Get;

import java.util.concurrent.*;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/13  上午11:43.
 * 类描述：
 */
public class CallableSubmit {

    private static final String SUCCESS = "success";


    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        System.out.println("线程开始执行");

        Future<String> future = executorService.submit(new CallableSubmit().new TaskCallable());
        try {select shop, item, max(volumn) max_volumn from sales s1 where s1.volumn = (select max(volumn) from sales s2 where s1.shop = s2.shop)group by shop;

            String s = future.get();
            if (SUCCESS.equals(s)) {
                System.out.println("线程执行结束");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
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
