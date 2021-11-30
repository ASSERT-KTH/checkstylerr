package com.hlj.thread.comhljthread.quequ;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/28

　　LinkedBlockingQueue是一个线程安全的阻塞队列，实现了先进先出等特性，是作为生产者消费者的首选，
    可以指定容量，也可以不指定，不指定的话默认最大是Integer.MAX_VALUE，
　　其中主要用到put和take方法，
   1、put方法将一个对象放到队列尾部，在队列满的时候会阻塞直到有队列成员被消费，
   2、take方法从head取一个对象，在队列为空的时候会阻塞，直到有队列成员被放进来

 */

public class BlockingQueueTest extends Thread {

    public static BlockingQueue<String> queue = new LinkedBlockingQueue<String>(4);

    private int index;

    public BlockingQueueTest(int i) {
        this.index = i;
    }

    public void run() {
        try {
            //把anObject加到BlockingQueue里，如果BlockingQueue没有空间，则调用此方法的线程被阻断直到BlockingQueue里有空间再继续。
            queue.put(String.valueOf(this.index));
            System.out.println(this.index + ":" + Thread.currentThread().getName());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String args[]) {
        ExecutorService service = Executors.newCachedThreadPool();
        for(int i = 0; i < 10; i++) {
            service.submit(new BlockingQueueTest(i));
        }
        Thread thread = new Thread() {

            public void run() {
                try {
                    while(true) {
                        Thread.sleep((int)(Math.random() * 1000));
                        if(BlockingQueueTest.queue.isEmpty()){
                            break;
                        }
                        //获取BlockingQueue里排在首位的对象，若BlockingQueue为空，阻断进入等待状态直到BlockingQueue有新的对象被加入为止。
                        String str = BlockingQueueTest.queue.take();
                        System.out.println("take {" + str + "} out of queue!");
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        service.submit(thread);
        service.shutdown();
    }


}