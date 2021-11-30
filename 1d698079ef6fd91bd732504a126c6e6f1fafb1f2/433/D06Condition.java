package com.hlj.moudle.thread.D11ReentrantLock;

import org.junit.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午10:11.
 *
 * 1、await()方法会让当前线程等待，同事释放当前锁，，当其他线程使用signal（或者signalAll（）方法时，先回重新获得锁，并继续执行
 * 2、singal（）方法用于唤醒一个再线程中等待的线程，，这方法在使用的时候，也要求线程，先获得相关锁，然后唤醒线程，在singal之后，要释放掉相关锁，谦让给被唤醒的线程，让它可以继续执行。
 *
 */
public class D06Condition {

    public Lock lock = new ReentrantLock(true) ;
    public Condition condition = lock.newCondition();


    public void task() {
        lock.lock();
        try {
            condition.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName());
        lock.unlock();
    }

    @Test
    public void test(){

        Thread thread = new Thread(()->{
            task();
        },"线程1");

        thread.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        lock.lock();
        condition.signal();
        lock.unlock();


        /**
         * 打印结果
         * 线程1
         */

    }

}
