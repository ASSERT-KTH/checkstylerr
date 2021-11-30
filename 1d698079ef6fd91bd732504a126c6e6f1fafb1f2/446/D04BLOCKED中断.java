package com.hlj.moudle.thread.d06Interupt线程中断;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午4:17.
 * 类描述： 阻塞状态下中断线程，并不会结束阻塞线程的运行，
 * 其实和运行状态下的场景是一样的，需要我们对阻塞线程的状态进行判断，然后去修改让它结束运行
 */
public class D04BLOCKED中断 {

    public static   synchronized void  task(){
        while (true){
        }
    }

    @Test
    public void BLOCKED中断(){

        Thread thread1 = new Thread(()->{

            task();
        });

        Thread thread2 = new Thread(()->{
            task();
        });


        thread1.start();
        try {
            Thread.sleep(1000); //确保线程1已经启动
            thread2.start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000); //确保上面的两个线程已经执行
            System.out.println("线程1状态"+thread1.getState()+"————————————");
            System.out.println("线程2状态"+thread2.getState()+"————————————");

            thread2.interrupt();
            System.out.println("线程1状态"+thread1.getState()+"————————————"+thread1.isInterrupted());
            System.out.println("线程2状态"+thread2.getState()+"————————————"+thread2.isInterrupted());

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        /**
         * 线程1状态RUNNABLE————————————
         * 线程2状态BLOCKED————————————
         * 线程1状态RUNNABLE————————————false
         * 线程2状态BLOCKED————————————true
         *
         *
         */



    }


}
