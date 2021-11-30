package com.hlj.redis.redisTool.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/11  下午1:58.
 * 类描述：
 */
@Service
@Slf4j
public class RedisListUtil {

    @Resource
    private RedisTemplate<String,Object> redisTemplate ;


    /**
     * 左进 （栈顶进）
     * @param key
     */
    public  void lpush(String key, Object... objects) {
        //放入一个
//      redisTemplate.opsForList().leftPush(key, objects[0]);
        redisTemplate.opsForList().leftPushAll(key, objects);//数据项中不不能为空
    }

    /**
     * 右面出
     * @param key
     * @return
     */
    public Object rpop(String key) {
        System.out.println(redisTemplate.opsForList().rightPop(key));
        String o = redisTemplate.opsForList().rightPop(key).toString();
        Object ob = redisTemplate.opsForList().rightPop(key);

        return o;
    }

    /**
     * 右面出 有限等待
     * @param key
     * @param time 超时时间
     * @param timeUnit 日期格式
     * @return
     */
    public Object brpop(String key, Long time,TimeUnit timeUnit) {
        return redisTemplate.opsForList().rightPop(key,time,timeUnit);
    }


    /**
     * 右面放入
     * @param key
     * @param objects
     */
    public void rpush(String key, Object... objects) {
        redisTemplate.opsForList().rightPushAll(key, objects);
    }



    /**
     * 左面出
     * @param key
     * @return
     */
    public Object lpop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 左面出 有限等待
     * @param key
     * @param time 超时时间
     * @param timeUnit 日期格式
     * @return
     */
    public Object blpop(String key, Long time,TimeUnit timeUnit) {
        return redisTemplate.opsForList().leftPop(key,time,timeUnit);
    }



    /**
     * 过期时间
     * @param key
     * @param time
     * @param timeUnit
     */
    public void setExpire(String key, Long time, TimeUnit timeUnit){
        redisTemplate.expire(key,time,timeUnit);
    }


    /**
     * 根据key 删除
     * @param key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }






}
