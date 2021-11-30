package com.hlj.moudle.thread.d07Join等待程序执行结束;

import org.junit.Test;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/17  下午4:51.

    主线程，等待异步线程执行结束
    Join等待线程执行结束
 */
public class D01Join {



    @Test
    public void testJoin(){

        Thread thread = new Thread(()->{

            for (int i = 0; i < 10 ; i++) {
                System.out.print(i+"_");
            }
            
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\n主程序结束运行");


        /**
         *
         0_1_2_3_4_5_6_7_8_9_
         主程序结束运行


         */



    }



}
