package com.hlj.moudle.thread.d09wait;

import org.junit.Test;

import java.util.PriorityQueue;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午7:58.
 * 类描述：
 */
public class D01生产者和消费者 {

    private int queueSize = 100;
    private PriorityQueue<Integer> queue = new PriorityQueue<Integer>(queueSize);

    @Test
    public void test()  {

        Thread producer = new Thread(()->{
            while(true){
                synchronized (queue) {
                    while(queue.size() == queueSize){
                        try {
                            System.out.println("队列满，等待有空余空间");
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    queue.offer(1);        //每次插入一个元素
                    queue.notify();           //有数据了，防止消费者一直在等待没有数据获取
                    System.out.println("向队列取中插入一个元素，队列剩余空间："+(queueSize-queue.size()));
                }
            }
        });

        Thread consumer  = new Thread(()->{
            while(true){
                synchronized (queue) {
                    while(queue.size() == 0){
                        try {
                            System.out.println("队列空，等待数据");
                            queue.wait(); //持续执行while，等待当前线程
                        } catch (InterruptedException e) {//如果当前线程出现了中断
                            e.printStackTrace();
                        }
                    }
                    queue.poll();          //每次移走队首元素
                    queue.notify();        //数据消耗了，防止生产者队列满了数据获取不到
                    System.out.println("从队列取走一个元素，队列剩余"+queue.size()+"个元素");
                }
            }
        });

        producer.start();
        consumer.start();
    }

}
