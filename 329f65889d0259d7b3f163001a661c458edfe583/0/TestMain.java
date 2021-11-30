package com.healerjean.proj.cache.avengers.H01_hyperloglog;

import com.healerjean.proj.cache.avengers.H01_hyperloglog.dto.Experiment;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import redis.clients.jedis.Jedis;

/**
 * @author zhangyujin
 * @date 2021/4/30  11:57 上午.
 * @description
 */
@Slf4j
public class TestMain {


    /**
     * 1、统计1000，多久出现不一致
     */
    @Test
    public void test1000() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        for (int i = 0; i < 1000; i++) {
            jedis.pfadd("codehole", "user" + i);

            long total = jedis.pfcount("codehole");
            if (total != i + 1) {
                log.info("total: {}, actually: {}", total, i + 1);
                break;
            }
        }
        jedis.close();
    }


    /**
     * 2、统计10w
     */
    @Test
    public void test1000000() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        for (int i = 0; i < 100000; i++) {
            jedis.pfadd("codehole", "user" + i);
        }
        long total = jedis.pfcount("codehole");
        log.info("total: {}, actually: {}", total, 100000);
        jedis.close();
    }



    @Test
    public void test(){
        for (int i = 1000; i < 100000; i += 100) {
            Experiment exp = new Experiment(i);
            exp.work();
            exp.debug();
        }
    }

}
