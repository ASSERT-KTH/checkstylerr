package com.hlj.threadpool.d01Submit.D01Get;

import java.util.concurrent.*;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/3/13  上午11:16.
 * 类描述：
 */
public class RunnableSubmit {

    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(2);


        /**
         * submit(Runnable x) 返回一个future。可以用这个future来判断任务是否成功完成。请看下面：
         */
        Future future = pool.submit(new RunnableTest("Task2"));

        try {
            if(future.get()==null){//Runable接口的对象，这样当调用get方法的时候，如果线程执行成功会直接返回null，如果线程执行异常会抛出异常的信息
                System.out.println("任务完成");
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        } catch (ExecutionException e) {
            //否则我们可以看看任务失败的原因是什么
            System.out.println(e.getCause().getMessage());
        }

    }

}

class RunnableTest implements Runnable {

    private String taskName;

    public RunnableTest(final String taskName) {
        this.taskName = taskName;
    }

    @Override
    public void run() {
        System.out.println("Inside "+taskName);
    }

}

