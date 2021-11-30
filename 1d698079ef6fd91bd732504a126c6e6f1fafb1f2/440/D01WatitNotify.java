package com.hlj.moudle.thread.d09wait;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午7:33.

 ```
 1）wait()、notify()和notifyAll()方法是本地方法，并且为final方法，无法被重写。

 2）调用某个对象的wait()方法能让当前线程阻塞，并且当前线程必须拥有此对象的monitor（即锁），调用某个对象的notify()方法，当前线程也必须拥有这个对象的monitor，因此调用notify()方法必须在同步块或者同步方法中进行（synchronized块或者synchronized方法）。

 3）调用某个对象的notify()方法能够唤醒一个正在等待这个对象的monitor的线程，如果有多个线程都在等待这个对象的monitor，则只能唤醒其中一个线程；具体唤醒哪个线程则不得而知。

 4）调用notifyAll()方法能够唤醒所有正在等待这个对象的monitor的线程；

 一个线程被唤醒不代表立即获取了对象的monitor，只有等调用完notify()或者notifyAll()并退出synchronized块，释放对象锁后，其余线程才可获得锁执行。

 */
public class D01WatitNotify {

    public final  static Object object = new Object() ;

    @Test
    public void testWati(){

        Thread thread = new Thread(()->{
            try {
                System.out.println("线程1");
                synchronized (object){
                    object.wait(1000);
                    System.out.println("wait");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        Thread thread2 = new Thread(()->{
            synchronized (object){
                object.notify(); //唤醒持有该对象的其他线程,执行完本同步代码块才会执行线程1
                System.out.println("notify");
            }

            System.out.println("线程2释放了锁");

        });

        thread.start();
        thread2.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**

         notify
         线程2释放了锁 //不排除第第三行输出
         wait

         */
    }

}
