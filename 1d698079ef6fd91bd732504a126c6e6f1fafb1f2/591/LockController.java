package com.hlj.redis.lock;

import com.hlj.redis.lock.utils.DistributedLock;
import com.hlj.redis.lock.utils.RedisTool;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/13  下午12:04.
 */
@RequestMapping("redis/lock")
@Controller
public class LockController {


    //库存个数
     int goodsCount = 10;

    //卖出个数
     int saleCount = 0;

    /**
     * 缓存key-用户体力锁
     */
    public static final String TEST_LOCK = "test_lock:";

    @Resource
    private DistributedLock lock;

    @GetMapping("test")
    @ResponseBody
    public void lockRedis(){
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                try {Thread.sleep(2);} catch (InterruptedException e) {}
                if (lock.lock(TEST_LOCK , 3000l, 5, 500)) {
                    if (goodsCount > 0) {
                        goodsCount--;
                        System.out.println("剩余库存：" + goodsCount + " 卖出个数" + ++saleCount);
                    }
                }
                lock.releaseLock(TEST_LOCK);

            }).start();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


}
