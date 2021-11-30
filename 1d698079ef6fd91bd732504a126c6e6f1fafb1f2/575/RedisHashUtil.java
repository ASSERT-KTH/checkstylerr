package com.hlj.redis.redisTool.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/9/19  下午2:03.
 */
@Slf4j
@Service
public class RedisHashUtil {

    @Resource
    private RedisTemplate<String,Object> redisTemplate ;

    /**
     * 添加集合 举例 key  hkey ,hvalue hkey1,hvalue2
     * @param key
     */
    public void putAll(String key, Object... objects) {

        if(objects.length % 2 != 0){
            throw new IllegalArgumentException("parameter须为key-value对应参数");
        }
        Map<String,Object> map = new HashMap<String, Object>();
        for (int i = 0; i < objects.length; i += 2) {
            map.put(objects[i].toString(),objects[i + 1]);
        }
            //添加单个 redisTemplate.opsForHash().put(key,hKey , hValue);

        redisTemplate.opsForHash().putAll(key,map);
    }


    /**
     * 获取单个hash key value
     */
    public Object get(String key,String hKey){
        //      二者都可以使用
        // return    redisTemplate.boundHashOps(key).get(hKey);
      return  redisTemplate.opsForHash().get(key,hKey );
    }


    /**
     * 获取所有的hash集合
     * @param key
     * @return
     */
    public Map<String ,Object> getAll(String key){
        Map<String ,Object> map = new HashMap<>();
        Set<Object> hKeys = redisTemplate.opsForHash().keys(key);
        hKeys.stream().forEach(hkey->{
            map.put(hkey.toString(), redisTemplate.opsForHash().get(key,hkey ));
        });

        return  map ;
    }

    /**
     * 判断hKey是否存在
     */
    public boolean isExists(String key,String hKey){
        return   redisTemplate.opsForHash().hasKey(key,hKey);
    }


      /**
     * 删除hash中的某个值
     */
    public void deleteHkey(String key,String hKey){
        redisTemplate.opsForHash().delete(key,hKey);
    }


    /**
     * 过期时间
     * @param key
     * @param time
     * @param timeUnit
     */
    public void setExpire(String key,Long time,TimeUnit timeUnit){
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
