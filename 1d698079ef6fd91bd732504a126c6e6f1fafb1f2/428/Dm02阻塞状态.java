package com.hlj.moudle.thread.d01线程状态;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/16  下午4:33.
 * 类描述：BLOCKED  synchronized对象或者类锁 可以造成线程阻塞状态
 */


public class Dm02阻塞状态 {

    public static void main(String[] args) {
        TaskBlockedObject blockedTask = new TaskBlockedObject() ;

       Thread thread1 =  new Thread(()->{
           blockedTask.offerThread1();
        },"线程1");


        Thread thread2  =  new Thread(()->{
            blockedTask.offerThread1();
        },"线程2");

        thread1.start();
        thread2.start();
        System.out.println(thread1.getName()+":"+thread1.getState()); // 线程1:RUNNABLE
        System.out.println(thread2.getName()+":"+thread2.getState()); // 线程2:BLOCKED
    }

}

class TaskBlockedObject {

    private static final Object object = new Object();

    /**
     * 提供给线程1进行调用
     */
    public void offerThread1(){
        synchronized (object){
            while (true){
            }
        }
    }

    /**
     * 提供给线程2进行调用
     */
    public void offerThread2(){
        synchronized (object){
            while (true){
            }
        }
    }

}
