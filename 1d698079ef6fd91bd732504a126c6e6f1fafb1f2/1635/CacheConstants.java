package com.hlj.moudle.cache.config;

/**
 * 类名称：CacheConstants
 * 类描述：缓存常量类
 * 创建人：HealerJean
 * 需要初始化的缓存定义名称需要以CACHE_为前缀。如：CACHE_XXX
 * 如果需要增加自定义过期时间，则增加过期时间变量定义EXPIRE_为前缀的缓存过期时间.如：EXPIRE_CACHE_XXX
 * 如不设置自定义过期时间即默认spring cache中设置过期时间
 *
 * @version 1.0.0
 */
public class CacheConstants {

    //公共缓存，

    public static final String CACHE_PUBLIC = "cacheSerializer.public";
    public static final Long EXPIRE_CACHE_PUBLIC = 60L;

    public static final String CACHE_PUBLIC_TEN_MINUTE = "cacheSerializer.public.ten.minute";
    public static final Long EXPIRE_CACHE_PUBLIC_TEN_MINUTE = 10 * 60L;

}


