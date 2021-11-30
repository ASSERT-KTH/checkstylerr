package com.hlj.redis.lock.service;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/13  上午11:28.
 */
public class CommonConsumerService {

    //库存个数
    static  int  goodsCount = 10;

    //卖出个数
    static  int saleCount = 0;

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                try {Thread.sleep(2);} catch (InterruptedException e) {}
                if (goodsCount > 0) {
                    goodsCount--;
                    System.out.println("剩余库存：" + goodsCount + " 卖出个数" + ++saleCount);
                }
            }).start();
        }
        Thread.sleep(3000);
    }

}