package com.hlj.Ehcache.config;

/**
 * @Desc:
 * @Author HealerJean
 * @Date 2018/8/17  上午11:34.
 */
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 类名称：EhCacheObjectData
 * 类描述：ehcache 对象数据存储
 *
 */
@Component
public class EhCacheObjectData {

    @Resource
    private CustomEhCacheManager customEhCacheManager;

    private CacheManager cacheManager;

    @PostConstruct
    public void init() {
        cacheManager = customEhCacheManager.getCacheManager();
    }

    public Object getData(String cacheName,String key){
        Cache cache = cacheManager.getCache(cacheName);
        if(cache == null ) return null;
        Element element = cache.get(key);
        if(element != null) return element.getObjectValue();
        return null;
    }

    public void setData(String cacheName,String key,Object data){
        Cache cache = cacheManager.getCache(cacheName);
        if(cache != null ) cache.put(new Element(key,data));
    }

    /**
     * 删除缓存下的key
     * @param cacheName
     * @param key
     * @return
     */
    public boolean delete(String cacheName,String key){
        Cache cache = cacheManager.getCache(cacheName);
        if(cache != null ) {
            return cache.remove(key);
        }
        return false;
    }
}
