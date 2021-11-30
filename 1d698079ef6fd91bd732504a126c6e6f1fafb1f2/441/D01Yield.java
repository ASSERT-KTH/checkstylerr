package com.hlj.moudle.thread.d04yield谦让;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/16  下午6:14.
 * 类描述：

    Java线程中有一个Thread.yield()方法，把自己CPU执行的时间让掉，让自己或者其它的线程运行。（也就是谁先抢到谁执行）

 */
public class D01Yield {


    @Test
    public  void testYield(){
        Thread thread1 = new Thread(()->{
            task();
        },"线程1");

        Thread thread2 = new Thread(()->{
            task();
        },"线程2");

        thread1.start();
        thread2.start();

    }

    public void task(){
        for (int i = 1; i <= 50; i++) {
            // 当i为30时，该线程就会把CPU时间让掉，让其他或者自己的线程执行（也就是谁先抢到谁执行）
            if (i == 30) {
                Thread.currentThread().yield();
                //下面的将不会打印，相当于把当前线程让掉了
                System.out.println("" + Thread.currentThread().getName() + "-----" + i);
            }else {
                System.out.println("" + Thread.currentThread().getName() + "-----" + i);
            }
        }
    }
}
