package com.hlj.Ehcache.config;

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

    //公共缓存，1分钟过期时间
    public static final String CACHE_PUBLIC_PERSON = "cacheSerializer.public.person";
    public static final Long EXPIRE_CACHE_PUBLIC_PERSON = 60L;


    public static final String CACHE_PUBLIC = "cacheSerializer.public";
    public static final Long EXPIRE_CACHE_PUBLIC = 60L;

    public static final String CACHE_PUBLIC_TEN_MINUTE = "cacheSerializer.public.ten.minute";
    public static final Long EXPIRE_CACHE_PUBLIC_TEN_MINUTE = 10 * 60L;

    public static final String CACHE_PUBLIC_HOUR = "cacheSerializer.public.hour";
    public static final Long EXPIRE_CACHE_PUBLIC_HOUR = 60 * 60L;

    public static final String CACHE_PUBLIC_DAY = "cacheSerializer.public.day";
    public static final Long EXPIRE_CACHE_PUBLIC_DAY = 60 * 60L * 24L;

    public static final String CACHE_AD_SHOW = "cacheSerializer.ad.show";
    public static final Long EXPIRE_CACHE_AD_SHOW = 60L;

    public static final String CACHE_AD_DISPLAY = "cacheSerializer.ad.display";
    public static final Long EXPIRE_CACHE_AD_DISPLAY = 60L;

    public static final String CACHE_APP = "cacheSerializer.app";
    public static final Long EXPIRE_CACHE_APP = 60L;

    public static final String CACHE_ADUNIT_TEST = "cacheSerializer.adunit.test";
    public static final Long EXPIRE_CACHE_ADUNIT_TEST = 60L;

    public static final String CACHE_SPREAD_AD_IDFA_TEST = "cacheSerializer.spread.ad.idfa.test";
    public static final Long EXPIRE_CACHE_SPREAD_AD_IDFA_TEST = 60L;

    public static final String CACHE_SPREAD_AD_TEST = "cacheSerializer.spread.ad.test";
    public static final Long EXPIRE_CACHE_SPREAD_AD_TEST = 60L;

    public static final String CACHE_ADUNIT = "cacheSerializer.adunit";
    public static final Long EXPIRE_CACHE_ADUNIT = 60L;

    public static final String CACHE_AD_GROUP = "cacheSerializer.ad.group";
    public static final Long EXPIRE_CACHE_AD_GROUP = 60L;

    public static final String CACHE_AD_HOUR_LIST = "cacheSerializer.ad.hour.list";
    public static final Long EXPIRE_CACHE_AD_HOUR_LIST = 60L;

    public static final String CACHE_AD_HOUR_MAP = "cacheSerializer.ad.hour.map";
    public static final Long EXPIRE_CACHE_AD_HOUR_MAP = 60L;

    public static final String CACHE_AD_OWN_URL = "cacheSerializer.ad.own.url";
    public static final Long EXPIRE_CACHE_AD_OWN_URL = 60L;

    public static final String CACHE_AD_SPREAD = "cacheSerializer.ad.spread";
    public static final Long EXPIRE_CACHE_AD_SPREAD = 300L;

    public static final String CACHE_AD_TRACK = "cacheSerializer.ad.track";
    public static final Long EXPIRE_CACHE_AD_TRACK = 300L;

    public static final String CACHE_AD_DISPLAY_SETTING = "cacheSerializer.ad.display.setting";
    public static final Long EXPIRE_CACHE_AD_DISPLAY_SETTING = 60L;

    public static final String CACHE_TEST = "cacheSerializer.test";
    public static final Long EXPIRE_CACHE_TEST = 60L;

    public static final String CACHE_EMAIL_SEND = "cacheSerializer.email.send";
    public static final Long EXPIRE_CACHE_EMAIL_SEND = 24 * 60L * 60L;

    public static final String CACHE_AD_SHOW_FLAG = "cacheSerializer.ad.show.flag";
    public static final Long EXPIRE_CACHE_AD_SHOW_FLAG = 60L;

    public static final String CACHE_MAIN_PAGE = "cacheSerializer.main.page";
    public static final Long EXPIRE_CACHE_MAIN_PAGE = 60L * 5L;

    public static final String CACHE_MOBILE = "cacheSerializer.mobile";
    public static final Long EXPIRE_CACHE_MOBILE = 60L;

    public static final String CACHE_MONITOR_APP = "cacheSerializer.monitor.app";
    public static final Long EXPIRE_CACHE_MONITOR_APP = 60L * 60L * 6;

    public static final String CACHE_MONITOR_APP_KEYWORD = "cacheSerializer.monitor.app.keyword";
    public static final Long EXPIRE_CACHE_MONITOR_APP_KEYWORD = 60L * 60L * 6;


    public static final String CACHE_INTERACTION_MEDIA = "cacheSerializer.interaction.media";
    public static final Long EXPIRE_CACHE_INTERACTION_MEDIA = 60L * 60L * 24;

    public static final String CACHE_INTERACTION_ADUNIT = "cacheSerializer.interaction.adunit";
    public static final Long EXPIRE_CACHE_INTERACTION_ADUNIT = 60L * 60L * 24;

    public static final String CACHE_INTERACTION_SHIELD = "cacheSerializer.interaction.shield";
    public static final Long EXPIRE_CACHE_INTERACTION_SHIELD = 60L * 5;

    public static final String CACHE_APPS_COOPERATE_CONFIG = "cacheSerializer.apps.cooperate.config";
    public static final Long EXPIRE_CACHE_APPS_COOPERATE_CONFIG = 60L * 60L * 24;


    public static final String CACHE_SPREAD_IDS = "cacheSerializer.spread.ids";
    public static final Long EXPIRE_CACHE_SPREAD_IDS = 30L;

    public static final String CACHE_SPREAD_LIST = "cacheSerializer.spread.list";
    public static final Long EXPIRE_CACHE_SPREAD_LIST = 60L;

    public static final String CACHE_ADMIN_USERACCOUNT_MENU = "cacheSerializer.admin.userAccount.menu";
    public static final Long EXPIRE_CACHE_ADMIN_USERACCOUNT_MENU = 60L;

    public static final String CACHE_ADMIN_USERID_MENU = "cacheSerializer.admin.userId.menu";
    public static final Long EXPIRE_CACHE_ADMIN_USERID_MENU = 60L;
}
