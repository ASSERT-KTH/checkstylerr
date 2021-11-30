package com.hlj.moudle.thread.d02死锁;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/16  下午5:13.
 * 类描述：过多的同步会造成死锁


 */
public class D01DeadLockThread implements Runnable {

    /**
     * 注意的的这里的final是 常亮。这样即使new两个对象，启动线程也是访问同一个资源
     线程thread1占有资源objectA，线程thread2占有资源objectB，
     当两个线程发出请求时，由于所请求的资源都在对方手中，从而发生线程阻塞，造成了线程的死锁。
     */
        private static final Object objectA = new Object();
        private static final Object objectB = new Object();
        private boolean flag;

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("当前线程 为：" + threadName + "\tflag = " + flag);
            if (flag) {
                synchronized (objectA) {
                    try {
                        Thread.sleep(1000); //等待其他线程执行
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println(threadName + "已进入同步代码块objectA，准备进入objectB");
                    synchronized (objectB) {
                        System.out.println(threadName + "已经进入同步代码块objectB");
                    }
                }

            } else {
                synchronized (objectB) {
                    try {
                        Thread.sleep(1000);//等待其他线程执行
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println(threadName + "已进入同步代码块objectB，准备进入objectA");
                    synchronized (objectA) {
                        System.out.println(threadName + "已经进入同步代码块objectA");
                    }
                }
            }
        }

        public static void main(String[] args) {
            D01DeadLockThread deadlock1 = new D01DeadLockThread();
            D01DeadLockThread deadlock2 = new D01DeadLockThread();

            deadlock1.flag = true;
            Thread thread1 = new Thread(deadlock1);

            deadlock2.flag = false;
            Thread thread2 = new Thread(deadlock2);

            thread1.start();
            thread2.start();

        }

    }
