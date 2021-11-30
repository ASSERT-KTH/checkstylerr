package com.hlj.moudle.thread.D11ReentrantLock;

import org.junit.Test;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午9:11.

 lockInterruptibly（） 如果当前线程未被中断，获取锁
 isHeldByCurrentThread() 当前线程是否保持锁锁定，线程的执行lock方法的前后分别是false和true

 */
public class D02中断lockInterruptibly {

//    不可以用下面的接口喽，因为它没有lockInterruptibly 方法
//    public Lock lock = new ReentrantLock() ;

    public ReentrantLock lock1 = new ReentrantLock() ;
    public ReentrantLock lock2= new ReentrantLock() ;

    int i = 0 ;

    @Test
    public void 线程中断(){
        Thread thread1 = new Thread(()->{
            i = 1 ;
            task();
        },"线程1");
        Thread thread2 = new Thread(()->{
            i = 2 ;
            task();
        },"线程2");

        thread1.start();
        thread2.start();

        try {
            Thread.sleep(1000); //等待线程1和线程2分到到了获取各自锁的地方，然后执行下面的中断
            thread2.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void task( ) {
        try {
            if ( i== 1) {
                lock1.lockInterruptibly();
                Thread.sleep(500); //等待线程2执行
                lock2.lockInterruptibly();
                System.out.println("111111");
            } else {
                lock2.lockInterruptibly();
                Thread.sleep(500);//等待线程1执行
                lock1.lockInterruptibly();
                System.out.println("2222222");

            }

        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName()+"中断了，但是这里跑出了InterruptedException异常，所以释放了");
        } finally {
            //判断是否拥有锁，先进来的先把锁解开了
            if (lock1.isHeldByCurrentThread()) {
                lock1.unlock();
            }
            if (lock2.isHeldByCurrentThread()) {
                lock2.unlock();
            }
            System.out.println(Thread.currentThread().getName() + ":线程退出");
        }

    }


    /**
     线程2中断了，但是这里跑出了InterruptedException异常，所以释放了
     线程2:线程退出
     111111
     线程1:线程退出
     */
}
