package com.hlj.redis.lock.service;

import com.hlj.redis.lock.utils.RedisTool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/13  上午11:32.
 */
public class RedisLockConsumerService {

    //库存个数
    static int goodsCount = 10;

    //卖出个数
    static int saleCount = 0;

    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "127.0.0.1", 6379, 1000);

        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                try {Thread.sleep(2);} catch (InterruptedException e) {}
                Jedis jedis = jedisPool.getResource();
                boolean lock = false;
                while (!lock) {
                    lock = RedisTool.tryGetDistributedLock(jedis, "goodsCount", Thread.currentThread().getName(), 10);
                }
                if (lock) {
                    if (goodsCount > 0) {
                        goodsCount--;
                        System.out.println("剩余库存：" + goodsCount + " 卖出个数" + ++saleCount);
                    }
                }
                RedisTool.releaseDistributedLock(jedis, "goodsCount", Thread.currentThread().getName());
                jedis.close();
            }).start();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jedisPool.close();
    }
}
