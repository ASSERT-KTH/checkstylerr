package com.hlj.moudle.thread.d06Interupt线程中断;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/16  下午6:49.

 严格 讲，线程中断不会使线程立即退出，而是给线程发送一个通知（抛出了异常），告诉目标线程，有人希望你退出，
         后续目前线程接到通知怎么处理，是线程自己的事情了

    像下面这种需要捕获中断异常的，中断标志位以及被清空了了，也就是说在catch中捕获的 查看状态为false

 */
public class D01简单测试 {

    @Test
    public void test简单测试(){

        Thread thread = new Thread(()->{

            try {
                System.out.println("1、线程开始执行");
                Thread.sleep(5000);
                System.out.println("2、线程结束休眠");
            } catch (InterruptedException e) {
                System.out.println("3、线程中断休眠");
                System.out.println(Thread.currentThread().getState() + "中断状态" + Thread.currentThread().isInterrupted());
                return;
            }
            System.out.println("4、线程正在运行");

        });

        thread.start();
        try {
            Thread.sleep(1000);//准备进行中断，让上面的线程确保已经执行
            thread.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
