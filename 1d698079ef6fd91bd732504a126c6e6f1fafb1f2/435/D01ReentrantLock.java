package com.hlj.moudle.thread.D11ReentrantLock;

import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午8:57.
 * 类描述：reentrantlock 可重入锁
 */
public class D01ReentrantLock {

    public Lock lock = new ReentrantLock() ;
    int i = 0 ;
    @Test
    public void testReentrantlock(){

        Thread thread = new Thread(()->{

            for (int j = 1; j <= 20 ; j++) {
                lock.lock();
                  i++ ;
                lock.unlock();
            }

        });

        Thread thread2 = new Thread(()->{

            for (int j = 1; j <= 20 ; j++) {
                lock.lock();
                i++ ;
                lock.unlock();
            }

        });

        thread.start();
        thread2.start();
        try {
            Thread.sleep(1000);
            System.out.println(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


}
