package com.hlj.moudle.thread.d03线程优先级;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/16  下午5:47.
 *
 *  1、main的优先级，是普通的优先级
 *  2、线程优先级别thread1.setPriority
 *  3、线程优先级，并不能保证优先级高的先运行，也不保证优先级高的更多的分配CPU时间
 *    只是对系统的建议而已，到底运行哪个，是操作系统决定的，都不是java说了算的。
 *    线程的优先级仍然无法保障线程的执行次序。只不过，优先级高的线程获取CPU资源的概率较大，优先级低的并非没机会执行。
 *  4、少量的执行看不出来效果只有数量特别多的时候才会有效果
 *
 */
public class D01Priority {



    @Test
    public void testPriority() {

        Thread thread1 = new Thread(()->{
                System.out.println("正常优先级 counter1:");
        },"线程1");

        Thread thread2 = new Thread(()->{
                System.out.println("最高优先级 counter2:");
        },"线程2");

        Thread thread3 = new Thread(()->{
                System.out.println("最低优先级 counter3:");
        },"线程3");

        thread1.setPriority(Thread.NORM_PRIORITY); //线程1  正常优先级
        thread2.setPriority(Thread.MAX_PRIORITY);  //线程2  最高优先级
        thread3.setPriority(Thread.MIN_PRIORITY ); //线程3  最低优先级

        thread1.start();
        thread2.start();
        thread3.start();


    }


}
