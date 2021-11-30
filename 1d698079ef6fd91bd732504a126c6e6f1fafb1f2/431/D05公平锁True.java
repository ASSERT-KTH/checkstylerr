package com.hlj.moudle.thread.D11ReentrantLock;

import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午10:05.
 * 类描述：
 */
public class D05公平锁True {

    public ReentrantLock lock = new ReentrantLock(true) ;

    public void task() {
        while (true){
            lock.lock();
            System.out.println(Thread.currentThread().getName());
            lock.unlock();
        }
    }

    @Test
    public void test(){

        Thread thread = new Thread(()->{
            task();
        },"线程1");
        Thread thread2 = new Thread(()->{
            task();
        },"线程2");

        thread.start();
        thread2.start();
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
