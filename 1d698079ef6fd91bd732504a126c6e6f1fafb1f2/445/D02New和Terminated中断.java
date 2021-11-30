package com.hlj.moudle.thread.d06Interupt线程中断;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午3:42.
 * 类描述： 测试中断状态
 */
public class D02New和Terminated中断 {


    /**
     * 线程的new状态表示还未调用start方法，还未真正启动。线程的terminated状态表示线程已经运行终止。
     * 这两个状态下调用中断方法来中断线程的时候，Java认为毫无意义，
     * 所以并不会设置线程的中断标识位，什么事也不会发生。
     *
     * 返回中断状态为false
     */
    @Test
    public void 测试new中断(){

        Thread thread = new Thread(()->{

        });

        //new新建状态 返回 false
        System.out.println("线程状态:"+thread.getState()+"-中断状态"+thread.isInterrupted());

        //此时线程还没启动 就进行中断，其实毫无意义
        thread.interrupt();
        System.out.println("中断状态"+thread.isInterrupted());

        //启动线程
        thread.start();

        try {
            Thread.sleep(3000);//确保线程已经执行完毕
            System.out.println("线程状态:"+thread.getState()+"-中断状态"+thread.isInterrupted());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /** 打印结果

         线程状态:NEW-中断状态false
         中断状态false
         线程状态:TERMINATED-中断状态false

         */


    }

}
