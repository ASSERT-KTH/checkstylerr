package com.hlj.Ehcache.config;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCache;
import org.springframework.cache.ehcache.EhCacheCacheManager;

import java.lang.reflect.Field;
import java.util.*;

/**
 *  类描述：继承 EhCacheCacheManager
 * @Description 初始化缓存。加载@CacheConstants中缓存定义及缓存自定义过期时间
 * @Date   2018/3/21 下午2:46.
 */
public class CustomEhCacheManager extends EhCacheCacheManager {

    private static String CACHE_PREFIX = "CACHE_";
    private static String EXPIRE_PREFIX = "EXPIRE_";
    private static Logger logger = LoggerFactory.getLogger(CustomEhCacheManager.class);

    private static List<String> cacheNames = new ArrayList<>();
    private static Map<String,Long> expires = new HashMap<>();

    @Override
    public void afterPropertiesSet() {
        try {
            Class clazz = CacheConstants.class;
            Field[] fields = clazz.getDeclaredFields();
            for(Field field : fields){
                if (field.getName().startsWith(CACHE_PREFIX)){
                    cacheNames.add(field.get(clazz).toString());
                } else if (field.getName().startsWith(EXPIRE_PREFIX)){
                    expires.put(
                            clazz.getField(field.getName().replace(EXPIRE_PREFIX,"")).get(clazz).toString(),
                            Long.parseLong(field.get(clazz).toString())
                    );
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException("init cacheSerializer failure!",e);
        }
        super.afterPropertiesSet();
    }

    @Override
    protected Collection<Cache> loadCaches() {
        LinkedHashSet<Cache> caches = new LinkedHashSet<Cache>(cacheNames.size());
        for(String name:cacheNames){
            Ehcache exist = this.getCacheManager().addCacheIfAbsent(name);
            if(exist != null){
                Cache cache = new EhCacheCache(exist);
                Ehcache ehcache = (Ehcache) cache.getNativeCache();
                CacheConfiguration configuration = ehcache.getCacheConfiguration();
                Long time = expires.get(name);
                configuration.setTimeToIdleSeconds(time);
                configuration.setTimeToLiveSeconds(time);
                caches.add(cache);
            }
        }
        return caches;
    }
}
