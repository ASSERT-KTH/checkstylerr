package com.hlj.moudle.thread.D10线程组;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午8:13.
 * 类描述：线程组
 */
public class D01ThreadGroup {


    @Test
    public void testThreadGrpup(){

        /**
         * 每个线程都有线程组，默认是 main
         */
        System.out.println(Thread.currentThread().getThreadGroup().getName());

        ThreadGroup tg = new ThreadGroup("PrintGroup");

        Thread thread1= new Thread(tg,()->{
            while (true){

            }
        },"线程1");


        Thread thread2 = new Thread(tg,()->{
            while (true){
            }
        },"线程2");

        thread1.start();
        thread2.start();
        System.out.println(tg.activeCount()); //返回此线程组中活动线程的估计数。


    }

}
