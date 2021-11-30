package com.hlj.redis.lock.utils;

/**
 * redis锁，原地址https://gitee.com/itopener/springboot/tree/master
 */
public interface DistributedLock {


    public static final long TIMEOUT_MILLIS = 30000; //超时时间

    public static final int RETRY_TIMES = Integer.MAX_VALUE; //重试次数

    public static final long SLEEP_MILLIS = 500; //重试时 线程休眠次数

    public boolean lock(String key);

    public boolean lock(String key, int retryTimes);

    public boolean lock(String key, int retryTimes, long sleepMillis);

    public boolean lock(String key, long expire);

    public boolean lock(String key, long expire, int retryTimes);

    public boolean lock(String key, long expire, int retryTimes, long sleepMillis);

    public boolean releaseLock(String key);
}
