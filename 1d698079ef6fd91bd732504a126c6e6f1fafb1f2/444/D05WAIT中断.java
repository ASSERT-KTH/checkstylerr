package com.hlj.moudle.thread.d06Interupt线程中断;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午4:28.
 *
 这两种状态本质上是同一种状态，只不过TIMED_WAITING在等待一段时间后会自动释放自己，
 而WAITING则是无限期等待，需要其他线程调用notify方法释放自己。

 但是他们都是线程在运行的过程中由于缺少某些条件而被挂起在某个对象的等待队列上。
 当这些线程遇到中断操作的时候，会抛出一个InterruptedException异常，并清空中断标志位。例如：

 */
public class D05WAIT中断 {

    @Test
    public void BLOCKED中断() {

        Thread thread = new Thread(() -> {
            try {
                synchronized (this){
                    System.out.println(Thread.currentThread().getState() + "中断状态" + Thread.currentThread().isInterrupted());
                    wait();
                    System.out.println("wait");
                }
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getState() + "中断状态" + Thread.currentThread().isInterrupted());
            }
        });

        thread.start();
        try {
            Thread.sleep(1000); //确保线程1已经启动
            thread.interrupt();
        } catch (InterruptedException e) {
        }


        /**

         RUNNABLE中断状态false
         RUNNABLE中断状态false

         */


    }
}
