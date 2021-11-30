package com.hlj.redis.redisTool;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/29  下午6:48.
 */
public class Readme {


    /*


方法	子API接口	描述
opsForValue()	ValueOperations	     描述具有简单值的条目
opsForList()	ListOperations	     操作具有list值的条目
opsForSet()	    SetOperations	     操作具有set值的条目
opsForZSet()	ZSetOperations	     操作具有ZSet值（排序的set）的条目
opsForHash()	HashOperations	     操作具有hash值的条目
boundValueOps(K)	BoundValueOperations	以绑定指定key的方式，操作具有简单值的条目
boundListOps(K)	BoundListOperations	以绑定指定key的方式，操作具有list的条目
boundSetOps(K)	BoundSetOperations	以绑定指定key的方式，操作具有set的条目
boundZSet(K)	BoundZSetOperations	以绑定指定key的方式，操作具有ZSet（排序的set）的条目
boundHashOps(K)	BoundHashOperations	以绑定指定key的方式，操作具有hash值的条目




## 1、
stringRedisTemplate.opsForValue().set("test", "100",60*10,TimeUnit.SECONDS);//向redis里存入数据和设置缓存时间

stringRedisTemplate.opsForValue().get("test")//根据key获取缓存中的val

stringRedisTemplate.expire("red_123",1000 , TimeUnit.MILLISECONDS);//设置过期时间


stringRedisTemplate.hasKey("546545");//检查key是否存在，返回boolean值


## 2
stringRedisTemplate.delete("test");//根据key删除缓存

stringRedisTemplate.getExpire("test")//根据key获取过期时间

stringRedisTemplate.getExpire("test",TimeUnit.SECONDS)//根据key获取过期时间并换算成指定单位



//以绑定指定key的方式，通过key获取设置属性
stringRedisTemplate.boundValueOps("test").increment(-1);//val做-1操作

stringRedisTemplate.boundValueOps("test").increment(1);//val +1

 stringRedisTemplate.boundValueOps(key).expire(seconds, TimeUnit.SECONDS);


set

stringRedisTemplate.opsForSet().add("red_123", "1","2","3");//向指定key中存放set集合

stringRedisTemplate.opsForSet().members("red_123");//根据key获取set集合
//用户是否在线
stringRedisTemplate.opsForSet().isMember("red_123", "1")//根据key查看集合中是否存在指定数据







    */
}
