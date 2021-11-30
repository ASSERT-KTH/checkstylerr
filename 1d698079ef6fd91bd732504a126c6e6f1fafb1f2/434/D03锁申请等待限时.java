package com.hlj.moudle.thread.D11ReentrantLock;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午9:37.
 * 类描述：
 */
public class D03锁申请等待限时 {

    public ReentrantLock lock = new ReentrantLock() ;

    public void task() {
        try {//等待时长，计时单位
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                System.out.println(Thread.currentThread().getName());
                System.out.println("get lock success");
                Thread.sleep(6000); //占用线程6秒，哈哈，肯定大于5秒了，所以两个
            } else {
                System.out.println(Thread.currentThread().getName());
                System.out.println("get lock failed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Test
    public void testReentrantlock(){

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


    /**
     线程2
     get lock success
     线程1
     get lock failed
     */



}
