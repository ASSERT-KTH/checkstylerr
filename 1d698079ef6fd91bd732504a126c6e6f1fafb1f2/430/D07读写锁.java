package com.hlj.moudle.thread.D11ReentrantLock;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午10:31.
 * 类描述：
 */
public class D07读写锁 {

    private static Lock lock = new ReentrantLock();
    private static ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private static Lock readLock = reentrantReadWriteLock.readLock();
    private static Lock writeLock = reentrantReadWriteLock.writeLock();
    private static int value;

    public static Object handleRead(Lock lock) throws InterruptedException {
        try {
            lock.lock();
            Thread.sleep(1000);// 模拟读操作
            System.out.println("读操作:" + value);
            return value;
        } finally {
            lock.unlock();
        }
    }

    public static void handleWrite(Lock lock, int index)
            throws InterruptedException {
        try {
            lock.lock();
            Thread.sleep(1000);// 模拟写操作
            System.out.println("写操作:" + value);
            value = index;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        TestReadThread testReadThread = new TestReadThread();
        TestWriteThread testWriteThread = new TestWriteThread();
        for (int i = 0; i < 18; i++) {
            new Thread(testReadThread).start();
        }
        for (int i = 18; i < 20; i++) {
            new Thread(testWriteThread).start();
        }

    }

    private static class TestReadThread extends Thread {
        @Override
        public void run() {
            try {
                //Test.handleRead(lock);
                D07读写锁.handleRead(readLock);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class TestWriteThread extends Thread {
        @Override
        public void run() {
            try {
                //Test.handleWrite(lock,new Random().nextInt(100));
                D07读写锁.handleWrite(writeLock,new Random().nextInt(100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
